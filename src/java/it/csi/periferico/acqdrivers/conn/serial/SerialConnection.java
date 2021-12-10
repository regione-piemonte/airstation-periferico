/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: serial connection for data port analyzers
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: SerialConnection.java,v 1.6 2015/11/19 18:41:11 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.conn.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

import it.csi.periferico.acqdrivers.conn.Chunk;
import it.csi.periferico.acqdrivers.conn.Connection;
import it.csi.periferico.acqdrivers.conn.ConnectionException;

/**
 * Serial connection for data port analyzers
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class SerialConnection extends Connection {

	private static final int SERIAL_PORT_CLOSE_WAIT = 3500;
	private static final int CHUNK_BUFFER_SIZE = 256;

	private Logger logger;
	private SerialPort port = null;
	private int baudRate;
	private DataBits dataBits;
	private StopBits stopBits;
	private Parity parity;
	private FlowControl flowControl;
	private String serialDeviceName;
	private InputStream in;
	private OutputStream out;

	public SerialConnection(String serialDeviceName, int baudRate, FlowControl flowControl) {
		this(serialDeviceName, baudRate, flowControl, DataBits._8, StopBits._1, Parity.NONE);
	}

	public SerialConnection(String serialDeviceName, int baudRate, SerialParams params) {
		this(serialDeviceName, baudRate, params.getFlowControl(), params.getDataBits(), params.getStopBits(),
				params.getParity());
	}

	public SerialConnection(String serialDeviceName, int baudRate, FlowControl flowControl, DataBits dataBits,
			StopBits stopBits, Parity parity) {
		logger = Logger.getLogger("periferico.conn." + getClass().getSimpleName() + "-" + getInstanceCount());
		this.serialDeviceName = serialDeviceName;
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		this.flowControl = flowControl;
	}

	@Override
	protected void openImpl() throws SerialPortInvalidPortException, IOException, ConnectionException {
		logger.debug("Opening serial port '" + serialDeviceName + "'...");
		if (port != null) {
			logger.warn("Serial port '" + serialDeviceName + "' was left open, closing now");
			closeImpl();
		}
		SerialPort tmpPort = SerialPort.getCommPort(serialDeviceName);
		if (!tmpPort.openPort())
			throw new ConnectionException("Error opening serial port '" + serialDeviceName + "'");
		try {
			if (!tmpPort.setComPortParameters(baudRate, dataBits.getValue(), stopBits.getValue(), parity.getValue()))
				throw new ConnectionException("Error setting parameters for serial port '" + toString() + "'");
			if (!tmpPort.setFlowControl(flowControl.getValue()))
				throw new ConnectionException("Error setting flow control for serial port '" + serialDeviceName + "'");
			in = tmpPort.getInputStream();
			out = tmpPort.getOutputStream();
			port = tmpPort;
			tmpPort = null;
			logger.debug("Serial port '" + serialDeviceName + "' is open");
		} finally {
			if (tmpPort != null)
				tmpPort.closePort();
		}
	}

	@Override
	protected void closeImpl() {
		if (port != null) {
			logger.debug("Closing serial port '" + serialDeviceName + "'...");
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				logger.error("Output stream close failed", e);
			}
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				logger.error("Input stream close failed", e);
			}
			port.closePort();
			try {
				Thread.sleep(SERIAL_PORT_CLOSE_WAIT);
			} catch (InterruptedException e) {
				logger.error("Serial port wait after close interrupted", e);
			}
			logger.debug("Serial port '" + serialDeviceName + "' closed");
			port = null;
		} else {
			logger.warn("Serial port '" + serialDeviceName + "' already closed");
		}
	}

	@Override
	protected void write(byte[] bytes) throws IOException {
		if (port == null)
			throw new IllegalStateException("Cannot send data when connection is closed");
		out.write(bytes);
		out.flush();
	}

	@Override
	protected Chunk read(long timeout) throws IOException, InterruptedException {
		if (port == null)
			throw new IllegalStateException("Cannot read data when connection is closed");
		try {
			port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, (int) timeout, 0);
			byte[] buffer = new byte[CHUNK_BUFFER_SIZE];
			int numRead = in.read(buffer);
			if (numRead == -1) {
				return null;
			}
			return new Chunk(buffer, numRead);
		} catch (SocketTimeoutException e) {
			return new Chunk();
		}
	}

	@Override
	public String toString() {
		return serialDeviceName + "@" + baudRate + "," + dataBits + "," + parity + "," + stopBits;
	}

}
