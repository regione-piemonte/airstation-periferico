/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for normalization
// Change log:
//   2008-10-29: initial version
// ----------------------------------------------------------------------------
// $Id: Normalizer.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common.converters;

import java.io.Serializable;

/**
 * Base class for normalization
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class Normalizer implements Serializable {

	private static final long serialVersionUID = -5752671083343018153L;

	public abstract double normalize(double value);

	public abstract double denormalize(double value);

}
