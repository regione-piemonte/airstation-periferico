/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: element that counts events
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: CounterElement.java,v 1.26 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.PerifericoUtil;
import it.csi.periferico.boards.BoardBindInfo;
import it.csi.periferico.boards.DIEvent;
import it.csi.periferico.boards.DIUser;
import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Element that counts events
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class CounterElement extends ScalarElement implements DIUser {

	private static final long serialVersionUID = 3672243266283494298L;

	private static Logger logger = Logger.getLogger("periferico."
			+ CounterElement.class.getSimpleName());

	private double valueForEvent;

	private boolean acqOnRisingEdge = true;

	private IOProvider ioProvider;

	private BoardBindInfo boardBindInfo;

	private ElementHolder elementHolder = null;

	private transient List<Sample> listSamples = new ArrayList<Sample>();

	private transient Sample lastSample = null;

	private transient Boolean currentValue = null;

	public String getBindLabel() {
		if (elementHolder == null)
			throw new IllegalStateException("ElementHolder must be initialized");
		return elementHolder.getName() + "/" + getParameterId();
	}

	public String getBindIdentifier() {
		if (elementHolder == null)
			throw new IllegalStateException("ElementHolder must be initialized");
		return elementHolder.getId() + "/" + getParameterId();
	}

	public IOProvider getIOProvider() {
		return ioProvider;
	}

	public void setIOProvider(IOProvider ioProvider) {
		this.ioProvider = ioProvider;
	}

	public boolean isAcqOnRisingEdge() {
		return acqOnRisingEdge;
	}

	public void setAcqOnRisingEdge(boolean acqOnRisingEdge) {
		this.acqOnRisingEdge = acqOnRisingEdge;
	}

	public double getValueForEvent() {
		return valueForEvent;
	}

	public void setValueForEvent(double valueForEvent) {
		this.valueForEvent = valueForEvent;
	}

	public BoardBindInfo getBoardBindInfo() {
		return boardBindInfo;
	}

	public void setBoardBindInfo(BoardBindInfo boardBindInfo) {
		this.boardBindInfo = boardBindInfo;
	}

	public boolean isActive() {
		return elementHolder.isEnabled() && isEnabled();
	}

	public void setConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			double valForEvent, boolean acqOnRisingEdge) throws ConfigException {
		checkForDataLoss(valForEvent, numDecimals);
		super.setConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals);
		setValueForEvent(valForEvent);
		setAcqOnRisingEdge(acqOnRisingEdge);
	}

	public boolean isSameConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			double valueForEvent, boolean acqOnRisingEdge) {
		return super.isSameConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals)
				&& this.valueForEvent == valueForEvent
				&& this.acqOnRisingEdge == acqOnRisingEdge;
	}

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
		checkForDataLoss(valueForEvent, getNumDec());
	}

	private void checkForDataLoss(double valForEvent, int numDecimals)
			throws ConfigException {
		try {
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(numDecimals);
			df.setMinimumFractionDigits(numDecimals);
			df.setRoundingMode(RoundingMode.UNNECESSARY);
			df.format(valForEvent);
		} catch (Exception ex) {
			throw new ConfigException("num_dec_too_low_for_value_for_event");
		}
	}

	void setElementHolder(ElementHolder elementHolder) {
		this.elementHolder = elementHolder;
	}

	public Sample getLastSample() {
		return lastSample;
	}

	synchronized List<Sample> getSamplesAndCleanList() {
		List<Sample> tmpList = listSamples;
		listSamples = new ArrayList<Sample>();
		return tmpList;
	}

	public boolean deliver(DIEvent event) {
		if (elementHolder == null)
			throw new IllegalStateException("ElementHolder must be initialized");
		if (event == null)
			return false;
		Double value = null;
		int flags = 0;
		if (elementHolder.isFaultActive())
			flags |= ValidationFlag.ANALYZER_FAULT;
		if (!elementHolder.isDataValidActive())
			flags |= ValidationFlag.ANALYZER_DATA_NOT_VALID;
		if (elementHolder.isMaintenanceInProgress())
			flags |= ValidationFlag.ANALYZER_MAINTENANCE;
		if (elementHolder.isManualCalibrationRunning())
			flags |= ValidationFlag.ANALYZER_MANUAL_CALIB;
		if (elementHolder.isAutoCheckRunning())
			flags |= ValidationFlag.ANALYZER_AUTO_CALIB;
		if (isCalibAutoCheckFailed())
			flags |= ValidationFlag.CHANNEL_UNTRIMMED;
		if (event.getError())
			flags |= ValidationFlag.ACQ_ERROR;
		else {
			Boolean oldValue = currentValue;
			currentValue = event.getValue();
			if (oldValue == null || oldValue == currentValue
					|| currentValue != acqOnRisingEdge)
				return true;
			value = valueForEvent;
		}
		boolean notvalid = value == null
				|| (flags & ValidationFlag.ANALYZER_DATA_NOT_VALID) != 0
				|| (flags & ValidationFlag.ANALYZER_MAINTENANCE) != 0
				|| (flags & ValidationFlag.ANALYZER_MANUAL_CALIB) != 0
				|| (flags & ValidationFlag.ANALYZER_AUTO_CALIB) != 0;

		Sample sample = new Sample(event.getTimeStamp(), value, notvalid, flags);
		addNewSample(sample);
		return true;
	}

	// NOTE: rain events occurred in the same second are added together into
	// a single Sample. It may happen, if getSamplesAndCleanList() is called
	// between two addNewSample() with same second's samples, that two samples
	// with same second timestamp could be written to files. This event should
	// be extremely improbable or nearly impossible, keeping into account thread
	// priorities, and the only consequence would be having the second of these
	// samples discarded during file read.
	private synchronized void addNewSample(Sample sample) {
		if (!listSamples.isEmpty()) {
			int lastSampleIndex = listSamples.size() - 1;
			Sample previousSample = listSamples.get(lastSampleIndex);
			if (PerifericoUtil.areInSameSecond(previousSample.getTimestamp(),
					sample.getTimestamp())) {
				Date timestamp = sample.getTimestamp();
				Double value = previousSample.getValue();
				if (value == null)
					value = sample.getValue();
				else if (sample.getValue() != null)
					value += sample.getValue();
				boolean notvalid = previousSample.isNotvalid()
						|| sample.isNotvalid();
				int flags = previousSample.getFlags() | sample.getFlags();
				Sample cumulativeSample = new Sample(timestamp, value,
						notvalid, flags);
				listSamples.remove(lastSampleIndex);
				sample = cumulativeSample;
			}
		}
		lastSample = sample;
		listSamples.add(sample);
	}

	@Override
	TotalValue computeAggregateValue(int aggregationPeriod_m,
			Date aggregationTime, List<Sample> data) {
		int numValid = 0;
		int numNotvalid = 0;
		int numIncoherentTimestamp = 0;
		double total = 0.0;
		int globalFlags = 0;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(aggregationTime);
		calendar.add(Calendar.MINUTE, -aggregationPeriod_m);
		Date aggregationStartTime = calendar.getTime();
		for (Sample sample : data) {
			if (!sample.getTimestamp().after(aggregationStartTime)) {
				numIncoherentTimestamp++;
				continue;
			}
			if (sample.getTimestamp().after(aggregationTime)) {
				numIncoherentTimestamp++;
				continue;
			}
			if (sample.isNotvalid()) {
				numNotvalid++;
			} else {
				numValid++;
				if (sample.getValue() == null)
					throw new IllegalStateException(
							"Valid sample must have not null value");
				total += sample.getValue();
			}
			globalFlags |= sample.getFlags();
		}
		if (numIncoherentTimestamp > 0)
			throw new IllegalStateException("Found " + numIncoherentTimestamp
					+ " samples with acquisition timestamp outside "
					+ "aggregation period");
		boolean notvalid = numNotvalid > 0;
		if (total < getMinValue() || total > getMaxValue()) {
			notvalid = true;
			globalFlags |= ValidationFlag.VALUE_OUT_OF_RANGE;
		}
		logger.debug("Total value computed: value=" + total + ", notvalid="
				+ notvalid + ", flags=" + Integer.toHexString(globalFlags)
				+ " (valid=" + numValid + ", notvalid=" + numNotvalid + ")");
		return new TotalValue(aggregationTime, total, notvalid, globalFlags);
	}

	// TODO: part of this function uses part of the code of ScalarElement's
	// computeAndSaveAggregateValues. It should be better to have the common
	// code in one place only
	@Override
	void computeAndSaveAggregateValues(Date dataManagerStartTime,
			Date aggregationComputeEndTime) {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		Calendar calendar = new GregorianCalendar();
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(aggregationComputeEndTime);
		startCal.add(Calendar.MINUTE, -DataManager.MAX_MANAGER_THREAD_PERIOD);
		Date defaultAggregationComputeStartTime = startCal.getTime();
		for (Integer period : getAvgPeriods()) {
			Date startTime = dataManagerStartTime
					.after(defaultAggregationComputeStartTime) ? dataManagerStartTime
					: defaultAggregationComputeStartTime;
			AggregateValue lastAV = getLastAggregateValue(period);
			if (lastAV != null && lastAV.getTimestamp().after(startTime))
				startTime = lastAV.getTimestamp();
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

	@Override
	void readAndSetLastAggregateValues(Date aggregationsComputeStartTime) {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		for (Integer period : getAvgPeriods()) {
			try {
				List<TotalValue> data = storageManager.readTotalData(
						getAnalyzerId(), getParameterId(), period,
						aggregationsComputeStartTime, false, new Date(), true,
						null);
				if (!data.isEmpty()) {
					TotalValue tv = data.get(data.size() - 1);
					setLastAggregateValue(period, tv);
					logger.debug("Last aggregate value for " + getBindLabel()
							+ ", period " + period + " is: " + tv.getValue()
							+ ", " + tv.getTimestamp());
				}
			} catch (StorageException e) {
				logger.error("Error reading last aggregate value " + "for "
						+ getBindLabel() + ", period " + period);
			}
		}
	}

	@Override
	void saveData() throws StorageException {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		List<Sample> data = getSamplesAndCleanList();
		storageManager.saveSampleData(getAnalyzerId(), getParameterId(),
				getNumDec(), data);
	}

}
