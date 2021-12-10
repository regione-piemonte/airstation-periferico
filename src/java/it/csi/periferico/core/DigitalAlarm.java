/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: implements a digital alarm
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: DigitalAlarm.java,v 1.24 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.boards.BoardBindInfo;
import it.csi.periferico.boards.DIEvent;
import it.csi.periferico.boards.DIUser;
import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.config.common.ConfigException;

/**
 * Implements a digital alarm
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DigitalAlarm extends Alarm implements DIUser {

	private static final long serialVersionUID = -1492503269501184178L;

	private boolean activeHigh = true;

	private IOProvider ioProvider;

	private BoardBindInfo boardBindInfo = null;

	private Holder holder = null;

	private transient BinaryStatusCollector statusCollector = null;

	public DigitalAlarm() {
		super();
	}

	public DigitalAlarm(String alarmNameId) {
		super(alarmNameId);
	}

	public String getBindLabel() {
		return ((holder == null ? "" : holder.getName() + "/") + getAlarmNameId());
	}

	public String getBindIdentifier() {
		return ((holder == null ? "" : holder.getId() + "/") + getAlarmNameId());
	}

	public IOProvider getIOProvider() {
		return ioProvider;
	}

	public void setIOProvider(IOProvider ioProvider) {
		this.ioProvider = ioProvider;
	}

	public boolean isActiveHigh() {
		return activeHigh;
	}

	public void setActiveHigh(boolean activeHigh) {
		this.activeHigh = activeHigh;
	}

	public void setConfig(boolean enabled, boolean activeHigh) {
		super.setConfig(enabled);
		setActiveHigh(activeHigh);
	}

	public boolean isSameConfig(boolean enabled, boolean activeHigh) {
		return super.isSameConfig(enabled) && this.activeHigh == activeHigh;
	}

	public BoardBindInfo getBoardBindInfo() {
		return boardBindInfo;
	}

	public void setBoardBindInfo(BoardBindInfo boardBindInfo) {
		this.boardBindInfo = boardBindInfo;
	}

	public boolean isActive() {
		if (holder != null && !holder.isEnabled())
			return false;
		if (!isEnabled())
			return false;
		return true;
	}

	@Override
	public void checkConfig() throws ConfigException {
	}

	void setHolder(Holder holder) {
		this.holder = holder;
	}

	BinaryStatusCollector getStatusCollector() {
		return statusCollector;
	}

	void setStatusCollector(BinaryStatusCollector statusCollector) {
		this.statusCollector = statusCollector;
	}

	public boolean deliver(DIEvent diEvent) {
		if (holder == null)
			throw new IllegalStateException("Holder must be initialized");
		if (diEvent == null)
			return false;
		if (statusCollector == null)
			return false;
		BinaryStatus bs = new BinaryStatus(diEvent.getTimeStamp(),
				diEvent.getValue() == activeHigh);
		statusCollector.add(bs);
		for (StatusObserver so : getListStatusObservers()) {
			so.deliver(bs);
		}
		return true;
	}

}
