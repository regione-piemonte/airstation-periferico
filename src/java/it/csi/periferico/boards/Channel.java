/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents an acquisition board's channel
// Change log:
//   2008-04-03: initial version
// ----------------------------------------------------------------------------
// $Id: Channel.java,v 1.10 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.io.Serializable;

/**
 * Represents an acquisition board's channel
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class Channel implements IOProvider, Serializable {

	private static final long serialVersionUID = -3207086803691201961L;

	private Subdevice subdevice;

	private int channel;

	private IOUser ioUser = null;

	private boolean acqError = false;

	public Channel(Subdevice subdevice, int channel) {
		this.subdevice = subdevice;
		this.channel = channel;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public void bindIOUser(IOUser ioUser) throws BoardsException {
		if (ioUser == null)
			throw new IllegalArgumentException("IOUser must not be null");
		this.ioUser = ioUser;
		BoardBindInfo boardBindInfo = new BoardBindInfo();
		boardBindInfo.setBoardId(subdevice.getBoardId());
		boardBindInfo.setSubDevice(subdevice.getSubdevice());
		boardBindInfo.setChannel(channel);
		ioUser.setBoardBindInfo(boardBindInfo);
		ioUser.setIOProvider(this);
	}

	public boolean isBinded() {
		return ioUser != null;
	}

	public void unbind() {
		if (ioUser == null)
			return;
		ioUser.setBoardBindInfo(null);
		ioUser.setIOProvider(null);
		ioUser = null;
	}

	public IOUser getBindedIOUser() {
		return ioUser;
	}

	void setIOUser(IOUser ioUser) {
		this.ioUser = ioUser;
	}

	public boolean isAcqError() {
		return acqError;
	}

	public void setAcqError(boolean acqError) {
		this.acqError = acqError;
	}

}
