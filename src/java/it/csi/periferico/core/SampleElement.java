/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: defines a sample element
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: SampleElement.java,v 1.48 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.boards.AIUser;
import it.csi.periferico.boards.AIValue;
import it.csi.periferico.boards.AnalogInput;
import it.csi.periferico.boards.BoardBindInfo;
import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.config.common.ConfigException;

/**
 * Defines a sample element
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class SampleElement extends GenericSampleElement implements AIUser {

	private static final long serialVersionUID = 4355844272044785508L;

	private double rangeLow = 0.0;

	private double rangeHigh;

	private IOProvider ioProvider;

	private BoardBindInfo boardBindInfo;

	private SampleElementHolder sampleElementHolder = null;

	public String getBindLabel() {
		if (sampleElementHolder == null)
			throw new IllegalStateException(
					"SampleElementHolder must be initialized");
		return sampleElementHolder.getName() + "/" + getParameterId();
	}

	public String getBindIdentifier() {
		if (sampleElementHolder == null)
			throw new IllegalStateException(
					"SampleElementHolder must be initialized");
		return sampleElementHolder.getId() + "/" + getParameterId();
	}

	public IOProvider getIOProvider() {
		return ioProvider;
	}

	public void setIOProvider(IOProvider ioProvider) {
		this.ioProvider = ioProvider;
	}

	public double getRangeHigh() {
		return rangeHigh;
	}

	public void setRangeHigh(double rangeHigh) {
		this.rangeHigh = rangeHigh;
	}

	public double getRangeLow() {
		return rangeLow;
	}

	public void setRangeLow(double rangeLow) {
		this.rangeLow = rangeLow;
	}

	public BoardBindInfo getBoardBindInfo() {
		return boardBindInfo;
	}

	public void setBoardBindInfo(BoardBindInfo boardBindInfo) {
		this.boardBindInfo = boardBindInfo;
	}

	public void setConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset,
			double rangeLow, double rangeHigh) throws ConfigException {
		checkRanges(rangeLow, rangeHigh);
		super.setConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals, analyzerMeasureUnitName, acqPeriod,
				correctionCoefficient, correctionOffset,
				linearizationCoefficient, linearizationOffset);
		setRangeLow(rangeLow);
		setRangeHigh(rangeHigh);
	}

	public boolean isSameConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset,
			double rangeLow, double rangeHigh) {
		return super.isSameConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals, analyzerMeasureUnitName, acqPeriod,
				correctionCoefficient, correctionOffset,
				linearizationCoefficient, linearizationOffset)
				&& this.rangeLow == rangeLow && this.rangeHigh == rangeHigh;
	}

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
		checkRanges(rangeLow, rangeHigh);
	}

	void setSampleElementHolder(SampleElementHolder sampleElementHolder) {
		this.sampleElementHolder = sampleElementHolder;
		super.setElementHolder(sampleElementHolder);
	}

	private void checkRanges(double rangeLow, double rangeHigh)
			throws ConfigException {
		if (rangeLow >= rangeHigh)
			throw new ConfigException("range_error");
	}

	public boolean isDifferentialModeNeeded() {
		if (sampleElementHolder == null)
			throw new IllegalStateException(
					"SampleElementHolder must be initialized");
		return sampleElementHolder.isDifferentialModeNeeded();
	}

	public boolean getMinRangeExtension() {
		if (sampleElementHolder == null)
			throw new IllegalStateException(
					"SampleElementHolder must be initialized");
		return sampleElementHolder.getMinRangeExtension();
	}

	public boolean getMaxRangeExtension() {
		if (sampleElementHolder == null)
			throw new IllegalStateException(
					"SampleElementHolder must be initialized");
		return sampleElementHolder.getMaxRangeExtension();
	}

	public boolean isActive() {
		if (sampleElementHolder == null)
			throw new IllegalStateException(
					"SampleElementHolder must be initialized");
		if (!sampleElementHolder.isEnabled())
			return false;
		if (!isEnabled())
			return false;
		return true;
	}

	public double getEndOfScale() {
		return rangeHigh;
	}

	public double getStartOfScale() {
		return rangeLow;
	}

	public double getMaxVoltage() {
		return sampleElementHolder.getMaxVoltage();
	}

	public double getMinVoltage() {
		return sampleElementHolder.getMinVoltage();
	}

	public boolean deliver(AIValue aiValue) {
		if (sampleElementHolder == null)
			throw new IllegalStateException(
					"SampleElementHolder must be initialized");
		if (getConverter() == null)
			throw new IllegalStateException("Converter must be initialized");
		if (aiValue == null)
			return false;
		SampleValues sv = null;
		int flags = 0;
		if (sampleElementHolder.isFaultActive())
			flags |= ValidationFlag.ANALYZER_FAULT;
		if (!sampleElementHolder.isDataValidActive())
			flags |= ValidationFlag.ANALYZER_DATA_NOT_VALID;
		if (sampleElementHolder.isMaintenanceInProgress())
			flags |= ValidationFlag.ANALYZER_MAINTENANCE;
		if (sampleElementHolder.isManualCalibrationRunning())
			flags |= ValidationFlag.ANALYZER_MANUAL_CALIB;
		if (sampleElementHolder.isAutoCheckRunning())
			flags |= ValidationFlag.ANALYZER_AUTO_CALIB;
		if (!sampleElementHolder.isEnvironmentOK())
			flags |= ValidationFlag.ENVIRONMENT_NOT_OK;
		if (isCalibAutoCheckFailed())
			flags |= ValidationFlag.CHANNEL_UNTRIMMED;
		if (aiValue.getError())
			flags |= ValidationFlag.ACQ_ERROR;
		else if (aiValue.isVoltageOutOfRange())
			flags |= ValidationFlag.ACQ_OUT_OF_SCALE;
		else {
			sv = computeSampleValues(aiValue.getVoltage());
			if (sv.getFinalValue() < getMinValue()
					|| sv.getFinalValue() > getMaxValue())
				flags |= ValidationFlag.VALUE_OUT_OF_RANGE;
		}
		boolean notvalid = sv == null
				|| (flags & ValidationFlag.ANALYZER_MAINTENANCE) != 0
				|| (flags & ValidationFlag.ANALYZER_MANUAL_CALIB) != 0
				|| (flags & ValidationFlag.ANALYZER_AUTO_CALIB) != 0
				|| (flags & ValidationFlag.VALUE_OUT_OF_RANGE) != 0;
		if (!sampleElementHolder.isDataValidWarningOnly()
				&& (flags & ValidationFlag.ANALYZER_DATA_NOT_VALID) != 0)
			notvalid = true;

		Sample sample = new Sample(aiValue.getTimestamp(), sv, notvalid, flags);
		addNewSample(sample, sv, aiValue.isExtra());
		return true;
	}

	private SampleValues computeSampleValues(double voltage) {
		double rawValue = getStartOfScale() + (voltage - getMinVoltage())
				/ (getMaxVoltage() - getMinVoltage())
				* (getEndOfScale() - getStartOfScale());
		double correctedValue = rawValue * getCorrectionCoefficient()
				+ getCorrectionOffset();
		double convertedValue = getConverter().convert(correctedValue);
		double linearizedValue = convertedValue * getLinearizationCoefficient()
				+ getLinearizationOffset();
		return new SampleValues(voltage, rawValue, correctedValue,
				convertedValue, Periferico.roundSampleData(linearizedValue,
						getNumDec()));
	}

	@Override
	public boolean isCalibActive() {
		if (sampleElementHolder == null)
			throw new IllegalStateException(
					"SampleElementHolder must be initialized");
		return sampleElementHolder.isManualCalibrationRunning();
	}

	@Override
	public void setCalib1stPoint(Double calib1stPoint) throws ConfigException {
		if (calib1stPoint != null
				&& (calib1stPoint < rangeLow || calib1stPoint > rangeHigh))
			throw new ConfigException("calib_point_out_of_range");
		super.setCalib1stPoint(calib1stPoint);
	}

	@Override
	public void setCalib2ndPoint(Double calib2ndPoint) throws ConfigException {
		if (calib2ndPoint != null
				&& (calib2ndPoint < rangeLow || calib2ndPoint > rangeHigh))
			throw new ConfigException("calib_point_out_of_range");
		super.setCalib2ndPoint(calib2ndPoint);
	}

	@Override
	public Double getCalib1stPointPercent() {
		Double point = getCalib1stPoint();
		if (point == null)
			return null;
		return (point - rangeLow) * 100.0 / (rangeHigh - rangeLow);
	}

	@Override
	public Double getCalib2ndPointPercent() {
		Double point = getCalib2ndPoint();
		if (point == null)
			return null;
		return (point - rangeLow) * 100.0 / (rangeHigh - rangeLow);
	}

	@Override
	public void setCalib1stPointPercent(Double calib1stPointPercent)
			throws ConfigException {
		if (calib1stPointPercent == null)
			setCalib1stPoint(null);
		if (calib1stPointPercent < 0.0 || calib1stPointPercent > 100.0)
			throw new ConfigException("calib_point_invalid_percentage");
		Double point = rangeLow
				+ ((rangeHigh - rangeLow) * calib1stPointPercent / 100.0);
		setCalib1stPoint(point);
	}

	@Override
	public void setCalib2ndPointPercent(Double calib2ndPointPercent)
			throws ConfigException {
		if (calib2ndPointPercent == null)
			setCalib2ndPoint(null);
		if (calib2ndPointPercent < 0.0 || calib2ndPointPercent > 100.0)
			throw new ConfigException("calib_point_invalid_percentage");
		Double point = rangeLow
				+ ((rangeHigh - rangeLow) * calib2ndPointPercent / 100.0);
		setCalib2ndPoint(point);
	}

	@Override
	public boolean isPercentageUsable() {
		return true;
	}

	void isRangeSupportedByIOProvider(double minVoltage, double maxVoltage,
			boolean minRangeExtension, boolean maxRangeExtension)
			throws ConfigException {
		if (ioProvider instanceof AnalogInput) {
			AnalogInput ai = (AnalogInput) ioProvider;
			if (!ai.isRangeSupported(minVoltage, maxVoltage, minRangeExtension,
					maxRangeExtension))
				throw new ConfigException(
						"analog_range_not_supported_by_current_bindings");
		}
	}

}
