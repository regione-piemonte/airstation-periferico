/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface to be implemented by users of analog inputs
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: AIUser.java,v 1.8 2011/12/23 09:34:57 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Interface to be implemented by users of analog inputs
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public interface AIUser extends IOUser {

	public double getMaxVoltage();

	public double getMinVoltage();

	public boolean isDifferentialModeNeeded();

	public boolean getMinRangeExtension();

	public boolean getMaxRangeExtension();

	public double getStartOfScale();

	public double getEndOfScale();

	public int getAcqPeriod();

	public boolean deliver(AIValue aiValue);

	public boolean isCalibActive();

}
