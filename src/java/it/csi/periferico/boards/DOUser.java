/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface to be implemented by users of digital outputs 
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: DOUser.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Interface to be implemented by users of digital outputs
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public interface DOUser extends IOUser {

	public boolean getValue();

	public boolean getLastWriteStatus();

	public void setLastWriteStatus(boolean success);

}
