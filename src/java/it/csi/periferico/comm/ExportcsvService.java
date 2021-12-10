/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: service for csv data export
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: ExportcsvService.java,v 1.27 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import it.csi.periferico.Periferico;
import it.csi.periferico.PerifericoException;
import it.csi.periferico.PerifericoUtil;
import it.csi.periferico.PropertyUtil;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.Parameter;
import it.csi.periferico.core.Analyzer;
import it.csi.periferico.core.Analyzer.Type;
import it.csi.periferico.core.AvgAnalyzer;
import it.csi.periferico.core.CounterElement;
import it.csi.periferico.core.DataPortAnalyzer;
import it.csi.periferico.core.GenericSampleElement;
import it.csi.periferico.core.MeanValue;
import it.csi.periferico.core.RainAnalyzer;
import it.csi.periferico.core.Sample;
import it.csi.periferico.core.SampleAnalyzer;
import it.csi.periferico.core.ScalarElement;
import it.csi.periferico.core.Station;
import it.csi.periferico.core.TotalValue;
import it.csi.periferico.core.WindAnalyzer;
import it.csi.periferico.core.WindElement;
import it.csi.periferico.core.WindValue;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.MissingResourceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service for csv data export
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */

public class ExportcsvService extends HttpServlet {

	private static final long serialVersionUID = -983158514063420000L;

	private Periferico perifericoApp;

	private StorageManager storageManager;

	private String locale = null;

	private PropertyUtil propertyUtil;

	private static final String FIELD_SEPARATOR = ",";

	private static final String DECIMAL_SEPARATOR = ".";

	private static final String ONE_DAY = "ONE_DAY";

	private static final String ONE_MONTH = "ONE_MONTH";

	private static final String ONE_YEAR = "ONE_YEAR";

	private static final String HALF_DAY = "HALF_DAY";

