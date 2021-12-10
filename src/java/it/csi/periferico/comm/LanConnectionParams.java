/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the parameters needed for a LAN connection
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: LanConnectionParams.java,v 1.7 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

/**
 * Represents the parameters needed for a LAN connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class LanConnectionParams extends ConnectionParams {

	private static final long serialVersionUID = 4412187643568225907L;

	static final String deviceType = "LAN";

	public LanConnectionParams() {
		this.setMaxConnectionAttempts(1);
		this.setConnectTimeout(15);
		this.setMaxLatency(2);
	}

	@Override
	public String getDeviceType() {
		return deviceType;
	}

}
