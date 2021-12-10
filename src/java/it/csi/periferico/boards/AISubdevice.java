/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents an analog input subdevice
// Change log:
//   2008-TODO-TODO: initial version
// ----------------------------------------------------------------------------
// $Id: AISubdevice.java,v 1.4 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an analog input subdevice
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AISubdevice extends Subdevice {

	private static final long serialVersionUID = 2453421528522501588L;

	private List<AnalogInput> listAI = new ArrayList<AnalogInput>();

	public AISubdevice(Board board, int subdevice) {
		super(board, subdevice);
	}

	public List<AnalogInput> getListAI() {
		return listAI;
	}

	public void setListAI(List<AnalogInput> listAI) {
		this.listAI = listAI;
	}

	public AnalogInput getAI(int channel) {
		if (listAI == null)
			return null;
		for (AnalogInput ai : listAI)
			if (ai.getChannel() == channel)
				return ai;
		return null;
	}

	@Override
	public Channel getChannel(int channel) {
		return getAI(channel);
	}

}
