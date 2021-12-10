/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception that is thrown by the UI service when user
//                  supplied parameters are not valid
// Change log:
//   2009-02-24: initial version
// ----------------------------------------------------------------------------
// $Id: UserParamsException.java,v 1.2 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client;

/**
 * Exception that is thrown by the UI service when user supplied parameters are
 * not valid
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class UserParamsException extends Exception {

	private static final long serialVersionUID = 770942724097137001L;

	public UserParamsException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public UserParamsException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public UserParamsException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserParamsException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
