/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for analyzers with basic IO electric interface
// Change log:
//   2013-07-11: initial version
// ----------------------------------------------------------------------------
// $Id: IOItfAnalyzer.java,v 1.3 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.config.common.ConfigException;

/**
 * Base class for analyzers with basic IO electric interface
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class IOItfAnalyzer extends Analyzer implements Holder {

	private static final long serialVersionUID = -2918488849108644566L;

	private DigitalAlarm fault = null;

	private DataValidAlarm dataValid = null;

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
		if (fault != null)
			fault.checkConfig();
		if (dataValid != null)
			dataValid.checkConfig();
	}

	@Override
	void onDelete() {
		unbindFault();
		unbindDataValid();
	}

	public DigitalAlarm getFault() {
		return fault;
	}

	public void setFault(DigitalAlarm fault) {
		if (fault != null) {
			fault.setHolder(this);
			fault.setStatusCollector(getFaultStatusCollector());
		}
		unbindFault();
		this.fault = fault;
	}

	public DigitalAlarm makeNewFault() {
		return new DigitalAlarm(EVT_FAULT);
	}

	public boolean deleteFault() {
		if (fault == null)
			return false;
		setFault(null);
		return true;
	}

	private void unbindFault() {
		if (fault != null) {
			IOProvider ioProvider = fault.getIOProvider();
			if (ioProvider != null)
				ioProvider.unbind();
		}
	}

	public DataValidAlarm getDataValid() {
		return dataValid;
	}

	public void setDataValid(DataValidAlarm dataValid) {
		if (dataValid != null) {
			dataValid.setHolder(this);
			dataValid.setStatusCollector(getDataValidStatusCollector());
		}
		unbindDataValid();
		this.dataValid = dataValid;
	}

	public DataValidAlarm makeNewDataValid() {
		return new DataValidAlarm(EVT_DATA_VALID);
	}

	public boolean deleteDataValid() {
		if (dataValid == null)
			return false;
		setDataValid(null);
		return true;
	}

	private void unbindDataValid() {
		if (dataValid != null) {
			IOProvider ioProvider = dataValid.getIOProvider();
			if (ioProvider != null)
				ioProvider.unbind();
		}
	}

	public boolean isDataValidWarningOnly() {
		return dataValid == null || !dataValid.isDiscardData();
	}

}
