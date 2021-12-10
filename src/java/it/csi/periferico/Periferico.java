/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: application's entry point
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: Periferico.java,v 1.103 2015/10/16 12:58:49 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.servlet.ProxyServlet.Transparent;

import it.csi.periferico.acqdrivers.DriverInfo;
import it.csi.periferico.acqdrivers.DriverManager;
import it.csi.periferico.boards.AcqManager;
import it.csi.periferico.boards.Board;
import it.csi.periferico.boards.BoardManagementForUI;
import it.csi.periferico.boards.BoardManager;
import it.csi.periferico.comm.ConnectionException;
import it.csi.periferico.comm.ConnectionManager;
import it.csi.periferico.comm.ConnectionParams;
import it.csi.periferico.config.BoardsCfg;
import it.csi.periferico.config.Config;
import it.csi.periferico.config.ConfigManager;
import it.csi.periferico.config.LoginCfg;
import it.csi.periferico.config.SetConfigResult;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.core.Analyzer;
import it.csi.periferico.core.DataManager;
import it.csi.periferico.core.DataPortAnalyzer;
import it.csi.periferico.core.Station;
import it.csi.periferico.gps.GpsDatum;
import it.csi.periferico.gps.GpsdClient;
import it.csi.periferico.storage.StorageManager;

