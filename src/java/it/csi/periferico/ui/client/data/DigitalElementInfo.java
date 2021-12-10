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
// $Id: DigitalElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DigitalElementInfo extends SampleElementInfo {

	private static final long serialVersionUID = 6318020404652947957L;
	private Boolean discardDataNotValidForAnalyzer;

	public Boolean getDiscardDataNotValidForAnalyzer() {
		return discardDataNotValidForAnalyzer;
	}

	public void setDiscardDataNotValidForAnalyzer(
			Boolean discardDataNotValidForAnalyzer) {
		this.discardDataNotValidForAnalyzer = discardDataNotValidForAnalyzer;
	}

	@Override
	public String toString() {
		return "DigitalElementInfo [discardDataNotValidForAnalyzer="
				+ discardDataNotValidForAnalyzer + ", getAcqPeriod()="
				+ getAcqPeriod() + ", getRangeLow()=" + getRangeLow()
				+ ", getRangeHigh()=" + getRangeHigh()
				+ ", getCorrectionCoefficient()=" + getCorrectionCoefficient()
				+ ", getCorrectionOffset()=" + getCorrectionOffset()
				+ ", getAnalyzerMeasureUnit()=" + getAnalyzerMeasureUnit()
				+ ", getAnalyzerMeasureUnits()=" + getAnalyzerMeasureUnits()
				+ ", getConversionInfo()=" + getConversionInfo()
				+ ", getLinearizationCoefficient()="
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

}
