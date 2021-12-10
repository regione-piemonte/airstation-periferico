/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: implementation of the functions called by the user interface
 * Change log:
 *   2008-02-26: initial version
 * ----------------------------------------------------------------------------
 * $Id: PerifericoUIServiceImpl.java,v 1.310 2015/10/26 17:25:44 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */

package it.csi.periferico.ui.server;

import it.csi.periferico.Periferico;
import it.csi.periferico.PerifericoException;
import it.csi.periferico.PerifericoStatus;
import it.csi.periferico.PerifericoUtil;
import it.csi.periferico.acqdrivers.DriverManager;
import it.csi.periferico.acqdrivers.itf.AnalyzerFault;
import it.csi.periferico.acqdrivers.itf.CustomCommand;
import it.csi.periferico.acqdrivers.itf.DriverCallback;
import it.csi.periferico.acqdrivers.itf.FaultValue;
import it.csi.periferico.acqdrivers.itf.PortType;
import it.csi.periferico.acqdrivers.itf.UnboundDriverException;
import it.csi.periferico.boards.AISubdevice;
import it.csi.periferico.boards.AnalogInput;
import it.csi.periferico.boards.Board;
import it.csi.periferico.boards.BoardDescriptor;
import it.csi.periferico.boards.BoardDescriptors;
import it.csi.periferico.boards.BoardList;
import it.csi.periferico.boards.BoardManagementForUI;
import it.csi.periferico.boards.BoardsException;
import it.csi.periferico.boards.DIOSubdevice;
import it.csi.periferico.boards.DISubdevice;
import it.csi.periferico.boards.DOSubdevice;
import it.csi.periferico.boards.DigitalIO;
import it.csi.periferico.boards.DigitalInput;
import it.csi.periferico.boards.DigitalOutput;
import it.csi.periferico.boards.DriverParam;
import it.csi.periferico.boards.DriverParamDescriptor;
import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.boards.IOUser;
import it.csi.periferico.boards.PCIBoard;
import it.csi.periferico.boards.PCIBoardDescriptor;
import it.csi.periferico.boards.PCIBoardVersion;
import it.csi.periferico.boards.PCIDevice;
import it.csi.periferico.comm.ConnectionParams;
import it.csi.periferico.comm.CopClient;
import it.csi.periferico.config.Config;
import it.csi.periferico.config.LoginCfg;
import it.csi.periferico.config.Password;
import it.csi.periferico.config.SetConfigResult;
import it.csi.periferico.config.common.AlarmName;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.Parameter;
import it.csi.periferico.config.common.converters.Converter;
import it.csi.periferico.core.Alarm;
import it.csi.periferico.core.AlarmStatus;
import it.csi.periferico.core.AnalogItfAnalyzer;
import it.csi.periferico.core.Analyzer;
import it.csi.periferico.core.Analyzer.Status;
import it.csi.periferico.core.Analyzer.Type;
import it.csi.periferico.core.AvgAnalyzer;
import it.csi.periferico.core.AvgElement;
import it.csi.periferico.core.AvgElementItf;
import it.csi.periferico.core.BinaryStatus;
import it.csi.periferico.core.ContainerAlarm;
import it.csi.periferico.core.Correction;
import it.csi.periferico.core.CounterElement;
import it.csi.periferico.core.DataPortAlarm;
import it.csi.periferico.core.DataPortAnalyzer;
import it.csi.periferico.core.DataPortAvgElement;
import it.csi.periferico.core.DataPortElement;
import it.csi.periferico.core.DataValidAlarm;
import it.csi.periferico.core.DigitalAlarm;
import it.csi.periferico.core.DigitalContainerAlarm;
import it.csi.periferico.core.Element;
import it.csi.periferico.core.GenericSampleElement;
import it.csi.periferico.core.IOItfAnalyzer;
import it.csi.periferico.core.IntegerStatus;
import it.csi.periferico.core.MeanValue;
import it.csi.periferico.core.ObservableAnalyzer;
import it.csi.periferico.core.ObservableSampleElement;
import it.csi.periferico.core.RainAnalyzer;
import it.csi.periferico.core.Sample;
import it.csi.periferico.core.SampleAnalyzer;
import it.csi.periferico.core.SampleElement;
import it.csi.periferico.core.SampleValues;
import it.csi.periferico.core.ScalarAggregateValue;
import it.csi.periferico.core.ScalarElement;
import it.csi.periferico.core.Station;
import it.csi.periferico.core.TotalValue;
import it.csi.periferico.core.TriggerAlarm;
import it.csi.periferico.core.TriggerContainerAlarm;
import it.csi.periferico.core.ValidationFlag;
import it.csi.periferico.core.WindAnalyzer;
import it.csi.periferico.core.WindElement;
import it.csi.periferico.core.WindValue;
import it.csi.periferico.gps.GpsDatum;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;
import it.csi.periferico.ui.client.ConfigResults;
import it.csi.periferico.ui.client.DgtAnalyzerException;
import it.csi.periferico.ui.client.PerifericoUIService;
import it.csi.periferico.ui.client.SessionExpiredException;
import it.csi.periferico.ui.client.UserParamsException;
import it.csi.periferico.ui.client.data.AnalogAvgElementInfo;
import it.csi.periferico.ui.client.data.AnalogElementInfo;
import it.csi.periferico.ui.client.data.DigitalAvgElementInfo;
import it.csi.periferico.ui.client.data.DigitalElementInfo;
import it.csi.periferico.ui.client.data.ElementInfo;
import it.csi.periferico.ui.client.data.RainElementInfo;
import it.csi.periferico.ui.client.data.SampleElementInfo;
import it.csi.periferico.ui.client.data.ScalarElementInfo;
import it.csi.periferico.ui.client.data.WindElementInfo;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * This class implements the server-side code that extends RemoteServiceServlet
 * and implements the defined interface.
 * 
 * @author isabella.vespa@csi.it
 * 
 */

