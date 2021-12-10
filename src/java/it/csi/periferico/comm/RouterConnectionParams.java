/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the parameters needed for a router connection
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: RouterConnectionParams.java,v 1.7 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

/**
 * Represents the parameters needed for a router connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class RouterConnectionParams extends LanConnectionParams {

	private static final long serialVersionUID = 5384064883661986845L;

	static final String deviceType = "ROUTER";

	public RouterConnectionParams() {
		this.setMaxConnectionAttempts(1);
		this.setConnectTimeout(40);
		this.setMaxLatency(2);
	}

	@Override
	public String getDeviceType() {
		return deviceType;
	}

}
