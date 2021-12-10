/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: board library implementation based on Advantech Adam
// Change log:
//   2016-10-26: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------
package it.csi.periferico.boards.adam;

import it.csi.periferico.acqdrivers.conn.net.NetworkConnection;
import it.csi.periferico.acqdrivers.conn.serial.DataBits;
import it.csi.periferico.acqdrivers.conn.serial.FlowControl;
import it.csi.periferico.acqdrivers.conn.serial.Parity;
import it.csi.periferico.acqdrivers.conn.serial.SerialConnection;
import it.csi.periferico.acqdrivers.conn.serial.StopBits;
import it.csi.periferico.boards.BoardsException;
import it.csi.periferico.boards.DriverParam;

import java.util.Map;

/**
 * Board library implementation based on Advantech Adam
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
class HandleFactory {

	private static final int PARAM_HOST = 0;
	private static final int PARAM_PORT = 1;
	private static final int PARAM_DEVICE = 0;
	private static final int PARAM_BAUDRATE = 1;
	private static final int PARAM_MAX_SCAN_ADDRESS = 2;

	static AdamHandle newInstance(String driverName,
			Map<Integer, DriverParam> mapParams) throws BoardsException {
		if ("serial_bus".equals(driverName))
			return new SerialBusHandle(createSerialConn(mapParams),
					parseMaxScanAddress(mapParams));
		if ("lan_bus".equals(driverName))
			return new LanBusHandle(createNetworkConn(mapParams),
					parseMaxScanAddress(mapParams));
		if ("lan_board".equals(driverName))
			return new LanBoardHandle(createNetworkConn(mapParams));
		throw new IllegalArgumentException("Cannot provide handle for "
				+ "unsupported board of type " + driverName);
	}

	private static NetworkConnection createNetworkConn(
			Map<Integer, DriverParam> mapParams) throws BoardsException {
		DriverParam dp;
		dp = mapParams.get(PARAM_HOST);
		if (dp == null)
			throw new BoardsException("Missing parameter 'host'");
		String host = dp.getValue();
		if (host == null)
			throw new BoardsException("Missing value for parameter "
					+ dp.getName());
		dp = mapParams.get(PARAM_PORT);
		if (dp == null)
			throw new BoardsException("Missing parameter 'port'");
		String strPort = dp.getValue();
		if (strPort == null)
			throw new BoardsException("Missing value for parameter "
					+ dp.getName());
		int port;
		try {
			port = Integer.parseInt(strPort);
		} catch (NumberFormatException e) {
			throw new BoardsException("Cannot parse value '" + strPort
					+ "' for parameter " + dp.getName());
		}
		return new NetworkConnection(host, port);
	}

	private static SerialConnection createSerialConn(
			Map<Integer, DriverParam> mapParams) throws BoardsException {
		DriverParam dp;
		dp = mapParams.get(PARAM_DEVICE);
		if (dp == null)
			throw new BoardsException("Missing parameter 'device'");
		String device = dp.getValue();
		if (device == null)
			throw new BoardsException("Missing value for parameter "
					+ dp.getName());
		dp = mapParams.get(PARAM_BAUDRATE);
		if (dp == null)
			throw new BoardsException("Missing parameter 'baudRate'");
		String strBaudRate = dp.getValue();
		if (strBaudRate == null)
			throw new BoardsException("Missing value for parameter "
					+ dp.getName());
		int baudRate;
		try {
			baudRate = Integer.parseInt(strBaudRate);
		} catch (NumberFormatException e) {
			throw new BoardsException("Cannot parse value '" + strBaudRate
					+ "' for parameter " + dp.getName());
		}
		String deviceFileName = device.startsWith("tty") ? ("/dev/" + device) : device;
		return new SerialConnection(deviceFileName, baudRate, FlowControl.NONE,
				DataBits._8, StopBits._1, Parity.NONE);
	}

	private static Integer parseMaxScanAddress(
			Map<Integer, DriverParam> mapParams) throws BoardsException {
		DriverParam dp = mapParams.get(PARAM_MAX_SCAN_ADDRESS);
		if (dp != null && dp.getValue() != null) {
			try {
				return Integer.parseInt(dp.getValue());
			} catch (NumberFormatException e) {
				throw new BoardsException("Cannot parse value '"
						+ dp.getValue() + "' for parameter " + dp.getName());
			}
		}
		return null;
	}

}
