/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: client for the gpsd dameon
// Change log:
//   2021-04-15: initial version
// ----------------------------------------------------------------------------
// $Id: $
// ----------------------------------------------------------------------------
package it.csi.periferico.gps.json1;

import it.csi.periferico.gps.GpsDatum;
import it.csi.periferico.gps.GpsDatum.Fix;
import it.csi.periferico.gps.JsonProtocol;

import java.io.BufferedReader;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * Client for the gpsd dameon
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class JsonProtocol1Impl implements JsonProtocol {

	private static Logger logger = Logger.getLogger("periferico."
			+ JsonProtocol1Impl.class.getSimpleName());

	@Override
	public GpsDatum readDatum(PrintWriter out, BufferedReader in) {
		GpsDatum datum = new GpsDatum(GpsDatum.Fix.GPS_APP_ERROR);
		String json = null;
		try {
			Gson gson = new Gson();
			out.println("?DEVICES;");
			out.flush();
			json = in.readLine();
			logger.debug("Gpsd DEVICES reply is: " + json);
			if (json == null) {
				logger.error("Cannot get available gps devices list");
				return datum;
			}
			boolean foundDevice = false;
			RootDevices devices = gson.fromJson(json, RootDevices.class);
			for (Devices device : devices.getDevices()) {
				if (device.getActivated() > 0)
					foundDevice = true;
			}
			if (!foundDevice) {
				logger.warn("No GPS device available");
				return new GpsDatum(GpsDatum.Fix.GPS_READ_ERROR);
			}
			out.println("?POLL;");
			out.flush();
			json = in.readLine();
			logger.debug("Gpsd POLL reply is: " + json);
			if (json == null) {
				logger.error("Cannot get gps position information");
				return datum;
			}
			RootPoll poll = gson.fromJson(json, RootPoll.class);
			datum = new GpsDatum(Fix.NO_FIX);
			for (Fixes fix : poll.getFixes()) {
				if (fix.getMode() >= 3) {
					datum = new GpsDatum(fix.getLat(), fix.getLon(),
							fix.getAlt());
				} else if (fix.getMode() >= 2) {
					if (datum.getFix() != Fix.FIX_3D)
						datum = new GpsDatum(fix.getLat(), fix.getLon());
				}
			}
			return datum;
		} catch (Exception ex) {
			logger.error("Error parsing gpsd json data: " + json, ex);
			return datum;
		}

	}

}
