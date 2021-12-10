/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface implemented by entities that need to observe
//                  acquired samples 
// Change log:
//   2008-09-10: initial version
// ----------------------------------------------------------------------------
// $Id: SampleObserver.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Interface implemented by entities that need to observe acquired samples
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface SampleObserver {

	public boolean deliver(Sample sample);

	public void unbind();

}
