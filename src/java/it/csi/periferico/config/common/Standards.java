/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: standard reference values
// Change log:
//   2008-10-28: initial version
// ----------------------------------------------------------------------------
// $Id: Standards.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common;

/**
 * Standard reference values
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public class Standards {

	private Double referenceTemperature_K;

	private Double referencePressure_kPa;

	public Double getReferencePressure_kPa() {
		return referencePressure_kPa;
	}

	public void setReferencePressure_kPa(Double referencePressure_kPa) {
		this.referencePressure_kPa = referencePressure_kPa;
	}

	public Double getReferenceTemperature_K() {
		return referenceTemperature_K;
	}

	public void setReferenceTemperature_K(Double referenceTemperature_K) {
		this.referenceTemperature_K = referenceTemperature_K;
	}

}
