/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class to represent an IO board subdevice
// Change log:
//   2008-04-24: initial version
// ----------------------------------------------------------------------------
// $Id: Subdevice.java,v 1.6 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.io.Serializable;
import java.util.UUID;

/**
 * Base class to represent an IO board subdevice
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class Subdevice implements Serializable {

	private static final long serialVersionUID = 6277757004099065912L;

	private Board board;

	private int subdevice;

	public Subdevice(Board board, int subdevice) {
		this.board = board;
		this.subdevice = subdevice;
	}

	public int getSubdevice() {
		return subdevice;
	}

	public void setSubdevice(int subdevice) {
		this.subdevice = subdevice;
	}

	public UUID getBoardId() {
		return board.getId();
	}

	public abstract Channel getChannel(int channel);

}
