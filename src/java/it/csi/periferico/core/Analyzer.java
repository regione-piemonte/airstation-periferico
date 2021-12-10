/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for all analyzers
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: Analyzer.java,v 1.35 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import it.csi.periferico.Periferico;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

/**
 * Base class for all analyzers
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public abstract class Analyzer extends ConfigItem {

	private static final long serialVersionUID = -4254661987372469143L;

	public static final String EVT_FAULT = "fault";

	public static final String EVT_DATA_VALID = "data_valid";

	public static final String EVT_MAINTENANCE = "maintenance";

	public static final String EVT_MAN_CALIB = "man_calib";

	public static final String EVT_ACHK_RUN = "achk_run";

	public static final String EVT_ACHK_FAIL = "achk_fail";

	public static final String[] ARRAY_EVT = { EVT_FAULT, EVT_DATA_VALID,
			EVT_MAINTENANCE, EVT_MAN_CALIB, EVT_ACHK_RUN, EVT_ACHK_FAIL };

	private static final int MIN_NAME_LEN = 2;

	private static final int MAX_NAME_LEN = 16;

	private static Logger logger = Logger.getLogger("periferico."
			+ Analyzer.class.getSimpleName());

	public enum Status {
		ENABLED, DISABLED, OUT_OF_ORDER, REMOVED, DELETED
	};

	public enum Type {
		DPA("Analizzatore con interfaccia dati"), AVG(
				"Analizzatore che calcola medie"), RAIN("Pluviometro a bascula"), SAMPLE(
				"Analizzatore sample generico"), WIND("Anemometro generico");

		private final String name;

		Type(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	};

	private static final Type[] types = { Type.DPA, Type.AVG, Type.RAIN,
			Type.SAMPLE, Type.WIND };

	private UUID id = UUID.randomUUID();

	private String name = "";

	private String brand = "";

	private String model = "";

	private String description = "";

	private String serialNumber = "";

	private String userNotes = "";

	private Status status = Status.DISABLED;

	private Date deletionDate = null;
	
	private String uiURL = "";

	private boolean initialized = false;

	private transient BinaryStatusCollector faultStatus = new BinaryStatusCollector();

	private transient BinaryStatusCollector dataValidStatus = new BinaryStatusCollector();

	private transient BinaryStatusCollector maintenanceInProgress = new BinaryStatusCollector();

	private transient BinaryStatusCollector manualCalibrationRunning = new BinaryStatusCollector();

	private transient BinaryStatusCollector autoCheckRunning = new BinaryStatusCollector();

	private transient BinaryStatusCollector autoCheckFailed = new BinaryStatusCollector();

	private transient ContainerMonitor containerMonitor = null;
	
	private transient String uiProxyPath = null;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getIdAsString() {
		if (id == null)
			return "";
		return id.toString();
	}

	public void setIdAsString(String strId) {
		this.id = UUID.fromString(trim(strId));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) throws ConfigException {
		this.name = checkLength("name", name, MIN_NAME_LEN, MAX_NAME_LEN);
	}

	public static Analyzer newAnalyzer(Type type) {
		if (type == Type.DPA)
			return new DataPortAnalyzer();
		if (type == Type.AVG)
			return new AvgAnalyzer();
		if (type == Type.RAIN)
			return new RainAnalyzer();
		if (type == Type.SAMPLE)
			return new SampleAnalyzer();
		if (type == Type.WIND)
			return new WindAnalyzer();
		else
			throw new IllegalArgumentException("Unknown analyzer type");
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = trim(brand);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = trim(description);
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = trim(model);
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = trim(serialNumber);
	}

	public String getUserNotes() {
		return userNotes;
	}

	public void setUserNotes(String userNotes) {
		this.userNotes = trimcrlf(userNotes);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (status == Status.DELETED && deletionDate == null) {
			deletionDate = new Date();
			onDelete();
		}
		this.status = status;
	}

	public boolean isEnabled() {
		return status == Status.ENABLED;
	}

	public String getStatusAsString() {
		return status.toString().toLowerCase();
	}

	public void setStatusAsString(String strStatus) {
		setStatus(Status.valueOf(trim(strStatus).toUpperCase()));
	}

	public Date getDeletionDate() {
		return deletionDate;
	}

	public void setDeletionDate(Date deletionDate) {
		this.deletionDate = deletionDate;
	}

	public String getUiURL() {
		return uiURL;
	}

	public void setUiURL(String uiURL) {
		this.uiURL = trim(uiURL);
	}

	protected void setConfig(String name, String brand, String description,
			String model, String serialNumber, String userNotes,
			String strStatus, String uiURL) throws ConfigException {
		setName(name);
		setBrand(brand);
		setDescription(description);
		setModel(model);
		setSerialNumber(serialNumber);
		setUserNotes(userNotes);
		setStatusAsString(strStatus);
		setUiURL(uiURL);
	}

	protected boolean isSameConfig(String name, String brand,
			String description, String model, String serialNumber,
			String userNotes, String strStatus, String uiURL) {
		return this.name.equals(trim(name)) && this.brand.equals(trim(brand))
				&& this.description.equals(trim(description))
				&& this.model.equals(trim(model))
				&& this.serialNumber.equals(trim(serialNumber))
				&& this.userNotes.equals(trimcrlf(userNotes))
				&& getStatusAsString().equals(trim(strStatus).toLowerCase())
				&& this.uiURL.equals(trim(uiURL));
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkLength("name", name, MIN_NAME_LEN, MAX_NAME_LEN);
		if (uiURL!=null && !uiURL.isEmpty()) {
			try {
				new URL(uiURL);
			} catch (MalformedURLException e) {
				throw new ConfigException("Malformed URL for analyzer web UI", e);
			}
		}
	}

	public Type getType() {
		if (this instanceof DataPortAnalyzer)
			return Type.DPA;
		if (this instanceof AvgAnalyzer)
			return Type.AVG;
		if (this instanceof RainAnalyzer)
			return Type.RAIN;
		if (this instanceof SampleAnalyzer)
			return Type.SAMPLE;
		if (this instanceof WindAnalyzer)
			return Type.WIND;
		throw new IllegalStateException("Unknown analyzer type");
	}

	public static Type[] getTypes() {
		return types;
	}

	public BinaryStatus getAutoCheckFailed() {
		return autoCheckFailed.getLast();
	}

	BinaryStatusCollector getAutoCheckFailedCollector() {
		return autoCheckFailed;
	}

	public BinaryStatus getAutoCheckRunning() {
		return autoCheckRunning.getLast();
	}

	public boolean isAutoCheckRunning() {
		if (autoCheckRunning.getLast() == null)
			return false;
		return autoCheckRunning.getLast().getStatus();
	}

	public void updateAutoCheckRunning(boolean value) {
		BinaryStatus tmp = autoCheckRunning.getLast();
		if (tmp != null && tmp.getStatus() == value)
			return;
		autoCheckRunning.add(new BinaryStatus(value));
	}

	BinaryStatusCollector getAutoCheckRunningCollector() {
		return autoCheckRunning;
	}

	public String getUiProxyPath() {
		return uiProxyPath;
	}

	public void setUiProxyPath(String uiProxyPath) {
		this.uiProxyPath = uiProxyPath;
	}

	public BinaryStatus getFaultStatus() {
		return faultStatus.getLast();
	}

	public boolean isFaultActive() {
		if (faultStatus.getLast() == null)
			return false;
		return faultStatus.getLast().getStatus();
	}

	BinaryStatusCollector getFaultStatusCollector() {
		return faultStatus;
	}

	public BinaryStatus getDataValidStatus() {
		return dataValidStatus.getLast();
	}

	public boolean isDataValidActive() {
		if (dataValidStatus.getLast() == null)
			return true;
		return dataValidStatus.getLast().getStatus();
	}

	BinaryStatusCollector getDataValidStatusCollector() {
		return dataValidStatus;
	}

	public BinaryStatus getMaintenanceInProgress() {
		return maintenanceInProgress.getLast();
	}

	public boolean isMaintenanceInProgress() {
		if (maintenanceInProgress.getLast() == null)
			return false;
		return maintenanceInProgress.getLast().getStatus();
	}

	public boolean startMaintenance() {
		if (status != Status.ENABLED)
			return false;
		setMaintenanceInProgress(true);
		return true;
	}

	public void stopMaintenance() {
		if (status == Status.ENABLED)
			setMaintenanceInProgress(false);
	}

	private void setMaintenanceInProgress(boolean value) {
		maintenanceInProgress.add(new BinaryStatus(value));
	}

	BinaryStatusCollector getMaintenanceInProgressCollector() {
		return maintenanceInProgress;
	}

	public BinaryStatus getManualCalibrationRunning() {
		return manualCalibrationRunning.getLast();
	}

	public boolean isManualCalibrationRunning() {
		if (manualCalibrationRunning.getLast() == null)
			return false;
		return manualCalibrationRunning.getLast().getStatus();
	}

	public boolean startManualCalibration() {
		if (status != Status.ENABLED)
			return false;
		setManualCalibrationRunning(true);
		return true;
	}

	public void stopManualCalibration() {
		if (status == Status.ENABLED)
			setManualCalibrationRunning(false);
	}

	private void setManualCalibrationRunning(boolean value) {
		manualCalibrationRunning.add(new BinaryStatus(value));
	}

	BinaryStatusCollector getManualCalibrationRunningCollector() {
		return manualCalibrationRunning;
	}

	BinaryStatusCollector getCollector(String name) {
		if (name == null)
			return null;
		if (name.equals(EVT_FAULT))
			return getFaultStatusCollector();
		if (name.equals(EVT_DATA_VALID))
			return getDataValidStatusCollector();
		if (name.equals(EVT_MAINTENANCE))
			return getMaintenanceInProgressCollector();
		if (name.equals(EVT_MAN_CALIB))
			return getManualCalibrationRunningCollector();
		if (name.equals(EVT_ACHK_RUN))
			return getAutoCheckRunningCollector();
		if (name.equals(EVT_ACHK_FAIL))
			return getAutoCheckFailedCollector();
		return null;
	}

	void setContainerMonitor(ContainerMonitor containerMonitor) {
		this.containerMonitor = containerMonitor;
	}

	public boolean isEnvironmentOK() {
		if (containerMonitor == null)
			return true;
		return containerMonitor.isContainerEnvironmentOK();
	}

	void readAndSetLastManualOperations() {
		if (status != Status.ENABLED)
			return;
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		Calendar calendar = new GregorianCalendar();
		Date endDate = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH,
				-storageManager.getMaxPossibleDaysOfEventData());
		Date startDate = calendar.getTime();
		try {
			List<BinaryStatus> data = storageManager.readEventData(id,
					EVT_MAINTENANCE, startDate, true, endDate, true, null);
			if (!data.isEmpty()) {
				BinaryStatus bs = data.get(data.size() - 1);
				maintenanceInProgress.setLast(bs);
				logger.debug("Last maintenance status for analyzer " + name
						+ " is: " + bs.getStatus() + ", " + bs.getTimestamp());
			}
		} catch (StorageException e) {
			logger.error("Error reading last maintenance status for analyzer "
					+ name);
		}
		try {
			List<BinaryStatus> data = storageManager.readEventData(id,
					EVT_MAN_CALIB, startDate, true, endDate, true, null);
			if (!data.isEmpty()) {
				BinaryStatus bs = data.get(data.size() - 1);
				manualCalibrationRunning.setLast(bs);
				logger.debug("Last manual calibration running status for "
						+ "analyzer " + name + " is: " + bs.getStatus() + ", "
						+ bs.getTimestamp());
			}
		} catch (StorageException e) {
			logger.error("Error reading last manual calibration running "
					+ "status for analyzer " + name);
		}
		if (maintenanceInProgress.getLast() == null)
			setMaintenanceInProgress(false);
		if (manualCalibrationRunning.getLast() == null)
			setManualCalibrationRunning(false);
	}

	@Override
	public void initConfig() {
		initialized = true;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public abstract Element[] getElements();

	public abstract Element getElement(String paramId);

	abstract void onDelete();

	public int compareName(Analyzer other) {
		String name1 = this.getName();
		String name2 = other == null ? null : other.getName();
		if (name1 == name2)
			return 0;
		if (name1 == null)
			return -1;
		if (name2 == null)
			return 1;
		return name1.compareToIgnoreCase(name2);
	}

}
