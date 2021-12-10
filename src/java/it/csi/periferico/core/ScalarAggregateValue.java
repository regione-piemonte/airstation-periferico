/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for scalar aggregate data
// Change log:
//   2008-06-16: initial version
// ----------------------------------------------------------------------------
// $Id: ScalarAggregateValue.java,v 1.5 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Base class for scalar aggregate data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ScalarAggregateValue extends AggregateValue {

	private Double value;

	public ScalarAggregateValue(Date timestamp, Double value, boolean notvalid,
			int flags) {
		super(timestamp, notvalid, flags);
		this.value = value;
	}

	public Double getValue() {
		return value;
	}

}
