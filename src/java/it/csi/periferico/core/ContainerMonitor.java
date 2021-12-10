/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: monitors alarms that may affect container environment
// Change log:
//   2008-09-24: initial version
// ----------------------------------------------------------------------------
// $Id: ContainerMonitor.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitors alarms that may affect container environment
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public class ContainerMonitor implements StatusObserver {

	private boolean containerEnvironmentOK = true;

	private List<Alarm> dataQualityRelevantAlarms = new ArrayList<Alarm>();

	public boolean deliver(Status status) {
		boolean cumulativeStatus = false;
		for (Alarm alarm : dataQualityRelevantAlarms) {
			if (alarm instanceof DigitalAlarm) {
				BinaryStatus bs = ((DigitalAlarm) alarm).getStatusCollector()
						.getLast();
				if (bs == null)
					continue;
				if (bs.getStatus()) {
					cumulativeStatus = true;
					break;
				}
			} else if (alarm instanceof TriggerAlarm) {
				AlarmStatus as = ((TriggerAlarm) alarm).getStatusCollector()
						.getLast();
				if (as == null)
					continue;
				if (as.getStatus() == AlarmStatus.Status.ALARM
						|| as.getStatus() == AlarmStatus.Status.ALARM_HIGH
						|| as.getStatus() == AlarmStatus.Status.ALARM_LOW) {
					cumulativeStatus = true;
					break;
				}
			}
		}
		containerEnvironmentOK = !cumulativeStatus;
		return true;
	}

	public void unbind() {
		// NOTE: no operation is needed here
	}

	public boolean isContainerEnvironmentOK() {
		return containerEnvironmentOK;
	}

	public void addAlarm(Alarm alarm) {
		dataQualityRelevantAlarms.add(alarm);
		alarm.addStatusObserver(this);
	}

	public void clearAlarms() {
		dataQualityRelevantAlarms.clear();
		containerEnvironmentOK = true;
	}

}
