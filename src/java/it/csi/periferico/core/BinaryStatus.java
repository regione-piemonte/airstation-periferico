/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: status information of type binary
// Change log:
//   2008-06-11: initial version
// ----------------------------------------------------------------------------
// $Id: BinaryStatus.java,v 1.5 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Status information of type binary
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class BinaryStatus extends Status {

	// TODO: it may be better converting status from boolean to Boolean in order
	// to be able to keep track of DigitalAlarm's failures.
	private boolean status;

	public BinaryStatus(Date timestamp, boolean status) {
		super(timestamp);
		this.status = status;
	}

	public BinaryStatus(boolean status) {
		this(new Date(), status);
	}

	public boolean getStatus() {
		return status;
	}

	public boolean isOn() {
		return status;
	}

	public boolean isOff() {
		return !status;
	}

}
