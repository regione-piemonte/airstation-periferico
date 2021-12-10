/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: Filename filter for the folders of aggregate data
// Change log:
//   2009-06-23: initial version
// ----------------------------------------------------------------------------
// $Id: IntegerFileFilter.java,v 1.1 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filename filter for the folders of aggregate data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class IntegerFileFilter implements FilenameFilter {

	public boolean accept(File dir, String name) {
		try {
			Integer.parseInt(name);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

}
