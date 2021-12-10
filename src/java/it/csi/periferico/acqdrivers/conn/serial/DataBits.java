/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: data bits configuration for serial connection
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: DataBits.java,v 1.1 2011/11/14 11:22:55 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.conn.serial;

/**
 * Data bits configuration for serial connection
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public enum DataBits {

	_5(5), //
	_6(6), //
	_7(7), //
	_8(8);

	private final int value;

	DataBits(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
