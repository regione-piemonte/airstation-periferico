/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: container alarm of type trigger
// Change log:
//   2008-07-09: initial version
// ----------------------------------------------------------------------------
// $Id: TriggerContainerAlarm.java,v 1.5 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

/**
 * Container alarm of type trigger
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class TriggerContainerAlarm extends ContainerAlarm {

	private static final long serialVersionUID = -40189364046524157L;

	private TriggerAlarm alarm;

	private transient AlarmStatusCollector statusCollector;

	public TriggerContainerAlarm() {
		statusCollector = new AlarmStatusCollector();
	}

	@Override
	public Alarm getAlarm() {
		return alarm;
	}

	public TriggerAlarm getTriggerAlarm() {
		return alarm;
	}

	public void setTriggerAlarm(TriggerAlarm alarm) {
		if (alarm != null) {
			alarm.setStatusCollector(statusCollector);
		}
		this.alarm = alarm;
	}

	public AlarmStatus getStatus() {
		return statusCollector.getLast();
	}

	AlarmStatusCollector getStatusCollector() {
		return statusCollector;
	}

	@Override
	void saveData() throws StorageException {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		storageManager.saveTriggerCAData(getId(), getAlarm().getAlarmNameId(),
				getStatusCollector().getAllAndClean());
	}
}
