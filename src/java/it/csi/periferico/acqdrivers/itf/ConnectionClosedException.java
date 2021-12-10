/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception used when the connection with a data port
// analyzer is closed
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: ConnectionClosedException.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.itf;

/**
 * Exception used when the connection with a data port analyzer is closed
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ConnectionClosedException extends ConnectionException {

	private static final long serialVersionUID = -3574666260092283425L;

	public ConnectionClosedException() {
	}

	/**
	 * @param message
	 */
	public ConnectionClosedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ConnectionClosedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConnectionClosedException(String message, Throwable cause) {
		super(message, cause);
	}

}
