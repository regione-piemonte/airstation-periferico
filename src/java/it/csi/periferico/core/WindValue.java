/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: vectorial wind aggregate value
// Change log:
//   2008-07-04: initial version
// ----------------------------------------------------------------------------
// $Id: WindValue.java,v 1.4 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;

/**
 * Vectorial wind aggregate value
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class WindValue extends AggregateValue {

	private Double vectorialSpeed;

	private Double vectorialDirection;

	private Double standardDeviation;

	private Double scalarSpeed;

	private Double gustSpeed; // gust = raffica

	private Double gustDirection;

	private Double calmsNumberPercent;

	private Boolean calm;

	public WindValue(Date timestamp, Double vectorialSpeed,
			Double vectorialDirection, Double standardDeviation,
			Double scalarSpeed, Double gustSpeed, Double gustDirection,
			Double calmsNumberPercent, Boolean calm, boolean notvalid, int flags) {
		super(timestamp, notvalid, flags);
		this.vectorialSpeed = vectorialSpeed;
		this.vectorialDirection = vectorialDirection;
		this.standardDeviation = standardDeviation;
		this.scalarSpeed = scalarSpeed;
		this.gustSpeed = gustSpeed;
		this.gustDirection = gustDirection;
		this.calmsNumberPercent = calmsNumberPercent;
		this.calm = calm;
	}

	public Double getCalmsNumberPercent() {
		return calmsNumberPercent;
	}

	public Double getGustDirection() {
		return gustDirection;
	}

	public Double getGustSpeed() {
		return gustSpeed;
	}

	public Double getScalarSpeed() {
		return scalarSpeed;
	}

	public Double getStandardDeviation() {
		return standardDeviation;
	}

	public Double getVectorialDirection() {
		return vectorialDirection;
	}

	public Double getVectorialSpeed() {
		return vectorialSpeed;
	}

	public Boolean getCalm() {
		return calm;
	}

}
