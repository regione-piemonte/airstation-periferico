/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: data type for elements for UI
// Change log:
//   2015-05-08: initial version
// ----------------------------------------------------------------------------
// $Id: SampleElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

import java.util.List;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class SampleElementInfo extends ScalarElementInfo {

	private static final long serialVersionUID = 265582106475455663L;
	private Integer acqPeriod;
	private Double rangeLow;
	private Double rangeHigh;
	private Double correctionCoefficient;
	private Double correctionOffset;
	private String analyzerMeasureUnit;
	private List<String> analyzerMeasureUnits;
	private String conversionInfo;
	private Double linearizationCoefficient;
	private Double linearizationOffset;

	public Integer getAcqPeriod() {
		return acqPeriod;
	}

	public void setAcqPeriod(Integer acqPeriod) {
		this.acqPeriod = acqPeriod;
	}

	public Double getRangeLow() {
		return rangeLow;
	}

	public void setRangeLow(Double rangeLow) {
		this.rangeLow = rangeLow;
	}

	public Double getRangeHigh() {
		return rangeHigh;
	}

	public void setRangeHigh(Double rangeHigh) {
		this.rangeHigh = rangeHigh;
	}

	public Double getCorrectionCoefficient() {
		return correctionCoefficient;
	}

	public void setCorrectionCoefficient(Double correctionCoefficient) {
		this.correctionCoefficient = correctionCoefficient;
	}

	public Double getCorrectionOffset() {
		return correctionOffset;
	}

	public void setCorrectionOffset(Double correctionOffset) {
		this.correctionOffset = correctionOffset;
	}

	public String getAnalyzerMeasureUnit() {
		return analyzerMeasureUnit;
	}

	public void setAnalyzerMeasureUnit(String analyzerMeasureUnit) {
		this.analyzerMeasureUnit = analyzerMeasureUnit;
	}

	public List<String> getAnalyzerMeasureUnits() {
		return analyzerMeasureUnits;
	}

	public void setAnalyzerMeasureUnits(List<String> analyzerMeasureUnits) {
		this.analyzerMeasureUnits = analyzerMeasureUnits;
	}

	public String getConversionInfo() {
		return conversionInfo;
	}

	public void setConversionInfo(String conversionInfo) {
		this.conversionInfo = conversionInfo;
	}

	public Double getLinearizationCoefficient() {
		return linearizationCoefficient;
	}

	public void setLinearizationCoefficient(Double linearizationCoefficient) {
		this.linearizationCoefficient = linearizationCoefficient;
	}

	public Double getLinearizationOffset() {
		return linearizationOffset;
	}

	public void setLinearizationOffset(Double linearizationOffset) {
		this.linearizationOffset = linearizationOffset;
	}

	@Override
	public String toString() {
		return "SampleElementInfo [acqPeriod=" + acqPeriod + ", rangeLow="
				+ rangeLow + ", rangeHigh=" + rangeHigh
				+ ", correctionCoefficient=" + correctionCoefficient
				+ ", correctionOffset=" + correctionOffset
				+ ", analyzerMeasureUnit=" + analyzerMeasureUnit
				+ ", analyzerMeasureUnits=" + analyzerMeasureUnits
				+ ", conversionInfo=" + conversionInfo
				+ ", linearizationCoefficient=" + linearizationCoefficient
				+ ", linearizationOffset=" + linearizationOffset
				+ ", getAcqMeasureUnit()=" + getAcqMeasureUnit()
				+ ", getAcqMeasureUnits()=" + getAcqMeasureUnits()
				+ ", getNumDec()=" + getNumDec() + ", getMinValue()="
				+ getMinValue() + ", getMaxValue()=" + getMaxValue()
				+ ", getParameterId()=" + getParameterId()
				+ ", getAnalyzerName()=" + getAnalyzerName() + ", isEnabled()="
				+ isEnabled() + ", getAvailableAvgPeriods()="
				+ getAvailableAvgPeriods() + ", getSelectedAvgPeriods()="
				+ getSelectedAvgPeriods() + ", isCalibrationRunning()="
				+ isCalibrationRunning() + ", isMaintenanceRunning()="
				+ isMaintenanceRunning() + ", isActiveInRunningCfg()="
				+ isActiveInRunningCfg() + "]";
	}

	@Override
	public String checkEmptyFields() {
		String error = super.checkEmptyFields();
		if (error != null)
			return error;
		if (empty(acqPeriod))
			error = "acq_period";
		else if (empty(correctionCoefficient))
			error = "correction_coefficient";
		else if (empty(correctionOffset))
			error = "correction_offset";
		else if (empty(linearizationCoefficient))
			error = "linearization_coefficient";
		else if (empty(linearizationOffset))
			error = "linearization_offset";
		return error;
	}

}
