/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for aggregate values
// Change log:
//   2008-06-16: initial version
// ----------------------------------------------------------------------------
// $Id: AggregateValue.java,v 1.5 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Base class for aggregate values
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class AggregateValue extends Value {

	private int flags;

	private boolean notvalid;

	public AggregateValue(Date timestamp, boolean notvalid, int flags) {
		super(timestamp);
		this.notvalid = notvalid;
		this.flags = flags;
	}

	public int getFlags() {
		return flags;
	}

	public boolean isNotvalid() {
		return notvalid;
	}

}
