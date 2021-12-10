/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: anagraphic information for an alarm
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: AlarmName.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common;

/**
 * Anagraphic information for an alarm
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class AlarmName extends ConfigItem {

	private static final long serialVersionUID = -1863771137403288860L;

	private static final int MIN_ID_LEN = 1;

	private static final int MAX_ID_LEN = 16;

	public enum AlarmType {
		DIGITAL, TRIGGER
	};

	private String id = "";

	private String name = "";

	private AlarmType type = AlarmType.DIGITAL;

	private boolean dataQualityRelevant;

	public boolean isDataQualityRelevant() {
		return dataQualityRelevant;
	}

	public void setDataQualityRelevant(boolean dataQualityRelevant) {
		this.dataQualityRelevant = dataQualityRelevant;
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

	public AlarmType getType() {
		return type;
	}

	public void setType(AlarmType type) {
		this.type = type;
	}

	public String getTypeAsString() {
		return type.toString().toLowerCase();
	}

	public void setTypeAsString(String str) {
		str = trim(str);
		this.type = AlarmType.valueOf(str.toUpperCase());
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkLength("id", id, MIN_ID_LEN, MAX_ID_LEN);
	}

}
