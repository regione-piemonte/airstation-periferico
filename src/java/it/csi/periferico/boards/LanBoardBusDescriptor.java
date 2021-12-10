/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: this class holds the information needed to configure a
//                  bus of acquisition boards over LAN
// Change log:
//   2016-08-31: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;

/**
 * This class holds the information needed to configure a bus of acquisition
 * boards over LAN
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class LanBoardBusDescriptor extends BoardDescriptor {

	private static final long serialVersionUID = 716760331953400302L;

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
	}

	@Override
	public LanBoardBus newBoard() {
		LanBoardBus board = new LanBoardBus();
		board.setBrand(getBrand());
		board.setModel(getModel());
		return board;
	}

}
