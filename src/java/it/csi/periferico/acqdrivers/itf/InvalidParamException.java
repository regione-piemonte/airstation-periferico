/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception used in data port drivers for invalid params
// Change log:
//   2015-04-17: initial version
// ----------------------------------------------------------------------------
// $Id: InvalidParamException.java,v 1.1 2015/04/17 16:03:51 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Exception used in data port drivers for invalid params
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class InvalidParamException extends DriverException {

	private static final long serialVersionUID = -8813429022156712715L;

	public InvalidParamException() {
	}

	public InvalidParamException(String message) {
		super(message);
	}

	public InvalidParamException(Throwable cause) {
		super(cause);
	}

	public InvalidParamException(String message, Throwable cause) {
		super(message, cause);
	}

}
