/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for file based data storage
// Change log:
//   2008-06-20: initial version
// ----------------------------------------------------------------------------
// $Id: DataStorage.java,v 1.24 2013/10/09 10:22:03 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.core.Value;
import it.csi.periferico.storage.DateFileFilter.TimeConstraint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Base class for file based data storage
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
abstract class DataStorage {

	static final String DATA_DIR = "data";

	static final String TIMESTAMP_SEPARATOR = " ";

	static final String DATE_FORMAT_STR = "yyyyMMdd";

	// ATTENTION: before modifying TIME_FORMAT_STR look at
	// SampleDataStorage.parseData() and
	// SampleDataStorage.hasTimestampsInTheFuture()
	static final String TIME_FORMAT_STR = "HHmmss";

	static final String MS_FORMAT_STR = "SSS";

	private static final String DAILY_FNAME_FORMAT_STR = "yyyyMMdd";

	private static final String MONTHLY_FNAME_FORMAT_STR = "yyyyMM";

	enum NewFileRate {
		DAILY, MONTHLY
	};

	final DateFormat dateFmt = new SimpleDateFormat(DATE_FORMAT_STR);

	final DateFormat timeFmt = new SimpleDateFormat(TIME_FORMAT_STR);

	final DateFormat time_msFmt = new SimpleDateFormat(TIME_FORMAT_STR
			+ TIMESTAMP_SEPARATOR + MS_FORMAT_STR);

	final DateFormat timestampFmt = new SimpleDateFormat(DATE_FORMAT_STR
			+ TIMESTAMP_SEPARATOR + TIME_FORMAT_STR);

	final DateFormat timestamp_msFmt = new SimpleDateFormat(DATE_FORMAT_STR
			+ TIMESTAMP_SEPARATOR + TIME_FORMAT_STR + TIMESTAMP_SEPARATOR
			+ MS_FORMAT_STR);

	private static Logger logger = Logger.getLogger("periferico."
			+ DataStorage.class.getSimpleName());

	private UUID stationId;

	private String stationName;

	private NewFileRate newFileRate;

	private DateFormat filenameDateFmt;

	DataStorage(NewFileRate newFileRate, UUID stationId, String stationName) {
		this.newFileRate = newFileRate;
		this.stationId = stationId;
		this.stationName = stationName;
		if (newFileRate == NewFileRate.DAILY)
			filenameDateFmt = new SimpleDateFormat(DAILY_FNAME_FORMAT_STR);
		else
			filenameDateFmt = new SimpleDateFormat(MONTHLY_FNAME_FORMAT_STR);
	}

	void writeData(List<? extends Value> data) throws StorageException {
		if (stationId == null)
			throw new IllegalStateException("Station ID cannot be null");
		if (data == null)
			throw new IllegalArgumentException("Data list cannot be null");
		if (data.size() == 0)
			return;
		makeDir();
		List<List<Value>> listPeriodData = new ArrayList<List<Value>>();
		List<Value> periodData = new ArrayList<Value>();
		listPeriodData.add(periodData);
		Calendar periodCal = Calendar.getInstance();
		periodCal.setTime(data.get(0).getTimestamp());
		Calendar currentCal = Calendar.getInstance();
		for (Value value : data) {
			currentCal.setTime(value.getTimestamp());
			if (!isSamePeriod(periodCal, currentCal)) {
				periodCal.setTime(value.getTimestamp());
				periodData = new ArrayList<Value>();
				listPeriodData.add(periodData);
			}
			periodData.add(value);
		}
		for (List<Value> currList : listPeriodData) {
			Date fileDate = currList.get(0).getTimestamp();
			String fileName = getFilePrefix()
					+ filenameDateFmt.format(fileDate) + getFileExtension();
			File file = new File(getDirName() + File.separator + fileName);
			logger.debug("Data write request for file: " + file);
			if (!file.exists())
				createNewFile(file, fileDate);
			writeFile(file, currList);
		}
	}

