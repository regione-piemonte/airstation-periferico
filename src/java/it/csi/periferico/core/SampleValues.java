/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents all the values related to a single sample
// Change log:
//   2008-11-06: initial version
// ----------------------------------------------------------------------------
// $Id: SampleValues.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Represents all the values related to a single sample
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class SampleValues {

	private Double voltage;

	private double rawValue;

	private double correctedValue;

	private double convertedValue;

	private double finalValue;

	public SampleValues(double rawValue, double correctedValue,
			double convertedValue, double finalValue) {
		this(null, rawValue, correctedValue, convertedValue, finalValue);
	}

	public SampleValues(Double voltage, double rawValue, double correctedValue,
			double convertedValue, double finalValue) {
		this.voltage = voltage;
		this.rawValue = rawValue;
		this.correctedValue = correctedValue;
		this.convertedValue = convertedValue;
		this.finalValue = finalValue;
	}

	public Double getVoltage() {
		return voltage;
	}

	public double getRawValue() {
		return rawValue;
	}

	public double getCorrectedValue() {
		return correctedValue;
	}

	public double getConvertedValue() {
		return convertedValue;
	}

	public double getFinalValue() {
		return finalValue;
	}

}
