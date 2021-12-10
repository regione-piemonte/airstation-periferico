/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for classes that provide an input or output
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: IOProvider.java,v 1.5 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Interface for classes that provide an input or output
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public interface IOProvider {

	public void bindIOUser(IOUser ioUser) throws BoardsException;

	public void unbind();

	public boolean isBinded();

	public IOUser getBindedIOUser();

}
