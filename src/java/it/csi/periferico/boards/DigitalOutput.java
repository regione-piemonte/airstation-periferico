/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents an acquisition board's digital output
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: DigitalOutput.java,v 1.8 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Represents an acquisition board's digital output
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DigitalOutput extends Channel {

	private static final long serialVersionUID = -3458800549170397033L;

	public DigitalOutput(DOSubdevice subdevice, int channel) {
		super(subdevice, channel);
	}

}
