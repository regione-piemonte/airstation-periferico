/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a generic configuration item
// Change log:
//   2008-03-06: initial version
// ----------------------------------------------------------------------------
// $Id: ConfigItem.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common;

import java.io.Serializable;

/**
 * Represents a generic configuration item
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ConfigItem implements Serializable {

	private static final long serialVersionUID = -5292850424059391049L;

	public abstract void checkConfig() throws ConfigException;

	public void initConfig() {
	}

	protected String checkLength(String name, String value, int max)
			throws ConfigException {
		return (checkLength(name, value, 0, max));
	}

	protected String checkLength(String name, String value, int min, int max)
			throws ConfigException {
		value = trim(value);
		if (value.isEmpty())
			throw new ConfigException("not_empty", name);
		if (value.length() < min)
			throw new ConfigException("min_length", name, min);
		if (value.length() > max)
			throw new ConfigException("max_length", name, max);
		return value;
	}

	protected String trim(String value) {
		if (value == null)
			return "";
		return value.trim();
	}

	protected String trimcrlf(String value) {
		if (value == null)
			return "";
		return value.trim().replaceAll("\r?\n", " ");
	}

	protected boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2)
			return true;
		if (obj1 == null || obj2 == null)
			return false;
		return obj1.equals(obj2);
	}

}
