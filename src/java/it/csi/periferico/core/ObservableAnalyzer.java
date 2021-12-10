/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface implemented by analyzers that need to be observed
// Change log:
//   2008-10-08: initial version
// ----------------------------------------------------------------------------
// $Id: ObservableAnalyzer.java,v 1.2 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.List;

/**
 * Interface implemented by analyzers that need to be observed
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
public interface ObservableAnalyzer {

	public List<? extends ObservableSampleElement> getListElements();

}
