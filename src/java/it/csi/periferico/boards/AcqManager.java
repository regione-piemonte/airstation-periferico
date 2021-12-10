/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: manages data acquisition from IO boards
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: AcqManager.java,v 1.16 2012/01/18 11:00:49 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

/**
 * Manages data acquisition from IO boards
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class AcqManager extends AcqLogic implements Runnable {

	private static final int DI_POLL_PERIOD_MS = 10;
	private static final int AI_ACQ_PERIOD_MS = 1000;
	private static final int AI_ACQ_RESYNC_THRESHOLD_ms = 3000;
	private static final int DO_SET_PERIOD_MS = 1000;
	private static final int DO_SET_RESYNC_THRESHOLD_ms = 3000;
	private static final int MAX_QUEUE_SIZE = 10000;
	public static final String THREAD_NAME = "boards_acq";
	private static final int ACQ_THREAD_PRIORITY = Thread.NORM_PRIORITY + 4;
	private static Logger logger = Logger.getLogger("periferico."
			+ AcqManager.class.getSimpleName());

	private Map<Board, ExternalAcqItf> mapAcquisitionItfs;
	private BlockingQueue<Dispatchable> dispatchQueue;
	private volatile Thread acquisitionThread = null;
	private DispatchThread dispatchThread = null;

	public AcqManager() {
		super(new AcqStats(DI_POLL_PERIOD_MS, AI_ACQ_PERIOD_MS));
		dispatchQueue = new ArrayBlockingQueue<Dispatchable>(MAX_QUEUE_SIZE);
	}

	public void startAcquisition(Map<Board, ExternalAcqItf> mapAcquisitionItfs) {
		if (mapAcquisitionItfs == null)
			throw new IllegalArgumentException("Null arguments not allowed");
		if (mapAcquisitionItfs.isEmpty()) {
			logger.info("No board library needs acquisition manager,"
					+ " not starting now");
			return;
		}
		this.mapAcquisitionItfs = mapAcquisitionItfs;
		dispatchQueue.clear();
		dispatchThread = new DispatchThread(dispatchQueue);
		dispatchThread.start();
		logger.info("Starting acquisition from IO boards...");
		acquisitionThread = new Thread(this, THREAD_NAME);
		acquisitionThread.setDaemon(true);
		acquisitionThread.setPriority(ACQ_THREAD_PRIORITY);
		acquisitionThread.start();
	}

	public void stopAcquisition() {
		Thread tmpThread = acquisitionThread;
		if (tmpThread != null) {
			acquisitionThread = null;
			logger.info("Acquisition manager shutdown in progress...");
			try {
				int maxWaitTime_s = 2;
				logger.info("Waiting up to " + maxWaitTime_s
						+ "s for acquisition manager to terminate...");
				// NOTE: join is needed before boardManager.destroy() is called
				tmpThread.join(maxWaitTime_s * 1000);
				logger.info("Finished waiting for acquisition manager to"
						+ " terminate");
			} catch (InterruptedException ie) {
				logger.error("Wait for acquisition manager to terminate"
						+ " interrupted");
			}
		}
		if (dispatchThread != null) {
			dispatchThread.stopDispatching();
			dispatchThread = null;
		}
	}

	private long computeNextAIAcqTime() {
		return (System.currentTimeMillis() / AI_ACQ_PERIOD_MS)
				* AI_ACQ_PERIOD_MS + AI_ACQ_PERIOD_MS;
	}

	private long computeNextDOSetTime() {
		return (System.currentTimeMillis() / DO_SET_PERIOD_MS)
				* DO_SET_PERIOD_MS + DO_SET_PERIOD_MS + DI_POLL_PERIOD_MS * 5;
	}

	@Override
	public void run() {
		logger.info("Acquisition from IO boards started");
		long nextTheoricAIAcqTime_ms = computeNextAIAcqTime();
		long nextTheoricDOSetTime_ms = computeNextDOSetTime();
		while (Thread.currentThread() == acquisitionThread) {
			long startTime = System.currentTimeMillis();
			for (Board board : mapAcquisitionItfs.keySet())
				acqDI(mapAcquisitionItfs.get(board), board);
			long timeDiff = computeNextAIAcqTime() - nextTheoricAIAcqTime_ms;
			if (Math.abs(timeDiff) >= AI_ACQ_RESYNC_THRESHOLD_ms) {
				logger.warn("Time discontinuity detected (" + (timeDiff / 1000)
						+ "s) for AI acquisition. Resync done.");
				getAcqStats().reset();
				nextTheoricAIAcqTime_ms = computeNextAIAcqTime();
			}
			if (System.currentTimeMillis() >= nextTheoricAIAcqTime_ms) {
				for (Board board : mapAcquisitionItfs.keySet())
					acqAI(mapAcquisitionItfs.get(board), board,
							nextTheoricAIAcqTime_ms, startTime);
				nextTheoricAIAcqTime_ms += AI_ACQ_PERIOD_MS;
			}
			timeDiff = computeNextDOSetTime() - nextTheoricDOSetTime_ms;
			if (Math.abs(timeDiff) >= DO_SET_RESYNC_THRESHOLD_ms) {
				logger.warn("Time discontinuity detected (" + (timeDiff / 1000)
						+ "s) for DO set. Resync done.");
				getAcqStats().reset();
				nextTheoricDOSetTime_ms = computeNextDOSetTime();
			}
			if (System.currentTimeMillis() >= nextTheoricDOSetTime_ms) {
				for (Board board : mapAcquisitionItfs.keySet())
					setDO(mapAcquisitionItfs.get(board), board);
				nextTheoricDOSetTime_ms += DO_SET_PERIOD_MS;
			}
			long acqTime = System.currentTimeMillis() - startTime;
			if (acqTime < 0) // This may happen in case of clock adjustment !
				continue;
			long sleepTime = DI_POLL_PERIOD_MS - acqTime;
			getAcqStats().putDITime(acqTime);
			try {
				if (sleepTime > 0)
					Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.warn("Acquisition from IO boards interrupted");
				return;
			}
		}
		logger.info("Acquisition from IO boards stopped");
	}

	@Override
	protected boolean manageDispatchable(Dispatchable dispatchable) {
		return dispatchQueue.offer(dispatchable);
	}

}
