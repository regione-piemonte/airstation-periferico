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
// $Id: ElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

import java.io.Serializable;
import java.util.List;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ElementInfo implements Serializable {

	private static final long serialVersionUID = 5050150176771644150L;
	private String parameterId;
	private String analyzerName;
	private boolean enabled;
	private List<Integer> availableAvgPeriods;
	private List<Integer> selectedAvgPeriods;
	private Boolean calibrationRunning;
	private Boolean maintenanceRunning;
	private boolean activeInRunningCfg;

	public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}

	public String getAnalyzerName() {
		return analyzerName;
	}

	public void setAnalyzerName(String analyzerName) {
		this.analyzerName = analyzerName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<Integer> getAvailableAvgPeriods() {
		return availableAvgPeriods;
	}

	public void setAvailableAvgPeriods(List<Integer> availableAvgPeriods) {
		this.availableAvgPeriods = availableAvgPeriods;
	}

	public List<Integer> getSelectedAvgPeriods() {
		return selectedAvgPeriods;
	}

	public void setSelectedAvgPeriods(List<Integer> selectedAvgPeriods) {
		this.selectedAvgPeriods = selectedAvgPeriods;
	}

	public Boolean isCalibrationRunning() {
		return calibrationRunning;
	}

	public void setCalibrationRunning(Boolean calibrationRunning) {
		this.calibrationRunning = calibrationRunning;
	}

	public Boolean isMaintenanceRunning() {
		return maintenanceRunning;
	}

	public void setMaintenanceRunning(Boolean maintenanceRunning) {
		this.maintenanceRunning = maintenanceRunning;
	}

	public boolean isActiveInRunningCfg() {
		return activeInRunningCfg;
	}

	public void setActiveInRunningCfg(boolean activeInRunningCfg) {
		this.activeInRunningCfg = activeInRunningCfg;
	}

	@Override
	public String toString() {
		return "ElementInfo [parameterId=" + parameterId + ", analyzerName="
				+ analyzerName + ", enabled=" + enabled
				+ ", availableAvgPeriods=" + availableAvgPeriods
				+ ", selectedAvgPeriods=" + selectedAvgPeriods
				+ ", calibrationRunning=" + calibrationRunning
				+ ", maintenanceRunning=" + maintenanceRunning
				+ ", activeInRunningCfg=" + activeInRunningCfg + "]";
	}

	public String checkEmptyFields() {
		return null;
	}

	protected boolean empty(Object object) {
		return object instanceof String ? ((String) object).isEmpty()
				: object == null;
	}

}
