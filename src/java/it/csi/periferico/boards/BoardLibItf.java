/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for a library that manages acquisition boards
// Change log:
//   2008-04-17: initial version
// ----------------------------------------------------------------------------
// $Id: BoardLibItf.java,v 1.8 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Interface for a library that manages acquisition boards
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface BoardLibItf {

	public static final String COMEDI = "comedi";
	public static final String ADAM = "adam";

	public void init() throws BoardsException;

	public void cleanup() throws BoardsException;

	public boolean isSupported(Board board);

	public void dismissBoard(Board board) throws BoardsException;

	public void initBoard(Board board, BoardDescriptor desc)
			throws BoardsException;

	public void startAcquisition(Board board);

}
