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
// $Id: AnalogElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AnalogElementInfo extends SampleElementInfo {

	private static final long serialVersionUID = -2101854273188517070L;
	private String boardBindInfo;

	public String getBoardBindInfo() {
		return boardBindInfo;
	}

	public void setBoardBindInfo(String boardBindInfo) {
		this.boardBindInfo = boardBindInfo;
	}

	@Override
	public String toString() {
		return "AnalogElementInfo [boardBindInfo=" + boardBindInfo
				+ ", getAcqPeriod()=" + getAcqPeriod() + ", getRangeLow()="
				+ getRangeLow() + ", getRangeHigh()=" + getRangeHigh()
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

	@Override
	public String checkEmptyFields() {
		String error = super.checkEmptyFields();
		if (error != null)
			return error;
		if (empty(getRangeLow()))
			error = "range_low";
		else if (empty(getRangeHigh()))
			error = "range_high";
		return error;
	}

}
