/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: this class holds the information needed to configure a
//                  PCI acquisition board
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: PCIBoardDescriptor.java,v 1.9 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the information needed to configure a PCI acquisition board
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class PCIBoardDescriptor extends BoardDescriptor {

	private static final long serialVersionUID = -348243300194016996L;

	private int vendorId = 0;

	private int deviceId = 0;

	private List<PCIBoardVersion> pciBoardVersions = new ArrayList<PCIBoardVersion>();

	private String versionWarning = "";

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceIdAsHexString() {
		return Integer.toHexString(deviceId);
	}

	public void setDeviceIdAsHexString(String hexString) {
		this.deviceId = Integer.parseInt(hexString, 16);
	}

	public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorIdAsHexString() {
		return Integer.toHexString(vendorId);
	}

	public void setVendorIdAsHexString(String hexString) {
		this.vendorId = Integer.parseInt(hexString, 16);
	}

	public String getVersionWarning() {
		return versionWarning;
	}

	public void setVersionWarning(String versionWarning) {
		this.versionWarning = trim(versionWarning);
	}

	public List<PCIBoardVersion> getPciBoardVersions() {
		return pciBoardVersions;
	}

	public void setPciBoardVersions(List<PCIBoardVersion> pciBoardVersions) {
		this.pciBoardVersions = pciBoardVersions;
	}

	public String getDriverName(String version) {
		if (version == null || version.isEmpty())
			return super.getDriverName();
		for (PCIBoardVersion pbv : pciBoardVersions) {
			if (pbv.getVersion().equals(version))
				return pbv.getDriverName();
		}
		return "";
	}

	@Override
	public Board newBoard() throws BoardsException {
		// TODO: cambiare il messaggio
		throw new BoardsException("isa_board_not_found_brand_model",
				getBrand(), getModel());
	}

}
