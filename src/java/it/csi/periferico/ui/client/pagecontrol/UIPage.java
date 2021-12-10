/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for user interface pages
// Change log:
//   2009-02-13: initial version
// ----------------------------------------------------------------------------
// $Id: UIPage.java,v 1.2 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.pagecontrol;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for user interface pages
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class UIPage extends Composite {

	private UIPage upperLevelPage = null;

	private Widget pageTab = null;

	public UIPage getUpperLevelPage() {
		return upperLevelPage;
	}

	public void setUpperLevelPage(UIPage page) {
		upperLevelPage = page;
	}

	public Widget getPageTab() {
		return pageTab;
	}

	public void setPageTab(Widget pageTab) {
		this.pageTab = pageTab;
	}

	protected void reset() {
	}

	void show() {
		Widget tab = getNearestPageTab();
		if (tab != null)
			tab.setStyleName("active");
		setVisible(true);
	}

	void hide() {
		Widget tab = getNearestPageTab();
		setVisible(false);
		if (tab != null)
			tab.removeStyleName("active");
	}

	protected void dismissContent(AsyncPageOperation asyncPageOperation) {
		asyncPageOperation.complete();
	}

	abstract protected void loadContent();

	private Widget getNearestPageTab() {
		Widget tab = pageTab;
		UIPage upperPage = upperLevelPage;
		while (tab == null && upperPage != null) {
			tab = upperPage.getPageTab();
			upperPage = upperPage.getUpperLevelPage();
		}
		return tab;
	}

	protected static void clearGridRowFormatter(FlexTable table) {
		if (table == null)
			return;
		for (int i = 0; i < table.getRowCount(); i++)
			for (int j = 0; j < table.getCellCount(i); j++) {
				table.getCellFormatter().removeStyleName(i, j,
						"gwt-row-selected");
				table.getCellFormatter().setStyleName(i, j, "gwt-table-data");
			}
	}

}
