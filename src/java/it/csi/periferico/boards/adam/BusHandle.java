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

import it.csi.periferico.acqdrivers.conn.Connection;
import it.csi.periferico.acqdrivers.conn.ConnectionClosedException;
import it.csi.periferico.boards.BoardsException;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Board library implementation based on Advantech Adam
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
abstract class BusHandle extends AdamHandle {

	private static Logger logger = Logger.getLogger("periferico.adam."
			+ BusHandle.class.getSimpleName());

	private Integer maxScanAddr = null;
	private Map<Integer, AcquisitionModule> mapModules;

	BusHandle(Connection connection, Integer maxScanAddr) {
		super(connection);
		mapModules = new LinkedHashMap<Integer, AcquisitionModule>();
		if (maxScanAddr == null || maxScanAddr <= 0 || maxScanAddr > 255)
			this.maxScanAddr = 255;
		else
			this.maxScanAddr = maxScanAddr;
		logger.debug("Maximum scan address for Adam bus set to " + maxScanAddr);
	}

	@Override
	void initImpl(Map<String, AdamModuleInfo> mapBoardInfo) throws IOException,
			ConnectionClosedException, InterruptedException, BoardsException {
		logger.info("Boards detection in progress...");
		mapModules.clear();
		long timeout = 200;
		int i = 0;
		while (i < maxScanAddr) {
			String addr = String.format("%02X", i);
			String reply = sendAndRead(i, "$", "M", timeout);
			if (reply == null) {
				i++;
				continue;
			}
			if (reply.startsWith("!")) {
				if (reply.length() >= 7) {
					String replyAddr = reply.substring(1, 3);
					String replyModel = reply.substring(3);
					if (replyAddr.equalsIgnoreCase(addr)) {
						AdamModuleInfo boardInfo = mapBoardInfo.get(replyModel);
						if (boardInfo == null)
							logger.warn("Unsupported board model " + replyModel);
						else
							mapModules.put(i, new AcquisitionModule(boardInfo,
									i));
						i++;
					} else {
						logger.warn("Address mismatch in reply quering address"
								+ " 0x" + addr + ": " + reply);
						Thread.sleep(timeout);
						clearBuffers();
						i--;
						logger.info("Restarting from address 0x"
								+ String.format("%02X", i));
					}
				} else {
					logger.warn("Incomplete reply quering address 0x" + addr
							+ ": " + reply);
					Thread.sleep(timeout);
					clearBuffers();
					logger.info("Restarting from address 0x"
							+ String.format("%02X", i));
				}
			} else {
				logger.warn("Unexpected reply quering address 0x" + addr + ": "
						+ reply);
				Thread.sleep(timeout);
				clearBuffers();
				i++;
			}
		}
		if (mapModules.isEmpty())
			throw new BoardsException("No supported boards detected");
		for (Integer address : mapModules.keySet())
			logger.info("Detected board " + mapModules.get(address).getName()
					+ " at address 0x" + String.format("%02X", address));
	}

	@Override
	Collection<Integer> getModuleAddresses() {
		return mapModules.keySet();
	}

	@Override
	AcquisitionModule getAcquisitionModule(Integer address) {
		return mapModules.get(address);
	}

}
