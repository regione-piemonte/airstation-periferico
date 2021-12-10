/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Isabella Vespa
// Purpose of file: represents a widget with Label and tooltip
// Change log:
//   2008-12-16: initial version
// ----------------------------------------------------------------------------
// $Id: ClickWidget.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * 
 * This class represents widget with Label and tooltip. It's used to insert in
 * table of real time data, history real time data and history means data.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class ClickWidget extends Composite {

	Label text;

	public ClickWidget(String title, String label) {
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setStyleName("gwt-pointer");
		text = new Label();
		text.setWidth("30px");
		text.setText(label);
		text.setTitle(title);
		hPanel.add(text);
		initWidget(hPanel);
	}

}// end class
