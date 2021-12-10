/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: page that shows a chart
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: ShowChartWidget.java,v 1.7 2009/04/14 16:06:46 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.UIPage;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * Widget that displays charts.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class ShowChartWidget extends UIPage {

	private VerticalPanel panel;

	private String chartName = null;

	Label title;

	public ShowChartWidget() {

		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();
		panelButtonWidget.addButton(PerifericoUI.makeBackButtonBlue());

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().chart_title(), panelButtonWidget);

		// Label and panel for subdevice info
		title = new Label();
		title.setStyleName("gwt-Label-title-blue");

		// panel that contains subdevice info and buttons
		panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		externalPanel.add(title);
		externalPanel.add(panel);
		initWidget(externalPanel);

	}// end constructor

	void setChartName(String chartName) {
		this.chartName = chartName;
	}

	@Override
	protected void reset() {
		panel.clear();
	}

	@Override
	protected void loadContent() {
		if (chartName == null)
			return;
		ChartImage chartImage = new ChartImage();
		ChartImage image = chartImage.displayChart(chartName);
		panel.add(image);
	}

}// end class
