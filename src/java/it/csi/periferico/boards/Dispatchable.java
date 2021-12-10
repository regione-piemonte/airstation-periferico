/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for dispatchable objects (acquired data, events)
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: Dispatchable.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Interface for dispatchable objects (acquired data, events)
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public interface Dispatchable {

	public IOUser getDestination();

}
