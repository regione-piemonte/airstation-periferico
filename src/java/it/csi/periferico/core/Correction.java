/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: stores coefficient and offset for linear correction
// Change log:
//   2008-11-7: initial version
// ----------------------------------------------------------------------------
// $Id: Correction.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Stores coefficient and offset for linear correction
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class Correction {

	private Double coefficient = null;

	private double offset;

	public Correction(double offset) {
		this.offset = offset;
	}

	public Correction(double coefficient, double offset) {
		this.coefficient = coefficient;
		this.offset = offset;
	}

	public Double getCoefficient() {
		return coefficient;
	}

	public double getOffset() {
		return offset;
	}

}
