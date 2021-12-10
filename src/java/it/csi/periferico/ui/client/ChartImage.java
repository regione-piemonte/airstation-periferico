/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: displays a chart
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: ChartImage.java,v 1.9 2009/04/14 16:06:46 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.user.client.ui.Image;

/**
 * 
 * This class provides method for display chart
 *
 * @author isabella.vespa@csi.it
 *
 */
public class ChartImage extends Image {

	public ChartImage() {
		super();
	}// end constructor

	public ChartImage displayChart(String chartName) {
		String imageUrl = "./displayChart?filename=" + chartName;
		setUrl(imageUrl);
		return this;
	}
}// end class
