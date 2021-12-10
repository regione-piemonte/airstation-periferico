/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: panel that holds buttons
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: PanelButtonWidget.java,v 1.44 2015/05/05 15:08:49 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * A composite Widget that implements the button panel for the periferico.
 * 
 * @author Isabella Vespa - CSI Piemonte (isabella.vespa@csi.it)
 * 
 */
public class PanelButtonWidget extends Composite {

	private static final int DEFAULT_BUTTON_SPACING = 10;

	private HorizontalPanel hPanel;

	private VerticalAlignmentConstant vAlign = null;

	public PanelButtonWidget() {
		this(DEFAULT_BUTTON_SPACING);
	}

	public PanelButtonWidget(VerticalAlignmentConstant vAlign) {
		this(DEFAULT_BUTTON_SPACING);
		this.vAlign = vAlign;
	}

	public PanelButtonWidget(int buttonSpacing) {
		hPanel = new HorizontalPanel();
		hPanel.setSpacing(buttonSpacing);
		initWidget(hPanel);
	}

	public void addButton(Button button) {
		hPanel.add(button);
		if (vAlign != null)
			hPanel.setCellVerticalAlignment(button, vAlign);
	}

	public void removeButtons() {
		while (hPanel.getWidgetCount() > 0)
			hPanel.remove(0);
	}

}
