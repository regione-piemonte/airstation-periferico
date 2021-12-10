/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for data port driver exceptions
// Change log:
//   2008-10-03: initial version
// ----------------------------------------------------------------------------
// $Id: DriverException.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Base class for data port driver exceptions
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public abstract class DriverException extends Exception {

	private static final long serialVersionUID = 6605908890835307373L;

	public DriverException() {
	}

	public DriverException(String message) {
		super(message);
	}

	public DriverException(Throwable cause) {
		super(cause);
	}

	public DriverException(String message, Throwable cause) {
		super(message, cause);
	}

}
