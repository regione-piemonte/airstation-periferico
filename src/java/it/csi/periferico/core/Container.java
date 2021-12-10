/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the container of the measurement station
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: Container.java,v 1.12 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.config.common.AlarmName;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Represents the container of the measurement station
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Container extends ConfigItem {

	private static final long serialVersionUID = 133517243710203212L;

	private List<ContainerAlarm> listAlarm = new ArrayList<ContainerAlarm>();

	private ContainerAlarm newAlarm = null;

	public List<ContainerAlarm> getListAlarm() {
		return listAlarm;
	}

	public void setListAlarm(List<ContainerAlarm> listAlarm) {
		this.listAlarm = listAlarm;
	}

	public ContainerAlarm makeNewAlarm(AlarmName an) {
		newAlarm = ContainerAlarm.getInstance(an);
		return newAlarm;
	}

	public boolean isNewAlarm(ContainerAlarm ca) {
		return (ca == newAlarm);
	}

	public boolean insertNewAlarm() {
		if (newAlarm == null)
			throw new IllegalStateException("No new alarm to insert");
		listAlarm.add(newAlarm);
		newAlarm = null;
		return true;
	}

	public ContainerAlarm getAlarm(String strId) {
		strId = trim(strId);
		UUID uid = UUID.fromString(strId);
		for (ContainerAlarm alarm : listAlarm) {
			if (uid.equals(alarm.getId()))
				return alarm;
		}
		if (newAlarm != null && uid.equals(newAlarm.getId()))
			return newAlarm;
		return null;
	}

	public boolean deleteAlarm(String strId) {
		strId = trim(strId);
		UUID uid = UUID.fromString(strId);
		Iterator<ContainerAlarm> it = listAlarm.iterator();
		while (it.hasNext()) {
			ContainerAlarm alarm = it.next();
			if (uid.equals(alarm.getId())) {
				it.remove();
				return true;
			}
		}
		if (newAlarm != null && uid.equals(newAlarm.getId())) {
			newAlarm = null;
			return true;
		}
		return false;
	}

	@Override
	public void checkConfig() throws ConfigException {
		for (ContainerAlarm ca : listAlarm)
			ca.checkConfig();
	}

}
