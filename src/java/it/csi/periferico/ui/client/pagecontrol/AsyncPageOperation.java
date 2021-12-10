/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for asynchronous page operations
// Change log:
//   2009-02-16: initial version
// ----------------------------------------------------------------------------
// $Id: AsyncPageOperation.java,v 1.4 2015/05/13 16:14:53 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.pagecontrol;

import it.csi.periferico.ui.client.UIAsyncCallback;

/**
 * Base class for asynchronous page operations
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class AsyncPageOperation extends UIAsyncCallback<Boolean> {

	public abstract void complete();

}
