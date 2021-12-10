/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception that is thrown by the UI service when the session
//                  is expired
// Change log:
//   2009-02-24: initial version
// ----------------------------------------------------------------------------
// $Id: SessionExpiredException.java,v 1.2 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client;

/**
 * Exception that is thrown by the UI service when the session is expired
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class SessionExpiredException extends Exception {

	private static final long serialVersionUID = 3326171908773689416L;

	public SessionExpiredException() {
	}

	/**
	 * @param message
	 */
	public SessionExpiredException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SessionExpiredException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SessionExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

}
