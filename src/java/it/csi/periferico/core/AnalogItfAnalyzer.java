/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for analyzers with analog interface
// Change log:
//   2008-03-11: initial version
// ----------------------------------------------------------------------------
// $Id: AnalogItfAnalyzer.java,v 1.18 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.config.common.ConfigException;

/**
 * Base class for analyzers with analog interface
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class AnalogItfAnalyzer extends IOItfAnalyzer implements
		SampleElementHolder {

	private static final long serialVersionUID = -3856168837412494209L;

	private double minVoltage;

	private double maxVoltage;

	private boolean minRangeExtension = true;

	private boolean maxRangeExtension = false;

	private boolean differentialModeNeeded = false;

	public double getMaxVoltage() {
		return maxVoltage;
	}

	public void setMaxVoltage(double maxVoltage) {
		this.maxVoltage = maxVoltage;
	}

	public double getMinVoltage() {
		return minVoltage;
	}

	public void setMinVoltage(double minVoltage) {
		this.minVoltage = minVoltage;
	}

	public void setConfig(String name, String brand, String description,
			String model, String serialNumber, String userNotes,
			String strStatus, String uiURL, double minVoltage, double maxVoltage,
			boolean minRangeExtension, boolean maxRangeExtension,
			boolean differentialModeNeeded) throws ConfigException {
		checkRanges(minVoltage, maxVoltage, minRangeExtension,
				maxRangeExtension);
		super.setConfig(name, brand, description, model, serialNumber,
				userNotes, strStatus, uiURL);
		setMinVoltage(minVoltage);
		setMaxVoltage(maxVoltage);
		setMinRangeExtension(minRangeExtension);
		setMaxRangeExtension(maxRangeExtension);
		setDifferentialModeNeeded(differentialModeNeeded);
	}

	public boolean isSameConfig(String name, String brand, String description,
			String model, String serialNumber, String userNotes,
			String strStatus, String uiURL, double minVoltage, double maxVoltage,
			boolean minRangeExtension, boolean maxRangeExtension,
			boolean differentialModeNeeded) {
		return super.isSameConfig(name, brand, description, model,
				serialNumber, userNotes, strStatus, uiURL)
				&& this.minVoltage == minVoltage
				&& this.maxVoltage == maxVoltage
				&& this.minRangeExtension == minRangeExtension
				&& this.maxRangeExtension == maxRangeExtension
				&& this.differentialModeNeeded == differentialModeNeeded;
	}

	public boolean isDifferentialModeNeeded() {
		return differentialModeNeeded;
	}

	public void setDifferentialModeNeeded(boolean differentialModeNeeded) {
		this.differentialModeNeeded = differentialModeNeeded;
	}

	public boolean getMinRangeExtension() {
		return minRangeExtension;
	}

	public void setMinRangeExtension(boolean minRangeExtension) {
		this.minRangeExtension = minRangeExtension;
	}

	public boolean getMaxRangeExtension() {
		return maxRangeExtension;
	}

	public void setMaxRangeExtension(boolean maxRangeExtension) {
		this.maxRangeExtension = maxRangeExtension;
	}

	public abstract boolean hasBindedElements();

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
		checkRanges(minVoltage, maxVoltage, minRangeExtension,
				maxRangeExtension);
	}

	private void checkRanges(double minVoltage, double maxVoltage,
			boolean minRangeExtension, boolean maxRangeExtension)
			throws ConfigException {
		if (maxVoltage <= minVoltage)
			throw new ConfigException("analog_range_error");
		isRangeSupportedByIOProviders(minVoltage, maxVoltage,
				minRangeExtension, maxRangeExtension);
	}

	abstract void isRangeSupportedByIOProviders(double minVoltage,
			double maxVoltage, boolean minRangeExtension,
			boolean maxRangeExtension) throws ConfigException;

}
