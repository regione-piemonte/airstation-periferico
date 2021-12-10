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
// $Id: ScalarElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

import java.util.List;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ScalarElementInfo extends ElementInfo {

	private static final long serialVersionUID = -3682074924082674146L;
	private String acqMeasureUnit;
	private List<String> acqMeasureUnits;
	private Integer numDec;
	private Double minValue;
	private Double maxValue;

	public String getAcqMeasureUnit() {
		return acqMeasureUnit;
	}

	public void setAcqMeasureUnit(String acqMeasureUnit) {
		this.acqMeasureUnit = acqMeasureUnit;
	}

	public List<String> getAcqMeasureUnits() {
		return acqMeasureUnits;
	}

	public void setAcqMeasureUnits(List<String> acqMeasureUnits) {
		this.acqMeasureUnits = acqMeasureUnits;
	}

	public Integer getNumDec() {
		return numDec;
	}

	public void setNumDec(Integer numDec) {
		this.numDec = numDec;
	}

	public Double getMinValue() {
		return minValue;
	}

	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public String toString() {
		return "ScalarElementInfo [acqMeasureUnit=" + acqMeasureUnit
				+ ", acqMeasureUnits=" + acqMeasureUnits + ", numDec=" + numDec
				+ ", minValue=" + minValue + ", maxValue=" + maxValue
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
		if (empty(numDec))
			error = "num_dec";
		else if (empty(minValue))
			error = "min_value";
		else if (empty(maxValue))
			error = "max_value";
		return error;
	}

}
