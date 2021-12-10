/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: this class holds the information needed to configure a
//                  lan acquisition board
// Change log:
//   2016-08-31: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;

/**
 * This class holds the information needed to configure a lan acquisition board
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class LanBoardDescriptor extends BoardDescriptor {

	private static final long serialVersionUID = -520776551355405840L;

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
	}

	@Override
	public LanBoard newBoard() {
		LanBoard board = new LanBoard();
		board.setBrand(getBrand());
		board.setModel(getModel());
		return board;
	}

}
