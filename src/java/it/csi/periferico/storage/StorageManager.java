/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: data storage public functions and management
// Change log:
//   2008-01-22: initial version
// ----------------------------------------------------------------------------
// $Id: StorageManager.java,v 1.36 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.Periferico;
import it.csi.periferico.config.common.StorageManagerCfg;
import it.csi.periferico.core.AlarmStatus;
import it.csi.periferico.core.BinaryStatus;
import it.csi.periferico.core.CounterElement;
import it.csi.periferico.core.GenericSampleElement;
import it.csi.periferico.core.MeanValue;
import it.csi.periferico.core.Sample;
import it.csi.periferico.core.ScalarAggregateValue;
import it.csi.periferico.core.ScalarElement;
import it.csi.periferico.core.TotalValue;
import it.csi.periferico.core.WindElement;
import it.csi.periferico.core.WindValue;
import it.csi.periferico.gps.GpsDatum;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Data storage public functions and management
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class StorageManager {

	private static Logger logger = Logger.getLogger("periferico."
			+ StorageManager.class.getSimpleName());

	public enum Status {
		UNAVAILABLE, OK, WARNING, ERROR
	}

	private StorageManagerCfg storageManagerCfg;

	private Integer rootFsUsedSpacePercent = null;

	private Integer tmpFsUsedSpacePercent = null;

	private Integer dataFsUsedSpacePercent = null;

	private Status smartStatus = null;

	private Status raidStatus = null;

	private Periferico p;

	private boolean osWindows = false;

	// TODO: it may be useful to implement a function that searches all data
	// files, when the application is starting, detecting eventual data with
	// date/time in the future: if this happens the application should show an
	// error refusing to start
	public StorageManager(StorageManagerCfg storageManagerCfg) {
		if (storageManagerCfg == null)
			storageManagerCfg = new StorageManagerCfg();
		this.storageManagerCfg = storageManagerCfg;
		p = Periferico.getInstance();
		String os = System.getProperty("os.name");
		if (os != null)
			osWindows = os.toLowerCase().trim().startsWith("win");
	}

	public void saveSampleData(UUID analyzerId, String paramId,
			int numDecimals, List<Sample> data) throws StorageException {
		UUID stId = p.getStationId();
		SampleDataStorage sds = new SampleDataStorage(stId,
				p.getResourceName(stId), analyzerId,
				p.getResourceName(analyzerId), paramId, numDecimals);
		sds.writeData(data);
	}

	public List<Sample> readSampleData(UUID analyzerId, String paramId,
			Date startTime, boolean startIncluded, Date endTime,
			boolean endIncluded, Integer maxData) throws StorageException {
		UUID stId = p.getStationId();
		SampleDataStorage sds = new SampleDataStorage(stId,
				p.getResourceName(stId), analyzerId,
				p.getResourceName(analyzerId), paramId, 0);
		return sds.readSampleData(startTime, startIncluded, endTime,
				endIncluded, maxData);
	}

	public void saveEventData(UUID analyzerId, String eventId,
			List<BinaryStatus> data) throws StorageException {
		UUID stId = p.getStationId();
		EventDataStorage eds = new EventDataStorage(stId,
				p.getResourceName(stId), analyzerId,
				p.getResourceName(analyzerId), eventId);
		eds.writeData(data);
	}

	public List<BinaryStatus> readEventData(UUID analyzerId, String eventId,
			Date startTime, boolean startIncluded, Date endTime,
			boolean endIncluded, Integer maxData) throws StorageException {
		return readEventData(analyzerId, eventId, startTime, startIncluded,
				endTime, endIncluded, maxData, false);
	}

	public List<BinaryStatus> readEventData(UUID analyzerId, String eventId,
			Date startTime, boolean startIncluded, Date endTime,
			boolean endIncluded, Integer maxData, boolean findInitialEvent)
			throws StorageException {
		UUID stId = p.getStationId();
		EventDataStorage eds = new EventDataStorage(stId,
				p.getResourceName(stId), analyzerId,
				p.getResourceName(analyzerId), eventId);
		List<BinaryStatus> listBs = eds.readEventData(startTime, startIncluded,
				endTime, endIncluded, maxData);
		if (findInitialEvent
				&& (listBs.isEmpty() || !listBs.get(0).getTimestamp()
						.equals(startTime))) {
			BinaryStatus bs = eds.getLastEventWithin(startTime,
					getMaxPossibleDaysOfEventData());
			if (bs != null) {
				listBs.add(0, bs);
				if (maxData != null && listBs.size() > maxData)
					listBs.remove(listBs.size() - 1);
			}
		}
		return listBs;
	}

	public void saveScalarAggregateData(UUID analyzerId, ScalarElement element,
			int period, List<ScalarAggregateValue> data)
			throws StorageException {
		UUID stId = p.getStationId();
		if (element instanceof GenericSampleElement)
			new MeanDataStorage(stId, p.getResourceName(stId), analyzerId,
					p.getResourceName(analyzerId), element.getParameterId(),
					element.getNumDec(), period).writeData(data);
		else if (element instanceof CounterElement)
			new TotalDataStorage(stId, p.getResourceName(stId), analyzerId,
					p.getResourceName(analyzerId), element.getParameterId(),
					element.getNumDec(), period).writeData(data);
		else
			throw new UnsupportedOperationException("Saving scalar aggregate "
					+ "data for class " + element.getClass().getName()
					+ " is not supported");
	}

	public List<MeanValue> readMeanData(UUID analyzerId, String paramId,
			int period, Date startTime, boolean startIncluded, Date endTime,
			boolean endIncluded, Integer maxData) throws StorageException {
		UUID stId = p.getStationId();
		MeanDataStorage mds = new MeanDataStorage(stId,
				p.getResourceName(stId), analyzerId,
				p.getResourceName(analyzerId), paramId, 0, period);
		return mds.readMeanData(startTime, startIncluded, endTime, endIncluded,
				maxData);
	}

	public List<TotalValue> readTotalData(UUID analyzerId, String paramId,
			int period, Date startTime, boolean startIncluded, Date endTime,
			boolean endIncluded, Integer maxData) throws StorageException {
		UUID stId = p.getStationId();
		TotalDataStorage tds = new TotalDataStorage(stId,
				p.getResourceName(stId), analyzerId,
				p.getResourceName(analyzerId), paramId, 0, period);
		return tds.readTotalData(startTime, startIncluded, endTime,
				endIncluded, maxData);
	}

	public void saveWindAggregateData(UUID analyzerId, WindElement element,
			Integer period, List<WindValue> windAggregateValues)
			throws StorageException {
		UUID stId = p.getStationId();
		new WindDataStorage(stId, p.getResourceName(stId), analyzerId,
				p.getResourceName(analyzerId), element.getParameterId(),
				element.getSpeedNumDec(), element.getDirectionNumDec(), period)
				.writeData(windAggregateValues);
	}

	public List<WindValue> readWindAggregateData(UUID analyzerId,
			String paramId, int period, Date startTime, boolean startIncluded,
			Date endTime, boolean endIncluded, Integer maxData)
			throws StorageException {
		UUID stId = p.getStationId();
		WindDataStorage wds = new WindDataStorage(stId,
				p.getResourceName(stId), analyzerId,
				p.getResourceName(analyzerId), paramId, 0, 0, period);
		return wds.readWindData(startTime, startIncluded, endTime, endIncluded,
				maxData);
	}

	public void saveDigitalCAData(UUID containerAlarmId, String alarmId,
			List<BinaryStatus> data) throws StorageException {
		UUID stId = p.getStationId();
		DigitalCADataStorage ds = new DigitalCADataStorage(stId,
				p.getResourceName(stId), containerAlarmId,
				p.getResourceName(containerAlarmId), alarmId);
		ds.writeData(data);
	}

	public List<BinaryStatus> readDigitalCAData(UUID containerAlarmId,
			String alarmId, Date startTime, boolean startIncluded,
			Date endTime, boolean endIncluded, Integer maxData)
			throws StorageException {
		UUID stId = p.getStationId();
		DigitalCADataStorage ds = new DigitalCADataStorage(stId,
				p.getResourceName(stId), containerAlarmId,
				p.getResourceName(containerAlarmId), alarmId);
		return ds.readDigitalCAData(startTime, startIncluded, endTime,
				endIncluded, maxData);
	}

	public void saveTriggerCAData(UUID containerAlarmId, String alarmId,
			List<AlarmStatus> data) throws StorageException {
		UUID stId = p.getStationId();
		TriggerCADataStorage ds = new TriggerCADataStorage(stId,
				p.getResourceName(stId), containerAlarmId,
				p.getResourceName(containerAlarmId), alarmId);
		ds.writeData(data);
	}

	public List<AlarmStatus> readTriggerCAData(UUID containerAlarmId,
			String alarmId, Date startTime, boolean startIncluded,
			Date endTime, boolean endIncluded, Integer maxData)
			throws StorageException {
		UUID stId = p.getStationId();
		TriggerCADataStorage ds = new TriggerCADataStorage(stId,
				p.getResourceName(stId), containerAlarmId,
				p.getResourceName(containerAlarmId), alarmId);
		return ds.readTriggerCAData(startTime, startIncluded, endTime,
				endIncluded, maxData);
	}

	public void saveGpsData(List<GpsDatum> data) throws StorageException {
		UUID stId = p.getStationId();
		GpsDataStorage gds = new GpsDataStorage(stId, p.getResourceName(stId));
		gds.writeData(data);
	}

	public List<GpsDatum> readGpsData(Date startTime, boolean startIncluded,
			Date endTime, boolean endIncluded, Integer maxData,
			boolean bestHourlyDatumOnly) throws StorageException {
		UUID stId = p.getStationId();
		GpsDataStorage gds = new GpsDataStorage(stId, p.getResourceName(stId));
		return gds.readGpsData(startTime, startIncluded, endTime, endIncluded,
				maxData, bestHourlyDatumOnly);
	}

	public void deleteOldData() {
		DataStorage.deleteOldFiles(storageManagerCfg.getMaxDaysOfData(),
				storageManagerCfg.getMaxDaysOfAggregateData());
	}

	public boolean findAndDeleteDataInTheFuture(boolean findOnly) {
		return DataStorage
				.findAndDeleteAllDataInTheFuture(new Date(), findOnly);
	}

	public int getMaxPossibleDaysOfEventData() {
		return storageManagerCfg.getMaxDaysOfData() + 31;
	}

	public void checkDiskSpace() {
		File rootFs = new File(osWindows ? "C:\\" : "/");
		rootFsUsedSpacePercent = computeUsedSpacePercent(
				rootFs.getTotalSpace(), rootFs.getFreeSpace());
		if (!osWindows) {
			File tmpFs = new File("/tmp");
			tmpFsUsedSpacePercent = computeUsedSpacePercent(tmpFs.getTotalSpace(),
					tmpFs.getFreeSpace());
		}
		File dataFs = new File(DataStorage.DATA_DIR);
		if (!dataFs.exists()) {
			if (!dataFs.mkdirs())
				logger.error("Cannot create data directory: " + dataFs);
			logger.info("Created data directory: " + dataFs);
		}
		dataFsUsedSpacePercent = computeUsedSpacePercent(
				dataFs.getTotalSpace(), dataFs.getUsableSpace());
		logger.debug("File system usage: root " + rootFsUsedSpacePercent + "%");
		logger.debug("File system usage: temp " + tmpFsUsedSpacePercent + "%");
		logger.debug("File system usage: data " + dataFsUsedSpacePercent + "%");
	}

	public void checkSmart() {
		logger.debug("Checking SMART status...");
		ProcessBuilder pb = new ProcessBuilder(osWindows ? "checkSmart.bat" : "checkSmart.sh");
		String ls = System.getProperty("line.separator");
		Process p = null;
		BufferedReader br = null;
		try {
			p = pb.start();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer commandLog = new StringBuffer();
			String line;
			boolean firstLine = true;
			while ((line = br.readLine()) != null) {
				if (!firstLine)
					commandLog.append(ls);
				commandLog.append(line);
				firstLine = false;
			}
			logger.debug(commandLog.toString());
			int commandResult = p.waitFor();
			switch (commandResult) {
			case 0:
				smartStatus = Status.UNAVAILABLE;
				break;
			case 1:
				smartStatus = Status.OK;
				break;
			case 2:
				smartStatus = Status.WARNING;
				break;
			case 3:
				smartStatus = Status.ERROR;
				break;
			default:
				logger.error("Unexpected return code from SMART check script: "
						+ commandResult);
				smartStatus = Status.ERROR;
				break;
			}
			logger.debug("SMART status is: " + smartStatus);
		} catch (Exception ex) {
			logger.error("Cannot execute SMART check script", ex);
			smartStatus = Status.ERROR;
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

	public void checkRaid() {
		logger.debug("Checking RAID status...");
		ProcessBuilder pb = new ProcessBuilder(osWindows ?  "checkRaid.bat" : "checkRaid.sh");
		String ls = System.getProperty("line.separator");
		Process p = null;
		BufferedReader br = null;
		try {
			p = pb.start();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer commandLog = new StringBuffer();
			String line;
			boolean firstLine = true;
			while ((line = br.readLine()) != null) {
				if (!firstLine)
					commandLog.append(ls);
				commandLog.append(line);
				firstLine = false;
			}
			logger.debug(commandLog.toString());
			int commandResult = p.waitFor();
			switch (commandResult) {
			case 0:
				raidStatus = Status.UNAVAILABLE;
				break;
			case 1:
				raidStatus = Status.OK;
				break;
			case 2:
				raidStatus = Status.WARNING;
				break;
			case 3:
				raidStatus = Status.ERROR;
				break;
			default:
				logger.error("Unexpected return code from RAID check script: "
						+ commandResult);
				raidStatus = Status.ERROR;
				break;
			}
			logger.debug("RAID status is: " + raidStatus);
		} catch (Exception ex) {
			logger.error("Cannot execute RAID check script", ex);
			raidStatus = Status.ERROR;
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

	private Integer computeUsedSpacePercent(long total, long free) {
		if (total <= 0)
			return 100;
		if (total - free <= 0)
			return 0;
		return new Integer((int) (((total - free) * 100) / total));
	}

	public Integer getDataFsUsedSpacePercent() {
		return dataFsUsedSpacePercent;
	}

	public Integer getRootFsUsedSpacePercent() {
		return rootFsUsedSpacePercent;
	}

	public Integer getTmpFsUsedSpacePercent() {
		return tmpFsUsedSpacePercent;
	}

	public int getUsedSpaceWarningThresholdPercent() {
		return storageManagerCfg.getDiskFullWarningThresholdPercent();
	}

	public int getUsedSpaceAlarmThresholdPercent() {
		return storageManagerCfg.getDiskFullAlarmThresholdPercent();
	}

	public Status getRaidStatus() {
		return raidStatus;
	}

	public Status getSmartStatus() {
		return smartStatus;
	}

}
