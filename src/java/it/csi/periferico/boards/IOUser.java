/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface to be implemented by users of inputs / outputs
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: IOUser.java,v 1.8 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Interface to be implemented by users of inputs / outputs
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public interface IOUser {

	public String getBindLabel();

	public String getBindIdentifier();

	public IOProvider getIOProvider();

	public void setIOProvider(IOProvider ioProvider);

	public boolean isActive();

	public BoardBindInfo getBoardBindInfo();

	public void setBoardBindInfo(BoardBindInfo boardBindInfo);

}
