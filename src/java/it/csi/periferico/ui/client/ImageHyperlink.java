/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: hyperlink with an associated image
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: ImageHyperlink.java,v 1.3 2013/06/12 08:10:37 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;

/**
 * Hyperlink which allows you to specify Image object as its content.
 * 
 * @author Isabella Vespa - CSI Piemonte (isabella.vespa@csi.it)
 * 
 */
public class ImageHyperlink extends Hyperlink {

	public ImageHyperlink(Image img) {
		this(img, "");
	}

	public ImageHyperlink(Image img, String targetHistoryToken) {
		super();
		DOM.appendChild(DOM.getFirstChild(getElement()), img.getElement());
		setTargetHistoryToken(targetHistoryToken);

		img.unsinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);
		sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);
	}

}