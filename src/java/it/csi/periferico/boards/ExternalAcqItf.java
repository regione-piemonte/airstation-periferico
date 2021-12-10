/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for data acquisition library
// Change log:
//   2016-10-31: initial version
// ----------------------------------------------------------------------------
// $Id: BoardLibItf.java,v 1.8 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Interface for data acquisition library
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface ExternalAcqItf {

	public AIValue readAI(Board board, AISubdevice aiSubdevice, AnalogInput ai);

	public DIEvent readDI(Board board, DISubdevice diSubdevice, DigitalInput di);

	public DIEvent readDIO(Board board, DIOSubdevice dioSubdevice, DigitalIO dio);

	public boolean writeDO(Board board, DOSubdevice doSubdevice,
			DigitalOutput dout, boolean value);

	public boolean writeDIO(Board board, DIOSubdevice doSubdevice,
			DigitalIO dio, boolean value);

}
