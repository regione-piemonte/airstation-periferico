/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: Dummy aggregate value, used to store aggregation time,
//                  when no data is available for aggregation
// Change log:
//   2009-07-06: initial version
// ----------------------------------------------------------------------------
// $Id: DummyAggregateValue.java,v 1.1 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Dummy aggregate value, used to store aggregation time, when no data is
 * available for aggregation
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DummyAggregateValue extends AggregateValue {

	public DummyAggregateValue(Date timestamp) {
		super(timestamp, true, ValidationFlag.MISSING_DATA);
	}

}
