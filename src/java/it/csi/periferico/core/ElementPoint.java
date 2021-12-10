/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: calibration point associated to an element
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: ElementPoint.java,v 1.7 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

/**
 * Calibration point associated to an element
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class ElementPoint extends ConfigItem {

	private static final long serialVersionUID = -913895085988874162L;

	private boolean enabled;

	private String parameterId;

	// TODO: for future implementation ...
	// private SampleElement element;

	private double cylinderValue;

	private int alarmThreshold; // full scale range percent

	public int getAlarmThreshold() {
		return alarmThreshold;
	}

	public void setAlarmThreshold(int alarmThreshold) {
		this.alarmThreshold = alarmThreshold;
	}

	public double getCylinderValue() {
		return cylinderValue;
	}

	public void setCylinderValue(double cylinderValue) {
		this.cylinderValue = cylinderValue;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}

	public void setConfig(boolean enabled, double cylinderValue,
			int alarmThreshold) {
		setEnabled(enabled);
		setCylinderValue(cylinderValue);
		setAlarmThreshold(alarmThreshold);
	}

	public boolean isSameConfig(boolean enabled, double cylinderValue,
			int alarmThreshold) {
		return this.enabled == enabled && this.cylinderValue == cylinderValue
				&& this.alarmThreshold == alarmThreshold;
	}

	@Override
	public void checkConfig() throws ConfigException {
	}

}
