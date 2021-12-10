/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: collects the status events of an alarm
// Change log:
//   2008-07-09: initial version
// ----------------------------------------------------------------------------
// $Id: AlarmStatusCollector.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the status events of an alarm
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public class AlarmStatusCollector {

	private List<AlarmStatus> listAlarmStatus = new ArrayList<AlarmStatus>();

	private AlarmStatus lastAlarmStatus = null;

	public AlarmStatus getLast() {
		return lastAlarmStatus;
	}

	synchronized void add(AlarmStatus as) {
		lastAlarmStatus = as;
		listAlarmStatus.add(as);
	}

	synchronized List<AlarmStatus> getAllAndClean() {
		List<AlarmStatus> tmpList = listAlarmStatus;
		listAlarmStatus = new ArrayList<AlarmStatus>();
		return tmpList;
	}

}
