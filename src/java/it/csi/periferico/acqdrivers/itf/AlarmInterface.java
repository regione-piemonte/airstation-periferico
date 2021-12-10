/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for alarms
// Change log:
//   2008-10-03: initial version
// ----------------------------------------------------------------------------
// $Id: AlarmInterface.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Interface for alarms. Questa interfaccia definisce le funzioni per
 * l’acquisizione dello stato dell’analizzatore.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface AlarmInterface {

	public boolean isActive();

	public int getAcqPeriod();

	public boolean deliver(FaultValue value);
}
