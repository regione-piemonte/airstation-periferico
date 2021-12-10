/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: element for analyzers with data port interface
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: DataPortElement.java,v 1.25 2015/11/04 16:35:34 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.acqdrivers.DriverManager;
import it.csi.periferico.acqdrivers.itf.ElementInterface;
import it.csi.periferico.acqdrivers.itf.ElementValue;
import it.csi.periferico.config.common.ConfigException;

/**
 * Element for analyzers with data port interface
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DataPortElement extends GenericSampleElement implements
		ElementInterface {

	private static final long serialVersionUID = 2562849002994361578L;

	private Double rangeLow = null;

	private Double rangeHigh = null;

	private Boolean discardDataNotValidForAnalyzer = null;

	private DataPortElementHolder elementHolder = null;

	public Double getRangeHigh() {
		return rangeHigh;
	}

	public void setRangeHigh(Double rangeHigh) {
		this.rangeHigh = rangeHigh;
	}

	public Double getRangeLow() {
		return rangeLow;
	}

	public void setRangeLow(Double rangeLow) {
		this.rangeLow = rangeLow;
	}

	public Boolean getDiscardDataNotValidForAnalyzer() {
		return discardDataNotValidForAnalyzer;
	}

	public void setDiscardDataNotValidForAnalyzer(
			Boolean discardDataNotValidForAnalyzer) {
		this.discardDataNotValidForAnalyzer = discardDataNotValidForAnalyzer;
	}

	void setElementHolder(DataPortElementHolder elementHolder) {
		this.elementHolder = elementHolder;
		super.setElementHolder(elementHolder);
	}

	DataPortElementHolder getElementHolder() {
		return elementHolder;
	}

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

	public void setConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset,
			Double rangeLow, Double rangeHigh,
			Boolean discardDataNotValidForAnalyzer) throws ConfigException {
		checkRanges(rangeLow, rangeHigh);
		checkDriverDependantConfig(getParameterId());
		super.setConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals, analyzerMeasureUnitName, acqPeriod,
				correctionCoefficient, correctionOffset,
				linearizationCoefficient, linearizationOffset);
		setRangeLow(rangeLow);
		setRangeHigh(rangeHigh);
		setDiscardDataNotValidForAnalyzer(discardDataNotValidForAnalyzer);
	}

	public boolean isSameConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset,
			Double rangeLow, Double rangeHigh,
			Boolean discardDataNotValidForAnalyzer) {
		return super.isSameConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals, analyzerMeasureUnitName, acqPeriod,
				correctionCoefficient, correctionOffset,
				linearizationCoefficient, linearizationOffset)
				&& equals(this.rangeLow, rangeLow)
				&& equals(this.rangeHigh, rangeHigh)
				&& equals(this.discardDataNotValidForAnalyzer,
						discardDataNotValidForAnalyzer);
	}

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
		checkRanges(rangeLow, rangeHigh);
		checkDriverDependantConfig(getParameterId());
	}

	private void checkRanges(Double rangeLow, Double rangeHigh)
			throws ConfigException {
		if (rangeLow == null || rangeHigh == null)
			return;
		if (rangeLow >= rangeHigh)
			throw new ConfigException("range_error");
	}

	private void checkDriverDependantConfig(String parameterId)
			throws ConfigException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		if (!dm.isParameterDefined(elementHolder.getBrand(),
				elementHolder.getModel(), parameterId))
			throw new ConfigException("param_undef_in_driver_for", parameterId,
					elementHolder.getBrand(), elementHolder.getModel());
	}

	public boolean deliver(ElementValue elementValue) {
		if (elementHolder == null)
			throw new IllegalStateException("ElementHolder must be initialized");
		if (getConverter() == null)
			throw new IllegalStateException("Converter must be initialized");
		if (elementValue == null)
			return false;
		elementHolder.updateAutoCheckRunning(elementValue.isCalibration());
		SampleValues sv = null;
		int flags = 0;
		if (elementHolder.isMaintenanceInProgress())
			flags |= ValidationFlag.ANALYZER_MAINTENANCE;
		if (elementHolder.isManualCalibrationRunning())
			flags |= ValidationFlag.ANALYZER_MANUAL_CALIB;
		if (elementValue.isCalibration())
			flags |= ValidationFlag.ANALYZER_AUTO_CALIB;
		if (!elementHolder.isEnvironmentOK())
			flags |= ValidationFlag.ENVIRONMENT_NOT_OK;
		if (isCalibAutoCheckFailed())
			flags |= ValidationFlag.CHANNEL_UNTRIMMED;
		if (elementValue.getError()) {
			if (Boolean.TRUE.equals(elementHolder.getFaultActive()))
				flags |= ValidationFlag.ANALYZER_FAULT;
			flags |= ValidationFlag.ACQ_ERROR;
		} else {
			if (elementHolder.isFaultActive())
				flags |= ValidationFlag.ANALYZER_FAULT;
			if (!elementValue.isValid())
				flags |= ValidationFlag.ANALYZER_DATA_NOT_VALID;
			sv = computeSampleValues(elementValue.getValue());
			if (sv.getFinalValue() < getMinValue()
					|| sv.getFinalValue() > getMaxValue())
				flags |= ValidationFlag.VALUE_OUT_OF_RANGE;
		}
		boolean notvalid = sv == null
				|| (flags & ValidationFlag.ANALYZER_MAINTENANCE) != 0
				|| (flags & ValidationFlag.ANALYZER_MANUAL_CALIB) != 0
				|| (flags & ValidationFlag.ANALYZER_AUTO_CALIB) != 0
				|| (flags & ValidationFlag.VALUE_OUT_OF_RANGE) != 0;
		if (Boolean.TRUE.equals(discardDataNotValidForAnalyzer)
				&& (flags & ValidationFlag.ANALYZER_DATA_NOT_VALID) != 0)
			notvalid = true;

		Sample sample = new Sample(elementValue.getTimestamp(), sv, notvalid,
				flags);
		addNewSample(sample, sv);
		return true;
	}

	private SampleValues computeSampleValues(double rawValue) {
		double correctedValue = rawValue * getCorrectionCoefficient()
				+ getCorrectionOffset();
		double convertedValue = getConverter().convert(correctedValue);
		double linearizedValue = convertedValue * getLinearizationCoefficient()
				+ getLinearizationOffset();
		return new SampleValues(rawValue, correctedValue, convertedValue,
				Periferico.roundSampleData(linearizedValue, getNumDec()));
	}

	public boolean isActive() {
		if (elementHolder != null && elementHolder.isEnabled())
			return isEnabled();
		return false;
	}

	@Override
	public boolean isReady() {
		return true;
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
	public Double getCalib1stPointPercent() throws ConfigException {
		if (rangeLow == null || rangeHigh == null)
			throw new ConfigException("percentage_needs_range");
		Double point = getCalib1stPoint();
		if (point == null)
			return null;
		return (point - rangeLow) * 100.0 / (rangeHigh - rangeLow);
	}

	@Override
	public Double getCalib2ndPointPercent() throws ConfigException {
		if (rangeLow == null || rangeHigh == null)
			throw new ConfigException("percentage_needs_range");
		Double point = getCalib2ndPoint();
		if (point == null)
			return null;
		return (point - rangeLow) * 100.0 / (rangeHigh - rangeLow);
	}

	@Override
	public void setCalib1stPointPercent(Double calib1stPointPercent)
			throws ConfigException {
		if (rangeLow == null || rangeHigh == null)
			throw new ConfigException("percentage_needs_range");
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
		if (rangeLow == null || rangeHigh == null)
			throw new ConfigException("percentage_needs_range");
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
		return rangeLow != null && rangeHigh != null;
	}

}
