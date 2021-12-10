/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a generic communication device
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: CommDevice.java,v 1.5 2012/10/15 12:13:27 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import java.io.IOException;

/**
 * Represents a generic communication device
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public abstract class CommDevice {

	abstract void disconnect(Connection connection) throws ConnectionException;

	boolean isConnected(Connection connection) throws ConnectionException {
		for (int i = 0; i < 3; i++) {
			if (ping(connection.getRemoteHost(), connection.getMaxLatency()))
				return true;
		}
		return false;
	}

	boolean ping(String host, int timeout) throws ConnectionException {
		Process p = null;
		try {
			String cmd[] = { "ping", "-c", "1", "-w",
					Integer.toString(timeout), "-n", host };
			ProcessBuilder pb = new ProcessBuilder(cmd);
			p = pb.start();
			int result = p.waitFor();
			return result == 0;
		} catch (Exception ex) {
			throw new ConnectionException("Cannot execute ping command", ex);
		} finally {
			if (p != null) {
				try {
					p.getOutputStream().close();
				} catch (IOException e) {
				}
				try {
					p.getErrorStream().close();
				} catch (IOException e) {
				}
				try {
					p.getInputStream().close();
				} catch (Exception e) {
				}
			}
		}
	}

}
