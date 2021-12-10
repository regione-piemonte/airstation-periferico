/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file:
//   Normalizer for volume-to-volume to mass-per-volume concentrations
// Change log:
//   2008-10-29: initial version
// ----------------------------------------------------------------------------
// $Id: V2VToMassPerVolumeNormalizer.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common.converters;

/**
 * Normalizer for volume-to-volume to mass-per-volume concentrations
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class V2VToMassPerVolumeNormalizer extends LinearNormalizer {

	private static final long serialVersionUID = -6308370477258331377L;

	// V0 [l/mole]: volume occupied by 1 mole at 0°C (273 K) and at 101.3 kPa
	private static final double V0_lpm = 22.41;

	private static final double T0_K = 273.0; // reference temperature [K]

	private static final double P0_kPa = 101.3; // reference pressure [kPa]

	// volume-to-volume to mass-per-volume coefficient
	private double v2v_To_mpv_coefficient;

	/**
	 * @param conversionMultiplyer
	 *            multiplyer for normalization
	 * 
	 * @param conversionAddendum
	 *            addendum for normalization
	 * 
	 * @param molecularWeight_gpm
	 *            molecular weight [grams/mole]
	 * 
	 * @param temperature_K
	 *            current temperature [K]
	 * 
	 * @param pressure_kPa
	 *            current pressure [kPa]
	 */
	public V2VToMassPerVolumeNormalizer(double conversionMultiplyer,
			double conversionAddendum, double molecularWeight_gpm,
			double temperature_K, double pressure_kPa) {
		super(conversionMultiplyer, conversionAddendum);
		if (molecularWeight_gpm <= 0)
			throw new IllegalArgumentException(
					"Molecular weight [grams/mole] must be a positive value");
		if (temperature_K <= 0)
			throw new IllegalArgumentException(
					"Temperature [K] must be a positive value");
		if (pressure_kPa <= 0)
			throw new IllegalArgumentException(
					"Pressure [kPa] must be a positive value");
		v2v_To_mpv_coefficient = (molecularWeight_gpm / V0_lpm)
				* (T0_K / temperature_K) * (pressure_kPa / P0_kPa);
	}

	@Override
	public double normalize(double value_v2v) {
		double normalizedValue_v2v = super.normalize(value_v2v);
		double normalizedValue_mpv = normalizedValue_v2v
				* v2v_To_mpv_coefficient;
		return normalizedValue_mpv;
	}

	@Override
	public double denormalize(double normalizedValue_mpv) {
		double normalizedValue_v2v = normalizedValue_mpv
				/ v2v_To_mpv_coefficient;
		double value_v2v = super.denormalize(normalizedValue_v2v);
		return value_v2v;
	}

}
