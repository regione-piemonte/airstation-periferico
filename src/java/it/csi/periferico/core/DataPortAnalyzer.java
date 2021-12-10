/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: analyzer with data port interface
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: DataPortAnalyzer.java,v 1.24 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.acqdrivers.DriverManager;
import it.csi.periferico.acqdrivers.itf.AlarmInterface;
import it.csi.periferico.acqdrivers.itf.AnalyzerInterface;
import it.csi.periferico.acqdrivers.itf.ElementInterface;
import it.csi.periferico.acqdrivers.itf.PortType;
import it.csi.periferico.acqdrivers.itf.UnboundDriverException;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzer with data port interface
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DataPortAnalyzer extends Analyzer implements AnalyzerInterface,
		DataPortElementHolder, ObservableAnalyzer {

	private static final long serialVersionUID = -5047960663211365784L;

	private PortType portType = PortType.NETWORK;

	private String hostName = "";

	private Integer ipPort = null;

	private String ttyDevice = "";

	private Integer ttyBaudRate = null;

	private String ttyParams = "";

	private String driverParams = "";

	private List<DataPortElement> listElements = new ArrayList<DataPortElement>();

	private DataPortAlarm fault = new DataPortAlarm(EVT_FAULT);

	private CalibAutoCheck calibrationCheck;

	public PortType getPortType() {
		return portType;
	}

	public void setPortType(PortType portType) {
		this.portType = portType;
	}

	public String getPortTypeAsString() {
		return portType.toString().toLowerCase();
	}

	public void setPortTypeAsString(String strPortType) {
		this.portType = PortType.valueOf(trim(strPortType).toUpperCase());
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = trim(hostName);
	}

	public Integer getIpPort() {
		return ipPort;
	}

	public void setIpPort(Integer ipPort) {
		this.ipPort = ipPort;
	}

	public Integer getTtyBaudRate() {
		return ttyBaudRate;
	}

	public void setTtyBaudRate(Integer ttyBaudRate) {
		this.ttyBaudRate = ttyBaudRate;
	}

	public String getTtyDevice() {
		return ttyDevice;
	}

	public void setTtyDevice(String ttyDevice) {
		this.ttyDevice = trim(ttyDevice);
	}

	public String getTtyParams() {
		return ttyParams;
	}

	public void setTtyParams(String ttyParams) {
		this.ttyParams = trim(ttyParams);
	}

	public String getDriverParams() {
		return driverParams;
	}

	public void setDriverParams(String driverParams) {
		this.driverParams = driverParams;
	}

	@Override
	public String getPassword() {
		return null;
	}

	public void setConfig(String name, String brand, String model,
			String description, String serialNumber, String userNotes,
			String strStatus, String uiURL, PortType portType, String hostName,
			int ipPort, String ttyDevice, int ttyBaudRate, String ttyParams,
			String driverParams) throws ConfigException {
		super.setConfig(name, brand, description, model, serialNumber,
				userNotes, strStatus, uiURL);
		setPortType(portType);
		setHostName(hostName);
		setIpPort(ipPort);
		setTtyDevice(ttyDevice);
		setTtyBaudRate(ttyBaudRate);
		setTtyParams(ttyParams);
		setDriverParams(driverParams);
	}

	public boolean isSameConfig(String name, String brand, String model,
			String description, String serialNumber, String userNotes,
			String strStatus, String uiURL, PortType portType, String hostName,
			int ipPort, String ttyDevice, int ttyBaudRate, String ttyParams,
			String driverParams) {
		return super.isSameConfig(name, brand, description, model,
				serialNumber, userNotes, strStatus, uiURL)
				&& this.portType == portType
				&& this.hostName.equals(trim(hostName))
				&& this.ipPort == ipPort
				&& this.ttyDevice.equals(trim(ttyDevice))
				&& this.ttyBaudRate == ttyBaudRate
				&& this.ttyParams.equals(trim(ttyParams))
				&& this.driverParams.equals(trim(driverParams));
	}

	public List<DataPortElement> getListElements() {
		return listElements;
	}

	public void setListElements(List<DataPortElement> listElements) {
		this.listElements = listElements;
		if (listElements != null)
			for (DataPortElement de : listElements)
				de.setElementHolder(this);

	}

	public DataPortElement getElement(String paramId) {
		paramId = trim(paramId);
		for (DataPortElement element : listElements) {
			if (paramId.equals(element.getParameterId()))
				return element;
		}
		return null;
	}

	public void addElement(String paramId, Integer aggregationPeriod_m,
			boolean dataValidSupported) {
		DataPortElement element = newElement(aggregationPeriod_m);
		element.setParameterId(paramId);
		element.setElementHolder(this);
		element.setDiscardDataNotValidForAnalyzer(dataValidSupported ? false
				: null);
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		Parameter param = cc.getParameter(paramId);
		if (param == null)
			throw new IllegalStateException("Parameter not found for id "
					+ paramId);
		List<String> analyzerMUs = cc.getAnalyzerMeasureUnitNames(param
				.getPhysicalDimension());
		if (analyzerMUs.isEmpty())
			throw new IllegalStateException("No analyzer measure units "
					+ "defined for parameter " + paramId);
		List<String> acquisitionMUs = cc.getMeasureUnitNames(param
				.getPhysicalDimension());
		if (acquisitionMUs.isEmpty())
			throw new IllegalStateException(
					"No acquisition measure units defined for parameter "
							+ paramId);
		element.setAnalyzerMeasureUnitName(analyzerMUs.get(0));
		element.setMeasureUnitName(acquisitionMUs.get(0));
		listElements.add(element);
	}

	private DataPortElement newElement(Integer aggregationPeriod_m) {
		if (aggregationPeriod_m == null || aggregationPeriod_m <= 0) {
			return new DataPortElement();
		} else {
			DataPortAvgElement elem = new DataPortAvgElement();
			elem.setAvgPeriod(aggregationPeriod_m);
			elem.setAcqDelay(aggregationPeriod_m == 1440 ? 360
					: aggregationPeriod_m / 3);
			elem.setAcqDuration(10);
			elem.setAcqPeriod(60);
			return elem;
		}
	}

	public DataPortAlarm getFault() {
		return fault;
	}

	public void setFault(DataPortAlarm fault) {
		this.fault = fault;
		if (fault != null) {
			fault.setHolder(this);
			fault.setStatusCollector(getFaultStatusCollector());
		}
	}

	public CalibAutoCheck getCalibrationCheck() {
		return calibrationCheck;
	}

	public void setCalibrationCheck(CalibAutoCheck calibrationCheck) {
		this.calibrationCheck = calibrationCheck;
	}

	public CalibAutoCheck makeNewCalibrationCheck() {
		return new CalibAutoCheck();
	}

	@Override
	public void checkConfig() throws ConfigException {
		if (getStatus() == Analyzer.Status.DELETED)
			return;
		super.checkConfig();
		if (getPortType() == PortType.NETWORK
				&& (hostName == null || hostName.trim().isEmpty()))
			throw new ConfigException("hostname_required_for_net_conn");
		if (getPortType() == PortType.SERIAL
				&& (ttyDevice == null || ttyDevice.trim().isEmpty()))
			throw new ConfigException("serial_dev_required_for_serial_conn");
		DriverManager dm = Periferico.getInstance().getDriverManager();
		if (!dm.hasDriverConfig(this))
			throw new ConfigException("drv_cfg_not_found_for", getBrand(),
					getModel());
		for (DataPortElement ae : listElements)
			ae.checkConfig();
		if (fault != null)
			fault.checkConfig();
		if (calibrationCheck != null)
			calibrationCheck.checkConfig();
	}

	@Override
	public void initConfig() {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		super.initConfig();
		if (listElements != null) {
			for (DataPortElement se : listElements) {
				se.setElementHolder(this);
				se.setAnalyzerId(getId());
				Boolean dataValidSupported = dm.isDataValidSupported(
						getBrand(), getModel(), se.getParameterId());
				if (dataValidSupported == null)
					continue;
				if (dataValidSupported) {
					if (se.getDiscardDataNotValidForAnalyzer() == null)
						se.setDiscardDataNotValidForAnalyzer(false);
				} else {
					se.setDiscardDataNotValidForAnalyzer(null);
				}
			}
		}
	}

	@Override
	public Element[] getElements() {
		if (listElements == null)
			return new Element[0];
		return listElements.toArray(new Element[listElements.size()]);
	}

	@Override
	void onDelete() {
	}

	public AlarmInterface getAlarmInterface() {
		return getFault();
	}

	public boolean isActive() {
		return isEnabled();
	}

	public boolean isConnectionUp() {
		DriverManager dm = Periferico.getInstance().getDriverManager();
		if (dm == null)
			return false;
		try {
			return dm.isRunning(this) && dm.isConnectionOK(this);
		} catch (UnboundDriverException e) {
			return false;
		}
	}

	public List<ElementInterface> getElementInterfaces() {
		return new ArrayList<ElementInterface>(listElements);
	}

	public boolean isDataValidWarningOnly() {
		return true;
	}

	@Override
	public Boolean getFaultActive() {
		BinaryStatus bs = getFaultStatus();
		if (bs instanceof IntegerStatus) {
			IntegerStatus is = (IntegerStatus) bs;
			return is.getValue() == null ? null : is.getStatus();
		} else {
			return isFaultActive();
		}
	}

}
