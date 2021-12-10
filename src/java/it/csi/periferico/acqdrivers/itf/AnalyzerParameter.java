/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: information for a parameter measured by a data port analyzer
// Change log:
//   2015-04-09: initial version
// ----------------------------------------------------------------------------
// $Id: AnalyzerParameter.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Information for a parameter measured by a data port analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AnalyzerParameter {

	private String name;
	private String id;
	private Double minValue;
	private Double maxValue;
	private int minAcquisitionPeriod;
	private boolean processParameter;

	public AnalyzerParameter(String name, String id, Double minValue,
			Double maxValue, int minAcquisitionPeriod, boolean processParameter) {
		this.name = name;
		this.id = id;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.minAcquisitionPeriod = minAcquisitionPeriod;
		this.processParameter = processParameter;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public Double getMinValue() {
		return minValue;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public int getMinAcquisitionPeriod() {
		return minAcquisitionPeriod;
	}

	public boolean isProcessParameter() {
		return processParameter;
	}

	@Override
	public String toString() {
		return "AnalyzerParameter [name=" + name + ", id=" + id + ", minValue="
				+ minValue + ", maxValue=" + maxValue
				+ ", minAcquisitionPeriod=" + minAcquisitionPeriod
				+ ", processParameter=" + processParameter + "]";
	}

}
