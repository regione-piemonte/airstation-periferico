/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for scalar elements
// Change log:
//   2008-08-06: initial version
// ----------------------------------------------------------------------------
// $Id: ScalarElement.java,v 1.19 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Base class for scalar elements
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ScalarElement extends Element {

	private static final long serialVersionUID = -6440033249741671651L;

	private static Logger logger = Logger.getLogger("periferico."
			+ ScalarElement.class.getSimpleName());

	private String measureUnitName = "";

	private double minValue = 0.0;

	private double maxValue;

	private int numDec = 1;

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public String getMeasureUnitName() {
		return measureUnitName;
	}

	public void setMeasureUnitName(String measureUnitName) {
		this.measureUnitName = trim(measureUnitName);
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public int getNumDec() {
		return numDec;
	}

	public void setNumDec(int numDec) {
		this.numDec = numDec;
	}

	public void setConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals)
			throws ConfigException {
		checkValidationRange(minValue, maxValue);
		checkNumDec(numDecimals);
		super.setConfig(enabled);
		setMeasureUnitName(measureUnitName);
		setMinValue(minValue);
		setMaxValue(maxValue);
		setNumDec(numDecimals);
	}

	public boolean isSameConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals) {
		return super.isSameConfig(enabled)
				&& this.measureUnitName.equals(trim(measureUnitName))
				&& this.minValue == minValue && this.maxValue == maxValue
				&& this.numDec == numDecimals;
	}

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
		checkValidationRange(minValue, maxValue);
		checkNumDec(numDec);
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		// NOTE: measureUnitName is null for wind direction, because it cannot
		// be specified by the user
		if (measureUnitName != null
				&& cc.getMeasureUnit(measureUnitName) == null)
			throw new ConfigException("measureunit_not_in_cc", measureUnitName);
	}

	private void checkValidationRange(double minValue, double maxValue)
			throws ConfigException {
		if (minValue > maxValue)
			throw new ConfigException("validation_error");
	}

	private void checkNumDec(int numDecimals) throws ConfigException {
		if (numDecimals < 0)
			throw new ConfigException("num_dec_should_be_gt_0");
	}

	abstract ScalarAggregateValue computeAggregateValue(
			int aggregationPeriod_m, Date aggregationTime, List<Sample> data);

	@Override
	void computeAndSaveAggregateValues(Date dataManagerStartTime,
			Date aggregationComputeEndTime) {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(aggregationComputeEndTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_MONTH,
				-DataManager.AGGREGATIONS_COMPUTE_TIME_OFFSET);
		Date aggregComputeMinimumTime = calendar.getTime();
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(aggregationComputeEndTime);
		startCal.add(Calendar.MINUTE, -DataManager.MAX_MANAGER_THREAD_PERIOD);
		Date defaultAggregationComputeStartTime = startCal.getTime();
		for (Integer period : getAvgPeriods()) {
			AggregateValue lastAV = getLastAggregateValue(period);
			Date startTime;
			if (lastAV != null
					&& lastAV.getTimestamp().after(aggregComputeMinimumTime)) {
				startTime = lastAV.getTimestamp();
			} else {
				startTime = dataManagerStartTime
						.after(defaultAggregationComputeStartTime) ? dataManagerStartTime
						: defaultAggregationComputeStartTime;
			}
			// This may happen in case of clock adjustment !
			if (startTime.after(aggregationComputeEndTime))
				startTime = defaultAggregationComputeStartTime;
			List<Date> aggTimes = computeAggregationTimes(startTime,
					aggregationComputeEndTime, period);
			List<ScalarAggregateValue> aggregateValues = new ArrayList<ScalarAggregateValue>();
			for (Date aggTime : aggTimes) {
				logger.debug("Computing aggregate value for "
						+ Periferico.getInstance().getResourceName(
								getAnalyzerId()) + ", " + getParameterId()
						+ ": period=" + period + "m, timestamp=" + aggTime);
				calendar.setTime(aggTime);
				calendar.add(Calendar.MINUTE, -period);
				Date aggTimeMinusPeriod = calendar.getTime();
				try {
					List<Sample> data = storageManager.readSampleData(
							getAnalyzerId(), getParameterId(),
							aggTimeMinusPeriod, false, aggTime, true, null);
					if (data.isEmpty()) {
						setLastAggregateValue(period, new DummyAggregateValue(
								aggTime));
						continue;
					}
					ScalarAggregateValue sav = computeAggregateValue(period,
							aggTime, data);
					setLastAggregateValue(period, sav);
					aggregateValues.add(sav);
				} catch (StorageException e) {
					logger.error("Aggregate value " + "computation failed");
				} catch (Exception e) {
					logger.error("Aggregate value " + "computation failed", e);
				}
			}
			try {
				storageManager.saveScalarAggregateData(getAnalyzerId(), this,
						period, aggregateValues);
			} catch (StorageException e) {
				// Nothing is logged here because saveScalarAggregateData
				// logs failures
			}
		}
	}

}
