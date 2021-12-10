/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception for connection related errors
// Change log:
//   2008-10-24: initial version
// ----------------------------------------------------------------------------
// $Id: ConnectionException.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import it.csi.periferico.PerifericoException;

/**
 * Exception for connection related errors
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ConnectionException extends PerifericoException {

	private static final long serialVersionUID = 8709783306886764286L;

	public ConnectionException() {
	}

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionException(String msg, Object... args) {
		super(msg, args);
	}

}
