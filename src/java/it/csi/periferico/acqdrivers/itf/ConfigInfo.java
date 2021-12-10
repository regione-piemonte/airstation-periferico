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
// $Id: ConfigInfo.java,v 1.2 2015/10/16 12:58:49 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.itf;

/**
 * Data port drivers management
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ConfigInfo {

	private String name;
	private boolean newCfg;
	private String version;
	private String checksum;
	private Boolean checksumValid;

	public ConfigInfo(String name, boolean newCfg, String version,
			String checksum, Boolean checksumValid) {
		this.name = name;
		this.newCfg = newCfg;
		this.version = version;
		this.checksum = checksum;
		this.checksumValid = checksumValid;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getChecksum() {
		return checksum;
	}

	public boolean isNewConfig() {
		return newCfg;
	}

	public Boolean isChecksumValid() {
		return checksumValid;
	}

	public boolean isOK() {
		return !newCfg && checksumValid != null && checksumValid;
	}

}