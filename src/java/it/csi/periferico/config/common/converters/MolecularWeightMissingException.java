/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception used when the molecular weight of a parameter is
//                  not specified
// Change log:
//   2008-05-08: initial version
// ----------------------------------------------------------------------------
// $Id: MolecularWeightMissingException.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common.converters;

/**
 * Exception used when the molecular weight of a parameter is not specified
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public class MolecularWeightMissingException extends Exception {

	private static final long serialVersionUID = 5845298866638281123L;

	public MolecularWeightMissingException() {
	}

	public MolecularWeightMissingException(String message) {
		super(message);
	}

	public MolecularWeightMissingException(Throwable cause) {
		super(cause);
	}

	public MolecularWeightMissingException(String message, Throwable cause) {
		super(message, cause);
	}

}
