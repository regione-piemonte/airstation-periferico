/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a PCI acquisition board
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: PCIBoard.java,v 1.10 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;

import java.util.List;

/**
 * Represents a PCI acquisition board
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class PCIBoard extends Board {

	private static final long serialVersionUID = 1856516394820922952L;

	private PCIDevice pciDevice;

	private String version = "";

	public PCIBoard() {
		this(null);
	}

	public PCIBoard(PCIDevice pciDevice) {
		this(pciDevice, "");
	}

	public PCIBoard(PCIDevice pciDevice, String version) {
		this.pciDevice = pciDevice;
		setVersion(version);
	}

	public PCIDevice getPciDevice() {
		return pciDevice;
	}

	public void setPciDevice(PCIDevice pciDevice) {
		this.pciDevice = pciDevice;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = trim(version);
	}

	public void setConfig(String brand, String model, String version,
			List<DriverParam> driverParams) throws ConfigException {
		super.setConfig(brand, model, driverParams);
		setVersion(version);
	}

	public boolean isSameConfig(String brand, String model, String version,
			List<DriverParam> driverParams) {
		return super.isSameConfig(brand, model, driverParams)
				&& this.version.equals(trim(version));
	}

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
	}

}
