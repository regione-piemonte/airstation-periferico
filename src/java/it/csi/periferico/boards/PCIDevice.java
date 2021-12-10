/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: identifies a PCI device
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: PCIDevice.java,v 1.6 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;

/**
 * Identifies a PCI device
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class PCIDevice extends PCILocation {

	private static final long serialVersionUID = -7120035477605027747L;

	private Integer vendorId;

	private Integer deviceId;

	public PCIDevice() {
		super();
		vendorId = null;
		deviceId = null;
	}

	public PCIDevice(PCILocation pciLocation, Integer vendorId, Integer deviceId) {
		super(pciLocation.getDomain(), pciLocation.getBus(), pciLocation
				.getSlot(), pciLocation.getFunction());
		this.vendorId = vendorId;
		this.deviceId = deviceId;
	}

	public PCIDevice(String arg) {
		if (arg == null)
			throw new IllegalArgumentException("Null argument not allowed");
		String str = arg;
		str = str.trim();
		int cbIndex = str.lastIndexOf("]");
		if (cbIndex < 0)
			throw new IllegalArgumentException("Unparsable string: " + arg);
		String strLocation = str.substring(0, cbIndex + 1);
		setPCILocation(new PCILocation(strLocation));
		String strVDid = str.substring(cbIndex + 1);
		strVDid = strVDid.trim();
		int colonIndex = strVDid.lastIndexOf(":");
		if (colonIndex < 0)
			throw new IllegalArgumentException("Unparsable string: " + arg);
		String strVendor = strVDid.substring(0, colonIndex);
		String strDevice = strVDid.substring(colonIndex + 1);
		try {
			vendorId = Integer.parseInt(strVendor, 16);
			deviceId = Integer.parseInt(strDevice, 16);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Unparsable string: " + arg);
		}
	}

	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceIdAsHexString() {
		return Integer.toHexString(deviceId);
	}

	public void setDeviceIdAsHexString(String hexString) {
		this.deviceId = Integer.parseInt(hexString, 16);
	}

	public Integer getVendorId() {
		return vendorId;
	}

	public void setVendorId(Integer vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorIdAsHexString() {
		return Integer.toHexString(vendorId);
	}

	public void setVendorIdAsHexString(String hexString) {
		this.vendorId = Integer.parseInt(hexString, 16);
	}

	public void setPCILocation(PCILocation location) {
		setDomain(location.getDomain());
		setBus(location.getBus());
		setSlot(location.getSlot());
		setFunction(location.getFunction());
	}

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
	}

	@Override
	public String toString() {
		return super.toString() + " "
				+ (vendorId == null ? "" : Integer.toHexString(vendorId)) + ":"
				+ (deviceId == null ? "" : Integer.toHexString(deviceId));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
		result = prime * result + ((vendorId == null) ? 0 : vendorId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PCIDevice other = (PCIDevice) obj;
		if (deviceId == null) {
			if (other.deviceId != null)
				return false;
		} else if (!deviceId.equals(other.deviceId))
			return false;
		if (vendorId == null) {
			if (other.vendorId != null)
				return false;
		} else if (!vendorId.equals(other.vendorId))
			return false;
		return true;
	}

}
