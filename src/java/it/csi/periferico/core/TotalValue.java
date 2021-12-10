/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: total value data
// Change log:
//   2008-06-16: initial version
// ----------------------------------------------------------------------------
// $Id: TotalValue.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Total value data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class TotalValue extends ScalarAggregateValue {

	public TotalValue(Date timestamp, Double value, boolean notvalid, int flags) {
		super(timestamp, value, notvalid, flags);
	}

}
