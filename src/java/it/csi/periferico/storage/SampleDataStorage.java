/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file based data storage for sample data
// Change log:
//   2008-06-20: initial version
// ----------------------------------------------------------------------------
// $Id: SampleDataStorage.java,v 1.16 2011/10/19 10:58:51 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.Periferico;
import it.csi.periferico.core.Sample;
import it.csi.periferico.core.Value;

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

import org.apache.log4j.Logger;

/**
 * File based data storage for sample data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
class SampleDataStorage extends AnalyzerDataStorage {

	static final String FILE_PREFIX = "s_";

	static final String FILE_EXT = ".txt";

	static final String FILE_DIR = "samples";

	private static Logger logger = Logger.getLogger("periferico."
			+ SampleDataStorage.class.getSimpleName());

	private String paramId;

	private DecimalFormat decimalFormat;

	SampleDataStorage(UUID stationId, String stationName, UUID analyzerId,
			String analyzerName, String paramId, int numDecimals) {
		super(NewFileRate.DAILY, stationId, stationName, analyzerId,
				analyzerName);
		if (paramId == null)
			throw new IllegalArgumentException("Parameter ID cannot be null");
		if (numDecimals < 0)
			throw new IllegalArgumentException(
					"Number of decimals cannot be negative");
		this.paramId = paramId;
		decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(numDecimals);
		decimalFormat.setMinimumFractionDigits(numDecimals);
		decimalFormat.setRoundingMode(Periferico.ROUNDING_MODE_FOR_SAMPLE_DATA);
		decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(
				Locale.US));
		decimalFormat.setGroupingUsed(false);
	}

	List<Sample> readSampleData(Date startTime, boolean startTimeIncluded,
			Date endTime, boolean endTimeIncluded, Integer maxData)
			throws StorageException {
		List<Value> data = readData(startTime, startTimeIncluded, endTime,
				endTimeIncluded, maxData);
		List<Sample> sampleData = new ArrayList<Sample>();
		for (Value value : data)
			sampleData.add((Sample) value);
		return sampleData;
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
				+ paramId;
	}

	@Override
	void writeHeader(PrintWriter pw, Date date) {
		pw.println("id = SAMPLE");
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
		pw.println("date = " + dateFmt.format(date));
	}

	@Override
	void checkHeader(BufferedReader br, Date date) throws IOException,
			HeaderException {
		checkHeaderLine(br, "id", "SAMPLE");
		checkHeaderLine(br, "version", "1.0");
		if (getStationId() != null)
			checkHeaderLine(br, "station", getStationId().toString());
		else
			getHeaderLineValue(br, "station");
		getHeaderLineValue(br, "station_name");
		checkHeaderLine(br, "analyzer", getAnalyzerId().toString());
		getHeaderLineValue(br, "analyzer_name");
		checkHeaderLine(br, "element", paramId);
		checkHeaderLine(br, "date", dateFmt.format(date));
	}

	@Override
	int getHeaderLineNumber() {
		return 8;
	}

	@Override
	void appendData(PrintWriter pw, List<Value> data) {
		for (Value value : data) {
			Sample sample = (Sample) value;
			pw.print(timeFmt.format(sample.getTimestamp()));
			pw.print(",");
			if (sample.getValue() != null)
				pw.print(decimalFormat.format(sample.getValue()));
			pw.print(",");
			pw.print(sample.isNotvalid() ? "1" : "0");
			pw.print(",");
			pw.printf("%08X", sample.getFlags());
			pw.println();
		}
	}

	@Override
	Sample parseDataLine(String strDate, String line) {
		String[] fields = line.split(",", -1);
		if (fields.length != 4)
			return null;
		try {
			String strTimestamp = strDate + TIMESTAMP_SEPARATOR
					+ fields[0].trim();
			String strValue = fields[1].trim();
			String strNotvalid = fields[2].trim();
			Double value = null;
			if (!strValue.isEmpty())
				value = Double.valueOf(strValue);
			boolean notvalid;
			if ("1".equals(strNotvalid))
				notvalid = true;
			else if ("0".equals(strNotvalid))
				notvalid = false;
			else
				return null;
			String strFlags = fields[3].trim();
			if (strFlags.length() != 8)
				return null;
			return new Sample(timestampFmt.parse(strTimestamp), value,
					notvalid, Integer.valueOf(strFlags, 16));
		} catch (NoSuchElementException ex) {
			return null;
		} catch (NumberFormatException e) {
			return null;
		} catch (ParseException e) {
			return null;
		}
	}

	// NOTE: this function replaces DataStorage.parseData for SampleDataStorage
	// files: it is about 10 times faster, but makes heavy assumptions on the
	// TIME_FORMAT_STR ...
	@Override
	int parseData(List<Value> data, BufferedReader br, Date intervalStart,
			boolean intervalStartIncluded, Date intervalEnd,
			boolean intervalEndIncluded, Integer maxData) throws IOException {
		final int timeFmtLength = TIME_FORMAT_STR.length();
		int iIntervalStart = Integer.parseInt(timeFmt.format(intervalStart));
		String strDate = dateFmt.format(intervalStart);
		int iPrevValTs = -1;
		int iValTs = -1;
		int unparsableLines = 0;
		String line = null;
		while ((line = readLine(br)) != null) {
			try {
				iValTs = Integer.parseInt(line.substring(0, timeFmtLength));
			} catch (Exception e) {
				unparsableLines++;
				continue;
			}
			if (iPrevValTs != -1 && iValTs <= iPrevValTs) {
				logger.warn("Datum timestamp out of sequence in file "
						+ getAbsoluteFilePath(intervalStart) + ": " + line);
				continue;
			}
			iPrevValTs = iValTs;
			if (iValTs < iIntervalStart)
				continue;
			if (iValTs <= iIntervalStart && !intervalStartIncluded)
				continue;
			Value value = parseDataLine(strDate, line);
			if (value == null) {
				unparsableLines++;
				continue;
			}
			Date valueTs = value.getTimestamp();
			if (valueTs.equals(intervalEnd) && !intervalEndIncluded)
				break;
			if (valueTs.after(intervalEnd))
				break;
			if (maxData != null && data.size() >= maxData)
				break;
			data.add(value);
		}
		return unparsableLines;
	}

	// NOTE: this function replaces DataStorage.hasTimestampsInTheFuture
	// for SampleDataStorage files: it is about 10 times faster, but makes
	// heavy assumptions on the TIME_FORMAT_STR ...
	@Override
	boolean hasTimestampsInTheFuture(BufferedReader br, Date currentDate)
			throws IOException {
		final int timeFmtLength = TIME_FORMAT_STR.length();
		String strDate = timeFmt.format(currentDate);
		int currentDateTs = Integer.parseInt(strDate);
		int iValTs = -1;
		String line = null;
		while ((line = readLine(br)) != null) {
			try {
				iValTs = Integer.parseInt(line.substring(0, timeFmtLength));
			} catch (Exception e) {
				continue;
			}
			if (iValTs > currentDateTs)
				return true;
		}
		return false;
	}

	// NOTE: this function is needed because DataStorage.parseTimestamp
	// is abstract, but it is not used because the hasTimestampsInTheFuture
	// implemented in this class does not call it
	@Override
	Date parseTimestamp(String strDate, String line) {
		String[] fields = line.split(",", -1);
		if (fields.length != 4)
			return null;
		try {
			String strTimestamp = strDate + TIMESTAMP_SEPARATOR
					+ fields[0].trim();
			return timestampFmt.parse(strTimestamp);
		} catch (ParseException e) {
			return null;
		}
	}

	static List<SampleDataStorage> listStorages() {
		List<SampleDataStorage> list = new ArrayList<SampleDataStorage>();
		for (UUID analyzer : listAnalyzers()) {
			File dir = new File(DATA_DIR + File.separator + DIR_ANALYZERS
					+ File.separator + analyzer + File.separator + FILE_DIR);
			if (dir.isDirectory()) {
				File[] subdirs = dir.listFiles();
				if (subdirs != null)
					for (int i = 0; i < subdirs.length; i++)
						if (subdirs[i].isDirectory())
							list.add(new SampleDataStorage(null, null,
									analyzer, null, subdirs[i].getName(), 0));
			}
		}
		return list;
	}

}
