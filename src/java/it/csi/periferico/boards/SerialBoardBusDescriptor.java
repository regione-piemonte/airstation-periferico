/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: this class holds the information needed to configure a
//                  bus of acquisition boards over serial port
// Change log:
//   2016-08-31: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;

/**
 * This class holds the information needed to configure a bus of acquisition
 * boards over serial port
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class SerialBoardBusDescriptor extends BoardDescriptor {

	private static final long serialVersionUID = -9102384714128031787L;

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
	}

	@Override
	public SerialBoardBus newBoard() {
		SerialBoardBus board = new SerialBoardBus();
		board.setBrand(getBrand());
		board.setModel(getModel());
		return board;
	}

}
