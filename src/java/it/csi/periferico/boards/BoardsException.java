/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception for acquisition boards
// Change log:
//   2008-04-11: initial version
// ----------------------------------------------------------------------------
// $Id: BoardsException.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.PerifericoException;

/**
 * Exception for acquisition boards
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public class BoardsException extends PerifericoException {

	private static final long serialVersionUID = -8603363215052244373L;

	/**
	 * 
	 */
	public BoardsException() {
	}

	/**
	 * @param message
	 */
	public BoardsException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public BoardsException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BoardsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param msg
	 * @param args
	 */
	public BoardsException(String msg, Object... args) {
		super(msg, args);
	}
}
