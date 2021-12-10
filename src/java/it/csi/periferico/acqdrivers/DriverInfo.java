/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: data port drivers management
// Change log:
//   2015-09-18: initial version
// ----------------------------------------------------------------------------
// $Id: DriverInfo.java,v 1.3 2015/10/16 12:58:49 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers;

import it.csi.periferico.acqdrivers.itf.ConfigInfo;
import it.csi.periferico.acqdrivers.itf.DriverVersion;

import java.util.List;

/**
 * Data port drivers management
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DriverInfo {

	private String name;
	private DriverVersion version;
	private List<ConfigInfo> listConfigInfo;

	public DriverInfo(String name, DriverVersion version,
			List<ConfigInfo> listConfigInfo) {
		this.name = name;
		this.version = version;
		this.listConfigInfo = listConfigInfo;
	}

	public String getName() {
		return name;
	}

	public DriverVersion getVersion() {
		return version;
	}

	public List<ConfigInfo> getListConfigInfo() {
		return listConfigInfo;
	}

	public boolean isOK() {
		if (listConfigInfo == null)
			return false;
		for (ConfigInfo ci : listConfigInfo)
			if (!ci.isOK())
				return false;
		return true;
	}

}