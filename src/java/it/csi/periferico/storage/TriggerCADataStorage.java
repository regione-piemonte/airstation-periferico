/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file based data storage for trigger container alarm data
// Change log:
//   2008-06-26: initial version
// ----------------------------------------------------------------------------
// $Id: TriggerCADataStorage.java,v 1.11 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.core.AlarmStatus;
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
 * File based data storage for trigger container alarm data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class TriggerCADataStorage extends ContainerAlarmDataStorage {

	static final String FILE_PREFIX = "tca_";

	static final String FILE_EXT = ".txt";

	private String alarmId;

	TriggerCADataStorage(UUID stationId, String stationName,
			UUID containerAlarmId, String alarmDescription, String alarmId) {
		super(NewFileRate.MONTHLY, stationId, stationName, containerAlarmId,
				alarmDescription);
		if (alarmId == null)
			throw new IllegalArgumentException("Alarm ID cannot be null");
		this.alarmId = alarmId;
	}

	List<AlarmStatus> readTriggerCAData(Date startTime,
			boolean startTimeIncluded, Date endTime, boolean endTimeIncluded,
			Integer maxData) throws StorageException {
		List<Value> data = readData(startTime, startTimeIncluded, endTime,
				endTimeIncluded, maxData);
		List<AlarmStatus> triggerCAData = new ArrayList<AlarmStatus>();
		for (Value value : data)
			triggerCAData.add((AlarmStatus) value);
		return triggerCAData;
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
		pw.println("id = TRIGGER_CONTAINER_ALARM");
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
		checkHeaderLine(br, "id", "TRIGGER_CONTAINER_ALARM");
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
			AlarmStatus as = (AlarmStatus) value;
			pw.print(timestampFmt.format(as.getTimestamp()));
			pw.print(",");
			String strStatus = null;
			if (as.getStatus() == AlarmStatus.Status.OK)
				strStatus = "OK";
			else if (as.getStatus() == AlarmStatus.Status.ALARM)
				strStatus = "A";
			else if (as.getStatus() == AlarmStatus.Status.ALARM_HIGH)
				strStatus = "AH";
			else if (as.getStatus() == AlarmStatus.Status.ALARM_LOW)
				strStatus = "AL";
			else if (as.getStatus() == AlarmStatus.Status.WARNING)
				strStatus = "W";
			else if (as.getStatus() == AlarmStatus.Status.WARNING_HIGH)
				strStatus = "WH";
			else if (as.getStatus() == AlarmStatus.Status.WARNING_LOW)
				strStatus = "WL";
			else
				strStatus = "?";
			pw.println(strStatus);
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
			AlarmStatus.Status value;
			if ("OK".equals(strValue))
				value = AlarmStatus.Status.OK;
			else if ("A".equals(strValue))
				value = AlarmStatus.Status.ALARM;
			else if ("AH".equals(strValue))
				value = AlarmStatus.Status.ALARM_HIGH;
			else if ("AL".equals(strValue))
				value = AlarmStatus.Status.ALARM_LOW;
			else if ("W".equals(strValue))
				value = AlarmStatus.Status.WARNING;
			else if ("WH".equals(strValue))
				value = AlarmStatus.Status.WARNING_HIGH;
			else if ("WL".equals(strValue))
				value = AlarmStatus.Status.WARNING_LOW;
			else
				return null;
			return new AlarmStatus(timestampFmt.parse(strTimestamp), value);
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
			return timestampFmt.parse(strTimestamp);
		} catch (ParseException e) {
			return null;
		}
	}

	static List<TriggerCADataStorage> listStorages() {
		List<TriggerCADataStorage> list = new ArrayList<TriggerCADataStorage>();
		for (UUID ca : listContainerAlarms()) {
			File dir = new File(DATA_DIR + File.separator
					+ DIR_CONTAINER_ALARMS + File.separator + ca);
			if (dir.isDirectory()) {
				File[] subdirs = dir.listFiles();
				if (subdirs != null)
					for (int i = 0; i < subdirs.length; i++)
						if (subdirs[i].isDirectory())
							list.add(new TriggerCADataStorage(null, null, ca,
									null, subdirs[i].getName()));
			}
		}
		return list;
	}

}
