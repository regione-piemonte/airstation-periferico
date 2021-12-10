/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file based data storage for scalar aggregate data
// Change log:
//   2008-08-06: initial version
// ----------------------------------------------------------------------------
// $Id: ScalarDataStorage.java,v 1.8 2011/10/19 10:58:51 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.Periferico;
import it.csi.periferico.core.ScalarAggregateValue;
import it.csi.periferico.core.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * File based data storage for scalar aggregate data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ScalarDataStorage extends AnalyzerDataStorage {

	static final String FILE_EXT = ".txt";

	private String paramId;

	private int period;

	private DecimalFormat decimalFormat;

	public ScalarDataStorage(UUID stationId, String stationName,
			UUID analyzerId, String analyzerName, String paramId,
			int numDecimals, int period) {
		super(NewFileRate.DAILY, stationId, stationName, analyzerId,
				analyzerName);
		if (paramId == null)
			throw new IllegalArgumentException("Parameter ID cannot be null");
		if (numDecimals < 0)
			throw new IllegalArgumentException(
					"Number of decimals cannot be negative");
		this.paramId = paramId;
		this.period = period;
		decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(numDecimals);
		decimalFormat.setMinimumFractionDigits(numDecimals);
		decimalFormat
				.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(
				Locale.US));
		decimalFormat.setGroupingUsed(false);
	}

	abstract String getFileType();

	abstract String getFileDir();

	abstract ScalarAggregateValue newAggregateValue(Date timestamp,
			Double value, boolean notvalid, int flags);

	@Override
	String getFileExtension() {
		return FILE_EXT;
	}

	@Override
	String getDirName() {
		return super.getDirName() + File.separator + getFileDir()
				+ File.separator + paramId + File.separator + period;
	}

	@Override
	void writeHeader(PrintWriter pw, Date date) {
		pw.println("id = " + getFileType());
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
		checkHeaderLine(br, "id", getFileType());
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
			ScalarAggregateValue sav = (ScalarAggregateValue) value;
			pw.print(timeFmt.format(sav.getTimestamp()));
			pw.print(",");
			if (sav.getValue() != null)
				pw.print(decimalFormat.format(sav.getValue()));
			pw.print(",");
			pw.print(sav.isNotvalid() ? "1" : "0");
			pw.print(",");
			pw.printf("%08X", sav.getFlags());
			pw.println();
		}
	}

	@Override
	ScalarAggregateValue parseDataLine(String strDate, String line) {
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
			return newAggregateValue(timestampFmt.parse(strTimestamp), value,
					notvalid, Integer.valueOf(strFlags, 16));
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

}
