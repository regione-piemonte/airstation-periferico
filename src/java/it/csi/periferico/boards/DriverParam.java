/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the value for a given driver parameter
// Change log:
//   2008-04-16: initial version
// ----------------------------------------------------------------------------
// $Id: DriverParam.java,v 1.5 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

/**
 * Represents the value for a given driver parameter
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DriverParam extends ConfigItem {

	private static final long serialVersionUID = -510158954225565742L;

	private String name = "";

	private String value = "";

	public DriverParam() {
	}

	public DriverParam(String name, String value) {
		this.name = trim(name);
		this.value = trim(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = trim(name);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = trim(value);
	}

	@Override
	public void checkConfig() throws ConfigException {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DriverParam other = (DriverParam) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
