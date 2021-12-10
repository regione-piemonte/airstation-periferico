/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: board library implementation based on Advantech Adam
// Change log:
//   2016-10-17: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------
package it.csi.periferico.boards.adam;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.List;

/**
 * Board library implementation based on Advantech Adam
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AdamModuleInfo extends ConfigItem {

	private static final long serialVersionUID = 6084159575128962238L;
	private String modelName;
	private int numAI = 0;
	private int numAO = 0;
	private int numDI = 0;
	private int numDO = 0;
	private int numDIO = 0;
	private List<AdamRange> listRange;

	public AdamModuleInfo(String modelName, int numAI, int numAO, int numDI,
			int numDO, int numDIO) {
		this.modelName = modelName;
		this.numAI = numAI;
		this.numAO = numAO;
		this.numDI = numDI;
		this.numDO = numDO;
		this.numDIO = numDIO;
	}

	public AdamModuleInfo() {
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public int getNumAI() {
		return numAI;
	}

	public void setNumAI(int numAI) {
		this.numAI = numAI;
	}

	public int getNumAO() {
		return numAO;
	}

	public void setNumAO(int numAO) {
		this.numAO = numAO;
	}

	public int getNumDI() {
		return numDI;
	}

	public void setNumDI(int numDI) {
		this.numDI = numDI;
	}

	public int getNumDO() {
		return numDO;
	}

	public void setNumDO(int numDO) {
		this.numDO = numDO;
	}

	public int getNumDIO() {
		return numDIO;
	}

	public void setNumDIO(int numDIO) {
		this.numDIO = numDIO;
	}

	public List<AdamRange> getListRange() {
		return listRange;
	}

	public void setListRange(List<AdamRange> listRange) {
		this.listRange = listRange;
	}

	@Override
	public void checkConfig() throws ConfigException {
	}

}
