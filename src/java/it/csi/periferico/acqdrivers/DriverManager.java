/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: data port drivers management
// Change log:
//   2008-09-29: initial version
// ----------------------------------------------------------------------------
// $Id: DriverManager.java,v 1.25 2015/11/11 13:35:28 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers;

import it.csi.periferico.acqdrivers.impl.Driver;
import it.csi.periferico.acqdrivers.itf.AnalyzerFault;
import it.csi.periferico.acqdrivers.itf.AnalyzerInterface;
import it.csi.periferico.acqdrivers.itf.AnalyzerName;
import it.csi.periferico.acqdrivers.itf.AnalyzerParameter;
import it.csi.periferico.acqdrivers.itf.ConfigInfo;
import it.csi.periferico.acqdrivers.itf.CustomCommand;
import it.csi.periferico.acqdrivers.itf.DriverCallback;
import it.csi.periferico.acqdrivers.itf.DriverCfg;
import it.csi.periferico.acqdrivers.itf.DriverConfigException;
import it.csi.periferico.acqdrivers.itf.DriverInterface;
import it.csi.periferico.acqdrivers.itf.DriverService;
import it.csi.periferico.acqdrivers.itf.ElementCfg;
import it.csi.periferico.acqdrivers.itf.ElementValue;
import it.csi.periferico.acqdrivers.itf.FaultValue;
import it.csi.periferico.acqdrivers.itf.UnboundDriverException;
import it.csi.periferico.core.DataPortAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Data port drivers management
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DriverManager {

	private static Logger logger = Logger.getLogger("periferico.drivers."
			+ DriverManager.class.getSimpleName());

	private ServiceLoader<DriverService> driverServiceLoader;
	private Map<AnalyzerName, DriverCfg> mapDriverConfigs = new HashMap<AnalyzerName, DriverCfg>();
	private Map<String, List<String>> mapBrands = new HashMap<String, List<String>>();
	private Map<Object, DriverInterface> mapDrivers = new HashMap<Object, DriverInterface>();
	private List<DriverInfo> listDriverInfo = new ArrayList<DriverInfo>();
	private Map<String, Set<DriverCfg>> mapParamDrvCfg = new HashMap<String, Set<DriverCfg>>();

	public DriverManager(Set<String> parameterIdSet)
			throws DriverConfigException, DriverLoaderException {
		driverServiceLoader = loadDrivers(parameterIdSet);
		// TODO: valutare se fare in modo che i driver che falliscono il
		// caricamento non facciano terminare l'esecuzione del Periferico
	}

	private ServiceLoader<DriverService> loadDrivers(Set<String> parameterIdSet)
			throws DriverLoaderException, DriverConfigException {
		logger.info("Loading analyzers drivers...");
		ServiceLoader<DriverService> sl = ServiceLoader
				.load(DriverService.class);
		int count = 0;
		for (DriverService service : sl) {
			logger.info("Loaded driver " + service.getName() + ", version "
					+ service.getDriverVersion());
			count++;
			logger.info("Reading driver configurations...");
			service.readDriverConfigs();
			List<? extends DriverCfg> configs = service.getDriverConfigs();
			addDriverConfigs(configs, parameterIdSet);
			logger.info(configs.size() + " configurations successfully added");
			List<ConfigInfo> listInfo = service.getConfigsInfo();
			Collections.sort(listInfo, new Comparator<ConfigInfo>() {
				@Override
				public int compare(ConfigInfo o1, ConfigInfo o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			listDriverInfo.add(new DriverInfo(service.getName(), service
					.getDriverVersion(), listInfo));
		}
		Collections.sort(listDriverInfo, new Comparator<DriverInfo>() {
			@Override
			public int compare(DriverInfo o1, DriverInfo o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		logger.info(count + " analyzers drivers loaded");
		return sl;
	}

	/**
	 * @param driverCfgList
	 * @param parameterIdSet
	 * @throws DriverConfigException
	 */
	private void addDriverConfigs(List<? extends DriverCfg> driverCfgList,
			Set<String> parameterIdSet) throws DriverConfigException {
		if (driverCfgList == null)
			throw new IllegalArgumentException("Null argument not allowed");
		for (DriverCfg driverCfg : driverCfgList) {
			driverCfg.check(parameterIdSet);
			List<AnalyzerName> anList = driverCfg.getAnalyzerNameList();
			if (anList == null || anList.isEmpty()) {
				throw new DriverConfigException(
						"Invalid driver config: no analyzer name present");
			}
			for (AnalyzerName an : anList) {
				if (mapDriverConfigs.get(an) != null) {
					throw new DriverConfigException("Analyzer name " + an
							+ " is already defined into another driver"
							+ " configuration");
				}
				mapDriverConfigs.put(an, driverCfg);
				List<String> models = mapBrands.get(an.getBrand());
				if (models == null) {
					models = new ArrayList<String>();
					mapBrands.put(an.getBrand(), models);
				}
				models.add(an.getModel());
			}
			for (ElementCfg elemCfg : driverCfg.getElementCfgList()) {
				String param = elemCfg.getParameterId();
				Set<DriverCfg> setConfigs = mapParamDrvCfg.get(param);
				if (setConfigs == null) {
					setConfigs = new HashSet<DriverCfg>();
					mapParamDrvCfg.put(param, setConfigs);
				}
				setConfigs.add(driverCfg);
			}
			driverCfg.getParameterIdSet();
		}
	}

	/**
	 * @return list o driver info
	 */
	public List<DriverInfo> getDriverInfo() {
		return listDriverInfo;
	}

	/**
	 * @return list of brand
	 */
	public List<String> getBrandList() {
		List<String> brands = new ArrayList<String>(mapBrands.keySet());
		Collections.sort(brands);
		return brands;
	}

	/**
	 * @param brand
	 * @return list of model
	 */
	public List<String> getModelList(String brand) {
		List<String> models = new ArrayList<String>(mapBrands.get(brand));
		Collections.sort(models);
		return models;
	}

	/**
	 * @param brand
	 * @param model
	 * @return
	 */
	public List<String> getEquivalentBrands(String brand, String model) {
		Set<DriverCfg> drvCfgSet = getEquivalentDriverConfigs(getDriverCfg(
				brand, model));
		List<AnalyzerName> names = new ArrayList<AnalyzerName>();
		for (DriverCfg drvCfg : drvCfgSet)
			names.addAll(drvCfg.getAnalyzerNameList());
		Set<String> setBrands = new HashSet<String>();
		for (AnalyzerName an : names)
			setBrands.add(an.getBrand());
		List<String> brands = new ArrayList<String>(setBrands);
		Collections.sort(brands);
		return brands;
	}

	/**
	 * @param newBrand
	 * @param brand
	 * @param model
	 * @return
	 */
	public List<String> getEquivalentModels(String newBrand, String brand,
			String model) {
		if (newBrand == null)
			newBrand = brand;
		Set<DriverCfg> drvCfgSet = getEquivalentDriverConfigs(getDriverCfg(
				brand, model));
		List<AnalyzerName> names = new ArrayList<AnalyzerName>();
		for (DriverCfg drvCfg : drvCfgSet)
			names.addAll(drvCfg.getAnalyzerNameList());
		List<String> models = new ArrayList<String>();
		for (AnalyzerName an : names)
			if (an.getBrand().equals(newBrand))
				models.add(an.getModel());
		Collections.sort(models);
		return models;
	}

	/**
	 * @param driverCfg
	 * @return
	 */
	private Set<DriverCfg> getEquivalentDriverConfigs(DriverCfg driverCfg) {
		Set<DriverCfg> resultSet = null;
		if (driverCfg != null) {
			for (ElementCfg elemCfg : driverCfg.getElementCfgList()) {
				String param = elemCfg.getParameterId();
				Set<DriverCfg> setConfigs = mapParamDrvCfg.get(param);
				if (setConfigs == null)
					setConfigs = new HashSet<DriverCfg>();
				if (resultSet == null) {
					resultSet = new HashSet<DriverCfg>();
					resultSet.addAll(setConfigs);
				} else {
					resultSet.retainAll(setConfigs);
				}
			}
		}
		if (resultSet == null)
			resultSet = new HashSet<DriverCfg>();
		resultSet.add(driverCfg);
		return resultSet;
	}

	/**
	 * @param brand
	 * @param model
	 * @return true if has network port only , false otherwise
	 */
	public boolean hasNetworkPortOnly(String brand, String model) {
		DriverCfg driverCfg = getDriverCfg(brand, model);
		return driverCfg.isNetworkInterfaceOnly();
	}

	/**
	 * @param brand
	 * @param model
	 * @return default port ip
	 */
	public Integer getDefaultIpPort(String brand, String model) {
		DriverCfg driverCfg = getDriverCfg(brand, model);
		return driverCfg.getDefaultIpPort();
	}

	/**
	 * @param brand
	 * @param model
	 * @return default tty baudrate
	 */
	public Integer getDefaultTtyBaudRate(String brand, String model) {
		DriverCfg driverCfg = getDriverCfg(brand, model);
		return driverCfg.getDefaultTtyBaudRate();
	}

	public List<Integer> getSupportedTtyBaudRates(String brand, String model) {
		DriverCfg driverCfg = getDriverCfg(brand, model);
		return driverCfg.getSupportedTtyBaudRates();
	}

	public String getDefaultTtyParams(String brand, String model) {
		DriverCfg driverCfg = getDriverCfg(brand, model);
		return driverCfg.getDefaultTtyParams();
	}

	public String getDefaultDriverParams(String brand, String model) {
		DriverCfg driverCfg = getDriverCfg(brand, model);
		return driverCfg.getDefaultDriverParams();
	}

	private DriverCfg getDriverCfg(String brand, String model) {
		DriverCfg driverCfg = mapDriverConfigs.get(new AnalyzerName(brand,
				model));
		if (driverCfg == null)
			throw new IllegalStateException("Driver configuration not found "
					+ "for: " + brand + " - " + model);
		return driverCfg;
	}

	public DataPortAnalyzer makeNewAnalyzer(String brand, String model) {
		DriverCfg driverCfg = getDriverCfg(brand, model);
		DataPortAnalyzer analyzer = new DataPortAnalyzer();
		analyzer.setBrand(brand);
		analyzer.setModel(model);
		for (ElementCfg cfg : driverCfg.getElementCfgList())
			analyzer.addElement(cfg.getParameterId(),
					cfg.getAggregationPeriod_m(), cfg.isDataValidSupported());
		return analyzer;
	}

	public boolean hasDriverConfig(AnalyzerInterface ai) {
		return mapDriverConfigs.containsKey(new AnalyzerName(ai.getBrand(), ai
				.getModel()));
	}

	public boolean isParameterDefined(String brand, String model, String paramId) {
		try {
			DriverCfg driverCfg = getDriverCfg(brand, model);
			return driverCfg.getParameterIdSet().contains(paramId);
		} catch (Exception ex) {
			return false;
		}
	}

	public Boolean isDataValidSupported(String brand, String model,
			String paramId) {
		DriverCfg drvCfg = mapDriverConfigs.get(new AnalyzerName(brand, model));
		if (drvCfg == null)
			return null;
		ElementCfg elemCfg = drvCfg.getElementCfg(paramId);
		if (elemCfg == null)
			return null;
		return elemCfg.isDataValidSupported();
	}

	public void initDriver(AnalyzerInterface ai) throws DriverConfigException {
		for (DriverService service : driverServiceLoader) {
			if (service.isSupported(ai)) {
				DriverInterface drv = service.newDriverInstance(ai);
				mapDrivers.put(ai.getId(), drv);
				return;
			}
		}
		throw new DriverConfigException("No driver found for analyzer "
				+ ai.getBrand() + " - " + ai.getModel());
	}

	public void startAcquisition() {
		for (DriverInterface driver : mapDrivers.values()) {
			driver.start();
			driver.enableAcquisition();
		}
	}

	public void stopAcquisition() {
		for (DriverInterface driver : mapDrivers.values()) {
			driver.disableAcquisition();
			driver.stop();
		}
	}

	public void destroyDrivers() {
		stopAcquisition();
		mapDrivers.clear();
		Driver.resetThreadCount(); // TODO: valutare soluzioni migliori
	}

	public int getStoppedOnFatalErrorThreadsNumber() {
		int count = 0;
		for (DriverInterface driver : mapDrivers.values())
			if (driver.isFailed())
				count++;
		return count;
	}

	// ////////////////

	private DriverInterface getDriver(Object analyzerId)
			throws UnboundDriverException {
		DriverInterface driver = mapDrivers.get(analyzerId);
		if (driver == null)
			throw new UnboundDriverException("Analyzer with id '" + analyzerId
					+ "' is not bound to any driver");
		return driver;
	}

	/**
	 * @param ai
	 * @return
	 * @throws UnboundDriverException
	 */
	public boolean isRunning(AnalyzerInterface ai)
			throws UnboundDriverException {
		return getDriver(ai.getId()).isRunning();
	}

	/**
	 * @param ai
	 * @return
	 * @throws UnboundDriverException
	 */
	public boolean isFailed(AnalyzerInterface ai) throws UnboundDriverException {
		return getDriver(ai.getId()).isFailed();
	}

	/**
	 * @param ai
	 * @return
	 * @throws UnboundDriverException
	 */
	public boolean isConnectionOK(AnalyzerInterface ai)
			throws UnboundDriverException {
		return getDriver(ai.getId()).isConnectionOK();
	}

	/**
	 * @param ai
	 * @param callback
	 * @throws UnboundDriverException
	 */
	public void testCommunication(AnalyzerInterface ai,
			DriverCallback<Boolean> callback) throws UnboundDriverException {
		getDriver(ai.getId()).testCommunication(callback);
	}

	public void getDate(AnalyzerInterface ai, DriverCallback<Date> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).getDate(callback);
	}

	public void setDate(AnalyzerInterface ai, DriverCallback<Date> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).setDate(callback);
	}

	public void getSerialNumber(AnalyzerInterface ai,
			DriverCallback<String> callback) throws UnboundDriverException {
		getDriver(ai.getId()).getSerialNumber(callback);
	}

	public void getParameters(AnalyzerInterface ai,
			DriverCallback<List<AnalyzerParameter>> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).getParameters(callback);
	}

	public void showMeasuredParameters(AnalyzerInterface ai,
			DriverCallback<List<String>> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).showMeasuredParameters(callback);
	}

	public void showProcessParameters(AnalyzerInterface ai,
			DriverCallback<List<String>> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).showProcessParameters(callback);
	}

	public void readParameter(AnalyzerInterface ai, String id,
			DriverCallback<ElementValue> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).readParameter(id, callback);
	}

	public void getFaults(AnalyzerInterface ai,
			DriverCallback<List<AnalyzerFault>> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).getFaults(callback);
	}

	public void showFaults(AnalyzerInterface ai,
			DriverCallback<List<String>> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).showFaults(callback);
	}

	public void isFaultActive(AnalyzerInterface ai, String id,
			DriverCallback<Boolean> callback) throws UnboundDriverException {
		getDriver(ai.getId()).isFaultActive(id, callback);
	}

	public void resetFault(AnalyzerInterface ai, String id,
			DriverCallback<Boolean> callback) throws UnboundDriverException {
		getDriver(ai.getId()).resetFault(id, callback);
	}

	public void readFaultStatus(AnalyzerInterface ai,
			DriverCallback<FaultValue> callback) throws UnboundDriverException {
		getDriver(ai.getId()).readFaultStatus(callback);
	}

	public void resetAllFaults(AnalyzerInterface ai,
			DriverCallback<FaultValue> callback) throws UnboundDriverException {
		getDriver(ai.getId()).resetAllFaults(callback);
	}

	public boolean isRemoteUISupported(AnalyzerInterface ai)
			throws UnboundDriverException {
		return getDriver(ai.getId()).isRemoteUISupported();
	}

	public String isRemoteGUISupported(AnalyzerInterface ai)
			throws UnboundDriverException {
		return getDriver(ai.getId()).isRemoteGUISupported();
	}

	public List<String> getKeyList(AnalyzerInterface ai)
			throws UnboundDriverException {
		return getDriver(ai.getId()).getKeyList();
	}

	public void sendKey(AnalyzerInterface ai, String key,
			DriverCallback<List<String>> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).sendKey(key, callback);
	}

	public void readDisplay(AnalyzerInterface ai,
			DriverCallback<List<String>> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).readDisplay(callback);
	}

	public void readDisplayImage(AnalyzerInterface ai,
			DriverCallback<byte[]> callback) throws UnboundDriverException {
		getDriver(ai.getId()).readDisplayImage(callback);
	}

	public boolean isCustomCommandSupported(AnalyzerInterface ai)
			throws UnboundDriverException {
		return getDriver(ai.getId()).isCustomCommandSupported();
	}

	public List<CustomCommand> getCommandList(AnalyzerInterface ai)
			throws UnboundDriverException {
		return getDriver(ai.getId()).getCommandList();
	}

	public void sendCommand(AnalyzerInterface ai, String[] command,
			DriverCallback<List<String>> callback)
			throws UnboundDriverException {
		getDriver(ai.getId()).sendCommand(command, callback);
	}

}
