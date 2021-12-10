/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for status information
// Change log:
//   2008-06-12: initial version
// ----------------------------------------------------------------------------
// $Id: Status.java,v 1.3 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Base class for status information
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class Status extends Value {

	public Status(Date timestamp) {
		super(timestamp);
	}

}
