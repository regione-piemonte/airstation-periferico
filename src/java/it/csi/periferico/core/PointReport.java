/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: calibration point report (this class is a draft for future
//                  implementation)
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: PointReport.java,v 1.4 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Calibration point report (this class is a draft for future implementation)
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class PointReport {

	private SampleElement element;

	private Date timestamp;

	private Double readValue;

	private double cylinderValue;

	private int appliedThreshold;

	private CheckReport.Status status;

	public SampleElement getElement() {
		return element;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Double getReadValue() {
		return readValue;
	}

	public double getCylinderValue() {
		return cylinderValue;
	}

	public int getAppliedThreshold() {
		return appliedThreshold;
	}

	public CheckReport.Status getStatus() {
		return status;
	}

}
