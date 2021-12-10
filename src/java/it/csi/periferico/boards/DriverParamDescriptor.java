/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: describes a driver parameter
// Change log:
//   2008-04-16: initial version
// ----------------------------------------------------------------------------
// $Id: DriverParamDescriptor.java,v 1.3 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a driver parameter
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DriverParamDescriptor extends ConfigItem {

	private static final long serialVersionUID = -4851141042492794688L;

	public enum Type {
		DECIMAL, HEXADECIMAL, STRING, IOBASE, IRQ, DMA0, DMA1
	}

	private String name = "";

	private int index = 0;

	private Type type = Type.DECIMAL;

	private boolean optional = true;

	private String help = "";

	private List<String> allowedValues = new ArrayList<String>();

	public List<String> getAllowedValues() {
		return allowedValues;
	}

	public void setAllowedValues(List<String> allowedValues) {
		this.allowedValues = allowedValues;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = trim(help);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = trim(name);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getTypeAsString() {
		return type.toString().toLowerCase();
	}

	public void setTypeAsString(String str) {
		str = trim(str);
		this.type = Type.valueOf(str.toUpperCase());
	}

	@Override
	public void checkConfig() throws ConfigException {
		// TODO Auto-generated method stub

	}

}
