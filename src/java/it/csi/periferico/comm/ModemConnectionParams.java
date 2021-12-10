/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the parameters needed for a modem connection
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: ModemConnectionParams.java,v 1.7 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

/**
 * Represents the parameters needed for a modem connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class ModemConnectionParams extends ConnectionParams {

	private static final long serialVersionUID = -6287492843495559743L;

	static final String deviceType = "MODEM";

	private String copDialupConfig = "cop";

	public ModemConnectionParams() {
		this.setMaxConnectionAttempts(3);
		this.setConnectTimeout(60);
		this.setMaxLatency(2);
	}

	public String getCopDialupConfig() {
		return copDialupConfig;
	}

	public void setCopDialupConfig(String copDialupConfig) {
		this.copDialupConfig = trim(copDialupConfig);
	}

	public void setConfig(String copIP, boolean outgoingCallEnabled,
			int maxConnectionRetry, int connectTimeout,
			String setCopDialupConfig) {
		super.setConfig(copIP, outgoingCallEnabled, maxConnectionRetry,
				connectTimeout);
		setCopDialupConfig(copDialupConfig);
	}

	public boolean isSameConfig(String copIP, boolean outgoingCallEnabled,
			int maxConnectionRetry, int connectTimeout, String copDialupConfig) {
		return super.isSameConfig(copIP, outgoingCallEnabled,
				maxConnectionRetry, connectTimeout)
				&& this.copDialupConfig.equals(trim(copDialupConfig));
	}

	@Override
	public String getDeviceType() {
		return deviceType;
	}

}
