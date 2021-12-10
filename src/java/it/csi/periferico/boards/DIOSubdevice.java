/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents an analog bidirectional input/output subdevice
// Change log:
//   2008-04-24: initial version
// ----------------------------------------------------------------------------
// $Id: DIOSubdevice.java,v 1.5 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an analog bidirectional input/output subdevice
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DIOSubdevice extends Subdevice {

	private static final long serialVersionUID = -2141839561618788490L;

	public DIOSubdevice(Board board, int subdevice) {
		super(board, subdevice);
	}

	private List<DigitalIO> listDIO = new ArrayList<DigitalIO>();

	public List<DigitalIO> getListDIO() {
		return listDIO;
	}

	public void setListDIO(List<DigitalIO> listDIO) {
		this.listDIO = listDIO;
	}

	public DigitalIO getDIO(int channel) {
		if (listDIO == null)
			return null;
		for (DigitalIO dio : listDIO)
			if (dio.getChannel() == channel)
				return dio;
		return null;
	}

	@Override
	public Channel getChannel(int channel) {
		return getDIO(channel);
	}

}
