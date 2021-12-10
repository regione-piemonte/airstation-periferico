/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: board library implementation based on Advantech Adam
// Change log:
//   2016-10-12: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------
package it.csi.periferico.boards.adam;

import it.csi.periferico.acqdrivers.conn.serial.SerialConnection;

/**
 * Board library implementation based on Advantech Adam
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
class SerialBusHandle extends BusHandle {

	private String handleName;

	SerialBusHandle(SerialConnection connection, Integer maxScanAddress) {
		super(connection, maxScanAddress);
		handleName = "adam_serial_bus://" + connection;
	}

	@Override
	public String toString() {
		return handleName;
	}

}
