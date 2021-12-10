/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: implements a dispatch thread for data and events
// Change log:
//   2008-05-22: initial version
// ----------------------------------------------------------------------------
// $Id: DispatchThread.java,v 1.10 2012/01/18 11:00:49 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Implements a dispatch thread for data and events
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DispatchThread extends Thread {

	private static final int QUEUE_POLL_TIMEOUT_MS = 250;

	public static final String THREAD_NAME = "boards_dispatch";

	private static final int DISPATCH_THREAD_PRIORITY = Thread.NORM_PRIORITY + 2;

	private static Logger logger = Logger.getLogger("periferico."
			+ DispatchThread.class.getSimpleName());

	private BlockingQueue<Dispatchable> dispatchQueue;

	private volatile boolean dispatching;

	public DispatchThread(BlockingQueue<Dispatchable> dispatchQueue) {
		super(THREAD_NAME);
		this.dispatchQueue = dispatchQueue;
		dispatching = true;
		setDaemon(true);
		setPriority(DISPATCH_THREAD_PRIORITY);
	}

	public void stopDispatching() {
		dispatching = false;
		logger.info("Dispatch thread shutdown in progress...");
		try {
			int maxWaitTime_s = 5;
			logger.info("Waiting up to " + maxWaitTime_s
					+ "s for dispatch thread to terminate...");
			join(maxWaitTime_s * 1000);
			logger.info("Finished waiting for dispatch thread to terminate");
		} catch (InterruptedException ie) {
			logger.error("Wait for dispatch thread to terminate interrupted");
		}
	}

	@Override
	public void run() {
		try {
			logger.info("Dispatch thread started");
			while (dispatching || !dispatchQueue.isEmpty()) {
				Dispatchable d = dispatchQueue.poll(QUEUE_POLL_TIMEOUT_MS,
						TimeUnit.MILLISECONDS);
				if (d == null)
					continue;
				if (d instanceof AIValue) {
					AIValue aiValue = (AIValue) d;
					AIUser aiUser = (AIUser) aiValue.getDestination();
					aiUser.deliver(aiValue);
				} else if (d instanceof DIEvent) {
					DIEvent diEvent = (DIEvent) d;
					DIUser diUser = (DIUser) diEvent.getDestination();
					diUser.deliver(diEvent);
				}
			}
			logger.info("Dispatch thread stopped");
		} catch (InterruptedException ie) {
			logger.warn("Dispatch thread interrupted");
		}
	}

}
