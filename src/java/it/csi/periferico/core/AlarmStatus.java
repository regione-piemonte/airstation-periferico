/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: status of an alarm signal
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: AlarmStatus.java,v 1.5 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Status of an alarm signal
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class AlarmStatus extends Status {

	public enum Status {
		OK, ALARM, WARNING, ALARM_HIGH, ALARM_LOW, WARNING_HIGH, WARNING_LOW
	}

	private Status status;

	public AlarmStatus(Date timestamp, Status status) {
		super(timestamp);
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

}
