/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: generic exception used for errors occurred
// communicating with a data port analyzer
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: ConnectionException.java,v 1.2 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.itf;

/**
 * Generic exception used for errors occurred communicating with a data port
 * analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ConnectionException extends DriverException {

	private static final long serialVersionUID = -1226140585716379198L;

	public ConnectionException() {
	}

	/**
	 * @param message
	 */
	public ConnectionException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ConnectionException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
