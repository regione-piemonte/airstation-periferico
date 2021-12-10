/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception used in data port drivers when the analyzer does
//   not reply to commands
// Change log:
//   2011-07-12: initial version
// ----------------------------------------------------------------------------
// $Id: NoReplyException.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Exception used in data port drivers when the analyzer does not reply to
 * commands
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class NoReplyException extends DriverException {

	private static final long serialVersionUID = -3999383416162361083L;

	public NoReplyException() {
	}

	public NoReplyException(String message) {
		super(message);
	}

	public NoReplyException(Throwable cause) {
		super(cause);
	}

	public NoReplyException(String message, Throwable cause) {
		super(message, cause);
	}

}
