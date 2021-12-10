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
// $Id: DataValidAlarm.java,v 1.1 2013/11/15 15:52:48 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Implements a digital alarm
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DataValidAlarm extends DigitalAlarm {

	private static final long serialVersionUID = 4074505976379628699L;
	private boolean discardData = false;

	public DataValidAlarm() {
		super();
	}

	public DataValidAlarm(String alarmNameId) {
		super(alarmNameId);
	}

	public boolean isDiscardData() {
		return discardData;
	}

	public void setDiscardData(boolean discardData) {
		this.discardData = discardData;
	}

	public void setConfig(boolean enabled, boolean activeHigh,
			boolean discardData) {
		super.setConfig(enabled, activeHigh);
		setDiscardData(discardData);
	}

	public boolean isSameConfig(boolean enabled, boolean activeHigh,
			boolean discardData) {
		return super.isSameConfig(enabled, activeHigh)
				&& this.discardData == discardData;
	}

}
