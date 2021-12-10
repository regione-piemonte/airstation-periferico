/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: converts a value from a measure unit to another compatible
//                  measure unit
// Change log:
//   2008-10-29: initial version
// ----------------------------------------------------------------------------
// $Id: Converter.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common.converters;

import it.csi.periferico.config.common.MeasureUnit;
import it.csi.periferico.config.common.Parameter;
import it.csi.periferico.config.common.Standards;

import java.io.Serializable;

/**
 * Converts a value from a measure unit to another compatible measure unit
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class Converter implements Serializable {

	private static final long serialVersionUID = -2436713067674028511L;

	private Normalizer srcNormalizer;

	private Normalizer destNormalizer;

	public Converter(MeasureUnit srcMeasureUnit, MeasureUnit destMeasureUnit,
			Parameter parameter, Standards standards)
			throws MolecularWeightMissingException {
		if (srcMeasureUnit == null)
			throw new IllegalArgumentException(
					"Source measure unit cannot be null");
		if (destMeasureUnit == null)
			throw new IllegalArgumentException(
					"Destination measure unit cannot be null");
		if (parameter == null)
			throw new IllegalArgumentException("Parameter cannot be null");
		if (standards == null)
			throw new IllegalStateException("Reference values missing");
		if (!areEqual(srcMeasureUnit.getPhysicalDimension(),
				destMeasureUnit.getPhysicalDimension())) {
			throw new IllegalArgumentException("Source and destination measure"
					+ " units must be of the same physical dimension: "
					+ srcMeasureUnit.getPhysicalDimension() + " != "
					+ destMeasureUnit.getPhysicalDimension());
		}
		boolean forceLinear = areEqual(srcMeasureUnit.getConversionFormula(),
				destMeasureUnit.getConversionFormula());
		srcNormalizer = getNormalizer(srcMeasureUnit, parameter, standards,
				forceLinear);
		destNormalizer = getNormalizer(destMeasureUnit, parameter, standards,
				forceLinear);
	}

	public double convert(double value) {
		double normalizedValue = srcNormalizer.normalize(value);
		return destNormalizer.denormalize(normalizedValue);
	}

	public static boolean isConversionFormulaSupported(String name) {
		if (name == null)
			return true;
		name = name.trim();
		if (name.isEmpty())
			return true;
		if (name.equalsIgnoreCase("vv2mpv"))
			return true;
		return false;
	}

	public double getConversionCoefficient() {
		return convert(1.0);
	}

	private Normalizer getNormalizer(MeasureUnit measureUnit,
			Parameter parameter, Standards standards, boolean forceLinear)
			throws MolecularWeightMissingException {
		String conversionFormula = measureUnit.getConversionFormula();
		if (forceLinear || areEqual("", measureUnit.getConversionFormula())) {
			return new LinearNormalizer(measureUnit.getConversionMultiplyer(),
					measureUnit.getConversionAddendum());
		} else if ("vv2mpv".equalsIgnoreCase(conversionFormula.trim())) {
			if (standards.getReferencePressure_kPa() == null)
				throw new IllegalStateException(
						"Reference pressure missing for vv2mpv formula");
			else if (standards.getReferenceTemperature_K() == null)
				throw new IllegalStateException(
						"Reference temperature missing for vv2mpv formula");
			if (parameter.getMolecularWeight() == null)
				throw new MolecularWeightMissingException(
						"Molecular weight not specified for parameter "
								+ parameter.getId() + ": the conversion "
								+ "formula vv2mpv cannot be used");
			return new V2VToMassPerVolumeNormalizer(
					measureUnit.getConversionMultiplyer(),
					measureUnit.getConversionAddendum(),
					parameter.getMolecularWeight(),
					standards.getReferenceTemperature_K(),
					standards.getReferencePressure_kPa());
		} else {
			throw new IllegalStateException("Unsupported conversion formula: "
					+ conversionFormula);
		}
	}

	private boolean areEqual(String str1, String str2) {
		if (str1 == null)
			str1 = "";
		if (str2 == null)
			str2 = "";
		str1 = str1.trim();
		str2 = str2.trim();
		return str1.equals(str2);
	}

}
