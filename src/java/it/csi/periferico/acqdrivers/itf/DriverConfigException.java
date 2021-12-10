/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception used in data port drivers for configuration issues
// Change log:
//   2011-07-12: initial version
// ----------------------------------------------------------------------------
// $Id: DriverConfigException.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Exception used in data port drivers for configuration issues
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DriverConfigException extends DriverException {

	private static final long serialVersionUID = 5674007924632816343L;

	public DriverConfigException() {
	}

	public DriverConfigException(String message) {
		super(message);
	}

	public DriverConfigException(Throwable cause) {
		super(cause);
	}

	public DriverConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
