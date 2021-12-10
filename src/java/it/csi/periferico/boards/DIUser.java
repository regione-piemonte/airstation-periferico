/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface to be implemented by users of digital inputs 
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: DIUser.java,v 1.4 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Interface to be implemented by users of digital inputs
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public interface DIUser extends IOUser {

	public boolean deliver(DIEvent diEvent);

}
