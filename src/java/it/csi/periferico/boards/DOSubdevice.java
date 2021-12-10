/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents an analog output subdevice
// Change log:
//   2008-04-29: initial version
// ----------------------------------------------------------------------------
// $Id: DOSubdevice.java,v 1.4 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an analog output subdevice
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DOSubdevice extends Subdevice {

	private static final long serialVersionUID = 2166755669101304667L;

	private List<DigitalOutput> listDO = new ArrayList<DigitalOutput>();

	public DOSubdevice(Board board, int subdevice) {
		super(board, subdevice);
	}

	public List<DigitalOutput> getListDO() {
		return listDO;
	}

	public void setListDO(List<DigitalOutput> listDO) {
		this.listDO = listDO;
	}

	public DigitalOutput getDI(int channel) {
		if (listDO == null)
			return null;
		for (DigitalOutput dout : listDO)
			if (dout.getChannel() == channel)
				return dout;
		return null;
	}

	@Override
	public Channel getChannel(int channel) {
		return getDI(channel);
	}

}
