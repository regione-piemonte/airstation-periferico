/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception used in data port drivers for invalid params
// Change log:
//   2015-04-27: initial version
// ----------------------------------------------------------------------------
// $Id: UnboundDriverException.java,v 1.1 2015/04/29 16:33:37 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Exception used in data port drivers for invalid params
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class UnboundDriverException extends DriverException {

	private static final long serialVersionUID = -8946826331168336778L;

	public UnboundDriverException() {
	}

	public UnboundDriverException(String message) {
		super(message);
	}

	public UnboundDriverException(Throwable cause) {
		super(cause);
	}

	public UnboundDriverException(String message, Throwable cause) {
		super(message, cause);
	}

}
