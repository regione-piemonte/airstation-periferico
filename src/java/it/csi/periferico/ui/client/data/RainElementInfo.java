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
// $Id: RainElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class RainElementInfo extends ScalarElementInfo {

	private static final long serialVersionUID = 7079367857863000347L;
	private Double valueForEvent;
	private boolean acqOnRisingEdge;
	private String boardBindInfo;

	public Double getValueForEvent() {
		return valueForEvent;
	}

	public void setValueForEvent(Double valueForEvent) {
		this.valueForEvent = valueForEvent;
	}

	public boolean isAcqOnRisingEdge() {
		return acqOnRisingEdge;
	}

	public void setAcqOnRisingEdge(boolean acqOnRisingEdge) {
		this.acqOnRisingEdge = acqOnRisingEdge;
	}

	public String getBoardBindInfo() {
		return boardBindInfo;
	}

	public void setBoardBindInfo(String boardBindInfo) {
		this.boardBindInfo = boardBindInfo;
	}

	@Override
	public String toString() {
		return "RainElementInfo [valueForEvent=" + valueForEvent
				+ ", acqOnRisingEdge=" + acqOnRisingEdge + ", boardBindInfo="
				+ boardBindInfo + ", getAcqMeasureUnit()="
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
