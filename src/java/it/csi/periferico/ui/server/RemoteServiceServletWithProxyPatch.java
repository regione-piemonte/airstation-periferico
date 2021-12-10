/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: hack to use reverse proxy from Centrale
// Change log:
//   2013-06-05: initial version
// ----------------------------------------------------------------------------
// $Id: RemoteServiceServletWithProxyPatch.java,v 1.3 2013/08/07 08:00:39 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.ui.server;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * Hack to use reverse proxy from Centrale
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class RemoteServiceServletWithProxyPatch extends RemoteServiceServlet {

	private static final long serialVersionUID = -914039502965207671L;

	//private static final String DEFAULT_REPLACE_REGEX = "/proxy/station_[^/]*/";
	private static final String DEFAULT_REPLACE_REGEX = "/proxy/station_[0-9]+/";

	private String replaceRegex;

	public RemoteServiceServletWithProxyPatch() {
		replaceRegex = System.getProperty("periferico.proxypatch.regex",
				DEFAULT_REPLACE_REGEX);
	}

	@Override
	protected SerializationPolicy doGetSerializationPolicy(
			HttpServletRequest request, String moduleBaseURL, String strongName) {
		String mbu = null;
		if (moduleBaseURL != null)
			mbu = moduleBaseURL.replaceFirst(replaceRegex, "/");
		return super.doGetSerializationPolicy(request, mbu, strongName);
	}

}
