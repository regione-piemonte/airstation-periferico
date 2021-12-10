/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception for file header's errors
// Change log:
//   2008-06-26: initial version
// ----------------------------------------------------------------------------
// $Id: HeaderException.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

/**
 * Exception for file header's errors
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class HeaderException extends Exception {

	private static final long serialVersionUID = 4429905104822789402L;

	public HeaderException() {
	}

	/**
	 * @param message
	 */
	public HeaderException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public HeaderException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public HeaderException(String message, Throwable cause) {
		super(message, cause);
	}

}
