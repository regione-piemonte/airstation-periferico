/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represnts a modem
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: Modem.java,v 1.6 2012/10/15 12:13:27 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * Represnts a modem
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Modem extends CommDevice {

	// TODO: it may be useful to add a function to check if pppd is still
	// running after poff has been called

	private static Logger logger = Logger.getLogger("periferico."
			+ Modem.class.getSimpleName());

	private String currentDialupConfig = null;

	public synchronized Connection connect(ModemConnectionParams cp)
			throws ConnectionException {
		logger.info("Requested connection to " + cp.getCopIP()
				+ " using dialup config " + cp.getCopDialupConfig());
		Process p = null;
		BufferedReader br = null;
		try {
			if (currentDialupConfig != null)
				throw new ConnectionException("The device is busy: "
						+ "current dialup config is " + currentDialupConfig);
			String cmd[] = { "pon", cp.getCopDialupConfig() };
			ProcessBuilder pb = new ProcessBuilder(cmd);
			p = pb.start();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			StringBuffer replyMessage = new StringBuffer();
			while ((line = br.readLine()) != null)
				replyMessage.append(line.trim()).append(' ');
			int result = p.waitFor();
			if (result != 0)
				throw new ConnectionException("Connection to " + cp.getCopIP()
						+ " using dialup config " + cp.getCopDialupConfig()
						+ " failed: " + replyMessage);
			logger.info("Connection succeeded: " + replyMessage);
			currentDialupConfig = cp.getCopDialupConfig();
			return new Connection(this, cp.getCopIP(), cp.getMaxLatency());
		} catch (Exception ex) {
			ConnectionException ce;
			if (ex instanceof ConnectionException)
				ce = (ConnectionException) ex;
			else
				ce = new ConnectionException("Connection to " + cp.getCopIP()
						+ " using dialup config " + cp.getCopDialupConfig()
						+ " failed", ex);
			logger.error(ex.getMessage(), ex);
			throw ce;
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
					if (br != null)
						br.close();
					else
						p.getInputStream().close();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	synchronized void disconnect(Connection connection)
			throws ConnectionException {
		logger.info("Disconnect requested");
		Process p = null;
		BufferedReader br = null;
		try {
			if (currentDialupConfig == null)
				throw new ConnectionException("Connection already closed");
			String cmd[] = { "poff", currentDialupConfig };
			ProcessBuilder pb = new ProcessBuilder(cmd);
			p = pb.start();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			StringBuffer replyMessage = new StringBuffer();
			while ((line = br.readLine()) != null)
				replyMessage.append(line.trim()).append(' ');
			int result = p.waitFor();
			if (result != 0)
				throw new ConnectionException(
						"Error closing connection of dialup config "
								+ currentDialupConfig + ": " + replyMessage);
		} catch (Exception ex) {
			ConnectionException ce;
			if (ex instanceof ConnectionException)
				ce = (ConnectionException) ex;
			else
				ce = new ConnectionException(
						"Error closing connection of dialup config "
								+ currentDialupConfig, ex);
			logger.error(ex.getMessage(), ex);
			throw ce;
		} finally {
			currentDialupConfig = null;
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
					if (br != null)
						br.close();
					else
						p.getInputStream().close();
				} catch (Exception e) {
				}
			}
		}
	}

}
