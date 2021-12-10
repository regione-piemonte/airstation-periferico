/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception used in data port drivers for protocol issues
// Change log:
//   2011-07-12: initial version
// ----------------------------------------------------------------------------
// $Id: ProtocolException.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Exception used in data port drivers for protocol issues
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ProtocolException extends DriverException {

	private static final long serialVersionUID = 1766462097323081604L;

	public ProtocolException() {
	}

	public ProtocolException(String message) {
		super(message);
	}

	public ProtocolException(Throwable cause) {
		super(cause);
	}

	public ProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

}
