/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: utility for language internationalization of UI service
 * Change log:
 *   2008-05-12: initial version
 * ----------------------------------------------------------------------------
 * $Id: PropertyUtilServer.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.server;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility for language internationalization of UI service
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class PropertyUtilServer {

	private Map<String, ResourceBundle> bundles = new HashMap<String, ResourceBundle>(); // Map<String,

	// ResourceBundle>

	private String propertyFileName;

	public PropertyUtilServer(String propertyFileName) throws IOException {
		this.propertyFileName = propertyFileName;
	}

	private ResourceBundle getBundle(String locale) {
		ResourceBundle bundle = (ResourceBundle) bundles.get(locale);
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(propertyFileName, new Locale(
					locale));
			bundles.put(locale, bundle);

		}
		return bundle;
	}

	public String getProperty(String locale, String name) {
		if (locale == null)
			locale = Locale.getDefault().toString();
		if (locale.equals("en"))
			locale = "";
		return getBundle(locale).getString(name);
	}

	public String getLocalizedMessage(String locale, String msg, String[] args) {
		String msgBundle = null;
		try {
			msgBundle = this.getProperty(locale, msg);
		} catch (MissingResourceException mrex) {
			return msg;
		}
		if (args == null)
			return msgBundle;

		Object[] translatedArgs = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			try {
				String resultString = this
						.getProperty(locale, (String) args[i]);
				translatedArgs[i] = resultString;
			} catch (MissingResourceException mrex) {
				translatedArgs[i] = args[i];
			}
		}
		return MessageFormat.format(msgBundle, translatedArgs);
	}

}
