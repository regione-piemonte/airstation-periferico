/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: default async callback for user interface service functions
// Change log:
//   2009-02-24: initial version
// ----------------------------------------------------------------------------
// $Id: UIAsyncCallback.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Default async callback for user interface service functions
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class UIAsyncCallback<T> implements AsyncCallback<T> {

	public void onFailure(Throwable caught) {
		if (caught instanceof SessionExpiredException)
			PerifericoUI.sessionEnded();
		else if (caught instanceof UserParamsException)
			Window.alert(caught.getMessage());
		else
			PerifericoUI.unexpectedError(caught);
	}

}
