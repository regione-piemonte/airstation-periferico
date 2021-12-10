/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: flow control configuration for serial connection
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: FlowControl.java,v 1.1 2011/11/14 11:22:55 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.conn.serial;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Flow control configuration for serial connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public enum FlowControl {

	NONE(SerialPort.FLOW_CONTROL_DISABLED), //
	RTSCTS(SerialPort.FLOW_CONTROL_RTS_ENABLED //
			| SerialPort.FLOW_CONTROL_CTS_ENABLED), //
	XONXOFF(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED //
			| SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);

	private final int value;

	FlowControl(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
