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
// $Id: DigitalAvgElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DigitalAvgElementInfo extends DigitalElementInfo {

	private static final long serialVersionUID = -5763672966079737199L;
	private Integer avgPeriod;
	private Integer acqDelay;
	private Integer acqDuration;

	public Integer getAvgPeriod() {
		return avgPeriod;
	}

	public void setAvgPeriod(Integer avgPeriod) {
		this.avgPeriod = avgPeriod;
	}

	public Integer getAcqDelay() {
		return acqDelay;
	}

	public void setAcqDelay(Integer acqDelay) {
		this.acqDelay = acqDelay;
	}

	public Integer getAcqDuration() {
		return acqDuration;
	}

	public void setAcqDuration(Integer acqDuration) {
		this.acqDuration = acqDuration;
	}

	@Override
	public String toString() {
		return "DigitalAvgElementInfo [avgPeriod=" + avgPeriod + ", acqDelay="
				+ acqDelay + ", acqDuration=" + acqDuration
				+ ", getRangeLow()=" + getRangeLow() + ", getRangeHigh()="
				+ getRangeHigh() + ", getDiscardDataNotValidForAnalyzer()="
				+ getDiscardDataNotValidForAnalyzer() + ", getAcqPeriod()="
				+ getAcqPeriod() + ", getCorrectionCoefficient()="
				+ getCorrectionCoefficient() + ", getCorrectionOffset()="
				+ getCorrectionOffset() + ", getAnalyzerMeasureUnit()="
				+ getAnalyzerMeasureUnit() + ", getAnalyzerMeasureUnits()="
				+ getAnalyzerMeasureUnits() + ", getConversionInfo()="
				+ getConversionInfo() + ", getLinearizationCoefficient()="
				+ getLinearizationCoefficient() + ", getLinearizationOffset()="
				+ getLinearizationOffset() + ", getAcqMeasureUnit()="
				+ getAcqMeasureUnit() + ", getAcqMeasureUnits()="
				+ getAcqMeasureUnits() + ", getNumDec()=" + getNumDec()
				+ ", getMinValue()=" + getMinValue() + ", getMaxValue()="
				+ getMaxValue() + ", getParameterId()=" + getParameterId()
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
		if (empty(avgPeriod))
			error = "acq_period_for_Avg";
		else if (empty(acqDelay))
			error = "acq_delay";
		else if (empty(acqDuration))
			error = "acq_duration";
		return error;
	}

}
