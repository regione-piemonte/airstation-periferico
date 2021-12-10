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
// $Id: AnalogAvgElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AnalogAvgElementInfo extends AnalogElementInfo {

	private static final long serialVersionUID = 6195880387135721629L;
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
		return "AnalogAvgElementInfo [avgPeriod=" + avgPeriod + ", acqDelay="
				+ acqDelay + ", acqDuration=" + acqDuration
				+ ", getRangeLow()=" + getRangeLow() + ", getRangeHigh()="
				+ getRangeHigh() + ", getBoardBindInfo()=" + getBoardBindInfo()
				+ ", getAcqPeriod()=" + getAcqPeriod()
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
