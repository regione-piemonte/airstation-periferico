/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a measure unit
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: MeasureUnit.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common;

/**
 * Represents a measure unit
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class MeasureUnit extends ConfigItem {

	private static final long serialVersionUID = -2823524918570784917L;

	private static final int MIN_NAME_LEN = 1;

	private static final int MAX_NAME_LEN = 16;

	private String name = "";

	private String description = "";

	private String physicalDimension = "";

	private boolean allowedForAnalyzer = false;

	private boolean allowedForAcquisition = false;

	private double conversionMultiplyer = 1.0;

	private double conversionAddendum = 0.0;

	private String conversionFormula = null;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = trim(description);
	}

	public String getPhysicalDimension() {
		return physicalDimension;
	}

	public void setPhysicalDimension(String physicalDimension) {
		this.physicalDimension = trim(physicalDimension);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) throws ConfigException {
		this.name = checkLength("name", name, MIN_NAME_LEN, MAX_NAME_LEN);
	}

	public boolean isAllowedForAcquisition() {
		return allowedForAcquisition;
	}

	public void setAllowedForAcquisition(boolean allowedForAcquisition) {
		this.allowedForAcquisition = allowedForAcquisition;
	}

	public boolean isAllowedForAnalyzer() {
		return allowedForAnalyzer;
	}

	public void setAllowedForAnalyzer(boolean allowedForAnalyzer) {
		this.allowedForAnalyzer = allowedForAnalyzer;
	}

	public double getConversionAddendum() {
		return conversionAddendum;
	}

	public void setConversionAddendum(double conversionAddendum) {
		this.conversionAddendum = conversionAddendum;
	}

	public String getConversionFormula() {
		return conversionFormula;
	}

	public void setConversionFormula(String conversionFormula) {
		this.conversionFormula = trim(conversionFormula);
	}

	public double getConversionMultiplyer() {
		return conversionMultiplyer;
	}

	public void setConversionMultiplyer(double conversionMultiplyer) {
		this.conversionMultiplyer = conversionMultiplyer;
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkLength("name", name, MIN_NAME_LEN, MAX_NAME_LEN);
	}

}
