/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: utility functions for the user interface
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: Utils.java,v 1.16 2009/04/14 16:06:46 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */

package it.csi.periferico.ui.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * Utility functions for the user interface
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class Utils {

	public final static String ANALOG_INPUT = "ANALOG_INPUT";

	public final static String DIGITAL_INPUT = "DIGITAL_INPUT";

	public final static String DIGITAL_INPUT_OUTPUT = "DIGITAL_INPUT_OUTPUT";

	public final static String DIGITAL_OUTPUT = "DIGITAL_OUTPUT";

	public static final String DPA = "DPA";

	public static final String SAMPLE = "SAMPLE";

	public static final String AVG = "AVG";

	public static final String RAIN = "RAIN";

	public static final String WIND = "WIND";

	public static final String NETWORK = "NETWORK";

	public static final String SERIAL = "SERIAL";

	public static native void changeLocale(String locale) /*-{
															$wnd.self.location.href = "PerifericoUI.html?locale=" + locale;
															}-*/;

	public static native void blockForPopup(String id) /*-{
														$doc.getElementById(id).style.display='block';
														
														}-*/;

	public static native void unlockForPopup(String id) /*-{
														$doc.getElementById(id).style.display='none';
														
														}-*/;

	public static FlexTable clearTable(FlexTable table) {
		int dim = table.getRowCount();
		for (int i = 0; i < dim; i++)
			table.removeRow(0);
		return table;
	} // end clearTable

	public static void sendVerifyOk() {
		Window.alert(PerifericoUI.getMessages().alert_send_verify_ok());
	}

}// end class
