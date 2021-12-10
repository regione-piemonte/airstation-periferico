/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Silvia Vergnano
// Purpose of file: utility for language internationalization
// Change log:
//   2008-04-29: initial version
// ----------------------------------------------------------------------------
// $Id: PropertyUtil.java,v 1.3 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Utility for language internationalization
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class PropertyUtil {

	private Map<String, ResourceBundle> bundles = new HashMap<String, ResourceBundle>(); // Map<String,
																							// ResourceBundle>

	private String propertyFileName;

	public PropertyUtil(String propertyFileName) {
		this.propertyFileName = propertyFileName;
	}

	private ResourceBundle getBundle(String locale) {
		ResourceBundle bundle = (ResourceBundle) bundles.get(locale);
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(propertyFileName, new Locale(locale));
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
}
