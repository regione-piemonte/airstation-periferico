/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: implements the element for a wind analyzer
// Change log:
//   2008-08-12: initial version
// ----------------------------------------------------------------------------
// $Id: WindElement.java,v 1.24 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.boards.BoardBindInfo;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.MeasureUnit;
import it.csi.periferico.config.common.Parameter;
import it.csi.periferico.config.common.Standards;
import it.csi.periferico.config.common.converters.Converter;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Implements the element for a wind analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class WindElement extends Element {

	private static final long serialVersionUID = -2531191249351420727L;

	private static final int VALID_DATA_NUMBER_THRESHOLD_PERCENT = 75;

	private static final double CALM_THRESHOLD_MPS = 0.4; // m/s

	private static final double CALM_NUMBER_THRESHOLD_PERCENT = 75;

	private static Logger logger = Logger.getLogger("periferico."
			+ WindElement.class.getSimpleName());

	private SampleElement speed;

	private SampleElement direction;

	private Double calmThreshold = null;

	public WindElement() {
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		setParameterId(cc.getParameters(Parameter.ParamType.WIND).get(0)
				.getId());
		speed = new SampleElement();
		speed.setParameterId(cc.getParameters(Parameter.ParamType.WIND_VEL)
				.get(0).getId());
		speed.setRangeLow(0.0);
		speed.setMinValue(0.0);
		setSpeedMeasureUnitName(CommonCfg.WIND_SPEED_MPS_MEASURE_UNIT_NAME);
		direction = new SampleElement();
		direction.setParameterId(cc.getParameters(Parameter.ParamType.WIND_DIR)
				.get(0).getId());
		direction.setRangeLow(0.0);
		direction.setMinValue(0.0);
		direction.setRangeHigh(360.0);
		direction.setMaxValue(360.0);
		direction
				.setMeasureUnitName(CommonCfg.WIND_DIRECTION_MEASURE_UNIT_NAME);
		direction
				.setAnalyzerMeasureUnitName(CommonCfg.WIND_DIRECTION_MEASURE_UNIT_NAME);
	}

	public SampleElement getSpeed() {
		return speed;
	}

	public SampleElement getDirection() {
		return direction;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		speed.setEnabled(enabled);
		direction.setEnabled(enabled);
	}

	public String getSpeedParameterId() {
		return speed.getParameterId();
	}

	public double getSpeedMaxValue() {
		return speed.getMaxValue();
	}

	public void setSpeedMaxValue(double maxValue) {
		speed.setMaxValue(maxValue);
	}

	public double getSpeedMinValue() {
		return speed.getMinValue();
	}

	public void setSpeedPrecision(double value) {
		speed.setMinValue(0.0 - Math.abs(value));
	}

	public double getSpeedPrecision() {
		return speed.getRangeLow() - speed.getMinValue();
	}

	public String getDirectionParameterId() {
		return direction.getParameterId();
	}

	public double getDirectionMaxValue() {
		return direction.getMaxValue();
	}

	public double getDirectionMinValue() {
		return direction.getMinValue();
	}

	public void setDirectionPrecision(double value) {
		direction.setMaxValue(360.0 + Math.abs(value));
		direction.setMinValue(0.0 - Math.abs(value));
	}

	public double getDirectionPrecision() {
		return direction.getRangeLow() - direction.getMinValue();
	}

	public String getSpeedMeasureUnitName() {
		return speed.getMeasureUnitName();
	}

	public void setSpeedMeasureUnitName(String measureUnitName) {
		speed.setMeasureUnitName(measureUnitName);
		speed.setAnalyzerMeasureUnitName(measureUnitName);
		updateCalmThreshold();
	}

	public String getDirectionMeasureUnitName() {
		return direction.getMeasureUnitName();
	}

	public int getSpeedNumDec() {
		return speed.getNumDec();
	}

	public void setSpeedNumDec(int numDec) {
		speed.setNumDec(numDec);
	}

	public int getDirectionNumDec() {
		return direction.getNumDec();
	}

	public void setDirectionNumDec(int numDec) {
		direction.setNumDec(numDec);
	}

	public int getAcqPeriod() {
		return speed.getAcqPeriod();
	}

	public void setAcqPeriod(int acqPeriod) {
		speed.setAcqPeriod(acqPeriod);
		direction.setAcqPeriod(acqPeriod);
	}

	public double getSpeedRangeHigh() {
		return speed.getRangeHigh();
	}

	public void setSpeedRangeHigh(double rangeHigh) {
		speed.setRangeHigh(rangeHigh);
	}

	public double getSpeedRangeLow() {
		return speed.getRangeLow();
	}

	public double getDirectionRangeHigh() {
		return direction.getRangeHigh();
	}

	public double getDirectionRangeLow() {
		return direction.getRangeLow();
	}

	void setSampleElementHolder(SampleElementHolder sampleElementHolder) {
		speed.setSampleElementHolder(sampleElementHolder);
		direction.setSampleElementHolder(sampleElementHolder);
	}

	public BoardBindInfo getSpeedBoardBindInfo() {
		return speed.getBoardBindInfo();
	}

	public void setSpeedBoardBindInfo(BoardBindInfo boardBindInfo) {
		speed.setBoardBindInfo(boardBindInfo);
	}

	public BoardBindInfo getDirectionBoardBindInfo() {
		return direction.getBoardBindInfo();
	}

	public void setDirectionBoardBindInfo(BoardBindInfo boardBindInfo) {
		direction.setBoardBindInfo(boardBindInfo);
	}

	public double getSpeedCorrectionCoefficient() {
		return speed.getCorrectionCoefficient();
	}

	public void setSpeedCorrectionCoefficient(double speedCorrectionCoefficient) {
		speed.setCorrectionCoefficient(speedCorrectionCoefficient);
	}

	public double getSpeedCorrectionOffset() {
		return speed.getCorrectionOffset();
	}

	public void setSpeedCorrectionOffset(double speedCorrectionOffset) {
		speed.setCorrectionOffset(speedCorrectionOffset);
	}

	public double getDirectionCorrectionCoefficient() {
		return direction.getCorrectionCoefficient();
	}

	public void setDirectionCorrectionCoefficient(
			double directionCorrectionCoefficient) {
		direction.setCorrectionCoefficient(directionCorrectionCoefficient);
	}

	public double getDirectionCorrectionOffset() {
		return direction.getCorrectionOffset();
	}

	public void setDirectionCorrectionOffset(double directionCorrectionOffset) {
		direction.setCorrectionOffset(directionCorrectionOffset);
	}

	public void setConfig(boolean enabled, String speedMeasureUnitName,
			double speedMaxValue, int speedNumDecimals, double speedPrecision,
			double speedRangeHigh, int directionNumDecimals,
			double directionPrecision, int acqPeriod,
			double speedCorrectionCoefficient, double speedCorrectionOffset,
			double directionCorrectionCoefficient,
			double directionCorrectionOffset) throws ConfigException {
		checkConfig(speedMaxValue, speedNumDecimals, speedRangeHigh,
				directionNumDecimals, acqPeriod, speedCorrectionCoefficient,
				directionCorrectionCoefficient);
		checkCalmThresholdConversion(speedMeasureUnitName);
		super.setConfig(enabled);
		setSpeedMeasureUnitName(speedMeasureUnitName);
		setSpeedMaxValue(speedMaxValue);
		setSpeedNumDec(speedNumDecimals);
		setSpeedPrecision(speedPrecision);
		setSpeedRangeHigh(speedRangeHigh);
		setDirectionNumDec(directionNumDecimals);
		setDirectionPrecision(directionPrecision);
		setAcqPeriod(acqPeriod);
		setSpeedCorrectionCoefficient(speedCorrectionCoefficient);
		setSpeedCorrectionOffset(speedCorrectionOffset);
		setDirectionCorrectionCoefficient(directionCorrectionCoefficient);
		setDirectionCorrectionOffset(directionCorrectionOffset);
	}

	public boolean isSameConfig(boolean enabled, String speedMeasureUnitName,
			double speedMaxValue, int speedNumDecimals, double speedPrecision,
			double speedRangeHigh, int directionNumDecimals,
			double directionPrecision, int acqPeriod,
			double speedCorrectionCoefficient, double speedCorrectionOffset,
			double directionCorrectionCoefficient,
			double directionCorrectionOffset) {
		return super.isSameConfig(enabled)
				&& trim(getSpeedMeasureUnitName()).equals(
						trim(speedMeasureUnitName))
				&& getSpeedMaxValue() == speedMaxValue
				&& getSpeedNumDec() == speedNumDecimals
				&& getSpeedPrecision() == speedPrecision
				&& getSpeedRangeHigh() == speedRangeHigh
				&& getDirectionNumDec() == directionNumDecimals
				&& getDirectionPrecision() == directionPrecision
				&& getAcqPeriod() == acqPeriod
				&& speedCorrectionCoefficient == getSpeedCorrectionCoefficient()
				&& speedCorrectionOffset == getSpeedCorrectionOffset()
				&& directionCorrectionCoefficient == getDirectionCorrectionCoefficient()
				&& directionCorrectionOffset == getDirectionCorrectionOffset();
	}

	private void checkConfig(double speedMaxValue, int speedNumDecimals,
			double speedRangeHigh, int directionNumDecimals, int acqPeriod,
			double speedCorrectionCoefficient,
			double directionCorrectionCoefficient) throws ConfigException {
		if (speedNumDecimals < 0)
			throw new ConfigException("speed_num_dec_should_be_gt_0");
		if (speedMaxValue <= 0)
			throw new ConfigException("validation_error");
		if (0 >= speedRangeHigh)
			throw new ConfigException("range_error");
		if (acqPeriod <= 0)
			throw new ConfigException("acqPeriod_le_0");
		if (speedCorrectionCoefficient <= 0)
			throw new ConfigException("correctionCoefficient_le_0");
		if (directionCorrectionCoefficient <= 0)
			throw new ConfigException("correctionCoefficient_le_0");
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkConfig(getSpeedMaxValue(), getSpeedNumDec(), getSpeedRangeHigh(),
				getDirectionNumDec(), getAcqPeriod(),
				getSpeedCorrectionCoefficient(),
				getDirectionCorrectionCoefficient());
		speed.checkConfig();
		direction.checkConfig();
		checkCalmThresholdConversion(getSpeedMeasureUnitName());
	}

	private void checkCalmThresholdConversion(String speedMeasureUnitName)
			throws ConfigException {
		try {
			CommonCfg cc = Periferico.getInstance().getCommonCfg();
			MeasureUnit srcMU = cc
					.getMeasureUnit(CommonCfg.WIND_SPEED_MPS_MEASURE_UNIT_NAME);
			MeasureUnit destMU = cc.getMeasureUnit(speedMeasureUnitName);
			Parameter param = cc.getParameter(getSpeedParameterId());
			Standards standards = cc.getStandards();
			Converter converter = new Converter(srcMU, destMU, param, standards);
			converter.convert(CALM_THRESHOLD_MPS);
		} catch (Exception ex) {
			logger.error("Calm thresold conversion failed", ex);
			throw new ConfigException("speed_threshold_conversion_error", ex);
		}
	}

	private void updateCalmThreshold() {
		try {
			CommonCfg cc = Periferico.getInstance().getCommonCfg();
			MeasureUnit srcMU = cc
					.getMeasureUnit(CommonCfg.WIND_SPEED_MPS_MEASURE_UNIT_NAME);
			MeasureUnit destMU = cc.getMeasureUnit(getSpeedMeasureUnitName());
			Parameter param = cc.getParameter(getSpeedParameterId());
			Standards standards = cc.getStandards();
			Converter converter = new Converter(srcMU, destMU, param, standards);
			calmThreshold = converter.convert(CALM_THRESHOLD_MPS);
			logger.debug("Calm thresold converted from " + CALM_THRESHOLD_MPS
					+ " [" + srcMU.getName() + "] to " + calmThreshold + " ["
					+ destMU.getName() + "]");
		} catch (Exception ex) {
			calmThreshold = null;
			logger.error("Calm thresold conversion failed", ex);
		}
	}

	private WindValue computeWindAggregateValue(int aggregationPeriod_m,
			Date aggregationTime, List<Sample> speedData, List<Sample> dirData) {
		if (getAcqPeriod() <= 0)
			throw new IllegalStateException(
					"Acquisition period must be greater than 0");
		if (aggregationPeriod_m * 60 < getAcqPeriod())
			throw new IllegalArgumentException("Aggregation period must not be"
					+ " lower than acquisition period");
		if (calmThreshold == null)
			throw new IllegalStateException(
					"Calm thresold speed conversion failed");
		int numExpected = 0;
		int numValid = 0;
		int numNotvalid = 0;
		int numMissing;
		int numSpeedIncoherentTimestamp = 0;
		int numDirIncoherentTimestamp = 0;
		int globalFlags = 0;
		double accumulatorX = 0.0;
		double accumulatorY = 0.0;
		double accumulatorSpeed = 0.0;
		double gustSpeed = 0.0;
		double gustDirection = 0.0;
		int numCalm = 0;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(aggregationTime);
		calendar.add(Calendar.MINUTE, -aggregationPeriod_m);
		calendar.add(Calendar.SECOND, getAcqPeriod());
		Date timestamp = calendar.getTime();
		ListIterator<Sample> itSpeed = speedData.listIterator();
		ListIterator<Sample> itDir = dirData.listIterator();
		while (!timestamp.after(aggregationTime)) {
			numExpected++;
			Sample speedSample = null;
			Sample dirSample = null;
			while (itSpeed.hasNext()) {
				Sample sample = itSpeed.next();
				if (sample.getTimestamp().before(timestamp)) {
					numSpeedIncoherentTimestamp++;
					continue;
				}
				if (sample.getTimestamp().equals(timestamp)) {
					speedSample = sample;
					break;
				}
				itSpeed.previous();
				break;
			}
			while (itDir.hasNext()) {
				Sample sample = itDir.next();
				if (sample.getTimestamp().before(timestamp)) {
					numDirIncoherentTimestamp++;
					continue;
				}
				if (sample.getTimestamp().equals(timestamp)) {
					dirSample = sample;
					break;
				}
				itDir.previous();
				break;
			}
			if (speedSample == null || dirSample == null) {
				// NOTHING TO DO
			} else if (speedSample.isNotvalid() || dirSample.isNotvalid()) {
				numNotvalid++;
				globalFlags |= speedSample.getFlags();
				globalFlags |= dirSample.getFlags();
			} else {
				numValid++;
				if (speedSample.getValue() == null
						|| dirSample.getValue() == null)
					throw new IllegalStateException(
							"Valid sample must have not null value");
				globalFlags |= speedSample.getFlags();
				globalFlags |= dirSample.getFlags();
				double speed = speedSample.getValue();
				if (speed > calmThreshold) {
					double dirRad = Math.toRadians(270 - dirSample.getValue());
					double x = speed * Math.cos(dirRad);
					double y = speed * Math.sin(dirRad);
					accumulatorX += x;
					accumulatorY += y;
					accumulatorSpeed += speed;
					if (speed > gustSpeed) {
						gustSpeed = speed;
						gustDirection = dirSample.getValue();
						while (gustDirection >= 360.0)
							gustDirection -= 360.0;
						while (gustDirection < 0.0)
							gustDirection += 360.0;
					}
					listX.add(x);
					listY.add(y);
				} else {
					numCalm++;
				}
			}
			calendar.add(Calendar.SECOND, getAcqPeriod());
			timestamp = calendar.getTime();
		}
		if (numExpected == 0)
			throw new IllegalStateException("Zero data expected for wind "
					+ "aggregation computation: at least 1 is needed");
		numMissing = numExpected - numValid - numNotvalid;
		boolean notvalid = true;
		Boolean calm = null;
		if (numValid > 0) {
			notvalid = ((numValid * 100) / numExpected) < VALID_DATA_NUMBER_THRESHOLD_PERCENT;
			calm = ((numCalm * 100) / numValid) >= CALM_NUMBER_THRESHOLD_PERCENT;
		}
		if (numMissing > 0)
			globalFlags |= ValidationFlag.MISSING_DATA;
		WindValue windAggregateValue = null;
		if (calm != null && !calm && (numValid - numCalm > 0)) {
			double meanX = accumulatorX / (numValid - numCalm);
			double meanY = accumulatorY / (numValid - numCalm);
			double meanSpeed = accumulatorSpeed / (numValid - numCalm);
			double meanVectDirRad = Math.atan2(meanY, meanX);
			double meanVectDir = (270 - Math.toDegrees(meanVectDirRad)) % 360;
			double calmsNumberPercent = (numCalm * 100.0) / numValid;
			double meanVectSpeed = (Math.sqrt(Math.pow(meanX, 2)
					+ Math.pow(meanY, 2)) * (numValid - numCalm))
					/ numValid;
			Double standardDeviation = null;
			if (numValid - numCalm > 1) {
				double accumulator_dvx = 0.0;
				double accumulator_dvy = 0.0;
				double accumulator_Rxy = 0.0;
				ListIterator<Double> itX = listX.listIterator();
				ListIterator<Double> itY = listY.listIterator();
				for (int i = 0; i < numValid - numCalm; i++) {
					double xi = itX.next();
					double yi = itY.next();
					accumulator_dvx += Math.pow(xi - meanX, 2);
					accumulator_dvy += Math.pow(yi - meanY, 2);
					accumulator_Rxy += (xi - meanX) * (yi - meanY);
				}
				double dvx = accumulator_dvx / (numValid - numCalm - 1);
				double dvy = accumulator_dvy / (numValid - numCalm - 1);
				double Rxy = accumulator_Rxy / (numValid - numCalm);
				double d1 = Math.cos(meanVectDirRad);
				double d2 = Math.sin(meanVectDirRad);
				standardDeviation = Math.sqrt(dvx + dvy - dvx * Math.pow(d1, 2)
						+ dvy * Math.pow(d2, 2) - 2 * d1 * d2 * Rxy);
			}
			int speedDec = getSpeedNumDec();
			int dirDec = getDirectionNumDec();
			windAggregateValue = new WindValue(aggregationTime,//
					Periferico.roundAggregateData(meanVectSpeed, speedDec),//
					Periferico.roundAggregateData(meanVectDir, dirDec),//
					standardDeviation != null ? Periferico.roundAggregateData(
							standardDeviation, speedDec) : null,//
					Periferico.roundAggregateData(meanSpeed, speedDec),//
					Periferico.roundAggregateData(gustSpeed, speedDec),//
					Periferico.roundAggregateData(gustDirection, dirDec),//
					Periferico.roundAggregateData(calmsNumberPercent, 1),//
					calm, notvalid, globalFlags);
		} else {
			windAggregateValue = new WindValue(aggregationTime, null, null,
					null, null, null, null, null, calm, notvalid, globalFlags);
		}
		if (numSpeedIncoherentTimestamp > 0)
			logger.warn("Found " + numSpeedIncoherentTimestamp
					+ " speed samples with acquisition timestamp"
					+ " incoherent with acquisition period");
		if (numDirIncoherentTimestamp > 0)
			logger.warn("Found " + numDirIncoherentTimestamp
					+ " direction samples with acquisition timestamp"
					+ " incoherent with acquisition period");
		logger.debug("Wind aggregate value computed: " + "VSpeed="
				+ windAggregateValue.getVectorialSpeed() + ", VDir="
				+ windAggregateValue.getVectorialDirection() + ", DevStd="
				+ windAggregateValue.getStandardDeviation() + ", Speed="
				+ windAggregateValue.getScalarSpeed() + ", GustSpeed="
				+ windAggregateValue.getGustSpeed() + ", GustDir="
				+ windAggregateValue.getGustDirection() + ", CalmsPercent="
				+ windAggregateValue.getCalmsNumberPercent() + ", IsCalm="
				+ windAggregateValue.getCalm() + ", notvalid=" + notvalid
				+ ", flags=" + Integer.toHexString(globalFlags) + " (expected="
				+ numExpected + ", valid=" + numValid + ", notvalid="
				+ numNotvalid + ", missing=" + numMissing + ")");
		return windAggregateValue;
	}

	// TODO: the first part of this function uses the same code of the first
	// part of ScalarElement's computeAndSaveAggregateValues. It should be
	// better to have the common code in one place only
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
			// ---------- End of code equal to ScalarElement's code -----------
			List<WindValue> windAggregateValues = new ArrayList<WindValue>();
			for (Date aggTime : aggTimes) {
				logger.debug("Computing wind aggregate value for "
						+ Periferico.getInstance().getResourceName(
								getAnalyzerId()) + ", " + getParameterId()
						+ ": period=" + period + "m, timestamp=" + aggTime);
				calendar.setTime(aggTime);
				calendar.add(Calendar.MINUTE, -period);
				Date aggTimeMinusPeriod = calendar.getTime();
				try {
					List<Sample> speedData = storageManager.readSampleData(
							getAnalyzerId(), getSpeed().getParameterId(),
							aggTimeMinusPeriod, false, aggTime, true, null);
					List<Sample> dirData = storageManager.readSampleData(
							getAnalyzerId(), getDirection().getParameterId(),
							aggTimeMinusPeriod, false, aggTime, true, null);
					if (speedData.isEmpty() && dirData.isEmpty()) {
						setLastAggregateValue(period, new DummyAggregateValue(
								aggTime));
						continue;
					}
					WindValue wv = computeWindAggregateValue(period, aggTime,
							speedData, dirData);
					setLastAggregateValue(period, wv);
					windAggregateValues.add(wv);
				} catch (StorageException e) {
					logger.error("Wind aggregate value computation failed");
				} catch (Exception e) {
					logger.error("Wind aggregate value computation failed", e);
				}
			}
			try {
				storageManager.saveWindAggregateData(getAnalyzerId(), this,
						period, windAggregateValues);
			} catch (StorageException e) {
				// Nothing is logged here because saveWindAggregateData
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
				List<WindValue> data = storageManager.readWindAggregateData(
						getAnalyzerId(), getParameterId(), period,
						aggregationsComputeStartTime, false, new Date(), true,
						null);
				if (!data.isEmpty()) {
					WindValue wv = data.get(data.size() - 1);
					setLastAggregateValue(period, wv);
					logger.debug("Last aggregate value for " + getParameterId()
							+ ", period " + period + " is: " + "Timestamp="
							+ wv.getTimestamp() + ", VSpeed="
							+ wv.getVectorialSpeed() + ", VDir="
							+ wv.getVectorialDirection() + ", DevStd="
							+ wv.getStandardDeviation() + ", Speed="
							+ wv.getScalarSpeed() + ", GustSpeed="
							+ wv.getGustSpeed() + ", GustDir="
							+ wv.getGustDirection() + ", CalmsPercent="
							+ wv.getCalmsNumberPercent() + ", IsCalm="
							+ wv.getCalm() + ", IsNotValid=" + wv.isNotvalid());
				}
			} catch (StorageException e) {
				logger.error("Error reading last aggregate value " + "for "
						+ getParameterId() + ", period " + period);
			}
		}
	}

	@Override
	void saveData() throws StorageException {
		StorageException speedSE = null;
		StorageException directionSE = null;
		try {
			speed.saveData();
		} catch (StorageException ex) {
			speedSE = ex;
		}
		try {
			direction.saveData();
		} catch (StorageException ex) {
			directionSE = ex;
		}
		if (speedSE != null)
			throw speedSE;
		if (directionSE != null)
			throw directionSE;
		// If both exceptions should happen keeping track of one is enough,
		// because related errors are already logged in lower level
	}

	@Override
	void setAnalyzerId(UUID analyzerId) {
		super.setAnalyzerId(analyzerId);
		speed.setAnalyzerId(analyzerId);
		direction.setAnalyzerId(analyzerId);
	}

}
