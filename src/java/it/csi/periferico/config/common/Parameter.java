/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a parameter
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: Parameter.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common;

/**
 * Represents a parameter
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Parameter extends ConfigItem {

	private static final long serialVersionUID = -7598866560204142888L;

	private static final int MIN_ID_LEN = 1;

	private static final int MAX_ID_LEN = 16;

	public enum ParamType {
		CHEMICAL, METEO, WIND, WIND_DIR, WIND_VEL, RAIN, OTHER
	};

	private String id;

	private String name;

	private ParamType type;

	private String physicalDimension = "";

	private Double molecularWeight = null;

	public Parameter() {
		this("", "", ParamType.CHEMICAL, "");
	}

	public Parameter(String id, String name, ParamType type,
			String physicalDimension) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.physicalDimension = physicalDimension;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) throws ConfigException {
		this.id = checkLength("id", id, MIN_ID_LEN, MAX_ID_LEN);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = trim(name);
	}

	public ParamType getType() {
		return type;
	}

	public void setType(ParamType type) {
		this.type = type;
	}

	public String getTypeAsString() {
		return type.toString().toLowerCase();
	}

	public void setTypeAsString(String str) {
		str = trim(str);
		this.type = ParamType.valueOf(str.toUpperCase());
	}

	public String getPhysicalDimension() {
		return physicalDimension;
	}

	public void setPhysicalDimension(String physicalDimension) {
		this.physicalDimension = trim(physicalDimension);
	}

	public Double getMolecularWeight() {
		return molecularWeight;
	}

	public void setMolecularWeight(Double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkLength("id", id, MIN_ID_LEN, MAX_ID_LEN);
	}

}
