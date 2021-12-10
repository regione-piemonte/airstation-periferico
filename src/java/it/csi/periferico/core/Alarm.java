/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for all alarms
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: Alarm.java,v 1.19 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.config.common.ConfigItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all alarms
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public abstract class Alarm extends ConfigItem {

	private static final long serialVersionUID = 4696521580380843966L;

	private String alarmNameId;

	private boolean enabled = true;

	// NOTE: this list is transient, because binding/unbinding operations are
	// not performed by the user interface
	private transient List<StatusObserver> listStatusObservers = new ArrayList<StatusObserver>();

	public Alarm() {
		this("");
	}

	public Alarm(String alarmNameId) {
		this.alarmNameId = alarmNameId;
	}

	public String getAlarmNameId() {
		return alarmNameId;
	}

	public void setAlarmNameId(String alarmNameId) {
		this.alarmNameId = trim(alarmNameId);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setConfig(boolean enabled) {
		setEnabled(enabled);
	}

	public boolean isSameConfig(boolean enabled) {
		return this.enabled == enabled;
	}

	List<StatusObserver> getListStatusObservers() {
		return listStatusObservers;
	}

	boolean addStatusObserver(StatusObserver so) {
		if (so == null)
			return false;
		listStatusObservers.add(so);
		return true;
	}

	void unbindObservers() {
		for (StatusObserver so : listStatusObservers) {
			so.unbind();
		}
		listStatusObservers = new ArrayList<StatusObserver>();
	}

}
