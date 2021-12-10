/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: button with style and tooltip
// Change log:
//   2009-02-23: initial version
// ----------------------------------------------------------------------------
// $Id: StyleButton.java,v 1.3 2013/06/12 08:10:38 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * Button with style and tooltip
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class StyleButton extends Button {

	public StyleButton(String style) {
		this(style, null, null);
	}

	public StyleButton(String style, String tooltip) {
		this(style, tooltip, null);
	}

	public StyleButton(String style, String tooltip, ClickHandler handler) {
		super();
		if (tooltip != null)
			setTitle(tooltip);
		if (handler != null)
			addClickHandler(handler);
		setStyleName(style);
	}

}
