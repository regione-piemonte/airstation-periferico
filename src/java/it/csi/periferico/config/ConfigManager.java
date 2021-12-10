/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: manages the configurations
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: ConfigManager.java,v 1.50 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config;

import it.csi.periferico.PerifericoUtil;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 * Manages the configurations
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class ConfigManager {

	private static final String COMMON_MAPPING = "common_mapping.xml";

	private static final String BOARDS_MAPPING = "boards_mapping.xml";

	private static final String LOGIN_MAPPING = "login_mapping.xml";

	private static final String STATION_MAPPING = "station_mapping.xml";

	private static final String CFG_DIR = "config";

	private static final String COMMON_CFG = "common.xml";

	private static final String COMMON_CFG_BCK = "common-bck.xml";

	private static final String BOARDS_CFG = "boards.xml";

	private static final String BOARDS_CFG_NO_COMEDI = "boards-no-comedi.xml";

	private static final String LOGIN_CFG = "login.xml";

	private static final String STATION_CFG = "station.xml";

	private static final String HISTORIC_CFG_DIR = CFG_DIR + File.separator
			+ "historic";

	private static final String HISTORIC_FILE_PREFIX = "station_";

	private static final String HISTORIC_FILE_DATE_FMT_STR = "yyyyMMdd_HHmm";

	private static final String LOG4J_CONFIG = "log4j.properties";

	private static Logger logger = Logger.getLogger("periferico."
			+ ConfigManager.class.getSimpleName());

	private Mapping commonMapping;

	private Mapping boardsMapping;

	private Mapping loginMapping;

	private Mapping stationMapping;

	public ConfigManager() throws ConfigException {
		logger.info("Loading XML mapping files...");
		try {
			ClassLoader cl = getClass().getClassLoader();
			boardsMapping = new Mapping();
			boardsMapping.loadMapping(cl.getResource(BOARDS_MAPPING));
			commonMapping = new Mapping();
			commonMapping.loadMapping(cl.getResource(COMMON_MAPPING));
			loginMapping = new Mapping();
			loginMapping.loadMapping(cl.getResource(LOGIN_MAPPING));
			stationMapping = new Mapping();
			stationMapping.loadMapping(cl.getResource(STATION_MAPPING));
			logger.info("Mapping files loaded");
		} catch (Exception ex) {
			throw new ConfigException("Error loading XML mapping files", ex);
		}
	}

	public synchronized Config loadConfig(boolean changeAllUUIDs) {
		logger.info("Loading station configuration ...");
		File stationCfgFile = new File(CFG_DIR + File.separator + STATION_CFG);
		if (!stationCfgFile.exists()) {
			logger.error("Station configuration file missing: "
					+ stationCfgFile.getAbsolutePath());
			return new Config(Config.Status.MISSING);
		}
		Config config = null;
		try {
			config = (Config) loadXMLConfigFile(stationMapping, STATION_CFG);
		} catch (Exception ex) {
			logger.error("Station configuration load/parse error: "
					+ stationCfgFile.getAbsolutePath(), ex);
			return new Config(Config.Status.PARSE_ERROR);
		}
		logger.info("Station configuration loaded");
		if (changeAllUUIDs) {
			logger.warn("Configuration conversion for usage in a"
					+ " new station...");
			config.changeAllUUIDs();
			logger.info("Configuration conversion completed");
		}
		logger.info("Configuration id is: " + config.getId());
		logger.info("Checking configuration...");
		try {
			config.initConfig();
			config.checkConfig();
		} catch (ConfigException e) {
			logger.error("Configuration check failed", e);
		}
		return config;
	}

	public Config parseConfig(BufferedReader br) throws Exception {
		Mapping mapping = new Mapping();
		mapping.loadMapping(getClass().getClassLoader().getResource(
				STATION_MAPPING));
		Unmarshaller unmar = new Unmarshaller(mapping);
		InputSource is = new InputSource(br);
		Config parsedConfig = (Config) unmar.unmarshal(is);
		parsedConfig.initConfig();
		// NOTE: the task of calling checkConfig() is demanded to the owner of
		// the returned configuration
		return parsedConfig;
	}

	public Config getHistoricConfig(Date date) throws Exception {
		BufferedReader br = null;
		try {
			DateFormat historicFileDateFormat = new SimpleDateFormat(
					HISTORIC_FILE_DATE_FMT_STR);
			String bckCfgFileName = HISTORIC_CFG_DIR + File.separator
					+ HISTORIC_FILE_PREFIX
					+ historicFileDateFormat.format(date) + ".xml";
			File bckCfgFile = new File(bckCfgFileName);
			br = new BufferedReader(new FileReader(bckCfgFile));
			Config cfg = parseConfig(br);
			cfg.setHistoric(true);
			br.close();
			return cfg;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	public synchronized void saveNewConfig(Config newCfg)
			throws ConfigException {
		if (newCfg == null)
			throw new IllegalArgumentException("Null argument not permitted");
		logger.info("Saving new configuration with id: " + newCfg.getId());
		Config cfgToSave = null;
		try {
			cfgToSave = (Config) PerifericoUtil.copy(newCfg);
			cfgToSave.setNewId();
		} catch (Exception e) {
			logger.error("Configuration copy failed", e);
			throw new ConfigException("error_new_conf");
		}
		if (!backupConfigFile())
			throw new ConfigException("error_archiving_conf");
		if (!saveConfig(cfgToSave))
			throw new ConfigException("error_saving_conf");
		logger.info("New configuration saved successfully");
	}

	public synchronized List<String> getConfigFile(long minLastModifiedTime)
			throws IOException {
		File stationCfgFile = new File(CFG_DIR + File.separator + STATION_CFG);
		if (!stationCfgFile.exists()) {
			return null;
		}
		long cfgFileLastModifiedTime = stationCfgFile.lastModified();
		if (cfgFileLastModifiedTime <= minLastModifiedTime)
			return null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(stationCfgFile));
		} catch (FileNotFoundException e) {
			return null;
		}
		List<String> cfgLines = new ArrayList<String>();
		cfgLines.add(Long.toString(cfgFileLastModifiedTime));
		String line = null;
		while ((line = br.readLine()) != null) {
			cfgLines.add(line);
		}
		br.close();
		return cfgLines;
	}

	public CommonCfg unmarshalCommonCfg(BufferedReader ccReader) {
		try {
			logger.info("Unmarshalling Common Config");
			Unmarshaller unmar = new Unmarshaller(commonMapping);
			InputSource is = new InputSource(ccReader);
			return ((CommonCfg) unmar.unmarshal(is));
		} catch (Exception e) {
			String msg = "Error unmarshalling Common Config";
			logger.error(msg, e);
			return null;
		}
	}

	public synchronized boolean saveNewCommonCfg(CommonCfg newCommonCfg) {
		logger.info("Saving new common configuration...");
		if (!saveXMLConfigFile(commonMapping, newCommonCfg, COMMON_CFG)) {
			logger.error("Cannot save new common configuration");
			return false;
		}
		logger.info("New common configuration saved");
		return true;
	}

	public synchronized boolean backupCommonCfg() {
		logger.info("Backing up current common configuration...");
		File bckCommonFile = new File(CFG_DIR + File.separator + COMMON_CFG_BCK);
		if (bckCommonFile.exists()) {
			if (bckCommonFile.delete()) {
				logger.info("Old common configuration backup file deleted");
			} else {
				logger.error("Cannot delete old common configuration backup "
						+ "file");
				return false;
			}
		}
		File commonFile = new File(CFG_DIR + File.separator + COMMON_CFG);
		if (!commonFile.renameTo(bckCommonFile)) {
			logger.error("Cannot backup current common configuration file");
			return false;
		}
		logger.info("Common configuration backup succeeded");
		return true;
	}

	public BoardsCfg loadBoardsCfg(boolean enableComedi) {
		String cfgFile = enableComedi ? BOARDS_CFG : BOARDS_CFG_NO_COMEDI;
		logger.info("Loading acquisition boards configuration '" + cfgFile + "'...");
		try {
			BoardsCfg boardsCfg = (BoardsCfg) loadXMLConfigFile(boardsMapping, cfgFile);
			logger.info("Acquisition boards configuration loaded");
			return boardsCfg;
		} catch (Exception e) {
			logger.error("Acquisition boards configuration load/parse error", e);
			return null;
		}
	}

	public LoginCfg loadLoginCfg() {
		logger.info("Loading login configuration ...");
		try {
			LoginCfg loginCfg = (LoginCfg) loadXMLConfigFile(loginMapping,
					LOGIN_CFG);
			logger.info("Login configuration loaded");
			return loginCfg;
		} catch (Exception ex) {
			logger.error("Login configuration load/parse error", ex);
			return null;
		}
	}

	public synchronized CommonCfg loadCommonConfig() {
		logger.info("Loading common configuration ...");
		try {
			CommonCfg commonCfg = (CommonCfg) loadXMLConfigFile(commonMapping,
					COMMON_CFG);
			logger.info("Checking common configuration consistency ...");
			try {
				commonCfg.checkAndInit();
				logger.info("Common configuration loaded");
				return commonCfg;
			} catch (Exception ex) {
				logger.error("Common configuration consistency error", ex);
				return null;
			}
		} catch (Exception ex) {
			logger.error("Common configuration load/parse error", ex);
			return null;
		}
	}

	public synchronized CommonCfg loadCommonConfigBackup() {
		logger.warn("Loading common configuration from backup file...");
		try {
			CommonCfg commonCfg = (CommonCfg) loadXMLConfigFile(commonMapping,
					COMMON_CFG_BCK);
			logger.info("Checking common configuration consistency ...");
			try {
				commonCfg.checkAndInit();
				logger.info("Common configuration loaded");
				return commonCfg;
			} catch (Exception ex) {
				logger.error("Common configuration consistency error", ex);
				return null;
			}
		} catch (Exception ex) {
			logger.error("Common configuration load/parse error", ex);
			return null;
		}
	}

	private boolean saveConfig(Config configToSave) {
		logger.info("Saving configuration...");
		configToSave.setNewDate();
		if (!saveXMLConfigFile(stationMapping, configToSave, STATION_CFG)) {
			logger.error("Cannot save the configuration");
			return false;
		}
		logger.info("Configuration saved");
		return true;
	}

	private boolean backupConfigFile() {
		logger.info("Backing up old configuration...");
		String cfgFileName = CFG_DIR + File.separator + STATION_CFG;
		File cfgFile = new File(cfgFileName);
		if (!cfgFile.exists()) {
			logger.warn("Old configuration does not exist: backup skipped");
			return true;
		}
		File bckCfgDir = new File(HISTORIC_CFG_DIR);
		if (!bckCfgDir.exists()) {
			logger.warn("Configuration backup directory missing");
			if (!bckCfgDir.mkdirs()) {
				logger.error("Cannot create configuration backup directory");
				return false;
			}
			logger.info("Configuration backup directory successfully created");
		}
		long lastModified = cfgFile.lastModified();
		Date bckFileDate = lastModified != 0 ? new Date(lastModified)
				: new Date();
		DateFormat historicFileDateFormat = new SimpleDateFormat(
				HISTORIC_FILE_DATE_FMT_STR);
		String bckCfgFileName = HISTORIC_CFG_DIR + File.separator
				+ HISTORIC_FILE_PREFIX
				+ historicFileDateFormat.format(bckFileDate) + ".xml";
		File bckCfgFile = new File(bckCfgFileName);
		if (bckCfgFile.exists()) {
			logger.warn("Old configuration used for less than 1 minute: "
					+ "backup skipped");
			return true;
		}
		if (!cfgFile.renameTo(bckCfgFile)) {
			logger.error("Backup failed: cannot move " + cfgFile.getName()
					+ " to " + bckCfgFile.getAbsolutePath());
			return false;
		}
		logger.info("Old configuration saved with name: "
				+ bckCfgFile.getName());
		return true;
	}

	private Object loadXMLConfigFile(Mapping mapping, String configFile)
			throws Exception {
		Reader rd = null;
		try {
			Unmarshaller unmar = new Unmarshaller(mapping);
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					CFG_DIR + File.separator + configFile), "UTF-8"));
			InputSource is = new InputSource(rd);
			return (unmar.unmarshal(is));
		} finally {
			if (rd != null) {
				try {
					rd.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private boolean saveXMLConfigFile(Mapping mapping, Object config,
			String configFile) {
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(new FileOutputStream(CFG_DIR
					+ File.separator + configFile), "UTF-8");
			Marshaller mar = new Marshaller(osw);
			mar.setMapping(mapping);
			mar.marshal(config);
			return true;
		} catch (Exception e) {
			String msg = "Error writing: " + configFile;
			logger.error(msg, e);
			return false;
		} finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public List<Date> readHistoricConfigsList() {
		return (readHistoricConfigsList(null, null, null));
	}

	public List<Date> readHistoricConfigsList(final Date startDate,
			final Date endDate, Integer limit) {

		List<Date> validConfigsList = new ArrayList<Date>();
		File historicDir = new File(HISTORIC_CFG_DIR);
		if (!historicDir.exists()) {
			logger.info("Historic config directory does not exist");
			return validConfigsList;
		}
		String[] historicFileNames = historicDir.list();
		if (historicFileNames == null)
			logger.error("Cannot read historic config directory"
					+ historicDir.getAbsolutePath());
		for (int i = 0; i < historicFileNames.length; i++) {
			Date date = parseHistoricCfgFileName(historicFileNames[i]);
			if (date == null)
				continue;
			if (startDate != null && date.before(startDate))
				continue;
			if (endDate != null && date.after(endDate))
				continue;
			validConfigsList.add(date);
		}
		if (validConfigsList.size() == 0)
			logger.info("Historic configs not available");
		Collections.sort(validConfigsList);
		if (limit != null) {
			while (validConfigsList.size() > 0
					&& validConfigsList.size() > limit)
				validConfigsList.remove(0);
		}
		Collections.reverse(validConfigsList);
		return validConfigsList;
	}

	private Date parseHistoricCfgFileName(String name) {
		if (name == null)
			return null;
		if (!name.startsWith(HISTORIC_FILE_PREFIX))
			return null;
		name = name.substring(HISTORIC_FILE_PREFIX.length());
		if (!name.endsWith(".xml"))
			return null;
		name = name.substring(0, name.length() - 4);
		try {
			DateFormat historicFileDateFormat = new SimpleDateFormat(
					HISTORIC_FILE_DATE_FMT_STR);
			Date fileNameDate = historicFileDateFormat.parse(name);
			return fileNameDate;
		} catch (ParseException e) {
		}
		return null;
	}

	public static Properties readLog4jProperties() {
		try {
			String fileName = CFG_DIR + File.separator + LOG4J_CONFIG;
			File file = new File(fileName);
			if (!file.exists() || !file.isFile() || !file.canRead())
				return null;
			Properties log4jProperties = new Properties();
			log4jProperties.load(new BufferedReader(new FileReader(file)));
			return log4jProperties;
		} catch (Exception ex) {
			return null;
		}
	}

}
