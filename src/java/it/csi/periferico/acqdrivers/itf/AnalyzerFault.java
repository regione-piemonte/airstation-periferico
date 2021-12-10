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
// $Id: AnalyzerFault.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Information for a parameter measured by a data port analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AnalyzerFault {

	private String name;
	private String id;
	private Integer code;
	private int minAcquisitionPeriod;

	public AnalyzerFault(String name, String id, Integer code,
			int minAcquisitionPeriod) {
		this.name = name;
		this.id = id;
		this.code = code;
		this.minAcquisitionPeriod = minAcquisitionPeriod;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public Integer getCode() {
		return code;
	}

	public int getMinAcquisitionPeriod() {
		return minAcquisitionPeriod;
	}

	public String printCode() {
		return code == null ? "" : ("0x" + Integer.toHexString(code));
	}

	@Override
	public String toString() {
		return "AnalyzerFault [name=" + name + ", id=" + id + ", code="
				+ printCode() + ", minAcquisitionPeriod="
				+ minAcquisitionPeriod + "]";
	}

}