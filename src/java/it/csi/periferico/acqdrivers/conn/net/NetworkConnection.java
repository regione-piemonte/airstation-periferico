/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: network connection for data port analyzers
// Change log:
//   2011-11-08: initial version
// ----------------------------------------------------------------------------
// $Id: NetworkConnection.java,v 1.1 2011/11/14 11:22:55 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.conn.net;

import it.csi.periferico.acqdrivers.conn.Chunk;
import it.csi.periferico.acqdrivers.conn.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

/**
 * Network connection for data port analyzers
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class NetworkConnection extends Connection {

	private static final int CHUNK_BUFFER_SIZE = 256;

	private Logger logger;

	private String host;

	private int port;

	private Socket socket;

	private InputStream in;

	private OutputStream out;

	private boolean canReopen;

	public NetworkConnection(String host, int port) {
		this(host, port, null);
		canReopen = true;
	}

	public NetworkConnection(Socket socket) {
		this(null, 0, socket);
		canReopen = false;
	}

	private NetworkConnection(String host, int port, Socket socket) {
		logger = Logger.getLogger("periferico.conn."
				+ getClass().getSimpleName() + "-" + getInstanceCount());
		this.host = host;
		this.port = port;
		this.socket = socket;
	}

	@Override
	protected void openImpl() throws UnknownHostException, IOException {
		if (socket == null) {
			if (canReopen)
				socket = new Socket(host, port);
			else
				throw new IllegalStateException("Cannot reopen the connection "
						+ "when a socket is given to the constructor");
		}
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}

	@Override
	protected void closeImpl() {
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
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			logger.error("Socket close failed", e);
		} finally {
			socket = null;
		}
	}

	@Override
	protected void write(byte[] bytes) throws IOException {
		if (socket == null)
			throw new IllegalStateException(
					"Cannot send data when connection is closed");
		out.write(bytes);
		out.flush();
	}

	@Override
	protected Chunk read(long timeout) throws IOException, InterruptedException {
		try {
			socket.setSoTimeout((int) timeout);
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
		return host + ":" + port;
	}

}
