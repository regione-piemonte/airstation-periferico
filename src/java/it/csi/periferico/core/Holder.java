/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface implemented by entities that own something
// Change log:
//   2008-04-30: initial version
// ----------------------------------------------------------------------------
// $Id: Holder.java,v 1.3 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.UUID;

/**
 * Interface implemented by entities that own something
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface Holder {

	public String getName();

	public UUID getId();

	public boolean isEnabled();

}
