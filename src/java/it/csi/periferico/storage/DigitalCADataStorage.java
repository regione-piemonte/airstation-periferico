/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file based data storage for digital container alarm data
// Change log:
//   2008-06-25: initial version
// ----------------------------------------------------------------------------
// $Id: DigitalCADataStorage.java,v 1.12 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.core.BinaryStatus;
import it.csi.periferico.core.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * File based data storage for digital container alarm data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DigitalCADataStorage extends ContainerAlarmDataStorage {

	static final String FILE_PREFIX = "dca_";

	static final String FILE_EXT = ".txt";

	private String alarmId;

	DigitalCADataStorage(UUID stationId, String stationName,
			UUID containerAlarmId, String alarmDescription, String alarmId) {
		super(NewFileRate.MONTHLY, stationId, stationName, containerAlarmId,
				alarmDescription);
		if (alarmId == null)
			throw new IllegalArgumentException("Alarm ID cannot be null");
		this.alarmId = alarmId;
	}

	List<BinaryStatus> readDigitalCAData(Date startTime,
			boolean startTimeIncluded, Date endTime, boolean endTimeIncluded,
			Integer maxData) throws StorageException {
		List<Value> data = readData(startTime, startTimeIncluded, endTime,
				endTimeIncluded, maxData);
		List<BinaryStatus> digitalCAData = new ArrayList<BinaryStatus>();
		for (Value value : data)
			digitalCAData.add((BinaryStatus) value);
		return digitalCAData;
	}

	String getFileExtension() {
		return FILE_EXT;
	}

	String getFilePrefix() {
		return FILE_PREFIX;
	}

	String getDirName() {
		return super.getDirName() + File.separator + alarmId;
	}

	void writeHeader(PrintWriter pw, Date date) {
		pw.println("id = DIGITAL_CONTAINER_ALARM");
		pw.println("version = 1.0");
		pw.println("station = " + getStationId());
		if (getStationName() != null)
			pw.println("station_name = " + getStationName());
		else
			pw.println("station_name =");
		pw.println("container_alarm = " + getContainerAlarmId());
		if (getAlarmDescription() != null)
			pw.println("alarm_description = " + getAlarmDescription());
		else
			pw.println("alarm_description =");
		pw.println("alarm = " + alarmId);
	}

	@Override
	void checkHeader(BufferedReader br, Date date) throws IOException,
			HeaderException {
		checkHeaderLine(br, "id", "DIGITAL_CONTAINER_ALARM");
		checkHeaderLine(br, "version", "1.0");
		if (getStationId() != null)
			checkHeaderLine(br, "station", getStationId().toString());
		else
			getHeaderLineValue(br, "station");
		getHeaderLineValue(br, "station_name");
		checkHeaderLine(br, "container_alarm", getContainerAlarmId().toString());
		getHeaderLineValue(br, "alarm_description");
		checkHeaderLine(br, "alarm", alarmId);
	}

	@Override
	int getHeaderLineNumber() {
		return 7;
	}

	void appendData(PrintWriter pw, List<Value> data) {
		for (Value value : data) {
			BinaryStatus bs = (BinaryStatus) value;
			pw.print(timestamp_msFmt.format(bs.getTimestamp()));
			pw.print(",");
			pw.println(bs.isOn() ? "1" : "0");
		}
	}

	@Override
	Value parseDataLine(String strDate, String line) {
		String[] fields = line.split(",", -1);
		if (fields.length != 2)
			return null;
		try {
			String strTimestamp = fields[0].trim();
			String strValue = fields[1].trim();
			boolean value;
			if ("1".equals(strValue))
				value = true;
			else if ("0".equals(strValue))
				value = false;
			else
				return null;
			return new BinaryStatus(timestamp_msFmt.parse(strTimestamp), value);
		} catch (NoSuchElementException ex) {
			return null;
		} catch (NumberFormatException e) {
			return null;
		} catch (ParseException e) {
			return null;
		}
	}

	@Override
	Date parseTimestamp(String strDate, String line) {
		String[] fields = line.split(",", -1);
		if (fields.length != 2)
			return null;
		try {
			String strTimestamp = fields[0].trim();
			return timestamp_msFmt.parse(strTimestamp);
		} catch (ParseException e) {
			return null;
		}
	}

	static List<DigitalCADataStorage> listStorages() {
		List<DigitalCADataStorage> list = new ArrayList<DigitalCADataStorage>();
		for (UUID ca : listContainerAlarms()) {
			File dir = new File(DATA_DIR + File.separator
					+ DIR_CONTAINER_ALARMS + File.separator + ca);
			if (dir.isDirectory()) {
				File[] subdirs = dir.listFiles();
				if (subdirs != null)
					for (int i = 0; i < subdirs.length; i++)
						if (subdirs[i].isDirectory())
							list.add(new DigitalCADataStorage(null, null, ca,
									null, subdirs[i].getName()));
			}
		}
		return list;
	}

}
