/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: holds information acquired from an anlog input
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: AIValue.java,v 1.7 2011/11/02 09:35:30 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.Date;

/**
 * Holds information acquired from an anlog input
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class AIValue implements Dispatchable {

	private IOUser destination;

	private Date timestamp;

	private boolean error;

	private boolean voltageOutOfRange;

	private double voltage;

	private boolean extra = false;

	public AIValue(Date timestamp, double voltage, boolean error,
			boolean voltageOutOfRange, IOUser destination) {
		this.timestamp = timestamp;
		this.voltage = voltage;
		this.error = error;
		this.voltageOutOfRange = voltageOutOfRange;
		this.destination = destination;
	}

	public IOUser getDestination() {
		return (destination);
	}

	public void setDestination(IOUser destination) {
		this.destination = destination;
	}

	public Date getTimestamp() {
		return (timestamp);
	}

	public boolean getError() {
		return (error);
	}

	public double getVoltage() {
		return (voltage);
	}

	public boolean isVoltageOutOfRange() {
		return (voltageOutOfRange);
	}

	public boolean isExtra() {
		return extra;
	}

	public void setExtra(boolean extra) {
		this.extra = extra;
	}

}
