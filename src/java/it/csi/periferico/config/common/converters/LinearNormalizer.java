/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: normalization between measure units of the same type
// Change log:
//   2008-10-29: initial version
// ----------------------------------------------------------------------------
// $Id: LinearNormalizer.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common.converters;

/**
 * Normalization between measure units of the same type
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class LinearNormalizer extends Normalizer {

	private static final long serialVersionUID = -3079776373551037956L;

	private double conversionMultiplyer;

	private double conversionAddendum;

	/**
	 * @param conversionMultiplyer
	 *            multiplyer for normalization
	 * 
	 * @param conversionAddendum
	 *            addendum for normalization
	 */
	public LinearNormalizer(double conversionMultiplyer,
			double conversionAddendum) {
		if (conversionMultiplyer == 0)
			throw new IllegalArgumentException(
					"Conversion multiplyer cannot be 0");
		this.conversionMultiplyer = conversionMultiplyer;
		this.conversionAddendum = conversionAddendum;
	}

	@Override
	public double normalize(double value) {
		return value * conversionMultiplyer + conversionAddendum;
	}

	@Override
	public double denormalize(double value) {
		return (value - conversionAddendum) / conversionMultiplyer;
	}

}
