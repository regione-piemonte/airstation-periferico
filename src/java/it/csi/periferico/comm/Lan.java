/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represent LAN communication
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: Lan.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import org.apache.log4j.Logger;

/**
 * Represent LAN communication
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Lan extends CommDevice {

	private static Logger logger = Logger.getLogger("periferico."
			+ Lan.class.getSimpleName());

	public Connection connect(LanConnectionParams cp)
			throws ConnectionException {
		logger.info("Requested connection to " + cp.getCopIP());
		try {
			int availableTime = cp.getConnectTimeout();
			while (!ping(cp.getCopIP(), cp.getMaxLatency())
					&& availableTime > 0)
				availableTime -= cp.getMaxLatency();
			if (availableTime <= 0)
				throw new ConnectionException(
						"Remote system is not responding to ping requests");
			logger.info("Connection succeeded");
			return new Connection(this, cp.getCopIP(), cp.getMaxLatency());
		} catch (Exception ex) {
			ConnectionException ce = new ConnectionException("Connection to "
					+ cp.getCopIP() + " failed", ex);
			logger.error(ex.getMessage(), ex);
			throw ce;
		}
	}

	@Override
	void disconnect(Connection connection) {
		logger.info("Disconnect requested");
		return;
	}

}
