/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the parameters needed for a generic connection
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: ConnectionParams.java,v 1.13 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the parameters needed for a generic connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public abstract class ConnectionParams extends ConfigItem {

	private static final long serialVersionUID = -4199480968557880689L;

	private String copIP = "cop";

	private boolean outgoingCallEnabled;

	private int maxConnectionAttempts;

	private int connectTimeout;

	private int maxLatency;

	public void setMaxConnectionAttempts(int value) {
		maxConnectionAttempts = (value <= 0) ? 1 : value;
	}

	public int getMaxConnectionAttempts() {
		return (maxConnectionAttempts);
	}

	public String getCopIP() {
		return copIP;
	}

	public void setCopIP(String copIP) {
		this.copIP = trim(copIP);
	}

	public boolean isOutgoingCallEnabled() {
		return outgoingCallEnabled;
	}

	public void setOutgoingCallEnabled(boolean outgoingCallEnabled) {
		this.outgoingCallEnabled = outgoingCallEnabled;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		if (connectTimeout <= 0)
			connectTimeout = 1;
		this.connectTimeout = connectTimeout;
	}

	public int getMaxLatency() {
		return maxLatency;
	}

	public void setMaxLatency(int maxLatency) {
		if (maxLatency <= 0)
			maxLatency = 1;
		this.maxLatency = maxLatency;
	}

	public void setConfig(String copIP, boolean outgoingCallEnabled,
			int maxConnectionAttempts, int connectTimeout) {
		setCopIP(copIP);
		setOutgoingCallEnabled(outgoingCallEnabled);
		setMaxConnectionAttempts(maxConnectionAttempts);
		setConnectTimeout(connectTimeout);
	}

	public boolean isSameConfig(String copIP, boolean outgoingCallEnabled,
			int maxConnectionAttempts, int connectTimeout) {
		return this.copIP.equals(trim(copIP))
				&& this.outgoingCallEnabled == outgoingCallEnabled
				&& this.maxConnectionAttempts == maxConnectionAttempts
				&& this.connectTimeout == connectTimeout;
	}

	@Override
	public void checkConfig() throws ConfigException {
	}

	public abstract String getDeviceType();

	public static List<String> getSupportedDeviceTypes() {
		List<String> supportedDeviceTypes = new ArrayList<String>();
		supportedDeviceTypes.add(RouterConnectionParams.deviceType);
		supportedDeviceTypes.add(ModemConnectionParams.deviceType);
		supportedDeviceTypes.add(LanConnectionParams.deviceType);
		return supportedDeviceTypes;
	}

	public static ConnectionParams getInstance(String deviceType) {
		if (deviceType == null)
			return null;
		deviceType = deviceType.trim();
		if (ModemConnectionParams.deviceType.equalsIgnoreCase(deviceType))
			return new ModemConnectionParams();
		else if (RouterConnectionParams.deviceType.equalsIgnoreCase(deviceType))
			return new RouterConnectionParams();
		else if (LanConnectionParams.deviceType.equalsIgnoreCase(deviceType))
			return new LanConnectionParams();
		return null;
	}

}
