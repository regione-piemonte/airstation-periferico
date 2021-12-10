/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: service that manages requests from Centrale software
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: CopService.java,v 1.42 2015/10/16 12:58:49 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import it.csi.periferico.Periferico;
import it.csi.periferico.PerifericoException;
import it.csi.periferico.PerifericoStatus;
import it.csi.periferico.PerifericoUtil;
import it.csi.periferico.Version;
import it.csi.periferico.acqdrivers.DriverInfo;
import it.csi.periferico.acqdrivers.itf.ConfigInfo;
import it.csi.periferico.config.Config;
import it.csi.periferico.core.AlarmStatus;
import it.csi.periferico.core.Analyzer;
import it.csi.periferico.core.BinaryStatus;
import it.csi.periferico.core.ContainerAlarm;
import it.csi.periferico.core.DigitalContainerAlarm;
import it.csi.periferico.core.Element;
import it.csi.periferico.core.IntegerStatus;
import it.csi.periferico.core.MeanValue;
import it.csi.periferico.core.Sample;
import it.csi.periferico.core.ScalarElement;
import it.csi.periferico.core.Station;
import it.csi.periferico.core.TotalValue;
import it.csi.periferico.core.TriggerContainerAlarm;
import it.csi.periferico.core.WindElement;
import it.csi.periferico.core.WindValue;
import it.csi.periferico.gps.GpsDatum;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

