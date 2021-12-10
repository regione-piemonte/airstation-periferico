/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface implemented by entities that need to know when
//                  samples are added to an element
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: ElementListener.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Interface implemented by entities that need to know when samples are added to
 * an element
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public interface ElementListener {

	public boolean addValue(Sample value, Element source);
}
