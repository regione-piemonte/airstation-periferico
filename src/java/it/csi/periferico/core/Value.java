/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for data
// Change log:
//   2008-07-01: initial version
// ----------------------------------------------------------------------------
// $Id: Value.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Base class for data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class Value {

	private Date timestamp;

	public Value(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

}
