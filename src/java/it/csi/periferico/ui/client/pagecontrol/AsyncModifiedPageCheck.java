/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: asynchronous operation that checks if a page has been
//                  modified
// Change log:
//   2009-03-12: initial version
// ----------------------------------------------------------------------------
// $Id: AsyncModifiedPageCheck.java,v 1.3 2015/05/13 16:14:53 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.pagecontrol;

import com.google.gwt.user.client.Window;

/**
 * Asynchronous operation that checks if a page has been modified
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AsyncModifiedPageCheck extends AsyncPageOperation {

	private String modifiedWarning;

	private UserAction userAction;

	public AsyncModifiedPageCheck(String modifiedWarning, UserAction userAction) {
		this.modifiedWarning = modifiedWarning;
		this.userAction = userAction;
	}

	@Override
	public void complete() {
		if (userAction != null) {
			userAction.action();
		}
	}

	@Override
	public void onSuccess(Boolean contentUnchanged) {
		if (contentUnchanged)
			complete();
		else if (modifiedWarning != null && Window.confirm(modifiedWarning))
			complete();
	}

}