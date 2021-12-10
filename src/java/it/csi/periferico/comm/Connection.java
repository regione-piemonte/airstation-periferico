/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: class that manages a connection
// Change log:
//   2008-10-24: initial version
// ----------------------------------------------------------------------------
// $Id: Connection.java,v 1.2 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

/**
 * Class that manages a connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class Connection {

	private CommDevice commDevice;

	private String remoteHost;

	private int maxLatency;

	Connection(CommDevice commDevice, String remoteHost, int maxLatency) {
		this.commDevice = commDevice;
		this.remoteHost = remoteHost;
		this.maxLatency = maxLatency;
	}

	public void close() throws ConnectionException {
		commDevice.disconnect(this);
	}

	public boolean isUp() throws ConnectionException {
		return commDevice.isConnected(this);
	}

	public CommDevice getCommDevice() {
		return commDevice;
	}

	public int getMaxLatency() {
		return maxLatency;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

}