	List<Value> readData(Date startTime, boolean startTimeIncluded,
			Date endTime, boolean endTimeIncluded, Integer maxData)
			throws StorageException {
		if (stationId == null)
			throw new IllegalStateException("Station ID cannot be null");
		if (startTime == null)
			throw new IllegalArgumentException("Start time must not be null");
		if (endTime == null)
			throw new IllegalArgumentException("End time must not be null");
		if (startTime.after(endTime)
				|| (startTime.equals(endTime) && (!startTimeIncluded || !endTimeIncluded)))
			throw new IllegalArgumentException("Empty time interval");
		List<Value> data = new ArrayList<Value>();
		Calendar calendar = new GregorianCalendar();
		boolean firstInterval = true;
		boolean lastInterval = false;
		Date intervalStart = startTime;
		while (!lastInterval) {
			if (maxData != null && data.size() >= maxData)
				break;
			calendar.setTime(intervalStart);
			if (newFileRate == NewFileRate.MONTHLY)
				calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			if (newFileRate == NewFileRate.DAILY)
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			else
				// NewFileRate.MONTHLY
				calendar.add(Calendar.MONTH, 1);
			Date intervalEnd = calendar.getTime();
			if (intervalEnd.after(endTime)) {
				intervalEnd = endTime;
				lastInterval = true;
			}
			if (!(intervalStart.equals(intervalEnd) && ((firstInterval && !startTimeIncluded) || (lastInterval && !endTimeIncluded)))) {
				String fileName = getFilePrefix()
						+ filenameDateFmt.format(intervalStart)
						+ getFileExtension();
				File file = new File(getDirName() + File.separator + fileName);
				if (file.exists()) {
					logger.debug("Data read request for file: " + file);
					BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(file));
						checkHeader(br, intervalStart);
						boolean intervalStartIncluded = firstInterval ? startTimeIncluded
								: true;
						boolean intervalEndIncluded = lastInterval ? endTimeIncluded
								: false;
						int corruptedRecords = parseData(data, br,
								intervalStart, intervalStartIncluded,
								intervalEnd, intervalEndIncluded, maxData);
						if (corruptedRecords > 0)
							logger.error("Found " + corruptedRecords
									+ " corrupted records in file: " + file);
						// TODO: it may be useful adding further reports about
						// corrupted records
						// TODO: it may happen that the last datum read is
						// corrupted because of an append operation is in
						// progress in the same file: this is not a true error
						// and should not be reported
					} catch (FileNotFoundException e) {
						logger.error("File not found: " + file, e);
						throw new StorageException("file_not_found_error",
								file.getPath());
					} catch (IOException e) {
						logger.error("IO error reading file: " + file, e);
						throw new StorageException("io_error", file.getPath());
					} catch (HeaderException e) {
						logger.error("Invalid header for file: " + file, e);
						throw new StorageException("invalid_header",
								file.getPath());
					} finally {
						if (br != null)
							try {
								br.close();
							} catch (IOException e) {
								logger.error("Error closing file: " + file);
							}
					}
				} else {
					logger.debug("Data read request from inexistent file: "
							+ file);
				}
			}
			intervalStart = intervalEnd;
			firstInterval = false;
		}
		return data;
	}

	int parseData(List<Value> data, BufferedReader br, Date intervalStart,
			boolean intervalStartIncluded, Date intervalEnd,
			boolean intervalEndIncluded, Integer maxData) throws IOException {
		int unparsableLines = 0;
		String strDate = dateFmt.format(intervalStart);
		Date prevValueTs = null;
		String line;
		while ((line = readLine(br)) != null) {
			Value value = parseDataLine(strDate, line);
			if (value == null) {
				unparsableLines++;
				continue;
			}
			Date valueTs = value.getTimestamp();
			if (prevValueTs != null && !valueTs.after(prevValueTs)) {
				logger.warn("Datum timestamp out of sequence in file "
						+ getAbsoluteFilePath(valueTs) + ": " + line);
				continue;
			}
			prevValueTs = valueTs;
			if (valueTs.before(intervalStart))
				continue;
			if (valueTs.equals(intervalStart) && !intervalStartIncluded)
				continue;
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

	String getAbsoluteFilePath(Date date) {
		return getDirName() + File.separator + getFilePrefix()
				+ filenameDateFmt.format(date) + getFileExtension();
	}

	abstract String getFileExtension();

	abstract String getFilePrefix();

	abstract void writeHeader(PrintWriter pw, Date date);

	abstract void appendData(PrintWriter pw, List<Value> data);

	abstract void checkHeader(BufferedReader br, Date date) throws IOException,
			HeaderException;

	abstract int getHeaderLineNumber();

	abstract Value parseDataLine(String strDate, String line);

	abstract Date parseTimestamp(String strDate, String line);

	String getDirName() {
		return DATA_DIR;
	}

	void makeDir() throws StorageException {
		File dir = new File(getDirName());
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				logger.error("Cannot create data directory: " + dir);
				throw new StorageException("directory_creation_error",
						dir.getPath());
			}
			logger.info("Created data directory: " + dir);
		}
	}

	UUID getStationId() {
		return stationId;
	}

	String getStationName() {
		return stationName;
	}

	void checkHeaderLine(BufferedReader br, String key, String value)
			throws IOException, HeaderException {
		String headerValue = getHeaderLineValue(br, key);
		if (value == null && headerValue.isEmpty())
			return;
		if (!headerValue.equalsIgnoreCase(value))
			throw new HeaderException("Value mismatch (expected: " + value
					+ ", found: " + headerValue + ")");
	}

	String getHeaderLineValue(BufferedReader br, String key)
			throws IOException, HeaderException {
		String headerLine = readLine(br);
		if (headerLine == null)
			throw new HeaderException("Premature end of file");
		int equalIndex = headerLine.indexOf('=');
		if (equalIndex < 0)
			throw new HeaderException("Line format error");
		String headerKey = headerLine.substring(0, equalIndex).trim();
		if (!headerKey.equalsIgnoreCase(key))
			throw new HeaderException("Key mismatch (expected: " + key
					+ ", found: " + headerKey + ")");
		String headerValue = headerLine.substring(equalIndex + 1).trim();
		return headerValue;
	}

	String readLine(BufferedReader br) throws IOException {
		do {
			String line = br.readLine();
			if (line == null)
				return null;
			line = line.trim();
			if (!line.isEmpty())
				return line;
		} while (true);
	}

	boolean findAndDeleteDataInTheFuture(Date currentDate, boolean findOnly)
			throws StorageException {
		if (currentDate == null)
			throw new IllegalArgumentException(
					"Current date should not be null");
		boolean dataInTheFuture = false;
		logger.debug("Searching data files in the future in folder: "
				+ getDirName());
		String fnameFormatStr = (newFileRate == NewFileRate.DAILY) ? DAILY_FNAME_FORMAT_STR
				: MONTHLY_FNAME_FORMAT_STR;
		dataInTheFuture |= findAndDeleteDataFilesInTheFuture(currentDate,
				findOnly, getDirName(), getFilePrefix(), fnameFormatStr,
				getFileExtension());
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(currentDate);
		if (newFileRate == NewFileRate.MONTHLY)
			calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date fileStartDate = calendar.getTime();
		String fileName = getFilePrefix()
				+ filenameDateFmt.format(fileStartDate) + getFileExtension();
		File file = new File(getDirName() + File.separator + fileName);
		if (file.exists()) {
			logger.debug("Searching data in the future in file: " + file);
			boolean result = false;
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
				checkHeader(br, fileStartDate);
				result = hasTimestampsInTheFuture(br, currentDate);
				if (result)
					logger.warn("Data in the future found in file: " + file);
				dataInTheFuture |= result;
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
			if (result && !findOnly) {
				deleteDataInTheFuture(file, currentDate);
			}
		} else {
			logger.debug("Searching data in the future, "
					+ "skipped inexistent file: " + file);
		}
		return dataInTheFuture;
	}

	boolean hasTimestampsInTheFuture(BufferedReader br, Date currentDate)
			throws IOException {
		String strDate = dateFmt.format(currentDate);
		String line;
		while ((line = readLine(br)) != null) {
			Date timestamp = parseTimestamp(strDate, line);
			if (timestamp == null) {
				continue;
			}
			if (timestamp.after(currentDate))
				return true;
		}
		return false;
	}

	static void deleteOldFiles(int maxDaysOfData, int maxDaysOfAggregateData) {
		FilenameFilter uuidFileFilter = new UUIDFilenameFilter();
		// Delete old files of the analyzers ...
		File[] analyzerDirs = getFiles(DATA_DIR + File.separator
				+ AnalyzerDataStorage.DIR_ANALYZERS, uuidFileFilter);
		for (int i = 0; i < analyzerDirs.length; i++) {
			if (!analyzerDirs[i].isDirectory())
				continue;
			// Delete old event files ...
			File eventsDir = new File(analyzerDirs[i].getPath()
					+ File.separator + EventDataStorage.FILE_DIR);
			OldFileFilter eventsFilter = new OldFileFilter(
					EventDataStorage.FILE_PREFIX, MONTHLY_FNAME_FORMAT_STR,
					EventDataStorage.FILE_EXT, 31, maxDaysOfData);
			deleteOldFilesGivenTypeOfData(eventsDir, eventsFilter, false);
			// Delete old sample files ...
			File samplesDir = new File(analyzerDirs[i].getPath()
					+ File.separator + SampleDataStorage.FILE_DIR);
			OldFileFilter samplesFilter = new OldFileFilter(
					SampleDataStorage.FILE_PREFIX, DAILY_FNAME_FORMAT_STR,
					SampleDataStorage.FILE_EXT, 1, maxDaysOfData);
			deleteOldFilesGivenTypeOfData(samplesDir, samplesFilter, false);
			// Delete old mean files ...
			File meansDir = new File(analyzerDirs[i].getPath() + File.separator
					+ MeanDataStorage.FILE_DIR);
			OldFileFilter meansFilter = new OldFileFilter(
					MeanDataStorage.FILE_PREFIX, DAILY_FNAME_FORMAT_STR,
					MeanDataStorage.FILE_EXT, 1, maxDaysOfAggregateData);
			deleteOldFilesGivenTypeOfData(meansDir, meansFilter, true);
			// Delete old total files ...
			File totalsDir = new File(analyzerDirs[i].getPath()
					+ File.separator + MeanDataStorage.FILE_DIR);
			OldFileFilter totalsFilter = new OldFileFilter(
					TotalDataStorage.FILE_PREFIX, DAILY_FNAME_FORMAT_STR,
					TotalDataStorage.FILE_EXT, 1, maxDaysOfAggregateData);
			deleteOldFilesGivenTypeOfData(totalsDir, totalsFilter, true);
			// Delete old wind files ...
			File windsDir = new File(analyzerDirs[i].getPath() + File.separator
					+ WindDataStorage.FILE_DIR);
			OldFileFilter windsFilter = new OldFileFilter(
					WindDataStorage.FILE_PREFIX, DAILY_FNAME_FORMAT_STR,
					WindDataStorage.FILE_EXT, 1, maxDaysOfAggregateData);
			deleteOldFilesGivenTypeOfData(windsDir, windsFilter, true);
		}
		// Delete old files of the container alarms ...
		File[] alarmDirs = getFiles(DATA_DIR + File.separator
				+ ContainerAlarmDataStorage.DIR_CONTAINER_ALARMS,
				uuidFileFilter);
		for (int i = 0; i < alarmDirs.length; i++) {
			if (!alarmDirs[i].isDirectory())
				continue;
			// Delete old event files ...
			OldFileFilter eventsFilter = new OldFileFilter(
					DigitalCADataStorage.FILE_PREFIX, MONTHLY_FNAME_FORMAT_STR,
					DigitalCADataStorage.FILE_EXT, 31, maxDaysOfData);
			deleteOldFilesGivenTypeOfData(alarmDirs[i], eventsFilter, false);
			eventsFilter = new OldFileFilter(TriggerCADataStorage.FILE_PREFIX,
					MONTHLY_FNAME_FORMAT_STR, TriggerCADataStorage.FILE_EXT,
					31, maxDaysOfData);
			deleteOldFilesGivenTypeOfData(alarmDirs[i], eventsFilter, false);
		}
		// Delete old GPS files
		File gpsDir = new File(DATA_DIR + File.separator
				+ GpsDataStorage.DIR_GPSDATA);
		if (gpsDir.isDirectory()) {
			OldFileFilter gpsFilter = new OldFileFilter(
					GpsDataStorage.FILE_PREFIX, DAILY_FNAME_FORMAT_STR,
					GpsDataStorage.FILE_EXT, 1, maxDaysOfData);
			deleteOldFilesGivenDataDir(gpsDir, gpsFilter);
		}
	}

	static boolean findAndDeleteAllDataInTheFuture(Date currentDate,
			boolean findOnly) {
		boolean dataFound = false;
		logger.info("Checking for data in the future...");
		for (DataStorage ds : listAllStorages())
			try {
				dataFound |= ds.findAndDeleteDataInTheFuture(currentDate,
						findOnly);
			} catch (StorageException e) {
				logger.error("Error checking for data in the future", e);
			}
		if (dataFound)
			logger.warn("Found data in the future");
		else
			logger.info("No data in the future found");
		return dataFound;
	}

	static List<DataStorage> listAllStorages() {
		List<DataStorage> list = new ArrayList<DataStorage>();
		list.addAll(DigitalCADataStorage.listStorages());
		list.addAll(EventDataStorage.listStorages());
		list.addAll(GpsDataStorage.listStorages());
		list.addAll(MeanDataStorage.listStorages());
		list.addAll(SampleDataStorage.listStorages());
		list.addAll(TotalDataStorage.listStorages());
		list.addAll(TriggerCADataStorage.listStorages());
		list.addAll(WindDataStorage.listStorages());
		return list;
	}

	private static File[] getFiles(String dirName, FilenameFilter filter) {
		return getFiles(new File(dirName), filter);
	}

	private static File[] getFiles(File dir, FilenameFilter filter) {
		if (dir.isDirectory()) {
			File[] subdirs = dir.listFiles(filter);
			if (subdirs != null)
				return subdirs;
		}
		return new File[0];
	}

	private static void deleteOldFilesGivenTypeOfData(File dirOfTypeOfData,
			FilenameFilter oldFileFilter, boolean hasAggregationSubirs) {
		File[] dataDirs = getFiles(dirOfTypeOfData, null);
		for (int i = 0; i < dataDirs.length; i++) {
			if (!dataDirs[i].isDirectory())
				continue;
			if (hasAggregationSubirs) {
				FilenameFilter intFileFilter = new IntegerFileFilter();
				File[] aggregationDirs = dataDirs[i].listFiles(intFileFilter);
				if (aggregationDirs == null)
					continue;
				for (int j = 0; j < aggregationDirs.length; j++) {
					if (!aggregationDirs[j].isDirectory())
						continue;
					deleteOldFilesGivenDataDir(aggregationDirs[j],
							oldFileFilter);
				}
			} else {
				deleteOldFilesGivenDataDir(dataDirs[i], oldFileFilter);
			}
		}
	}

	private static void deleteOldFilesGivenDataDir(File dataDir,
			FilenameFilter oldFileFilter) {
		File[] dataFiles = getFiles(dataDir, oldFileFilter);
		for (int i = 0; i < dataFiles.length; i++) {
			if (!dataFiles[i].isFile())
				continue;
			logger.debug("Deleting old file: " + dataFiles[i].getAbsolutePath());
			if (!dataFiles[i].delete()) {
				logger.error("Cannot delete old file: "
						+ dataFiles[i].getAbsolutePath());
			}
		}
	}

	private boolean findAndDeleteDataFilesInTheFuture(Date currentDate,
			boolean findOnly, String dataDir, String filePrefix,
			String fileDateFormat, String fileExtension) {
		DateFileFilter dateFilterFuture = new DateFileFilter(filePrefix,
				fileDateFormat, fileExtension, currentDate,
				TimeConstraint.FUTURE);
		boolean dataFound = false;
		File[] dataFiles = getFiles(dataDir, dateFilterFuture);
		for (int i = 0; i < dataFiles.length; i++) {
			if (!dataFiles[i].isFile())
				continue;
			dataFound |= true;
			if (findOnly) {
				logger.warn("Found data file in the future: "
						+ dataFiles[i].getAbsolutePath());
				continue;
			}
			logger.warn("Deleting data file in the future: "
					+ dataFiles[i].getAbsolutePath());
			if (!dataFiles[i].delete()) {
				logger.error("Cannot delete data file in the future: "
						+ dataFiles[i].getAbsolutePath());
			}
		}
		return dataFound;
	}

	private void deleteDataInTheFuture(File inFile, Date currentDate) {
		File outFile = new File(inFile.getPath() + "_");
		BufferedReader br = null;
		PrintWriter pw = null;
		final String errMsg = "Error deleting data in the future from: ";
		logger.warn("Deleting data in the future in file: " + inFile);
		try {
			br = new BufferedReader(new FileReader(inFile));
			pw = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
			for (int i = 0; i < getHeaderLineNumber(); i++)
				pw.println(br.readLine());
			String strDate = dateFmt.format(currentDate);
			String line;
			while ((line = readLine(br)) != null) {
				Date timestamp = parseTimestamp(strDate, line);
				if (timestamp != null && timestamp.after(currentDate)) {
					continue;
				}
				pw.println(line);
			}
			br.close();
			br = null;
			pw.close();
			pw = null;
			if (inFile.delete()) {
				if (!outFile.renameTo(inFile))
					logger.error(errMsg + inFile + " (rename failed)");
			} else {
				logger.error(errMsg + inFile + " (delete failed)");
			}
		} catch (FileNotFoundException e) {
			logger.error(errMsg + inFile, e);
		} catch (IOException e) {
			logger.error(errMsg + inFile, e);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
			if (pw != null)
				pw.close();
		}
	}

	private void createNewFile(File file, Date fileDate)
			throws StorageException {
		FileWriter fw;
		try {
			fw = new FileWriter(file, false);
		} catch (IOException e) {
			logger.error("Cannot create file: " + file, e);
			throw new StorageException("file_creation_error", file.getPath());
		}
		PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
		writeHeader(pw, fileDate);
		boolean error = pw.checkError();
		pw.close();
		if (error) {
			logger.error("Cannot write header to file: " + file);
			throw new StorageException("file_creation_error", file.getPath());
		}
		logger.info("Created data file: " + file);
	}

	private void writeFile(File file, List<Value> listValues)
			throws StorageException {
		FileWriter fw;
		try {
			fw = new FileWriter(file, true);
		} catch (IOException e) {
			logger.error("Cannot open file: " + file, e);
			throw new StorageException("file_write_error", file.getPath());
		}
		PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
		appendData(pw, listValues);
		boolean error = pw.checkError();
		pw.close();
		if (error) {
			logger.error("Cannot write data to file: " + file);
			throw new StorageException("file_write_error", file.getPath());
		}

	}

	private boolean isSamePeriod(Calendar cal1, Calendar cal2) {
		if (newFileRate == NewFileRate.DAILY)
			return isSameDay(cal1, cal2);
		// NewFileRate.MONTHLY
		return isSameMonth(cal1, cal2);
	}

	private boolean isSameDay(Calendar cal1, Calendar cal2) {
		return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2
						.get(Calendar.DAY_OF_YEAR);
	}

	private boolean isSameMonth(Calendar cal1, Calendar cal2) {
		return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}

}
