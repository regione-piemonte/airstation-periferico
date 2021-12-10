/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception for service errors
// Change log:
//   2008-07-24: initial version
// ----------------------------------------------------------------------------
// $Id: ServiceException.java,v 1.2 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

/**
 * Exception for service errors
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public class ServiceException extends Exception {

	private static final long serialVersionUID = -1481594225677937053L;

	public ServiceException() {
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
