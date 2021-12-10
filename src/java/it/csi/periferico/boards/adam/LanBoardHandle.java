/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: board library implementation based on Advantech Adam
// Change log:
//   2016-10-12: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------
package it.csi.periferico.boards.adam;

import it.csi.periferico.acqdrivers.conn.ConnectionClosedException;
import it.csi.periferico.acqdrivers.conn.net.NetworkConnection;
import it.csi.periferico.boards.BoardsException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Board library implementation based on Advantech Adam
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
class LanBoardHandle extends AdamHandle {

	private static Logger logger = Logger.getLogger("periferico.adam."
			+ LanBoardHandle.class.getSimpleName());
	private static final int DEFAULT_ADDR = 1;

	private AcquisitionModule acquisitionModule = null;
	private List<Integer> listAddresses = new ArrayList<Integer>();
	private String handleName;

	LanBoardHandle(NetworkConnection connection) {
		super(connection);
		handleName = "adam_lan_board://" + connection;
		listAddresses.add(DEFAULT_ADDR);
	}

	@Override
	void initImpl(Map<String, AdamModuleInfo> mapModuleInfo)
			throws IOException, ConnectionClosedException,
			InterruptedException, BoardsException {
		logger.info("Board detection in progress...");
		acquisitionModule = null;
		String replyModel = sendAndParse(DEFAULT_ADDR, "$", "M", "!", true);
		logger.info("Detected board '" + replyModel + "'");
		AdamModuleInfo boardInfo = mapModuleInfo.get(replyModel);
		if (boardInfo == null)
			throw new BoardsException("Unsupported board model " + replyModel);
		acquisitionModule = new AcquisitionModule(boardInfo, DEFAULT_ADDR);
	}

	@Override
	Collection<Integer> getModuleAddresses() {
		return listAddresses;
	}

	@Override
	AcquisitionModule getAcquisitionModule(Integer address) {
		return address == DEFAULT_ADDR ? acquisitionModule : null;
	}

	@Override
	public String toString() {
		return handleName;
	}

}
