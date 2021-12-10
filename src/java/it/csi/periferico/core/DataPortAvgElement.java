/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: element for analyzer, with data port interface, that
// computes average values
// Change log:
//   2015-05-08: initial version
// ----------------------------------------------------------------------------
// $Id: DataPortAvgElement.java,v 1.9 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.PerifericoUtil;
import it.csi.periferico.acqdrivers.itf.ElementValue;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Element for analyzer, with data port interface, that computes average values
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DataPortAvgElement extends DataPortElement implements
		AvgElementItf {

	private static final long serialVersionUID = 983275451321299261L;
	private static Logger logger = Logger.getLogger("periferico."
			+ DataPortAvgElement.class.getSimpleName());

	private int avgPeriod; // minutes
	private int acqDelay; // minutes
	private int acqDuration; // minutes
	private transient ElementValue lastElementValue = null;
	private transient Sample lastSample;
	private transient Map<Date, MeanValue> mapValues = new HashMap<Date, MeanValue>();

	public DataPortAvgElement() {
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

	@Override
	public int getAvgPeriod() {
		return avgPeriod;
	}

	@Override
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
			Double rangeLow, Double rangeHigh,
			Boolean discardDataNotValidForAnalyzer, int acqDelay,
			int acqDuration, int avgPeriod) throws ConfigException {
		checkTimings(avgPeriod, acqDelay, acqDuration);
		super.setConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals, analyzerMeasureUnitName, acqPeriod,
				correctionCoefficient, correctionOffset,
				linearizationCoefficient, linearizationOffset, rangeLow,
				rangeHigh, discardDataNotValidForAnalyzer);
		setAcqDelay(acqDelay);
		setAcqDuration(acqDuration);
		setAvgPeriod(avgPeriod);
	}

	public boolean isSameConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset,
			Double rangeLow, Double rangeHigh,
			Boolean discardDataNotValidForAnalyzer, int acqDelay,
			int acqDuration, int avgPeriod) {
		return super.isSameConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals, analyzerMeasureUnitName, acqPeriod,
				correctionCoefficient, correctionOffset,
				linearizationCoefficient, linearizationOffset, rangeLow,
				rangeHigh, discardDataNotValidForAnalyzer)
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

	@Override
	public boolean isReady() {
		Calendar cal = new GregorianCalendar();
		long currTimestamp = cal.getTimeInMillis();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long dayTime = currTimestamp - cal.getTimeInMillis();
		long avgPeriod_ms = avgPeriod * 60000;
		long prevAggregTime = (dayTime / avgPeriod_ms) * avgPeriod_ms;
		long minTime = prevAggregTime + acqDelay * 60000;
		long maxTime = prevAggregTime + (acqDelay + acqDuration) * 60000;
		return dayTime >= minTime && dayTime <= maxTime;
	}

	@Override
	public Sample getLastSample() {
		DataPortElementHolder elementHolder = getElementHolder();
		if (elementHolder == null || !elementHolder.isConnectionUp())
			return null;
		if (lastSample != null)
			return lastSample;
		AggregateValue av = getLastAggregateValue(avgPeriod);
		if (av instanceof ScalarAggregateValue)
			return new Sample(av.getTimestamp(),
					((ScalarAggregateValue) av).getValue(), av.isNotvalid(),
					av.getFlags());
		return null;
	}

	@Override
	public boolean deliver(ElementValue elementValue) {
		DataPortElementHolder elementHolder = getElementHolder();
		if (elementHolder == null)
			throw new IllegalStateException("ElementHolder must be initialized");
		if (getConverter() == null)
			throw new IllegalStateException("Converter must be initialized");
		if (elementValue == null)
			return false;
		if (elementValue.getPeriod() != avgPeriod) {
			logger.warn("Aggregation period mismatch: configured " + avgPeriod
					+ ", received " + elementValue.getPeriod());
			return false;
		}
		if (!checkTimestampAlignment(elementValue.getTimestamp(), avgPeriod)) {
			logger.warn("The timestamp received is not aligned to aggregation "
					+ "period " + avgPeriod + ": "
					+ elementValue.getTimestamp());
			return false;
		}
		if (sameElementValue(elementValue))
			return true;
		lastElementValue = elementValue;
		synchronized (mapValues) {
			if (mapValues.containsKey(elementValue.getTimestamp()))
				return true;
			AggregateValue lastAV = getLastAggregateValue(avgPeriod);
			if (lastAV != null
					&& lastAV.getTimestamp()
							.equals(elementValue.getTimestamp()))
				return true;
			Double value = null;
			int flags = 0;
			if (!elementValue.isValid())
				flags |= ValidationFlag.ANALYZER_DATA_NOT_VALID;
			if (elementValue.isCalibration())
				flags |= ValidationFlag.ANALYZER_AUTO_CALIB;
			if (elementValue.getError()) {
				flags |= ValidationFlag.ACQ_ERROR;
			} else {
				double rawValue = elementValue.getValue();
				double correctedValue = rawValue * getCorrectionCoefficient()
						+ getCorrectionOffset();
				double convertedValue = getConverter().convert(correctedValue);
				double linearizedValue = convertedValue
						* getLinearizationCoefficient()
						+ getLinearizationOffset();
				value = Periferico
						.roundSampleData(linearizedValue, getNumDec());
				if (value < getMinValue() || value > getMaxValue())
					flags |= ValidationFlag.VALUE_OUT_OF_RANGE;
			}
			boolean notvalid = value == null
					|| (flags & ValidationFlag.VALUE_OUT_OF_RANGE) != 0
					|| (flags & ValidationFlag.ANALYZER_DATA_NOT_VALID) != 0
					|| (flags & ValidationFlag.ANALYZER_AUTO_CALIB) != 0;
			if (Boolean.TRUE.equals(getDiscardDataNotValidForAnalyzer())
					&& (flags & ValidationFlag.ANALYZER_DATA_NOT_VALID) != 0)
				value = null;
			MeanValue mv = new MeanValue(elementValue.getTimestamp(), value,
					notvalid, flags);
			mapValues.put(mv.getTimestamp(), mv);
			lastSample = new Sample(elementValue.getTimestamp(), value,
					notvalid, flags);
		}
		return true;
	}

	private boolean checkTimestampAlignment(Date timestamp, int avgPeriod) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(timestamp);
		if (cal.get(Calendar.SECOND) != 0 || cal.get(Calendar.MILLISECOND) != 0)
			return false;
		if (avgPeriod > 1440 || avgPeriod <= 0) {
			return false;
		} else if (avgPeriod == 1440) {
			return cal.get(Calendar.HOUR_OF_DAY) == 0
					&& cal.get(Calendar.MINUTE) == 0;
		} else if (avgPeriod > 60) {
			return (cal.get(Calendar.HOUR) * 60 + cal.get(Calendar.MINUTE))
					% avgPeriod == 0;
		} else if (avgPeriod <= 60) {
			return cal.get(Calendar.MINUTE) % avgPeriod == 0;
		} else {
			return false;
		}
	}

	private boolean sameElementValue(ElementValue elementValue) {
		if (lastElementValue == null)
			return false;
		return elementValue.getPeriod() == lastElementValue.getPeriod()
				&& PerifericoUtil.areEqual(elementValue.getTimestamp(),
						lastElementValue.getTimestamp())
				&& PerifericoUtil.areEqual(elementValue.getValue(),
						lastElementValue.getValue())
				&& elementValue.getError() == lastElementValue.getError();
	}

	@Override
	void computeAndSaveAggregateValues(Date dataManagerStartTime,
			Date aggregationComputeEndTime) {
		// NOTE: nothing to do for this type of element, as it reads
		// aggregate values from the analyzer
	}

	@Override
	void saveData() throws StorageException {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		List<MeanValue> listMeanValues = new ArrayList<MeanValue>();
		synchronized (mapValues) {
			if (mapValues.isEmpty())
				return;
			List<Date> listTimestamp = new ArrayList<Date>(mapValues.keySet());
			Collections.sort(listTimestamp);
			for (Date timestamp : listTimestamp) {
				AggregateValue lastAV = getLastAggregateValue(avgPeriod);
				if (isCompletionPossible(lastAV, timestamp)) {
					logger.debug("Completion from '" + lastAV.getTimestamp()
							+ "' to '" + timestamp + "'");
					Date nextTimestamp = addAvgPeriod(lastAV.getTimestamp(),
							avgPeriod);
					while (nextTimestamp.before(timestamp)) {
						listMeanValues.add(new MeanValue(nextTimestamp, null,
								true, ValidationFlag.MISSING_DATA));
						logger.debug("Completed timestamp '" + nextTimestamp
								+ "'");
						nextTimestamp = addAvgPeriod(nextTimestamp, avgPeriod);
					}
				}
				MeanValue mv = mapValues.get(timestamp);
				setLastAggregateValue(avgPeriod, mv);
				listMeanValues.add(mv);
			}
			mapValues.clear();
		}
		List<ScalarAggregateValue> listFlaggedValues = new ArrayList<ScalarAggregateValue>();
		for (MeanValue value : listMeanValues) {
			Map<Integer, List<BinaryStatus>> mapEvents;
			mapEvents = new HashMap<Integer, List<BinaryStatus>>();
			Date endTime = value.getTimestamp();
			Date beginTime = addAvgPeriod(endTime, -avgPeriod);
			List<BinaryStatus> listEvents;
			listEvents = storageManager.readEventData(getAnalyzerId(),
					Analyzer.EVT_FAULT, beginTime, false, endTime, true, null,
					true);
			if (!listEvents.isEmpty())
				mapEvents.put(ValidationFlag.ANALYZER_FAULT, listEvents);
			listEvents = storageManager.readEventData(getAnalyzerId(),
					Analyzer.EVT_MAINTENANCE, beginTime, false, endTime, true,
					null, true);
			if (!listEvents.isEmpty())
				mapEvents.put(ValidationFlag.ANALYZER_MAINTENANCE, listEvents);
			listEvents = storageManager.readEventData(getAnalyzerId(),
					Analyzer.EVT_MAN_CALIB, beginTime, false, endTime, true,
					null, true);
			if (!listEvents.isEmpty())
				mapEvents.put(ValidationFlag.ANALYZER_MANUAL_CALIB, listEvents);
			// NOTE: The following code should be used when automatic
			// calibration will be implemented
			// listEvents = storageManager.readEventData(getAnalyzerId(),
			// Analyzer.EVT_ACHK_RUN, beginTime, false, endTime, true,
			// null, true);
			// if (!listEvents.isEmpty())
			// mapEvents.put(ValidationFlag.ANALYZER_AUTO_CALIB, listEvents);
			// listEvents = storageManager.readEventData(getAnalyzerId(),
			// Analyzer.EVT_ACHK_FAIL, beginTime, false, endTime, true,
			// null, true);
			// if (!listEvents.isEmpty())
			// mapEvents.put(ValidationFlag.CHANNEL_UNTRIMMED, listEvents);
			MeanValue fv = setFlags(value, beginTime, endTime, mapEvents);
			logger.debug("Mean value read: value=" + fv.getValue()
					+ ", notvalid=" + fv.isNotvalid() + ", flags="
					+ Integer.toHexString(fv.getFlags()));
			listFlaggedValues.add(fv);
			lastSample = new Sample(fv.getTimestamp(), fv.getValue(),
					fv.isNotvalid(), fv.getFlags());
		}
		storageManager.saveScalarAggregateData(getAnalyzerId(), this,
				avgPeriod, listFlaggedValues);
	}

	private boolean isCompletionPossible(AggregateValue lastValue,
			Date targetTimestamp) {
		if (lastValue == null)
			return false;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(targetTimestamp);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_MONTH,
				-DataManager.AGGREGATIONS_COMPUTE_TIME_OFFSET);
		return !lastValue.getTimestamp().before(calendar.getTime());
	}

	private Date addAvgPeriod(Date timestamp, int avgPeriod) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(timestamp);
		calendar.add(Calendar.MINUTE, avgPeriod);
		return calendar.getTime();
	}

	private MeanValue setFlags(MeanValue value, Date beginTime, Date endTime,
			Map<Integer, List<BinaryStatus>> mapEvents) {
		if (mapEvents.isEmpty())
			return value;
		List<Interval> listIntervals = new ArrayList<Interval>();
		int flags = value.getFlags();
		boolean notValid = value.isNotvalid();
		for (Integer flag : mapEvents.keySet()) {
			Interval interval = null;
			List<BinaryStatus> listEvents = mapEvents.get(flag);
			for (BinaryStatus bs : listEvents) {
				if (bs.getStatus())
					flags |= flag;
				if (flag == ValidationFlag.ANALYZER_MAINTENANCE
						|| flag == ValidationFlag.ANALYZER_MANUAL_CALIB
						|| flag == ValidationFlag.ANALYZER_AUTO_CALIB) {
					Date ts = bs.getTimestamp().before(beginTime) ? beginTime
							: bs.getTimestamp();
					if (bs.getStatus() && interval == null) {
						interval = new Interval(ts);
					} else if (!bs.getStatus() && interval != null) {
						interval.setEnd(ts);
						listIntervals.add(interval);
						interval = null;
					}
				}
			}
			if (interval != null) {
				interval.setEnd(endTime);
				listIntervals.add(interval);
			}
		}
		Collections.sort(listIntervals, new Comparator<Interval>() {
			@Override
			public int compare(Interval o1, Interval o2) {
				return o1.getBegin().compareTo(o2.getBegin());
			}
		});
		List<Interval> listMergedIntervals = new ArrayList<Interval>();
		ListIterator<Interval> itIntervals = listIntervals.listIterator();
		while (itIntervals.hasNext()) {
			Interval current = itIntervals.next();
			listMergedIntervals.add(current);
			while (itIntervals.hasNext()) {
				Interval next = itIntervals.next();
				if (!current.merge(next)) {
					itIntervals.previous();
					break;
				}
			}
		}
		long totalInvalidTime = 0;
		for (Interval i : listMergedIntervals)
			totalInvalidTime += i.getTime();
		long notValidityPercent = (totalInvalidTime * 100)
				/ (endTime.getTime() - beginTime.getTime());
		logger.debug("Time of not validity: " + notValidityPercent + "%");
		if (notValidityPercent > 25)
			notValid = true;
		return new MeanValue(value.getTimestamp(), value.getValue(), notValid,
				flags);
	}

	private class Interval {
		private Date begin;
		private Date end;

		Interval(Date begin) {
			this.begin = begin;
			this.end = null;
		}

		void setEnd(Date end) {
			this.end = end;
		}

		Date getBegin() {
			return begin;
		}

		Date getEnd() {
			return end;
		}

		boolean merge(Interval other) {
			if (other.getBegin().after(end))
				return false;
			if (other.getEnd().after(end))
				end = other.getEnd();
			return true;
		}

		long getTime() {
			return end.getTime() - begin.getTime();
		}
	}
	
}
