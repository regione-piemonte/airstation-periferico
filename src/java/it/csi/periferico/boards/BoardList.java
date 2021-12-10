/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: implements a list of acquisition boards
// Change log:
//   2008-04-10: initial version
// ----------------------------------------------------------------------------
// $Id: BoardList.java,v 1.6 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Implements a list of acquisition boards
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class BoardList extends ConfigItem {

	private static final long serialVersionUID = -533101340852742101L;

	private List<Board> boards = new ArrayList<Board>();

	private Board newBoard = null;

	public List<Board> getBoards() {
		return boards;
	}

	public void setBoards(List<Board> boards) {
		this.boards = boards;
	}

	void setNewBoard(Board b) {
		this.newBoard = b;
	}

	public boolean isNewBoard(Board b) {
		return (b == newBoard);
	}

	public boolean insertNewBoard() {
		if (newBoard == null)
			throw new IllegalStateException("No new board to insert");
		boards.add(newBoard);
		newBoard = null;
		return true;
	}

	public Board getBoard(String strId) {
		strId = trim(strId);
		UUID uid = UUID.fromString(strId);
		for (Board b : boards) {
			if (uid.equals(b.getId()))
				return b;
		}
		if (newBoard != null && uid.equals(newBoard.getId()))
			return newBoard;
		return null;
	}

	boolean deleteBoard(String strId) {
		strId = trim(strId);
		UUID uid = UUID.fromString(strId);
		Iterator<Board> it = boards.iterator();
		while (it.hasNext()) {
			Board b = it.next();
			if (uid.equals(b.getId())) {
				it.remove();
				return true;
			}
		}
		if (newBoard != null && uid.equals(newBoard.getId())) {
			newBoard = null;
			return true;
		}
		return false;
	}

	@Override
	public void checkConfig() throws ConfigException {
		for (Board b : boards)
			b.checkConfig();
	}
}
