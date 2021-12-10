/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for manage results of drivers' async functions
// Change log:
//   2015-04-09: initial version
// ----------------------------------------------------------------------------
// $Id: DriverCallback.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.itf;

/**
 * Interface for manage results of drivers' async functions. Questa interfaccia
 * serve per la gestione del risultato dei comandi inviati agli analizzatori.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface DriverCallback<T> {

	void onFailure(Throwable caught);

	void onSuccess(T result);

}
