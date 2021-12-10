/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: asynchronous operation that manages the update of a page 
// Change log:
//   2009-02-16: initial version
// ----------------------------------------------------------------------------
// $Id: AsyncPageUpdate.java,v 1.3 2015/05/13 16:14:53 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.pagecontrol;

import it.csi.periferico.ui.client.PerifericoUI;

import com.google.gwt.user.client.Window;

/**
 * Asynchronous operation that manages the update of a page
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AsyncPageUpdate extends AsyncPageOperation {

	private PageController pageController;

	private PageUpdateAction pageUpdateAction;

	public AsyncPageUpdate(PageController pageController,
			PageUpdateAction pageUpdateAction) {
		this.pageController = pageController;
		this.pageUpdateAction = pageUpdateAction;
		if (pageUpdateAction != null)
			pageUpdateAction.setAsyncPageUpdate(this);
	}

	@Override
	public void complete() {
		if (pageUpdateAction != null) {
			pageUpdateAction.action();
		} else
			pageController.updateCurrentPageImpl();
	}

	void updatePage() {
		pageController.updateCurrentPageImpl();
	}

	@Override
	public void onSuccess(Boolean contentUnchanged) {
		if (contentUnchanged)
			complete();
		else if (Window.confirm(PerifericoUI.getMessages().confirm_reload_page()))
			complete();
	}

}