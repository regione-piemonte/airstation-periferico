/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface implemented by entities that own elements
// Change log:
//   2008-08-05: initial version
// ----------------------------------------------------------------------------
// $Id: ElementHolder.java,v 1.5 2013/11/15 15:52:48 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Interface implemented by entities that own elements
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface ElementHolder extends Holder {

	public boolean isFaultActive();

	public boolean isDataValidActive();

	public boolean isMaintenanceInProgress();

	public boolean isManualCalibrationRunning();

	public boolean isAutoCheckRunning();

	public boolean isEnvironmentOK();

	public boolean isDataValidWarningOnly();

	public void updateAutoCheckRunning(boolean value);

}
