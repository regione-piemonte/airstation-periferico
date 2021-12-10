/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file based data storage for event data
// Change log:
//   2008-06-23: initial version
// ----------------------------------------------------------------------------
// $Id: EventDataStorage.java,v 1.15 2015/05/27 14:59:26 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.core.BinaryStatus;
import it.csi.periferico.core.IntegerStatus;
import it.csi.periferico.core.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * File based data storage for event data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class EventDataStorage extends AnalyzerDataStorage {

	static final String FILE_PREFIX = "e_";

	static final String FILE_EXT = ".txt";

	static final String FILE_DIR = "events";

	private static Logger logger = Logger.getLogger("periferico."
			+ EventDataStorage.class.getSimpleName());

	private String eventId;

	public EventDataStorage(UUID stationId, String stationName,
			UUID analyzerId, String analyzerName, String eventId) {
		super(NewFileRate.MONTHLY, stationId, stationName, analyzerId,
				analyzerName);
		if (eventId == null)
			throw new IllegalArgumentException("Event ID cannot be null");
		this.eventId = eventId;
	}

	List<BinaryStatus> readEventData(Date startTime, boolean startTimeIncluded,
			Date endTime, boolean endTimeIncluded, Integer maxData)
			throws StorageException {
		List<Value> data = readData(startTime, startTimeIncluded, endTime,
				endTimeIncluded, maxData);
		List<BinaryStatus> eventData = new ArrayList<BinaryStatus>();
		for (Value value : data)
			eventData.add((BinaryStatus) value);
		return eventData;
	}

	BinaryStatus getLastEventWithin(Date timeStamp, int maxDaysOfData)
			throws StorageException {
		if (getStationId() == null)
			throw new IllegalStateException("Station ID cannot be null");
		if (timeStamp == null)
			throw new IllegalArgumentException("Timestamp should not be null");
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(timeStamp);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		File file = new File(getAbsoluteFilePath(timeStamp));
		int limit = maxDaysOfData / 30;
		while (limit-- >= 0) {
			if (file.exists()) {
				logger.debug("Data read request for file: " + file);
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(file));
					checkHeader(br, timeStamp);
					BinaryStatus datum = findDatum(br, timeStamp);
					if (datum != null)
						return datum;
				} catch (FileNotFoundException e) {
					logger.error("File not found: " + file, e);
					throw new StorageException("file_not_found_error",
							file.getPath());
				} catch (IOException e) {
					logger.error("IO error reading file: " + file, e);
					throw new StorageException("io_error", file.getPath());
				} catch (HeaderException e) {
					logger.error("Invalid header for file: " + file, e);
					throw new StorageException("invalid_header", file.getPath());
				} finally {
					if (br != null)
						try {
							br.close();
						} catch (IOException e) {
							logger.error("Error closing file: " + file);
						}
				}
			}
			calendar.add(Calendar.MONTH, -1);
		}
		return null;
	}

	private BinaryStatus findDatum(BufferedReader br, Date timeStamp)
			throws IOException {
		BinaryStatus prevValue = null;
		Date prevValueTs = null;
		String line;
		while ((line = readLine(br)) != null) {
			BinaryStatus value = parseDataLine(null, line);
			if (value == null)
				continue;
			Date valueTs = value.getTimestamp();
			if (prevValueTs != null && !valueTs.after(prevValueTs)) {
				logger.warn("Datum timestamp out of sequence in file "
						+ getAbsoluteFilePath(valueTs) + ": " + line);
				continue;
			}
			if (valueTs.after(timeStamp))
				break;
			prevValueTs = valueTs;
			prevValue = value;
		}
		return prevValue;
	}

	String getFileExtension() {
		return FILE_EXT;
	}

	String getFilePrefix() {
		return FILE_PREFIX;
	}

	String getDirName() {
		return super.getDirName() + File.separator + FILE_DIR + File.separator
				+ eventId;
	}

	void writeHeader(PrintWriter pw, Date date) {
		pw.println("id = EVENT");
		pw.println("version = 1.0");
		pw.println("station = " + getStationId());
		if (getStationName() != null)
			pw.println("station_name = " + getStationName());
		else
			pw.println("station_name =");
		pw.println("analyzer = " + getAnalyzerId());
		if (getAnalyzerName() != null)
			pw.println("analyzer_name = " + getAnalyzerName());
		else
			pw.println("analyzer_name =");
		pw.println("source = " + eventId);
	}

	@Override
	void checkHeader(BufferedReader br, Date date) throws IOException,
			HeaderException {
		checkHeaderLine(br, "id", "EVENT");
		checkHeaderLine(br, "version", "1.0");
		if (getStationId() != null)
			checkHeaderLine(br, "station", getStationId().toString());
		else
			getHeaderLineValue(br, "station");
		getHeaderLineValue(br, "station_name");
		checkHeaderLine(br, "analyzer", getAnalyzerId().toString());
		getHeaderLineValue(br, "analyzer_name");
		checkHeaderLine(br, "source", eventId);
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
			pw.print(bs.isOn() ? "1" : "0");
			if (bs instanceof IntegerStatus) {
				IntegerStatus is = (IntegerStatus) bs;
				pw.print(",");
				Integer intValue = is.getValue();
				if (intValue != null)
					pw.print(Integer.toHexString(intValue));
			}
			pw.println();
		}
	}

	@Override
	BinaryStatus parseDataLine(String strDate, String line) {
		String[] fields = line.split(",", -1);
		if (fields.length < 2 || fields.length > 3)
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
			if (fields.length == 2) {
				return new BinaryStatus(timestamp_msFmt.parse(strTimestamp),
						value);
			}
			String strIntValue = fields[2].trim();
			Integer intValue = strIntValue.isEmpty() ? null : (int) Long
					.parseLong(strIntValue, 16);
			return new IntegerStatus(timestamp_msFmt.parse(strTimestamp),
					intValue, value);
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
		if (fields.length < 2 || fields.length > 3)
			return null;
		try {
			String strTimestamp = fields[0].trim();
			return timestamp_msFmt.parse(strTimestamp);
		} catch (ParseException e) {
			return null;
		}
	}

	static List<EventDataStorage> listStorages() {
		List<EventDataStorage> list = new ArrayList<EventDataStorage>();
		for (UUID analyzer : listAnalyzers()) {
			File dir = new File(DATA_DIR + File.separator + DIR_ANALYZERS
					+ File.separator + analyzer + File.separator + FILE_DIR);
			if (dir.isDirectory()) {
				File[] subdirs = dir.listFiles();
				if (subdirs != null)
					for (int i = 0; i < subdirs.length; i++)
						if (subdirs[i].isDirectory())
							list.add(new EventDataStorage(null, null, analyzer,
									null, subdirs[i].getName()));
			}
		}
		return list;
	}

}
