/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: provider of statistics for data acquisition
// Change log:
//   2016-11-02: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------
package it.csi.periferico.boards;

/**
 * Provider of statistics for data acquisition
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface AcqStatsProvider {

	public AcqStats getAcqStats();

}
