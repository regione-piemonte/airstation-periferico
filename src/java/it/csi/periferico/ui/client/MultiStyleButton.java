/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: button that can change its style
// Change log:
//   2009-02-23: initial version
// ----------------------------------------------------------------------------
// $Id: MultiStyleButton.java,v 1.3 2013/06/12 08:10:37 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * Button that can change its style
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class MultiStyleButton extends Button {

	private String style;

	private String disabledStyle;

	public MultiStyleButton(String style, String disabledStyle, String tooltip,
			ClickHandler handler) {
		super();
		this.style = style;
		this.disabledStyle = disabledStyle;
		if (tooltip != null)
			setTitle(tooltip);
		if (handler != null)
			addClickHandler(handler);
		setStyleName(style);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setStyleName(enabled ? style : disabledStyle);
	}

}
