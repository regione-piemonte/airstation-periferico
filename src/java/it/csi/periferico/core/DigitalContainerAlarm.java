/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: container alarm of type digital
// Change log:
//   2008-07-09: initial version
// ----------------------------------------------------------------------------
// $Id: DigitalContainerAlarm.java,v 1.5 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

/**
 * Container alarm of type digital
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DigitalContainerAlarm extends ContainerAlarm implements Holder {

	private static final long serialVersionUID = 4622675754999709296L;

	private DigitalAlarm alarm;

	private transient BinaryStatusCollector statusCollector;

	public DigitalContainerAlarm() {
		statusCollector = new BinaryStatusCollector();
	}

	@Override
	public Alarm getAlarm() {
		return alarm;
	}

	public DigitalAlarm getDigitalAlarm() {
		return alarm;
	}

	public void setDigitalAlarm(DigitalAlarm alarm) {
		if (alarm != null) {
			alarm.setHolder(this);
			alarm.setStatusCollector(statusCollector);
		}
		this.alarm = alarm;
	}

	public BinaryStatus getStatus() {
		return statusCollector.getLast();
	}

	BinaryStatusCollector getStatusCollector() {
		return statusCollector;
	}

	@Override
	void saveData() throws StorageException {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		storageManager.saveDigitalCAData(getId(), getAlarm().getAlarmNameId(),
				getStatusCollector().getAllAndClean());
	}

}
