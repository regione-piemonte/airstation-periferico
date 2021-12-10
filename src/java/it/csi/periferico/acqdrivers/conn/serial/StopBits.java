/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: stop bits configuration for serial connection
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: StopBits.java,v 1.1 2011/11/14 11:22:55 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.conn.serial;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Stop bits configuration for serial connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public enum StopBits {

	_1(SerialPort.ONE_STOP_BIT), //
	_1_5(SerialPort.ONE_POINT_FIVE_STOP_BITS), //
	_2(SerialPort.TWO_STOP_BITS);

	private final int value;

	StopBits(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
