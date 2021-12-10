/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a digital input event
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: DIEvent.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.Date;

/**
 * Represents a digital input event
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DIEvent implements Dispatchable {

	private IOUser destination;
	private Date timestamp;
	private boolean error;
	private boolean value;

	public DIEvent(Date timestamp, boolean value, boolean error,
			IOUser destination) {
		this.timestamp = timestamp;
		this.value = value;
		this.error = error;
		this.destination = destination;
	}

	public IOUser getDestination() {
		return (destination);
	}

	public void setDestination(IOUser destination) {
		this.destination = destination;
	}

	public Date getTimeStamp() {
		return (timestamp);
	}

	public boolean getError() {
		return (error);
	}

	public boolean getValue() {
		return (value);
	}
}
