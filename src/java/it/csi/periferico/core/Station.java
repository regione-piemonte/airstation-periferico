/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the measurement station
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: Station.java,v 1.48 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.boards.IOUser;
import it.csi.periferico.comm.ConnectionParams;
import it.csi.periferico.comm.RouterConnectionParams;
import it.csi.periferico.config.common.AlarmName;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;
import it.csi.periferico.config.common.StorageManagerCfg;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Represents the measurement station
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Station extends ConfigItem {

	private static final long serialVersionUID = -8604760128461333599L;

	private static final int MIN_SHORTNAME_LEN = 2;

	private static final int MAX_SHORTNAME_LEN = 16;

	private static Logger logger = Logger.getLogger("periferico."
			+ Station.class.getSimpleName());

	private UUID id = UUID.randomUUID();

	private String shortName = "";

	private String name = "";

	private String location = "";

	private String address = "";

	private String city = "";

	private String province = "";

	private boolean gpsInstalled = false;

	private String userNotes = "";

	private ConnectionParams connectionParams = new RouterConnectionParams();

	private Container container = new Container();

	private List<Analyzer> listAnalyzer = new ArrayList<Analyzer>();

	private Analyzer newAnalyzer = null;

	private transient ContainerMonitor containerMonitor = new ContainerMonitor();

	private transient Map<UUID, String> resourceNamesMap = new HashMap<UUID, String>();

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = trim(address);
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = trim(city);
	}

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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = trim(location);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = trim(name);
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) throws ConfigException {
		this.shortName = checkLength("shortname", shortName, MIN_SHORTNAME_LEN,
				MAX_SHORTNAME_LEN);
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = trim(province);
	}

	public String getUserNotes() {
		return userNotes;
	}

	public void setUserNotes(String userNotes) {
		this.userNotes = trimcrlf(userNotes);
	}

	public boolean isGpsInstalled() {
		return gpsInstalled;
	}

	public void setGpsInstalled(boolean gpsInstalled) {
		this.gpsInstalled = gpsInstalled;
	}

	public void setConfig(String shortName, String name, String location,
			String address, String city, String province, String userNotes,
			boolean gpsInstalled) throws ConfigException {
		setShortName(shortName);
		setName(name);
		setLocation(location);
		setAddress(address);
		setCity(city);
		setProvince(province);
		setUserNotes(userNotes);
		setGpsInstalled(gpsInstalled);
	}

	public boolean isSameConfig(String shortName, String name, String location,
			String address, String city, String province, String userNotes,
			boolean gpsInstalled) {
		return this.shortName.equals(trim(shortName))
				&& this.name.equals(trim(name))
				&& this.location.equals(trim(location))
				&& this.address.equals(trim(address))
				&& this.city.equals(trim(city))
				&& this.province.equals(trim(province))
				&& this.userNotes.equals(trimcrlf(userNotes))
				&& this.gpsInstalled == gpsInstalled;
	}

	public ConnectionParams getConnectionParams() {
		return connectionParams;
	}

	public void setConnectionParams(ConnectionParams connectionParams) {
		this.connectionParams = connectionParams;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public List<Analyzer> getListAnalyzer() {
		return listAnalyzer;
	}

	public void setListAnalyzer(List<Analyzer> listAnalyzer) {
		this.listAnalyzer = listAnalyzer;
	}

	public Analyzer makeNewAnalyzer(Analyzer.Type type) {
		newAnalyzer = Analyzer.newAnalyzer(type);
		return newAnalyzer;
	}

	public DataPortAnalyzer makeNewDataPortAnalyzer(String brand, String model) {
		DataPortAnalyzer dpa = Periferico.getInstance().getDriverManager()
				.makeNewAnalyzer(brand, model);
		newAnalyzer = dpa;
		return dpa;
	}

	public boolean isNewAnalyzer(Analyzer a) {
		return (a == newAnalyzer);
	}

	public boolean insertNewAnalyzer() {
		if (newAnalyzer == null)
			throw new IllegalStateException("No new analyzer to insert");
		listAnalyzer.add(newAnalyzer);
		newAnalyzer = null;
		return true;
	}

	public Analyzer getAnalyzer(String strId) {
		strId = trim(strId);
		UUID uid = UUID.fromString(strId);
		for (Analyzer a : listAnalyzer) {
			if (uid.equals(a.getId()))
				return a;
		}
		if (newAnalyzer != null && uid.equals(newAnalyzer.getId()))
			return newAnalyzer;
		return null;
	}

	public Analyzer getAnalyzer(UUID uid) {
		for (Analyzer a : listAnalyzer) {
			if (uid.equals(a.getId()))
				return a;
		}
		if (newAnalyzer != null && uid.equals(newAnalyzer.getId()))
			return newAnalyzer;
		return null;
	}

	public boolean deleteAnalyzer(String strId) {
		strId = trim(strId);
		UUID uid = UUID.fromString(strId);
		Iterator<Analyzer> it = listAnalyzer.iterator();
		while (it.hasNext()) {
			Analyzer an = it.next();
			if (uid.equals(an.getId())) {
				an.setStatus(Analyzer.Status.DELETED);
				if (!an.isInitialized())
					it.remove();
				return true;
			}
		}
		if (newAnalyzer != null && uid.equals(newAnalyzer.getId())) {
			newAnalyzer = null;
			return true;
		}
		return false;
	}

	public List<IOUser> getListIOUser() {
		List<IOUser> listIOUser = new ArrayList<IOUser>();
		for (Analyzer an : listAnalyzer) {
			if (an.getStatus() == Analyzer.Status.DELETED)
				continue;
			if (an instanceof RainAnalyzer) {
				RainAnalyzer rainAn = (RainAnalyzer) an;
				if (rainAn.getRainElement() != null)
					listIOUser.add(rainAn.getRainElement());
				if (rainAn.getFault() != null)
					listIOUser.add(rainAn.getFault());
				if (rainAn.getDataValid() != null)
					listIOUser.add(rainAn.getDataValid());
			} else if (an instanceof AvgAnalyzer) {
				AvgAnalyzer avgAn = (AvgAnalyzer) an;
				List<AvgElement> listAE = avgAn.getListElements();
				listIOUser.addAll(listAE);
				if (avgAn.getFault() != null)
					listIOUser.add(avgAn.getFault());
				if (avgAn.getDataValid() != null)
					listIOUser.add(avgAn.getDataValid());
			} else if (an instanceof SampleAnalyzer) {
				SampleAnalyzer sampleAn = (SampleAnalyzer) an;
				List<SampleElement> listSE = sampleAn.getListElements();
				listIOUser.addAll(listSE);
				if (sampleAn.getFault() != null)
					listIOUser.add(sampleAn.getFault());
				if (sampleAn.getDataValid() != null)
					listIOUser.add(sampleAn.getDataValid());
			} else if (an instanceof WindAnalyzer) {
				WindAnalyzer wa = (WindAnalyzer) an;
				listIOUser.add(wa.getWind().getSpeed());
				listIOUser.add(wa.getWind().getDirection());
				if (wa.getFault() != null)
					listIOUser.add(wa.getFault());
				if (wa.getDataValid() != null)
					listIOUser.add(wa.getDataValid());
			}
		}
		List<ContainerAlarm> listCA = container.getListAlarm();
		for (ContainerAlarm ca : listCA) {
			Alarm alarm = ca.getAlarm();
			if (alarm instanceof DigitalAlarm)
				listIOUser.add((DigitalAlarm) alarm);
		}
		return listIOUser;
	}

	public void checkConfig() throws ConfigException {
		checkLength("shortname", shortName, MIN_SHORTNAME_LEN,
				MAX_SHORTNAME_LEN);
		if (connectionParams != null)
			connectionParams.checkConfig();
		container.checkConfig();
		for (Analyzer an : listAnalyzer)
			an.checkConfig();
	}

	@Override
	public void initConfig() {
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		StorageManagerCfg smc = cc.getStorageManagerCfg();
		if (smc == null)
			smc = new StorageManagerCfg();
		resourceNamesMap.clear();
		resourceNamesMap.put(getId(), getName());
		if (listAnalyzer != null) {
			Iterator<Analyzer> itAnalyzers = listAnalyzer.listIterator();
			while (itAnalyzers.hasNext()) {
				Analyzer an = itAnalyzers.next();
				Date deletionDate = an.getDeletionDate();
				if (deletionDate != null) {
					long elapsedDays = (System.currentTimeMillis() - deletionDate
							.getTime()) / (24 * 60 * 60 * 1000);
					if (elapsedDays > smc.getMaxDaysOfAggregateData()) {
						itAnalyzers.remove();
						continue;
					}
				}
				an.initConfig();
				an.setContainerMonitor(containerMonitor);
				resourceNamesMap.put(an.getId(), an.getName());
			}
		}
		containerMonitor.clearAlarms();
		if (container != null && container.getListAlarm() != null) {
			for (ContainerAlarm ca : container.getListAlarm()) {
				ca.initConfig();
				resourceNamesMap.put(ca.getId(), ca.getDescription());
				Alarm alarm = ca.getAlarm();
				alarm.unbindObservers();
				if (alarm instanceof TriggerAlarm)
					bindTriggerAlarm((TriggerAlarm) alarm);
				AlarmName an = cc.getAlarmName(alarm.getAlarmNameId());
				if (an != null && an.isDataQualityRelevant())
					containerMonitor.addAlarm(alarm);
			}
		}
	}

	public List<ObservableSampleElement> getTriggerAlarmBindableElements() {
		List<ObservableSampleElement> listElements = new ArrayList<ObservableSampleElement>();
		if (listAnalyzer != null) {
			for (Analyzer an : listAnalyzer) {
				if (an.getStatus() == Analyzer.Status.DELETED)
					continue;
				if (an instanceof ObservableAnalyzer) {
					ObservableAnalyzer oa = (ObservableAnalyzer) an;
					listElements.addAll(oa.getListElements());
				}
			}
		}
		return listElements;
	}

	public String getBindLabelForTriggerAlarm(TriggerAlarm ta) {
		if (ta == null)
			return null;
		UUID analyzerId = ta.getObservedAnalyzerId();
		String paramId = ta.getObservedParameterId();
		if (analyzerId == null || paramId == null)
			return null;
		Analyzer an = getAnalyzer(analyzerId);
		if (!(an instanceof ObservableAnalyzer))
			return null;
		Element element = an.getElement(paramId);
		if (!(element instanceof ObservableSampleElement))
			return null;
		ObservableSampleElement oe = (ObservableSampleElement) element;
		return oe.getBindLabel();
	}

	public String getBindIdentifierForTriggerAlarm(TriggerAlarm ta) {
		if (ta == null)
			return null;
		UUID analyzerId = ta.getObservedAnalyzerId();
		String paramId = ta.getObservedParameterId();
		if (analyzerId == null || paramId == null)
			return null;
		Analyzer an = getAnalyzer(analyzerId);
		if (!(an instanceof ObservableAnalyzer))
			return null;
		Element element = an.getElement(paramId);
		if (!(element instanceof ObservableSampleElement))
			return null;
		ObservableSampleElement oe = (ObservableSampleElement) element;
		return oe.getBindIdentifier();
	}

	public boolean bindTriggerAlarmById(TriggerAlarm ta, String id) {
		if (ta == null || id == null || listAnalyzer == null)
			return false;
		for (Analyzer an : listAnalyzer) {
			if (an instanceof ObservableAnalyzer) {
				ObservableAnalyzer oa = (ObservableAnalyzer) an;
				for (ObservableSampleElement oe : oa.getListElements()) {
					if (oe.getBindIdentifier().equals(id)) {
						ta.setObservedAnalyzerId(an.getId());
						ta.setObservedParameterId(oe.getParameterId());
						bindTriggerAlarm(ta);
						return true;
					}
				}
			}
		}
		return false;
	}

	public void unbindTriggerAlarm(TriggerAlarm ta) {
		ta.setObservedAnalyzerId(null);
		ta.setObservedParameterId(null);
	}

	private void bindTriggerAlarm(TriggerAlarm ta) {
		ta.setActive(false);
		UUID observedAnalyzerId = ta.getObservedAnalyzerId();
		String observedParamId = ta.getObservedParameterId();
		if (observedAnalyzerId == null || observedParamId == null)
			return;
		logger.info("Binding TriggerAlarm: " + ta.getAlarmNameId() + " to "
				+ observedAnalyzerId + "/" + observedParamId);
		Analyzer an = getAnalyzer(observedAnalyzerId);
		if (an == null) {
			logger.error("Analyzer " + observedAnalyzerId + " not found");
			return;
		}
		Element element = an.getElement(observedParamId);
		if (element == null) {
			logger.error("The analyzer " + an.getName()
					+ " does not have element " + observedParamId);
			return;
		}
		if (!(element instanceof ObservableSampleElement)) {
			logger.error("Element " + element.getParameterId() + " of type "
					+ element.getClass() + " is not supported by TriggerAlarm");
			return;
		}
		ObservableSampleElement oe = (ObservableSampleElement) element;
		if (an.getStatus() != Analyzer.Status.ENABLED) {
			logger.warn("TriggerAlarm not activated: analyzer not enabled");
			return;
		}
		if (!element.isEnabled()) {
			logger.warn("TriggerAlarm not activated: element not enabled");
			return;
		}
		oe.addSampleObserver(ta);
		ta.setActive(true);
	}

	public String getResourceName(UUID uuid) {
		return resourceNamesMap.get(uuid);
	}

}
