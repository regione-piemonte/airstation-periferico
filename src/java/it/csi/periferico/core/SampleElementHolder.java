/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface implemented by entities that own sample elements
// Change log:
//   2008-04-30: initial version
// ----------------------------------------------------------------------------
// $Id: SampleElementHolder.java,v 1.7 2011/12/23 09:34:56 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Interface implemented by entities that own sample elements
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface SampleElementHolder extends ElementHolder {

	public double getMaxVoltage();

	public double getMinVoltage();

	public boolean isDifferentialModeNeeded();

	public boolean getMinRangeExtension();

	public boolean getMaxRangeExtension();

}
