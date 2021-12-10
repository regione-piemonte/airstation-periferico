/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception related to storage management
// Change log:
//   2008-05-30: initial version
// ----------------------------------------------------------------------------
// $Id: StorageException.java,v 1.5 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.PerifericoException;

/**
 * Exception related to storage management
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class StorageException extends PerifericoException {

	private static final long serialVersionUID = -5182644658447132658L;

	public StorageException() {
	}

	/**
	 * @param message
	 */
	public StorageException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public StorageException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param msg
	 * @param args
	 */
	public StorageException(String msg, Object... args) {
		super(msg, args);
	}

}
