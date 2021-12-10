/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception used in analyzers' drivers for driver loading
// issues
// Change log:
//   2015-04-01: initial version
// ----------------------------------------------------------------------------
// $Id: DriverLoaderException.java,v 1.2 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers;

import it.csi.periferico.acqdrivers.itf.DriverException;

/**
 * Exception used in analyzers' drivers for driver loading
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DriverLoaderException extends DriverException {

	private static final long serialVersionUID = 5824781208896007584L;

	public DriverLoaderException() {
	}

	public DriverLoaderException(String message) {
		super(message);
	}

	public DriverLoaderException(Throwable cause) {
		super(cause);
	}

	public DriverLoaderException(String message, Throwable cause) {
		super(message, cause);
	}

}
