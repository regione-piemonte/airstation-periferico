/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: client for Centrale software services
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: CopClient.java,v 1.10 2010/01/14 16:16:38 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.comm;

import it.csi.periferico.Periferico;
import it.csi.periferico.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Client for Centrale software services
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class CopClient {

	private static final int CONNECT_TIMEOUT_MS = 60000;

	private static final int READ_TIMEOUT_MS = 60000;

	private static final String SERVICE_URL = "/perifservice";

	private final DateFormat requestDateFmt = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	private String copHost;

	private int copPort;

	public CopClient(String copHost, int copPort) {
		this.copHost = copHost;
		this.copPort = copPort;
	}

	public List<String> getStationNames() throws ClientException {
		try {
			String requestParams = "?function=listStationNames";
			List<String> stationNames = readStringList(requestParams);
			return stationNames;
		} catch (Exception ex) {
			throw new ClientException("error_reading_station_names_from_cop",
					ex);
		}
	}

	public List<String> getStationConfigs(String stationName, Date startDate,
			Date endDate, Integer limit) throws ClientException {
		try {
			StringBuffer requestParams = new StringBuffer();
			requestParams.append("?function=listStationConfigs");
			requestParams.append("&station=" + urlEncode(stationName));
			if (startDate != null)
				requestParams.append("&startDate="
						+ urlEncode(requestDateFmt.format(startDate)));
			if (endDate != null)
				requestParams.append("&endDate="
						+ urlEncode(requestDateFmt.format(endDate)));
			if (limit != null)
				requestParams.append("&limit=" + limit);
			List<String> stationConfigs = readStringList(requestParams
					.toString());
			return stationConfigs;
		} catch (Exception ex) {
			throw new ClientException(
					"error_reading_station_cfg_names_from_cop", ex);
		}
	}

	public Config readConfig(String stationName, String configName)
			throws ClientException {
		BufferedReader in = null;
		try {
			String requestParams = "?function=getStationConfig&station="
					+ urlEncode(stationName) + "&config="
					+ urlEncode(configName);
			URL cop = new URL("http://" + copHost + ":" + copPort + SERVICE_URL
					+ requestParams);
			URLConnection copConn = cop.openConnection();
			copConn.setDoInput(true);
			copConn.setDoOutput(false);
			copConn.setUseCaches(false);
			copConn.setConnectTimeout(CONNECT_TIMEOUT_MS);
			copConn.setReadTimeout(READ_TIMEOUT_MS);
			in = new BufferedReader(new InputStreamReader(
					copConn.getInputStream(), "UTF-8"));
			String result = in.readLine();
			if (result == null)
				throw new Exception("At least 1 line in response expected");
			if (!result.equals("OK"))
				throw new Exception("The server returned the following error: "
						+ result);
			Config config = Periferico.getInstance().parseConfig(in);
			return config;
		} catch (Exception ex) {
			throw new ClientException("error_reading_station_cfg_from_cop", ex);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
	}

	private List<String> readStringList(String requestParams) throws Exception {
		List<String> responseLines = new ArrayList<String>();
		URL cop = new URL("http://" + copHost + ":" + copPort + SERVICE_URL
				+ requestParams);
		URLConnection copConn = cop.openConnection();
		copConn.setDoInput(true);
		copConn.setDoOutput(false);
		copConn.setUseCaches(false);
		copConn.setConnectTimeout(CONNECT_TIMEOUT_MS);
		copConn.setReadTimeout(READ_TIMEOUT_MS);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					copConn.getInputStream(), "UTF-8"));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				inputLine = inputLine.trim();
				if (!inputLine.isEmpty())
					responseLines.add(inputLine);
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception exClose) {
				}
			}
		}
		if (responseLines.isEmpty())
			throw new Exception("At least 1 line in response expected");
		String result = responseLines.remove(0);
		if (!result.equals("OK"))
			throw new Exception("The server returned the following error: "
					+ result);
		if (responseLines.isEmpty())
			throw new Exception("The number of response lines is missing");
		String strResponseLinesNumber = responseLines.remove(0);
		int responseLinesNumber;
		try {
			responseLinesNumber = Integer.parseInt(strResponseLinesNumber);
		} catch (NumberFormatException nfe) {
			throw new Exception("Error parsing the number of response lines");
		}
		if (responseLines.size() != responseLinesNumber)
			throw new Exception("The number of response lines received ("
					+ responseLines.size() + ") is different from the number "
					+ "of response lines declared (" + responseLinesNumber
					+ ")");
		return responseLines;
	}

	private static String urlEncode(String str)
			throws UnsupportedEncodingException {
		if (str == null)
			return "";
		return URLEncoder.encode(str, "UTF-8");
	}

}
