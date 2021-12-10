/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface implemented by entities that own data port
//                  elements
// Change log:
//   2008-10-09: initial version
// ----------------------------------------------------------------------------
// $Id: DataPortElementHolder.java,v 1.4 2015/08/21 11:58:09 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Interface implemented by entities that own data port elements
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface DataPortElementHolder extends ElementHolder {

	public String getBrand();

	public String getModel();

	public Boolean getFaultActive();

	public boolean isConnectionUp();

}
