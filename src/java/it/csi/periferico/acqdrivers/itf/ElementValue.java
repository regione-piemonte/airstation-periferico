/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a value acquired for an element, with timestamp
// Change log:
//   2008-10-07: initial version
// ----------------------------------------------------------------------------
// $Id: ElementValue.java,v 1.2 2015/05/22 16:34:59 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

import java.util.Date;

/**
 * Represents a value acquired for an element, with timestamp
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ElementValue {

	private Date timestamp;
	private Double value;
	private int period;
	private boolean valid;
	private boolean calibration;

	public ElementValue(Date timestamp) {
		this(timestamp, 0, null, false, false);
	}

	public ElementValue(Date timestamp, Double value) {
		this(timestamp, 0, value, true, false);
	}

	public ElementValue(Date timestamp, Double value, boolean valid) {
		this(timestamp, 0, value, valid, false);
	}

	public ElementValue(Date timestamp, Double value, boolean valid,
			boolean calibration) {
		this(timestamp, 0, value, valid, calibration);
	}

	public ElementValue(Date timestamp, int period, Double value, boolean valid) {
		this(timestamp, period, value, valid, false);
	}

	public ElementValue(Date timestamp, int period, Double value,
			boolean valid, boolean calibration) {
		this.timestamp = timestamp;
		this.period = period;
		this.value = value;
		this.valid = valid;
		this.calibration = calibration;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Double getValue() {
		return value;
	}

	public boolean getError() {
		return value == null;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isCalibration() {
		return calibration;
	}

	public int getPeriod() {
		return period;
	}

}
