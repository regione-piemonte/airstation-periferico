/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: collects binary status
// Change log:
//   2008-07-08: initial version
// ----------------------------------------------------------------------------
// $Id: BinaryStatusCollector.java,v 1.3 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects binary status
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class BinaryStatusCollector {

	private List<BinaryStatus> listBinaryStatus = new ArrayList<BinaryStatus>();

	private BinaryStatus lastBinaryStatus = null;

	public BinaryStatus getLast() {
		return lastBinaryStatus;
	}

	synchronized void setLast(BinaryStatus bs) {
		lastBinaryStatus = bs;
	}

	synchronized void add(BinaryStatus bs) {
		lastBinaryStatus = bs;
		listBinaryStatus.add(bs);
	}

	synchronized List<BinaryStatus> getAllAndClean() {
		List<BinaryStatus> tmpList = listBinaryStatus;
		listBinaryStatus = new ArrayList<BinaryStatus>();
		return tmpList;
	}

}
