/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: holds the information about the location of a PCI resource
//                  in the PCI bus
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: PCILocation.java,v 1.11 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.StringTokenizer;

/**
 * Holds the information about the location of a PCI resource in the PCI bus
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class PCILocation extends ConfigItem {

	private static final long serialVersionUID = -5475516396217994959L;

	private int domain;

	private int bus;

	private int slot;

	private int function;

	public PCILocation() {
		this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
				Integer.MAX_VALUE);
	}

	public PCILocation(int domain, int bus, int slot, int function) {
		this.domain = domain;
		this.bus = bus;
		this.slot = slot;
		this.function = function;
	}

	public PCILocation(String arg) {
		if (arg == null)
			throw new IllegalArgumentException("Null argument not allowed");
		String str = arg;
		str = str.trim();
		if (!str.startsWith("[") || !str.endsWith("]"))
			throw new IllegalArgumentException("Unparsable string: " + arg);
		str = str.substring(1, str.length() - 1);
		StringTokenizer strTok = new StringTokenizer(str, ":");
		int numTokens = strTok.countTokens();
		if (numTokens < 2 || numTokens > 3)
			throw new IllegalArgumentException("Unparsable string: " + arg);
		String strDomain = null;
		if (numTokens == 3)
			strDomain = strTok.nextToken().trim();
		String strBus = strTok.nextToken().trim();
		String strSlotAndFunction = strTok.nextToken().trim();
		strTok = new StringTokenizer(strSlotAndFunction, ".");
		if (strTok.countTokens() != 2)
			throw new IllegalArgumentException("Unparsable string: " + arg);
		String strSlot = strTok.nextToken().trim();
		String strFunction = strTok.nextToken().trim();
		try {
			if (strDomain != null)
				domain = Integer.parseInt(strDomain, 16);
			else
				domain = 0;
			bus = Integer.parseInt(strBus, 16);
			slot = Integer.parseInt(strSlot, 16);
			function = Integer.parseInt(strFunction, 16);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Unparsable string: " + arg, e);
		}
	}

	public int getBus() {
		return bus;
	}

	public void setBus(int bus) {
		this.bus = bus;
	}

	public String getBusAsHexString() {
		return Integer.toHexString(bus);
	}

	public void setBusAsHexString(String hexString) {
		this.bus = Integer.parseInt(hexString, 16);
	}

	public int getDomain() {
		return domain;
	}

	public void setDomain(int domain) {
		this.domain = domain;
	}

	public String getDomainAsHexString() {
		return Integer.toHexString(domain);
	}

	public void setDomainAsHexString(String hexString) {
		this.domain = Integer.parseInt(hexString, 16);
	}

	public int getFunction() {
		return function;
	}

	public void setFunction(int function) {
		this.function = function;
	}

	public String getFunctionAsHexString() {
		return Integer.toHexString(function);
	}

	public void setFunctionAsHexString(String hexString) {
		this.function = Integer.parseInt(hexString, 16);
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public String getSlotAsHexString() {
		return Integer.toHexString(slot);
	}

	public void setSlotAsHexString(String hexString) {
		this.slot = Integer.parseInt(hexString, 16);
	}

	@Override
	public void checkConfig() throws ConfigException {
	}

	@Override
	public String toString() {
		return "[" + Integer.toHexString(domain) + ":"
				+ Integer.toHexString(bus) + ":" + Integer.toHexString(slot)
				+ "." + Integer.toHexString(function) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bus;
		result = prime * result + domain;
		result = prime * result + function;
		result = prime * result + slot;
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
		PCILocation other = (PCILocation) obj;
		if (bus != other.bus)
			return false;
		if (domain != other.domain)
			return false;
		if (function != other.function)
			return false;
		return slot == other.slot;
	}

}
