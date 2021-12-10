/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception for errors occurred in the client side of
//                  communication
// Change log:
//   2008-10-23: initial version
// ----------------------------------------------------------------------------
// $Id: ClientException.java,v 1.2 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import it.csi.periferico.PerifericoException;

/**
 * Exception for errors occurred in the client side of communication
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public class ClientException extends PerifericoException {

	private static final long serialVersionUID = 5713970497792266192L;

	public ClientException() {
	}

	public ClientException(String message) {
		super(message);
	}

	public ClientException(Throwable cause) {
		super(cause);
	}

	public ClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientException(String msg, Object... args) {
		super(msg, args);
	}

}