	public ExportcsvService() {
		perifericoApp = Periferico.getInstance();
		storageManager = perifericoApp.getStorageManager();
		propertyUtil = new PropertyUtil("it/csi/periferico/MessageBundleCore");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			String function = request.getParameter("function");
			if (function == null) {
				throw new PerifericoException("no_function_specified");
			}
			locale = request.getParameter("locale");
			if ("getRealTimeData".equals(function))
				getRealTimeData(request, response);
			else if ("getMeansData".equals(function))
				getMeansData(request, response);
			else {
				throw new PerifericoException("unknown_function");
			}
		} catch (PerifericoException pEx) {
			sendError(response, pEx.getLocalizedMessage(locale));
		} catch (IllegalArgumentException e) {
			sendError(response, e.getMessage());
		}
	}

	private void getRealTimeData(HttpServletRequest request,
			HttpServletResponse response) throws PerifericoException,
			IOException {
		String requestedDateString = request.getParameter("reqDate");
		String hourStr = request.getParameter("hourStr");
		String minutesStr = request.getParameter("minutesStr");
		String analyzerIdStr = request.getParameter("analyzerIdStr");
		String elementIdStr = request.getParameter("elementIdStr");
		String halfDay = request.getParameter("halfDay");
		String fieldSeparator = request.getParameter("fieldSeparator");
		String decimalSeparator = request.getParameter("decimalSeparator");

		if (requestedDateString == null || analyzerIdStr == null
				|| elementIdStr == null) {
			throw new PerifericoException("missing_parameter");
		}
		// set fieldSeparator and decimalSeparator
		if (fieldSeparator == null)
			fieldSeparator = FIELD_SEPARATOR;
		if (decimalSeparator == null)
			decimalSeparator = DECIMAL_SEPARATOR;
		if (fieldSeparator != null && decimalSeparator != null
				&& fieldSeparator.equals(decimalSeparator)) {
			throw new PerifericoException("wrong_separator");
		}
		// create startDate and endDate
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		sdf.setLenient(false);
		SimpleDateFormat sdfTimestamp = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss");
		Date startDate = new Date();
		try {
			startDate = sdf.parse(requestedDateString);
		} catch (ParseException pEx) {
			throw new PerifericoException("start_date_error");
		}
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(startDate);
		Calendar endCal = new GregorianCalendar();

		if (hourStr != null) {
			if (hourStr.startsWith("0"))
				hourStr = hourStr.substring(1);
			int reqHour = new Integer(hourStr).intValue();
			startCal.set(Calendar.HOUR_OF_DAY, reqHour);
			if (minutesStr != null && !minutesStr.equals("")) {
				int reqMinutes = new Integer(minutesStr).intValue();
				startCal.set(Calendar.MINUTE, reqMinutes);
			} else
				startCal.set(Calendar.MINUTE, 0);

			startCal.set(Calendar.SECOND, 0);
			startCal.set(Calendar.MILLISECOND, 0);

			endCal.setTime(startCal.getTime());
			if (minutesStr == null || minutesStr.equals(""))
				endCal.add(Calendar.HOUR_OF_DAY, 1);
			else
				endCal.add(Calendar.MINUTE, 10);
		} else {
			String startHour = new String("00");
			String endHour = new String("24");
			if (halfDay != null) {
				if ("0".equals(halfDay))
					endHour = "12";
				else if ("1".equals(halfDay))
					startHour = "12";
			}

			if (startHour.startsWith("0"))
				startHour = startHour.substring(1);
			int reqHour = new Integer(startHour).intValue();
			startCal.set(Calendar.HOUR_OF_DAY, reqHour);
			startCal.set(Calendar.MINUTE, 0);
			startCal.set(Calendar.SECOND, 0);
			startCal.set(Calendar.MILLISECOND, 0);

			if (endHour.startsWith("0"))
				endHour = endHour.substring(1);
			reqHour = new Integer(endHour).intValue();
			endCal.setTime(startDate);
			endCal.set(Calendar.HOUR_OF_DAY, reqHour);
			endCal.set(Calendar.MINUTE, 0);
			endCal.set(Calendar.SECOND, 0);
			endCal.set(Calendar.MILLISECOND, 0);
		}

		Station station = perifericoApp.getStation();
		Analyzer analyzer = station.getAnalyzer(analyzerIdStr);
		Type type = analyzer.getType();
		switch (type) {
		case DPA:
			DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
			GenericSampleElement sampleElement = dpaAnalyzer
					.getElement(elementIdStr);
			prepareRealTimeData(response, station.getName(), analyzer,
					sampleElement, sdfTimestamp, elementIdStr, fieldSeparator,
					decimalSeparator, startCal, endCal);
			break;
		case AVG:
			AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
			sampleElement = avgAnalyzer.getElement(elementIdStr);
			prepareRealTimeData(response, station.getName(), analyzer,
					sampleElement, sdfTimestamp, elementIdStr, fieldSeparator,
					decimalSeparator, startCal, endCal);
			break;
		case RAIN:
			RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
			CounterElement rainElement = rainAnalyzer.getElement(elementIdStr);
			prepareRealTimeData(response, station.getName(), analyzer,
					rainElement, sdfTimestamp, elementIdStr, fieldSeparator,
					decimalSeparator, startCal, endCal);
			break;
		case WIND:
			CommonCfg cc = Periferico.getInstance().getCommonCfg();
			WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
			Parameter param = cc.getParameter(elementIdStr);
			if (param == null)
				sampleElement = null;
			else if (param.getType() == Parameter.ParamType.WIND_VEL)
				sampleElement = windAnalyzer.getWind().getSpeed();
			else if (param.getType() == Parameter.ParamType.WIND_DIR)
				sampleElement = windAnalyzer.getWind().getDirection();
			else
				sampleElement = null;
			prepareRealTimeData(response, station.getName(), analyzer,
					sampleElement, sdfTimestamp, elementIdStr, fieldSeparator,
					decimalSeparator, startCal, endCal);
			break;
		case SAMPLE:
			SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
			sampleElement = sampleAnalyzer.getElement(elementIdStr);
			prepareRealTimeData(response, station.getName(), analyzer,
					sampleElement, sdfTimestamp, elementIdStr, fieldSeparator,
					decimalSeparator, startCal, endCal);
			break;
		}

	} // end getRealTimeData

	private void prepareRealTimeData(HttpServletResponse response,
			String stationName, Analyzer analyzer, ScalarElement scalarElement,
			SimpleDateFormat sdfTimestamp, String elementIdStr,
			String fieldSeparator, String decimalSeparator, Calendar startCal,
			Calendar endCal) throws StorageException, IOException {
		List<Sample> sampleList = new ArrayList<Sample>();
		sampleList = storageManager.readSampleData(analyzer.getId(),
				elementIdStr, startCal.getTime(), false, endCal.getTime(),
				true, null);
		response.setContentType("text/x-comma-separated-values");
		response.setCharacterEncoding("UTF-8");
		String fn = encodeFileName(stationName, analyzer.getName(),
				scalarElement.getParameterId(), true);
		response.setHeader("Content-Disposition", "attachment; " + fn);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF-8"), true);
		Integer acqPeriod = null;
		if (scalarElement instanceof GenericSampleElement)
			acqPeriod = ((GenericSampleElement) scalarElement).getAcqPeriod();
		printCsvHeader(pw, fieldSeparator, analyzer.getName(),
				scalarElement.getParameterId(),
				scalarElement.getMeasureUnitName(), acqPeriod, null,
				localize("sample"), sdfTimestamp.format(new Date()));
		pw.println(localize("date") + fieldSeparator + localize("value")
				+ fieldSeparator + localize("not_valid") + fieldSeparator
				+ localize("multi_flag"));
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(decimalSeparator.charAt(0));
		DecimalFormat df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(Periferico.ROUNDING_MODE_FOR_SAMPLE_DATA);
		df.setMinimumFractionDigits(scalarElement.getNumDec());
		df.setMaximumFractionDigits(scalarElement.getNumDec());
		df.setGroupingUsed(false);
		for (int i = 0; i < sampleList.size(); i++) {
			Sample sample = sampleList.get(i);
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(sdfTimestamp.format(sample.getTimestamp()));
			strBuf.append(fieldSeparator);
			strBuf.append(PerifericoUtil.formatDouble(sample.getValue(), df));
			strBuf.append(fieldSeparator);
			strBuf.append(new Boolean(sample.isNotvalid()).toString());
			strBuf.append(fieldSeparator);
			strBuf.append(Integer.toHexString(sample.getFlags()));
			pw.println(strBuf.toString());
		}
		pw.close();
	}

	private void getMeansData(HttpServletRequest request,
			HttpServletResponse response) throws PerifericoException,
			IOException {
		String startDateStr = request.getParameter("startDateStr");
		String startHourStr = request.getParameter("startHourStr");
		String endDateStr = request.getParameter("endDateStr");
		String endHourStr = request.getParameter("endHourStr");
		String analyzerIdStr = request.getParameter("analyzerIdStr");
		String elementIdStr = request.getParameter("elementIdStr");
		String periodStr = request.getParameter("periodStr");
		String maxDays = request.getParameter("maxDays");
		String fieldSeparator = request.getParameter("fieldSeparator");
		String decimalSeparator = request.getParameter("decimalSeparator");
		String chooseType = request.getParameter("chooseType");
		String halfDayStr = request.getParameter("halfDayStr");

		if (startDateStr == null || startHourStr == null || endDateStr == null
				|| endHourStr == null || analyzerIdStr == null
				|| elementIdStr == null || periodStr == null) {
			throw new PerifericoException("missing_parameter");
		}
		// set fieldSeparator and decimalSeparator
		if (fieldSeparator == null)
			fieldSeparator = FIELD_SEPARATOR;
		if (decimalSeparator == null)
			decimalSeparator = DECIMAL_SEPARATOR;
		if (fieldSeparator != null && decimalSeparator != null
				&& fieldSeparator.equals(decimalSeparator)) {
			throw new PerifericoException("wrong_separator");
		}
		// create startDate and endDate
		SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
		sdfDay.setLenient(false);
		SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
		sdfHour.setLenient(false);
		SimpleDateFormat sdfTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Calendar startCal = new GregorianCalendar();
		Calendar endCal = new GregorianCalendar();

		if (chooseType == null || chooseType.equals("")
				|| ONE_MONTH.equals(chooseType) || ONE_YEAR.equals(chooseType)) {
			Date startDate = null;
			try {
				startDate = sdfDay.parse(startDateStr);
			} catch (ParseException e) {
				throw new PerifericoException("start_date_error");
			}
			startCal.setTime(startDate);
			Date startHour = null;
			try {
				startHour = sdfHour.parse(startHourStr);
			} catch (ParseException e) {
				throw new PerifericoException("start_hour_error");
			}
			Calendar hourCal = new GregorianCalendar();
			hourCal.setTime(startHour);
			startCal.set(Calendar.HOUR_OF_DAY,
					hourCal.get(Calendar.HOUR_OF_DAY));
			startCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
			Date endDate = null;
			try {
				endDate = sdfDay.parse(endDateStr);
			} catch (ParseException e) {
				throw new PerifericoException("end_date_error");
			}
			endCal.setTime(endDate);
			Date endHour = null;
			try {
				endHour = sdfHour.parse(endHourStr);
			} catch (ParseException e) {
				throw new PerifericoException("end_hour_error");
			}
			hourCal = new GregorianCalendar();
			hourCal.setTime(endHour);
			endCal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
			endCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
		} else if (ONE_DAY.equals(chooseType)) {
			Date startDate = null;
			try {
				startDate = sdfDay.parse(startDateStr);
			} catch (ParseException e) {
				throw new PerifericoException("start_date_error");
			}
			startCal.setTime(startDate);
			startCal.set(Calendar.HOUR_OF_DAY, 0);
			startCal.set(Calendar.MINUTE, 0);
			endCal.setTime(startCal.getTime());
			endCal.add(Calendar.DAY_OF_MONTH, 1);
		} else if (HALF_DAY.equals(chooseType)) {
			Date startDate = null;
			try {
				startDate = sdfDay.parse(startDateStr);
			} catch (ParseException e) {
				throw new PerifericoException("start_date_error");
			}
			startCal.setTime(startDate);
			if ("0".equals(halfDayStr))
				startCal.set(Calendar.HOUR_OF_DAY, 0);
			else
				startCal.set(Calendar.HOUR_OF_DAY, 12);
			startCal.set(Calendar.MINUTE, 0);
			endCal.setTime(startCal.getTime());
			if ("0".equals(halfDayStr))
				endCal.set(Calendar.HOUR_OF_DAY, 12);
			else
				endCal.set(Calendar.HOUR_OF_DAY, 24);
		}
		endCal.set(Calendar.SECOND, 0);
		endCal.set(Calendar.MILLISECOND, 0);
		if (endCal.before(startCal)) {
			throw new PerifericoException("incoherent_calendar");
		}

		// verify if chosen period is over maxDays, if maxDays has a value
		if (maxDays != null && !maxDays.equals("")) {
			int maxDaysValue = new Integer(maxDays).intValue();
			Calendar cmpCal = new GregorianCalendar();
			cmpCal.setTime(startCal.getTime());
			cmpCal.add(Calendar.DAY_OF_MONTH, maxDaysValue);
			if (cmpCal.before(endCal)) {
				throw new PerifericoException("max_data_requested", maxDays);
			}
		}

		Station station = perifericoApp.getStation();
		Analyzer analyzer = station.getAnalyzer(analyzerIdStr);
		Type type = analyzer.getType();
		switch (type) {
		case DPA:
			DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
			GenericSampleElement sampleElement = dpaAnalyzer
					.getElement(elementIdStr);
			prepareMeansData(response, station.getName(), analyzer,
					sampleElement, sdfTimestamp, elementIdStr, periodStr,
					fieldSeparator, decimalSeparator, startCal, endCal);
			break;
		case AVG:
			AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
			sampleElement = avgAnalyzer.getElement(elementIdStr);
			prepareMeansData(response, station.getName(), analyzer,
					sampleElement, sdfTimestamp, elementIdStr, periodStr,
					fieldSeparator, decimalSeparator, startCal, endCal);
			break;
		case RAIN:
			RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
			CounterElement rainElement = rainAnalyzer.getElement(elementIdStr);
			prepareTotalData(response, station.getName(), analyzer,
					rainElement, sdfTimestamp, elementIdStr, periodStr,
					fieldSeparator, decimalSeparator, startCal, endCal);
			break;
		case WIND:
			WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
			WindElement windElement = windAnalyzer.getWind();
			elementIdStr = windElement.getParameterId();
			prepareWindData(response, station.getName(), analyzer, windElement,
					sdfTimestamp, elementIdStr, periodStr, fieldSeparator,
					decimalSeparator, startCal, endCal);
			break;
		case SAMPLE:
			SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
			sampleElement = sampleAnalyzer.getElement(elementIdStr);
			prepareMeansData(response, station.getName(), analyzer,
					sampleElement, sdfTimestamp, elementIdStr, periodStr,
					fieldSeparator, decimalSeparator, startCal, endCal);
			break;
		}

	} // end getMeansData

	private void prepareMeansData(HttpServletResponse response,
			String stationName, Analyzer analyzer,
			GenericSampleElement sampleElement, SimpleDateFormat sdfTimestamp,
			String elementIdStr, String periodStr, String fieldSeparator,
			String decimalSeparator, Calendar startCal, Calendar endCal)
			throws NumberFormatException, StorageException, IOException {
		List<MeanValue> meanValueList = new ArrayList<MeanValue>();
		Integer aggregationPeriod = Integer.parseInt(periodStr);
		meanValueList = storageManager.readMeanData(analyzer.getId(),
				elementIdStr, aggregationPeriod, startCal.getTime(), false,
				endCal.getTime(), true, null);
		response.setContentType("text/x-comma-separated-values");
		response.setCharacterEncoding("UTF-8");
		String fn = encodeFileName(stationName, analyzer.getName(),
				sampleElement.getParameterId(), false);
		response.setHeader("Content-Disposition", "attachment; " + fn);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF-8"), true);
		printCsvHeader(pw, fieldSeparator, analyzer.getName(),
				sampleElement.getParameterId(),
				sampleElement.getMeasureUnitName(),
				sampleElement.getAcqPeriod(), aggregationPeriod,
				localize("mean"), sdfTimestamp.format(new Date()));
		pw.println(localize("date") + fieldSeparator + localize("value")
				+ fieldSeparator + localize("not_valid") + fieldSeparator
				+ localize("multi_flag"));
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(decimalSeparator.charAt(0));
		DecimalFormat df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		df.setMinimumFractionDigits(sampleElement.getNumDec());
		df.setMaximumFractionDigits(sampleElement.getNumDec());
		df.setGroupingUsed(false);
		for (int i = 0; i < meanValueList.size(); i++) {
			MeanValue meanValue = meanValueList.get(i);
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(sdfTimestamp.format(meanValue.getTimestamp()));
			strBuf.append(fieldSeparator);
			strBuf.append(PerifericoUtil.formatDouble(meanValue.getValue(), df));
			strBuf.append(fieldSeparator);
			strBuf.append(new Boolean(meanValue.isNotvalid()).toString());
			strBuf.append(fieldSeparator);
			strBuf.append(Integer.toHexString(meanValue.getFlags()));
			pw.println(strBuf.toString());
		}
		pw.close();
	}

	private void prepareTotalData(HttpServletResponse response,
			String stationName, Analyzer analyzer,
			CounterElement counterElement, SimpleDateFormat sdfTimestamp,
			String elementIdStr, String periodStr, String fieldSeparator,
			String decimalSeparator, Calendar startCal, Calendar endCal)
			throws NumberFormatException, StorageException, IOException {
		List<TotalValue> totalValueList = new ArrayList<TotalValue>();
		Integer aggregationPeriod = Integer.parseInt(periodStr);
		totalValueList = storageManager.readTotalData(analyzer.getId(),
				elementIdStr, aggregationPeriod, startCal.getTime(), false,
				endCal.getTime(), true, null);
		response.setContentType("text/x-comma-separated-values");
		response.setCharacterEncoding("UTF-8");
		String fn = encodeFileName(stationName, analyzer.getName(),
				counterElement.getParameterId(), false);
		response.setHeader("Content-Disposition", "attachment; " + fn);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF-8"), true);
		printCsvHeader(pw, fieldSeparator, analyzer.getName(),
				counterElement.getParameterId(),
				counterElement.getMeasureUnitName(), null, aggregationPeriod,
				localize("total"), sdfTimestamp.format(new Date()));
		pw.println(localize("date") + fieldSeparator + localize("value")
				+ fieldSeparator + localize("not_valid") + fieldSeparator
				+ localize("multi_flag"));
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(decimalSeparator.charAt(0));
		DecimalFormat df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		df.setMinimumFractionDigits(counterElement.getNumDec());
		df.setMaximumFractionDigits(counterElement.getNumDec());
		df.setGroupingUsed(false);
		for (TotalValue totalValue : totalValueList) {
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(sdfTimestamp.format(totalValue.getTimestamp()));
			strBuf.append(fieldSeparator);
			strBuf.append(PerifericoUtil.formatDouble(totalValue.getValue(), df));
			strBuf.append(fieldSeparator);
			strBuf.append(new Boolean(totalValue.isNotvalid()).toString());
			strBuf.append(fieldSeparator);
			strBuf.append(Integer.toHexString(totalValue.getFlags()));
			pw.println(strBuf.toString());
		}
		pw.close();
	}

	private void prepareWindData(HttpServletResponse response,
			String stationName, Analyzer analyzer, WindElement windElement,
			SimpleDateFormat sdfTimestamp, String elementIdStr,
			String periodStr, String fieldSeparator, String decimalSeparator,
			Calendar startCal, Calendar endCal) throws NumberFormatException,
			StorageException, IOException {
		List<WindValue> windValueList = new ArrayList<WindValue>();
		Integer aggregationPeriod = Integer.parseInt(periodStr);
		windValueList = storageManager.readWindAggregateData(analyzer.getId(),
				elementIdStr, aggregationPeriod, startCal.getTime(), false,
				endCal.getTime(), true, null);
		response.setContentType("text/x-comma-separated-values");
		response.setCharacterEncoding("UTF-8");
		String fn = encodeFileName(stationName, analyzer.getName(),
				windElement.getParameterId(), false);
		response.setHeader("Content-Disposition", "attachment; " + fn);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF-8"), true);
		printCsvHeader(
				pw,
				fieldSeparator,
				analyzer.getName(),
				windElement.getParameterId(),
				windElement.getSpeedMeasureUnitName() + " - "
						+ windElement.getDirectionMeasureUnitName(),
				windElement.getAcqPeriod(), aggregationPeriod,
				localize("vect_wind"), sdfTimestamp.format(new Date()));
		pw.println(localize("date") + fieldSeparator + localize("vect_speed")
				+ fieldSeparator + localize("vect_dir") + fieldSeparator
				+ localize("dev_std") + fieldSeparator
				+ localize("scalar_speed") + fieldSeparator
				+ localize("gust_speed") + fieldSeparator
				+ localize("gust_dir") + fieldSeparator
				+ localize("calm_percent") + fieldSeparator
				+ localize("is_calm") + fieldSeparator + localize("not_valid")
				+ fieldSeparator + localize("multi_flag"));
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(decimalSeparator.charAt(0));
		DecimalFormat dfSpeed = new DecimalFormat();
		dfSpeed.setDecimalFormatSymbols(dfs);
		dfSpeed.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		dfSpeed.setMinimumFractionDigits(windElement.getSpeedNumDec());
		dfSpeed.setMaximumFractionDigits(windElement.getSpeedNumDec());
		dfSpeed.setGroupingUsed(false);
		DecimalFormat dfDir = new DecimalFormat();
		dfDir.setDecimalFormatSymbols(dfs);
		dfDir.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		dfDir.setMinimumFractionDigits(windElement.getDirectionNumDec());
		dfDir.setMaximumFractionDigits(windElement.getDirectionNumDec());
		dfDir.setGroupingUsed(false);
		DecimalFormat df1 = new DecimalFormat();
		df1.setDecimalFormatSymbols(dfs);
		df1.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		df1.setMinimumFractionDigits(1);
		df1.setMaximumFractionDigits(1);
		df1.setGroupingUsed(false);
		for (WindValue windValue : windValueList) {
			StringBuffer sb = new StringBuffer();
			sb.append(sdfTimestamp.format(windValue.getTimestamp()));
			sb.append(fieldSeparator);
			sb.append(PerifericoUtil.formatDouble(
					windValue.getVectorialSpeed(), dfSpeed));
			sb.append(fieldSeparator);
			sb.append(PerifericoUtil.formatDouble(
					windValue.getVectorialDirection(), dfDir));
			sb.append(fieldSeparator);
			sb.append(PerifericoUtil.formatDouble(
					windValue.getStandardDeviation(), dfSpeed));
			sb.append(fieldSeparator);
			sb.append(PerifericoUtil.formatDouble(windValue.getScalarSpeed(),
					dfSpeed));
			sb.append(fieldSeparator);
			sb.append(PerifericoUtil.formatDouble(windValue.getGustSpeed(),
					dfSpeed));
			sb.append(fieldSeparator);
			sb.append(PerifericoUtil.formatDouble(windValue.getGustDirection(),
					dfDir));
			sb.append(fieldSeparator);
			sb.append(PerifericoUtil.formatDouble(
					windValue.getCalmsNumberPercent(), df1));
			sb.append(fieldSeparator);
			sb.append(formatObject(windValue.getCalm()));
			sb.append(fieldSeparator);
			sb.append(formatObject(windValue.isNotvalid()));
			sb.append(fieldSeparator);
			sb.append(formatHex(windValue.getFlags()));
			pw.println(sb.toString());
		}
		pw.close();
	}

	private String formatObject(Object object) {
		if (object == null)
			return "";
		return object.toString();
	}

	private String formatHex(Integer value) {
		if (value == null)
			return "";
		return Integer.toHexString(value);
	}

	private void sendError(HttpServletResponse response, String message)
			throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter pw = response.getWriter();
		pw.println(message);
		pw.close();
	}

	private void printCsvHeader(PrintWriter pw, String fieldSeparator,
			String analyzerName, String parameterId, String measureUnit,
			Integer acquisitionPeriod, Integer aggregationPeriod,
			String dataType, String exportDate) {
		Station station = perifericoApp.getConfig().getStation();
		String stationName = station.getName();
		if (stationName == null || stationName.isEmpty())
			stationName = station.getShortName();
		pw.println(localize("station") + fieldSeparator + stationName);
		pw.println(localize("analyzer") + fieldSeparator + analyzerName);
		pw.println(localize("parameter") + fieldSeparator + parameterId);
		pw.println(localize("measure_unit") + fieldSeparator + measureUnit);
		pw.println(localize("acquisition_period") + fieldSeparator
				+ (acquisitionPeriod != null ? acquisitionPeriod : ""));
		pw.println(localize("aggregation_period") + fieldSeparator
				+ (aggregationPeriod != null ? aggregationPeriod : ""));
		pw.println(localize("data_type") + fieldSeparator + dataType);
		pw.println(localize("export_date") + fieldSeparator + exportDate);
		pw.println();
	}

	private String localize(String message) {
		if (message == null)
			return "";
		if (propertyUtil == null || locale == null)
			return message;
		String msgBundle = null;
		try {
			msgBundle = propertyUtil.getProperty(locale, message);
		} catch (MissingResourceException mrex) {
			return message;
		}
		return msgBundle;
	}

	private String encodeFileName(String stationName, String analyzerName,
			String paramName, boolean sampleData) {
		String date = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
		String fileName = stationName + "_" + analyzerName + "_" + paramName
				+ (sampleData ? "_samples" : "") + "_" + date + ".csv";
		return "filename=\"" + fileName + "\"";
	}

}
