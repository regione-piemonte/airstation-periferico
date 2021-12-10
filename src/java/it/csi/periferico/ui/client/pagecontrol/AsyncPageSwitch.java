/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: asynchronous operation that manages a page switch
// Change log:
//   2009-02-16: initial version
// ----------------------------------------------------------------------------
// $Id: AsyncPageSwitch.java,v 1.3 2015/05/13 16:14:53 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.pagecontrol;

import it.csi.periferico.ui.client.PerifericoUI;

import com.google.gwt.user.client.Window;

/**
 * Asynchronous operation that manages a page switch
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AsyncPageSwitch extends AsyncPageOperation {

	private PageController pageController;

	private UIPage newPage;

	public AsyncPageSwitch(PageController pageController, UIPage newPage) {
		this.pageController = pageController;
		this.newPage = newPage;
	}

	@Override
	public void complete() {
		pageController.setCurrentPageImpl(newPage);
	}

	@Override
	public void onSuccess(Boolean contentUnchanged) {
		if (contentUnchanged)
			complete();
		else if (Window.confirm(PerifericoUI.getMessages().confirm_abandon_page()))
			complete();
	}

}
