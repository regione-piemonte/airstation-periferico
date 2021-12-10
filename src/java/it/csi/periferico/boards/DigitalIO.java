/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents an acquisition board's bidirectional digital
//                  input / output
// Change log:
//   2008-04-24: initial version
// ----------------------------------------------------------------------------
// $Id: DigitalIO.java,v 1.6 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Represents an acquisition board's bidirectional digital input / output
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DigitalIO extends Channel {

	private static final long serialVersionUID = 4657671613450963408L;

	private Boolean lastValue = null;

	public DigitalIO(DIOSubdevice subdevice, int channel) {
		super(subdevice, channel);
	}

	public Boolean getLastValue() {
		return lastValue;
	}

	public void setLastValue(Boolean lastValue) {
		this.lastValue = lastValue;
	}

}
