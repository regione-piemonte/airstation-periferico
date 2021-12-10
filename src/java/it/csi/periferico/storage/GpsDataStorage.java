/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file based data storage for gps data
// Change log:
//   2008-09-29: initial version
// ----------------------------------------------------------------------------
// $Id: GpsDataStorage.java,v 1.5 2011/07/14 08:36:42 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.core.Value;
import it.csi.periferico.gps.GpsDatum;

import java.io.BufferedReader;
import java.io.File;
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

/**
 * File based data storage for gps data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class GpsDataStorage extends DataStorage {

	static final String DIR_GPSDATA = "gps";

	private static final String FILE_TYPE = "GPSDATA";

	static final String FILE_PREFIX = "gpsdata_";

	static final String FILE_EXT = ".txt";

	public GpsDataStorage(UUID stationId, String stationName) {
		super(NewFileRate.DAILY, stationId, stationName);
	}

	@Override
	String getDirName() {
		String dirName = super.getDirName() + File.separator + DIR_GPSDATA;
		return dirName;
	}

	@Override
	String getFilePrefix() {
		return FILE_PREFIX;
	}

	@Override
	String getFileExtension() {
		return FILE_EXT;
	}

	@Override
	void writeHeader(PrintWriter pw, Date date) {
		pw.println("id = " + FILE_TYPE);
		pw.println("version = 1.0");
		pw.println("station = " + getStationId());
		if (getStationName() != null)
			pw.println("station_name = " + getStationName());
		else
			pw.println("station_name =");
		pw.println("date = " + dateFmt.format(date));
	}

	@Override
	void checkHeader(BufferedReader br, Date date) throws IOException,
			HeaderException {
		checkHeaderLine(br, "id", FILE_TYPE);
		checkHeaderLine(br, "version", "1.0");
		if (getStationId() != null)
			checkHeaderLine(br, "station", getStationId().toString());
		else
			getHeaderLineValue(br, "station");
		getHeaderLineValue(br, "station_name");
		checkHeaderLine(br, "date", dateFmt.format(date));
	}

	@Override
	int getHeaderLineNumber() {
		return 5;
	}

	@Override
	void appendData(PrintWriter pw, List<Value> data) {
		for (Value value : data) {
			GpsDatum gd = (GpsDatum) value;
			pw.print(timeFmt.format(gd.getTimestamp()));
			pw.print(",");
			if (gd.getLatitude() != null)
				pw.print(gd.getLatitude());
			pw.print(",");
			if (gd.getLongitude() != null)
				pw.print(gd.getLongitude());
			pw.print(",");
			if (gd.getAltitude() != null)
				pw.print(gd.getAltitude());
			pw.print(",");
			pw.print(gd.getFix());
			pw.println();
		}
	}

	@Override
	Value parseDataLine(String strDate, String line) {
		String[] fields = line.split(",", -1);
		if (fields.length != 5)
			return null;
		try {
			String strTimestamp = strDate + TIMESTAMP_SEPARATOR
					+ fields[0].trim();
			String strLatitude = fields[1].trim();
			String strLongitude = fields[2].trim();
			String strAltitude = fields[3].trim();
			String strFix = fields[4].trim();
			Double latitude = null;
			Double longitude = null;
			Double altitude = null;
			if (!strLatitude.isEmpty())
				latitude = Double.valueOf(strLatitude);
			if (!strLongitude.isEmpty())
				longitude = Double.valueOf(strLongitude);
			if (!strAltitude.isEmpty())
				altitude = Double.valueOf(strAltitude);
			GpsDatum.Fix fix = GpsDatum.Fix.valueOf(strFix);
			return new GpsDatum(timestampFmt.parse(strTimestamp), latitude,
					longitude, altitude, fix);
		} catch (NoSuchElementException ex) {
			return null;
		} catch (NumberFormatException e) {
			return null;
		} catch (ParseException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	Date parseTimestamp(String strDate, String line) {
		String[] fields = line.split(",", -1);
		if (fields.length != 5)
			return null;
		try {
			String strTimestamp = strDate + TIMESTAMP_SEPARATOR
					+ fields[0].trim();
			return timestampFmt.parse(strTimestamp);
		} catch (ParseException e) {
			return null;
		}
	}

	List<GpsDatum> readGpsData(Date startTime, boolean startTimeIncluded,
			Date endTime, boolean endTimeIncluded, Integer maxData,
			boolean bestHourlyDatumOnly) throws StorageException {
		List<Value> data = readData(startTime, startTimeIncluded, endTime,
				endTimeIncluded, maxData);
		List<GpsDatum> gpsData = new ArrayList<GpsDatum>();
		if (!bestHourlyDatumOnly) {
			for (Value value : data)
				gpsData.add((GpsDatum) value);
			return gpsData;
		}
		GpsDatum hourlyDatum = null;
		for (Value value : data) {
			GpsDatum datum = (GpsDatum) value;
			if (hourlyDatum == null) {
				hourlyDatum = datum;
				continue;
			}
			if (inSameHour(hourlyDatum, datum)) {
				if (hasBetterFix(hourlyDatum, datum))
					hourlyDatum = datum;
			} else {
				gpsData.add(hourlyDatum);
				hourlyDatum = datum;
			}
		}
		if (hourlyDatum != null)
			gpsData.add(hourlyDatum);
		return gpsData;
	}

	private boolean inSameHour(GpsDatum datum, GpsDatum otherDatum) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(datum.getTimestamp());
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date date = calendar.getTime();
		calendar.setTime(otherDatum.getTimestamp());
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date otherDate = calendar.getTime();
		return date.equals(otherDate);
	}

	private boolean hasBetterFix(GpsDatum datum, GpsDatum otherDatum) {
		return gpsFixToScore(otherDatum.getFix()) > gpsFixToScore(datum
				.getFix());
	}

	private int gpsFixToScore(GpsDatum.Fix fix) {
		if (fix == GpsDatum.Fix.FIX_3D)
			return 5;
		else if (fix == GpsDatum.Fix.FIX_2D)
			return 4;
		else if (fix == GpsDatum.Fix.NO_FIX)
			return 3;
		else if (fix == GpsDatum.Fix.GPS_READ_ERROR)
			return 2;
		else if (fix == GpsDatum.Fix.GPS_APP_ERROR)
			return 1;
		return 0;
	}

	static List<GpsDataStorage> listStorages() {
		List<GpsDataStorage> list = new ArrayList<GpsDataStorage>();
		list.add(new GpsDataStorage(null, null));
		return list;
	}

}
