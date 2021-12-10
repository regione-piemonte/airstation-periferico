/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for actions to be performed when a page is
//                  updated
// Change log:
//   2009-02-17: initial version
// ----------------------------------------------------------------------------
// $Id: PageUpdateAction.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.pagecontrol;

/**
 * Base class for actions to be performed when a page is updated
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class PageUpdateAction extends UserAction {

	private AsyncPageUpdate asyncPageUpdate;

	void setAsyncPageUpdate(AsyncPageUpdate asyncPageUpdate) {
		this.asyncPageUpdate = asyncPageUpdate;
	}

	public void updatePage() {
		asyncPageUpdate.updatePage();
	}

}
