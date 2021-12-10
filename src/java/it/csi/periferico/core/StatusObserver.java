/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface to observe a status
// Change log:
//   2008-09-24: initial version
// ----------------------------------------------------------------------------
// $Id: StatusObserver.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Interface to observe a status
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public interface StatusObserver {

	public boolean deliver(Status status);

	public void unbind();

}
