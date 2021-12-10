/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for element configuration in data port drivers
// Change log:
//   2008-09-30: initial version
// ----------------------------------------------------------------------------
// $Id: ElementCfg.java,v 1.3 2015/05/14 15:15:44 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Base class for element configuration in data port drivers
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ElementCfg {

	private String parameterId = "";
	private Integer aggregationPeriod_m = null;

	public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}

	public Integer getAggregationPeriod_m() {
		return aggregationPeriod_m;
	}

	public void setAggregationPeriod_m(Integer aggregationPeriod_m) {
		this.aggregationPeriod_m = aggregationPeriod_m;
	}

	public boolean isDataValidSupported() {
		return false;
	}

}
