/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: Filter that finds UUID like file names
// Change log:
//   2009-06-19: initial version
// ----------------------------------------------------------------------------
// $Id: UUIDFilenameFilter.java,v 1.1 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

/**
 * Filter that finds UUID like file names
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class UUIDFilenameFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		try {
			UUID.fromString(name);
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

}
