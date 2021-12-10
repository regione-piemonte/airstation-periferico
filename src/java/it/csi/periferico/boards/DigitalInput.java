/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents an acquisition board's digital input
// Change log:
//   2008-MM-DD: initial version
// ----------------------------------------------------------------------------
// $Id: DigitalInput.java,v 1.9 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Represents an acquisition board's digital input
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class DigitalInput extends Channel {

	private static final long serialVersionUID = 8954621928361226292L;

	private Boolean lastValue = null;

	public DigitalInput(DISubdevice subdevice, int channel) {
		super(subdevice, channel);
	}

	public Boolean getLastValue() {
		return lastValue;
	}

	public void setLastValue(Boolean lastValue) {
		this.lastValue = lastValue;
	}

}
