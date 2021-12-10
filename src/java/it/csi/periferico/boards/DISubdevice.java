/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a digital input subdevice
// Change log:
//   2008-04-24: initial version
// ----------------------------------------------------------------------------
// $Id: DISubdevice.java,v 1.4 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a digital input subdevice
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DISubdevice extends Subdevice {

	private static final long serialVersionUID = 7317610372016311736L;

	private List<DigitalInput> listDI = new ArrayList<DigitalInput>();

	public DISubdevice(Board board, int subdevice) {
		super(board, subdevice);
	}

	public List<DigitalInput> getListDI() {
		return listDI;
	}

	public void setListDI(List<DigitalInput> listDI) {
		this.listDI = listDI;
	}

	public DigitalInput getDI(int channel) {
		if (listDI == null)
			return null;
		for (DigitalInput di : listDI)
			if (di.getChannel() == channel)
				return di;
		return null;
	}

	@Override
	public Channel getChannel(int channel) {
		return getDI(channel);
	}

}
