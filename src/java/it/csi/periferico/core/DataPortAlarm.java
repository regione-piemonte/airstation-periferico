/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: alarm for analyzers with data port interface
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: DataPortAlarm.java,v 1.8 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.acqdrivers.itf.AlarmInterface;
import it.csi.periferico.acqdrivers.itf.FaultValue;
import it.csi.periferico.config.common.ConfigException;

/**
 * Alarm for analyzers with data port interface
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DataPortAlarm extends Alarm implements AlarmInterface {

	private static final int DEFAULT_ACQ_PERIOD = 10; // s

	private static final long serialVersionUID = 4376640324194564321L;

	private int acqPeriod = DEFAULT_ACQ_PERIOD;

	private Holder holder = null;

	private transient BinaryStatusCollector statusCollector = null;

	public DataPortAlarm() {
		super();
	}

	public DataPortAlarm(String alarmNameId) {
		super(alarmNameId);
	}

	public int getAcqPeriod() {
		return acqPeriod;
	}

	public void setAcqPeriod(int acqPeriod) {
		this.acqPeriod = acqPeriod;
	}

	public void setConfig(boolean enabled, int acqPeriod)
			throws ConfigException {
		super.setConfig(enabled);
		setAcqPeriod(acqPeriod);
	}

	public boolean isSameConfig(boolean enabled, int acqPeriod) {
		return super.isSameConfig(enabled) && this.acqPeriod == acqPeriod;
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

	@Override
	public void checkConfig() throws ConfigException {
	}

	public boolean isActive() {
		return isEnabled();
	}

	public boolean deliver(FaultValue value) {
		if (holder == null)
			throw new IllegalStateException("Holder must be initialized");
		if (value == null)
			return false;
		if (statusCollector == null)
			return false;
		boolean fault = value.getFault() == null ? true : value.getFault();
		IntegerStatus is = new IntegerStatus(value.getTimestamp(),
				value.getValue(), fault);
		statusCollector.add(is);
		return true;
	}

}
