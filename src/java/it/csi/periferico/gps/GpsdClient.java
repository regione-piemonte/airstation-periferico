/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: client for the gpsd dameon
// Change log:
//   2008-09-26: initial version
// ----------------------------------------------------------------------------
// $Id: GpsdClient.java,v 1.10 2012/01/18 11:00:49 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.gps;

import it.csi.periferico.gps.json1.JsonProtocol1Impl;
import it.csi.periferico.gps.json2.JsonProtocol2Impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * Client for the gpsd dameon
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class GpsdClient implements Runnable {

	private static Logger logger = Logger.getLogger("periferico."
			+ GpsdClient.class.getSimpleName());

	private static final String DEFAULT_HOST = "localhost";

	private static final int DEFAULT_PORT = 2947;

	private static final int REQUEST_TIMEOUT = 2000; // ms

	public static final String THREAD_NAME = "gpsd_client";

	private static final int GPSDCLIENT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 2;

	private static final int GPSDCLIENT_THREAD_PERIOD = 60; // seconds

	private static final int GPSDCLIENT_THREAD_OFFSET = 2; // seconds

	private static final int GPSDCLIENT_THREAD_SLEEP_TIME = 2; // seconds

	private String host;

	private int port;

	private List<GpsDatum> listData = new ArrayList<GpsDatum>();

	private GpsDatum lastDatum;

	private volatile Thread gpsdClientThread = null;

	public GpsdClient() {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}

	public GpsdClient(String host, int port) {
		this.host = host;
		this.port = port;
		this.lastDatum = null;
	}

	public void start() {
		gpsdClientThread = new Thread(this, THREAD_NAME);
		gpsdClientThread.setDaemon(true);
		gpsdClientThread.setPriority(GPSDCLIENT_THREAD_PRIORITY);
		gpsdClientThread.start();
	}

	public void stop() {
		Thread tmpThread = gpsdClientThread;
		gpsdClientThread = null;
		if (tmpThread != null) {
			logger.info("Gpsd client thread shutdown in progress...");
			try {
				int maxWaitTime_s = 10;
				logger.info("Waiting up to " + maxWaitTime_s
						+ "s for gpsd client thread to terminate...");
				tmpThread.join(maxWaitTime_s * 1000);
				logger.info("Finished waiting for gpsd client thread"
						+ " to terminate");
			} catch (InterruptedException ie) {
				logger.error("Wait for gpsd client thread to terminate"
						+ " interrupted");
			}
		}
	}

	private long computeNextActionTime() {
		long threadPeriodMillis = GPSDCLIENT_THREAD_PERIOD * 1000;
		return (System.currentTimeMillis() / threadPeriodMillis)
				* threadPeriodMillis + threadPeriodMillis
				+ GPSDCLIENT_THREAD_OFFSET * 1000;
	}

	public void run() {
		logger.info("Gpsd client thread started");
		try {
			readDatum();
		} catch (InterruptedException ie) {
			logger.warn("Gpsd client thread interrupted");
			return;
		}
		long nextActionTime = computeNextActionTime();
		while (Thread.currentThread() == gpsdClientThread) {
			try {
				Date currentTime = new Date();
				if (currentTime.getTime() >= nextActionTime) {
					nextActionTime = computeNextActionTime();
					logger.debug("Querying gpsd service...");
					readDatum();
					logger.debug("Gpsd service query completed in: "
							+ (System.currentTimeMillis() - currentTime
									.getTime()) + " ms");
				} else if (currentTime.getTime() < nextActionTime
						- GPSDCLIENT_THREAD_PERIOD * 1000) {
					// This code may run only in case of clock adjustment !
					nextActionTime = computeNextActionTime();
					continue;
				}
				Thread.sleep(GPSDCLIENT_THREAD_SLEEP_TIME * 1000);
			} catch (InterruptedException e) {
				logger.warn("Gpsd client thread interrupted");
				return;
			}
		}
		logger.info("Gpsd client thread stopped");
	}

	public GpsDatum getLastDatum() {
		return lastDatum;
	}

	public synchronized List<GpsDatum> getAllDataAndClean() {
		List<GpsDatum> tmpList = listData;
		listData = new ArrayList<GpsDatum>();
		return tmpList;
	}

	private synchronized void add(GpsDatum datum) {
		lastDatum = datum;
		listData.add(datum);
	}

	private void readDatum() throws InterruptedException {
		Socket gpsdSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		GpsDatum datum = null;
		try {
			gpsdSocket = new Socket(host, port);
			gpsdSocket.setSoTimeout(REQUEST_TIMEOUT);
			out = new PrintWriter(gpsdSocket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(
					gpsdSocket.getInputStream()));
			// Wait for Json protocol to output its version
			int count = 20;
			while (!in.ready() && count-- > 0)
				Thread.sleep(10);
			if (in.ready()) { // Json protocol
				String reply = in.readLine();
				logger.debug("Gpsd VERSION reply is: " + reply);
				if (reply == null)
					logger.error("Cannot get protocol version");
				Gson gson = new Gson();
				JsonProtocolVersion ver = gson.fromJson(reply,
						JsonProtocolVersion.class);
				logger.debug("Protocol version is: " + ver.getProto_major()
						+ "." + ver.getProto_minor());
				JsonProtocol jsonProtocol = null;
				if (ver.getProto_major() <= 3 && ver.getProto_minor() <= 4)
					jsonProtocol = new JsonProtocol1Impl();
				else
					jsonProtocol = new JsonProtocol2Impl();
				datum = jsonProtocol.readDatum(out, in);
			} else { // Old textual protocol
				datum = readDatumTextProtocol(out, in);
			}
		} catch (SocketTimeoutException ex) {
			logger.error("Timeout reading data from gpsd service", ex);
		} catch (IOException ex) {
			logger.error("Error reading data from gpsd service", ex);
		} finally {
			if (out != null)
				out.close();
			try {
				if (in != null)
					in.close();
			} catch (Exception ex) {
			}
			if (gpsdSocket != null)
				try {
					gpsdSocket.close();
				} catch (Exception e) {
				}
		}
		add(datum != null ? datum : new GpsDatum(GpsDatum.Fix.GPS_APP_ERROR));
	}

	private GpsDatum readDatumTextProtocol(PrintWriter out, BufferedReader in)
			throws InterruptedException, IOException {
		out.println("I");
		out.flush();
		String replyI = in.readLine();
		logger.debug("Gpsd I reply is: " + replyI);
		// ATTENTION: in order to obtain proper reply from the XPA command,
		// it must not be the first issued command and a 1 second sleep
		// before issuing it is needed
		Thread.sleep(1000);
		out.println("XPA");
		out.flush();
		String replyXPA = in.readLine();
		logger.debug("Gpsd XPA reply is: " + replyXPA);
		if (replyXPA == null) {
			logger.error("Cannot read gps data line");
			return new GpsDatum(GpsDatum.Fix.GPS_APP_ERROR);
		}
		return parseXPACommandReply(replyXPA);
	}

	private GpsDatum parseXPACommandReply(String reply) {
		// --> XPA
		// <-- GPSD,X=1222433576.751891,P=45.038270 7.651783,A=254.623
		final String REPLY_PREFIX = "GPSD,";
		try {
			if (!reply.startsWith(REPLY_PREFIX))
				throw new RuntimeException("Gpsd reply does not start with: "
						+ REPLY_PREFIX);
			reply = reply.substring(REPLY_PREFIX.length());
			String[] tokens = reply.split(",");
			if (tokens == null || tokens.length != 3)
				throw new RuntimeException(
						"Three tokens expected in reply, found "
								+ (tokens == null ? "0" : tokens.length));
			if (!tokens[0].startsWith("X="))
				throw new RuntimeException("X token not found");
			String strValueX = tokens[0].substring(2).trim();
			if ("?".equals(strValueX))
				return new GpsDatum(GpsDatum.Fix.GPS_READ_ERROR);
			Double.parseDouble(strValueX);
			if (!tokens[1].startsWith("P="))
				throw new RuntimeException("Position token not found");
			String strValueP = tokens[1].substring(2).trim();
			if ("?".equals(strValueP))
				return new GpsDatum(GpsDatum.Fix.NO_FIX);
			String[] coordinates = strValueP.split(" ");
			if (coordinates == null || coordinates.length != 2)
				throw new RuntimeException("Coordinate values not found");
			double latitude;
			double longitude;
			try {
				latitude = Double.parseDouble(coordinates[0]);
				longitude = Double.parseDouble(coordinates[1]);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException("Error parsing coordinate values");
			}
			if (!tokens[2].startsWith("A="))
				throw new RuntimeException("Altitude token not found");
			String strValueA = tokens[2].substring(2).trim();
			if ("?".equals(strValueA))
				return new GpsDatum(latitude, longitude);
			double altitude;
			try {
				altitude = Double.parseDouble(strValueA);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException("Error parsing altitude value");
			}
			return new GpsDatum(latitude, longitude, altitude);
		} catch (Exception ex) {
			logger.error("Error parsing gpsd data line: " + reply, ex);
			return new GpsDatum(GpsDatum.Fix.GPS_APP_ERROR);
		}
	}

}
