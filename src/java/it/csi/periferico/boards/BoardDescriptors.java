/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: manages a collection of board descriptors
// Change log:
//   2008-04-10: initial version
// ----------------------------------------------------------------------------
// $Id: BoardDescriptors.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages a collection of board descriptors
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public class BoardDescriptors {

	private boolean comediEnabled = true;
	private boolean adamEnabled = false;
	private List<BoardDescriptor> list = new ArrayList<BoardDescriptor>();

	public boolean isComediEnabled() {
		return comediEnabled;
	}

	public void setComediEnabled(boolean comediEnabled) {
		this.comediEnabled = comediEnabled;
	}

	public boolean isAdamEnabled() {
		return adamEnabled;
	}

	public void setAdamEnabled(boolean adamEnabled) {
		this.adamEnabled = adamEnabled;
	}

	public List<BoardDescriptor> getList() {
		return list;
	}

	public void setList(List<BoardDescriptor> list) {
		this.list = list;
	}

	public BoardDescriptor get(String brand, String model) {
		if (brand == null || model == null)
			return null;
		for (BoardDescriptor bd : list) {
			if (brand.equals(bd.getBrand()) && model.equals(bd.getModel()))
				return bd;
		}
		return null;
	}

	private boolean isUsable(BoardDescriptor bDesc) {
		return comediEnabled && BoardLibItf.COMEDI.equals(bDesc.getLibName())
				|| adamEnabled && BoardLibItf.ADAM.equals(bDesc.getLibName());
	}

	// TODO: continuare... (magari sostituire le stringhe "comedi" con una
	// static final...)

	public PCIBoardDescriptor get(int vendorId, int deviceId) {
		if (list == null)
			return null;
		for (BoardDescriptor bd : list) {
			if (bd instanceof PCIBoardDescriptor) {
				PCIBoardDescriptor pciBd = (PCIBoardDescriptor) bd;
				if (pciBd.getVendorId() == vendorId
						&& pciBd.getDeviceId() == deviceId)
					return pciBd;
			}
		}
		return null;
	}

	public List<String> getBrands() {
		return getBrands(false);
	}

	public List<String> getBrands(boolean excludePCI) {
		Set<String> set = new HashSet<String>();
		for (BoardDescriptor bd : list) {
			if (excludePCI && bd instanceof PCIBoardDescriptor)
				continue;
			if (isUsable(bd))
				set.add(bd.getBrand());
		}
		List<String> listBrands = new ArrayList<String>(set);
		java.util.Collections.sort(listBrands);
		return listBrands;
	}

	public List<String> getModels(String brand) {
		return getModels(brand, false);
	}

	public List<String> getModels(String brand, boolean excludePCI) {
		Set<String> set = new HashSet<String>();
		if (brand != null)
			for (BoardDescriptor bd : list) {
				if (excludePCI && bd instanceof PCIBoardDescriptor)
					continue;
				if (isUsable(bd) && brand.equals(bd.getBrand()))
					set.add(bd.getModel());
			}
		List<String> listModels = new ArrayList<String>(set);
		java.util.Collections.sort(listModels);
		return listModels;
	}

}
