/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: board library implementation based on Advantech Adam
// Change log:
//   2016-10-21: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------
package it.csi.periferico.boards.adam;

import java.util.List;

/**
 * Board library implementation based on Advantech Adam
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
class AcquisitionModule {

	private AdamModuleInfo moduleInfo;
	private int address;

	AcquisitionModule(AdamModuleInfo boardInfo, int address) {
		if (boardInfo == null)
			throw new IllegalArgumentException(
					"AdamModuleInfo should not be null");
		this.moduleInfo = boardInfo;
		this.address = address;
	}

	String getName() {
		return moduleInfo.getModelName();
	}

	int getAddress() {
		return address;
	}

	int getNumAI() {
		return moduleInfo.getNumAI();
	}

	int getNumAO() {
		return moduleInfo.getNumAO();
	}

	int getNumDI() {
		return moduleInfo.getNumDI();
	}

	int getNumDO() {
		return moduleInfo.getNumDO();
	}

	int getNumDIO() {
		return moduleInfo.getNumDIO();
	}

	List<AdamRange> getListRange() {
		return moduleInfo.getListRange();
	}

	AdamRange getRange(int rangeId) {
		for (AdamRange r : moduleInfo.getListRange())
			if (rangeId == r.getId())
				return r;
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}

}
