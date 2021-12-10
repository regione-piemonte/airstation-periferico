/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: element for analyzer that computes average values
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: AvgElement.java,v 1.23 2015/10/15 11:47:02 pfvallosio Exp $
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
import java.util.ListIterator;

import org.apache.log4j.Logger;

/**
 * Element for analyzer that computes average values
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class AvgElement extends SampleElement implements AvgElementItf {

	private static final long serialVersionUID = 9125073850459497424L;

	private static Logger logger = Logger.getLogger("periferico."
			+ AvgElement.class.getSimpleName());

	private int avgPeriod; // minutes

	private int acqDelay; // minutes

	private int acqDuration; // minutes

	public AvgElement() {
		avgPeriod = 0;
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		List<Integer> avgPeriods = cc.getAvgPeriods();
		for (Integer value : avgPeriods) {
			if (value > avgPeriod)
				avgPeriod = value;
		}
		if (avgPeriod == 0)
			avgPeriod = 1440;
	}

	public int getAcqDelay() {
		return acqDelay;
	}

	public void setAcqDelay(int acqDelay) {
		this.acqDelay = acqDelay;
	}

	public int getAcqDuration() {
		return acqDuration;
	}

	public void setAcqDuration(int acqDuration) {
		this.acqDuration = acqDuration;
	}

	public int getAvgPeriod() {
		return avgPeriod;
	}

	public void setAvgPeriod(int avgPeriod) {
		this.avgPeriod = avgPeriod;
	}

	@Override
	public List<Integer> getAvgPeriods() {
		List<Integer> tmpList = new ArrayList<Integer>();
		tmpList.add(avgPeriod);
		return tmpList;
	}

	@Override
	public void setAvgPeriods(List<Integer> avgPeriods) {
	}

	@Override
	public boolean insertAvgPeriod(Integer avgPeriod) {
		throw new UnsupportedOperationException("Class "
				+ this.getClass().getName()
				+ " does not support the list of average periods");
	}

	@Override
	public boolean deleteAvgPeriod(Integer avgPeriod) {
		throw new UnsupportedOperationException("Class "
				+ this.getClass().getName()
				+ " does not support the list of average periods");
	}

	public void setConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset,
			double rangeLow, double rangeHigh, int acqDelay, int acqDuration,
			int avgPeriod) throws ConfigException {
		checkTimings(avgPeriod, acqDelay, acqDuration);
		super.setConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals, analyzerMeasureUnitName, acqPeriod,
				correctionCoefficient, correctionOffset,
				linearizationCoefficient, linearizationOffset, rangeLow,
				rangeHigh);
		setAcqDelay(acqDelay);
		setAcqDuration(acqDuration);
		setAvgPeriod(avgPeriod);
	}

	public boolean isSameConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset,
			double rangeLow, double rangeHigh, int acqDelay, int acqDuration,
			int avgPeriod) {
		return super.isSameConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals, analyzerMeasureUnitName, acqPeriod,
				correctionCoefficient, correctionOffset,
				linearizationCoefficient, linearizationOffset, rangeLow,
				rangeHigh)
				&& this.acqDelay == acqDelay
				&& this.acqDuration == acqDuration
				&& this.avgPeriod == avgPeriod;
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkTimings(avgPeriod, acqDelay, acqDuration);
		super.checkConfig();
	}

	private void checkTimings(int avgPeriod, int acqDelay, int acqDuration)
			throws ConfigException {
		if (avgPeriod <= 0)
			throw new ConfigException("avg_period_must_be_positive");
		if (avgPeriod > 1440)
			throw new ConfigException("avg_period_above_1440");
		if (acqDelay < 0)
			throw new ConfigException("acq_delay_must_not_be_negative");
		if (acqDuration <= 0)
			throw new ConfigException("acq_duration_must_be_positive");
		if (acqDelay >= avgPeriod)
			throw new ConfigException("acq_delay_must_be_lt_avg_period");
		if (acqDuration > avgPeriod)
			throw new ConfigException("acq_duration_must_not_be_gt_avg_period");
		if (acqDelay + acqDuration > avgPeriod)
			throw new ConfigException(
					"acq_delay_plus_duration_must_not_be_gt_avg_period");
	}

	private MeanValue acquireMeanValue(Date acqEndTime, List<Sample> data) {
		if (getAcqPeriod() <= 0)
			throw new IllegalStateException(
					"Acquisition period must be greater than 0");
		if (acqDuration * 60 < getAcqPeriod())
			throw new IllegalArgumentException("Acquisition duration must not"
					+ " be lower than acquisition period");
		List<Sample> validData = new ArrayList<Sample>();
		int numExpected = 0;
		int numValid = 0;
		int numNotvalid = 0;
		int numNotValidForAnalyzer = 0;
		int numMissing;
		int numIncoherentTimestamp = 0;
		double accumulator = 0.0;
		int globalFlags = 0;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(acqEndTime);
		calendar.add(Calendar.MINUTE, -acqDuration);
		Date acqStartTime = calendar.getTime();
		calendar.add(Calendar.MINUTE, -acqDelay);
		Date aggregationTrueTime = calendar.getTime();
		calendar.setTime(acqStartTime);
		calendar.add(Calendar.SECOND, getAcqPeriod());
		Date timestamp = calendar.getTime();
		ListIterator<Sample> it = data.listIterator();
		while (!timestamp.after(acqEndTime)) {
			numExpected++;
			while (it.hasNext()) {
				Sample sample = it.next();
				if (sample.getTimestamp().before(timestamp)) {
					numIncoherentTimestamp++;
					continue;
				}
				if (sample.getTimestamp().equals(timestamp)) {
					if (sample.isNotvalid()) {
						numNotvalid++;
					} else {
						numValid++;
						if (sample.getValue() == null)
							throw new IllegalStateException(
									"Valid sample must have not null value");
						accumulator += sample.getValue();
						validData.add(sample);
						if ((sample.getFlags() & ValidationFlag.ANALYZER_DATA_NOT_VALID) != 0)
							numNotValidForAnalyzer++;
					}
					globalFlags |= sample.getFlags();
					break;
				}
				it.previous();
				break;
			}
			calendar.add(Calendar.SECOND, getAcqPeriod());
			timestamp = calendar.getTime();
		}
		if (numExpected == 0)
			throw new IllegalStateException("Zero data expected for mean"
					+ " computation: at least 1 is needed");
		numMissing = numExpected - numValid - numNotvalid;
		Double value = null;
		double sigma = 0.0;
		if (numValid > 0) {
			value = Periferico.roundAggregateData(accumulator / numValid,
					getNumDec());
			accumulator = 0.0;
			for (Sample s : validData) {
				accumulator += Math.pow(s.getValue() - value, 2);
			}
			accumulator /= validData.size();
			sigma = Math.sqrt(accumulator);
		}
		boolean notCostant = sigma > 3.0;
		boolean notvalid = (((numValid - numNotValidForAnalyzer) * 100) / numExpected) < 75
				| notCostant;
		if (numMissing > 0)
			globalFlags |= ValidationFlag.MISSING_DATA;
		if (notCostant)
			globalFlags |= ValidationFlag.NOT_CONSTANT_DATA;
		if (numIncoherentTimestamp > 0)
			logger.warn("Found " + numIncoherentTimestamp
					+ " samples with acquisition timestamp incoherent "
					+ "with acquisition period");
		logger.info("Mean value acquired: value=" + value + ", notvalid="
				+ notvalid + ", flags=" + Integer.toHexString(globalFlags)
				+ " (expected=" + numExpected + ", valid=" + numValid
				+ ", notvalid for analyzer=" + numNotValidForAnalyzer
				+ ", notvalid=" + numNotvalid + ", missing=" + numMissing
				+ ", notcostant=" + notCostant + ", sigma=" + sigma + ")");
		return new MeanValue(aggregationTrueTime, value, notvalid, globalFlags);
	}

	private List<Date> computeAcquisitionEndTimes(Date startTime, Date endTime,
			int period_min) {
		if (period_min <= 0)
			throw new IllegalStateException(
					"The aggregation period must not be less than 1 minute");
		if (period_min > 1440)
			throw new IllegalStateException(
					"The aggregation period must not be  greater than 24 hours");
		List<Date> acqEndTimes = new ArrayList<Date>();
		Calendar cal = new GregorianCalendar();
		cal.setTime(startTime);
		if (60 % period_min != 0)
			cal.set(Calendar.HOUR_OF_DAY, 0);
		if (period_min > 1)
			cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MINUTE, acqDelay + acqDuration);
		Date acqEndTime = cal.getTime();
		while (!acqEndTime.after(endTime)) {
			if (acqEndTime.after(startTime))
				acqEndTimes.add(acqEndTime);
			cal.add(Calendar.MINUTE, period_min);
			acqEndTime = cal.getTime();
		}
		return acqEndTimes;
	}

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
		AggregateValue lastAV = getLastAggregateValue(avgPeriod);
		Date startTime = dataManagerStartTime
				.after(defaultAggregationComputeStartTime) ? dataManagerStartTime
				: defaultAggregationComputeStartTime;
		if (lastAV != null
				&& !lastAV.getTimestamp().before(aggregComputeMinimumTime)) {
			calendar.setTime(lastAV.getTimestamp());
			calendar.add(Calendar.MINUTE, acqDelay + acqDuration);
			if (calendar.getTime().after(aggregComputeMinimumTime))
				startTime = calendar.getTime();
		}
		// This may happen in case of clock adjustment !
		if (startTime.after(aggregationComputeEndTime))
			startTime = defaultAggregationComputeStartTime;
		List<Date> acquisitionEndTimes = computeAcquisitionEndTimes(startTime,
				aggregationComputeEndTime, avgPeriod);
		List<ScalarAggregateValue> meanValues = new ArrayList<ScalarAggregateValue>();
		for (Date acqEndTime : acquisitionEndTimes) {
			calendar.setTime(acqEndTime);
			calendar.add(Calendar.MINUTE, -acqDuration);
			Date acqStartTime = calendar.getTime();
			calendar.add(Calendar.MINUTE, -acqDelay);
			Date aggregationTime = calendar.getTime();
			logger.debug("Acquiring aggregate value for "
					+ Periferico.getInstance().getResourceName(getAnalyzerId())
					+ ", " + getParameterId() + ": aggregationTime="
					+ aggregationTime + ", period=" + avgPeriod
					+ "m, acqStart=" + acqStartTime + ", acqEnd=" + acqEndTime);
			try {
				List<Sample> data = storageManager.readSampleData(
						getAnalyzerId(), getParameterId(), acqStartTime, false,
						acqEndTime, true, null);
				MeanValue mv = acquireMeanValue(acqEndTime, data);
				setLastAggregateValue(avgPeriod, mv);
				meanValues.add(mv);
			} catch (StorageException e) {
				logger.error("Aggregate value " + "acquisition failed");
			} catch (Exception e) {
				logger.error("Aggregate value " + "acquisition failed", e);
			}
		}
		try {
			storageManager.saveScalarAggregateData(getAnalyzerId(), this,
					avgPeriod, meanValues);
		} catch (StorageException e) {
			// Nothing is logged here because saveScalarAggregateData
			// logs failures
		}
	}

}
