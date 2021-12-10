/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for alarm listeners
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: AlarmListener.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Interface for alarm listeners
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public interface AlarmListener {

	public boolean alarmChanged(AlarmStatus value, Alarm source);

}
