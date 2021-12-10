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
package it.csi.periferico.gps.json2;

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
public class JsonProtocol2Impl implements JsonProtocol {

	private static Logger logger = Logger.getLogger("periferico."
			+ JsonProtocol2Impl.class.getSimpleName());

	@Override
	public GpsDatum readDatum(PrintWriter out, BufferedReader in) {
		String json = null;
		try {
			Gson gson = new Gson();
			out.println("?WATCH={\"class\":\"WATCH\",\"enable\":true,\"json\":false};");
			out.flush();
			// Wait for WATCH reply, it may take up to 2s
			int count = 20;
			while (!in.ready() && count-- > 0)
				Thread.sleep(100);
			Boolean foundDevice = null;
			boolean watchEnabled = false;
			while (in.ready()) {
				json = in.readLine();
				if (json != null) {
					logger.debug("Gpsd WATCH reply is: " + json);
					Root root = gson.fromJson(json, Root.class);
					if ("DEVICES".equals(root.getClass_())) {
						foundDevice = false;
						RootDevices devices = gson.fromJson(json,
								RootDevices.class);
						for (Devices device : devices.getDevices()) {
							if (device.getDriver() != null
									&& !device.getDriver().isEmpty())
								foundDevice = true;
						}
					} else if ("WATCH".equals(root.getClass_())) {
						RootWatch watch = gson.fromJson(json, RootWatch.class);
						watchEnabled = watch.getEnable();
					}
				}
			}
			if (foundDevice == null) {
				logger.error("Cannot read DEVICES list");
				return new GpsDatum(GpsDatum.Fix.GPS_APP_ERROR);
			}
			if (!foundDevice) {
				logger.warn("No GPS device available");
				return new GpsDatum(GpsDatum.Fix.GPS_READ_ERROR);
			}
			if (!watchEnabled) {
				logger.error("Cannot enable gps data observing");
				return new GpsDatum(GpsDatum.Fix.GPS_APP_ERROR);
			}
			out.println("?POLL;");
			out.flush();
			json = in.readLine();
			logger.debug("Gpsd POLL reply is: " + json);
			if (json == null) {
				logger.error("Cannot get gps position information");
				return new GpsDatum(GpsDatum.Fix.GPS_APP_ERROR);
			}
			RootPoll poll = gson.fromJson(json, RootPoll.class);
			GpsDatum datum = new GpsDatum(Fix.NO_FIX);
			for (Tpv tpv : poll.getTpv()) {
				if (tpv.getMode() >= 3) {
					datum = new GpsDatum(tpv.getLat(), tpv.getLon(),
							tpv.getAlt());
				} else if (tpv.getMode() >= 2) {
					if (datum.getFix() != Fix.FIX_3D)
						datum = new GpsDatum(tpv.getLat(), tpv.getLon());
				}
			}
			return datum;
		} catch (Exception ex) {
			logger.error("Error parsing gpsd json data: " + json, ex);
			return new GpsDatum(GpsDatum.Fix.GPS_APP_ERROR);
		}

	}

}
