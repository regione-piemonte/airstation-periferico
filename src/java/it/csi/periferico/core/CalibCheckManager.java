/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: manages automatic calibrations
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: CalibCheckManager.java,v 1.6 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages automatic calibrations
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
// TODO: this class is only the starting point for future implementation
public class CalibCheckManager {

	private List<CalibAutoCheck> listCheck = new ArrayList<CalibAutoCheck>();

	public List<CalibAutoCheck> getListCheck() {
		return listCheck;
	}

}

/*
 * TODO: Lista delle funzioni:
 * 
 * attivazione dei check di calibrazione: questa funzione determina il momento
 * esatto per l'attivazione di ciascuna operazione di check e la attiva, dopo
 * aver verificato che lo strumento associato al check sia abilitato e non in
 * fase di calibrazione o manutenzione. Ciascuna operazione di check deve essere
 * svolta in un proprio thread.
 */
