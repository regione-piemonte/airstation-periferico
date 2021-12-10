/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: analyzer's identification
// Change log:
//   2008-09-29: initial version
// ----------------------------------------------------------------------------
// $Id: AnalyzerName.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Analyzer's identification
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AnalyzerName {

	private String brand;

	private String model;

	public AnalyzerName() {
		this(null, null);
	}

	public AnalyzerName(String brand, String model) {
		this.brand = brand;
		this.model = model;
	}

	public String getBrand() {
		return (brand);
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return (model);
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public boolean equals(Object obj) {
		String otherBrand = ((AnalyzerName) obj).getBrand();
		if (brand == null && otherBrand != null)
			return (false);
		if (brand != null && otherBrand == null)
			return (false);
		if (brand != null && otherBrand != null && !brand.equals(otherBrand))
			return (false);
		String otherModel = ((AnalyzerName) obj).getModel();
		if (model == null && otherModel != null)
			return (false);
		if (model != null && otherModel == null)
			return (false);
		if (model != null && otherModel != null)
			return (model.equals(otherModel));
		return (true);
	}

	@Override
	public int hashCode() {
		int hash = 0;
		if (brand != null)
			hash += brand.hashCode();
		if (model != null)
			hash += model.hashCode();
		return (hash);
	}

	@Override
	public String toString() {
		return brand + " - " + model;
	}
}
