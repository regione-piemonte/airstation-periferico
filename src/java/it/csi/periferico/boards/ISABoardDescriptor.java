/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: this class holds the information needed to configure an
//                  ISA acquisition board
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: ISABoardDescriptor.java,v 1.6 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;

/**
 * This class holds the information needed to configure an ISA acquisition board
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class ISABoardDescriptor extends BoardDescriptor {

	private static final long serialVersionUID = 223609380640126589L;

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
	}

	@Override
	public Board newBoard() {
		ISABoard board = new ISABoard();
		board.setBrand(getBrand());
		board.setModel(getModel());
		return board;
	}

}
