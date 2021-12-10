/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: manages and saves acquired data
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: DataManager.java,v 1.51 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.boards.AcqStats;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.gps.GpsDatum;
import it.csi.periferico.gps.GpsdClient;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Manages and saves acquired data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DataManager implements Runnable {

	private static Logger logger = Logger.getLogger("periferico."
			+ DataManager.class.getSimpleName());

	public static final String THREAD_NAME = "data_manager";

	private static final int MANAGER_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

	private static final int MANAGER_THREAD_OFFSET = 10; // seconds

	private static final int MANAGER_THREAD_SLEEP_TIME = 1; // seconds

	static final int AGGREGATIONS_COMPUTE_TIME_OFFSET = 7; // days

	static final int MAX_MANAGER_THREAD_PERIOD = 60; // minutes

	private static final int OLD_DATA_DELETE_PERIOD = 1440; // minutes (>= 1440)

	private int managerThreadPeriod = 1; // minutes (1 to MAX_MANAGER_THREAD_P.)

	private int manualOperationsAutoResetPeriod = 60; // minutes

	private volatile Thread dataManagerThread = null;

	private Station station = null;

	private GpsdClient gpsdClient = null;

	private StorageManager storageManager;

	private StatVars acqStatVars;

	private List<StatVars> listBoardStatVars;

	private DecimalFormat statsFormat = new DecimalFormat();

	private Date managementStartTime = null;

	private int totalWriteErrorCount = 0;

	public DataManager(CommonCfg commonConfig, StorageManager storageManager,
			AcqStats acqStats) {
		this.storageManager = storageManager;
		acqStatVars = new StatVars("Acquisition manager", acqStats);
		listBoardStatVars = new ArrayList<StatVars>();
		statsFormat.setMaximumFractionDigits(3);
		statsFormat
				.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		statsFormat.setGroupingUsed(false);
		onCommonConfigUpdated(commonConfig);
	}

	public void onCommonConfigUpdated(CommonCfg commonConfig) {
		Integer writeToDiskPeriod = commonConfig.getDataWriteToDiskPeriod();
		if (writeToDiskPeriod != null && writeToDiskPeriod >= 1
				&& writeToDiskPeriod <= MAX_MANAGER_THREAD_PERIOD)
			managerThreadPeriod = writeToDiskPeriod;
		Integer manualOperationsAutoResetPeriod = commonConfig
				.getManualOperationsAutoResetPeriod();
		if (manualOperationsAutoResetPeriod != null
				&& manualOperationsAutoResetPeriod >= 1)
			this.manualOperationsAutoResetPeriod = manualOperationsAutoResetPeriod;
		logger.debug("Manual operations auto reset period set to "
				+ manualOperationsAutoResetPeriod);
	}

	public void startManagement(Station station, GpsdClient gpsdClient,
			Map<String, AcqStats> mapAcqStats) {
		this.station = station;
		this.gpsdClient = gpsdClient;
		setAcqStats(mapAcqStats);
		managementStartTime = new Date();
		dataManagerThread = new Thread(this, THREAD_NAME);
		dataManagerThread.setDaemon(false);
		dataManagerThread.setPriority(MANAGER_THREAD_PRIORITY);
		dataManagerThread.start();
	}

	private void setAcqStats(Map<String, AcqStats> mapAcqStats) {
		List<StatVars> listStats = new ArrayList<StatVars>();
		for (String key : mapAcqStats.keySet())
			listStats.add(new StatVars(key, mapAcqStats.get(key)));
		this.listBoardStatVars = listStats;
	}

	public void stopManagement() {
		Thread tmpThread = dataManagerThread;
		dataManagerThread = null;
		if (tmpThread != null) {
			logger.info("Data manager thread shutdown in progress...");
			try {
				int maxWaitTime_s = 60;
				logger.info("Waiting up to " + maxWaitTime_s
						+ "s for data manager to terminate...");
				tmpThread.join(maxWaitTime_s * 1000);
				logger.info("Finished waiting for data manager to terminate");
			} catch (InterruptedException ie) {
				logger.error("Wait for data manager to terminate"
						+ " interrupted");
			}
		}
	}

	private long computeNextManagementTime() {
		long managerThreadPeriodMillis = managerThreadPeriod * 60000;
		return (System.currentTimeMillis() / managerThreadPeriodMillis)
				* managerThreadPeriodMillis + managerThreadPeriodMillis
				+ MANAGER_THREAD_OFFSET * 1000;
	}

	private long computeNextOldDataDeleteTime() {
		long oldDataDeletePeriodMillis = OLD_DATA_DELETE_PERIOD * 60000;
		return (System.currentTimeMillis() / oldDataDeletePeriodMillis)
				* oldDataDeletePeriodMillis + oldDataDeletePeriodMillis;
	}

	public void run() {
		logger.info("Data manager thread started");
		readAndSetPersistentData();
		checkManualOperations();
		storageManager.checkDiskSpace();
		storageManager.checkRaid();
		storageManager.checkSmart();
		logMemory(true);
		long nextManagementTime = computeNextManagementTime();
		long nextOldDataDeleteTime = computeNextOldDataDeleteTime();
		while (Thread.currentThread() == dataManagerThread) {
			Date currentTime = new Date();
			if (currentTime.getTime() >= nextManagementTime) {
				logger.debug("Data management started: " + currentTime);
				nextManagementTime = computeNextManagementTime();
				saveStationData();
				computeAggregateValues(currentTime);
				saveGpsData();
				storageManager.checkDiskSpace();
				storageManager.checkRaid();
				storageManager.checkSmart();
				checkManualOperations();
				logAcqTimings(acqStatVars);
				for (StatVars asv : listBoardStatVars)
					logAcqTimings(asv);
				logMemory(false);
				logger.debug("Data management duration: "
						+ (System.currentTimeMillis() - currentTime.getTime())
						+ " ms");
				long oldDataDeleteTimeDiff = currentTime.getTime()
						- nextOldDataDeleteTime;
				if (Math.abs(oldDataDeleteTimeDiff) > OLD_DATA_DELETE_PERIOD * 60000) {
					// This code may run only in case of clock adjustment !
					nextOldDataDeleteTime = computeNextOldDataDeleteTime();
				} else if (oldDataDeleteTimeDiff >= 0) {
					nextOldDataDeleteTime = computeNextOldDataDeleteTime();
					deleteOldData();
					logMemory(true);
				}
			} else if (currentTime.getTime() < nextManagementTime
					- managerThreadPeriod * 60000) {
				// This code may run only in case of clock adjustment !
				nextManagementTime = computeNextManagementTime();
				continue;
			}
			try {
				if (Thread.currentThread() == dataManagerThread)
					Thread.sleep(MANAGER_THREAD_SLEEP_TIME * 1000);
			} catch (InterruptedException e) {
				logger.warn("Interruption requested for data manager thread");
				break;
			}
		}
		saveStationData();
		logger.info("Data manager thread stopped");
	}

	private void readAndSetPersistentData() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_MONTH, -AGGREGATIONS_COMPUTE_TIME_OFFSET);
		Date aggregationsComputeStartTime = calendar.getTime();
		List<Analyzer> listAnalyzer = station.getListAnalyzer();
		for (Analyzer an : listAnalyzer) {
			an.readAndSetLastManualOperations();
			Element[] elements = an.getElements();
			for (int i = 0; i < elements.length; i++)
				elements[i]
						.readAndSetLastAggregateValues(aggregationsComputeStartTime);
		}
	}

	public void saveStationData() {
		logger.debug("Saving station data ...");
		int errorCount = 0;
		List<ContainerAlarm> listCA = station.getContainer().getListAlarm();
		for (ContainerAlarm ca : listCA) {
			try {
				ca.saveData();
			} catch (StorageException ex) {
				errorCount++;
				// Note: these errors are already logged in lower level
			}
		}
		List<Analyzer> listAnalyzer = station.getListAnalyzer();
		for (Analyzer an : listAnalyzer) {
			UUID anId = an.getId();
			for (int i = 0; i < Analyzer.ARRAY_EVT.length; i++) {
				try {
					storageManager.saveEventData(anId, Analyzer.ARRAY_EVT[i],
							an.getCollector(Analyzer.ARRAY_EVT[i])
									.getAllAndClean());
				} catch (StorageException e) {
					errorCount++;
					// Note: these errors are already logged in lower level
				}
			}
			Element[] elements = an.getElements();
			for (int i = 0; i < elements.length; i++) {
				try {
					elements[i].saveData();
				} catch (StorageException e) {
					errorCount++;
					// Note: these errors are already logged in lower level
				}
			}
		}
		if (errorCount == 0)
			logger.debug("Station data saved successfully");
		else
			logger.error(errorCount
					+ " error(s) occurred while saving station data");
		totalWriteErrorCount += errorCount;
	}

	public int getTotalWriteErrorCount() {
		return totalWriteErrorCount;
	}

	private void checkManualOperations() {
		logger.debug("Checking for manual operations left active ...");
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.MINUTE, -manualOperationsAutoResetPeriod);
		Date autoResetDate = calendar.getTime();
		List<Analyzer> listAnalyzer = station.getListAnalyzer();
		for (Analyzer an : listAnalyzer) {
			BinaryStatus maintenance = an.getMaintenanceInProgress();
			BinaryStatus manualCalib = an.getManualCalibrationRunning();
			if (maintenance != null && maintenance.getStatus()
					&& maintenance.getTimestamp().before(autoResetDate)) {
				an.stopMaintenance();
				logger.info("Maintenance stopped for analyzer " + an.getName());
			}
			if (manualCalib != null && manualCalib.getStatus()
					&& manualCalib.getTimestamp().before(autoResetDate)) {
				an.stopManualCalibration();
				logger.info("Manual calibration stopped for analyzer "
						+ an.getName());
			}
		}

	}

	private void computeAggregateValues(Date aggregationComputeEndTime) {
		logger.debug("Computing aggregate values ...");
		List<Analyzer> listAnalyzer = station.getListAnalyzer();
		for (Analyzer an : listAnalyzer) {
			Element[] elements = an.getElements();
			for (int i = 0; i < elements.length; i++) {
				elements[i].computeAndSaveAggregateValues(managementStartTime,
						aggregationComputeEndTime);
			}
		}
	}

	private void saveGpsData() {
		if (gpsdClient == null)
			return;
		List<GpsDatum> gpsData = gpsdClient.getAllDataAndClean();
		logger.debug("Saving gps data ...");
		try {
			storageManager.saveGpsData(gpsData);
			logger.debug("Gps data saved successfully");
		} catch (StorageException ex) {
			logger.error("An error occurred while saving gps data");
		}
	}

	private void deleteOldData() {
		try {
			Date copDate = Periferico.getInstance().getCopDate();
			if (copDate == null) {
				logger.warn("Old data deletion skipped: no connection with "
						+ "COP since application startup");
				return;
			}
			if (System.currentTimeMillis() - copDate.getTime() > OLD_DATA_DELETE_PERIOD * 60000) {
				logger.warn("Old data deletion skipped: last connection "
						+ "with COP too old (" + copDate + ")");
				return;
			}
			logger.info("Deleting old data ...");
			long beginTime = System.currentTimeMillis();
			storageManager.deleteOldData();
			logger.info("Old data deleted in "
					+ (System.currentTimeMillis() - beginTime) + " ms");
		} catch (Exception ex) {
			logger.error("An error occurred deleting old data", ex);
		}
	}

	private void logAcqTimings(StatVars vars) {
		AcqStats stats = vars.getStats();
		boolean showDI;
		if (logger.isDebugEnabled())
			showDI = vars.digitalCounter != stats.getDelayedDICount();
		else
			showDI = stats.getDelayedDICount() > vars.digitalCounter
					+ vars.digitalCounter / 10;
		if (vars.dispatchableCounter == stats.getDispatchableLostCount()
				&& vars.analogCounter == stats.getDelayedAICount()
				&& vars.digitalMaxDelay == stats.getMaximumDIDelay() && !showDI)
			return;
		vars.dispatchableCounter = stats.getDispatchableLostCount();
		vars.analogCounter = stats.getDelayedAICount();
		vars.digitalCounter = stats.getDelayedDICount();
		vars.digitalMaxDelay = stats.getMaximumDIDelay();
		logger.info(vars.getOwner() + " statistics:");
		String msg = "number of dispatchable discarded for full queue: "
				+ stats.getDispatchableLostCount();
		if (stats.getDispatchableLostCount() == 0)
			logger.info(msg);
		else
			logger.error(msg);
		logger.info("delayed DI acquisition (Count/Max delay [ms]): "
				+ stats.getDelayedDICount() + "/" + stats.getMaximumDIDelay());
		logger.info("intime DI acquisition  (Count/Avg time  [ms]): "
				+ stats.getIntimeDICount() + "/"
				+ statsFormat.format(stats.getIntimeDIAverage()));
		logger.info("delayed AI acquisition (Count/Max delay [ms]): "
				+ stats.getDelayedAICount() + "/" + stats.getMaximumAIDelay());
		logger.info("intime AI acquisition  (Count/Avg time  [ms]): "
				+ stats.getIntimeAICount() + "/"
				+ statsFormat.format(stats.getIntimeAIAverage()));
	}

	private void logMemory(boolean logInfo) {
		if (logInfo)
			logger.info(printMemory());
		else if (logger.isDebugEnabled())
			logger.debug(printMemory());
	}

	private String printMemory() {
		Runtime rt = java.lang.Runtime.getRuntime();
		long usedMem = rt.totalMemory() - rt.freeMemory();
		return "JVM heap memory used/total: " + (usedMem / 1024) + "/"
				+ (rt.totalMemory() / 1024) + " KiB";
	}

	private class StatVars {
		private String owner;
		private AcqStats stats;

		long digitalMaxDelay = -1;
		long digitalCounter = -1;
		long analogCounter = -1;
		long dispatchableCounter = -1;

		StatVars(String owner, AcqStats stats) {
			this.owner = owner;
			this.stats = stats;
		}

		String getOwner() {
			return owner;
		}

		AcqStats getStats() {
			return stats;
		}
	}

}
