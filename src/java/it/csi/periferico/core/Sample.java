/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a sample
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: Sample.java,v 1.8 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Represents a sample
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Sample extends Value {

	private Double value;

	private int flags;

	private boolean notvalid;

	public Sample(Date timestamp, SampleValues sampleValues, boolean notvalid,
			int flags) {
		this(timestamp, sampleValues == null ? null : sampleValues
				.getFinalValue(), notvalid, flags);
	}

	public Sample(Date timestamp, Double value, boolean notvalid, int flags) {
		super(timestamp);
		this.value = value;
		this.notvalid = notvalid;
		this.flags = flags;
	}

	public int getFlags() {
		return flags;
	}

	public Double getValue() {
		return value;
	}

	public boolean isNotvalid() {
		return notvalid;
	}

}
