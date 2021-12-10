/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: connection management
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: ConnectionManager.java,v 1.8 2012/01/18 11:00:49 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import org.apache.log4j.Logger;

/**
 * Connection management
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class ConnectionManager implements Runnable {

	private static final int CONN_MANAGER_THREAD_PRIORITY = Thread.NORM_PRIORITY - 3;

	private static final int CONN_MANAGER_THREAD_SLEEP_TIME = 10;

	private static final int AUTO_DISCONNECT_TIME = 10 * 60;

	private static Logger logger = Logger.getLogger("periferico."
			+ ConnectionManager.class.getSimpleName());

	private CommDevice commDevice = null;

	private Connection connection = null;

	private volatile Thread autoDisconnectThread = null;

	private int timeToAutoDisconnect = 0;

	public synchronized void connect(ConnectionParams cp)
			throws ConnectionException {
		if (cp == null)
			throw new ConnectionException("cannot_connect_cfg_missing");
		if (!cp.isOutgoingCallEnabled())
			throw new ConnectionException(
					"cannot_connect_outgoing_call_not_enabled");
		if (connection != null) {
			if (connection.isUp()) {
				timeToAutoDisconnect = AUTO_DISCONNECT_TIME;
				return;
			}
			try {
				connection.close();
			} catch (ConnectionException ce) {
			} finally {
				connection = null;
			}
		}
		int attempts = 0;
		while (connection == null) {
			try {
				if (cp instanceof ModemConnectionParams)
					connection = getModem().connect((ModemConnectionParams) cp);
				else if (cp instanceof RouterConnectionParams)
					connection = getRouter().connect(
							(RouterConnectionParams) cp);
				else if (cp instanceof LanConnectionParams)
					connection = getLan().connect((LanConnectionParams) cp);
				else
					throw new IllegalStateException(
							"Connection type not supported");
			} catch (Exception ex) {
				attempts++;
				if (attempts >= cp.getMaxConnectionAttempts())
					throw new ConnectionException("connection_failed", ex);
			}
		}
		timeToAutoDisconnect = AUTO_DISCONNECT_TIME;
		autoDisconnectThread = new Thread(this);
		autoDisconnectThread.setPriority(CONN_MANAGER_THREAD_PRIORITY);
		autoDisconnectThread.setDaemon(true);
		autoDisconnectThread.start();
	}

	public synchronized void disconnect() throws ConnectionException {
		autoDisconnectThread = null;
		if (connection != null) {
			try {
				connection.close();
			} finally {
				connection = null;
			}
		}
	}

	public synchronized boolean isConnected() throws ConnectionException {
		if (connection == null)
			return false;
		return connection.isUp();
	}

	private Lan getLan() {
		if (!(commDevice instanceof Lan))
			commDevice = new Lan();
		return (Lan) commDevice;
	}

	private Modem getModem() {
		if (!(commDevice instanceof Modem))
			commDevice = new Modem();
		return (Modem) commDevice;
	}

	private Router getRouter() {
		if (!(commDevice instanceof Router))
			commDevice = new Router();
		return (Router) commDevice;
	}

	public void run() {
		try {
			logger.info("Auto disconnect thread started");
			while (Thread.currentThread() == autoDisconnectThread) {
				try {
					Thread.sleep(CONN_MANAGER_THREAD_SLEEP_TIME * 1000);
					timeToAutoDisconnect -= CONN_MANAGER_THREAD_SLEEP_TIME;
					synchronized (this) {
						if (Thread.currentThread() != autoDisconnectThread)
							break;
						if (timeToAutoDisconnect > 0)
							continue;
						try {
							logger.info("Disconnect forced by disconnect thread");
							disconnect();
						} catch (ConnectionException ce) {
						}
					}
				} catch (InterruptedException ie) {
					logger.warn("Interruption requested for auto disconnect thread");
					try {
						disconnect();
					} catch (ConnectionException ce) {
					}
					break;
				}
			}
			logger.info("Auto disconnect thread stopped");
		} catch (Exception ex) {
			logger.error("Auto disconnect thread terminated with fatal error",
					ex);
		}
	}
}
