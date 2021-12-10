/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file based data storage for wind vectorial aggregate data
// Change log:
//   2008-08-14: initial version
// ----------------------------------------------------------------------------
// $Id: WindDataStorage.java,v 1.11 2013/10/09 10:22:03 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.Periferico;
import it.csi.periferico.core.Value;
import it.csi.periferico.core.WindValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * File based data storage for wind vectorial aggregate data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class WindDataStorage extends AnalyzerDataStorage {

	static final String FILE_TYPE = "WIND";

	static final String FILE_PREFIX = "w_";

	static final String FILE_EXT = ".txt";

	static final String FILE_DIR = "wind";

	private String paramId;

	private int period;

	private DecimalFormat decimalFormatSpeed;

	private DecimalFormat decimalFormatDir;

	private DecimalFormat decimalFormatPercent;

	public WindDataStorage(UUID stationId, String stationName, UUID analyzerId,
			String analyzerName, String paramId, int numDecimalsForSpeed,
			int numDecimalsForDir, int period) {
		super(NewFileRate.DAILY, stationId, stationName, analyzerId,
				analyzerName);
		if (paramId == null)
			throw new IllegalArgumentException("Parameter ID cannot be null");
		if (numDecimalsForSpeed < 0)
			throw new IllegalArgumentException(
					"Number of decimals for speed cannot be negative");
		if (numDecimalsForDir < 0)
			throw new IllegalArgumentException(
					"Number of decimals for direction cannot be negative");
		this.paramId = paramId;
		this.period = period;
		decimalFormatSpeed = new DecimalFormat();
		decimalFormatSpeed.setMaximumFractionDigits(numDecimalsForSpeed);
		decimalFormatSpeed.setMinimumFractionDigits(numDecimalsForSpeed);
		decimalFormatSpeed
				.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		decimalFormatSpeed.setDecimalFormatSymbols(new DecimalFormatSymbols(
				Locale.US));
		decimalFormatSpeed.setGroupingUsed(false);
		decimalFormatDir = new DecimalFormat();
		decimalFormatDir.setMaximumFractionDigits(numDecimalsForDir);
		decimalFormatDir.setMinimumFractionDigits(numDecimalsForDir);
		decimalFormatDir
				.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		decimalFormatDir.setDecimalFormatSymbols(new DecimalFormatSymbols(
				Locale.US));
		decimalFormatDir.setGroupingUsed(false);
		decimalFormatPercent = new DecimalFormat();
		decimalFormatPercent.setMaximumFractionDigits(1);
		decimalFormatPercent.setMinimumFractionDigits(1);
		decimalFormatPercent
				.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		decimalFormatPercent.setDecimalFormatSymbols(new DecimalFormatSymbols(
				Locale.US));
		decimalFormatPercent.setGroupingUsed(false);
	}

	List<WindValue> readWindData(Date startTime, boolean startTimeIncluded,
			Date endTime, boolean endTimeIncluded, Integer maxData)
			throws StorageException {
		List<Value> data = readData(startTime, startTimeIncluded, endTime,
				endTimeIncluded, maxData);
		List<WindValue> windData = new ArrayList<WindValue>();
		for (Value value : data)
			windData.add((WindValue) value);
		return windData;
	}

	@Override
	String getFileExtension() {
		return FILE_EXT;
	}

	@Override
	String getFilePrefix() {
		return FILE_PREFIX;
	}

	@Override
	String getDirName() {
		return super.getDirName() + File.separator + FILE_DIR + File.separator
				+ paramId + File.separator + period;
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
		pw.println("analyzer = " + getAnalyzerId());
		if (getAnalyzerName() != null)
			pw.println("analyzer_name = " + getAnalyzerName());
		else
			pw.println("analyzer_name =");
		pw.println("element = " + paramId);
		pw.println("period = " + period);
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
		checkHeaderLine(br, "analyzer", getAnalyzerId().toString());
		getHeaderLineValue(br, "analyzer_name");
		checkHeaderLine(br, "element", paramId);
		checkHeaderLine(br, "period", Integer.toString(period));
		checkHeaderLine(br, "date", dateFmt.format(date));
	}

	@Override
	int getHeaderLineNumber() {
		return 9;
	}

	@Override
	void appendData(PrintWriter pw, List<Value> data) {
		for (Value value : data) {
			WindValue wv = (WindValue) value;
			pw.print(timeFmt.format(wv.getTimestamp()));
			pw.print(",");
			if (wv.getVectorialSpeed() != null)
				pw.print(decimalFormatSpeed.format(wv.getVectorialSpeed()));
			pw.print(",");
			if (wv.getVectorialDirection() != null)
				pw.print(decimalFormatDir.format(wv.getVectorialDirection()));
			pw.print(",");
			if (wv.getStandardDeviation() != null)
				pw.print(decimalFormatSpeed.format(wv.getStandardDeviation()));
			pw.print(",");
			if (wv.getScalarSpeed() != null)
				pw.print(decimalFormatSpeed.format(wv.getScalarSpeed()));
			pw.print(",");
			if (wv.getGustSpeed() != null)
				pw.print(decimalFormatSpeed.format(wv.getGustSpeed()));
			pw.print(",");
			if (wv.getGustDirection() != null)
				pw.print(decimalFormatDir.format(wv.getGustDirection()));
			pw.print(",");
			if (wv.getCalmsNumberPercent() != null)
				pw.print(decimalFormatPercent.format(wv.getCalmsNumberPercent()));
			pw.print(",");
			if (wv.getCalm() != null)
				pw.print(wv.getCalm() ? "1" : "0");
			pw.print(",");
			pw.print(wv.isNotvalid() ? "1" : "0");
			pw.print(",");
			pw.printf("%08X", wv.getFlags());
			pw.println();
		}
	}

	@Override
	WindValue parseDataLine(String strDate, String line) {
		String[] fields = line.split(",", -1);
		if (fields.length != 11)
			return null;
		try {
			String strTimestamp = strDate + TIMESTAMP_SEPARATOR
					+ fields[0].trim();
			String strVectorialSpeed = fields[1].trim();
			String strVectorialDirection = fields[2].trim();
			String strStandardDeviation = fields[3].trim();
			String strScalarSpeed = fields[4].trim();
			String strGustSpeed = fields[5].trim();
			String strGustDirection = fields[6].trim();
			String strCalmsNumberPercent = fields[7].trim();
			String strCalm = fields[8].trim();
			String strNotvalid = fields[9].trim();
			String strFlags = fields[10].trim();
			Double vectorialSpeed = null;
			if (!strVectorialSpeed.isEmpty())
				vectorialSpeed = Double.valueOf(strVectorialSpeed);
			Double vectorialDirection = null;
			if (!strVectorialDirection.isEmpty())
				vectorialDirection = Double.valueOf(strVectorialDirection);
			Double standardDeviation = null;
			if (!strStandardDeviation.isEmpty())
				standardDeviation = Double.valueOf(strStandardDeviation);
			Double scalarSpeed = null;
			if (!strScalarSpeed.isEmpty())
				scalarSpeed = Double.valueOf(strScalarSpeed);
			Double gustSpeed = null;
			if (!strGustSpeed.isEmpty())
				gustSpeed = Double.valueOf(strGustSpeed);
			Double gustDirection = null;
			if (!strGustDirection.isEmpty())
				gustDirection = Double.valueOf(strGustDirection);
			Double calmsNumberPercent = null;
			if (!strCalmsNumberPercent.isEmpty())
				calmsNumberPercent = Double.valueOf(strCalmsNumberPercent);
			Boolean calm = null;
			if (!strCalm.isEmpty()) {
				if ("1".equals(strCalm))
					calm = true;
				else if ("0".equals(strCalm))
					calm = false;
				else
					return null;
			}
			boolean notvalid;
			if ("1".equals(strNotvalid))
				notvalid = true;
			else if ("0".equals(strNotvalid))
				notvalid = false;
			else
				return null;
			if (strFlags.length() != 8)
				return null;
			return new WindValue(timestampFmt.parse(strTimestamp),
					vectorialSpeed, vectorialDirection, standardDeviation,
					scalarSpeed, gustSpeed, gustDirection, calmsNumberPercent,
					calm, notvalid, Integer.valueOf(strFlags, 16));
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
		if (fields.length != 11)
			return null;
		try {
			String strTimestamp = strDate + TIMESTAMP_SEPARATOR
					+ fields[0].trim();
			return timestampFmt.parse(strTimestamp);
		} catch (ParseException e) {
			return null;
		}
	}

	static List<WindDataStorage> listStorages() {
		List<WindDataStorage> list = new ArrayList<WindDataStorage>();
		for (UUID analyzer : listAnalyzers()) {
			File dir = new File(DATA_DIR + File.separator + DIR_ANALYZERS
					+ File.separator + analyzer + File.separator + FILE_DIR);
			if (!dir.isDirectory())
				continue;
			File[] paramdirs = dir.listFiles();
			if (paramdirs == null)
				continue;
			for (int i = 0; i < paramdirs.length; i++) {
				if (!paramdirs[i].isDirectory())
					continue;
				File[] aggdirs = paramdirs[i]
						.listFiles(new IntegerFileFilter());
				if (aggdirs == null)
					continue;
				for (int j = 0; j < aggdirs.length; j++) {
					Integer aggTime;
					try {
						aggTime = Integer.parseInt(aggdirs[j].getName());
					} catch (NumberFormatException nfe) {
						continue;
					}
					if (!aggdirs[j].isDirectory())
						continue;
					list.add(new WindDataStorage(null, null, analyzer, null,
							paramdirs[i].getName(), 0, 0, aggTime));
				}
			}
		}
		return list;
	}
}
