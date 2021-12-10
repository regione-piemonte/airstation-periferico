/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: holds specific version of a PCI board
// Change log:
//   2008-04-16: initial version
// ----------------------------------------------------------------------------
// $Id: PCIBoardVersion.java,v 1.4 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

/**
 * Holds specific version of a PCI board
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class PCIBoardVersion extends ConfigItem {

	private static final long serialVersionUID = 4023763351605108682L;

	private String version = "";

	private String driverName = "";

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = trim(driverName);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = trim(version);
	}

	@Override
	public void checkConfig() throws ConfigException {
	}

}
