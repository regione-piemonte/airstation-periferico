/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface with specific functions for elements that
// compute average values
// Change log:
//   2015-05-08: initial version
// ----------------------------------------------------------------------------
// $Id: AvgElementItf.java,v 1.1 2015/05/08 10:54:44 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.core;

/**
 * Interface with specific functions for elements that
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface AvgElementItf {

	public int getAcqDelay();

	public void setAcqDelay(int acqDelay);

	public int getAcqDuration();

	public void setAcqDuration(int acqDuration);

	public int getAvgPeriod();

	public void setAvgPeriod(int avgPeriod);

}