/**
 * Service that manages requests from Centrale software
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class CopService extends HttpServlet {

	private static final long serialVersionUID = 1447915842105672086L;

	private static final Integer MAX_DATA_FOR_REQUEST = 14400;

	private static final Version VERSION = new Version(1, 0, 3);

	private static final int ERR_CODE_BAD_REQUEST = 1;

	private static final int ERR_CODE_FN_BAD_PARAMS = 2;

	private static final int ERR_CODE_FN_EXEC_FAIL = 3;

	// ATTENTION: SimpleDateFormat parse and format functions are not reentrant
	private final DateFormat requestDateFmt = new SimpleDateFormat("yyyyMMddHHmmss");

	private final DateFormat requestTzDateFmt = new SimpleDateFormat("yyyyMMddHHmmssZ");

	private final DateFormat dataTzDateFmt = new SimpleDateFormat("yyyyMMdd HHmmss Z");

	private static final String DATA_DATE_FMT_STR = "yyyyMMdd HHmmss";

	private static final String DATA_DATE_FMT_MS_STR = "yyyyMMdd HHmmss.SSS";

	private static final String MSG_INV_PARAM_FORMAT = "Invalid format for parameter ";

	private static final String MSG_PARAM_MISSING = "Missing requested parameter ";

	private static final String MSG_PERIF_NOT_INIT = "Periferico application not initialized";

	private static Logger logger = Logger.getLogger("periferico." + CopService.class.getSimpleName());

	private Periferico periferico;

	private StorageManager storageManager;

	public CopService() {
		periferico = Periferico.getInstance();
		storageManager = periferico.getStorageManager();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String function = request.getParameter("function");
		if (function == null) {
			sendError(response, ERR_CODE_BAD_REQUEST, "No function specified");
			return;
		}
		try {
			if ("getPerifericoVersion".equals(function))
				getPerifericoVersion(request, response);
			else if ("getProtocolVersion".equals(function))
				getProtocolVersion(request, response);
			else if ("getDriverVersions".equals(function))
				getDriverVersions(request, response);
			else if ("getApplicationStatus".equals(function))
				getApplicationStatus(request, response);
			else if ("getDiskUsage".equals(function))
				getDiskUsage(request, response);
			else if ("listContainerAlarms".equals(function))
				listContainerAlarms(request, response);
			else if ("listAnalyzers".equals(function))
				listAnalyzers(request, response);
			else if ("getStationConfig".equals(function))
				getStationConfig(request, response);
			else if ("getContainerAlarmsConfig".equals(function))
				getContainerAlarmsConfig(request, response);
			else if ("getAnalyzersConfig".equals(function))
				getAnalyzersConfig(request, response);
			else if ("getEventData".equals(function))
				getEventData(request, response);
			else if ("getSampleData".equals(function))
				getSampleData(request, response);
			else if ("getMeanData".equals(function))
				getMeanData(request, response);
			else if ("getTotalData".equals(function))
				getTotalData(request, response);
			else if ("getWindData".equals(function))
				getWindData(request, response);
			else if ("getDigitalCAData".equals(function))
				getDigitalCAData(request, response);
			else if ("getTriggerCAData".equals(function))
				getTriggerCAData(request, response);
			else if ("getGpsData".equals(function))
				getGpsData(request, response);
			else if ("getConfigFile".equals(function))
				getConfigFile(request, response);
			else if ("doTimeSync".equals(function))
				doTimeSync(request, response);
			else {
				sendError(response, ERR_CODE_BAD_REQUEST, "Unknown function");
				return;
			}
		} catch (ServiceException e) {
			sendError(response, ERR_CODE_BAD_REQUEST, e.getMessage());
		} catch (IllegalArgumentException e) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, e.getMessage());
		} catch (StorageException e) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, e.getMessage());
		} catch (PerifericoException e) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
		// NOTE: using Jetty as application server, it is not possible to read
		// request parameters calling getParameter function and then obtaining
		// an InputStream or Reader to read request postdata: for this reason
		// function identifier is passed as the first line of request postadata
		String function = br.readLine();
		if (function == null) {
			sendError(response, ERR_CODE_BAD_REQUEST, "No function specified");
			return;
		}
		try {
			if ("setCommonFile".equals(function))
				setCommonFile(br, response);
			else {
				sendError(response, ERR_CODE_BAD_REQUEST, "Unknown function");
				return;
			}
		} catch (ServiceException e) {
			sendError(response, ERR_CODE_BAD_REQUEST, e.getMessage());
		} catch (IllegalArgumentException e) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, e.getMessage());
		} catch (PerifericoException e) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, e.getMessage());
		}
	}

	// ----------------------------------------------------
	// metodi privati per ottenere le informazioni dal cop
	// --------------------------------------------------
	private void getPerifericoVersion(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println(Periferico.VERSION.toString());
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getProtocolVersion(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println(VERSION.toString());
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void getDriverVersions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException {
		String output = parseString(request, "mode", true);
		PrintWriter pw = getPrintWriterFromResponse(response);
		List<DriverInfo> info = periferico.getDriverManager().getDriverInfo();
		if ("drivertable".equalsIgnoreCase(output)) {
			pw.println("OK");
			pw.println(info.size());
			for (DriverInfo di : info)
				pw.println(di.getName() + "," + di.getVersion().toString());
		} else if ("configtable".equalsIgnoreCase(output)) {
			pw.println("OK");
			int rows = 0;
			for (DriverInfo di : info)
				rows += di.getListConfigInfo().size();
			pw.println(rows);
			for (DriverInfo di : info)
				for (ConfigInfo ci : di.getListConfigInfo())
					pw.println(di.getName() + "," + di.getVersion().toString() + "," + ci.getName() + ","
							+ objToString(ci.getVersion()) + "," + objToString(ci.getChecksum()) + ","
							+ ci.isNewConfig() + "," + objToString(ci.isChecksumValid()));
		} else {
			pw.println("OK");
			pw.println("drivers: " + info.size());
			for (DriverInfo di : info) {
				pw.println(di.getName());
				pw.println("attributes: " + 1);
				pw.println("version = " + di.getVersion().toString());
				List<ConfigInfo> cfgInfos = di.getListConfigInfo();
				pw.println("configs: " + cfgInfos.size());
				for (ConfigInfo ci : cfgInfos) {
					pw.println(ci.getName());
					pw.println("attributes: " + 4);
					pw.println("version = " + objToString(ci.getVersion()));
					pw.println("checksum = " + objToString(ci.getChecksum()));
					pw.println("newConfig = " + ci.isNewConfig());
					pw.println("checksumValid = " + objToString(ci.isChecksumValid()));
				}
			}
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void getDiskUsage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException {
		UUID stationId = parseUUID(request, "station", false);
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println("SMART = " + storageManager.getSmartStatus());
		pw.println("RAID = " + storageManager.getRaidStatus());
		pw.println("warn = " + storageManager.getUsedSpaceWarningThresholdPercent());
		pw.println("alarm = " + storageManager.getUsedSpaceAlarmThresholdPercent());
		pw.println("filesystems: " + 3);
		pw.println("root = " + storageManager.getRootFsUsedSpacePercent());
		pw.println("temp = " + storageManager.getTmpFsUsedSpacePercent());
		pw.println("data = " + storageManager.getDataFsUsedSpacePercent());
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void getApplicationStatus(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException {
		UUID stationId = parseUUID(request, "station", false);
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		PerifericoStatus status = periferico.getStatus();
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println("attributes: " + 18);
		pw.println("isOK = " + status.isOK());
		pw.println("boardManInitStatus = " + objToString(status.getBoardManagerInitStatus()));
		pw.println("cfgBoardsNumber = " + objToString(status.getConfiguredBoardsNumber()));
		pw.println("initBoardsNumber = " + objToString(status.getInitializedBoardsNumber()));
		pw.println("failedBoardBindingsNumber = " + objToString(status.getFailedBoardBindingsNumber()));
		pw.println("areDPAOK = " + objToString(status.isDataPortAnalyzersOK()));
		pw.println("enabledDPANumber = " + objToString(status.getEnabledDataPortAnalyzersNumber()));
		pw.println("initDPADriversNumber = " + objToString(status.getInitializedDataPortDriversNumber()));
		pw.println("failedDPAThreadsNumber = " + objToString(status.getFailedDataPortThreadsNumber()));
		pw.println("areDriverConfigsOK = " + objToString(status.areDriverConfigsOK()));
		pw.println("loadCfgStatus = " + configStatusToString(status.getLoadConfigurationStatus()));
		pw.println("saveCfgStatus = " + objToString(status.getSaveNewConfigurationStatus()));
		pw.println("cfgActivationStatus = " + objToString(status.getConfigActivationStatus()));
		pw.println("totalThreadFailures = " + status.getTotalThreadFailures());
		pw.println("currentThreadFailures = " + status.getCurrentThreadFailures());
		pw.println("dataWriteErrorCount = " + status.getDataWriteErrorCount());
		pw.println("commonCfgFromCopStatus = " + objToString(status.getCommonConfigFromCopStatus()).toLowerCase());
		pw.println("dataInTheFuture = " + status.isDataInTheFuture());
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void listContainerAlarms(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException {
		UUID stationId = parseUUID(request, "station", false);
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(station.getContainer().getListAlarm().size());
		for (ContainerAlarm ca : station.getContainer().getListAlarm()) {
			StringBuffer sb = new StringBuffer();
			sb.append(ca.getIdAsString());
			if (ca instanceof DigitalContainerAlarm) {
				DigitalContainerAlarm dca = (DigitalContainerAlarm) ca;
				sb.append(',');
				sb.append("DIGITAL");
				sb.append(',');
				sb.append(binaryStatusToString(dca.getStatus()));
			} else if (ca instanceof TriggerContainerAlarm) {
				TriggerContainerAlarm tca = (TriggerContainerAlarm) ca;
				sb.append(',');
				sb.append("TRIGGER");
				sb.append(',');
				sb.append(alarmStatusToString(tca.getStatus()));
			}
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void listAnalyzers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException {
		UUID stationId = parseUUID(request, "station", false);
		Boolean sendDataValid = parseBoolean(request, "dataValid", true);
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(station.getListAnalyzer().size());
		for (Analyzer an : station.getListAnalyzer()) {
			StringBuffer sb = new StringBuffer();
			sb.append(an.getIdAsString());
			sb.append(',');
			sb.append(an.getStatusAsString());
			sb.append(',');
			sb.append(binaryStatusToString(an.getFaultStatus()));
			if (Boolean.TRUE.equals(sendDataValid)) {
				sb.append(',');
				sb.append(binaryStatusToString(an.getDataValidStatus()));
			}
			sb.append(',');
			sb.append(binaryStatusToString(an.getMaintenanceInProgress()));
			sb.append(',');
			sb.append(binaryStatusToString(an.getManualCalibrationRunning()));
			sb.append(',');
			sb.append(binaryStatusToString(an.getAutoCheckRunning()));
			sb.append(',');
			sb.append(binaryStatusToString(an.getAutoCheckFailed()));
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void getStationConfig(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException {
		UUID stationId = parseUUID(request, "station", true);
		UUID configId = parseUUID(request, "config", true);
		Config config = periferico.getConfig();
		if (stationId == null && configId != null) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID must be specified when config ID is specified");
			return;
		}
		if (config == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (stationId != null && !config.getStation().getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		Station station = config.getStation();
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(config.getIdAsString());
		if (PerifericoUtil.areEqual(configId, config.getId())) {
			pw.close();
			return;
		}
		DateFormat dataDateFmt = new SimpleDateFormat(DATA_DATE_FMT_STR);
		pw.println(station.getIdAsString());
		pw.println("attributes: " + 11);
		pw.println("shortName = " + station.getShortName());
		pw.println("name = " + station.getName());
		pw.println("province = " + station.getProvince());
		pw.println("city = " + station.getCity());
		pw.println("address = " + station.getAddress());
		pw.println("location = " + station.getLocation());
		pw.println("notes = " + station.getUserNotes());
		pw.println("gpsInstalled = " + station.isGpsInstalled());
		String strDate = "";
		if (config.getDate() != null)
			strDate = dataDateFmt.format(config.getDate());
		pw.println("cfgDate = " + strDate);
		pw.println("cfgAuthor = " + config.getAuthor());
		pw.println("cfgComment = " + config.getComment());
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void getContainerAlarmsConfig(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException {
		UUID stationId = parseUUID(request, "station", false);
		UUID configId = parseUUID(request, "config", true);
		Config config = periferico.getConfig();
		if (config == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		Station station = config.getStation();
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(config.getIdAsString());
		if (PerifericoUtil.areEqual(configId, config.getId())) {
			pw.close();
			return;
		}
		pw.println("containerAlarms: " + station.getContainer().getListAlarm().size());
		for (ContainerAlarm ca : station.getContainer().getListAlarm()) {
			pw.println(ca.getIdAsString());
			pw.println("attributes: " + 2);
			pw.println("alarmId = " + ca.getAlarm().getAlarmNameId());
			pw.println("notes = " + ca.getDescription());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void getAnalyzersConfig(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException {
		UUID stationId = parseUUID(request, "station", false);
		UUID configId = parseUUID(request, "config", true);
		Config config = periferico.getConfig();
		if (config == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		Station station = config.getStation();
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(config.getIdAsString());
		if (PerifericoUtil.areEqual(configId, config.getId())) {
			pw.close();
			return;
		}
		pw.println("analyzers: " + station.getListAnalyzer().size());
		for (Analyzer an : station.getListAnalyzer()) {
			pw.println(an.getIdAsString());
			pw.println("attributes: " + 8);
			pw.println("name = " + an.getName());
			pw.println("brand = " + an.getBrand());
			pw.println("model = " + an.getModel());
			pw.println("description = " + an.getDescription());
			pw.println("sn = " + an.getSerialNumber());
			pw.println("status = " + an.getStatusAsString());
			pw.println("type = " + an.getType().toString());
			pw.println("notes = " + an.getUserNotes());
			Element[] elements = an.getElements();
			pw.println("elements: " + elements.length);
			for (int i = 0; i < elements.length; i++) {
				Element el = elements[i];
				pw.println(el.getParameterId());
				if (el instanceof ScalarElement) {
					ScalarElement se = (ScalarElement) el;
					pw.println("attributes: " + 7);
					pw.println("type = scalar");
					pw.println("enabled = " + el.isEnabled());
					pw.println("unit = " + se.getMeasureUnitName());
					pw.println("minval = " + se.getMinValue());
					pw.println("maxval = " + se.getMaxValue());
					pw.println("numdec = " + se.getNumDec());
				} else if (el instanceof WindElement) {
					WindElement we = (WindElement) el;
					pw.println("attributes: " + 12);
					pw.println("type = wind");
					pw.println("enabled = " + el.isEnabled());
					pw.println("speedParam = " + we.getSpeedParameterId());
					pw.println("speedUnit = " + we.getSpeedMeasureUnitName());
					pw.println("speedMinval = " + we.getSpeedMinValue());
					pw.println("speedMaxval = " + we.getSpeedMaxValue());
					pw.println("speedNumdec = " + we.getSpeedNumDec());
					pw.println("dirParam = " + we.getDirectionParameterId());
					pw.println("dirMinval = " + we.getDirectionMinValue());
					pw.println("dirMaxval = " + we.getDirectionMaxValue());
					pw.println("dirNumdec = " + we.getDirectionNumDec());

				} else {
					String msg = "Found unknown element type in configuration";
					logger.error(msg);
					throw new IllegalStateException(msg);
				}
				StringBuffer sb = new StringBuffer();
				sb.append("avgperiods = ");
				boolean first = true;
				for (Integer period : el.getAvgPeriods()) {
					if (!first)
						sb.append(",");
					sb.append(period);
					first = false;
				}
				pw.println(sb.toString());
			}
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws StorageException
	 */
	private void getEventData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, StorageException {
		UUID stationId = parseUUID(request, "station", false);
		UUID analyzerId = parseUUID(request, "analyzer", false);
		String eventId = parseString(request, "event", false);
		Date startDate = parseDate(request, "startDate", false);
		Date endDate = parseDate(request, "endDate", true);
		if (endDate == null)
			endDate = new Date();
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		DateFormat dataDateFmt_ms = new SimpleDateFormat(DATA_DATE_FMT_MS_STR);
		List<BinaryStatus> data = storageManager.readEventData(analyzerId, eventId, startDate, false, endDate, true,
				MAX_DATA_FOR_REQUEST);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(data.size());
		for (BinaryStatus bs : data) {
			StringBuffer sb = new StringBuffer();
			sb.append(dataDateFmt_ms.format(bs.getTimestamp()));
			sb.append(',');
			sb.append(binaryStatusToString(bs));
			if (bs instanceof IntegerStatus) {
				IntegerStatus is = (IntegerStatus) bs;
				sb.append(",");
				Integer intValue = is.getValue();
				if (intValue != null)
					sb.append(Integer.toHexString(intValue));
			}
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws StorageException
	 */
	private void getSampleData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, StorageException {
		UUID stationId = parseUUID(request, "station", false);
		UUID analyzerId = parseUUID(request, "analyzer", false);
		String paramId = parseString(request, "parameter", false);
		Date startDate = parseDate(request, "startDate", false);
		Date endDate = parseDate(request, "endDate", true);
		if (endDate == null)
			endDate = new Date();
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		DateFormat dataDateFmt = new SimpleDateFormat(DATA_DATE_FMT_STR);
		List<Sample> data = storageManager.readSampleData(analyzerId, paramId, startDate, false, endDate, true,
				MAX_DATA_FOR_REQUEST);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(data.size());
		for (Sample s : data) {
			StringBuffer sb = new StringBuffer();
			sb.append(dataDateFmt.format(s.getTimestamp()));
			sb.append(',');
			if (s.getValue() != null)
				sb.append(s.getValue().toString());
			sb.append(',');
			sb.append(s.isNotvalid() ? "1" : "0");
			sb.append(',');
			sb.append(Integer.toHexString(s.getFlags()));
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws StorageException
	 */
	private void getMeanData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, StorageException {
		UUID stationId = parseUUID(request, "station", false);
		UUID analyzerId = parseUUID(request, "analyzer", false);
		String paramId = parseString(request, "parameter", false);
		Integer period = parseInt(request, "period", false);
		Date startDate = parseDate(request, "startDate", false);
		Date endDate = parseDate(request, "endDate", true);
		if (endDate == null)
			endDate = new Date();
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		DateFormat dataDateFmt = new SimpleDateFormat(DATA_DATE_FMT_STR);
		List<MeanValue> data = storageManager.readMeanData(analyzerId, paramId, period, startDate, false, endDate, true,
				MAX_DATA_FOR_REQUEST);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(data.size());
		for (MeanValue m : data) {
			StringBuffer sb = new StringBuffer();
			sb.append(dataDateFmt.format(m.getTimestamp()));
			sb.append(',');
			if (m.getValue() != null)
				sb.append(m.getValue().toString());
			sb.append(',');
			sb.append(m.isNotvalid() ? "1" : "0");
			sb.append(',');
			sb.append(Integer.toHexString(m.getFlags()));
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws StorageException
	 */
	private void getTotalData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, StorageException {
		UUID stationId = parseUUID(request, "station", false);
		UUID analyzerId = parseUUID(request, "analyzer", false);
		String paramId = parseString(request, "parameter", false);
		Integer period = parseInt(request, "period", false);
		Date startDate = parseDate(request, "startDate", false);
		Date endDate = parseDate(request, "endDate", true);
		if (endDate == null)
			endDate = new Date();
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		DateFormat dataDateFmt = new SimpleDateFormat(DATA_DATE_FMT_STR);
		List<TotalValue> data = storageManager.readTotalData(analyzerId, paramId, period, startDate, false, endDate,
				true, MAX_DATA_FOR_REQUEST);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(data.size());
		for (TotalValue t : data) {
			StringBuffer sb = new StringBuffer();
			sb.append(dataDateFmt.format(t.getTimestamp()));
			sb.append(',');
			if (t.getValue() != null)
				sb.append(t.getValue().toString());
			sb.append(',');
			sb.append(t.isNotvalid() ? "1" : "0");
			sb.append(',');
			sb.append(Integer.toHexString(t.getFlags()));
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws StorageException
	 */
	private void getWindData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, StorageException {
		UUID stationId = parseUUID(request, "station", false);
		UUID analyzerId = parseUUID(request, "analyzer", false);
		String paramId = parseString(request, "parameter", false);
		Integer period = parseInt(request, "period", false);
		Date startDate = parseDate(request, "startDate", false);
		Date endDate = parseDate(request, "endDate", true);
		if (endDate == null)
			endDate = new Date();
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		DateFormat dataDateFmt = new SimpleDateFormat(DATA_DATE_FMT_STR);
		List<WindValue> data = storageManager.readWindAggregateData(analyzerId, paramId, period, startDate, false,
				endDate, true, MAX_DATA_FOR_REQUEST);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(data.size());
		for (WindValue wv : data) {
			StringBuffer sb = new StringBuffer();
			sb.append(dataDateFmt.format(wv.getTimestamp()));
			sb.append(',');
			sb.append(objToString(wv.getVectorialSpeed()));
			sb.append(',');
			sb.append(objToString(wv.getVectorialDirection()));
			sb.append(',');
			sb.append(objToString(wv.getStandardDeviation()));
			sb.append(',');
			sb.append(objToString(wv.getScalarSpeed()));
			sb.append(',');
			sb.append(objToString(wv.getGustSpeed()));
			sb.append(',');
			sb.append(objToString(wv.getGustDirection()));
			sb.append(',');
			sb.append(objToString(wv.getCalmsNumberPercent()));
			sb.append(',');
			sb.append(objToString(wv.getCalm()));
			sb.append(',');
			sb.append(wv.isNotvalid() ? "1" : "0");
			sb.append(',');
			sb.append(Integer.toHexString(wv.getFlags()));
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws StorageException
	 */
	private void getDigitalCAData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, StorageException {
		UUID stationId = parseUUID(request, "station", false);
		UUID containerAlarmId = parseUUID(request, "containerAlarm", false);
		String alarmId = parseString(request, "alarm", false);
		Date startDate = parseDate(request, "startDate", false);
		Date endDate = parseDate(request, "endDate", true);
		if (endDate == null)
			endDate = new Date();
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		DateFormat dataDateFmt_ms = new SimpleDateFormat(DATA_DATE_FMT_MS_STR);
		List<BinaryStatus> data = storageManager.readDigitalCAData(containerAlarmId, alarmId, startDate, false, endDate,
				true, MAX_DATA_FOR_REQUEST);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(data.size());
		for (BinaryStatus bs : data) {
			StringBuffer sb = new StringBuffer();
			sb.append(dataDateFmt_ms.format(bs.getTimestamp()));
			sb.append(',');
			sb.append(bs.getStatus() ? "1" : "0");
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws StorageException
	 */
	private void getTriggerCAData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, StorageException {
		UUID stationId = parseUUID(request, "station", false);
		UUID containerAlarmId = parseUUID(request, "containerAlarm", false);
		String alarmId = parseString(request, "alarm", false);
		Date startDate = parseDate(request, "startDate", false);
		Date endDate = parseDate(request, "endDate", true);
		if (endDate == null)
			endDate = new Date();
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		DateFormat dataDateFmt_ms = new SimpleDateFormat(DATA_DATE_FMT_MS_STR);
		List<AlarmStatus> data = storageManager.readTriggerCAData(containerAlarmId, alarmId, startDate, false, endDate,
				true, MAX_DATA_FOR_REQUEST);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(data.size());
		for (AlarmStatus as : data) {
			StringBuffer sb = new StringBuffer();
			sb.append(dataDateFmt_ms.format(as.getTimestamp()));
			sb.append(',');
			sb.append(alarmStatusToString(as));
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws StorageException
	 */
	private void getGpsData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, StorageException {
		UUID stationId = parseUUID(request, "station", false);
		Date startDate = parseDate(request, "startDate", false);
		Date endDate = parseDate(request, "endDate", true);
		Boolean bestHourlyDatumOnlyObj = parseBoolean(request, "hourly", true);
		boolean bestHourlyDatumOnly = bestHourlyDatumOnlyObj == null ? false : bestHourlyDatumOnlyObj;
		if (endDate == null)
			endDate = new Date();
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		DateFormat dataDateFmt = new SimpleDateFormat(DATA_DATE_FMT_STR);
		List<GpsDatum> data = storageManager.readGpsData(startDate, false, endDate, true, MAX_DATA_FOR_REQUEST,
				bestHourlyDatumOnly);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(data.size());
		for (GpsDatum gd : data) {
			StringBuffer sb = new StringBuffer();
			sb.append(dataDateFmt.format(gd.getTimestamp()));
			sb.append(',');
			if (gd.getLatitude() != null)
				sb.append(gd.getLatitude().toString());
			sb.append(',');
			if (gd.getLongitude() != null)
				sb.append(gd.getLongitude().toString());
			sb.append(',');
			if (gd.getAltitude() != null)
				sb.append(gd.getAltitude().toString());
			sb.append(',');
			sb.append(gd.getFix().toString());
			pw.println(sb.toString());
		}
		pw.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws PerifericoException
	 */
	private void getConfigFile(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, PerifericoException {
		UUID stationId = parseUUID(request, "station", false);
		Long lastModifyTime = parseLong(request, "lastModifyTime", false);
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		List<String> cfgFileLines = periferico.getConfigFile(lastModifyTime);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		if (cfgFileLines == null) {
			pw.println("false");
		} else {
			pw.println("true");
			pw.println(cfgFileLines.size());
			for (String line : cfgFileLines)
				pw.println(line);
		}
		pw.close();
	}

	/**
	 * @param br
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws PerifericoException
	 */
	private void setCommonFile(BufferedReader br, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, PerifericoException {
		UUID stationId = null;
		String line = br.readLine();
		try {
			stationId = UUID.fromString(line);
		} catch (IllegalArgumentException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + "stationId");
		}
		Station station = periferico.getStation();
		if (station == null) {
			sendError(response, ERR_CODE_FN_EXEC_FAIL, MSG_PERIF_NOT_INIT);
			return;
		}
		if (!station.getId().equals(stationId)) {
			sendError(response, ERR_CODE_FN_BAD_PARAMS, "Station ID mismatch");
			return;
		}
		Periferico.CommonCfgResult result = periferico.setNewCommonCfg(br);
		if (result == Periferico.CommonCfgResult.OK)
			logger.info("New common configuration set successfully");
		else
			logger.error("Error setting new common configuration: " + result.toString().toLowerCase());
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println(result.toString().toLowerCase());
		pw.close();
		br.close();
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws PerifericoException
	 */
	private void doTimeSync(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ServiceException, PerifericoException {
		String server = parseString(request, "server", false);
		Date copDate = parseDateWithTimeZone(request, "date", false);
		Boolean syncSuccedeed = periferico.doTimeSync(server, copDate);
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("OK");
		pw.println("sync = " + (syncSuccedeed != null ? syncSuccedeed : ""));
		String strDate;
		synchronized (dataTzDateFmt) {
			strDate = dataTzDateFmt.format(new Date());
		}
		pw.println("date = " + strDate);
		pw.close();
	}

	private void sendError(HttpServletResponse response, int code, String message)
			throws ServletException, IOException {
		PrintWriter pw = getPrintWriterFromResponse(response);
		pw.println("ERROR " + code);
		pw.println(message);
		pw.close();
	}

	private String parseString(HttpServletRequest request, String paramName, boolean optional)
			throws ServletException, IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		return paramValue;
	}

	private Integer parseInt(HttpServletRequest request, String paramName, boolean optional)
			throws ServletException, IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			return Integer.parseInt(paramValue);
		} catch (NumberFormatException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	private Boolean parseBoolean(HttpServletRequest request, String paramName, boolean optional)
			throws ServletException, IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		return Boolean.parseBoolean(paramValue);
	}

	private Long parseLong(HttpServletRequest request, String paramName, boolean optional)
			throws ServletException, IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			return Long.parseLong(paramValue);
		} catch (NumberFormatException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	private Date parseDate(HttpServletRequest request, String paramName, boolean optional)
			throws ServletException, IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			synchronized (requestDateFmt) {
				return requestDateFmt.parse(paramValue);
			}
		} catch (ParseException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	private Date parseDateWithTimeZone(HttpServletRequest request, String paramName, boolean optional)
			throws ServletException, IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			synchronized (requestTzDateFmt) {
				return requestTzDateFmt.parse(paramValue);
			}
		} catch (ParseException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	private UUID parseUUID(HttpServletRequest request, String paramName, boolean optional)
			throws ServletException, IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			return UUID.fromString(paramValue);
		} catch (IllegalArgumentException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	private PrintWriter getPrintWriterFromResponse(HttpServletResponse response)
			throws UnsupportedEncodingException, IOException {
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		return new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"), true);
	}

	private static String objToString(Object obj) {
		if (obj == null)
			return "";
		return obj.toString();
	}

	private static String binaryStatusToString(BinaryStatus bs) {
		if (bs == null)
			return "";
		if (bs.getStatus())
			return "1";
		return "0";
	}

	private static String alarmStatusToString(AlarmStatus as) {
		if (as == null)
			return "";
		if (as.getStatus() == AlarmStatus.Status.OK)
			return "OK";
		if (as.getStatus() == AlarmStatus.Status.ALARM)
			return "A";
		if (as.getStatus() == AlarmStatus.Status.ALARM_HIGH)
			return "AH";
		if (as.getStatus() == AlarmStatus.Status.ALARM_LOW)
			return "AL";
		if (as.getStatus() == AlarmStatus.Status.WARNING)
			return "W";
		if (as.getStatus() == AlarmStatus.Status.WARNING_HIGH)
			return "WH";
		if (as.getStatus() == AlarmStatus.Status.WARNING_LOW)
			return "WL";
		return "?";
	}

	private static String configStatusToString(Config.Status cs) {
		if (cs == null)
			return "";
		if (cs == Config.Status.MISSING)
			return "MISSING";
		else if (cs == Config.Status.PARSE_ERROR)
			return "CORRUPTED";
		else if (cs == Config.Status.UNCHECKED)
			return "NO_CHK";
		else if (cs == Config.Status.CHECK_ERROR)
			return "CHK_ERR";
		else if (cs == Config.Status.OK)
			return "OK";
		return "?";
	}

}