/**
 * Starts Periferico application.
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Periferico extends Thread implements
		Thread.UncaughtExceptionHandler {

	public static final Version VERSION = new Version(3, 5, 0);

	private static final int PERIFERICO_PORT = 55000;

	public static final int DEFAULT_COP_PORT = 55000;

	private static final int MAX_RESTARTS_ALLOWED = 3;

	private static final int RESTART_COUNT_RESET_PERIOD_M = 60; // minutes

	private static final long MAX_TIME_DIFF_FOR_SYNC_S = 600; // seconds

	// Attention: the strings in this enum are used in UI InformaticStatusWidget
	public enum CommonCfgResult {
		OK, UNPARSABLE, CONSISTENCY_ERROR, SAVE_ERROR, LOAD_ERROR, INCOMPATIBLE, CONFIG_LOAD_ERROR, CONFIG_START_ERROR
	}

	// Available rounding modes for decimal numbers to text conversion:
	// Round half away from zero: RoundingMode.HALF_UP
	// Round half towards zero: RoundingMode.HALF_DOWN
	// Round half to even: RoundingMode.HALF_EVEN
	public static final RoundingMode ROUNDING_MODE_FOR_SAMPLE_DATA = RoundingMode.HALF_EVEN;

	public static final RoundingMode ROUNDING_MODE_FOR_AGGREGATE_DATA = RoundingMode.HALF_UP;

	private static final String PROXY_BASE_PATH = "/proxy/analyzer_";
	
	private static Logger logger = Logger.getLogger("periferico."
			+ Periferico.class.getSimpleName());

	private static Periferico perifericoApp = null;

	private static String[] perifericoArgs = null;

	private BoardsCfg boardsCfg = null;

	private LoginCfg loginCfg = null;

	private CommonCfg commonCfg = null;

	private PerifericoStatus status;

	private ConfigManager configManager;

	private BoardManager boardManager;

	private BoardManagementForUI boardManagementForUI;

	private AcqManager acqManager;

	private DriverManager driverManager;

	private StorageManager storageManager;

	private DataManager dataManager;

	private GpsdClient gpsdClient = null;

	private Config config = null;

	private ConnectionManager connectionManager = null;

	private Server jettyServer = null;
	
	private WebAppContext webAppContext = null;

	private int restartCount = 0;

	private long lastRestartTime = 0;

	private Date copDate = null;

	private boolean dataInTheFuture = false;

	private boolean deleteFutureData = false;

	private boolean bckCommonCfgActive = false;
	
	private Map<UUID, ServletHolder> mapProxy = new HashMap<UUID, ServletHolder>();

	// TODO: predisposition for automatic calibration check management
	// private CalibCheckManager calibCheckManager = new CalibCheckManager();

	private Periferico() {
		Properties log4jProperties = ConfigManager.readLog4jProperties();
		if (log4jProperties == null)
			PropertyConfigurator.configure(getClass().getResource(
					"/log4j.properties"));
		else
			PropertyConfigurator.configure(log4jProperties);
		Runtime.getRuntime().addShutdownHook(this);
		logger.info("Starting Periferico application, version " + VERSION
				+ " ...");
		if (log4jProperties == null)
			logger.info("Logger initialized using application"
					+ " built in properties");
		else
			logger.info("Logger initialized using user defined properties");
		String strDefCharset = Charset.defaultCharset().toString();
		logger.info("Platform character set is: " + strDefCharset);
		if (!"UTF-8".equals(strDefCharset))
			logger.error("Platform character set should be: UTF-8");
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public static void main(String[] args) throws Exception {
		try {
			perifericoArgs = args;
			Periferico p = getInstance();
			if (!p.startJetty())
				System.exit(2);
		} catch (Exception ex) {
			System.exit(1);
		}
	}

	// NOTE: this function exists in order to start the application both in
	// standard environment and in GWT emulator
	public static synchronized Periferico getInstance() {
		if (perifericoApp == null) {
			perifericoApp = new Periferico();
			if (!perifericoApp.init(perifericoArgs)) {
				String msg = "Fatal error: cannot start Periferico application";
				logger.error(msg);
				System.out.println("   " + msg);
				throw new RuntimeException(msg);
			}
			perifericoApp.printStartupResult();
		}
		return (perifericoApp);
	}

	private boolean init(String[] args) {
		try {
			Set<String> argsSet = new HashSet<String>();
			if (args != null) {
				for (int i = 0; i < args.length; i++)
					argsSet.add(args[i]);
			}
			status = new PerifericoStatus();
			logger.info("Initializing configuration manager...");
			configManager = new ConfigManager();
			commonCfg = configManager.loadCommonConfig();
			if (commonCfg == null) {
				commonCfg = configManager.loadCommonConfigBackup();
				if (commonCfg == null)
					return false;
				bckCommonCfgActive = true;
			} else {
				bckCommonCfgActive = false;
			}
			boolean comediEnable =
					!"false".equalsIgnoreCase(System.getProperty("periferico.comedi"));
			logger.info("Comedi library is " + (comediEnable ? "" : "NOT ") + "enabled");
			boardsCfg = configManager.loadBoardsCfg(comediEnable);
			if (boardsCfg == null)
				return false;
			loginCfg = configManager.loadLoginCfg();
			if (loginCfg == null)
				return false;
			logger.info("Initializing connection manager...");
			connectionManager = new ConnectionManager();
			logger.info("Initializing storage manager...");
			storageManager = new StorageManager(
					commonCfg.getStorageManagerCfg());
			deleteFutureData = argsSet.contains("delete_future_data");
			dataInTheFuture = storageManager
					.findAndDeleteDataInTheFuture(!deleteFutureData);
			status.setDataInTheFuture(dataInTheFuture && !deleteFutureData);
			logger.info("Initializing IO board acquisition manager...");
			acqManager = new AcqManager();
			logger.info("Initializing data manager...");
			dataManager = new DataManager(commonCfg, storageManager,
					acqManager.getAcqStats());
			logger.info("Initializing data port acquisition manager...");
			driverManager = new DriverManager(commonCfg.getParameterIdSet());
			boolean drvCfgOK = true;
			for (DriverInfo di : driverManager.getDriverInfo())
				drvCfgOK = drvCfgOK && di.isOK();
			status.setDriverConfigsOK(drvCfgOK);
			gpsdClient = new GpsdClient();
			logger.info("Initializing user interface board manager...");
			boardManagementForUI = new BoardManagementForUI(
					boardsCfg.getBoardDescriptors());
			config = configManager.loadConfig(argsSet
					.contains("as_new_station"));
			status.setLoadConfigurationStatus(config.getStatus());
			configStartup();
			return true;
		} catch (Exception ex) {
			logger.error("Initialization failed", ex);
			return false;
		}
	}

	private void printStartupResult() {
		String msg = null;
		Config.Status cfgStatus = getStatus().getLoadConfigurationStatus();
		if (cfgStatus == Config.Status.OK)
			msg = "Configuration is OK";
		else
			msg = "Error: configuration is " + Config.statusToString(cfgStatus);
		System.out.println("   " + msg);
		if (dataInTheFuture) {
			if (!deleteFutureData) {
				System.out.println("   "
						+ "Warning: data files contain data in the future;");
				System.out.println("   " + "         "
						+ "restart the application with the argument");
				System.out.println("   " + "         " + "'delete_future_data'"
						+ " to delete the data in the future");
			} else {
				System.out.println("   "
						+ "Warning: data in the future found and deleted");
			}
		}
		if (getStatus().isOK())
			msg = "Data acquisition started";
		else
			msg = "Error: errors occurred starting acquisition";
		System.out.println("   " + msg);
	}

	private void forceDisconnect() {
		try {
			if (connectionManager != null)
				connectionManager.disconnect();
		} catch (Exception ex) {
			logger.error("Error occurred forcing disconnect", ex);
		}
	}

	/**
	 * This function updates and then returns Periferico application status
	 * 
	 * @return returns current Periferico application status
	 */
	public PerifericoStatus getStatus() {
		status.setFailedDataPortThreadsNumber(driverManager
				.getStoppedOnFatalErrorThreadsNumber());
		status.setDataWriteErrorCount(dataManager.getTotalWriteErrorCount());
		return status;
	}

	public BoardsCfg getBoardsCfg() {
		return boardsCfg;
	}

	public LoginCfg getLoginCfg() {
		return loginCfg;
	}

	public synchronized CommonCfg getCommonCfg() {
		return commonCfg;
	}

	public synchronized Config getConfig() {
		return config;
	}

	public Config getConfigCopy() {
		try {
			return ((Config) PerifericoUtil.copy(getConfig()));
		} catch (Exception e) {
			logger.error("Configuration copy failed", e);
		}
		return null;
	}

	public Station getStation() {
		return getConfig().getStation();
	}

	public Config parseConfig(BufferedReader br) throws Exception {
		return configManager.parseConfig(br);
	}

	public Config getHistoricConfig(Date date) throws Exception {
		return configManager.getHistoricConfig(date);
	}

	public List<Date> readHistoricConfigsList(Date startDate, Date endDate,
			Integer limit) {
		return configManager.readHistoricConfigsList(startDate, endDate, limit);
	}

	public String getResourceName(UUID uuid) {
		return config.getStation().getResourceName(uuid);
	}

	public UUID getStationId() {
		return config.getStation().getId();
	}

	public List<String> getConfigFile(long minLastModifiedTime)
			throws PerifericoException {
		try {
			return configManager.getConfigFile(minLastModifiedTime);
		} catch (IOException e) {
			throw new PerifericoException("cfg_file_read_error");
		}
	}

	public StorageManager getStorageManager() {
		return storageManager;
	}

	public DriverManager getDriverManager() {
		return driverManager;
	}

	public GpsDatum getLastGpsDatum() {
		if (!config.getStation().isGpsInstalled())
			return null;
		return gpsdClient.getLastDatum();
	}

	public void connectToCop() throws ConnectionException {
		connectionManager.connect(config.getStation().getConnectionParams());
	}

	public void connectToCop(String deviceType, String copAddress)
			throws ConnectionException {
		ConnectionParams userCP = ConnectionParams.getInstance(deviceType);
		if (userCP == null || copAddress == null || copAddress.trim().isEmpty())
			connectToCop();
		userCP.setOutgoingCallEnabled(true);
		userCP.setCopIP(copAddress);
		connectionManager.connect(userCP);
	}

	public boolean isConnectedToCop() throws ConnectionException {
		return connectionManager.isConnected();
	}

	public void disconnect() throws ConnectionException {
		connectionManager.disconnect();
	}

	public SetConfigResult setNewConfig(Config newCfg) {
		return setNewConfig(newCfg, false);
	}

	public SetConfigResult checkNewConfig(Config newCfg, boolean allowObsolete) {
		if (newCfg == null)
			throw new IllegalArgumentException("Null argument not permitted");
		boolean obsolete = (newCfg.getId() != null && config.getId() != null && !newCfg
				.getId().equals(config.getId()))
				|| (newCfg.getId() == null && config.getId() != null);
		if (!allowObsolete && obsolete)
			return new SetConfigResult(
					newCfg.isHistoric() ? SetConfigResult.Result.HISTORIC
							: SetConfigResult.Result.OBSOLETE);
		if (obsolete)
			logger.warn("Checking obsolete configuration with id: "
					+ newCfg.getId() + " ...");
		else
			logger.info("Checking configuration with id: " + newCfg.getId()
					+ " ...");
		try {
			newCfg.checkConfig();
			logger.info("Configuration check successful");
			return new SetConfigResult(SetConfigResult.Result.CHECK_OK);
		} catch (ConfigException e) {
			logger.error("Configuration check failed", e);
			return new SetConfigResult(SetConfigResult.Result.CHECK_ERROR, e);
		}
	}

	public synchronized SetConfigResult setNewConfig(Config newCfg,
			boolean allowObsolete) {
		logger.info("Setting new configuration...");
		SetConfigResult scr = checkNewConfig(newCfg, allowObsolete);
		if (scr.getResult() != SetConfigResult.Result.CHECK_OK)
			return scr;
		configShutdown();
		try {
			status.setSaveNewConfigurationStatus(null);
			status.setLoadConfigurationStatus(null);
			configManager.saveNewConfig(newCfg);
			status.setSaveNewConfigurationStatus(true);
		} catch (ConfigException e) {
			status.setSaveNewConfigurationStatus(false);
			return new SetConfigResult(SetConfigResult.Result.ACTIVATION_ERROR,
					e);
		}
		// NOTE: After saving the new configuration, the configuration is
		// reloaded from the xml file, in order to ensure that it will be loaded
		// correctly the next time the application is restarted.
		// Besides this, transient variables initialized in constructors are
		// left uninitialized when the configuration is copied using
		// serialization: reloading the configuration from xml file solves this
		// problem.
		config = configManager.loadConfig(false);
		status.setLoadConfigurationStatus(config.getStatus());
		if (config.getStatus() != Config.Status.OK) {
			return new SetConfigResult(SetConfigResult.Result.ACTIVATION_ERROR);
		}
		status.resetCurrentThreadFailures();
		configStartup();
		if (status.isOK(true))
			return new SetConfigResult(SetConfigResult.Result.ACTIVATED);
		else
			return new SetConfigResult(SetConfigResult.Result.ACTIVATION_ERROR);
	}

	public synchronized CommonCfgResult setNewCommonCfg(BufferedReader ccReader) {
		logger.info("Setting new common configuration ...");
		CommonCfg newCommonCfg = configManager.unmarshalCommonCfg(ccReader);
		if (newCommonCfg == null) {
			status.setCommonConfigFromCopStatus(CommonCfgResult.UNPARSABLE);
			return CommonCfgResult.UNPARSABLE;
		}
		try {
			logger.info("Checking common configuration consistency ...");
			newCommonCfg.checkAndInit();
		} catch (Exception ex) {
			logger.error("Common configuration consistency error", ex);
			status.setCommonConfigFromCopStatus(CommonCfgResult.CONSISTENCY_ERROR);
			return CommonCfgResult.CONSISTENCY_ERROR;
		}
		if (!bckCommonCfgActive) {
			if (!configManager.backupCommonCfg()) {
				status.setCommonConfigFromCopStatus(CommonCfgResult.SAVE_ERROR);
				return CommonCfgResult.SAVE_ERROR;
			}
		}
		bckCommonCfgActive = true;
		if (!configManager.saveNewCommonCfg(newCommonCfg)) {
			status.setCommonConfigFromCopStatus(CommonCfgResult.SAVE_ERROR);
			return CommonCfgResult.SAVE_ERROR;
		}
		newCommonCfg = configManager.loadCommonConfig();
		if (newCommonCfg == null) {
			status.setCommonConfigFromCopStatus(CommonCfgResult.LOAD_ERROR);
			return CommonCfgResult.LOAD_ERROR;
		}
		configShutdown();
		commonCfg = newCommonCfg;
		bckCommonCfgActive = false;
		status.setCommonConfigFromCopStatus(CommonCfgResult.OK);
		dataManager.onCommonConfigUpdated(commonCfg);
		logger.info("New common configuration set");
		status.setLoadConfigurationStatus(null);
		config = configManager.loadConfig(false);
		status.setLoadConfigurationStatus(config.getStatus());
		status.resetCurrentThreadFailures();
		configStartup();
		if (config.getStatus() == Config.Status.CHECK_ERROR) {
			status.setCommonConfigFromCopStatus(CommonCfgResult.INCOMPATIBLE);
			return CommonCfgResult.INCOMPATIBLE;
		}
		if (config.getStatus() != Config.Status.OK) {
			status.setCommonConfigFromCopStatus(CommonCfgResult.CONFIG_LOAD_ERROR);
			return CommonCfgResult.CONFIG_LOAD_ERROR;
		}
		if (!status.isOK()) {
			status.setCommonConfigFromCopStatus(CommonCfgResult.CONFIG_START_ERROR);
			return CommonCfgResult.CONFIG_START_ERROR;
		}
		return CommonCfgResult.OK;
	}

	public BoardManagementForUI getBoardManagementForUI() {
		return boardManagementForUI;
	}

	public Boolean doTimeSync(String server, Date copDate)
			throws PerifericoException {
		if (copDate != null) {
			this.copDate = copDate;
			long perifTime_ms = System.currentTimeMillis();
			long copTime_ms = copDate.getTime();
			long difference_s = (copTime_ms - perifTime_ms) / 1000;
			if (Math.abs(difference_s) > MAX_TIME_DIFF_FOR_SYNC_S) {
				logger.error("Time synchronization skipped: excessive time "
						+ "difference with COP: " + difference_s + "s");
				return null;
			}
		}
		String ls = System.getProperty("line.separator");
		ProcessBuilder pb = new ProcessBuilder("doTimeSync.sh", server);
		Process p = null;
		BufferedReader br = null;
		try {
			p = pb.start();
			br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuffer timeSyncLog = new StringBuffer();
			String line;
			boolean firstLine = true;
			while ((line = br.readLine()) != null) {
				if (!firstLine)
					timeSyncLog.append(ls);
				timeSyncLog.append(line);
				firstLine = false;
			}
			boolean syncSuccedeed = p.waitFor() == 0;
			if (syncSuccedeed) {
				logger.info("Time synchronization succeeded");
				logger.debug(timeSyncLog.toString());
			} else {
				logger.error("Time synchronization failed");
				logger.error(timeSyncLog.toString());
			}
			return syncSuccedeed;
		} catch (Exception ex) {
			String msg = "Cannot execute time synchronization script";
			logger.error(msg, ex);
			throw new PerifericoException(msg);
		} finally {
			if (p != null) {
				try {
					p.getOutputStream().close();
				} catch (IOException e) {
				}
				try {
					p.getInputStream().close();
				} catch (IOException e) {
				}
				try {
					if (br != null)
						br.close();
					else
						p.getErrorStream().close();
				} catch (Exception e) {
				}
			}
		}
	}

	public Date getCopDate() {
		return copDate;
	}

	@Override
	public void run() {
		// NOTE: this is the application's shutdown hook
		String msg = "Stopping Periferico application...";
		logger.info(msg);
		forceDisconnect();
		configShutdown();
		if (jettyServer != null)
			try {
				logger.info("Waiting for user interface service to terminate");
				jettyServer.join();
				msg = "User interface service stopped";
				logger.info(msg);
				System.out.println("   " + msg);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		msg = "Periferico application stopped";
		logger.info(msg);
		System.out.println("   " + msg);
	}

	@Override
	public synchronized void uncaughtException(Thread t, Throwable e) {
		logger.error("Crash detected for thread: " + t.getName(), e);
		status.incrementThreadFailures();
		if (lastRestartTime < System.currentTimeMillis()
				- RESTART_COUNT_RESET_PERIOD_M * 60 * 1000) {
			restartCount = 0;
			logger.info("Resetting restart counter: "
					+ "no errors detected during reset period");
		}
		if (restartCount < MAX_RESTARTS_ALLOWED) {
			lastRestartTime = System.currentTimeMillis();
			restartCount++;
			logger.warn("Trying to recover restarting current configuration ...");
			configShutdown();
			status.resetCurrentThreadFailures();
			configStartup();
		} else {
			logger.error("Too many restarts attempted (" + restartCount
					+ "), give up restarting");
		}
	}

	private void configStartup() {
		try {
			logger.info("Configuration initialization...");
			status.setConfigActivationStatus(null);
			List<Board> boards = config.getBoardList().getBoards();
			// TODO: it may be better having the portion of BoardManager's init
			// code, that implements boards detection and boards's capability
			// query, called in Periferico's init: to achieve this
			// BoardManager's init should be split in two functions.
			boardManager = new BoardManager(boardsCfg.getBoardDescriptors(),
					boards);
			int result = boardManager.init();
			status.setBoardManagerInitStatus(result >= 0);
			status.setConfiguredBoardsNumber(boardManager
					.getConfiguredBoardsNumber());
			status.setInitializedBoardsNumber(result >= 0 ? result : 0);
			int numFailed = boardManager.doIOBinding(config.getStation()
					.getListIOUser());
			status.setFailedBoardBindingsNumber(numFailed);
			if (numFailed > 0)
				logger.error(numFailed + " bind(s) failed");
			driverManager.destroyDrivers();
			if (config.getStatus() == Config.Status.OK) {
				logger.info("Configuration is OK, activation in progress...");
				okConfigStartup(boards);
				status.setConfigActivationStatus(true);
				if (status.isOK(true)) {
					logger.info("Configuration activated successfully");
				} else {
					logger.error("Configuration activated with errors");
					logger.error(status);
				}
			} else {
				logger.error("Configuration is "
						+ Config.statusToString(config.getStatus())
						+ " and cannot be activated");
				koConfigStartup();
				status.setConfigActivationStatus(false);
			}
			// NOTE: at application startup webAppContext is still null and so proxies
			// initialization is deferred at the end of startJetty function 
			if (webAppContext != null)
				addAnalyzerUiProxies();
		} catch (Exception ex) {
			logger.error("Unexpected failure during configuration"
					+ " initialization/activation", ex);
			status.setConfigActivationStatus(false);
		}
	}

	private void okConfigStartup(List<Board> boards) {
		int numEnabledDPA = 0;
		int numDriverInitialized = 0;
		for (Analyzer an : config.getStation().getListAnalyzer()) {
			if (!(an instanceof DataPortAnalyzer))
				continue;
			if (!an.isEnabled())
				continue;
			DataPortAnalyzer dpa = (DataPortAnalyzer) an;
			numEnabledDPA++;
			try {
				driverManager.initDriver(dpa);
				numDriverInitialized++;
			} catch (Exception ex) {
				logger.error("Driver initialization failed for data port"
						+ " analyzer " + an.getName(), ex);
			}
		}
		status.setEnabledDataPortAnalyzersNumber(numEnabledDPA);
		status.setInitializedDataPortDriversNumber(numDriverInitialized);
		status.setFailedDataPortThreadsNumber(null);
		boardManagementForUI.setBoardManager(boardManager);
		dataManager.startManagement(config.getStation(), config.getStation()
				.isGpsInstalled() ? gpsdClient : null, boardManager
				.getAcqStats());
		acqManager.startAcquisition(boardManager.getMapAcquisitionItfs(boards));
		boardManager.start();
		driverManager.startAcquisition();
		if (config.getStation().isGpsInstalled())
			gpsdClient.start();
	}

	private void koConfigStartup() {
		status.setEnabledDataPortAnalyzersNumber(null);
		status.setInitializedDataPortDriversNumber(null);
		status.setFailedDataPortThreadsNumber(null);
		boardManagementForUI.setBoardManager(boardManager);
	}

	private void configShutdown() {
		// TODO: verificare che le join siano su tutti gli stop !!!
		try {
			logger.info("Stopping activity with current configuration...");
			status.setConfigActivationStatus(null);
			removeAnalyzerUiProxies();
			if (gpsdClient != null)
				gpsdClient.stop();
			if (driverManager != null)
				driverManager.destroyDrivers();
			if (acqManager != null)
				acqManager.stopAcquisition();
			if (boardManager != null)
				boardManager.destroy();
			if (dataManager != null)
				dataManager.stopManagement();
			logger.info("Activity stopped");
		} catch (Exception ex) {
			logger.error("Unexpected failure during configuration"
					+ " shutdown", ex);
		}
	}

	private boolean startJetty() throws Exception {
		try {
			logger.info("Starting Jetty application server...");
			int port = PERIFERICO_PORT;
			String strPort = System.getProperty("periferico.port");
			if (strPort != null && !strPort.trim().isEmpty()) {
				try {
					int tmpPort = Integer.parseInt(strPort.trim());
					if (tmpPort <= 0)
						throw new IllegalArgumentException(
								"Periferico application port should be > 0");
					port = tmpPort;
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException(
							"Cannot parse value for Periferico application port: "
									+ strPort);
				}
			}
			logger.info("Using ip port " + port + " for all services");
			Server server = new Server();
			Connector connector = new SelectChannelConnector();
			connector.setPort(port);
			connector.setHost("0.0.0.0");
			server.addConnector(connector);
			WebAppContext wac = new WebAppContext();
			wac.setContextPath("/");
			String sessionName = "SESSIONID_PERIF";
			if (config != null && config.getStation() != null)
				sessionName += config.getStation().getIdAsString();
			@SuppressWarnings("unchecked")
			Map<Object, Object> initParams = wac.getInitParams();
			if (initParams == null)
				initParams = new HashMap<Object, Object>();
			initParams.put("org.mortbay.jetty.servlet.SessionCookie",
					sessionName);
			initParams.put("org.mortbay.jetty.servlet.SessionURL",
					sessionName.toLowerCase());
			wac.setInitParams(initParams);
			wac.setWar("bin/perifericoUI.war");
			server.setStopAtShutdown(true);
			configureRequestLogging(server, wac);
			server.start();
			String msg = "User interface service started";
			logger.info(msg);
			System.out.println("   " + msg);
			// NOTE: do not change this message because it is used to trigger
			// startup completion detection in the init script:
			// /etc/init.d/periferico
			System.out.println("   Periferico startup completed");
			jettyServer = server;
			webAppContext = wac;
			addAnalyzerUiProxies();
			return true;
		} catch (Exception ex) {
			String msg = "Fatal error: cannot start user interface service";
			logger.error(msg, ex);
			System.out.println("   " + msg);
			return false;
		}
	}

	private void addServlet(ServletHolder servletHolder, String pathSpec) {
		try {
			webAppContext.addServlet(servletHolder, pathSpec);
			logger.debug("Added servlet holder '" + servletHolder.getName()
					+ "', with servlet '" + servletHolder.getServlet()
					+ "' at path '" + pathSpec + "'");
		} catch (ServletException e) {
			logger.error("Error adding servlet " + servletHolder.getName(), e);
		}
	}

	private void removeServlet(ServletHolder servletHolder) {
		try {
			logger.debug("Removing servlet holder '" + servletHolder.getName()
					+ "', with servlet " + servletHolder.getServlet());
			ServletHandler handler = webAppContext.getServletHandler();
			if (handler == null) {
				logger.warn("Servlet handler is null, nothing to do!");
				return;
			}
			List<ServletHolder> holders = new ArrayList<ServletHolder>();
			Set<String> names = new HashSet<String>();
			if (handler.getServlets() != null) {
				for (ServletHolder holder : handler.getServlets()) {
					if (servletHolder.getName().equals(holder.getName())) {
						logger.trace("Discarding servlet holder '" + holder.getName());
						names.add(holder.getName());
					} else {
						logger.trace("Keeping servlet holder '" + holder.getName());
						holders.add(holder);
					}
				}
			}
			List<ServletMapping> mappings = new ArrayList<ServletMapping>();
			if (handler.getServletMappings() != null) {
				for (ServletMapping mapping : handler.getServletMappings()) {
					if (!names.contains(mapping.getServletName())) {
						mappings.add(mapping);
						logger.trace("Keeping mapping: " + mapping.getServletName());
					} else {
						logger.trace("Discarding mapping: "
								+ mapping.getServletName());
					}
				}
			}
			handler.setServletMappings(mappings
					.toArray(new ServletMapping[mappings.size()]));
			handler.setServlets(holders.toArray(new ServletHolder[holders
					.size()]));
		} catch (Exception e) {
			logger.error("Error removing servlet " + servletHolder.getName(), e);
		}
	}
	
	private void addAnalyzerUiProxies() throws PerifericoException {
		if (config == null || config.getStation() == null
				|| config.getStation().getListAnalyzer() == null)
			return;
		logger.info("Web UI proxies initialization...");
		for (Analyzer an : config.getStation().getListAnalyzer()) {
			if (!an.isEnabled())
				continue;
			if (an.getUiURL() != null && !an.getUiURL().isEmpty()) {
				try {
					addAnalyzerUiProxy(an);
				} catch (Exception ex) {
					logger.error("Web UI proxy initialization failed for"
							+ " analyzer " + an.getName(), ex);
				}
			}
		}
	}

	private void addAnalyzerUiProxy(Analyzer analyzer) throws Exception {
		URL url = new URL(analyzer.getUiURL());
		if (!"http".equals(url.getProtocol()))
			throw new PerifericoException(
					"Unsupported protocol for analyzer web UI proxy: '"
							+ url.getProtocol() + "'");
		String proxyPath = PROXY_BASE_PATH + analyzer.getId();
		Transparent transparentProxy = new AnalyzerProxy(proxyPath,
				url.getHost(), (url.getPort() == -1 ? 80 : url.getPort()));
		ServletHolder proxyHolder = new ServletHolder(transparentProxy);
		addServlet(proxyHolder, proxyPath + "/*");
		mapProxy.put(analyzer.getId(), proxyHolder);
		analyzer.setUiProxyPath("." + proxyPath + (url.getPath().isEmpty() ? "/" : url.getPath()));
		logger.info("Added proxy with ID " + analyzer.getId()
			+ " for analyzer " + analyzer.getName());
	}

	private void removeAnalyzerUiProxies() {
		for (Analyzer an : config.getStation().getListAnalyzer())
			an.setUiProxyPath(null);
		Iterator<Map.Entry<UUID, ServletHolder>> itMap = mapProxy.entrySet().iterator();
		while (itMap.hasNext()) {
			Map.Entry<UUID, ServletHolder> entry = itMap.next();
			UUID proxyId = entry.getKey();
			logger.debug("Removing proxy with ID " + proxyId);
			ServletHolder proxyHolder = mapProxy.get(proxyId);
			if (proxyHolder != null) {
				removeServlet(proxyHolder);
				itMap.remove();
				logger.info("Removed proxy with ID " + proxyId);
			} else {
				logger.error("Cannot find proxy with ID " + proxyId);
			}
		}
	}

	private static void configureRequestLogging(Server server, WebAppContext wac) {
		HandlerCollection handlers = new HandlerCollection();
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		RequestLogHandler logHandler = new RequestLogHandler();
		handlers.setHandlers(new Handler[] { contexts, wac, logHandler });
		server.setHandler(handlers);
		NCSARequestLog requestLog = new NCSARequestLog(
				"./log/jetty-yyyy_mm_dd.request.log");
		requestLog.setRetainDays(30);
		requestLog.setAppend(true);
		requestLog.setExtended(false);
		TimeZone timeZone = Calendar.getInstance().getTimeZone();
		requestLog.setLogTimeZone(timeZone.getID());
		logHandler.setRequestLog(requestLog);
	}

	public static double roundSampleData(double value, int numDecimals) {
		return roundHalfToEven(value, numDecimals);
	}

	public static double roundAggregateData(double value, int numDecimals) {
		return roundHalfAwayFromZero(value, numDecimals);
	}

	// Round half to even
	private static double roundHalfToEven(double value, int numDecimals) {
		if (numDecimals > 16)
			return value;
		if (numDecimals <= 0)
			return Math.rint(value);
		double multiplier = Math.pow(10, numDecimals);
		return Math.rint(value * multiplier) / multiplier;
	}

	// Round half away from zero
	private static double roundHalfAwayFromZero(double value, int numDecimals) {
		if (numDecimals > 16)
			return value;
		if (numDecimals <= 0)
			return roundHAFZ(value);
		double multiplier = Math.pow(10, numDecimals);
		return roundHAFZ(value * multiplier) / multiplier;
	}

	private static double roundHAFZ(double value) {
		return Math.signum(value) * Math.floor(Math.abs(value) + 0.5d);
	}

	// // Round half towards plus infinity
	// private static double roundHalfTowardsPlusInfinity(double value,
	// int numDecimals) {
	// if (numDecimals > 16)
	// return value;
	// if (numDecimals <= 0)
	// return roundHTPI(value);
	// double multiplier = Math.pow(10, numDecimals);
	// return roundHTPI(value * multiplier) / multiplier;
	// }
	//
	// private static double roundHTPI(double value) {
	// return Math.floor(value + 0.5d);
	// }

}
