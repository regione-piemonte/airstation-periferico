/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: binary status information associated with an integer code 
// Change log:
//   2008-10-08: initial version
// ----------------------------------------------------------------------------
// $Id: IntegerStatus.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Binary status information associated with an integer code
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class IntegerStatus extends BinaryStatus {

	private Integer value;

	public IntegerStatus(Date timestamp, Integer value, boolean status) {
		super(timestamp, status);
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}

}
