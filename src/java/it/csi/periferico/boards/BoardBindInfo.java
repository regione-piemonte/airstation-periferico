/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the information needed to identify a channel on
//                  a given acquisition board
// Change log:
//   2008-03-04: initial version
// ----------------------------------------------------------------------------
// $Id: BoardBindInfo.java,v 1.7 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.UUID;

/**
 * Represents the information needed to identify a channel on a given
 * acquisition board
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class BoardBindInfo extends ConfigItem {

	private static final long serialVersionUID = -4623683113142889707L;

	private UUID boardId;

	private int subDevice;

	private int channel;

	public UUID getBoardId() {
		return boardId;
	}

	public void setBoardId(UUID boardId) {
		this.boardId = boardId;
	}

	public String getBoardIdAsString() {
		if (boardId == null)
			return "";
		return boardId.toString();
	}

	public void setBoardIdAsString(String strBoardId) {
		this.boardId = UUID.fromString(trim(strBoardId));
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getSubDevice() {
		return subDevice;
	}

	public void setSubDevice(int subDevice) {
		this.subDevice = subDevice;
	}

	@Override
	public void checkConfig() throws ConfigException {
	}

	@Override
	public String toString() {
		return boardId + "/" + subDevice + "/" + channel;
	}

}