public class PerifericoUIServiceImpl extends RemoteServiceServletWithProxyPatch
		implements PerifericoUIService {

	private PropertyUtilServer propertyUtil;

	private static final long serialVersionUID = 382648489447778696L;

	private static final String SESSION_ENDED = "session_ended";

	public static final String ONE_DAY = "ONE_DAY";

	public static final String ONE_MONTH = "ONE_MONTH";

	public static final String ONE_YEAR = "ONE_YEAR";

	public static final String HALF_DAY = "HALF_DAY";

	public static final String DECIMAL_SEPARATOR = ".";

	public static final String NETWORK = "NETWORK";

	public static final String SERIAL = "SERIAL";

	static final String LINE = "-----------------------------------------------------";

	private static Logger logger = Logger.getLogger("uiservice."
			+ PerifericoUIServiceImpl.class.getSimpleName());

	private static final String DATE_TIME_FMT = "dd/MM/yyyy HH:mm";

	private static final String DATE_FMT = "dd/MM/yyyy";

	private Map<String, UIDriverCallback<List<String>>> mapCommandCallbacks;

	public PerifericoUIServiceImpl() {
		mapCommandCallbacks = new HashMap<String, UIDriverCallback<List<String>>>();
		try {
			propertyUtil = new PropertyUtilServer(
					"it/csi/periferico/ui/server/MessageBundleService");
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * Returns the current session
	 * 
	 * @return The current Session
	 * 
	 */
	private HttpSession getSession() {

		// Get the current request and then return its session

		return this.getThreadLocalRequest().getSession();

	}

	private Config getConfig() throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		return config;
	}

	private String getLocale() {
		return (String) getSession().getAttribute("locale");
	}

	public Boolean isToSave() {
		// TODO scommentare quando si effettua il grigettamento del pulsante
		// salva
		/*
		 * if (getSession().getAttribute("toSave") == null)
		 * getSession().setAttribute("toSave", new Boolean(false).toString());
		 * 
		 * return new Boolean(((String)getSession().getAttribute("toSave")));
		 */
		return new Boolean(true);
	}

	private void setIsToSave(Boolean isToSave) {
		// TODO scommentare quando si effettua il grigettamento del pulsante
		// salva
		// getSession().setAttribute("toSave", isToSave.toString());
		getSession().setAttribute("toSave", "true");
	}

	public Boolean verifySessionEnded() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			return new Boolean(true);
		else
			return new Boolean(false);
	}

	/*
	 * Methods for loading and saving station information and connection
	 * information
	 */

	public String getNickname() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return config.getStation().getShortName();
		return (SESSION_ENDED);
	}

	public String getName() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return config.getStation().getName();
		return (SESSION_ENDED);
	}

	public String getLocation() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return config.getStation().getLocation();
		return (SESSION_ENDED);
	}

	public String getAddress() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return config.getStation().getAddress();
		return (SESSION_ENDED);
	}

	public String getCity() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return config.getStation().getCity();
		return (SESSION_ENDED);
	}

	public String getProvince() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return config.getStation().getProvince();
		return (SESSION_ENDED);
	}

	public String getUserNotes() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return config.getStation().getUserNotes();
		return (SESSION_ENDED);
	}

	public String getUseGps() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return new Boolean(config.getStation().isGpsInstalled()).toString();
		return (SESSION_ENDED);
	}

	public String getCopIp() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return config.getStation().getConnectionParams().getCopIP();
		return (SESSION_ENDED);
	}

	public String getMaxConnectionRetry() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return new Integer(config.getStation().getConnectionParams()
					.getMaxConnectionAttempts()).toString();
		return (SESSION_ENDED);
	}

	public String getTimeOut() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return new Integer(config.getStation().getConnectionParams()
					.getConnectTimeout()).toString();
		return (SESSION_ENDED);
	}

	public String[] getListConnectionType() {
		List<String> list = ConnectionParams.getSupportedDeviceTypes();
		String[] result = new String[list.size()];
		Iterator<String> iterator = list.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			result[i] = iterator.next();
			i++;
		}
		return result;
	}

	public String createNewConnectionParam(String type) {
		ConnectionParams connParam = ConnectionParams.getInstance(type);
		return new Integer(connParam.getConnectTimeout()).toString();
	}

	public String getConnectionType() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			return config.getStation().getConnectionParams().getDeviceType();
		}
		return (SESSION_ENDED);
	}

	public String isOutgoingCallEnabled() {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			return new Boolean(config.getStation().getConnectionParams()
					.isOutgoingCallEnabled()).toString();
		return (SESSION_ENDED);
	}

	@Override
	public boolean verifySameStationConfig(String[] fieldsValue)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		try {
			boolean resultValue = false;
			Station station = config.getStation();
			resultValue = station.isSameConfig(fieldsValue[0], fieldsValue[1],
					fieldsValue[2], fieldsValue[3], fieldsValue[4],
					fieldsValue[5], fieldsValue[6],
					new Boolean(fieldsValue[10]).booleanValue());
			if (resultValue) {
				ConnectionParams connectionParams = config.getStation()
						.getConnectionParams();
				if (fieldsValue[12].equals(connectionParams.getDeviceType())) {
					resultValue = connectionParams.isSameConfig(fieldsValue[7],
							new Boolean(fieldsValue[8]).booleanValue(),
							new Integer(fieldsValue[9]).intValue(),
							new Integer(fieldsValue[11]).intValue());
				} else {
					// configuration is modified because the type connection is
					// changed
					resultValue = false;
				}
			}
			return resultValue;
		} catch (NumberFormatException nfe) {
			// If numbers are not parsable, the configuration contained in the
			// user interface is certainly different from application's config.
			return false;
		}
	}// end verifySameStationConfig

	public void setVerifyStationFields(String[] stationFields)
			throws SessionExpiredException, UserParamsException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();

		Station station = config.getStation();
		try {
			station.setConfig(stationFields[0], stationFields[1],
					stationFields[2], stationFields[3], stationFields[4],
					stationFields[5], stationFields[6], new Boolean(
							stationFields[7]).booleanValue());
		} catch (ConfigException ce) {
			throw new UserParamsException(ce.getLocalizedMessage(getLocale()));
		}
		setIsToSave(true);
	}// end setVerifyStationFields

	public String setVerifyConnectionStationField(String[] connectionInfo) {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			return SESSION_ENDED;
		try {
			ConnectionParams connectionParams = config.getStation()
					.getConnectionParams();
			if (connectionInfo[4].equals(connectionParams.getDeviceType())) {
				connectionParams.setConfig(connectionInfo[0], new Boolean(
						connectionInfo[1]).booleanValue(), new Integer(
						connectionInfo[2]).intValue(), new Integer(
						connectionInfo[3]).intValue());
			} else {
				connectionParams = ConnectionParams
						.getInstance(connectionInfo[4]);
				connectionParams.setConfig(connectionInfo[0], new Boolean(
						connectionInfo[1]).booleanValue(), new Integer(
						connectionInfo[2]).intValue(), new Integer(
						connectionInfo[3]).intValue());
				config.getStation().setConnectionParams(connectionParams);
			}
			return null;
		} catch (NumberFormatException nfe) {
			return propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"not_character");
		}
	}// end setVerifyConnectionStationField

	/*
	 * Methods for loading and saving global configuration
	 */

	public void getConfig(String locale) {
		Periferico perifericoApp = Periferico.getInstance();
		Config config = perifericoApp.getConfigCopy();
		// TODO: verificare con config.checkconfig se la configurazione e'
		// corretta
		// set config session attribute
		getSession().setAttribute("config", config);
		getSession().setAttribute("toSave", new Boolean(false).toString());
		setLocale(locale);
	}

	public void setLocale(String locale) {
		getSession().setAttribute("locale", locale);
	}

	public String[] setStationCfg(Boolean saveForced, String comment,
			String user) {
		String[] resultString = null;
		Periferico perifericoApp = Periferico.getInstance();
		Password pwd = (Password) getSession().getAttribute("password");
		if (pwd == null || pwd.getType() == null
				|| !pwd.getType().trim().equalsIgnoreCase("readwrite")) {
			resultString = new String[1];
			resultString[0] = ConfigResults.SAVE_FORBIDDEN;
			return (resultString);
		}
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			config.setComment(comment);
			config.setAuthor(user);
			SetConfigResult setConfigResult = null;
			if (saveForced == null || !saveForced) {
				setConfigResult = perifericoApp.setNewConfig(config);
			} else {
				setConfigResult = perifericoApp
						.setNewConfig(config, saveForced);
			}

			// logger.debug("Set config result:"
			// + setConfigResult.getResult());
			// printConfig(config);

			if (setConfigResult.getResult() == SetConfigResult.Result.ACTIVATED
					|| setConfigResult.getResult() == SetConfigResult.Result.ACTIVATION_ERROR) {
				config = perifericoApp.getConfigCopy();
				getSession().setAttribute("config", config);
			}

			// set the resultString
			if (setConfigResult.getResult() == SetConfigResult.Result.ACTIVATED) {
				resultString = new String[1];
				resultString[0] = ConfigResults.ACTIVATED;
			}
			if (setConfigResult.getResult() == SetConfigResult.Result.ACTIVATION_ERROR) {
				if (null != setConfigResult.getError()) {
					resultString = new String[2];
					resultString[0] = ConfigResults.ACTIVATION_ERROR;
					resultString[1] = setConfigResult.getError()
							.getLocalizedMessage(getLocale());
				} else {
					resultString = new String[1];
					resultString[0] = ConfigResults.ACTIVATION_ERROR;
				}
			}
			if (setConfigResult.getResult() == SetConfigResult.Result.OBSOLETE) {
				if (null != setConfigResult.getError()) {
					resultString = new String[2];
					resultString[0] = ConfigResults.OBSOLETE;
					resultString[1] = setConfigResult.getError()
							.getLocalizedMessage(getLocale());
				} else {
					resultString = new String[1];
					resultString[0] = ConfigResults.OBSOLETE;
				}
			}
			if (setConfigResult.getResult() == SetConfigResult.Result.HISTORIC) {
				if (null != setConfigResult.getError()) {
					resultString = new String[2];
					resultString[0] = ConfigResults.HISTORIC;
					resultString[1] = setConfigResult.getError()
							.getLocalizedMessage(getLocale());
				} else {
					resultString = new String[1];
					resultString[0] = ConfigResults.HISTORIC;
				}
			}
			if (setConfigResult.getResult() == SetConfigResult.Result.CHECK_ERROR) {
				if (null != setConfigResult.getError()) {
					resultString = new String[2];
					resultString[0] = ConfigResults.CHECK_ERROR;
					resultString[1] = setConfigResult.getError()
							.getLocalizedMessage(getLocale());
				} else {
					resultString = new String[1];
					resultString[0] = ConfigResults.CHECK_ERROR;
				}
			}
		} else {
			resultString = new String[1];
			resultString[0] = SESSION_ENDED;
		}
		return (resultString);
	}

	// private void printConfig(Config config) {
	// logger.debug("NEW comment:" + config.getComment());
	// logger.debug("NEW author:" + config.getAuthor());
	// logger.debug("NEW station.nickname:"
	// + config.getStation().getShortName());
	// logger.debug("NEW station.name:" + config.getStation().getName());
	// logger.debug("NEW station.location:"
	// + config.getStation().getLocation());
	// logger.debug("NEW station.address:" + config.getStation().getAddress());
	// logger.debug("NEW station.city:" + config.getStation().getCity());
	// logger.debug("NEW station.province:"
	// + config.getStation().getProvince());
	// logger.debug("NEW station.usernotes:"
	// + config.getStation().getUserNotes());
	// logger.debug("NEW station.copip:"
	// + config.getStation().getConnectionParams().getCopIP());
	// logger.debug("NEW station.maxconnparams:"
	// + config.getStation().getConnectionParams()
	// .getMaxConnectionAttempts());
	// logger.debug("NEW station.outgoingcallenabled:"
	// + config.getStation().getConnectionParams()
	// .isOutgoingCallEnabled());
	// logger.debug("NEW station.getConnectTimeout:"
	// + config.getStation().getConnectionParams().getConnectTimeout());
	// logger.debug("NEW station.getDeviceType():"
	// + config.getStation().getConnectionParams().getDeviceType());
	// List<ContainerAlarm> alarmList = config.getStation().getContainer()
	// .getListAlarm();
	// for (int i = 0; i < alarmList.size(); i++) {
	// ContainerAlarm ca = (ContainerAlarm) alarmList.get(i);
	// Alarm alarm = ca.getAlarm();
	// logger.debug("NEW alarm alarmnameid:" + alarm.getAlarmNameId());
	// logger.debug("NEW alarm description:" + ca.getDescription());
	// if (alarm instanceof TriggerAlarm) {
	// logger.debug("NEW alarm threshold high:"
	// + ((TriggerAlarm) (alarm)).getAlarmThresholdHigh());
	// logger.debug("NEW alarm threshold low:"
	// + ((TriggerAlarm) (alarm)).getAlarmThresholdLow());
	// logger.debug("NEW alarm warning high:"
	// + ((TriggerAlarm) (alarm)).getWarningThresholdHigh());
	// logger.debug("NEW alarm warning low:"
	// + ((TriggerAlarm) (alarm)).getWarningThresholdLow());
	// } else if (alarm instanceof DigitalAlarm) {
	// logger.debug("NEW alarm active high:"
	// + ((DigitalAlarm) (alarm)).isActiveHigh());
	// }
	// }
	// List<Analyzer> analyzerList = config.getStation().getListAnalyzer();
	// for (int i = 0; i < analyzerList.size(); i++) {
	// Analyzer analyzer = (Analyzer) analyzerList.get(i);
	// logger.debug("NEW analyzer id:" + analyzer.getIdAsString());
	// logger.debug("NEW analyzer name:" + analyzer.getName());
	// logger.debug("NEW analyzer brand:" + analyzer.getBrand());
	// logger.debug("NEW analyzer model:" + analyzer.getModel());
	// logger.debug("NEW analyzer description:"
	// + analyzer.getDescription());
	// logger.debug("NEW analyzer serial number:"
	// + analyzer.getSerialNumber());
	// logger.debug("NEW analyzer status:"
	// + analyzer.getStatus().toString());
	// logger.debug("NEW analyzer user notes:" + analyzer.getUserNotes());
	// }
	// List<Board> boardList = config.getBoardList().getBoards();
	// for (int i = 0; i < boardList.size(); i++) {
	// Board board = boardList.get(i);
	// logger.debug("NEW board id:" + board.getIdAsString());
	// logger.debug("NEW board brand:" + board.getBrand());
	// logger.debug("NEW board model:" + board.getModel());
	// logger.debug("NEW board status:" + board.getBoardStatus());
	// if (board instanceof ISABoard) {
	// logger.debug("NEW board ioBase:"
	// + ((ISABoard) (board)).getIoBase());
	// }
	// List<DriverParam> dParamList = board.getDriverParams();
	// for (int j = 0; j < dParamList.size(); j++) {
	// DriverParam dParam = dParamList.get(j);
	// logger.debug("NEW board driver param name:" + dParam.getName());
	// logger.debug("NEW board driver param value:"
	// + dParam.getValue());
	// }
	// List<AISubdevice> aiSubdeviceList = board.getListAISubdevice();
	// for (int k = 0; k < aiSubdeviceList.size(); k++) {
	// AISubdevice aiSubdevice = aiSubdeviceList.get(k);
	// logger.debug("NEW aisubdevice id:" + aiSubdevice.getSubdevice());
	// List<AnalogInput> analogInputList = aiSubdevice.getListAI();
	// for (int z = 0; z < analogInputList.size(); z++) {
	// AnalogInput ai = analogInputList.get(z);
	// logger.debug("NEW ai channel:" + ai.getChannel());
	// if (ai.getBindedIOUser() != null)
	// logger.debug("NEW ai ioUser:"
	// + ai.getBindedIOUser().getBindLabel());
	// }
	// }
	// List<DISubdevice> diSubdeviceList = board.getListDISubdevice();
	// for (int k = 0; k < diSubdeviceList.size(); k++) {
	// DISubdevice diSubdevice = diSubdeviceList.get(k);
	// List<DigitalInput> digitalInputList = diSubdevice.getListDI();
	// for (int z = 0; z < digitalInputList.size(); z++) {
	// DigitalInput di = digitalInputList.get(z);
	// logger.debug("NEW di channel:" + di.getChannel());
	// if (di.getBindedIOUser() != null)
	// logger.debug("NEW di ioUser:"
	// + di.getBindedIOUser().getBindLabel());
	// }
	// }
	// List<DIOSubdevice> dioSubdeviceList = board.getListDIOSubdevice();
	// for (int k = 0; k < dioSubdeviceList.size(); k++) {
	// DIOSubdevice dioSubdevice = dioSubdeviceList.get(k);
	// List<DigitalIO> digitalIOList = dioSubdevice.getListDIO();
	// for (int z = 0; z < digitalIOList.size(); z++) {
	// DigitalIO dIO = digitalIOList.get(z);
	// logger.debug("NEW dIO channel:" + dIO.getChannel());
	// if (dIO.getBindedIOUser() != null)
	// logger.debug("NEW dIO ioUser:"
	// + dIO.getBindedIOUser().getBindLabel());
	// }
	// }
	// List<DOSubdevice> doSubdeviceList = board.getListDOSubdevice();
	// for (int k = 0; k < doSubdeviceList.size(); k++) {
	// DOSubdevice doSubdevice = doSubdeviceList.get(k);
	// List<DigitalOutput> digitalOutputList = doSubdevice.getListDO();
	// for (int z = 0; z < digitalOutputList.size(); z++) {
	// DigitalOutput dO = digitalOutputList.get(z);
	// logger.debug("NEW dO channel:" + dO.getChannel());
	// if (dO.getBindedIOUser() != null)
	// logger.debug("NEW dO ioUser:"
	// + dO.getBindedIOUser().getBindLabel());
	// }
	// }
	// }
	// }

	/*
	 * Methods for loading and save alarm
	 */

	public String[][] getAlarms() throws SessionExpiredException {
		String[][] alarmMatrix = null;
		Periferico perifericoApp = Periferico.getInstance();
		// TODO: capire se qualcosa di questo puo' essere null e controllare
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		List<ContainerAlarm> alarmList = new ArrayList<ContainerAlarm>(config
				.getStation().getContainer().getListAlarm());
		Collections.sort(alarmList, new Comparator<ContainerAlarm>() {
			@Override
			public int compare(ContainerAlarm o1, ContainerAlarm o2) {
				return o1.compareNameAndDescription(o2);
			}
		});
		alarmMatrix = new String[alarmList.size()][6];
		for (int i = 0; i < alarmList.size(); i++) {
			ContainerAlarm ca = (ContainerAlarm) alarmList.get(i);
			AlarmName alarmName = perifericoApp.getCommonCfg().getAlarmName(
					ca.getAlarm().getAlarmNameId());
			alarmMatrix[i][0] = new Boolean(ca.getAlarm().isEnabled())
					.toString();
			alarmMatrix[i][1] = ca.getAlarm().getAlarmNameId();
			alarmMatrix[i][2] = alarmName == null ? "" : alarmName.getName();
			alarmMatrix[i][3] = ca.getDescription();
			alarmMatrix[i][4] = ca.getIdAsString();
			boolean digitalAlarm = false;
			if (ca.getAlarm() instanceof DigitalAlarm)
				digitalAlarm = true;
			alarmMatrix[i][5] = new Boolean(digitalAlarm).toString();
		}

		return alarmMatrix;
	}

	public String[] getAlarmNamesCfg() throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		// TODO: capire se c'e' qualcosa a null
		Periferico perifericoApp = Periferico.getInstance();
		List<String> alarmsList = perifericoApp.getCommonCfg()
				.getAlarmNamesAsStrings();
		String[] alarmsArray = (String[]) alarmsList
				.toArray(new String[alarmsList.size()]);
		return alarmsArray;
	}

	public String[] setNewAlarmId(String alarmNameStr)
			throws SessionExpiredException {
		String[] resultString = null;
		// TODO: capire se c'e' qualcosa a null
		Periferico perifericoApp = Periferico.getInstance();
		AlarmName alarmName = perifericoApp.getCommonCfg()
				.getAlarmNameFromString(alarmNameStr);
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		ContainerAlarm ca = config.getStation().getContainer()
				.makeNewAlarm(alarmName);
		// set new alarm enabled as default
		resultString = new String[8];
		resultString[0] = new Boolean(true).toString();
		resultString[1] = ca.getId().toString();
		resultString[2] = ca.getAlarm().getAlarmNameId();
		resultString[3] = alarmName.getName();
		resultString[4] = "";
		boolean digitalAlarm = ca.getAlarm() instanceof DigitalAlarm;
		// set if is digital alarm or not
		resultString[5] = new Boolean(digitalAlarm).toString();
		resultString[6] = null;
		resultString[7] = digitalAlarm ? Boolean.toString(((DigitalAlarm) ca
				.getAlarm()).isActiveHigh()) : null;

		return resultString;
	}

	public boolean deleteAlarm(String id) throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		return config.getStation().getContainer().deleteAlarm(id);
	}

	public String[] getAlarmDetails(String id) throws SessionExpiredException {
		List<String> detailsList = new ArrayList<String>();
		String[] alarmsArray = null;
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManagementForUI = perifericoApp
				.getBoardManagementForUI();
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		ContainerAlarm ca = config.getStation().getContainer().getAlarm(id);
		AlarmName alarmName = perifericoApp.getCommonCfg().getAlarmName(
				ca.getAlarm().getAlarmNameId());
		detailsList.add(new Boolean(ca.getAlarm().isEnabled()).toString());
		detailsList.add(ca.getId().toString());
		detailsList.add(ca.getAlarm().getAlarmNameId());
		detailsList.add(alarmName.getName());
		detailsList.add(ca.getDescription());
		boolean digitalAlarm = false;
		if (ca.getAlarm() instanceof DigitalAlarm)
			digitalAlarm = true;
		detailsList.add(new Boolean(digitalAlarm).toString());
		if (ca.getAlarm() instanceof DigitalAlarm) {
			DigitalAlarm da = (DigitalAlarm) ca.getAlarm();
			if (da.getBoardBindInfo() == null)
				detailsList.add(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "not_done"));
			else {
				detailsList.add(boardManagementForUI.getBoardBindLabel(
						config.getBoardList(), da.getBoardBindInfo()));
			}
			detailsList.add(new Boolean(da.isActiveHigh()).toString());
		} else if (ca.getAlarm() instanceof TriggerAlarm) {
			Double alarmThresholdHigh = ((TriggerAlarm) (ca.getAlarm()))
					.getAlarmThresholdHigh();
			if (alarmThresholdHigh != null)
				detailsList.add(alarmThresholdHigh.toString());
			else
				detailsList.add("");
			Double alarmThresholdLow = ((TriggerAlarm) (ca.getAlarm()))
					.getAlarmThresholdLow();
			if (alarmThresholdLow != null)
				detailsList.add(alarmThresholdLow.toString());
			else
				detailsList.add("");
			Double warningThresholdHigh = ((TriggerAlarm) (ca.getAlarm()))
					.getWarningThresholdHigh();
			if (warningThresholdHigh != null)
				detailsList.add(warningThresholdHigh.toString());
			else
				detailsList.add("");
			Double warningThresholdLow = ((TriggerAlarm) (ca.getAlarm()))
					.getWarningThresholdLow();
			if (warningThresholdLow != null)
				detailsList.add(warningThresholdLow.toString());
			else
				detailsList.add("");
			String bindLabel = config.getStation().getBindLabelForTriggerAlarm(
					(TriggerAlarm) ca.getAlarm());
			detailsList.add(bindLabel);
		}
		alarmsArray = (String[]) detailsList.toArray(new String[detailsList
				.size()]);

		return alarmsArray;
	}

	public String[][] getConfigurationPage(String start, String end,
			Integer limit) {
		/*
		 * pageParameter:
		 * 
		 * [0][0]: date
		 * 
		 * [1][0]: comment
		 * 
		 * [2][0]: author
		 * 
		 * [3][0]: ipAddress
		 * 
		 * [4][0]: deviceType
		 * 
		 * [5][0] -> [5][...]: listDeviceType
		 * 
		 * [6][0] -> [6][...]: listLocalConfiguration
		 */
		SimpleDateFormat sdfDay = new SimpleDateFormat(DATE_FMT);
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FMT);
		String[][] pageParameter = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Periferico perifericoApp = Periferico.getInstance();
			Date startDate = null;
			Date endDate = null;
			try {
				startDate = sdfDay.parse(start);
			} catch (ParseException e) {
			}
			try {
				Calendar cal = new GregorianCalendar();
				cal.setTime(sdfDay.parse(end));
				cal.add(Calendar.HOUR_OF_DAY, 24);
				cal.add(Calendar.MINUTE, -1);
				endDate = cal.getTime();
			} catch (ParseException e) {
			}
			List<Date> historic = perifericoApp.readHistoricConfigsList(
					startDate, endDate, limit);
			List<String> deviceTypeList = ConnectionParams
					.getSupportedDeviceTypes();
			pageParameter = new String[7][Math.max(deviceTypeList.size(),
					historic.size())];

			pageParameter[0][0] = (config.getDate() != null ? sdf.format(config
					.getDate()) : "");
			pageParameter[1][0] = (config.getComment() != null ? config
					.getComment() : "");
			pageParameter[2][0] = (config.getAuthor() != null ? config
					.getAuthor() : "");
			pageParameter[3][0] = (config.getStation().getAddress() != null ? config
					.getStation().getConnectionParams().getCopIP()
					: "");
			pageParameter[4][0] = (config.getStation().getConnectionParams()
					.getDeviceType() != null ? config.getStation()
					.getConnectionParams().getDeviceType() : "");

			Iterator<String> iterDeviceType = deviceTypeList.iterator();
			int i = 0;
			while (iterDeviceType.hasNext()) {
				pageParameter[5][i] = iterDeviceType.next();
				i++;
			}

			Iterator<Date> iterator = historic.iterator();
			i = 0;
			while (iterator.hasNext()) {
				// write dateformat of date
				pageParameter[6][i] = sdf.format(iterator.next());
				i++;
			}// end while

		} else {
			pageParameter = new String[1][1];
			pageParameter[0][0] = SESSION_ENDED;
		}
		return pageParameter;
	}// end getListHistoricConf

	public String[] connectToCop(String host, String deviceType)
			throws SessionExpiredException, UserParamsException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Periferico perifericoApp = Periferico.getInstance();
		try {
			perifericoApp.connectToCop(deviceType, host);
		} catch (PerifericoException pe) {
			logger.error("Cannot connect to COP", pe);
			throw new UserParamsException(
					pe.getLocalizedMessage((String) getSession().getAttribute(
							"locale")));
		}
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		Integer port = cc.getCopServicePort();
		if (port == null)
			port = Periferico.DEFAULT_COP_PORT;
		CopClient copClient = new CopClient(host, port);
		getSession().removeAttribute("copClient");
		getSession().setAttribute("copClient", copClient);
		List<String> stationList = null;
		try {
			stationList = copClient.getStationNames();
		} catch (PerifericoException pe) {
			logger.error("Cannot read station list from COP", pe);
			try {
				perifericoApp.disconnect();
			} catch (Exception ex) {
				logger.error("Disconnect error", ex);
			}
			throw new UserParamsException(
					pe.getLocalizedMessage((String) getSession().getAttribute(
							"locale")));
		}
		if (stationList.isEmpty()) {
			try {
				perifericoApp.disconnect();
			} catch (Exception ex) {
				logger.error("Disconnect error", ex);
			}
			throw new UserParamsException(propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"), "no_station"));
		}
		String[] returnList = new String[stationList.size()];
		Iterator<String> iterator = stationList.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			returnList[i] = iterator.next();
			i++;
		}
		return returnList;
	}

	public String[] loadListConf(String stationName, String start, String end,
			Integer limit) throws SessionExpiredException, UserParamsException {
		SimpleDateFormat sdfDay = new SimpleDateFormat(DATE_FMT);
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Date startDate = null;
		Date endDate = null;
		if (start != null && !start.trim().isEmpty()) {
			try {
				startDate = sdfDay.parse(start);
			} catch (ParseException e) {
				throw new UserParamsException(propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"start_date_error"));
			}
		}
		if (end != null && !end.trim().isEmpty()) {
			try {
				Calendar cal = new GregorianCalendar();
				cal.setTime(sdfDay.parse(end));
				cal.add(Calendar.HOUR_OF_DAY, 24);
				cal.add(Calendar.MINUTE, -1);
				endDate = cal.getTime();
			} catch (ParseException e) {
				throw new UserParamsException(propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"end_date_error"));
			}
		}
		Periferico perifericoApp = Periferico.getInstance();
		CopClient copClient = (CopClient) getSession()
				.getAttribute("copClient");
		List<String> confList = null;
		try {
			if (!perifericoApp.isConnectedToCop() || copClient == null) {
				throw new UserParamsException(propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"not_connected"));
			}
			confList = copClient.getStationConfigs(stationName, startDate,
					endDate, limit);
		} catch (PerifericoException pe) {
			logger.error("Cannot read configuration list from "
					+ "COP for station " + stationName, pe);
			try {
				perifericoApp.disconnect();
			} catch (Exception ex) {
				logger.error("Disconnect error", ex);
			}
			throw new UserParamsException(
					pe.getLocalizedMessage((String) getSession().getAttribute(
							"locale")));
		}
		if (confList.isEmpty()) {
			try {
				perifericoApp.disconnect();
			} catch (Exception ex) {
				logger.error("Disconnect error", ex);
			}
			throw new UserParamsException(propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"no_configuration"));
		}
		String[] returnList = new String[confList.size()];
		Iterator<String> iterator = confList.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			returnList[i] = iterator.next();
			i++;
		}
		return returnList;
	}

	public String[] readConf(String stationName, String configName)
			throws SessionExpiredException, UserParamsException {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FMT);
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Periferico perifericoApp = Periferico.getInstance();
		CopClient copClient = (CopClient) getSession()
				.getAttribute("copClient");
		Config newConf = null;
		try {
			if (!perifericoApp.isConnectedToCop() || copClient == null) {
				throw new UserParamsException(propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"not_connected"));
			}
			newConf = copClient.readConfig(stationName, configName);
		} catch (PerifericoException pe) {
			logger.error("Cannot read configuration " + configName + " from "
					+ "COP for station " + stationName, pe);
			throw new UserParamsException(
					pe.getLocalizedMessage((String) getSession().getAttribute(
							"locale")));
		} finally {
			try {
				perifericoApp.disconnect();
			} catch (Exception ex) {
				logger.error("Disconnect error", ex);
			}
		}
		getSession().removeAttribute("config");
		getSession().setAttribute("config", newConf);

		/*
		 * returnParameter:
		 * 
		 * [0][0]: date
		 * 
		 * [1][0]: comment
		 * 
		 * [2][0]: author
		 */
		String[] returnParameter = new String[3];
		returnParameter[0] = (newConf.getDate() != null ? sdf.format(newConf
				.getDate()) : "");
		returnParameter[1] = (newConf.getComment() != null ? newConf
				.getComment() : "");
		returnParameter[2] = (newConf.getAuthor() != null ? newConf.getAuthor()
				: "");

		return returnParameter;
	}

	public String[] loadHistoricConf(String date) {
		/*
		 * result:
		 * 
		 * [0] : date
		 * 
		 * [1] : comment
		 * 
		 * [2] : author
		 */
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FMT);
		String[] result = null;
		Periferico perifericoApp = Periferico.getInstance();
		try {
			Config newConfig = perifericoApp.getHistoricConfig(sdf.parse(date));
			getSession().removeAttribute("config");
			getSession().setAttribute("config", newConfig);
			result = new String[3];
			result[0] = date;
			result[1] = newConfig.getComment();
			result[2] = newConfig.getAuthor();
		} catch (ParseException e) {
			result = new String[2];
			result[0] = date;
			result[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "not_loaded");
			logger.error(e);
		} catch (Exception e) {
			result = new String[2];
			result[0] = date;
			result[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "not_loaded");
			logger.error("Error loading historic configuration", e);
		}

		return result;
	}// end loadHistoricConf

	public String[][] getBindableSampleElements() {
		String resStrings[][] = null;
		List<ObservableSampleElement> elementList = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			elementList = config.getStation().getTriggerAlarmBindableElements();
			resStrings = new String[elementList.size()][2];
			for (int i = 0; i < elementList.size(); i++) {
				resStrings[i][0] = elementList.get(i).getBindLabel();
				resStrings[i][1] = elementList.get(i).getBindIdentifier();
			}
		} else {
			resStrings = new String[1][1];
			resStrings[0][0] = SESSION_ENDED;
		}
		return resStrings;
	} // end getBindableSampleElements

	public String[] bindTriggerAlarm(String alarmId, String elementId,
			String elementLbl) {
		String[] resultString = new String[2];
		resultString[0] = elementLbl;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			ContainerAlarm ca = config.getStation().getContainer()
					.getAlarm(alarmId);
			boolean resValue = config.getStation().bindTriggerAlarmById(
					(TriggerAlarm) ca.getAlarm(), elementId);
			resultString[1] = new Boolean(resValue).toString();
		} else
			resultString[1] = SESSION_ENDED;
		return (resultString);
	} // end bindTriggerAlarm

	public void unbindTriggerAlarm(String id) throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		ContainerAlarm ca = config.getStation().getContainer().getAlarm(id);
		config.getStation().unbindTriggerAlarm((TriggerAlarm) ca.getAlarm());
	}

	public String setVerifyAlarmFields(String id, String[] alarmFields)
			throws SessionExpiredException {
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		try {
			ContainerAlarm ca = config.getStation().getContainer().getAlarm(id);
			Alarm alarm = ca.getAlarm();
			if (alarm instanceof DigitalAlarm) {
				if (alarmFields.length == 3) {
					// case of digital alarm
					DigitalAlarm digitalAlarm = (DigitalAlarm) alarm;
					ca.setConfig(alarmFields[1]);
					digitalAlarm.setConfig(
							new Boolean(alarmFields[0]).booleanValue(),
							new Boolean(alarmFields[2]).booleanValue());
				} else {
					// TODO capire se e' il caso di verificare o no il caso
					// in cui ci sia un errore software
					// non dovrebbe mai capitare
				}

			} else if (alarm instanceof TriggerAlarm) {
				if (alarmFields.length == 6) {
					try {
						// case of trigger alarm
						TriggerAlarm triggerAlarm = (TriggerAlarm) alarm;
						ca.setConfig(alarmFields[1]);
						triggerAlarm.setConfig(
								new Boolean(alarmFields[0]).booleanValue(),
								string2Double(alarmFields[2]),
								string2Double(alarmFields[4]),
								string2Double(alarmFields[5]),
								string2Double(alarmFields[3]));
					} catch (NumberFormatException nfe) {
						resultString = propertyUtil.getProperty(
								(String) getSession().getAttribute("locale"),
								"not_character");
					}
				} else {
					// TODO capire se e' il caso di verificare o no il caso
					// in cui ci sia un errore software
					// non dovrebbe mai capitare
				}

			}

		} catch (ConfigException ex) {
			resultString = ex.getLocalizedMessage(getLocale());
		}
		return resultString;
	}

	public void verifyInsertNewAlarm(String id) throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		ContainerAlarm ca = config.getStation().getContainer().getAlarm(id);
		if (config.getStation().getContainer().isNewAlarm(ca)) {
			config.getStation().getContainer().insertNewAlarm();
		}
	}// end verifyInsertNewAlarm

	@Override
	public boolean verifySameAlarmConfig(String id, String[] alarmFields)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		boolean resultValue = false;
		try {
			ContainerAlarm ca = config.getStation().getContainer().getAlarm(id);
			Alarm alarm = ca.getAlarm();
			if (alarm instanceof DigitalAlarm) {
				if (alarmFields.length == 3) {
					// case of digital alarm
					DigitalAlarm digitalAlarm = (DigitalAlarm) alarm;
					resultValue = ca.isSameConfig(alarmFields[1])
							&& digitalAlarm.isSameConfig(new Boolean(
									alarmFields[0]).booleanValue(),
									new Boolean(alarmFields[2]).booleanValue());
				} else {
					// TODO capire se e' il caso di verificare o no il caso
					// in cui ci sia un errore software
					// non dovrebbe mai capitare
				}

			} else if (alarm instanceof TriggerAlarm) {
				if (alarmFields.length == 6) {
					// case of trigger alarm
					TriggerAlarm triggerAlarm = (TriggerAlarm) alarm;
					resultValue = ca.isSameConfig(alarmFields[1])
							&& triggerAlarm.isSameConfig(new Boolean(
									alarmFields[0]).booleanValue(),
									string2Double(alarmFields[2]),
									string2Double(alarmFields[4]),
									string2Double(alarmFields[5]),
									string2Double(alarmFields[3]));
				} else {
					// TODO capire se e' il caso di verificare o no il caso
					// in cui ci sia un errore software
					// non dovrebbe mai capitare
				}

			}
			return resultValue;
		} catch (NumberFormatException nfe) {
			// If numbers are not parsable, the configuration contained in the
			// user interface is certainly different from application's config.
			return false;
		}
	} // end verifySameAlarmConfig

	/*
	 * Methods for manage analyzers
	 */

	public String[][] getAnalyzersCfg() {
		String[][] analizersMatrix = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			List<Analyzer> analyzersList = config.getStation()
					.getListAnalyzer();
			List<Analyzer> orderedList = new ArrayList<Analyzer>(analyzersList);
			Collections.sort(orderedList, new Comparator<Analyzer>() {
				@Override
				public int compare(Analyzer o1, Analyzer o2) {
					return o1.compareName(o2);
				}
			});
			analizersMatrix = new String[orderedList.size()][6];
			for (int i = 0; i < orderedList.size(); i++) {
				Analyzer analyzer = orderedList.get(i);
				analizersMatrix[i][0] = analyzer.getId().toString();
				analizersMatrix[i][1] = analyzer.getType().toString();
				analizersMatrix[i][2] = analyzer.getStatus().toString();
				analizersMatrix[i][3] = analyzer.getName();
				analizersMatrix[i][4] = analyzer.getBrand();
				analizersMatrix[i][5] = analyzer.getModel();
			}
		} else {
			analizersMatrix = new String[1][1];
			analizersMatrix[0][0] = SESSION_ENDED;
		}

		return analizersMatrix;
	}

	public String[][] getAnalyzersTypeCfg() {
		String[][] analizersTypeMatrix = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Analyzer.Type[] analyzersTypeList = Analyzer.getTypes();
			analizersTypeMatrix = new String[analyzersTypeList.length][2];
			for (int i = 0; i < analyzersTypeList.length; i++) {
				Analyzer.Type type = analyzersTypeList[i];
				analizersTypeMatrix[i][0] = type.getName();
				analizersTypeMatrix[i][1] = type.toString();
			}
		} else {
			analizersTypeMatrix = new String[1][1];
			analizersTypeMatrix[0][0] = SESSION_ENDED;
		}

		return analizersTypeMatrix;
	}

	public String[] getBrand() {
		String[] brand = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Periferico perifericoApp = Periferico.getInstance();
			DriverManager driverManager = perifericoApp.getDriverManager();
			List<String> brandList = driverManager.getBrandList();
			brand = new String[brandList.size()];
			for (int i = 0; i < brandList.size(); i++) {
				brand[i] = brandList.get(i);
			}// end for
		} else {
			brand = new String[1];
			brand[0] = SESSION_ENDED;
		}
		return brand;
	}// end getBrand

	public String[] getModel(String brand) {
		String[] model = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Periferico perifericoApp = Periferico.getInstance();
			DriverManager driverManager = perifericoApp.getDriverManager();
			List<String> modelList = driverManager.getModelList(brand);
			model = new String[modelList.size()];
			for (int i = 0; i < modelList.size(); i++) {
				model[i] = modelList.get(i);
			}// end for
		} else {
			model = new String[1];
			model[0] = SESSION_ENDED;
		}
		return model;
	}// end getModel

	@Override
	public ElementInfo getElementInfo(String analyzerId, String paramId)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Periferico perifApp = Periferico.getInstance();
		BoardManagementForUI boardManager = perifApp.getBoardManagementForUI();
		BoardList boardList = config.getBoardList();
		Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
		Element element;
		if (analyzer instanceof RainAnalyzer)
			element = ((RainAnalyzer) analyzer).getRainElement();
		else if (analyzer instanceof WindAnalyzer)
			element = ((WindAnalyzer) analyzer).getWind();
		else
			element = analyzer.getElement(paramId);
		ElementInfo elementInfo;
		if (element instanceof DataPortElement) {
			DataPortElement dpaElem = (DataPortElement) element;
			DigitalElementInfo deInfo;
			if (element instanceof DataPortAvgElement) {
				DataPortAvgElement dpaAvgElem = (DataPortAvgElement) dpaElem;
				DigitalAvgElementInfo daeInfo = new DigitalAvgElementInfo();
				daeInfo.setAvgPeriod(dpaAvgElem.getAvgPeriod());
				daeInfo.setAcqDelay(dpaAvgElem.getAcqDelay());
				daeInfo.setAcqDuration(dpaAvgElem.getAcqDuration());
				deInfo = daeInfo;
			} else {
				deInfo = new DigitalElementInfo();
			}
			deInfo.setRangeLow(dpaElem.getRangeLow());
			deInfo.setRangeHigh(dpaElem.getRangeHigh());
			deInfo.setDiscardDataNotValidForAnalyzer(dpaElem
					.getDiscardDataNotValidForAnalyzer());
			elementInfo = deInfo;
		} else if (element instanceof SampleElement) {
			SampleElement sampleElement = (SampleElement) element;
			AnalogElementInfo aeInfo;
			if (sampleElement instanceof AvgElement) {
				AvgElement avgElem = (AvgElement) sampleElement;
				AnalogAvgElementInfo aaeInfo = new AnalogAvgElementInfo();
				aaeInfo.setAvgPeriod(avgElem.getAvgPeriod());
				aaeInfo.setAcqDelay(avgElem.getAcqDelay());
				aaeInfo.setAcqDuration(avgElem.getAcqDuration());
				aeInfo = aaeInfo;
			} else {
				aeInfo = new AnalogElementInfo();
			}
			aeInfo.setRangeLow(sampleElement.getRangeLow());
			aeInfo.setRangeHigh(sampleElement.getRangeHigh());
			aeInfo.setBoardBindInfo(boardManager.getBoardBindLabel(boardList,
					sampleElement.getBoardBindInfo()));
			elementInfo = aeInfo;
		} else if (element instanceof CounterElement) {
			CounterElement counterElement = (CounterElement) element;
			RainElementInfo reInfo = new RainElementInfo();
			reInfo.setValueForEvent(counterElement.getValueForEvent());
			reInfo.setAcqOnRisingEdge(counterElement.isAcqOnRisingEdge());
			reInfo.setBoardBindInfo(boardManager.getBoardBindLabel(boardList,
					counterElement.getBoardBindInfo()));
			elementInfo = reInfo;
		} else if (element instanceof WindElement) {
			WindElement windElement = (WindElement) element;
			List<String> listMeasureUnits = new ArrayList<String>();
			Parameter param = perifApp.getCommonCfg().getParameter(
					windElement.getSpeed().getParameterId());
			if (param != null)
				listMeasureUnits.addAll(perifApp.getCommonCfg()
						.getAcquisitionMeasureUnitNames(
								param.getPhysicalDimension()));
			WindElementInfo weInfo = new WindElementInfo();
			weInfo.setAcqPeriod(windElement.getAcqPeriod());
			weInfo.setSpeedMaxValue(windElement.getSpeedMaxValue());
			weInfo.setSpeedCorrectionCoefficient(windElement
					.getSpeedCorrectionCoefficient());
			weInfo.setSpeedCorrectionOffset(windElement
					.getSpeedCorrectionOffset());
			weInfo.setSpeedBoardBindInfo(boardManager.getBoardBindLabel(
					boardList, windElement.getSpeedBoardBindInfo()));
			weInfo.setSpeedMeasureUnit(windElement.getSpeedMeasureUnitName());
			weInfo.setSpeedMeasureUnits(listMeasureUnits);
			weInfo.setSpeedNumDec(windElement.getSpeedNumDec());
			weInfo.setSpeedPrecision(windElement.getSpeedPrecision());
			weInfo.setSpeedRangeHigh(windElement.getSpeedRangeHigh());
			weInfo.setDirectionCorrectionCoefficient(windElement
					.getDirectionCorrectionCoefficient());
			weInfo.setDirectionCorrectionOffset(windElement
					.getDirectionCorrectionOffset());
			weInfo.setDirectionBoardBindInfo(boardManager.getBoardBindLabel(
					boardList, windElement.getDirectionBoardBindInfo()));
			weInfo.setDirectionMeasureUnit(windElement
					.getDirectionMeasureUnitName());
			weInfo.setDirectionNumDec(windElement.getDirectionNumDec());
			weInfo.setDirectionPrecision(windElement.getDirectionPrecision());
			elementInfo = weInfo;
		} else if (element == null) {
			throw new IllegalStateException("Element with parameter id '"
					+ paramId + "' not found in analyzer with id '"
					+ analyzerId + "'");
		} else {
			throw new IllegalStateException("Unkown element type: "
					+ element.getClass().getSimpleName());
		}
		if (element instanceof GenericSampleElement) {
			if (!(elementInfo instanceof SampleElementInfo))
				throw new IllegalStateException("Expected 'SampleElementInfo'"
						+ " type, found '"
						+ elementInfo.getClass().getSimpleName() + "'");
			GenericSampleElement genSmpElem = (GenericSampleElement) element;
			SampleElementInfo seInfo = (SampleElementInfo) elementInfo;
			List<String> listMeasureUnits = new ArrayList<String>();
			Parameter param = perifApp.getCommonCfg().getParameter(
					genSmpElem.getParameterId());
			if (param != null)
				listMeasureUnits.addAll(perifApp.getCommonCfg()
						.getAnalyzerMeasureUnitNames(
								param.getPhysicalDimension()));
			seInfo.setAcqPeriod(genSmpElem.getAcqPeriod());
			seInfo.setCorrectionCoefficient(genSmpElem
					.getCorrectionCoefficient());
			seInfo.setCorrectionOffset(genSmpElem.getCorrectionOffset());
			seInfo.setAnalyzerMeasureUnit(genSmpElem
					.getAnalyzerMeasureUnitName());
			seInfo.setAnalyzerMeasureUnits(listMeasureUnits);
			seInfo.setConversionInfo(genSmpElem.getConversionInfo());
			seInfo.setLinearizationCoefficient(genSmpElem
					.getLinearizationCoefficient());
			seInfo.setLinearizationOffset(genSmpElem.getLinearizationOffset());
		}
		if (element instanceof ScalarElement) {
			if (!(elementInfo instanceof ScalarElementInfo))
				throw new IllegalStateException("Expected 'ScalarElementInfo'"
						+ " type, found '"
						+ elementInfo.getClass().getSimpleName() + "'");
			ScalarElement scalarElem = (ScalarElement) element;
			ScalarElementInfo scInfo = (ScalarElementInfo) elementInfo;
			List<String> listMeasureUnits = new ArrayList<String>();
			Parameter param = perifApp.getCommonCfg().getParameter(
					scalarElem.getParameterId());
			if (param != null)
				listMeasureUnits.addAll(perifApp.getCommonCfg()
						.getAcquisitionMeasureUnitNames(
								param.getPhysicalDimension()));
			scInfo.setAcqMeasureUnit(scalarElem.getMeasureUnitName());
			scInfo.setAcqMeasureUnits(listMeasureUnits);
			scInfo.setNumDec(scalarElem.getNumDec());
			scInfo.setMinValue(scalarElem.getMinValue());
			scInfo.setMaxValue(scalarElem.getMaxValue());
		}
		elementInfo.setParameterId(element.getParameterId());
		elementInfo.setAnalyzerName(analyzer.getName());
		elementInfo.setEnabled(element.isEnabled());
		List<Integer> avgPeriods = perifApp.getCommonCfg().getAvgPeriods();
		Collections.sort(avgPeriods);
		elementInfo.setAvailableAvgPeriods(avgPeriods);
		elementInfo.setSelectedAvgPeriods(element.getAvgPeriods());
		Analyzer an = perifApp.getStation().getAnalyzer(analyzerId);
		elementInfo.setCalibrationRunning(an == null ? null : an
				.isManualCalibrationRunning());
		elementInfo.setMaintenanceRunning(an == null ? null : an
				.isMaintenanceInProgress());
		elementInfo.setActiveInRunningCfg(isElementActiveInRunningConfig(
				analyzerId, paramId));
		return elementInfo;
	}

	@Override
	public AnalogElementInfo makeNewElement(String analyzerId, String paramId)
			throws SessionExpiredException, UserParamsException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Periferico perifApp = Periferico.getInstance();
		BoardManagementForUI boardManager = perifApp.getBoardManagementForUI();
		BoardList boardList = config.getBoardList();
		Parameter param = perifApp.getCommonCfg().getParameter(paramId);
		if (param == null)
			throw new IllegalStateException("Cannot find parameter with id '"
					+ paramId + "' in common configuration");
		Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
		SampleElement sampleElem;
		AnalogElementInfo aeInfo;
		try {
			if (analyzer instanceof SampleAnalyzer) {
				SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
				sampleElem = sampleAnalyzer.makeNewElement(param);
				aeInfo = new AnalogElementInfo();
			} else if (analyzer instanceof AvgAnalyzer) {
				AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
				AvgElement avgElem = avgAnalyzer.makeNewElement(param);
				sampleElem = avgElem;
				AnalogAvgElementInfo aaeInfo = new AnalogAvgElementInfo();
				aaeInfo.setAvgPeriod(avgElem.getAvgPeriod());
				aaeInfo.setAcqDelay(avgElem.getAcqDelay());
				aaeInfo.setAcqDuration(avgElem.getAcqDuration());
				aeInfo = aaeInfo;
			} else if (analyzer != null) {
				throw new IllegalStateException("Analyzers of type '"
						+ analyzer.getType() + "' does not allow creation of "
						+ "new elements");
			} else {
				throw new IllegalStateException(
						"Cannot find analyzer with id '" + analyzerId + "'");
			}
		} catch (ConfigException ex) {
			throw new IllegalStateException(ex.getLocalizedMessage(getLocale()));
		}
		aeInfo.setRangeLow(sampleElem.getRangeLow());
		aeInfo.setBoardBindInfo(boardManager.getBoardBindLabel(boardList,
				sampleElem.getBoardBindInfo()));
		List<String> listAnMeasureUnits = new ArrayList<String>();
		listAnMeasureUnits.addAll(perifApp.getCommonCfg()
				.getAnalyzerMeasureUnitNames(param.getPhysicalDimension()));
		aeInfo.setAcqPeriod(sampleElem.getAcqPeriod());
		aeInfo.setCorrectionCoefficient(sampleElem.getCorrectionCoefficient());
		aeInfo.setCorrectionOffset(sampleElem.getCorrectionOffset());
		aeInfo.setAnalyzerMeasureUnit(sampleElem.getAnalyzerMeasureUnitName());
		aeInfo.setAnalyzerMeasureUnits(listAnMeasureUnits);
		aeInfo.setConversionInfo(sampleElem.getConversionInfo());
		aeInfo.setLinearizationCoefficient(sampleElem
				.getLinearizationCoefficient());
		aeInfo.setLinearizationOffset(sampleElem.getLinearizationOffset());
		List<String> listAcqMeasureUnits = new ArrayList<String>();
		listAcqMeasureUnits.addAll(perifApp.getCommonCfg()
				.getAcquisitionMeasureUnitNames(param.getPhysicalDimension()));
		aeInfo.setAcqMeasureUnit(sampleElem.getMeasureUnitName());
		aeInfo.setAcqMeasureUnits(listAcqMeasureUnits);
		aeInfo.setNumDec(sampleElem.getNumDec());
		aeInfo.setMinValue(sampleElem.getMinValue());
		aeInfo.setParameterId(sampleElem.getParameterId());
		aeInfo.setAnalyzerName(analyzer.getName());
		aeInfo.setEnabled(sampleElem.isEnabled());
		aeInfo.setAvailableAvgPeriods(perifApp.getCommonCfg().getAvgPeriods());
		aeInfo.setSelectedAvgPeriods(sampleElem.getAvgPeriods());
		Analyzer an = perifApp.getStation().getAnalyzer(analyzerId);
		aeInfo.setCalibrationRunning(an == null ? null : an
				.isManualCalibrationRunning());
		aeInfo.setMaintenanceRunning(an == null ? null : an
				.isMaintenanceInProgress());
		aeInfo.setActiveInRunningCfg(isElementActiveInRunningConfig(analyzerId,
				paramId));
		return aeInfo;
	}

	public String[] getConversionCoefficient(String analyzerId, String paramId,
			String acquisitionSelectedItemText, String analyzerSelectedItemText) {
		String resultString[] = new String[3];
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
			GenericSampleElement sampleElement = null;
			if (analyzer instanceof SampleAnalyzer)
				sampleElement = ((SampleAnalyzer) analyzer).getElement(paramId);
			else if (analyzer instanceof DataPortAnalyzer)
				sampleElement = ((DataPortAnalyzer) analyzer)
						.getElement(paramId);
			else if (analyzer instanceof AvgAnalyzer)
				sampleElement = ((AvgAnalyzer) analyzer).getElement(paramId);
			resultString[0] = acquisitionSelectedItemText;
			resultString[1] = analyzerSelectedItemText;
			resultString[2] = sampleElement.getConversionInfo(
					analyzerSelectedItemText, acquisitionSelectedItemText);
		} else
			resultString[0] = SESSION_ENDED;
		return resultString;
	}

	public String[][] getElementsParam(String analyzerId) {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Periferico perifericoApp = Periferico.getInstance();
			String[][] elementsArray = null;
			Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
			// load info for specific analyzers
			Type type = analyzer.getType();
			if (type.equals(Analyzer.Type.AVG)
					|| type.equals(Analyzer.Type.SAMPLE)) {
				// Sample and avg analyzer can be chemical, meteo or other
				// load chemical ,meteo and other element
				int index = 0;
				List<Parameter> chemicalList = perifericoApp.getCommonCfg()
						.getParameters(Parameter.ParamType.CHEMICAL);
				List<Parameter> meteoList = perifericoApp.getCommonCfg()
						.getParameters(Parameter.ParamType.METEO);
				List<Parameter> otherList = perifericoApp.getCommonCfg()
						.getParameters(Parameter.ParamType.OTHER);
				elementsArray = new String[3 + chemicalList.size() + 3
						+ meteoList.size() + 3 + otherList.size()][2];
				index = 0;
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = LINE;
				index++;
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"chemical");
				index++;
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = LINE;
				index++;
				for (int i = 0; i < chemicalList.size(); i++) {
					Parameter parameter = (Parameter) chemicalList.get(i);
					elementsArray[index][0] = parameter.getId();
					elementsArray[index][1] = parameter.getName();
					index++;
				}
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = LINE;
				index++;
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"), "meteo");
				index++;
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = LINE;
				index++;
				for (int i = 0; i < meteoList.size(); i++) {
					Parameter parameter = (Parameter) meteoList.get(i);
					elementsArray[index][0] = parameter.getId();
					elementsArray[index][1] = parameter.getName();
					index++;
				}
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = LINE;
				index++;
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"), "other");
				index++;
				elementsArray[index][0] = new String("0");
				elementsArray[index][1] = LINE;
				index++;
				for (int i = 0; i < otherList.size(); i++) {
					Parameter parameter = (Parameter) otherList.get(i);
					elementsArray[index][0] = parameter.getId();
					elementsArray[index][1] = parameter.getName();
					index++;
				}
			}// end if
			return elementsArray;
		} else {
			// session timed out
			return null;
		}

	}// end getElementsParam

	public String[] getAnalyzerInfo(String id) throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		/*
		 * AnalyzerInfo:
		 * 
		 * [00]: id
		 * 
		 * [01]: name
		 * 
		 * [02]: brand
		 * 
		 * [03]: model
		 * 
		 * [04]: description
		 * 
		 * [05]: serial number
		 * 
		 * [06]: status
		 * 
		 * [07]: uiURL
		 * 
		 * [08]: usernotes
		 * 
		 * [09]: type name
		 * 
		 * [10]: manualCalibrationRunning
		 * 
		 * [11]: maintenanceInProgress
		 * 
		 * [12]: min voltage for sample/wind/avg analyzers | portType for dpa
		 * analyzer
		 * 
		 * [13]: max voltage for sample/wind/avg analyzers | hostName for dpa
		 * analyzer
		 * 
		 * [14]: min range extension for sample/wind/avg analyzers | ipPort for
		 * dpa analyzer
		 * 
		 * [15]: max range extension for sample/wind/avg analyzers | ttyDevice
		 * for dpa analyzer
		 * 
		 * [16]: differential mode for sample/wind/avg analyzers | ttyBaudRate
		 * for dpa analyzer
		 * 
		 * [17]: ttyParams for dpa analyzer
		 * 
		 * [18]: hasNetworkPortOnly for dpa analyzer
		 * 
		 * [19]: driverParams for dpa analyzer
		 */
		String[] analyzerInfo = null;
		Periferico perifericoApp = Periferico.getInstance();
		Analyzer analyzer = config.getStation().getAnalyzer(id);
		Analyzer runningCfgAnalyzer = perifericoApp.getStation()
				.getAnalyzer(id);
		boolean calib = false;
		boolean maintenance = false;
		if (runningCfgAnalyzer != null) {
			calib = runningCfgAnalyzer.isManualCalibrationRunning();
			maintenance = runningCfgAnalyzer.isMaintenanceInProgress();
		}
		if (analyzer instanceof AnalogItfAnalyzer) {
			analyzerInfo = new String[17];
			AnalogItfAnalyzer aia = (AnalogItfAnalyzer) analyzer;
			analyzerInfo[12] = new Double(aia.getMinVoltage()).toString();
			analyzerInfo[13] = new Double(aia.getMaxVoltage()).toString();
			analyzerInfo[14] = new Boolean(aia.getMinRangeExtension())
					.toString();
			analyzerInfo[15] = new Boolean(aia.getMaxRangeExtension())
					.toString();
			analyzerInfo[16] = new Boolean(aia.isDifferentialModeNeeded())
					.toString();
		} else if (analyzer.getType().equals(Analyzer.Type.DPA)) {
			DriverManager driverManager = perifericoApp.getDriverManager();
			analyzerInfo = new String[20];
			DataPortAnalyzer dpa = (DataPortAnalyzer) analyzer;
			analyzerInfo[12] = dpa.getPortType().toString();
			analyzerInfo[13] = dpa.getHostName();
			analyzerInfo[14] = (dpa.getIpPort() == null ? null : dpa
					.getIpPort().toString());
			analyzerInfo[15] = dpa.getTtyDevice();
			analyzerInfo[16] = (dpa.getTtyBaudRate() == null ? null : dpa
					.getTtyBaudRate().toString());
			analyzerInfo[17] = dpa.getTtyParams();
			analyzerInfo[18] = new Boolean(driverManager.hasNetworkPortOnly(
					dpa.getBrand(), dpa.getModel())).toString();
			analyzerInfo[19] = dpa.getDriverParams();
		} else
			analyzerInfo = new String[12];
		// load common info
		analyzerInfo[0] = id;
		analyzerInfo[1] = analyzer.getName();
		analyzerInfo[2] = analyzer.getBrand();
		analyzerInfo[3] = analyzer.getModel();
		analyzerInfo[4] = analyzer.getDescription();
		analyzerInfo[5] = analyzer.getSerialNumber();
		analyzerInfo[6] = analyzer.getStatus().toString();
		analyzerInfo[7] = analyzer.getUiURL();
		analyzerInfo[8] = analyzer.getUserNotes();
		analyzerInfo[9] = analyzer.getType().name();
		analyzerInfo[10] = Boolean.toString(calib);
		analyzerInfo[11] = Boolean.toString(maintenance);
		return analyzerInfo;
	}// end getAnalyzerInfo

	public String[][] getElementsCfg(String analyzerId)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
		if (!(analyzer instanceof ObservableAnalyzer))
			return null;
		ObservableAnalyzer obsAnalyzer = (ObservableAnalyzer) analyzer;
		List<? extends ObservableSampleElement> listElements = obsAnalyzer
				.getListElements();
		// elementInfo for analyzer:
		// [i][0]: bindLabel
		// [i][1]: paramId
		// [i][2]: isSampleType
		String[][] elementInfo = new String[listElements.size()][3];
		for (int i = 0; i < listElements.size(); i++) {
			ObservableSampleElement element = listElements.get(i);
			elementInfo[i][0] = element.getBindLabel();
			elementInfo[i][1] = element.getParameterId();
			elementInfo[i][2] = Boolean
					.toString(!(element instanceof AvgElementItf));
		}
		return elementInfo;
	}// end getElementsCfg

	public Boolean[] activeCalibration(String id)
			throws SessionExpiredException {
		if (getSession().getAttribute("config") == null)
			throw new SessionExpiredException();
		Periferico perifericoApp = Periferico.getInstance();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(id);
		Boolean[] result = new Boolean[2];
		if (analyzer != null) {
			result[0] = analyzer.startManualCalibration();
			result[1] = analyzer.isManualCalibrationRunning();
		} else {
			result[0] = result[1] = false;
		}
		return result;
	}

	public String disactiveCalibration(String id)
			throws SessionExpiredException {
		if (getSession().getAttribute("config") == null)
			throw new SessionExpiredException();
		Periferico perifericoApp = Periferico.getInstance();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(id);
		if (analyzer == null)
			return Boolean.toString(false);
		analyzer.stopManualCalibration();
		return Boolean.toString(analyzer.isManualCalibrationRunning());
	}

	public Boolean[] activeMaintenance(String id)
			throws SessionExpiredException {
		if (getSession().getAttribute("config") == null)
			throw new SessionExpiredException();
		Periferico perifericoApp = Periferico.getInstance();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(id);
		Boolean[] result = new Boolean[2];
		if (analyzer != null) {
			result[0] = analyzer.startMaintenance();
			result[1] = analyzer.isMaintenanceInProgress();
		} else {
			result[0] = result[1] = false;
		}
		return result;
	}

	public String disactiveMaintenance(String id)
			throws SessionExpiredException {
		if (getSession().getAttribute("config") == null)
			throw new SessionExpiredException();
		Periferico perifericoApp = Periferico.getInstance();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(id);
		if (analyzer == null)
			return Boolean.toString(false);
		analyzer.stopMaintenance();
		return Boolean.toString(analyzer.isMaintenanceInProgress());
	}

	public String[] getAnalyzerFaultInfo(String id)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		String faultInfo[] = null;
		/*
		 * faultFields:
		 * 
		 * [0] : analyzer type
		 * 
		 * [1] : enabled/disabled/missing
		 * 
		 * [2] : description NOT USED | acqPeriod for dpa analyzer only
		 * 
		 * [3] : activeHigh
		 * 
		 * [4] : bind label
		 */
		Analyzer analyzer = config.getStation().getAnalyzer(id);

		// load info for specific analyzers
		Type type = analyzer.getType();
		switch (type) {
		case DPA:
			faultInfo = new String[3];
			faultInfo[0] = type.toString();

			DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
			if (dpaAnalyzer.getFault() == null) {
				// fault is not configured yet
				faultInfo[1] = null;
				faultInfo[2] = Integer.toString(10);
			} else {
				// fault is configured
				faultInfo[1] = new Boolean(dpaAnalyzer.getFault().isEnabled())
						.toString();
				faultInfo[2] = new Integer(dpaAnalyzer.getFault()
						.getAcqPeriod()).toString();
			}
			break;
		case AVG:
		case SAMPLE:
		case WIND:
			faultInfo = new String[5];
			faultInfo[0] = type.toString();

			AnalogItfAnalyzer aiAnalyzer = (AnalogItfAnalyzer) analyzer;
			if (aiAnalyzer.getFault() == null) {
				// fault is not configured yet
				faultInfo[1] = null;
				faultInfo[2] = null;
				faultInfo[3] = Boolean.TRUE.toString();
				faultInfo[4] = null;
			} else {
				// fault is configured
				faultInfo[1] = Boolean.toString(aiAnalyzer.getFault()
						.isEnabled());
				// TODO: eliminare (ex description dell'Alarm)
				faultInfo[2] = "";
				faultInfo[3] = Boolean.toString(aiAnalyzer.getFault()
						.isActiveHigh());
				Periferico perifericoApp = Periferico.getInstance();
				BoardManagementForUI boardManagementForUI = perifericoApp
						.getBoardManagementForUI();
				if (aiAnalyzer.getFault().getBoardBindInfo() == null) {
					faultInfo[4] = propertyUtil.getProperty(
							(String) getSession().getAttribute("locale"),
							"not_done");
				} else {
					BoardList boardList = config.getBoardList();
					faultInfo[4] = boardManagementForUI
							.getBoardBindLabel(boardList, aiAnalyzer.getFault()
									.getBoardBindInfo());
				}// end else
			}// end else
			break;
		case RAIN:
			faultInfo = new String[5];
			faultInfo[0] = type.toString();

			RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
			if (rainAnalyzer.getFault() == null) {
				// fault is not configured yet
				faultInfo[1] = null;
				faultInfo[2] = null;
				faultInfo[3] = Boolean.TRUE.toString();
				faultInfo[4] = null;
			} else {
				// fault is configured
				faultInfo[1] = new Boolean(rainAnalyzer.getFault().isEnabled())
						.toString();
				// TODO: eliminare (ex description dell'Alarm)
				faultInfo[2] = "";
				faultInfo[3] = new Boolean(rainAnalyzer.getFault()
						.isActiveHigh()).toString();
				Periferico perifericoApp = Periferico.getInstance();
				BoardManagementForUI boardManagementForUI = perifericoApp
						.getBoardManagementForUI();
				if (rainAnalyzer.getFault().getBoardBindInfo() == null) {
					faultInfo[4] = propertyUtil.getProperty(
							(String) getSession().getAttribute("locale"),
							"not_done");
				} else {
					BoardList boardList = config.getBoardList();
					faultInfo[4] = boardManagementForUI.getBoardBindLabel(
							boardList, rainAnalyzer.getFault()
									.getBoardBindInfo());
				}// end else
			}// end else
			break;
		default:
			throw new IllegalStateException("Unknown analyzer type");
		}// end switch
		return faultInfo;
	}// end getAnalyzerFaultInfo

	public String[] getAnalyzerDataValidInfo(String id)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		String dataValidInfo[] = null;
		// dataValidFields:
		// [0] : analyzer type
		// [1] : enabled/disabled/missing
		// [2] : description NOT USED
		// [3] : activeHigh
		// [4] : discardData
		// [5] : bind label
		Analyzer analyzer = config.getStation().getAnalyzer(id);
		if (analyzer instanceof IOItfAnalyzer) {
			IOItfAnalyzer aiAnalyzer = (IOItfAnalyzer) analyzer;
			dataValidInfo = new String[6];
			dataValidInfo[0] = aiAnalyzer.getType().toString();
			DataValidAlarm dva = aiAnalyzer.getDataValid();
			if (dva == null) {
				// dataValid is not configured yet
				dva = new DataValidAlarm();
				dataValidInfo[1] = null;
				dataValidInfo[2] = null;
				dataValidInfo[3] = Boolean.toString(dva.isActiveHigh());
				dataValidInfo[4] = Boolean.toString(dva.isDiscardData());
				dataValidInfo[5] = null;
			} else {
				// dataValid is configured
				dataValidInfo[1] = Boolean.toString(dva.isEnabled());
				// TODO: eliminare (ex description dell'Alarm)
				dataValidInfo[2] = "";
				dataValidInfo[3] = Boolean.toString(dva.isActiveHigh());
				dataValidInfo[4] = Boolean.toString(dva.isDiscardData());
				Periferico perifericoApp = Periferico.getInstance();
				BoardManagementForUI boardManagementForUI = perifericoApp
						.getBoardManagementForUI();
				if (dva.getBoardBindInfo() == null) {
					dataValidInfo[5] = propertyUtil.getProperty(
							(String) getSession().getAttribute("locale"),
							"not_done");
				} else {
					BoardList boardList = config.getBoardList();
					dataValidInfo[5] = boardManagementForUI.getBoardBindLabel(
							boardList, dva.getBoardBindInfo());
				}
			}
		} else
			throw new IllegalStateException("Unsupported analyzer type: "
					+ analyzer.getType());
		return dataValidInfo;
	}// end getAnalyzerDataValidInfo

	public String[] setNewAnalyzer(String type, String brand, String model)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		String[] resultString = null;
		if (type.equals(Analyzer.Type.DPA.toString())) {
			Periferico perifericoApp = Periferico.getInstance();
			DriverManager driverManager = perifericoApp.getDriverManager();
			DataPortAnalyzer dataPortAnalyzer = config.getStation()
					.makeNewDataPortAnalyzer(brand, model);
			resultString = new String[9];
			/*
			 * resultString:
			 * 
			 * resultString[0] : uid
			 * 
			 * resultString[1] : brand
			 * 
			 * resultString[2] : model
			 * 
			 * resultString[3] : portType
			 * 
			 * resultString[4] : defaultIpPort
			 * 
			 * resultString[5] : defaultTtyBaudRate
			 * 
			 * resultString[6] : defaultTtyParams
			 * 
			 * resultString[7] : hasNetworkPortOnly
			 * 
			 * resultString[8] : driverParams
			 */
			resultString[0] = dataPortAnalyzer.getIdAsString();
			resultString[1] = brand;
			resultString[2] = model;
			resultString[3] = dataPortAnalyzer.getPortType().toString();
			resultString[4] = (driverManager.getDefaultIpPort(brand, model) == null ? null
					: new Integer(driverManager.getDefaultIpPort(brand, model))
							.toString());
			resultString[5] = (driverManager
					.getDefaultTtyBaudRate(brand, model) == null ? null
					: new Integer(driverManager.getDefaultTtyBaudRate(brand,
							model)).toString());
			resultString[6] = driverManager.getDefaultTtyParams(brand, model);
			resultString[7] = new Boolean(driverManager.hasNetworkPortOnly(
					brand, model)).toString();
			resultString[8] = driverManager
					.getDefaultDriverParams(brand, model);
		} else {
			/*
			 * resultString:
			 * 
			 * resultString[0] : uid
			 * 
			 * resultString[1] : min range extension
			 * 
			 * resultString[2] : max range extension
			 * 
			 * resultString[3] : differentialModeNeeded
			 */
			resultString = new String[4];
			Analyzer analyzer = config.getStation().makeNewAnalyzer(
					Type.valueOf(type));
			resultString[0] = analyzer.getIdAsString();
			if (analyzer instanceof AnalogItfAnalyzer) {
				AnalogItfAnalyzer aiAnalyzer = (AnalogItfAnalyzer) analyzer;
				resultString[1] = Boolean.toString(aiAnalyzer
						.getMinRangeExtension());
				resultString[2] = Boolean.toString(aiAnalyzer
						.getMaxRangeExtension());
				resultString[3] = Boolean.toString(aiAnalyzer
						.isDifferentialModeNeeded());
			} else
				resultString[1] = resultString[2] = resultString[3] = "";
		}

		return resultString;
	}

	public String[] setAvgPeriod(String analyzerId, String paramId,
			String avgPeriod) {
		String[] resultString = new String[2];
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
			if (analyzer.getType().equals(Type.SAMPLE)) {
				SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
				SampleElement sampleElement = sampleAnalyzer
						.getElement(paramId);
				resultString[0] = new Boolean(
						sampleElement.insertAvgPeriod(new Integer(avgPeriod)))
						.toString();
			}
			if (analyzer.getType().equals(Type.DPA)) {
				DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
				DataPortElement dpaElement = dpaAnalyzer.getElement(paramId);
				resultString[0] = new Boolean(
						dpaElement.insertAvgPeriod(new Integer(avgPeriod)))
						.toString();
			}
			if (analyzer.getType().equals(Type.RAIN)) {
				RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
				CounterElement rainElement = rainAnalyzer.getRainElement();
				resultString[0] = new Boolean(
						rainElement.insertAvgPeriod(new Integer(avgPeriod)))
						.toString();
			}
			if (analyzer.getType().equals(Type.WIND)) {
				WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
				WindElement windElement = windAnalyzer.getWind();
				resultString[0] = new Boolean(
						windElement.insertAvgPeriod(new Integer(avgPeriod)))
						.toString();
			}
			resultString[1] = avgPeriod;
		} else
			resultString[0] = SESSION_ENDED;
		return (resultString);
	}// end setAvgPeriod

	public String deleteElement(String analyzerId, String paramId) {
		boolean result = false;
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
			if (analyzer.getType().equals(Type.SAMPLE)) {
				SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
				result = sampleAnalyzer.deleteElement(paramId);
			}
			if (analyzer.getType().equals(Type.DPA)) {
				// TODO implementare
			}
			if (analyzer.getType().equals(Type.AVG)) {
				AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
				result = avgAnalyzer.deleteElement(paramId);
			}
			resultString = new Boolean(result).toString();
		} else
			resultString = SESSION_ENDED;

		return (resultString);
	}// end deleteElement

	public String[] deleteAvgPeriod(String analyzerId, String paramId,
			String avgPeriod) {
		boolean result = false;
		String[] resultString = new String[2];
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
			if (analyzer.getType().equals(Type.SAMPLE)) {
				SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
				result = sampleAnalyzer.getElement(paramId).deleteAvgPeriod(
						new Integer(avgPeriod));
			}
			if (analyzer.getType().equals(Type.DPA)) {
				DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
				result = dpaAnalyzer.getElement(paramId).deleteAvgPeriod(
						new Integer(avgPeriod));
			}
			if (analyzer.getType().equals(Type.RAIN)) {
				RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
				result = rainAnalyzer.getRainElement().deleteAvgPeriod(
						new Integer(avgPeriod));
			}
			if (analyzer.getType().equals(Type.WIND)) {
				WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
				result = windAnalyzer.getWind().deleteAvgPeriod(
						new Integer(avgPeriod));
			}
			resultString[0] = new Boolean(result).toString();
			resultString[1] = avgPeriod;
		} else {
			resultString[0] = SESSION_ENDED;
		}
		return (resultString);
	}// end deleteAvgPeriod

	public String deleteFault(String id) throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		boolean result = false;
		Analyzer analyzer = config.getStation().getAnalyzer(id);
		if (analyzer.getType().equals(Type.SAMPLE)) {
			SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
			result = sampleAnalyzer.deleteFault();
		}
		// for DPA analyzer delete fault not available
		if (analyzer.getType().equals(Type.RAIN)) {
			RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
			result = rainAnalyzer.deleteFault();
		}
		if (analyzer.getType().equals(Type.AVG)) {
			AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
			result = avgAnalyzer.deleteFault();
		}
		if (analyzer.getType().equals(Type.WIND)) {
			WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
			result = windAnalyzer.deleteFault();
		}
		return Boolean.toString(result);
	}// end deleteFault

	public String deleteDataValid(String id) throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		boolean result = false;
		Analyzer analyzer = config.getStation().getAnalyzer(id);
		if (analyzer instanceof IOItfAnalyzer) {
			IOItfAnalyzer ioAnalyzer = (IOItfAnalyzer) analyzer;
			result = ioAnalyzer.deleteDataValid();
		}
		return Boolean.toString(result);
	}

	public String deleteAnalyzer(String id) {
		boolean result = false;
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			result = config.getStation().deleteAnalyzer(id);
			resultString = new Boolean(result).toString();
		} else
			resultString = SESSION_ENDED;
		return (resultString);
	}

	@Override
	public boolean verifySameAnalyzerConfig(String id, String[] infoFields,
			String[] faultFields, String[] dataValidFields)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		// infoFields:
		// [0]: name
		// [1]: brand
		// [2]: model
		// [3]: description
		// [4]: serial number
		// [5]: user notes
		// [6]: status
		// [7]: min voltage for sample/wind/avg analyzer only |
		// _____ portType for dpa analyzer only
		// [8]: max voltage for sample/wind/avg analyzer only |
		// _____ hostName for dpa analyzer only
		// [9]: min range extension for sample/wind/avg analyzer only |
		// _____ ipPort for dpa analyzer only
		// [10]: max range extension for sample/wind/avg analyzer only |
		// _____ ttyDevice for dpa analyzer only
		// [11]: differential mode for sample/wind/avg analyzer only |
		// ----- ttyBaudRate for dpa analyzer only
		// [12]: ttyParams for dpa analyzer only
		// [13]: driverParams for dpa analyzer only
		//
		// faultFields:
		// [0]: fault enabled
		// [1]: fault active high | acquisition period for dpa analyzer only
		// null: no fault
		//
		// dataValidFields:
		// [0]: dataValid enabled
		// [1]: dataValid active high
		// [2]: dataValid discard data
		// null: no dataValid
		try {
			Boolean returnValue = new Boolean(false);
			boolean isSameFault = false;
			boolean isSameDataValid = false;
			boolean isSameAnalyzer = false;
			Analyzer analyzer = config.getStation().getAnalyzer(id);
			if (analyzer instanceof AnalogItfAnalyzer) {
				// case of SAMPLE/AVG/WIND analyzer
				AnalogItfAnalyzer analog = (AnalogItfAnalyzer) analyzer;
				isSameAnalyzer = analog.isSameConfig(infoFields[0],
						infoFields[1], infoFields[3], infoFields[2],
						infoFields[4], infoFields[5], infoFields[6],
						infoFields[7], new Double(infoFields[8]),
						new Double(infoFields[9]),
						new Boolean(infoFields[10]),
						new Boolean(infoFields[11]),
						new Boolean(infoFields[12]));
				DigitalAlarm digitalAlarm = analog.getFault();
				if (digitalAlarm != null && faultFields != null) {
					isSameFault = digitalAlarm.isSameConfig(new Boolean(
							faultFields[0]).booleanValue(), new Boolean(
							faultFields[1]).booleanValue());
				} else {
					isSameFault = digitalAlarm == null && faultFields == null;
				}
				DataValidAlarm dataValidAlarm = analog.getDataValid();
				if (dataValidAlarm != null && dataValidFields != null) {
					isSameDataValid = dataValidAlarm.isSameConfig(new Boolean(
							dataValidFields[0]),
							new Boolean(dataValidFields[1]), new Boolean(
									dataValidFields[2]));
				} else {
					isSameDataValid = dataValidAlarm == null
							&& dataValidFields == null;
				}
			} // end if SAMPLE/AVG/WIND
			else if (analyzer.getType().equals(Analyzer.Type.RAIN)) {
				// case of RAIN analyzer
				RainAnalyzer rain = (RainAnalyzer) analyzer;
				isSameAnalyzer = rain.isSameConfig(infoFields[0],
						infoFields[1], infoFields[3], infoFields[2],
						infoFields[4], infoFields[5], infoFields[6],
						infoFields[7]);
				DigitalAlarm digitalAlarm = ((RainAnalyzer) analyzer)
						.getFault();
				if (digitalAlarm != null && faultFields != null) {
					isSameFault = digitalAlarm.isSameConfig(new Boolean(
							faultFields[0]).booleanValue(), new Boolean(
							faultFields[1]).booleanValue());
				} else {
					isSameFault = digitalAlarm == null && faultFields == null;
				}
				DataValidAlarm dataValidAlarm = rain.getDataValid();
				if (dataValidAlarm != null && dataValidFields != null) {
					isSameDataValid = dataValidAlarm.isSameConfig(new Boolean(
							dataValidFields[0]),
							new Boolean(dataValidFields[1]), new Boolean(
									dataValidFields[2]));
				} else {
					isSameDataValid = dataValidAlarm == null
							&& dataValidFields == null;
				}
			}// end if RAIN
			else if (analyzer.getType().equals(Analyzer.Type.DPA)) {
				DataPortAnalyzer dpa = (DataPortAnalyzer) analyzer;
				DataPortAlarm dpaAlarm = dpa.getFault();
				isSameAnalyzer = dpa.isSameConfig(infoFields[0], infoFields[1],
						infoFields[2], infoFields[3], infoFields[4],
						infoFields[5], infoFields[6], infoFields[7],
						PortType.valueOf(infoFields[8]), infoFields[9],
						new Integer(infoFields[10]).intValue(), infoFields[11],
						new Integer(infoFields[12]).intValue(), infoFields[13],
						infoFields[14]);
				isSameFault = dpaAlarm.isSameConfig(
						new Boolean(faultFields[0]).booleanValue(),
						new Integer(faultFields[1]).intValue());
				isSameDataValid = true;
			}
			if (!isSameAnalyzer || !isSameFault || !isSameDataValid)
				returnValue = new Boolean(false);
			else
				returnValue = new Boolean(true);
			return returnValue;
		} catch (NumberFormatException nfe) {
			// If numbers are not parsable, the configuration contained in the
			// user interface is certainly different from application's config.
			return false;
		}
	}// end verifySameAnalyzerConfig

	@Override
	public boolean verifySameElementConfig(String analyzerId, ElementInfo info)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
		if (analyzer == null)
			throw new IllegalStateException("Cannot find analyzer with id '"
					+ analyzerId + "'");
		Element element = analyzer.getElement(info.getParameterId());
		if (element == null)
			throw new IllegalStateException("Cannot find element with id '"
					+ info.getParameterId() + "' for analyzer "
					+ analyzer.getName());
		String error = info.checkEmptyFields();
		if (error != null)
			return false;
		if (info instanceof DigitalAvgElementInfo) {
			DigitalAvgElementInfo daeInfo = (DigitalAvgElementInfo) info;
			if (!(element instanceof DataPortAvgElement))
				throw new IllegalStateException("Expected DataPortAvgElement"
						+ ", found " + element.getClass().getSimpleName());
			DataPortAvgElement avgElement = (DataPortAvgElement) element;
			return avgElement.isSameConfig(daeInfo.isEnabled(),
					daeInfo.getAcqMeasureUnit(), daeInfo.getMinValue(),
					daeInfo.getMaxValue(), daeInfo.getNumDec(),
					daeInfo.getAnalyzerMeasureUnit(), daeInfo.getAcqPeriod(),
					daeInfo.getCorrectionCoefficient(),
					daeInfo.getCorrectionOffset(),
					daeInfo.getLinearizationCoefficient(),
					daeInfo.getLinearizationOffset(), daeInfo.getRangeLow(),
					daeInfo.getRangeHigh(),
					daeInfo.getDiscardDataNotValidForAnalyzer(),
					daeInfo.getAcqDelay(), daeInfo.getAcqDuration(),
					daeInfo.getAvgPeriod());
		} else if (info instanceof DigitalElementInfo) {
			DigitalElementInfo deInfo = (DigitalElementInfo) info;
			if (!(element instanceof DataPortElement))
				throw new IllegalStateException("Expected DataPortElement"
						+ ", found " + element.getClass().getSimpleName());
			DataPortElement dpElement = (DataPortElement) element;
			return dpElement.isSameConfig(deInfo.isEnabled(),
					deInfo.getAcqMeasureUnit(), deInfo.getMinValue(),
					deInfo.getMaxValue(), deInfo.getNumDec(),
					deInfo.getAnalyzerMeasureUnit(), deInfo.getAcqPeriod(),
					deInfo.getCorrectionCoefficient(),
					deInfo.getCorrectionOffset(),
					deInfo.getLinearizationCoefficient(),
					deInfo.getLinearizationOffset(), deInfo.getRangeLow(),
					deInfo.getRangeHigh(),
					deInfo.getDiscardDataNotValidForAnalyzer());
		} else {
			throw new UnsupportedOperationException("ElementInfo of type '"
					+ info.getClass().getSimpleName()
					+ "' is not supported yet");
		}
	}

	@Override
	public boolean verifySameElementConfig(String analyzerId,
			String[] fieldsValue) throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		try {
			boolean returnValue = false;
			boolean isSameElement = false;

			Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
			if (analyzer.getType().equals(Analyzer.Type.SAMPLE)) {
				SampleAnalyzer sample = (SampleAnalyzer) analyzer;
				SampleElement sampleElement = sample.getElement(fieldsValue[0]);
				/*
				 * info for sample element :
				 * 
				 * [0] : paramId
				 * 
				 * [1] : AcquisitionMeasureUnitName selected
				 * 
				 * [2] : MaxValue
				 * 
				 * [3] : MinValue
				 * 
				 * [4] : NumDec
				 * 
				 * [5] : enabled
				 * 
				 * [6] : AcqPeriod
				 * 
				 * [7] : CorrectionCoefficient
				 * 
				 * [8] : CorrectionOffset
				 * 
				 * [9] : RangeHigh
				 * 
				 * [10] : RangeLow
				 * 
				 * [11] : Linearization Coefficient
				 * 
				 * [12] : Linearization Offset
				 * 
				 * [13] : AnalyzerMeasureUnitName selected
				 */

				// TODO: capire se bisogna controllare che i campi numerici non
				// siano
				// null o stringa vuota
				isSameElement = sampleElement.isSameConfig(new Boolean(
						fieldsValue[5]).booleanValue(), fieldsValue[1],
						new Double(fieldsValue[3]).doubleValue(), new Double(
								fieldsValue[2]).doubleValue(), new Integer(
								fieldsValue[4]).intValue(), fieldsValue[13],
						new Integer(fieldsValue[6]).intValue(), new Double(
								fieldsValue[7]).doubleValue(), new Double(
								fieldsValue[8]).doubleValue(), new Double(
								fieldsValue[11]).doubleValue(), new Double(
								fieldsValue[12]).doubleValue(), new Double(
								fieldsValue[10]).doubleValue(), new Double(
								fieldsValue[9]).doubleValue());
				returnValue = isSameElement;
			}// end sample

			if (analyzer.getType().equals(Analyzer.Type.AVG)) {
				AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
				AvgElement avgElement = avgAnalyzer.getElement(fieldsValue[0]);
				/*
				 * info for avg element :
				 * 
				 * [0] : paramId
				 * 
				 * [1] : AcquisitionMeasureUnitName selected
				 * 
				 * [2] : MaxValue
				 * 
				 * [3] : MinValue
				 * 
				 * [4] : NumDec
				 * 
				 * [5] : enabled
				 * 
				 * [6] : AcqPeriod
				 * 
				 * [7] : CorrectionCoefficient
				 * 
				 * [8] : CorrectionOffset
				 * 
				 * [9] : RangeHigh
				 * 
				 * [10] : RangeLow
				 * 
				 * [11] : LinearizationCoefficient
				 * 
				 * [12] : LinearizationOffset
				 * 
				 * [13] : acqDelay
				 * 
				 * [14] : acqDuration
				 * 
				 * [15] : acqPeriodForAvg
				 * 
				 * [16] : AnalyzerMeasureUnitName selected
				 */

				// TODO: capire se bisogna controllare che i campi numerici non
				// siano
				// null o stringa vuota
				isSameElement = avgElement.isSameConfig(new Boolean(
						fieldsValue[5]).booleanValue(), fieldsValue[1],
						new Double(fieldsValue[3]).doubleValue(), new Double(
								fieldsValue[2]).doubleValue(), new Integer(
								fieldsValue[4]).intValue(), fieldsValue[16],
						new Integer(fieldsValue[6]).intValue(), new Double(
								fieldsValue[7]).doubleValue(), new Double(
								fieldsValue[8]).doubleValue(), new Double(
								fieldsValue[11]).doubleValue(), new Double(
								fieldsValue[12]).doubleValue(), new Double(
								fieldsValue[10]).doubleValue(), new Double(
								fieldsValue[9]).doubleValue(), new Integer(
								fieldsValue[13]).intValue(), new Integer(
								fieldsValue[14]).intValue(), new Integer(
								fieldsValue[15]).intValue());
				returnValue = isSameElement;
			}// end avg

			// wind
			if (analyzer.getType().equals(Analyzer.Type.WIND)) {
				WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
				WindElement windElement = windAnalyzer.getWind();
				/*
				 * info for wind element :
				 * 
				 * [0] : SpeedMeasureUnitName selected
				 * 
				 * [1] : SpeedMaxValue
				 * 
				 * [2] : SpeedNumDec
				 * 
				 * [3] : SpeedPrecision
				 * 
				 * [4] : DirectionNumDec
				 * 
				 * [5] : DirectionPrecision
				 * 
				 * [6] : enabled
				 * 
				 * [7] : AcqPeriod
				 * 
				 * [8] : SpeedCorrectionCoefficient
				 * 
				 * [9] : SpeedCorrectionOffset
				 * 
				 * [10] : SpeedRangeHigh
				 * 
				 * [11] : DirectionCorrectionCoefficient
				 * 
				 * [12] : DirectionCorrectionOffset
				 */

				// TODO: capire se bisogna controllare che i campi numerici non
				// siano null o stringa vuota
				isSameElement = windElement.isSameConfig(//
						Boolean.valueOf(fieldsValue[6]),//
						fieldsValue[0],//
						Double.valueOf(fieldsValue[1]),//
						Integer.valueOf(fieldsValue[2]),//
						Double.valueOf(fieldsValue[3]),//
						Double.valueOf(fieldsValue[10]),//
						Integer.valueOf(fieldsValue[4]),//
						Double.valueOf(fieldsValue[5]),//
						Integer.valueOf(fieldsValue[7]),//
						Double.valueOf(fieldsValue[8]),//
						Double.valueOf(fieldsValue[9]),//
						Double.valueOf(fieldsValue[11]),//
						Double.valueOf(fieldsValue[12]));
				returnValue = isSameElement;
			} // end wind

			// rain
			if (analyzer.getType().equals(Analyzer.Type.RAIN)) {
				RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
				CounterElement rainElement = rainAnalyzer.getRainElement();
				/*
				 * info for rain element :
				 * 
				 * [0] : paramId
				 * 
				 * [1] : MeasureUnitName selected
				 * 
				 * [2] : MaxValue
				 * 
				 * [3] : MinValue
				 * 
				 * [4] : NumDec
				 * 
				 * [5] : enabled
				 * 
				 * [6] : ValueForEvent
				 * 
				 * [7] : AcqOnRisingEdge
				 */

				// TODO: capire se bisogna controllare che i campi numerici non
				// siano
				// null o stringa vuota
				isSameElement = rainElement.isSameConfig(new Boolean(
						fieldsValue[5]).booleanValue(), fieldsValue[1],
						new Double(fieldsValue[3]).doubleValue(), new Double(
								fieldsValue[2]).doubleValue(), new Integer(
								fieldsValue[4]).intValue(), new Double(
								fieldsValue[6]).doubleValue(), new Boolean(
								fieldsValue[7]).booleanValue());
				if (isSameElement)
					returnValue = new Boolean(true);
				else
					returnValue = new Boolean(false);
			}// end rain

			return returnValue;
		} catch (NumberFormatException nfe) {
			// If numbers are not parsable, the configuration contained in the
			// user interface is certainly different from application's config.
			return false;
		}
	}// end verifySameElementConfig

	public void setVerifyAnalyzerFields(String id, String[] analyzerFields)
			throws SessionExpiredException, UserParamsException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		/*
		 * analyzerFields:
		 * 
		 * [0]: name
		 * 
		 * [1]: brand
		 * 
		 * [2]: model
		 * 
		 * [3]: description
		 * 
		 * [4]: serial number
		 * 
		 * [5]: user notes
		 * 
		 * [6]: status
		 * 
		 * [7]: uiURL
		 * 
		 * [8]: min voltage for sample/wind/avg analyzer only | portType for dpa
		 * analyzer only
		 * 
		 * [9]: max voltage for api and sample/wind/avg analyzer only | hostName
		 * for dpa analyzer only
		 * 
		 * [10] : ipPort for dpa analyzer only | min range extension
		 * sample/wind/avg analyzer only
		 * 
		 * [11] : ttyDevice for dpa analyzer only | max range extension
		 * sample/wind/avg analyzer only
		 * 
		 * [12] : ttyBaudRate for dpa analyzer only | differential mode for
		 * sample/wind/avg analyzer only
		 * 
		 * [13] : ttyParams for dpa analyzer only
		 * 
		 * [14] : driverParams for dpa analyzer only
		 */
		Analyzer analyzer = config.getStation().getAnalyzer(id);
		if (analyzer == null)
			throw new IllegalStateException("Analyzer not found for id: " + id);
		try {
			if (analyzer instanceof AnalogItfAnalyzer) { // sample/wind/avg
				if (analyzerFields.length != 13)
					throw new IllegalStateException(
							"13 fields expected for AnalogItfAnalyzer");
				String[] args = new String[1];
				if (analyzerFields[8] == null
						|| analyzerFields[8].trim().isEmpty())
					args[0] = "min_voltage";
				else if (analyzerFields[9] == null
						|| analyzerFields[9].trim().isEmpty())
					args[0] = "max_voltage";
				else
					args[0] = null;
				if (args[0] != null) {
					throw new UserParamsException(
							propertyUtil.getLocalizedMessage(
									(String) getSession()
											.getAttribute("locale"),
									"not_empty", args));
				}
				boolean diffMode = Boolean.parseBoolean(analyzerFields[12]);
				AnalogItfAnalyzer aia = (AnalogItfAnalyzer) analyzer;
				if (diffMode != aia.isDifferentialModeNeeded()
						&& aia.hasBindedElements()) {
					throw new UserParamsException(propertyUtil.getProperty(
							(String) getSession().getAttribute("locale"),
							"cannot_change_diff_mode"));
				}
				aia.setConfig(analyzerFields[0], analyzerFields[1],
						analyzerFields[3], analyzerFields[2],
						analyzerFields[4], analyzerFields[5],
						analyzerFields[6], analyzerFields[7],
						new Double(analyzerFields[8]),
						new Double(analyzerFields[9]), new Boolean(
								analyzerFields[10]), new Boolean(
								analyzerFields[11]), diffMode);
			} else if (analyzer instanceof DataPortAnalyzer) {
				if (analyzerFields.length != 15)
					throw new IllegalStateException(
							"15 fields expected for DataPortAnalyzer");
				String[] args = new String[1];
				args[0] = null;
				if (analyzerFields[8].equals(NETWORK)) {
					if (analyzerFields[9] == null
							|| analyzerFields[9].trim().isEmpty())
						args[0] = "host_name";
					if (analyzerFields[10] == null
							|| analyzerFields[10].trim().isEmpty())
						args[0] = "ip_port";
				} else if (analyzerFields[8].equals(SERIAL)) {
					if (analyzerFields[11] == null
							|| analyzerFields[11].trim().isEmpty())
						args[0] = "tty_device";
					if (analyzerFields[12] == null
							|| analyzerFields[12].trim().isEmpty())
						args[0] = "tty_baud_rate";
					if (analyzerFields[13] == null
							|| analyzerFields[13].trim().isEmpty())
						args[0] = "tty_params";
				}
				if (args[0] != null) {
					throw new UserParamsException(
							propertyUtil.getLocalizedMessage(
									(String) getSession()
											.getAttribute("locale"),
									"not_empty", args));
				}
				DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
				dpaAnalyzer.setConfig(analyzerFields[0], analyzerFields[1],
						analyzerFields[2], analyzerFields[3],
						analyzerFields[4], analyzerFields[5],
						analyzerFields[6], analyzerFields[7],
						PortType.valueOf(analyzerFields[8]),
						analyzerFields[9],
						new Integer(analyzerFields[10]).intValue(),
						analyzerFields[11],
						new Integer(analyzerFields[12]).intValue(),
						analyzerFields[13], analyzerFields[14]);
			} else if (analyzer instanceof RainAnalyzer) {
				if (analyzerFields.length != 8)
					throw new IllegalStateException(
							"8 fields expected for RainAnalyzer");
				RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
				rainAnalyzer
						.setConfig(analyzerFields[0], analyzerFields[1],
								analyzerFields[3], analyzerFields[2],
								analyzerFields[4], analyzerFields[5],
								analyzerFields[6], analyzerFields[7]);
			} else
				throw new IllegalStateException("Unknown analyzer type");
		} catch (NumberFormatException nfe) {
			throw new UserParamsException(propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"not_character"));
		} catch (ConfigException ce) {
			throw new UserParamsException(ce.getLocalizedMessage(getLocale()));
		}
	}// end setVerifyAnalyzerFields

	public String[] readValueForCalibration(String analyzerId, String paramId) {
		/*
		 * result:
		 * 
		 * [0] : voltage
		 * 
		 * [1] : rawValue
		 * 
		 * [2] : correctedValue
		 * 
		 * [3] : measureUnit
		 */
		String[] result = null;
		Periferico perifericoApp = Periferico.getInstance();
		GenericSampleElement gse = (GenericSampleElement) perifericoApp
				.getStation().getAnalyzer(analyzerId).getElement(paramId);
		SampleValues sample = gse.getLastSampleValues();
		if (sample != null) {
			NumberFormat nf = NumberFormat.getInstance(Locale.US);
			nf.setRoundingMode(Periferico.ROUNDING_MODE_FOR_SAMPLE_DATA);
			nf.setMinimumFractionDigits(6);
			nf.setMaximumFractionDigits(6);
			nf.setGroupingUsed(false);
			result = new String[4];
			result[0] = PerifericoUtil.formatDouble(sample.getVoltage(), nf);
			result[1] = PerifericoUtil.formatDouble(sample.getRawValue(), nf);
			result[2] = PerifericoUtil.formatDouble(sample.getCorrectedValue(),
					nf);
			result[3] = gse.getAnalyzerMeasureUnitName();
		} else {
			result = new String[1];
			result[0] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "acquisition_not_active");
		}
		return result;
	}

	public String[] calculate(String analyzerId, String paramId,
			String expectedValue1, String expectedValue2, String readValue1,
			String readValue2) throws SessionExpiredException,
			UserParamsException {
		/*
		 * result: [0] : coefficient (m) [1] : offset (q)
		 */
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();

		if (expectedValue1.equals("") && expectedValue2.equals(""))
			throw new UserParamsException(propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"expected_value_needed"));

		GenericSampleElement genericSampleElement = (GenericSampleElement) config
				.getStation().getAnalyzer(analyzerId).getElement(paramId);

		Double expVal1 = null;
		Double expVal2 = null;
		Double readVal1 = null;
		Double readVal2 = null;

		try {
			if (!expectedValue1.equals("")) {
				expVal1 = new Double(expectedValue1);
				if (!readValue1.equals(""))
					readVal1 = new Double(readValue1);
			}
			if (!expectedValue2.equals("")) {
				expVal2 = new Double(expectedValue2);
				if (!readValue2.equals(""))
					readVal2 = new Double(readValue2);
			}
		} catch (NumberFormatException nfe) {
			logger.error(nfe);
			throw new UserParamsException(propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"not_character"));
		}

		Correction correction = null;
		try {
			genericSampleElement.setCalib1stPoint(expVal1);
			genericSampleElement.setCalib2ndPoint(expVal2);
			genericSampleElement.setValue1stPoint(readVal1);
			genericSampleElement.setValue2ndPoint(readVal2);
			correction = genericSampleElement.computeCorrection();
		} catch (ConfigException ce) {
			throw new UserParamsException(ce.getLocalizedMessage(getLocale()));
		}

		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		nf.setRoundingMode(Periferico.ROUNDING_MODE_FOR_SAMPLE_DATA);
		nf.setMinimumFractionDigits(1);
		nf.setMaximumFractionDigits(6);
		nf.setGroupingUsed(false);

		String[] result = new String[2];
		result[0] = PerifericoUtil
				.formatDouble(correction.getCoefficient(), nf);
		result[1] = PerifericoUtil.formatDouble(correction.getOffset(), nf);
		return result;
	}

	@Override
	public void setVerifyElementFields(String analyzerId, ElementInfo info)
			throws SessionExpiredException, UserParamsException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
		if (analyzer == null)
			throw new IllegalStateException("Cannot find analyzer with id '"
					+ analyzerId + "'");
		Element element = analyzer.getElement(info.getParameterId());
		if (element == null)
			throw new IllegalStateException("Cannot find element with id '"
					+ info.getParameterId() + "' for analyzer "
					+ analyzer.getName());
		String error = info.checkEmptyFields();
		if (error != null)
			throw new UserParamsException(propertyUtil.getLocalizedMessage(
					(String) getSession().getAttribute("locale"), "not_empty",
					new String[] { error }));
		if (info instanceof DigitalAvgElementInfo) {
			DigitalAvgElementInfo daeInfo = (DigitalAvgElementInfo) info;
			if (!(element instanceof DataPortAvgElement))
				throw new IllegalStateException("Expected DataPortAvgElement"
						+ ", found " + element.getClass().getSimpleName());
			DataPortAvgElement avgElement = (DataPortAvgElement) element;
			try {
				avgElement.setConfig(daeInfo.isEnabled(),
						daeInfo.getAcqMeasureUnit(), daeInfo.getMinValue(),
						daeInfo.getMaxValue(), daeInfo.getNumDec(),
						daeInfo.getAnalyzerMeasureUnit(),
						daeInfo.getAcqPeriod(),
						daeInfo.getCorrectionCoefficient(),
						daeInfo.getCorrectionOffset(),
						daeInfo.getLinearizationCoefficient(),
						daeInfo.getLinearizationOffset(),
						daeInfo.getRangeLow(), daeInfo.getRangeHigh(),
						daeInfo.getDiscardDataNotValidForAnalyzer(),
						daeInfo.getAcqDelay(), daeInfo.getAcqDuration(),
						daeInfo.getAvgPeriod());
			} catch (ConfigException e) {
				throw new UserParamsException(
						e.getLocalizedMessage(getLocale()));
			}
		} else if (info instanceof DigitalElementInfo) {
			DigitalElementInfo deInfo = (DigitalElementInfo) info;
			if (!(element instanceof DataPortElement))
				throw new IllegalStateException("Expected DataPortElement"
						+ ", found " + element.getClass().getSimpleName());
			DataPortElement dpElement = (DataPortElement) element;
			try {
				dpElement.setConfig(deInfo.isEnabled(),
						deInfo.getAcqMeasureUnit(), deInfo.getMinValue(),
						deInfo.getMaxValue(), deInfo.getNumDec(),
						deInfo.getAnalyzerMeasureUnit(), deInfo.getAcqPeriod(),
						deInfo.getCorrectionCoefficient(),
						deInfo.getCorrectionOffset(),
						deInfo.getLinearizationCoefficient(),
						deInfo.getLinearizationOffset(), deInfo.getRangeLow(),
						deInfo.getRangeHigh(),
						deInfo.getDiscardDataNotValidForAnalyzer());
			} catch (ConfigException e) {
				throw new UserParamsException(
						e.getLocalizedMessage(getLocale()));
			}
		} else {
			throw new UnsupportedOperationException("ElementInfo of type '"
					+ info.getClass().getSimpleName()
					+ "' is not supported yet");
		}
	}

	public String setVerifyElementFields(String analyzerId, String[] info) {
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));

		if (config != null) {
			Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
			try {

				if (analyzer.getType().equals(Analyzer.Type.SAMPLE)) {
					// case sample analyzer
					/*
					 * info for sample element :
					 * 
					 * [0] : paramId
					 * 
					 * [1] : AcquisitionMeasureUnitName selected
					 * 
					 * [2] : MaxValue
					 * 
					 * [3] : MinValue
					 * 
					 * [4] : NumDec
					 * 
					 * [5] : enabled
					 * 
					 * [6] : AcqPeriod
					 * 
					 * [7] : CorrectionCoefficient
					 * 
					 * [8] : CorrectionOffset
					 * 
					 * [9] : RangeHigh
					 * 
					 * [10] : RangeLow
					 * 
					 * [11] : Linearization Coefficient
					 * 
					 * [12] : Linearization Offset
					 * 
					 * [13] : AnalyzerMeasureUnitName selected
					 */

					if (info[3] == null || info[2] == null || info[4] == null
							|| info[3].equals("") || info[2].equals("")
							|| info[4].equals("") || info[7] == null
							|| info[6] == null || info[9] == null
							|| info[10] == null || info[11] == null
							|| info[12] == null || info[8] == null
							|| info[7].equals("") || info[6].equals("")
							|| info[9].equals("") || info[10].equals("")
							|| info[11].equals("") || info[12].equals("")
							|| info[8].equals("")) {
						// error case
						String[] args = new String[1];
						if (info[3] == null || info[3].equals(""))
							args[0] = "min_value";
						if (info[2] == null || info[2].equals(""))
							args[0] = "max_value";
						if (info[4] == null || info[4].equals(""))
							args[0] = "num_dec";
						if (info[7] == null || info[7].equals(""))
							args[0] = "correction_coefficient";
						if (info[6] == null || info[6].equals(""))
							args[0] = "acq_period";
						if (info[8] == null || info[8].equals(""))
							args[0] = "correction_offset";
						if (info[9] == null || info[9].equals(""))
							args[0] = "range_high";
						if (info[10] == null || info[10].equals(""))
							args[0] = "range_low";
						if (info[11] == null || info[11].equals(""))
							args[0] = "linearization_coefficient";
						if (info[12] == null || info[12].equals(""))
							args[0] = "linearization_offset";

						resultString = propertyUtil.getLocalizedMessage(
								(String) getSession().getAttribute("locale"),
								"not_empty", args);
					} else {
						try {
							SampleAnalyzer sample = (SampleAnalyzer) analyzer;
							SampleElement sampleElement = sample
									.getElement(info[0]);
							sampleElement.setConfig(
									new Boolean(info[5]).booleanValue(),
									info[1], new Double(info[3]).doubleValue(),
									new Double(info[2]).doubleValue(),
									new Integer(info[4]).intValue(), info[13],
									new Integer(info[6]).intValue(),
									new Double(info[7]).doubleValue(),
									new Double(info[8]).doubleValue(),
									new Double(info[11]).doubleValue(),
									new Double(info[12]).doubleValue(),
									new Double(info[10]).doubleValue(),
									new Double(info[9]).doubleValue());
						} catch (NumberFormatException nfe) {
							resultString = propertyUtil.getProperty(
									(String) getSession()
											.getAttribute("locale"),
									"not_character");
						}
					}
				} else if (analyzer.getType().equals(Analyzer.Type.WIND)) {
					// case wind analyzer
					/*
					 * info for wind element :
					 * 
					 * [0] : SpeedMeasureUnitName selected
					 * 
					 * [1] : SpeedMaxValue
					 * 
					 * [2] : SpeedNumDec
					 * 
					 * [3] : SpeedPrecision
					 * 
					 * [4] : DirectionNumDec
					 * 
					 * [5] : DirectionPrecision
					 * 
					 * [6] : enabled
					 * 
					 * [7] : AcqPeriod
					 * 
					 * [8] : SpeedCorrectionCoefficient
					 * 
					 * [9] : SpeedCorrectionOffset
					 * 
					 * [10] : SpeedRangeHigh
					 * 
					 * [11] : DirectionCorrectionCoefficient
					 * 
					 * [12] : DirectionCorrectionOffset
					 */

					if (info[1] == null || info[1].equals("")
							|| info[2] == null || info[2].equals("")
							|| info[3] == null || info[3].equals("")
							|| info[4] == null || info[4].equals("")
							|| info[5] == null || info[5].equals("")
							|| info[7] == null || info[7].equals("")
							|| info[8] == null || info[8].equals("")
							|| info[9] == null || info[9].equals("")
							|| info[10] == null || info[10].equals("")
							|| info[11] == null || info[11].equals("")
							|| info[12] == null || info[12].equals("")) {

						// error case
						String[] args = new String[1];
						if (info[1] == null || info[1].equals(""))
							args[0] = "speed_max_value";
						if (info[2] == null || info[2].equals(""))
							args[0] = "speed_num_dec";
						if (info[3] == null || info[3].equals(""))
							args[0] = "speed_precision";
						if (info[4] == null || info[4].equals(""))
							args[0] = "direction_num_dec";
						if (info[5] == null || info[5].equals(""))
							args[0] = "direction_precision";
						if (info[7] == null || info[7].equals(""))
							args[0] = "acq_period";
						if (info[8] == null || info[8].equals(""))
							args[0] = "speed_correction_coefficient";
						if (info[9] == null || info[9].equals(""))
							args[0] = "speed_correction_offset";
						if (info[10] == null || info[10].equals(""))
							args[0] = "speed_range_high";
						if (info[11] == null || info[11].equals(""))
							args[0] = "direction_correction_coefficient";
						if (info[12] == null || info[12].equals(""))
							args[0] = "direction_correction_offset";

						resultString = propertyUtil.getLocalizedMessage(
								(String) getSession().getAttribute("locale"),
								"not_empty", args);
					} else {
						try {
							WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
							WindElement windElement = windAnalyzer.getWind();
							windElement.setConfig(
									new Boolean(info[6]).booleanValue(),
									info[0], new Double(info[1]).doubleValue(),
									new Integer(info[2]).intValue(),
									new Double(info[3]).doubleValue(),
									new Double(info[10]).doubleValue(),
									new Integer(info[4]).intValue(),
									new Double(info[5]).doubleValue(),
									new Integer(info[7]).intValue(),
									new Double(info[8]).doubleValue(),
									new Double(info[9]).doubleValue(),
									new Double(info[11]).doubleValue(),
									new Double(info[12]).doubleValue());

						} catch (NumberFormatException nfe) {
							resultString = propertyUtil.getProperty(
									(String) getSession()
											.getAttribute("locale"),
									"not_character");
						}
					}
				} else if (analyzer.getType().equals(Analyzer.Type.RAIN)) {
					// case rain analyzer
					/*
					 * info for rain element :
					 * 
					 * [0] : paramId
					 * 
					 * [1] : MeasureUnitName selected
					 * 
					 * [2] : MaxValue
					 * 
					 * [3] : MinValue
					 * 
					 * [4] : NumDec
					 * 
					 * [5] : enabled
					 * 
					 * [6] : ValueForEvent
					 * 
					 * [7] : AcqOnRisingEdge
					 */

					if (info[3] == null || info[2] == null || info[4] == null
							|| info[3].equals("") || info[2].equals("")
							|| info[4].equals("") || info[6] == null
							|| info[6].equals("")) {
						// error case
						String[] args = new String[1];
						if (info[3] == null || info[3].equals(""))
							args[0] = "min_value";
						if (info[2] == null || info[2].equals(""))
							args[0] = "max_value";
						if (info[4] == null || info[4].equals(""))
							args[0] = "num_dec";
						if (info[6] == null || info[6].equals(""))
							args[0] = "value_for_event";

						resultString = propertyUtil.getLocalizedMessage(
								(String) getSession().getAttribute("locale"),
								"not_empty", args);
					} else {
						try {
							RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
							CounterElement rainElement = rainAnalyzer
									.getRainElement();

							rainElement.setConfig(
									new Boolean(info[5]).booleanValue(),
									info[1], new Double(info[3]).doubleValue(),
									new Double(info[2]).doubleValue(),
									new Integer(info[4]).intValue(),
									new Double(info[6]).doubleValue(),
									new Boolean(info[7]).booleanValue());

						} catch (NumberFormatException nfe) {
							resultString = propertyUtil.getProperty(
									(String) getSession()
											.getAttribute("locale"),
									"not_character");
						}
					}
				} else if (analyzer.getType().equals(Analyzer.Type.AVG)) {
					// case of avg analyzer
					/*
					 * info for avg element :
					 * 
					 * [0] : paramId
					 * 
					 * [1] : AcquisitionMeasureUnitName selected
					 * 
					 * [2] : MaxValue
					 * 
					 * [3] : MinValue
					 * 
					 * [4] : NumDec
					 * 
					 * [5] : enabled
					 * 
					 * [6] : AcqPeriod
					 * 
					 * [7] : CorrectionCoefficient
					 * 
					 * [8] : CorrectionOffset
					 * 
					 * [9] : RangeHigh
					 * 
					 * [10] : RangeLow
					 * 
					 * [11] : LinearizationCoefficient
					 * 
					 * [12] : LinearizationOffset
					 * 
					 * [13] : acqDelay
					 * 
					 * [14] : acqDuration
					 * 
					 * [15] : acqPeriodForAvg
					 * 
					 * [16] : AnalyzerMeasureUnitName selected
					 */

					if (info[3] == null || info[2] == null || info[4] == null
							|| info[3].equals("") || info[2].equals("")
							|| info[4].equals("") || info[7] == null
							|| info[6] == null || info[9] == null
							|| info[10] == null || info[11] == null
							|| info[12] == null || info[8] == null
							|| info[13] == null || info[14] == null
							|| info[15] == null || info[7].equals("")
							|| info[6].equals("") || info[9].equals("")
							|| info[10].equals("") || info[11].equals("")
							|| info[12].equals("") || info[8].equals("")
							|| info[13].equals("") || info[14].equals("")
							|| info[15].equals("")) {
						// error case
						String[] args = new String[1];
						if (info[3] == null || info[3].equals(""))
							args[0] = "min_value";
						if (info[2] == null || info[2].equals(""))
							args[0] = "max_value";
						if (info[4] == null || info[4].equals(""))
							args[0] = "num_dec";
						if (info[7] == null || info[7].equals(""))
							args[0] = "correction_coefficient";
						if (info[6] == null || info[6].equals(""))
							args[0] = "acq_period";
						if (info[8] == null || info[8].equals(""))
							args[0] = "correction_offset";
						if (info[9] == null || info[9].equals(""))
							args[0] = "range_high";
						if (info[10] == null || info[10].equals(""))
							args[0] = "range_low";
						if (info[11] == null || info[11].equals(""))
							args[0] = "linearization_coefficient";
						if (info[12] == null || info[12].equals(""))
							args[0] = "linearization_offset";
						if (info[13] == null || info[13].equals(""))
							args[0] = "acq_delay";
						if (info[14] == null || info[14].equals(""))
							args[0] = "acq_duration";
						if (info[15] == null || info[15].equals(""))
							args[0] = "acq_period_for_Avg";

						resultString = propertyUtil.getLocalizedMessage(
								(String) getSession().getAttribute("locale"),
								"not_empty", args);
					} else {
						try {
							AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
							AvgElement avgElement = avgAnalyzer
									.getElement(info[0]);
							avgElement.setConfig(
									new Boolean(info[5]).booleanValue(),
									info[1], new Double(info[3]).doubleValue(),
									new Double(info[2]).doubleValue(),
									new Integer(info[4]).intValue(), info[16],
									new Integer(info[6]).intValue(),
									new Double(info[7]).doubleValue(),
									new Double(info[8]).doubleValue(),
									new Double(info[11]).doubleValue(),
									new Double(info[12]).doubleValue(),
									new Double(info[10]).doubleValue(),
									new Double(info[9]).doubleValue(),
									new Integer(info[13]).intValue(),
									new Integer(info[14]).intValue(),
									new Integer(info[15]).intValue());
						} catch (NumberFormatException nfe) {
							resultString = propertyUtil.getProperty(
									(String) getSession()
											.getAttribute("locale"),
									"not_character");
						}
					}
				}

			} catch (ConfigException ce) {
				resultString = ce.getLocalizedMessage(getLocale());
			}
		} else {
			resultString = SESSION_ENDED;
		}
		return resultString;
	}// end setVerifyElementFields

	public String setVerifyAnalyzerFaultAndDataValidFields(String id,
			String[] faultInfo, String[] dataValidInfo)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		String resultString = null;
		/*
		 * faultInfo: [0] : enabled/disabled [1] : activeHigh | acqPeriod for
		 * dpa analyzer only
		 */
		Analyzer analyzer = config.getStation().getAnalyzer(id);
		if (analyzer instanceof IOItfAnalyzer) {
			IOItfAnalyzer aiAnaliyzer = (IOItfAnalyzer) analyzer;
			if (faultInfo != null) {
				DigitalAlarm digitalAlarm = aiAnaliyzer.getFault();
				if (digitalAlarm == null) {
					// case of new fault
					digitalAlarm = aiAnaliyzer.makeNewFault();
					aiAnaliyzer.setFault(digitalAlarm);
				}
				digitalAlarm.setConfig(new Boolean(faultInfo[0]), new Boolean(
						faultInfo[1]));
			}
			if (dataValidInfo != null) {
				DataValidAlarm dataValidAlarm = aiAnaliyzer.getDataValid();
				if (dataValidAlarm == null) {
					// case of new dataValid
					dataValidAlarm = aiAnaliyzer.makeNewDataValid();
					aiAnaliyzer.setDataValid(dataValidAlarm);
				}
				dataValidAlarm.setConfig(new Boolean(dataValidInfo[0]),
						new Boolean(dataValidInfo[1]), new Boolean(
								dataValidInfo[2]));
			}
		} else if (analyzer instanceof DataPortAnalyzer) {
			DataPortAnalyzer dpaAnaliyzer = (DataPortAnalyzer) analyzer;
			DataPortAlarm dpaAlarm = dpaAnaliyzer.getFault();
			// the fault is always existent
			try {
				dpaAlarm.setConfig(new Boolean(faultInfo[0]), new Integer(
						faultInfo[1]));
			} catch (NumberFormatException e) {
				resultString = propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "not_character");
			} catch (ConfigException ce) {
				resultString = ce.getLocalizedMessage(getLocale());
			}
		}
		return resultString;
	}// end setVerifyAnalyzerFaultAndDataValidFields

	public String verifyInsertNewAnalyzer(String id)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		Analyzer analyzer = config.getStation().getAnalyzer(id);
		if (config.getStation().isNewAnalyzer(analyzer)) {
			config.getStation().insertNewAnalyzer();
			return config.getStation().getAnalyzer(id).getType().getName();
		}
		return null;
	}

	@Override
	public boolean verifyDpaIsActive(String analyzerId, String paramId)
			throws SessionExpiredException {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			throw new SessionExpiredException();
		return isElementActiveInRunningConfig(analyzerId, paramId);
	}

	public String[] verifyInsertNewElement(String analyzerId, String paramId) {
		String[] resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			resultString = new String[2];
			Analyzer analyzer = config.getStation().getAnalyzer(analyzerId);
			if (analyzer.getType().equals(Analyzer.Type.SAMPLE)) {
				SampleAnalyzer sample = (SampleAnalyzer) analyzer;
				SampleElement sampleElement = sample.getElement(paramId);
				if (sample.isNewElement(sampleElement)) {
					sample.insertNewElement();
					resultString[0] = paramId;
				} else
					resultString[0] = null;
				resultString[1] = Boolean
						.toString(isElementActiveInRunningConfig(analyzerId,
								paramId));
			}
			if (analyzer.getType().equals(Analyzer.Type.AVG)) {
				AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
				AvgElement avgElement = avgAnalyzer.getElement(paramId);
				if (avgAnalyzer.isNewElement(avgElement)) {
					avgAnalyzer.insertNewElement();
					resultString[0] = paramId;
				} else
					resultString[0] = null;
				resultString[1] = Boolean
						.toString(isElementActiveInRunningConfig(analyzerId,
								paramId));
			}
		} else {
			resultString = new String[1];
			resultString[0] = SESSION_ENDED;
		}
		return resultString;

	}// end verifyInsertNewElement

	private boolean isElementActiveInRunningConfig(String analyzerId,
			String paramId) {
		Periferico perifericoApp = Periferico.getInstance();
		Station station = perifericoApp.getStation();
		if (station == null)
			return false;
		Analyzer analyzer = station.getAnalyzer(analyzerId);
		if (analyzer == null || !analyzer.isEnabled())
			return false;
		Element element = analyzer.getElement(paramId);
		if (element == null)
			return false;
		return element.isEnabled();
	}

	public String[][] getAnalyzerStatusList() {
		String resultString[][] = null;

		Status[] listStatus = Analyzer.Status.values();
		resultString = new String[listStatus.length][2];
		for (int i = 0; i < listStatus.length; i++) {
			resultString[i][0] = listStatus[i].toString();
			resultString[i][1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), listStatus[i].toString()
					.toLowerCase());
		}

		return resultString;

	}// end getAnalyzerStatusList

	/*
	 * Methods for board management
	 */

	public String[][] getBoards() {
		String[][] boardMatrix = null;
		// TODO: capire se qualcosa di questo puo' essere null e controllare
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			List<Board> boardList = config.getBoardList().getBoards();
			boardMatrix = new String[boardList.size()][5];
			for (int i = 0; i < boardList.size(); i++) {
				Board board = boardList.get(i);
				boardMatrix[i][0] = board.getBrand();
				boardMatrix[i][1] = board.getModel();
				boardMatrix[i][2] = board.getBoardStatus().toString();
				boardMatrix[i][3] = board.getIdAsString();
				boolean pciBoard = false;
				if (board instanceof PCIBoard)
					pciBoard = true;
				boardMatrix[i][4] = new Boolean(pciBoard).toString();
			}
		} else {
			boardMatrix = new String[1][1];
			boardMatrix[0][0] = SESSION_ENDED;
		}

		return boardMatrix;
	}

	public String[] getBoardBrandsCfg() {
		// TODO: capire se c'e' qualcosa a null
		Periferico perifericoApp = Periferico.getInstance();
		BoardDescriptors boardDescriptors = perifericoApp.getBoardsCfg()
				.getBoardDescriptors();
		String[] brandsArray = (String[]) boardDescriptors.getBrands().toArray(
				new String[boardDescriptors.getBrands().size()]);

		return brandsArray;
	}

	public String[] getBoardModelsCfg(String brand) {
		// TODO: capire se c'e' qualcosa a null
		Periferico perifericoApp = Periferico.getInstance();
		BoardDescriptors boardDescriptors = perifericoApp.getBoardsCfg()
				.getBoardDescriptors();
		String[] modelsArray = (String[]) boardDescriptors.getModels(brand,
				true).toArray(new String[0]);

		return modelsArray;
	}

	public String[][] getDetectedDevice() {
		String[][] deviceMatrix = null;
		// TODO: capire se qualcosa di questo puo' essere null e controllare
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManagementForUI = perifericoApp
				.getBoardManagementForUI();
		BoardDescriptors boardDescriptors = perifericoApp.getBoardsCfg()
				.getBoardDescriptors();
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			BoardList boardList = config.getBoardList();
			List<PCIDevice> pciDeviceList = new ArrayList<PCIDevice>();
			try {
				pciDeviceList = boardManagementForUI
						.getNotConfiguredDevices(boardList);
			} catch (BoardsException bEx) {
				deviceMatrix = new String[1][1];
				deviceMatrix[0][0] = bEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			deviceMatrix = new String[pciDeviceList.size()][3];
			for (int i = 0; i < pciDeviceList.size(); i++) {
				PCIDevice pciDevice = pciDeviceList.get(i);
				deviceMatrix[i][0] = pciDevice.toString();
				PCIBoardDescriptor pciBoardDescriptor = boardDescriptors.get(
						pciDevice.getVendorId(), pciDevice.getDeviceId());
				deviceMatrix[i][1] = pciBoardDescriptor.getBrand();
				deviceMatrix[i][2] = pciBoardDescriptor.getModel();
			}
		} else {
			deviceMatrix = new String[1][1];
			deviceMatrix[0][0] = SESSION_ENDED;
		}

		return deviceMatrix;
	}

	public String[] getPciDevice(String id) {
		String[] resStr = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Board board = config.getBoardList().getBoard(id);
			resStr = new String[4];
			resStr[0] = board.getBrand();
			resStr[1] = board.getModel();
			resStr[2] = ((PCIBoard) (board)).getPciDevice().toString();
			resStr[3] = id;
		} else {
			resStr = new String[1];
			resStr[0] = SESSION_ENDED;
		}
		return resStr;
	}

	public String deleteBoard(String id) {
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		boolean result = false;
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			result = boardManage.deleteBoard(config.getBoardList(), id, config
					.getStation().getListIOUser());
			resultString = new Boolean(result).toString();
		} else
			resultString = SESSION_ENDED;
		return (resultString);
	}

	public String unbindBoard(String id) {
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		boolean result = false;
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			result = boardManage.unbindBoard(config.getBoardList(), id, config
					.getStation().getListIOUser());
			resultString = new Boolean(result).toString();
		} else
			resultString = SESSION_ENDED;
		return (resultString);
	}

	public String associateBoard(Integer boardIndex, String deviceStrId) {
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			BoardList boardList = config.getBoardList();
			PCIDevice device = new PCIDevice(deviceStrId);
			try {
				boardManage.bindPCIBoardToDevice(boardList, boardIndex, device,
						config.getStation().getListIOUser());
			} catch (BoardsException ex) {
				resultString = ex.getLocalizedMessage((String) getSession()
						.getAttribute("locale"));
			}
		} else
			resultString = SESSION_ENDED;
		return resultString;
	}

	public String[] getBindableBoardsIndexes(String deviceStrId) {
		String[] boardIndexes = null;
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			BoardList boardList = config.getBoardList();
			PCIDevice device = new PCIDevice(deviceStrId);
			List<Integer> pciBoardIndexList = new ArrayList<Integer>();
			try {
				pciBoardIndexList = boardManage.getBindableBoardIndexes(
						boardList, device);
			} catch (BoardsException bEx) {
				boardIndexes = new String[1];
				boardIndexes[0] = bEx.getLocalizedMessage((String) getSession()
						.getAttribute("locale"));
				return boardIndexes;
			}
			boardIndexes = new String[pciBoardIndexList.size() + 1];
			boardIndexes[0] = null;
			int i = 1;
			for (Integer pciBoardIndex : pciBoardIndexList)
				boardIndexes[i++] = pciBoardIndex.toString();
		} else {
			boardIndexes = new String[1];
			boardIndexes[0] = SESSION_ENDED;
		}
		return boardIndexes;
	}

	public String getBoardStatus(String id) {
		String result = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null)
			result = config.getBoardList().getBoard(id).getBoardStatus()
					.toString();
		else
			result = SESSION_ENDED;
		return result;
	}

	public String[][] getBoardChannelInfo(String id) {
		int boardInfoNumber = 0;
		String[][] resMatrix = null;
		List<List<String>> globalChannelInfoList = new ArrayList<List<String>>();
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			resMatrix = new String[boardInfoNumber][3];
			List<String> channelInfoList;
			List<AISubdevice> aiSubdeviceList = config.getBoardList()
					.getBoard(id).getListAISubdevice();
			ListIterator<AISubdevice> li_aiSubdevice = aiSubdeviceList
					.listIterator();
			while (li_aiSubdevice.hasNext()) {
				channelInfoList = new ArrayList<String>();
				AISubdevice aiSubdevice = li_aiSubdevice.next();
				channelInfoList.add("ANALOG_INPUT");
				channelInfoList.add(new Integer(aiSubdevice.getSubdevice())
						.toString());
				channelInfoList.add(new Integer(aiSubdevice.getListAI().size())
						.toString());
				globalChannelInfoList.add(channelInfoList);
			}

			List<DISubdevice> diSubdeviceList = config.getBoardList()
					.getBoard(id).getListDISubdevice();
			ListIterator<DISubdevice> li_diSubdevice = diSubdeviceList
					.listIterator();
			while (li_diSubdevice.hasNext()) {
				channelInfoList = new ArrayList<String>();
				DISubdevice diSubdevice = li_diSubdevice.next();
				channelInfoList.add("DIGITAL_INPUT");
				channelInfoList.add(new Integer(diSubdevice.getSubdevice())
						.toString());
				channelInfoList.add(new Integer(diSubdevice.getListDI().size())
						.toString());
				globalChannelInfoList.add(channelInfoList);
			}

			List<DIOSubdevice> dioSubdeviceList = config.getBoardList()
					.getBoard(id).getListDIOSubdevice();
			ListIterator<DIOSubdevice> li_dioSubdevice = dioSubdeviceList
					.listIterator();
			while (li_dioSubdevice.hasNext()) {
				channelInfoList = new ArrayList<String>();
				DIOSubdevice dioSubdevice = li_dioSubdevice.next();
				channelInfoList.add("DIGITAL_INPUT_OUTPUT");
				channelInfoList.add(new Integer(dioSubdevice.getSubdevice())
						.toString());
				channelInfoList.add(new Integer(dioSubdevice.getListDIO()
						.size()).toString());
				globalChannelInfoList.add(channelInfoList);
			}

			List<DOSubdevice> doSubdeviceList = config.getBoardList()
					.getBoard(id).getListDOSubdevice();
			ListIterator<DOSubdevice> li_doSubdevice = doSubdeviceList
					.listIterator();
			while (li_doSubdevice.hasNext()) {
				channelInfoList = new ArrayList<String>();
				DOSubdevice doSubdevice = li_doSubdevice.next();
				channelInfoList.add("DIGITAL_OUTPUT");
				channelInfoList.add(new Integer(doSubdevice.getSubdevice())
						.toString());
				channelInfoList.add(new Integer(doSubdevice.getListDO().size())
						.toString());
				globalChannelInfoList.add(channelInfoList);
			}

			boardInfoNumber = globalChannelInfoList.size();
			resMatrix = new String[boardInfoNumber][3];
			for (int i = 0; i < boardInfoNumber; i++) {
				List<String> tmpStrList = globalChannelInfoList.get(i);
				resMatrix[i] = tmpStrList.toArray(resMatrix[i]);
			}
		} else {
			resMatrix = new String[1][1];
			resMatrix[0][0] = SESSION_ENDED;
		}
		return resMatrix;
	}

	public String[][] getDriverParams(String brand, String model, String id) {
		int maxColumnNumber = 0;
		int driverParamsNumber = 0;
		String[][] resStrMatrix = null;
		Periferico perifericoApp = Periferico.getInstance();
		BoardDescriptors boardDescriptors = perifericoApp.getBoardsCfg()
				.getBoardDescriptors();
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			resStrMatrix = new String[driverParamsNumber][maxColumnNumber];
			BoardDescriptor boardDescriptor = boardDescriptors
					.get(brand, model);
			if (boardDescriptor != null) {
				List<List<String>> allDParamFieldsStr = new ArrayList<List<String>>();
				List<DriverParamDescriptor> driverParamDescriptors = boardDescriptor
						.getDriverParamDescriptors();
				driverParamsNumber = driverParamDescriptors.size();
				for (int i = 0; i < driverParamsNumber; i++) {
					DriverParamDescriptor dParamDesc = driverParamDescriptors
							.get(i);
					String dParamValue = null;
					if (id != null) {
						Board board = config.getBoardList().getBoard(id);
						DriverParam dParam = board
								.getDriverParamFromName(dParamDesc.getName());
						if (dParam != null)
							dParamValue = dParam.getValue();
					}
					List<String> dParamFieldsStr = new ArrayList<String>();
					dParamFieldsStr.add(new Integer(dParamDesc.getIndex())
							.toString());
					dParamFieldsStr.add(dParamDesc.getName());
					dParamFieldsStr.add(dParamValue);
					dParamFieldsStr.add(dParamDesc.getHelp());
					dParamFieldsStr.add(new Boolean(dParamDesc.isOptional())
							.toString());
					dParamFieldsStr.addAll(dParamDesc.getAllowedValues());
					if (dParamFieldsStr.size() > maxColumnNumber)
						maxColumnNumber = dParamFieldsStr.size();
					allDParamFieldsStr.add(dParamFieldsStr);
				}
				resStrMatrix = new String[driverParamsNumber][maxColumnNumber];
				for (int i = 0; i < driverParamsNumber; i++) {
					List<String> tmpStrList = allDParamFieldsStr.get(i);
					resStrMatrix[i] = tmpStrList.toArray(resStrMatrix[i]);
				}
			}
		} else {
			resStrMatrix = new String[1][1];
			resStrMatrix[0][0] = SESSION_ENDED;
		}

		return resStrMatrix;
	}

	public String[] getPCIBoardVersionList(String deviceStrId) {
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null) {
			String[] result = new String[1];
			result[0] = SESSION_ENDED;
			return result;
		}
		PCIDevice device = new PCIDevice(deviceStrId);
		PCIBoardDescriptor pbd = boardManage.getPCIBoardDescriptor(device);
		if (pbd == null)
			return new String[0];
		List<PCIBoardVersion> pciBoardVersions = pbd.getPciBoardVersions();
		String[] versions = new String[pciBoardVersions.size()];
		int i = 0;
		for (PCIBoardVersion pciBoardVersion : pciBoardVersions)
			versions[i++] = pciBoardVersion.getVersion();
		return versions;
	}

	public String getPCIBoardVersion(String boardId) {
		Config config = (Config) (getSession().getAttribute("config"));
		if (config == null)
			return SESSION_ENDED;
		String version = ((PCIBoard) (config.getBoardList().getBoard(boardId)))
				.getVersion();
		return version;
	}

	public String setVerifyPCIBoardFields(String id, String brand,
			String model, String version, String[][] driverParamsMatrix) {
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			PCIBoard board = (PCIBoard) config.getBoardList().getBoard(id);
			List<DriverParam> driverParams = new ArrayList<DriverParam>();
			for (int i = 0; i < driverParamsMatrix.length; i++)
				driverParams.add(new DriverParam(driverParamsMatrix[i][0],
						driverParamsMatrix[i][1]));
			try {
				board.setConfig(brand, model, version, driverParams);
			} catch (ConfigException ex) {
				resultString = ex.getLocalizedMessage(getLocale());
			}
		} else {
			resultString = SESSION_ENDED;
		}
		return resultString;
	}

	public void setVerifyBoardFields(String id, String brand, String model,
			String[][] driverParamsMatrix) throws SessionExpiredException,
			UserParamsException {
		Config config = getConfig();
		Board board = config.getBoardList().getBoard(id);
		List<DriverParam> driverParams = new ArrayList<DriverParam>();
		for (int i = 0; i < driverParamsMatrix.length; i++)
			driverParams.add(new DriverParam(driverParamsMatrix[i][0],
					driverParamsMatrix[i][1]));
		try {
			board.setConfig(brand, model, driverParams);
		} catch (ConfigException ex) {
			throw new UserParamsException(ex.getLocalizedMessage(getLocale()));
		}
		return;
	}// end setVerifyISABoardFields

	public String verifyInsertNewBoard(String id) {
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			Board board = config.getBoardList().getBoard(id);
			if (config.getBoardList().isNewBoard(board))
				config.getBoardList().insertNewBoard();
		} else {
			resultString = SESSION_ENDED;
		}
		return resultString;

	}// end verifyInsertNewBoard

	// TODO: PFV rivedere la new delle schede
	public String setNewBoard(Boolean autoDetectable, String brand,
			String model, String autoDetectStr) throws SessionExpiredException,
			UserParamsException {
		BoardManagementForUI boardMan = Periferico.getInstance()
				.getBoardManagementForUI();
		Config config = getConfig();
		Board board = null;
		try {
			if (autoDetectable) {
				PCIDevice device = new PCIDevice(autoDetectStr);
				board = boardMan.newPCIBoard(config.getBoardList(), device);
			} else
				board = boardMan.newBoard(config.getBoardList(), brand, model);
			return board.getIdAsString();
		} catch (BoardsException e) {
			throw new UserParamsException(
					e.getLocalizedMessage((String) getSession().getAttribute(
							"locale")));
		}
	}

	public void initBoard(Boolean type, String id)
			throws SessionExpiredException, UserParamsException {
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		Config config = getConfig();
		Board board = config.getBoardList().getBoard(id);
		try {
			if (type) {
				boardManage.initPCIBoard(config.getBoardList(),
						(PCIBoard) board);
			} else {
				boardManage.initBoard(config.getBoardList(), board);
			}
		} catch (BoardsException e) {
			throw new UserParamsException(e.getLocalizedMessage(getLocale()));
		}
	}

	@Override
	public boolean verifySamePCIBoardConfig(String id, String brand,
			String model, String version, String[][] driverParamsStr)
			throws SessionExpiredException {
		Config config = getConfig();
		PCIBoard pciBoard = (PCIBoard) config.getBoardList().getBoard(id);
		List<DriverParam> driverParams = new ArrayList<DriverParam>();
		for (int i = 0; i < driverParamsStr.length; i++) {
			DriverParam dParam = new DriverParam();
			dParam.setName(driverParamsStr[i][0]);
			dParam.setValue(driverParamsStr[i][1]);
			driverParams.add(dParam);
		}
		return pciBoard.isSameConfig(brand, model, version, driverParams);
	}

	@Override
	public boolean verifySameBoardConfig(String id, String brand, String model,
			String[][] driverParamsStr) throws SessionExpiredException {
		Config config = getConfig();
		Board board = config.getBoardList().getBoard(id);
		List<DriverParam> driverParams = new ArrayList<DriverParam>();
		for (int i = 0; i < driverParamsStr.length; i++) {
			DriverParam dParam = new DriverParam();
			dParam.setName(driverParamsStr[i][0]);
			dParam.setValue(driverParamsStr[i][1]);
			driverParams.add(dParam);
		}
		return board.isSameConfig(brand, model, driverParams);
	} // end verifySameBoardConfig

	public String[][] setSubdeviceFields(String boardId, String type,
			String deviceId) {
		String[][] subdeviceFields = null;
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			List<List<String>> fields = new ArrayList<List<String>>();
			/*
			 * logger.debug("boardId vale:" + boardId + " type vale:" + type +
			 * " deviceId vale:" + deviceId);
			 */
			if ("ANALOG_INPUT".equals(type)) {
				AISubdevice aiSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getAISubdevice(new Integer(deviceId).intValue());
				/*
				 * logger.debug("AIsubdevice vale:" + aiSubdevice + " listai
				 * size vale:" + aiSubdevice.getListAI().size());
				 */
				for (int i = 0; i < aiSubdevice.getListAI().size(); i++) {
					List<String> aiFields = new ArrayList<String>();
					AnalogInput ai = aiSubdevice.getListAI().get(i);
					// logger.debug("ai.getchannel vale:" +
					// ai.getChannel());
					aiFields.add(new Integer(ai.getChannel()).toString());
					IOUser ioUser = boardManage.getBindedIOUser(aiSubdevice
							.getChannel(i));
					if (ioUser != null) {
						/*
						 * logger.debug("ioUser vale:" + ioUser.getBindLabel());
						 */
						aiFields.add(ioUser.getBindLabel());
					} else
						aiFields.add(null);
					// TODO: implementare get di range
					aiFields.add(null);
					fields.add(aiFields);
				}
			} else if ("DIGITAL_INPUT".equals(type)) {

				DISubdevice diSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDISubdevice(new Integer(deviceId).intValue());
				for (int i = 0; i < diSubdevice.getListDI().size(); i++) {
					List<String> diFields = new ArrayList<String>();
					DigitalInput di = diSubdevice.getListDI().get(i);
					diFields.add(new Integer(di.getChannel()).toString());
					IOUser ioUser = boardManage.getBindedIOUser(diSubdevice
							.getChannel(i));
					if (ioUser != null)
						diFields.add(ioUser.getBindLabel());
					else
						diFields.add(null);
					// TODO: implementare get di range
					diFields.add(null);
					fields.add(diFields);
				}
			} else if ("DIGITAL_INPUT_OUTPUT".equals(type)) {
				DIOSubdevice dioSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDIOSubdevice(new Integer(deviceId).intValue());
				for (int i = 0; i < dioSubdevice.getListDIO().size(); i++) {
					List<String> dioFields = new ArrayList<String>();
					DigitalIO dIO = dioSubdevice.getListDIO().get(i);
					dioFields.add(new Integer(dIO.getChannel()).toString());
					IOUser ioUser = boardManage.getBindedIOUser(dioSubdevice
							.getChannel(i));
					if (ioUser != null)
						dioFields.add(ioUser.getBindLabel());
					else
						dioFields.add(null);
					// TODO: implementare get di range
					dioFields.add(null);
					fields.add(dioFields);
				}
			} else if ("DIGITAL_OUTPUT".equals(type)) {
				DOSubdevice doSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDOSubdevice(new Integer(deviceId).intValue());
				for (int i = 0; i < doSubdevice.getListDO().size(); i++) {
					List<String> doFields = new ArrayList<String>();
					DigitalOutput dO = doSubdevice.getListDO().get(i);
					doFields.add(new Integer(dO.getChannel()).toString());
					IOUser ioUser = boardManage.getBindedIOUser(doSubdevice
							.getChannel(i));
					if (ioUser != null)
						doFields.add(ioUser.getBindLabel());
					else
						doFields.add(null);
					// TODO: implementare get di range
					doFields.add(null);
					fields.add(doFields);
				}
			}
			// logger.debug("fields.size:" + fields.size());
			subdeviceFields = new String[fields.size()][3];
			for (int i = 0; i < fields.size(); i++) {
				List<String> tmpStrList = fields.get(i);
				subdeviceFields[i] = tmpStrList.toArray(subdeviceFields[i]);
			}
		} else {
			subdeviceFields = new String[1][1];
			subdeviceFields[0][0] = SESSION_ENDED;
		}
		return subdeviceFields;
	} // end setSubdeviceFields

	public String[][] getBindableIOUsers(String boardId, String type,
			String deviceId, String channelStr) {
		String resStrings[][] = null;
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		List<IOUser> ioUserList = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			IOProvider ioProvider = null;
			if ("ANALOG_INPUT".equals(type)) {
				AISubdevice aiSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getAISubdevice(new Integer(deviceId).intValue());
				ioProvider = aiSubdevice.getChannel(new Integer(channelStr)
						.intValue());
				// logger.debug("ioProvider:" + ioProvider);
			}
			if ("DIGITAL_INPUT".equals(type)) {
				DISubdevice diSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDISubdevice(new Integer(deviceId).intValue());
				ioProvider = diSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			if ("DIGITAL_INPUT_OUTPUT".equals(type)) {
				DIOSubdevice dioSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDIOSubdevice(new Integer(deviceId).intValue());
				ioProvider = dioSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			if ("DIGITAL_OUTPUT".equals(type)) {
				DOSubdevice doSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDOSubdevice(new Integer(deviceId).intValue());
				ioProvider = doSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			ioUserList = boardManage.getBindableIOUsers(ioProvider, config
					.getStation().getListIOUser());
			// logger.debug("ioUsersList:" + ioUserList);

			resStrings = new String[ioUserList.size()][2];
			for (int i = 0; i < ioUserList.size(); i++) {
				resStrings[i][0] = ioUserList.get(i).getBindLabel();
				resStrings[i][1] = ioUserList.get(i).getBindIdentifier();
			}
		} else {
			resStrings = new String[1][1];
			resStrings[0][0] = SESSION_ENDED;
		}
		// logger.debug("resStrings.size:" + resStrings.length);
		return resStrings;
	} // end getBindableIOUsers

	public String bindIOUser(String boardId, String type, String deviceId,
			String channelStr, String ioUserStr) {
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			IOProvider ioProvider = null;
			if ("ANALOG_INPUT".equals(type)) {
				AISubdevice aiSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getAISubdevice(new Integer(deviceId).intValue());
				ioProvider = aiSubdevice.getChannel(new Integer(channelStr)
						.intValue());
				// logger.debug("ioProvider:" + ioProvider);
			}
			if ("DIGITAL_INPUT".equals(type)) {
				DISubdevice diSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDISubdevice(new Integer(deviceId).intValue());
				ioProvider = diSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			if ("DIGITAL_INPUT_OUTPUT".equals(type)) {
				DIOSubdevice dioSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDIOSubdevice(new Integer(deviceId).intValue());
				ioProvider = dioSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			if ("DIGITAL_OUTPUT".equals(type)) {
				DOSubdevice doSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDOSubdevice(new Integer(deviceId).intValue());
				ioProvider = doSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			List<IOUser> ioUserList = boardManage.getBindableIOUsers(
					ioProvider, config.getStation().getListIOUser());
			IOUser ioUser = boardManage.findIOUser(ioUserList, ioUserStr);
			boardManage.bindIOUser(ioProvider, ioUser);
		} else
			resultString = SESSION_ENDED;
		return (resultString);
	} // end bindIOUser

	public String unbindChannel(String boardId, String type, String deviceId,
			String channelStr) {
		Periferico perifericoApp = Periferico.getInstance();
		BoardManagementForUI boardManage = perifericoApp
				.getBoardManagementForUI();
		String resultString = null;
		Config config = (Config) (getSession().getAttribute("config"));
		if (config != null) {
			IOProvider ioProvider = null;
			if ("ANALOG_INPUT".equals(type)) {
				AISubdevice aiSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getAISubdevice(new Integer(deviceId).intValue());
				ioProvider = aiSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			if ("DIGITAL_INPUT".equals(type)) {
				DISubdevice diSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDISubdevice(new Integer(deviceId).intValue());
				ioProvider = diSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			if ("DIGITAL_INPUT_OUTPUT".equals(type)) {
				DIOSubdevice dioSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDIOSubdevice(new Integer(deviceId).intValue());
				ioProvider = dioSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			if ("DIGITAL_OUTPUT".equals(type)) {
				DOSubdevice doSubdevice = config.getBoardList()
						.getBoard(boardId)
						.getDOSubdevice(new Integer(deviceId).intValue());
				ioProvider = doSubdevice.getChannel(new Integer(channelStr)
						.intValue());
			}
			boardManage.unbind(ioProvider);
		} else
			resultString = SESSION_ENDED;
		return (resultString);
	}

	/*
	 * Method for viewing data
	 */

	public String[][] getStationStatusFields() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FMT);
		String[][] stationStatusFields = null;
		Periferico perifericoApp = Periferico.getInstance();
		List<ContainerAlarm> caList = new ArrayList<ContainerAlarm>(
				perifericoApp.getStation().getContainer().getListAlarm());
		Collections.sort(caList, new Comparator<ContainerAlarm>() {
			@Override
			public int compare(ContainerAlarm o1, ContainerAlarm o2) {
				return o1.compareNameAndDescription(o2);
			}
		});
		stationStatusFields = new String[caList.size()][6];
		boolean errorFound = false;
		for (int i = 0; i < caList.size() && !errorFound; i++) {
			ContainerAlarm ca = caList.get(i);
			String alarmNameId = ca.getAlarm().getAlarmNameId();
			AlarmName alarmName = perifericoApp.getCommonCfg().getAlarmName(
					alarmNameId);
			String alarmNameName = alarmName == null ? "" : alarmName.getName();
			stationStatusFields[i][0] = ca.getIdAsString();
			stationStatusFields[i][1] = alarmNameId;
			stationStatusFields[i][2] = alarmNameName;
			stationStatusFields[i][3] = ca.getDescription();
			if (ca instanceof DigitalContainerAlarm) {
				BinaryStatus status = ((DigitalContainerAlarm) (ca))
						.getStatus();
				if (status != null) {
					stationStatusFields[i][4] = new Boolean(status.getStatus())
							.toString();
					stationStatusFields[i][5] = sdf.format(status
							.getTimestamp());
				}
			} else if (ca instanceof TriggerContainerAlarm) {
				AlarmStatus status = ((TriggerContainerAlarm) (ca)).getStatus();
				if (status != null) {
					stationStatusFields[i][4] = status.getStatus().name();
					stationStatusFields[i][5] = sdf.format(status
							.getTimestamp());
				}
			}
		}
		return stationStatusFields;
	} // end getStationStatusFields

	public String[][] getHistoryStationStatusFields(String alarmIdStr,
			String startDateStr, String startHourStr, String endDateStr,
			String endHourStr) {
		String[][] historyStationStatusFields = null;

		// create and verify startDate and endDate
		SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
		SimpleDateFormat sdfTimestamp = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss.SSS");
		Calendar startCal = new GregorianCalendar();
		Calendar endCal = new GregorianCalendar();
		Date startDate = null;
		try {
			startDate = sdfDay.parse(startDateStr);
		} catch (ParseException e) {
			historyStationStatusFields = new String[1][1];
			historyStationStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"start_date_error");
			return historyStationStatusFields;
		}
		startCal.setTime(startDate);
		Date startHour = null;
		try {
			startHour = sdfHour.parse(startHourStr);
		} catch (ParseException e) {
			historyStationStatusFields = new String[1][1];
			historyStationStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"start_hour_error");
			return historyStationStatusFields;
		}
		Calendar hourCal = new GregorianCalendar();
		hourCal.setTime(startHour);
		startCal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
		startCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
		Date endDate = null;
		try {
			endDate = sdfDay.parse(endDateStr);
		} catch (ParseException e) {
			historyStationStatusFields = new String[1][1];
			historyStationStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"end_date_error");
			return historyStationStatusFields;
		}
		endCal.setTime(endDate);
		Date endHour = null;
		try {
			endHour = sdfHour.parse(endHourStr);
		} catch (ParseException e) {
			historyStationStatusFields = new String[1][1];
			historyStationStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"end_hour_error");
			return historyStationStatusFields;
		}
		hourCal = new GregorianCalendar();
		hourCal.setTime(endHour);
		endCal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
		endCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);
		endCal.set(Calendar.SECOND, 0);
		endCal.set(Calendar.MILLISECOND, 0);
		if (!endCal.after(startCal)) {
			historyStationStatusFields = new String[1][1];
			historyStationStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"incoherent_calendar");
			return historyStationStatusFields;
		}

		Periferico perifericoApp = Periferico.getInstance();
		StorageManager storageManager = perifericoApp.getStorageManager();
		List<List<String>> fieldsList = new ArrayList<List<String>>();
		ContainerAlarm ca = perifericoApp.getStation().getContainer()
				.getAlarm(alarmIdStr);
		Alarm alarm = ca.getAlarm();
		if (ca instanceof DigitalContainerAlarm) {
			try {
				List<BinaryStatus> statusList = storageManager
						.readDigitalCAData(ca.getId(), alarm.getAlarmNameId(),
								startCal.getTime(), false, endCal.getTime(),
								true, null);
				for (int i = 0; i < statusList.size(); i++) {
					List<String> singleAlarmFieldsList = new ArrayList<String>();
					BinaryStatus status = statusList.get(i);
					singleAlarmFieldsList.add(sdfTimestamp.format(status
							.getTimestamp()));
					singleAlarmFieldsList.add(new Boolean(status.getStatus())
							.toString());
					fieldsList.add(singleAlarmFieldsList);
				}
			} catch (StorageException stEx) {
				historyStationStatusFields = new String[1][1];
				historyStationStatusFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
		} else if (ca instanceof TriggerContainerAlarm) {
			try {
				List<AlarmStatus> statusList = storageManager
						.readTriggerCAData(ca.getId(), alarm.getAlarmNameId(),
								startCal.getTime(), true, endCal.getTime(),
								true, null);
				for (int i = 0; i < statusList.size(); i++) {
					List<String> singleAlarmFieldsList = new ArrayList<String>();
					AlarmStatus status = statusList.get(i);
					singleAlarmFieldsList.add(sdfTimestamp.format(status
							.getTimestamp()));
					singleAlarmFieldsList.add(status.getStatus().name());
					fieldsList.add(singleAlarmFieldsList);
				}
			} catch (StorageException stEx) {
				historyStationStatusFields = new String[1][1];
				historyStationStatusFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
		}
		historyStationStatusFields = new String[fieldsList.size()][2];
		for (int k = 0; k < fieldsList.size(); k++) {
			List<String> tmpStrList = fieldsList.get(k);
			historyStationStatusFields[k] = tmpStrList
					.toArray(historyStationStatusFields[k]);
		}

		return historyStationStatusFields;
	} // end getHistoryStationStatusFields

	public String[] getAnalyzerEvtArray() {
		return Analyzer.ARRAY_EVT;
	} // end getAnalyzerEvtArray

	public String[] getPerifericoStatusFields() {
		// perifericoStatus:
		// [0] : isOk
		// [1] : boardManagerInitStatus
		// [2] : configuredBoardsNumber
		// [3] : initializedBoardsNumber
		// [4] : loadConfigurationStatus
		// [5] : saveNewConfigurationStatus
		// [6] : acquisitionStarted
		// [7] : rootFsUsedSpacePercent
		// [8] : tmpFsUsedSpacePercent
		// [9] : dataFsUsedSpacePercent
		// [10] : usedSpaceAlarmThresholdPercent
		// [11] : usedSpaceWarningThresholdPercent
		// [12] : SMART status
		// [13] : RAID status
		// [14] : failedBoardBindingsNumber
		// [15] : enabledDataPortAnalyzersNumber
		// [16] : initializedDataPortDriversNumber
		// [17] : failedDataPortThreadsNumber
		// [18] : dpaOk
		// [19] : driverConfigsOK
		// [20] : dataWriteErrorCount
		// [21] : totalThreadFailures
		// [22] : currentThreadFailures
		// [23] : commonConfigFromCopStatus
		// [24] : dataInTheFuture
		// [25] : altitude
		// [26] : latitude
		// [27] : longitude
		// [28] : fix
		// [29] : timestamp
		// [30] : link to google maps

		Periferico perifericoApp = Periferico.getInstance();

		String[] perifericoStatus = null;
		if (!perifericoApp.getStation().isGpsInstalled())
			perifericoStatus = new String[25];
		else
			perifericoStatus = new String[31];

		PerifericoStatus status = perifericoApp.getStatus();
		perifericoStatus[0] = new Boolean(status.isOK()).toString();
		perifericoStatus[1] = status.getBoardManagerInitStatus() == null ? null
				: status.getBoardManagerInitStatus().toString();
		perifericoStatus[2] = status.getConfiguredBoardsNumber() == null ? null
				: status.getConfiguredBoardsNumber().toString();
		perifericoStatus[3] = status.getInitializedBoardsNumber() == null ? null
				: status.getInitializedBoardsNumber().toString();
		perifericoStatus[4] = status.getLoadConfigurationStatus() == null ? null
				: status.getLoadConfigurationStatus().toString();
		perifericoStatus[5] = (status.getSaveNewConfigurationStatus() == null ? null
				: status.getSaveNewConfigurationStatus().toString());
		perifericoStatus[6] = (status.getConfigActivationStatus() == null ? null
				: status.getConfigActivationStatus().toString());

		StorageManager storageManager = perifericoApp.getStorageManager();
		perifericoStatus[7] = (storageManager.getRootFsUsedSpacePercent() == null ? null
				: storageManager.getRootFsUsedSpacePercent().toString());
		perifericoStatus[8] = (storageManager.getTmpFsUsedSpacePercent() == null ? null
				: new Integer(storageManager.getTmpFsUsedSpacePercent())
						.toString());
		perifericoStatus[9] = (storageManager.getDataFsUsedSpacePercent() == null ? null
				: new Integer(storageManager.getDataFsUsedSpacePercent())
						.toString());
		perifericoStatus[10] = new Integer(
				storageManager.getUsedSpaceAlarmThresholdPercent()).toString();
		perifericoStatus[11] = new Integer(
				storageManager.getUsedSpaceWarningThresholdPercent())
				.toString();
		perifericoStatus[12] = storageManager.getSmartStatus() == null ? null
				: storageManager.getSmartStatus().toString();
		perifericoStatus[13] = storageManager.getRaidStatus() == null ? null
				: storageManager.getRaidStatus().toString();

		perifericoStatus[14] = (status.getFailedBoardBindingsNumber() == null ? null
				: status.getFailedBoardBindingsNumber().toString());

		perifericoStatus[15] = (status.getEnabledDataPortAnalyzersNumber() == null ? null
				: status.getEnabledDataPortAnalyzersNumber().toString());
		perifericoStatus[16] = (status.getInitializedDataPortDriversNumber() == null ? null
				: status.getInitializedDataPortDriversNumber().toString());
		perifericoStatus[17] = (status.getFailedDataPortThreadsNumber() == null ? null
				: status.getFailedDataPortThreadsNumber().toString());
		perifericoStatus[18] = (status.isDataPortAnalyzersOK() == null ? null
				: status.isDataPortAnalyzersOK().toString());
		perifericoStatus[19] = (status.areDriverConfigsOK() == null ? null
				: status.areDriverConfigsOK().toString());
		perifericoStatus[20] = new Integer(status.getDataWriteErrorCount())
				.toString();
		perifericoStatus[21] = new Integer(status.getTotalThreadFailures())
				.toString();
		perifericoStatus[22] = new Integer(status.getCurrentThreadFailures())
				.toString();
		perifericoStatus[23] = status.getCommonConfigFromCopStatus() == null ? null
				: status.getCommonConfigFromCopStatus().toString();
		perifericoStatus[24] = Boolean.toString(status.isDataInTheFuture());

		GpsDatum gpsDatum = perifericoApp.getLastGpsDatum();
		if (gpsDatum != null) {
			perifericoStatus[25] = (gpsDatum.getAltitude() == null ? null
					: new Double(gpsDatum.getAltitude()).toString());
			Double latitude = gpsDatum.getLatitude();
			perifericoStatus[26] = (latitude == null ? null : new Double(
					latitude).toString());
			Double longitude = gpsDatum.getLongitude();
			perifericoStatus[27] = (longitude == null ? null : new Double(
					longitude).toString());
			perifericoStatus[28] = gpsDatum.getFix().name();
			// TODO capire come internazionalizzare la data
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			perifericoStatus[29] = sdf.format(gpsDatum.getTimestamp());
			String url = perifericoApp.getCommonCfg().getMapsSiteURLFormatter();
			if (latitude != null && longitude != null) {
				perifericoStatus[30] = String
						.format(Locale.US, url, latitude, longitude,
								(String) getSession().getAttribute("locale"));
			} else
				perifericoStatus[30] = "";
		}
		return perifericoStatus;
	}

	public String[][] getAnalyzersStatusFields() {
		String[][] analyzersStatusFields = null;
		Periferico perifericoApp = Periferico.getInstance();
		List<Analyzer> allAnalyzersList = perifericoApp.getStation()
				.getListAnalyzer();
		List<Analyzer> analyzersList = new ArrayList<Analyzer>();
		for (Analyzer an : allAnalyzersList)
			if (an.getStatus() != Analyzer.Status.DELETED)
				analyzersList.add(an);
		Collections.sort(analyzersList, new Comparator<Analyzer>() {
			@Override
			public int compare(Analyzer o1, Analyzer o2) {
				return o1.compareName(o2);
			}
		});
		analyzersStatusFields = new String[analyzersList.size()][11];
		boolean errorFound = false;
		for (int i = 0; i < analyzersList.size() && !errorFound; i++) {
			Analyzer analyzer = analyzersList.get(i);
			analyzersStatusFields[i][0] = analyzer.getIdAsString();
			analyzersStatusFields[i][1] = Boolean.toString(analyzer.isEnabled()
					&& analyzer.getType() == Analyzer.Type.DPA);
			analyzersStatusFields[i][2] = analyzer.getName();
			analyzersStatusFields[i][3] = analyzer.getBrand() + " - "
					+ analyzer.getModel();
			if (analyzer.getFaultStatus() != null) {
				BinaryStatus binaryStatus = analyzer.getFaultStatus();
				String returnString = null;
				if (binaryStatus instanceof IntegerStatus) {
					IntegerStatus is = (IntegerStatus) binaryStatus;
					// add the value to status
					if (is.getValue() != null)
						returnString = is.getStatus() + " - "
								+ String.format("0x%08x", is.getValue());
					else
						returnString = new Boolean(is.getStatus()).toString();
				} else
					returnString = new Boolean(binaryStatus.getStatus())
							.toString();
				analyzersStatusFields[i][4] = returnString;
			}
			if (analyzer.getDataValidStatus() != null)
				analyzersStatusFields[i][5] = new Boolean(analyzer
						.getDataValidStatus().getStatus()).toString();
			if (analyzer.getMaintenanceInProgress() != null)
				analyzersStatusFields[i][6] = new Boolean(analyzer
						.getMaintenanceInProgress().getStatus()).toString();
			if (analyzer.getManualCalibrationRunning() != null)
				analyzersStatusFields[i][7] = new Boolean(analyzer
						.getManualCalibrationRunning().getStatus()).toString();
			if (analyzer.getAutoCheckRunning() != null)
				analyzersStatusFields[i][8] = new Boolean(analyzer
						.getAutoCheckRunning().getStatus()).toString();
			if (analyzer.getAutoCheckFailed() != null)
				analyzersStatusFields[i][9] = new Boolean(analyzer
						.getAutoCheckFailed().getStatus()).toString();
			analyzersStatusFields[i][10] = analyzer.getUiProxyPath();
		}
		return analyzersStatusFields;
	} // end getAnalyzersStatusFields

	public String[][] getHistoryAnalyzersStatusFields(String analyzerIdStr,
			String startDateStr, String startHourStr, String endDateStr,
			String endHourStr, String evtType) {
		String[][] historyAnalyzerStatusFields = null;

		// create and verify startDate and endDate
		SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
		SimpleDateFormat sdfTimestamp = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss.SSS");
		Calendar startCal = new GregorianCalendar();
		Calendar endCal = new GregorianCalendar();
		Date startDate = null;
		try {
			startDate = sdfDay.parse(startDateStr);
		} catch (ParseException e) {
			historyAnalyzerStatusFields = new String[1][1];
			historyAnalyzerStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"start_date_error");
			return historyAnalyzerStatusFields;
		}
		startCal.setTime(startDate);
		Date startHour = null;
		try {
			startHour = sdfHour.parse(startHourStr);
		} catch (ParseException e) {
			historyAnalyzerStatusFields = new String[1][1];
			historyAnalyzerStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"start_hour_error");
			return historyAnalyzerStatusFields;
		}
		Calendar hourCal = new GregorianCalendar();
		hourCal.setTime(startHour);
		startCal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
		startCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
		Date endDate = null;
		try {
			endDate = sdfDay.parse(endDateStr);
		} catch (ParseException e) {
			historyAnalyzerStatusFields = new String[1][1];
			historyAnalyzerStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"end_date_error");
			return historyAnalyzerStatusFields;
		}
		endCal.setTime(endDate);
		Date endHour = null;
		try {
			endHour = sdfHour.parse(endHourStr);
		} catch (ParseException e) {
			historyAnalyzerStatusFields = new String[1][1];
			historyAnalyzerStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"end_hour_error");
			return historyAnalyzerStatusFields;
		}
		hourCal = new GregorianCalendar();
		hourCal.setTime(endHour);
		endCal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
		endCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);
		endCal.set(Calendar.SECOND, 0);
		endCal.set(Calendar.MILLISECOND, 0);
		if (!endCal.after(startCal)) {
			historyAnalyzerStatusFields = new String[1][1];
			historyAnalyzerStatusFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"incoherent_calendar");
			return historyAnalyzerStatusFields;
		}

		Periferico perifericoApp = Periferico.getInstance();
		StorageManager storageManager = perifericoApp.getStorageManager();
		List<List<String>> fieldsList = new ArrayList<List<String>>();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(
				analyzerIdStr);
		try {
			List<BinaryStatus> statusList = storageManager.readEventData(
					analyzer.getId(), evtType, startCal.getTime(), false,
					endCal.getTime(), true, null);
			for (int i = 0; i < statusList.size(); i++) {
				List<String> singleAnalyzerFieldsList = new ArrayList<String>();
				BinaryStatus status = statusList.get(i);
				singleAnalyzerFieldsList.add(sdfTimestamp.format(status
						.getTimestamp()));
				String returnString = null;
				if (status instanceof IntegerStatus) {
					// add the value to status
					if (((IntegerStatus) status).getValue() != null) {
						returnString = new Boolean(status.getStatus())
								.toString()
								+ " - "
								+ String.format("0x%08x",
										((IntegerStatus) status).getValue());
					} else
						returnString = new Boolean(status.getStatus())
								.toString();
				} else
					returnString = new Boolean(status.getStatus()).toString();
				singleAnalyzerFieldsList.add(returnString);
				fieldsList.add(singleAnalyzerFieldsList);
			}
		} catch (StorageException stEx) {
			historyAnalyzerStatusFields = new String[1][1];
			historyAnalyzerStatusFields[0][0] = stEx
					.getLocalizedMessage((String) getSession().getAttribute(
							"locale"));
		}

		historyAnalyzerStatusFields = new String[fieldsList.size()][2];
		for (int k = 0; k < fieldsList.size(); k++) {
			List<String> tmpStrList = fieldsList.get(k);
			historyAnalyzerStatusFields[k] = tmpStrList
					.toArray(historyAnalyzerStatusFields[k]);
		}

		return historyAnalyzerStatusFields;
	} // end getHistoryAnalyzersStatusFields

	public String[][] getRealTimeDataFields(Boolean onlyDeletedAnalyzers) {
		String[][] realTimeDataFields = null;
		List<List<String>> fieldsList = new ArrayList<List<String>>();
		Periferico perifericoApp = Periferico.getInstance();
		List<Analyzer> allAnalyzersList = perifericoApp.getStation()
				.getListAnalyzer();
		List<Analyzer> analyzersList = new ArrayList<Analyzer>();
		if (onlyDeletedAnalyzers != null && onlyDeletedAnalyzers) {
			for (Analyzer an : allAnalyzersList)
				if (an.getStatus() == Analyzer.Status.DELETED)
					analyzersList.add(an);
		} else {
			for (Analyzer an : allAnalyzersList)
				if (an.getStatus() != Analyzer.Status.DELETED)
					analyzersList.add(an);
		}
		Collections.sort(analyzersList, new Comparator<Analyzer>() {
			@Override
			public int compare(Analyzer o1, Analyzer o2) {
				return o1.compareName(o2);
			}
		});
		boolean errorFound = false;
		for (int i = 0; i < analyzersList.size() && !errorFound; i++) {
			Analyzer analyzer = analyzersList.get(i);
			Type type = analyzer.getType();
			switch (type) {
			case DPA:
				DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
				List<DataPortElement> dpaElementsList = dpaAnalyzer
						.getListElements();
				for (int j = 0; j < dpaElementsList.size(); j++) {
					GenericSampleElement sampleElement = dpaElementsList.get(j);
					List<String> fieldsValueList = new ArrayList<String>();
					fieldsValueList.add(dpaAnalyzer.getIdAsString());
					fillFieldsValuesForSampleElmn(analyzer.isEnabled(),
							sampleElement, fieldsValueList);
					fieldsValueList.add(type.toString().toUpperCase());
					fieldsList.add(fieldsValueList);
				}
				break;
			case AVG:
				AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
				List<AvgElement> avgElementsList = avgAnalyzer
						.getListElements();
				for (int j = 0; j < avgElementsList.size(); j++) {
					GenericSampleElement sampleElement = avgElementsList.get(j);
					List<String> fieldsValueList = new ArrayList<String>();
					fieldsValueList.add(avgAnalyzer.getIdAsString());
					fillFieldsValuesForSampleElmn(analyzer.isEnabled(),
							sampleElement, fieldsValueList);
					fieldsValueList.add(type.toString().toUpperCase());
					fieldsList.add(fieldsValueList);
				}
				break;
			case SAMPLE:
				SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
				List<SampleElement> sampleElementsList = sampleAnalyzer
						.getListElements();
				for (int j = 0; j < sampleElementsList.size(); j++) {
					GenericSampleElement sampleElement = sampleElementsList
							.get(j);
					List<String> fieldsValueList = new ArrayList<String>();
					fieldsValueList.add(sampleAnalyzer.getIdAsString());
					fillFieldsValuesForSampleElmn(analyzer.isEnabled(),
							sampleElement, fieldsValueList);
					fieldsValueList.add(type.toString().toUpperCase());
					fieldsList.add(fieldsValueList);

				}
				break;
			case RAIN:
				RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
				CounterElement rainElement = rainAnalyzer.getRainElement();
				List<String> fieldsValueList = new ArrayList<String>();
				fieldsValueList.add(rainAnalyzer.getIdAsString());
				fieldsValueList.add(rainElement.getParameterId());
				fieldsValueList.add(rainElement.getBindLabel());
				Sample sample = rainElement.getLastSample();
				if (sample != null) {
					NumberFormat nf = NumberFormat.getInstance();
					nf.setRoundingMode(Periferico.ROUNDING_MODE_FOR_SAMPLE_DATA);
					nf.setMinimumFractionDigits(rainElement.getNumDec());
					nf.setMaximumFractionDigits(rainElement.getNumDec());
					nf.setGroupingUsed(false);
					fieldsValueList.add(PerifericoUtil.formatDouble(
							sample.getValue(), nf));
					fieldsValueList.add(rainElement.getMeasureUnitName());
					SimpleDateFormat sdf = new SimpleDateFormat(
							"dd/MM/yyyy HH:mm:ss");
					fieldsValueList.add(sdf.format(sample.getTimestamp()));
					fieldsValueList.add(new Boolean(sample.isNotvalid())
							.toString());
					fieldsValueList.add(String.format("0x%08x",
							(sample.getFlags())));
					// rainElement has not acquistion period
					fieldsValueList.add("");
					fieldsValueList
							.add(getMultipleFlagsTitle(sample.getFlags()));
				} else {
					for (int index = 0; index < 7; index++)
						fieldsValueList.add("");
				}
				fieldsValueList.add(type.toString().toUpperCase());
				fieldsList.add(fieldsValueList);

				break;

			case WIND:
				WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
				SampleElement speed = windAnalyzer.getWind().getSpeed();
				fieldsValueList = new ArrayList<String>();
				fieldsValueList.add(analyzer.getIdAsString());
				fillFieldsValuesForSampleElmn(analyzer.isEnabled(), speed,
						fieldsValueList);
				fieldsValueList.add(type.toString().toUpperCase());
				fieldsList.add(fieldsValueList);
				SampleElement dir = windAnalyzer.getWind().getDirection();
				fieldsValueList = new ArrayList<String>();
				fieldsValueList.add(analyzer.getIdAsString());
				fillFieldsValuesForSampleElmn(analyzer.isEnabled(), dir,
						fieldsValueList);
				fieldsValueList.add(type.toString().toUpperCase());
				fieldsList.add(fieldsValueList);
				break;
			}
		}
		realTimeDataFields = new String[fieldsList.size()][11];
		for (int k = 0; k < fieldsList.size(); k++) {
			List<String> tmpStrList = fieldsList.get(k);
			realTimeDataFields[k] = tmpStrList.toArray(realTimeDataFields[k]);
		}
		return realTimeDataFields;
	} // end getRealTimeDataFields

	private void fillFieldsValuesForSampleElmn(boolean analyzerEnabled,
			GenericSampleElement sampleElement, List<String> fieldsValueList) {
		fieldsValueList.add(sampleElement.getParameterId());
		fieldsValueList.add(sampleElement.getBindLabel());
		Sample sample = sampleElement.getLastSample();
		if (sample != null) {
			NumberFormat nf = NumberFormat.getInstance();
			nf.setRoundingMode(Periferico.ROUNDING_MODE_FOR_SAMPLE_DATA);
			nf.setMinimumFractionDigits(sampleElement.getNumDec());
			nf.setMaximumFractionDigits(sampleElement.getNumDec());
			nf.setGroupingUsed(false);
			fieldsValueList.add(PerifericoUtil.formatDouble(sample.getValue(),
					nf));
			fieldsValueList.add(sampleElement.getMeasureUnitName());
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			fieldsValueList.add(sdf.format(sample.getTimestamp()));
			fieldsValueList.add(new Boolean(sample.isNotvalid()).toString());
			fieldsValueList.add(String.format("0x%08x", sample.getFlags()));
			fieldsValueList.add(new Integer(sampleElement.getAcqPeriod())
					.toString());
			fieldsValueList.add(getMultipleFlagsTitle(sample.getFlags()));
		} else {
			for (int index = 0; index < 7; index++)
				fieldsValueList.add("");
			if (analyzerEnabled && sampleElement.isEnabled())
				fieldsValueList.set(fieldsValueList.size() - 2,
						Integer.toString(sampleElement.getAcqPeriod()));
		}
	}

	public String getMultipleFlagsTitle(int flag) {
		if (0 == flag)
			return propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"), "flag_ok");
		else {
			String lineSeparator = " - ";
			StringBuffer tooltip = new StringBuffer();
			if ((ValidationFlag.ACQ_ERROR & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "acq_error")
						+ lineSeparator);
			if ((ValidationFlag.ACQ_OUT_OF_SCALE & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "acq_out_of_scale")
						+ lineSeparator);
			if ((ValidationFlag.ANALYZER_AUTO_CALIB & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "analyzer_auto_calib")
						+ lineSeparator);
			if ((ValidationFlag.ANALYZER_FAULT & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "analyzer_fault")
						+ lineSeparator);
			if ((ValidationFlag.ANALYZER_DATA_NOT_VALID & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "analyzer_data_not_valid")
						+ lineSeparator);
			if ((ValidationFlag.ANALYZER_MAINTENANCE & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "analyzer_maintenance")
						+ lineSeparator);
			if ((ValidationFlag.ANALYZER_MANUAL_CALIB & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "analyzer_manual_calib")
						+ lineSeparator);
			if ((ValidationFlag.CHANNEL_UNTRIMMED & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "channel_untrimmed")
						+ lineSeparator);
			if ((ValidationFlag.ENVIRONMENT_NOT_OK & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "environment_not_ok")
						+ lineSeparator);
			if ((ValidationFlag.MISSING_DATA & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "missing_data")
						+ lineSeparator);
			if ((ValidationFlag.NOT_CONSTANT_DATA & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "not_constant_data")
						+ lineSeparator);
			if ((ValidationFlag.VALUE_OUT_OF_RANGE & flag) != 0)
				tooltip.append(propertyUtil.getProperty((String) getSession()
						.getAttribute("locale"), "value_out_of_range")
						+ lineSeparator);
			String returnString = tooltip.toString();
			return returnString.substring(0, returnString.length() - 3);
		}
	}

	public String[] getAvgPeriods(String analyzerIdStr, String elementIdStr) {
		String[] resAvgPeriods = null;
		Periferico perifericoApp = Periferico.getInstance();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(
				analyzerIdStr);
		Type type = analyzer.getType();
		switch (type) {
		case DPA:
			DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
			GenericSampleElement sampleElement = dpaAnalyzer
					.getElement(elementIdStr);
			List<Integer> avgPeriodsList = sampleElement.getAvgPeriods();
			resAvgPeriods = new String[avgPeriodsList.size()];
			for (int i = 0; i < avgPeriodsList.size(); i++) {
				Integer avgPeriod = avgPeriodsList.get(i);
				resAvgPeriods[i] = avgPeriod.toString();
			}
			break;
		case AVG:
			AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
			sampleElement = avgAnalyzer.getElement(elementIdStr);
			avgPeriodsList = sampleElement.getAvgPeriods();
			resAvgPeriods = new String[avgPeriodsList.size()];
			for (int i = 0; i < avgPeriodsList.size(); i++) {
				Integer avgPeriod = avgPeriodsList.get(i);
				resAvgPeriods[i] = avgPeriod.toString();
			}
			break;
		case RAIN:
			RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
			avgPeriodsList = rainAnalyzer.getRainElement().getAvgPeriods();
			resAvgPeriods = new String[avgPeriodsList.size()];
			for (int i = 0; i < avgPeriodsList.size(); i++) {
				Integer avgPeriod = avgPeriodsList.get(i);
				resAvgPeriods[i] = avgPeriod.toString();
			}
			break;
		case WIND:
			WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
			avgPeriodsList = windAnalyzer.getWind().getAvgPeriods();
			resAvgPeriods = new String[avgPeriodsList.size()];
			for (int i = 0; i < avgPeriodsList.size(); i++) {
				Integer avgPeriod = avgPeriodsList.get(i);
				resAvgPeriods[i] = avgPeriod.toString();
			}
			break;
		case SAMPLE:
			SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
			sampleElement = sampleAnalyzer.getElement(elementIdStr);
			avgPeriodsList = sampleElement.getAvgPeriods();
			resAvgPeriods = new String[avgPeriodsList.size()];
			for (int i = 0; i < avgPeriodsList.size(); i++) {
				Integer avgPeriod = avgPeriodsList.get(i);
				resAvgPeriods[i] = avgPeriod.toString();
			}
			break;
		}

		return resAvgPeriods;
	} // end getAvgPeriods

	public String getAcqPeriod(String analyzerIdStr, String elementIdStr) {
		String resultString = "";
		Periferico perifericoApp = Periferico.getInstance();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(
				analyzerIdStr);
		Type type = analyzer.getType();
		switch (type) {
		case DPA:
			DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
			GenericSampleElement sampleElement = dpaAnalyzer
					.getElement(elementIdStr);
			int acqPeriod = sampleElement.getAcqPeriod();
			resultString = new Integer(acqPeriod).toString();
			break;
		case AVG:
			AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
			sampleElement = avgAnalyzer.getElement(elementIdStr);
			acqPeriod = sampleElement.getAcqPeriod();
			resultString = new Integer(acqPeriod).toString();
			break;
		case RAIN:
			// Rain is event driven and does not have acquisition period
			resultString = "";
			break;
		case WIND:
			WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
			acqPeriod = windAnalyzer.getWind().getAcqPeriod();
			resultString = Integer.toString(acqPeriod);
			break;
		case SAMPLE:
			SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
			sampleElement = sampleAnalyzer.getElement(elementIdStr);
			acqPeriod = sampleElement.getAcqPeriod();
			resultString = new Integer(acqPeriod).toString();
			break;
		}

		return resultString;
	} // end getAcqPeriod

	public String[][] getHistoryRealTimeDataFields(String analyzerIdStr,
			String elementIdStr, String requestedDateString, String hourStr,
			String minutesStr) {
		String[][] historyRealTimeDataFields = null;

		// create startDate and endDate
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfTimestamp = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss");
		Date startDate = new Date();
		try {
			startDate = sdf.parse(requestedDateString);
		} catch (ParseException pEx) {
			historyRealTimeDataFields = new String[1][1];
			historyRealTimeDataFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"start_date_error");
			return historyRealTimeDataFields;
		}
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(startDate);
		try {
			if (hourStr.startsWith("0"))
				hourStr = hourStr.substring(1);
			int reqHour = new Integer(hourStr).intValue();
			startCal.set(Calendar.HOUR_OF_DAY, reqHour);
			if (minutesStr != null) {
				int reqMinutes = new Integer(minutesStr).intValue();
				startCal.set(Calendar.MINUTE, reqMinutes);
			} else
				startCal.set(Calendar.MINUTE, 0);
		} catch (NumberFormatException nfEx) {
			historyRealTimeDataFields = new String[1][1];
			historyRealTimeDataFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"start_hour_error");
			return historyRealTimeDataFields;
		}
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);

		Calendar endCal = new GregorianCalendar();
		endCal.setTime(startCal.getTime());
		if (minutesStr == null)
			endCal.add(Calendar.HOUR_OF_DAY, 1);
		else
			endCal.add(Calendar.MINUTE, 10);

		Periferico perifericoApp = Periferico.getInstance();
		StorageManager storageManager = perifericoApp.getStorageManager();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(
				analyzerIdStr);
		Type type = analyzer.getType();
		switch (type) {
		case DPA:
			try {
				DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
				GenericSampleElement sampleElement = dpaAnalyzer
						.getElement(elementIdStr);
				historyRealTimeDataFields = fillScalarRealTimeDataFields(
						analyzer, sampleElement, elementIdStr, storageManager,
						startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyRealTimeDataFields = new String[1][1];
				historyRealTimeDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		case AVG:
			try {
				AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
				GenericSampleElement sampleElement = avgAnalyzer
						.getElement(elementIdStr);
				historyRealTimeDataFields = fillScalarRealTimeDataFields(
						analyzer, sampleElement, elementIdStr, storageManager,
						startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyRealTimeDataFields = new String[1][1];
				historyRealTimeDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		case RAIN:
			try {
				RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
				ScalarElement scalarElement = rainAnalyzer
						.getElement(elementIdStr);
				historyRealTimeDataFields = fillScalarRealTimeDataFields(
						analyzer, scalarElement, elementIdStr, storageManager,
						startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyRealTimeDataFields = new String[1][1];
				historyRealTimeDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		case WIND:
			try {
				CommonCfg cc = Periferico.getInstance().getCommonCfg();
				WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
				GenericSampleElement sampleElement = null;
				Parameter param = cc.getParameter(elementIdStr);
				if (param == null)
					sampleElement = null;
				else if (param.getType() == Parameter.ParamType.WIND_VEL)
					sampleElement = windAnalyzer.getWind().getSpeed();
				else if (param.getType() == Parameter.ParamType.WIND_DIR)
					sampleElement = windAnalyzer.getWind().getDirection();
				else
					sampleElement = null;
				historyRealTimeDataFields = fillScalarRealTimeDataFields(
						analyzer, sampleElement, elementIdStr, storageManager,
						startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyRealTimeDataFields = new String[1][1];
				historyRealTimeDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		case SAMPLE:
			try {
				SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
				GenericSampleElement sampleElement = sampleAnalyzer
						.getElement(elementIdStr);
				historyRealTimeDataFields = fillScalarRealTimeDataFields(
						analyzer, sampleElement, elementIdStr, storageManager,
						startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyRealTimeDataFields = new String[1][1];
				historyRealTimeDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		}

		return historyRealTimeDataFields;
	} // end getHistoryRealTimeDataFields

	private String[][] fillScalarRealTimeDataFields(Analyzer analyzer,
			ScalarElement scalarElement, String elementIdStr,
			StorageManager storageManager, Calendar startCal, Calendar endCal,
			SimpleDateFormat sdfTimestamp) throws StorageException {
		String[][] fields = null;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(DECIMAL_SEPARATOR.charAt(0));
		DecimalFormat df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(Periferico.ROUNDING_MODE_FOR_SAMPLE_DATA);
		df.setMinimumFractionDigits(scalarElement.getNumDec());
		df.setMaximumFractionDigits(scalarElement.getNumDec());
		df.setGroupingUsed(false);
		List<Sample> sampleList = storageManager.readSampleData(
				analyzer.getId(), elementIdStr, startCal.getTime(), false,
				endCal.getTime(), true, null);
		fields = new String[sampleList.size() + 1][5];
		fields[0][0] = scalarElement.getMeasureUnitName();
		fields[0][1] = Boolean
				.toString(scalarElement instanceof GenericSampleElement);
		int i = 1;
		for (Sample sample : sampleList) {
			fields[i][0] = sdfTimestamp.format(sample.getTimestamp());
			fields[i][1] = PerifericoUtil.formatDouble(sample.getValue(), df);
			fields[i][2] = new Boolean(sample.isNotvalid()).toString();
			fields[i][3] = String.format("0x%08x", sample.getFlags());
			fields[i][4] = getMultipleFlagsTitle(sample.getFlags());
			i++;
		}
		return fields;
	}

	public String[][] getHistoryMeansDataFields(String analyzerIdStr,
			String elementIdStr, String periodStr, String chooseType,
			String halfDayStr, String startDateStr, String startHourStr,
			String endDateStr, String endHourStr, String maxDays) {
		String[][] historyMeansDataFields = null;

		// create startDate and endDate
		SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
		SimpleDateFormat sdfTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Calendar startCal = new GregorianCalendar();
		Calendar endCal = new GregorianCalendar();
		if (ONE_DAY.equals(chooseType)) {
			Date startDate = null;
			try {
				startDate = sdfDay.parse(startDateStr);
			} catch (ParseException e) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"start_date_error");
				return historyMeansDataFields;
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
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"start_date_error");
				return historyMeansDataFields;
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
		} else if (ONE_MONTH.equals(chooseType) || ONE_YEAR.equals(chooseType)) {
			Date startDate = null;
			try {
				startDate = sdfDay.parse(startDateStr);
			} catch (ParseException e) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"start_date_error");
				return historyMeansDataFields;
			}
			startCal.setTime(startDate);
			Date startHour = null;
			try {
				startHour = sdfHour.parse(startHourStr);
			} catch (ParseException e) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"start_hour_error");
				return historyMeansDataFields;
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
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"end_date_error");
				return historyMeansDataFields;
			}
			endCal.setTime(endDate);
			Date endHour = null;
			try {
				endHour = sdfHour.parse(endHourStr);
			} catch (ParseException e) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = propertyUtil.getProperty(
						(String) getSession().getAttribute("locale"),
						"end_hour_error");
				return historyMeansDataFields;
			}
			hourCal = new GregorianCalendar();
			hourCal.setTime(endHour);
			endCal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
			endCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
		}
		endCal.set(Calendar.SECOND, 0);
		endCal.set(Calendar.MILLISECOND, 0);
		if (!endCal.after(startCal)) {
			historyMeansDataFields = new String[1][1];
			historyMeansDataFields[0][0] = propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"incoherent_calendar");
			return historyMeansDataFields;
		}

		// verify if chosen period is over maxDays
		if (maxDays.equals("") || maxDays.equals("null"))
			maxDays = null;
		if (maxDays != null && !maxDays.isEmpty()) {
			int maxDaysValue = new Integer(maxDays).intValue();
			Calendar cmpCal = new GregorianCalendar();
			cmpCal.setTime(startCal.getTime());
			cmpCal.add(Calendar.DAY_OF_MONTH, maxDaysValue);
			if (cmpCal.before(endCal)) {
				historyMeansDataFields = new String[1][1];
				String[] args = new String[1];
				args[0] = maxDays;
				historyMeansDataFields[0][0] = propertyUtil
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"), "max_data_requested",
								args);
				return historyMeansDataFields;
			}
		}

		Periferico perifericoApp = Periferico.getInstance();
		StorageManager storageManager = perifericoApp.getStorageManager();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(
				analyzerIdStr);
		Type type = analyzer.getType();
		switch (type) {
		case DPA:
			try {
				DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
				GenericSampleElement sampleElement = dpaAnalyzer
						.getElement(elementIdStr);
				historyMeansDataFields = fillSampleMeansDataFields(
						sampleElement, storageManager, analyzer, elementIdStr,
						periodStr, startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		case AVG:
			try {
				AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
				SampleElement sampleElement = avgAnalyzer
						.getElement(elementIdStr);
				historyMeansDataFields = fillSampleMeansDataFields(
						sampleElement, storageManager, analyzer, elementIdStr,
						periodStr, startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		case RAIN:
			try {
				RainAnalyzer rainAnayzer = (RainAnalyzer) analyzer;
				CounterElement rainElement = rainAnayzer
						.getElement(elementIdStr);
				historyMeansDataFields = fillTotalDataFields(rainElement,
						storageManager, analyzer, elementIdStr, periodStr,
						startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		case WIND:
			// TODO implementare
			try {
				WindAnalyzer windAnayzer = (WindAnalyzer) analyzer;
				WindElement windElement = windAnayzer.getWind();
				historyMeansDataFields = fillWindMeansDataFields(windElement,
						storageManager, analyzer, windElement.getParameterId(),
						periodStr, startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		case SAMPLE:
			try {
				SampleAnalyzer sampleAnayzer = (SampleAnalyzer) analyzer;
				SampleElement sampleElement = sampleAnayzer
						.getElement(elementIdStr);
				historyMeansDataFields = fillSampleMeansDataFields(
						sampleElement, storageManager, analyzer, elementIdStr,
						periodStr, startCal, endCal, sdfTimestamp);
			} catch (StorageException stEx) {
				historyMeansDataFields = new String[1][1];
				historyMeansDataFields[0][0] = stEx
						.getLocalizedMessage((String) getSession()
								.getAttribute("locale"));
			}
			break;
		}
		return historyMeansDataFields;
	} // end getHistoryMeansDataFields

	private String[][] fillSampleMeansDataFields(
			GenericSampleElement sampleElement, StorageManager storageManager,
			Analyzer analyzer, String elementIdStr, String periodStr,
			Calendar startCal, Calendar endCal, SimpleDateFormat sdfTimestamp)
			throws StorageException {
		String[][] fields = null;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(DECIMAL_SEPARATOR.charAt(0));
		DecimalFormat df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		df.setMinimumFractionDigits(sampleElement.getNumDec());
		df.setMaximumFractionDigits(sampleElement.getNumDec());
		df.setGroupingUsed(false);
		List<MeanValue> meanValueList = storageManager.readMeanData(
				analyzer.getId(), elementIdStr,
				new Integer(periodStr).intValue(), startCal.getTime(), false,
				endCal.getTime(), true, null);
		fields = new String[meanValueList.size() + 1][5];
		fields[0][0] = sampleElement.getMeasureUnitName();
		fields[0][1] = periodStr;
		fields[0][2] = "mean";
		int i = 1;
		for (MeanValue meanValue : meanValueList) {
			fields[i][0] = sdfTimestamp.format(meanValue.getTimestamp());
			fields[i][1] = PerifericoUtil
					.formatDouble(meanValue.getValue(), df);
			fields[i][2] = new Boolean(meanValue.isNotvalid()).toString();
			fields[i][3] = String.format("0x%08x", meanValue.getFlags());
			fields[i][4] = getMultipleFlagsTitle(meanValue.getFlags());
			i++;
		}
		return fields;
	}

	private String[][] fillTotalDataFields(CounterElement counterElement,
			StorageManager storageManager, Analyzer analyzer,
			String elementIdStr, String periodStr, Calendar startCal,
			Calendar endCal, SimpleDateFormat sdfTimestamp)
			throws StorageException {
		String[][] fields = null;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(DECIMAL_SEPARATOR.charAt(0));
		DecimalFormat df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		df.setMinimumFractionDigits(counterElement.getNumDec());
		df.setMaximumFractionDigits(counterElement.getNumDec());
		df.setGroupingUsed(false);
		List<TotalValue> totalValueList = storageManager.readTotalData(
				analyzer.getId(), elementIdStr,
				new Integer(periodStr).intValue(), startCal.getTime(), false,
				endCal.getTime(), true, null);
		fields = new String[totalValueList.size() + 1][5];
		fields[0][0] = counterElement.getMeasureUnitName();
		fields[0][1] = periodStr;
		fields[0][2] = "total";
		int i = 1;
		for (TotalValue totalValue : totalValueList) {
			fields[i][0] = sdfTimestamp.format(totalValue.getTimestamp());
			fields[i][1] = PerifericoUtil.formatDouble(totalValue.getValue(),
					df);
			fields[i][2] = new Boolean(totalValue.isNotvalid()).toString();
			fields[i][3] = String.format("0x%08x", totalValue.getFlags());
			fields[i][4] = getMultipleFlagsTitle(totalValue.getFlags());
			i++;
		}
		return fields;
	}

	private String[][] fillWindMeansDataFields(WindElement windElement,
			StorageManager storageManager, Analyzer analyzer,
			String elementIdStr, String periodStr, Calendar startCal,
			Calendar endCal, SimpleDateFormat sdfTimestamp)
			throws StorageException {
		String[][] fields = null;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(DECIMAL_SEPARATOR.charAt(0));
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
		List<WindValue> windValueList = storageManager.readWindAggregateData(
				analyzer.getId(), elementIdStr,
				new Integer(periodStr).intValue(), startCal.getTime(), false,
				endCal.getTime(), true, null);
		fields = new String[windValueList.size() + 1][12];
		fields[0][0] = windElement.getSpeedMeasureUnitName();
		fields[0][1] = windElement.getDirectionMeasureUnitName();
		fields[0][2] = periodStr;
		int i = 1;
		for (WindValue windValue : windValueList) {
			fields[i][0] = sdfTimestamp.format(windValue.getTimestamp());
			fields[i][1] = PerifericoUtil.formatDouble(
					windValue.getVectorialSpeed(), dfSpeed);
			fields[i][2] = PerifericoUtil.formatDouble(
					windValue.getVectorialDirection(), dfDir);
			fields[i][3] = PerifericoUtil.formatDouble(
					windValue.getStandardDeviation(), dfSpeed);
			fields[i][4] = PerifericoUtil.formatDouble(
					windValue.getScalarSpeed(), dfSpeed);
			fields[i][5] = PerifericoUtil.formatDouble(
					windValue.getGustSpeed(), dfSpeed);
			fields[i][6] = PerifericoUtil.formatDouble(
					windValue.getGustDirection(), dfDir);
			fields[i][7] = PerifericoUtil.formatDouble(
					windValue.getCalmsNumberPercent(), df1);
			fields[i][8] = formatObject(windValue.getCalm());
			fields[i][9] = formatObject(windValue.isNotvalid());
			fields[i][10] = formatHex(windValue.getFlags());
			fields[i][11] = getMultipleFlagsTitle(windValue.getFlags());
			i++;
		}
		return fields;
	}

	private String formatObject(Object object) {
		if (object == null)
			return "";
		return object.toString();
	}

	private String formatHex(Integer value) {
		if (value == null)
			return "";
		return String.format("0x%08x", value);
	}

	/*
	 * Method for verify login
	 */

	public Boolean verifyLogin(String password) {
		// Load login configuration
		Periferico perifericoApp = Periferico.getInstance();
		LoginCfg loginCfg = perifericoApp.getLoginCfg();

		for (Password pwd : loginCfg.getPasswordList()) {
			if (pwd.getValue().equals(password)) {
				// set password session attribute
				getSession().setAttribute("password", pwd);
				return new Boolean(true);
			}
		}// end for
		return new Boolean(false);

	}

	/**
	 * Create linear chart
	 * 
	 * @param title
	 *            of chart
	 * @param legend
	 *            of line
	 * @param fieldsMatrix
	 *            fields (date, value)
	 * @return chartname
	 */
	// TODO: gestire eccezioni
	public String generateChart(String title, String legend,
			String measureUnit, String[][] fieldsMatrix, boolean withSeconds,
			boolean interpolated) {
		return generateChartImpl(title, legend, measureUnit, fieldsMatrix,
				withSeconds, interpolated, null, null, null, null);
	}

	private String generateChartImpl(String title, String legend,
			String measureUnit, String[][] fieldsMatrix, boolean withSeconds,
			boolean interpolated, Double yMin, Double yMax,
			Double yThresholdLow, Double yThresholdHigh) {
		/*
		 * Hold our stored chart name, it will be returned to the GWT caller.
		 */
		String chartName = legend;

		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series = null;
		series = new XYSeries(legend);

		for (int i = 0; i < fieldsMatrix.length; i++) {
			String value = fieldsMatrix[i][0];
			String dateStr = fieldsMatrix[i][1];
			// create data
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			if (!withSeconds)
				sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

			Date date = null;
			try {
				date = sdf.parse(dateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(date);

			Double dato = null;
			if (value != null && !value.equals("")) {
				dato = new Double(value);
			}

			// add point to series
			series.add(c.getTimeInMillis(), dato);
		}// end for

		dataset.addSeries(series);

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FMT);
		// Add prevalidation thresholds, if requested
		if (yThresholdHigh != null && fieldsMatrix.length > 0) {
			XYSeries thresholdHigh = new XYSeries(propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"threshold_max"));
			try {
				Date date = sdf.parse(fieldsMatrix[0][1]);
				thresholdHigh.add(date.getTime(), yThresholdHigh);
				date = sdf.parse(fieldsMatrix[fieldsMatrix.length - 1][1]);
				thresholdHigh.add(date.getTime(), yThresholdHigh);
				dataset.addSeries(thresholdHigh);
			} catch (ParseException e) {
				logger.error(e);
			}
		}
		if (yThresholdLow != null && fieldsMatrix.length > 0) {
			XYSeries thresholdLow = new XYSeries(propertyUtil.getProperty(
					(String) getSession().getAttribute("locale"),
					"threshold_min"));
			try {
				Date date = sdf.parse(fieldsMatrix[0][1]);
				thresholdLow.add(date.getTime(), yThresholdLow);
				date = sdf.parse(fieldsMatrix[fieldsMatrix.length - 1][1]);
				thresholdLow.add(date.getTime(), yThresholdLow);
				dataset.addSeries(thresholdLow);
			} catch (ParseException e) {
				logger.error(e);
			}
		}

		// NOTE: the chart title is set to null because the title is alredy
		// shown outside the chart
		JFreeChart chart = ChartFactory.createXYLineChart(null, // chart title
				null, // x axis label
				null, // y axis label
				(XYDataset) dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
				);

		// set graphic option
		chart.setBackgroundPaint(Color.white);
		// set graphic border
		RectangleInsets rect = new RectangleInsets(15, 5, 15, 15);
		chart.setPadding(rect);
		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		// sfondo bianco
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setNoDataMessage(propertyUtil.getProperty((String) getSession()
				.getAttribute("locale"), "no_data"));

		// Asse X -> domain axis
		DateAxis dateAxis = new DateAxis();
		dateAxis.setDateFormatOverride(new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss"));
		dateAxis.setAutoRange(true);
		dateAxis.setLabel(propertyUtil.getProperty((String) getSession()
				.getAttribute("locale"), "date"));
		plot.setDomainAxis(0, dateAxis);
		if ((interpolated && fieldsMatrix.length <= 96) || !interpolated)
			plot.setRenderer(new XYLineAndShapeRenderer(interpolated, true));

		// Asse Y -> range axis
		NumberAxis numberAxis = new NumberAxis();
		numberAxis.setLabel(propertyUtil.getProperty((String) getSession()
				.getAttribute("locale"), "value")
				+ (measureUnit == null || measureUnit.equals("") ? "" : " ["
						+ measureUnit + "]"));

		// set range or use autorange
		if (yMin != null && yMax != null)
			numberAxis.setRange(yMin, yMax);
		else
			numberAxis.setAutoRange(true);

		numberAxis
				.setNumberFormatOverride(new DecimalFormat("###.###;-###.###"));
		plot.setRangeAxis(0, numberAxis);

		/*
		 * Save the chart as an '980px x 460px' jpeg image.
		 */
		try {
			HttpSession session = getThreadLocalRequest().getSession();
			chartName = ServletUtilities.saveChartAsJPEG(chart, 980, 460, null,
					session);
		} catch (Exception e) {
			// TODO
			// handle exception
		}

		/*
		 * Finally, return the chart name to the caller.
		 */
		return chartName;
	}// end generateChart

	public String[] createAndGetChartNameOfRealTimeData(String analyzerIdStr,
			String elementIdStr, String requestedDateString,
			String startHourStr, String endHourStr, String title,
			String legend, String measureUnit, boolean autoScale,
			boolean showMinMax) {
		String[] resultString = new String[2];
		boolean interpolated = true;
		// create startDate and endDate
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfTimestamp = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss");
		Date startDate = new Date();
		try {
			startDate = sdf.parse(requestedDateString);
		} catch (ParseException pEx) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "start_date_error");
			return resultString;
		}
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(startDate);
		try {
			startCal.set(Calendar.HOUR_OF_DAY,
					new Integer(startHourStr).intValue());
		} catch (NumberFormatException nfEx) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "start_hour_error");
			return resultString;
		}
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);

		Calendar endCal = new GregorianCalendar();
		endCal.setTime(startCal.getTime());
		try {
			endCal.set(Calendar.HOUR_OF_DAY, new Integer(endHourStr).intValue());
		} catch (NumberFormatException nfEx) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "end_hour_error");
			return resultString;
		}

		String[][] historyRealTimeDataFields = null;
		Periferico perifericoApp = Periferico.getInstance();
		StorageManager storageManager = perifericoApp.getStorageManager();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(
				analyzerIdStr);
		Type type = analyzer.getType();
		DecimalFormatSymbols dfs = null;
		DecimalFormat df = null;
		List<Sample> sampleList = null;
		ScalarElement scalarElement = null;
		switch (type) {
		case DPA:
			DataPortAnalyzer dpaAnalyzer = (DataPortAnalyzer) analyzer;
			scalarElement = dpaAnalyzer.getElement(elementIdStr);
			break;
		case AVG:
			AvgAnalyzer avgAnalyzer = (AvgAnalyzer) analyzer;
			scalarElement = avgAnalyzer.getElement(elementIdStr);
			break;
		case RAIN:
			interpolated = false;
			RainAnalyzer rainAnalyzer = (RainAnalyzer) analyzer;
			scalarElement = rainAnalyzer.getElement(elementIdStr);
			break;
		case SAMPLE:
			SampleAnalyzer sampleAnalyzer = (SampleAnalyzer) analyzer;
			scalarElement = sampleAnalyzer.getElement(elementIdStr);
			break;
		case WIND:
			CommonCfg cc = Periferico.getInstance().getCommonCfg();
			WindAnalyzer windAnalyzer = (WindAnalyzer) analyzer;
			Parameter param = cc.getParameter(elementIdStr);
			if (param != null) {
				if (param.getType() == Parameter.ParamType.WIND_VEL)
					scalarElement = windAnalyzer.getWind().getSpeed();
				else if (param.getType() == Parameter.ParamType.WIND_DIR)
					scalarElement = windAnalyzer.getWind().getDirection();
			}
			break;
		}
		sampleList = new ArrayList<Sample>();
		try {
			sampleList = storageManager.readSampleData(analyzer.getId(),
					elementIdStr, startCal.getTime(), false, endCal.getTime(),
					true, null);
		} catch (StorageException stEx) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = stEx.getLocalizedMessage((String) getSession()
					.getAttribute("locale"));
			return resultString;
		}
		dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(DECIMAL_SEPARATOR.charAt(0));
		df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(Periferico.ROUNDING_MODE_FOR_SAMPLE_DATA);
		df.setMinimumFractionDigits(scalarElement.getNumDec());
		df.setMaximumFractionDigits(scalarElement.getNumDec());
		df.setGroupingUsed(false);
		historyRealTimeDataFields = new String[sampleList.size()][2];
		for (int i = 0; i < sampleList.size(); i++) {
			Sample sample = sampleList.get(i);
			historyRealTimeDataFields[i][0] = PerifericoUtil.formatDouble(
					sample.getValue(), df);
			historyRealTimeDataFields[i][1] = sdfTimestamp.format(sample
					.getTimestamp());
		}
		Double yMin = null;
		Double yMax = null;
		if (!autoScale && scalarElement instanceof SampleElement) {
			yMin = ((SampleElement) scalarElement).getRangeLow();
			yMax = ((SampleElement) scalarElement).getRangeHigh();
		}
		Double yThresholdLow = showMinMax ? scalarElement.getMinValue() : null;
		Double yThresholdHigh = showMinMax ? scalarElement.getMaxValue() : null;
		// N.B.: boolean value is used to indicate if there is an error or not
		resultString[0] = new Boolean(false).toString();
		resultString[1] = generateChartImpl(title, legend, measureUnit,
				historyRealTimeDataFields, true, interpolated, yMin, yMax,
				yThresholdLow, yThresholdHigh);

		return resultString;
	} // end createAndGetChartNameOfRealTimeData

	public String[] createAndGetChartNameOfMeansData(String analyzerIdStr,
			String elementIdStr, String startDateStr, String startHourStr,
			String endDateStr, String endHourStr, String maxDays,
			String periodStr, String title, String legend, String measureUnit,
			boolean autoScale, boolean showMinMax) {
		String[] resultString = new String[2];
		boolean interpolated = true;
		// create startDate and endDate
		SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
		SimpleDateFormat sdfTimestamp = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss");
		Calendar startCal = new GregorianCalendar();
		Calendar endCal = new GregorianCalendar();

		Date startDate = null;
		try {
			startDate = sdfDay.parse(startDateStr);
		} catch (ParseException e) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "start_date_error");
			return resultString;
		}
		startCal.setTime(startDate);
		Date startHour = null;
		try {
			startHour = sdfHour.parse(startHourStr);
		} catch (ParseException e) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "start_hour_error");
			return resultString;
		}
		Calendar hourCal = new GregorianCalendar();
		hourCal.setTime(startHour);
		startCal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
		startCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));
		Date endDate = null;
		try {
			endDate = sdfDay.parse(endDateStr);
		} catch (ParseException e) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "end_date_error");
			return resultString;
		}
		endCal.setTime(endDate);
		Date endHour = null;
		try {
			endHour = sdfHour.parse(endHourStr);
		} catch (ParseException e) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "end_hour_error");
			return resultString;
		}
		hourCal = new GregorianCalendar();
		hourCal.setTime(endHour);
		endCal.set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY));
		endCal.set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE));

		endCal.set(Calendar.SECOND, 0);
		endCal.set(Calendar.MILLISECOND, 0);
		if (!endCal.after(startCal)) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getProperty((String) getSession()
					.getAttribute("locale"), "incoherent_calendar");
			return resultString;
		}

		// verify if chosen period is over maxDays
		int maxDaysValue = new Integer(maxDays).intValue();
		Calendar cmpCal = new GregorianCalendar();
		cmpCal.setTime(startCal.getTime());
		cmpCal.add(Calendar.DAY_OF_MONTH, maxDaysValue);
		if (cmpCal.before(endCal)) {
			String[] args = new String[1];
			args[0] = maxDays;
			resultString[0] = new Boolean(true).toString();
			resultString[1] = propertyUtil.getLocalizedMessage(
					(String) getSession().getAttribute("locale"),
					"max_data_requested", args);
			return resultString;
		}

		String[][] historyMeanDataFields = null;
		Periferico perifericoApp = Periferico.getInstance();
		StorageManager storageManager = perifericoApp.getStorageManager();
		Analyzer analyzer = perifericoApp.getStation().getAnalyzer(
				analyzerIdStr);
		Type type = analyzer.getType();
		List<? extends ScalarAggregateValue> valuesList = null;
		DecimalFormatSymbols dfs = null;
		DecimalFormat df = null;
		ScalarElement scalarElement = null;
		switch (type) {
		case DPA:
			DataPortAnalyzer dpaAnayzer = (DataPortAnalyzer) analyzer;
			scalarElement = dpaAnayzer.getElement(elementIdStr);
			break;
		case AVG:
			AvgAnalyzer avgAnayzer = (AvgAnalyzer) analyzer;
			scalarElement = avgAnayzer.getElement(elementIdStr);
			break;
		case RAIN:
			interpolated = false;
			RainAnalyzer rainAnayzer = (RainAnalyzer) analyzer;
			scalarElement = rainAnayzer.getElement(elementIdStr);
			break;
		case SAMPLE:
			SampleAnalyzer sampleAnayzer = (SampleAnalyzer) analyzer;
			scalarElement = sampleAnayzer.getElement(elementIdStr);
			break;
		case WIND:
			throw new UnsupportedOperationException("Chart of means data not"
					+ " supported for wind analyzer");
		}
		try {
			if (scalarElement instanceof GenericSampleElement)
				valuesList = storageManager
						.readMeanData(analyzer.getId(), elementIdStr,
								Integer.parseInt(periodStr),
								startCal.getTime(), false, endCal.getTime(),
								true, null);
			else if (scalarElement instanceof CounterElement)
				valuesList = storageManager
						.readTotalData(analyzer.getId(), elementIdStr,
								Integer.parseInt(periodStr),
								startCal.getTime(), false, endCal.getTime(),
								true, null);
		} catch (StorageException stEx) {
			resultString[0] = new Boolean(true).toString();
			resultString[1] = stEx.getLocalizedMessage((String) getSession()
					.getAttribute("locale"));
			return resultString;
		}
		dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(DECIMAL_SEPARATOR.charAt(0));
		df = new DecimalFormat();
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(Periferico.ROUNDING_MODE_FOR_AGGREGATE_DATA);
		df.setMinimumFractionDigits(scalarElement.getNumDec());
		df.setMaximumFractionDigits(scalarElement.getNumDec());
		df.setGroupingUsed(false);
		historyMeanDataFields = new String[valuesList.size()][2];
		for (int i = 0; i < valuesList.size(); i++) {
			ScalarAggregateValue meanValue = valuesList.get(i);
			historyMeanDataFields[i][0] = PerifericoUtil.formatDouble(
					meanValue.getValue(), df);
			historyMeanDataFields[i][1] = sdfTimestamp.format(meanValue
					.getTimestamp());
		}
		Double yMin = null;
		Double yMax = null;
		if (!autoScale && scalarElement instanceof SampleElement) {
			SampleElement se = (SampleElement) scalarElement;
			yMin = se.getRangeLow();
			yMax = se.getRangeHigh();
			Converter conv = se.getConverter();
			if (conv != null) {
				yMin = conv.convert(yMin);
				yMax = conv.convert(yMax);
			}
		}
		Double yThresholdLow = showMinMax ? scalarElement.getMinValue() : null;
		Double yThresholdHigh = showMinMax ? scalarElement.getMaxValue() : null;
		resultString[0] = new Boolean(false).toString();
		resultString[1] = generateChartImpl(title, legend, measureUnit,
				historyMeanDataFields, false, interpolated, yMin, yMax,
				yThresholdLow, yThresholdHigh);

		return resultString;
	} // end createAndGetChartNameOfMeansData

	/**
	 * Get current date
	 * 
	 * @return string that rapresents date
	 */
	public String[] getCurrentDate() {
		String[] resStrings = new String[2];
		resStrings[0] = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
		resStrings[1] = new SimpleDateFormat("HH:mm").format(new Date());
		return resStrings;
	}

	/**
	 * Convert string to double
	 * 
	 * @param string
	 *            to convert
	 * @return a double value of string
	 */
	private Double string2Double(String str) {
		Double doubleValue = null;
		if (str != null && !str.trim().isEmpty())
			doubleValue = new Double(str.trim());
		return doubleValue;

	}

	/**
	 * Validate IP address
	 * 
	 * @param iPaddress
	 * @return true if it's a valid ipAddress, false otherwise
	 */
	// private boolean validateAnIpAddressWithRegularExpression(String
	// iPaddress) {
	//
	// String re = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b";
	// Pattern pattern = Pattern.compile(re);
	// Matcher m = pattern.matcher(iPaddress);
	// if (m.matches()) {
	// String[] parts = iPaddress.split("\\.");
	// if (new Integer(parts[0]).intValue() == 0) {
	// return false;
	// }
	// for (int i = 0; i < parts.length; i++) {
	// if (new Integer(parts[i]).intValue() > 255) {
	// return false;
	// }
	// }
	// return true;
	// } else {
	// return false;
	// }
	// }

	/**
	 * Get Integer from a Hex String
	 * 
	 * @param HexString
	 * @return Integer
	 * @throws PerifericoException
	 */
	// private Integer getIntegerFromHexString(String fieldName, String
	// hexString)
	// throws PerifericoException {
	// Integer resValue = null;
	// try {
	// resValue = Integer.parseInt(hexString, 16);
	// } catch (NumberFormatException nfEx) {
	// throw new PerifericoException("error_incorrect_hex_string",
	// fieldName);
	// }
	// return resValue;
	// }

	public String makeCsv() {
		String tmpFileName = "prova.csv";
		File csvFile = new File("/tmp/" + tmpFileName);
		FileWriter writer;
		try {
			writer = new FileWriter(csvFile);
			writer.append("pippo");
			writer.append("pluto");
			writer.append("paperino");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error writing csv file", e);
		}

		return tmpFileName;
	}

	public String getPerifericoVersion() {
		return Periferico.VERSION.toString();
	}

	@Override
	public String getStationName() {
		Config cfg = Periferico.getInstance().getConfig();
		if (cfg == null)
			return "";
		String stationName = cfg.getStation().getName();
		if (stationName == null || stationName.isEmpty())
			stationName = cfg.getStation().getShortName();
		if (stationName == null)
			return "";
		return stationName;
	}

	@Override
	public List<String[]> getCommandList(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		List<CustomCommand> listCommands;
		try {
			listCommands = dm.getCommandList(dpa);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		ArrayList<String[]> commandList = new ArrayList<String[]>();
		for (CustomCommand cc : listCommands)
			commandList.add(new String[] { cc.getId(), cc.getName(),
					cc.getArgsDescription() });
		return commandList;
	}

	private void checkDpaConnected(DriverManager dm, DataPortAnalyzer dpa)
			throws UnboundDriverException, DgtAnalyzerException {
		if (!dm.isConnectionOK(dpa))
			throw new DgtAnalyzerException(
					DgtAnalyzerException.Type.NOT_CONNECTED,
					"The connection with the analyzer is not working");
	}

	@Override
	public String sendCommand(String analyzerId, String command[])
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<List<String>> callback = new UIDriverCallback<List<String>>();
		try {
			checkDpaConnected(dm, dpa);
			dm.sendCommand(dpa, command, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		cacheCommandCallback(callback);
		return callback.getId().toString();
	}

	@Override
	public List<String> getCommandResult(String commandId)
			throws SessionExpiredException, DgtAnalyzerException {
		UIDriverCallback<List<String>> callback = removeCommandCallback(commandId);
		getConfig();
		if (callback == null)
			throw new DgtAnalyzerException("Command result expired");
		if (callback.waitForCompletion(10)) {
			List<String> result = callback.waitForResult();
			if (result == null)
				result = new ArrayList<String>();
			return result;
		} else {
			cacheCommandCallback(callback);
			return null;
		}
	}

	private void cacheCommandCallback(UIDriverCallback<List<String>> callback) {
		synchronized (mapCommandCallbacks) {
			Iterator<UIDriverCallback<List<String>>> it = mapCommandCallbacks
					.values().iterator();
			while (it.hasNext()) {
				UIDriverCallback<List<String>> cb = it.next();
				if (System.currentTimeMillis() - cb.getTimestamp() > 60 * 60 * 1000)
					it.remove();
			}
			mapCommandCallbacks.put(callback.getId().toString(), callback);
		}
	}

	private UIDriverCallback<List<String>> removeCommandCallback(
			String commandId) {
		synchronized (mapCommandCallbacks) {
			return mapCommandCallbacks.remove(commandId);
		}
	}

	@Override
	public List<String> getAlarmValues(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<List<String>> callback = new UIDriverCallback<List<String>>();
		try {
			checkDpaConnected(dm, dpa);
			dm.showFaults(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		return callback.waitForResult();
	}

	@Override
	public List<String> getProcessParameterValues(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<List<String>> callback = new UIDriverCallback<List<String>>();
		try {
			checkDpaConnected(dm, dpa);
			dm.showProcessParameters(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		return callback.waitForResult();
	}

	@Override
	public List<String> getMeasureValues(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<List<String>> callback = new UIDriverCallback<List<String>>();
		try {
			checkDpaConnected(dm, dpa);
			dm.showMeasuredParameters(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		return callback.waitForResult();
	}

	@Override
	public List<String> getAlarmList(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<List<AnalyzerFault>> callback = new UIDriverCallback<List<AnalyzerFault>>();
		try {
			checkDpaConnected(dm, dpa);
			dm.getFaults(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		List<AnalyzerFault> faults = callback.waitForResult();
		List<String> strFaults = new ArrayList<String>();
		for (AnalyzerFault fault : faults)
			strFaults.add(String.format("0x%08X", fault.getCode()) + ": "
					+ fault.getName());
		return strFaults;
	}

	@Override
	public Date getAnalyzerDate(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<Date> callback = new UIDriverCallback<Date>();
		try {
			checkDpaConnected(dm, dpa);
			dm.getDate(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		return callback.waitForResult();
	}

	@Override
	public Date setAnalyzerDate(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<Date> callback = new UIDriverCallback<Date>();
		try {
			checkDpaConnected(dm, dpa);
			dm.setDate(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		return callback.waitForResult();
	}

	@Override
	public String getSerialNumber(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<String> callback = new UIDriverCallback<String>();
		try {
			checkDpaConnected(dm, dpa);
			dm.getSerialNumber(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		return callback.waitForResult();
	}

	@Override
	public Integer resetAllFaults(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<FaultValue> callback = new UIDriverCallback<FaultValue>();
		try {
			checkDpaConnected(dm, dpa);
			dm.resetAllFaults(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		FaultValue value = callback.waitForResult();
		return value.getValue();
	}

	@Override
	public Boolean isRemoteUISupported(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		try {
			return dm.isRemoteUISupported(dpa);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
	}

	@Override
	public String isRemoteGUISupported(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		try {
			return dm.isRemoteGUISupported(dpa);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
	}

	@Override
	public List<String> getKeyList(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		try {
			return dm.getKeyList(dpa);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
	}

	@Override
	public List<String> readDisplay(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<List<String>> callback = new UIDriverCallback<List<String>>();
		try {
			checkDpaConnected(dm, dpa);
			dm.readDisplay(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		return callback.waitForResult();
	}

	@Override
	public String readDisplayImage(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<byte[]> callback = new UIDriverCallback<byte[]>();
		String imageFormat;
		try {
			checkDpaConnected(dm, dpa);
			imageFormat = dm.isRemoteGUISupported(dpa);
			if (imageFormat == null)
				throw new DgtAnalyzerException(
						"Remote GUI not supported for this analyzer");
			dm.readDisplayImage(dpa, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		byte[] displayImage = callback.waitForResult();
		if (displayImage == null)
			return null;
		return "data:image/" + imageFormat + ";base64,"
				+ Base64.encodeBase64String(displayImage);
	}

	@Override
	public List<String> sendKey(String analyzerId, String key)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		UIDriverCallback<List<String>> callback = new UIDriverCallback<List<String>>();
		try {
			checkDpaConnected(dm, dpa);
			dm.sendKey(dpa, key, callback);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
		return callback.waitForResult();
	}

	@Override
	public Boolean isCustomCommandSupported(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		DataPortAnalyzer dpa = getDpaAnalyzer(analyzerId);
		try {
			return dm.isCustomCommandSupported(dpa);
		} catch (UnboundDriverException e) {
			throw new DgtAnalyzerException(e);
		}
	}

	private DataPortAnalyzer getDpaAnalyzer(String id)
			throws SessionExpiredException, DgtAnalyzerException {
		Analyzer analyzer = getConfig().getStation().getAnalyzer(id);
		if (analyzer == null)
			throw new DgtAnalyzerException("Analyzer not found for id: " + id);
		if (analyzer.getType() != Type.DPA)
			throw new DgtAnalyzerException("Analyzer with id '" + id
					+ "' is not a data port analyzer");
		DataPortAnalyzer dpa = (DataPortAnalyzer) analyzer;
		return dpa;
	}

	@Override
	public List<String> getEquivalentBrands(String brand, String model)
			throws SessionExpiredException {
		getConfig();
		DriverManager dm = Periferico.getInstance().getDriverManager();
		return dm.getEquivalentBrands(brand, model);
	}

	@Override
	public List<String> getEquivalentModels(String newBrand, String brand,
			String model) throws SessionExpiredException {
		getConfig();
		DriverManager dm = Periferico.getInstance().getDriverManager();
		return dm.getEquivalentModels(newBrand, brand, model);
	}

	private class UIDriverCallback<T> implements DriverCallback<T> {

		private UUID id = UUID.randomUUID();
		private long timestamp = System.currentTimeMillis();
		private T result = null;
		private Throwable failure = null;
		private volatile boolean running = true;

		UUID getId() {
			return id;
		}

		long getTimestamp() {
			return timestamp;
		}

		@Override
		public void onSuccess(T okResult) {
			result = okResult;
			running = false;
		}

		@Override
		public void onFailure(Throwable caught) {
			failure = caught;
			running = false;
		}

		boolean waitForCompletion(int seconds) throws DgtAnalyzerException {
			int count = seconds * 10;
			while (running && count-- > 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new DgtAnalyzerException(e);
				}
			}
			return !running;
		}

		T waitForResult() throws DgtAnalyzerException {
			while (running) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new DgtAnalyzerException(e);
				}
			}
			if (failure == null) {
				return result;
			} else {
				if (failure instanceof UnsupportedOperationException)
					throw new DgtAnalyzerException(
							DgtAnalyzerException.Type.NOT_SUPPORTED, failure);
				else
					throw new DgtAnalyzerException(failure);
			}
		}
	}

}// end class
