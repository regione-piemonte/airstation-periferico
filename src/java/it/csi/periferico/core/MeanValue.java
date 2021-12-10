/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: mean value data
// Change log:
//   2008-06-16: initial version
// ----------------------------------------------------------------------------
// $Id: MeanValue.java,v 1.4 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Mean value data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class MeanValue extends ScalarAggregateValue {

	public MeanValue(Date timestamp, Double value, boolean notvalid, int flags) {
		super(timestamp, value, notvalid, flags);
	}

}
