/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: parity configuration for serial connection
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: Parity.java,v 1.1 2011/11/14 11:22:55 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.conn.serial;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Parity configuration for serial connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public enum Parity {

	EVEN(SerialPort.EVEN_PARITY), //
	MARK(SerialPort.MARK_PARITY), //
	NONE(SerialPort.NO_PARITY), //
	ODD(SerialPort.ODD_PARITY), //
	SPACE(SerialPort.SPACE_PARITY);

	private final int value;

	Parity(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
