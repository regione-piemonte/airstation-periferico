/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: default partial implementation for interface for
// analyzers' drivers
// Change log:
//   2015-04-02: initial version
// ----------------------------------------------------------------------------
// $Id: DefaultDriverService.java,v 1.4 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.impl;

import it.csi.periferico.acqdrivers.itf.AnalyzerInterface;
import it.csi.periferico.acqdrivers.itf.AnalyzerName;
import it.csi.periferico.acqdrivers.itf.ConfigInfo;
import it.csi.periferico.acqdrivers.itf.DriverCfg;
import it.csi.periferico.acqdrivers.itf.DriverConfigException;
import it.csi.periferico.acqdrivers.itf.DriverService;
import it.csi.periferico.acqdrivers.itf.DriverVersion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 * Default partial implementation for interface for analyzers' drivers. La
 * classe DefaultDriverService implementa l’interfaccia DriverService e
 * comprende il codice necessario per leggere dei file di configurazione per il
 * driver. Questa implementazione di riferimento prevede che si usino dei file
 * di configurazione per gestire eventuali differenze tra modelli di
 * analizzatore che hanno lo stesso protocollo, ma che richiedono comandi
 * specifici per la lettura delle grandezze misurate o che forniscono risposte
 * con numeri di campi differenti.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class DefaultDriverService<T extends DriverCfg> implements
		DriverService {

	protected static final String CFG_BASEDIR = "config" + File.separator
			+ "drivers";
	private static final String DEFAULT_DRIVER_MAPPING = "driver_mapping.xml";
	private static Logger logger = Logger.getLogger("periferico.drivers."
			+ DefaultDriverService.class.getSimpleName());
	private String name;
	private DriverVersion version;
	private String configMappingName;
	private String configDir;
	private Map<String, String> mapConfigChecksum;
	private List<T> listConfigs;
	private List<ConfigInfo> listConfigInfo;

	public DefaultDriverService(String name, DriverVersion version,
			String configMappingName, String configDir,
			Map<String, String> mapConfigChecksum) {
		this.name = name;
		this.version = version;
		this.configMappingName = configMappingName;
		this.configDir = configDir;
		this.mapConfigChecksum = mapConfigChecksum;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DriverVersion getDriverVersion() {
		return version;
	}

	@Override
	public void readDriverConfigs() throws DriverConfigException {
		Mapping driverMapping = loadDriverMapping(configMappingName);
		listConfigs = loadDriverConfigs(driverMapping, CFG_BASEDIR
				+ File.separator + configDir);
	}

	@Override
	public List<T> getDriverConfigs() {
		return listConfigs;
	}

	@Override
	public List<ConfigInfo> getConfigsInfo() {
		return listConfigInfo;
	}

	protected void setConfigsInfo(List<ConfigInfo> listConfigInfo) {
		this.listConfigInfo = listConfigInfo;
	}

	@Override
	public boolean isSupported(AnalyzerInterface analyzerInterface) {
		return getConfig(analyzerInterface) != null;
	}

	protected T getConfig(AnalyzerInterface ai) {
		if (listConfigs == null)
			throw new IllegalStateException(
					"Driver configurations not loaded yet");
		AnalyzerName an = new AnalyzerName(ai.getBrand(), ai.getModel());
		for (T cfg : listConfigs)
			if (cfg.getAnalyzerNameList().contains(an))
				return cfg;
		return null;
	}

	protected Mapping loadDriverMapping(String name)
			throws DriverConfigException {
		logger.debug("Loading XML mapping file " + name + " for " + getName()
				+ " driver configuration...");
		try {
			Mapping driverMapping = new Mapping();
			URL baseResource = DefaultDriverService.class.getClassLoader()
					.getResource(DEFAULT_DRIVER_MAPPING);
			URL resource = getClass().getClassLoader().getResource(name);
			driverMapping.loadMapping(baseResource);
			driverMapping.loadMapping(resource);
			logger.trace("Mapping file loaded");
			return driverMapping;
		} catch (Exception ex) {
			throw new DriverConfigException("Error loading XML mapping file "
					+ name + " for " + getName() + " driver", ex);
		}
	}

	protected List<T> loadDriverConfigs(Mapping driverMapping, String configDir)
			throws DriverConfigException {
		logger.debug("Loading " + getName() + " driver configurations ...");
		Set<String> setExpectedConfigNames = new HashSet<String>(
				mapConfigChecksum.keySet());
		List<T> driverConfigs = new ArrayList<T>();
		File driversDir = new File(configDir);
		if (!driversDir.isDirectory())
			throw new DriverConfigException("No configurations found for "
					+ getName() + " driver");
		FilenameFilter xmlFileFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
		File[] driverCfgFiles = driversDir.listFiles(xmlFileFilter);
		if (driverCfgFiles == null)
			throw new DriverConfigException("Error reading " + getName()
					+ " driver configurations folder");
		List<File> driverCfgFilesList = new ArrayList<File>();
		for (int i = 0; i < driverCfgFiles.length; i++) {
			if (driverCfgFiles[i].isFile())
				driverCfgFilesList.add(driverCfgFiles[i]);
		}
		if (driverCfgFilesList.isEmpty() && !mapConfigChecksum.isEmpty())
			throw new DriverConfigException("No driver configurations found "
					+ " for " + getName() + " driver");
		logger.debug("Found " + driverCfgFilesList.size()
				+ " configuration(s) for " + getName() + " driver");
		List<ConfigInfo> listCfgInfo = new ArrayList<ConfigInfo>();
		for (File driverCfgFile : driverCfgFilesList) {
			FileInputStream inputStream = null;
			try {
				String fileName = driverCfgFile.getName();
				setExpectedConfigNames.remove(fileName);
				inputStream = new FileInputStream(driverCfgFile);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int n = 0;
				while (-1 != (n = inputStream.read(buffer)))
					baos.write(buffer, 0, n);
				byte[] cfgContent = baos.toByteArray();
				MessageDigest md = MessageDigest.getInstance("MD5");
				String checksum = DatatypeConverter.printHexBinary(md
						.digest(cfgContent));
				String expectedChecksum = mapConfigChecksum.get(fileName);
				Unmarshaller unmar = new Unmarshaller();
				unmar.setClassLoader(getClass().getClassLoader());
				unmar.setMapping(driverMapping);
				InputSource is = new InputSource(new ByteArrayInputStream(
						cfgContent));
				@SuppressWarnings("unchecked")
				T driverCfg = (T) unmar.unmarshal(is);
				driverConfigs.add(driverCfg);
				ConfigInfo cfgInfo = new ConfigInfo(fileName,
						expectedChecksum == null, driverCfg.getVersion(),
						checksum, expectedChecksum == null ? null
								: checksum.equalsIgnoreCase(expectedChecksum));
				logger.info("Loaded " + cfgInfo.getName() + ", version "
						+ cfgInfo.getVersion() + ", checksum "
						+ cfgInfo.getChecksum());
				if (cfgInfo.isNewConfig())
					logger.warn("Configuration " + cfgInfo.getName()
							+ " is new");
				if (Boolean.FALSE.equals(cfgInfo.isChecksumValid()))
					logger.warn("Configuration " + cfgInfo.getName()
							+ " is changed");
				listCfgInfo.add(cfgInfo);
			} catch (Exception e) {
				String msg = "Error loading " + getName()
						+ " driver configuration: " + driverCfgFile;
				logger.error(msg, e);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}
		}
		for (String name : setExpectedConfigNames) {
			logger.warn("Expected configuration " + name + " is missing");
			listCfgInfo.add(new ConfigInfo(name, false, null, null, null));
		}
		if (driverConfigs.size() == driverCfgFilesList.size()) {
			logger.debug("All " + getName() + " driver configurations"
					+ " successfully loaded");
			listConfigInfo = listCfgInfo;
			return driverConfigs;
		}
		if (driverConfigs.size() > 0)
			throw new DriverConfigException("Only " + driverConfigs.size()
					+ " of " + driverCfgFilesList.size() + " configurations "
					+ "successfully loaded for " + getName() + " driver");
		else
			throw new DriverConfigException("None of "
					+ driverCfgFilesList.size() + " configurations "
					+ "successfully loaded for " + getName() + " driver");
	}

}
