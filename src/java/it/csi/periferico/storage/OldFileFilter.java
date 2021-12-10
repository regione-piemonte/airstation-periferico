/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file filter to detect old data files
// Change log:
//   2008-09-19: initial version
// ----------------------------------------------------------------------------
// $Id: OldFileFilter.java,v 1.2 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * File filter to detect old data files
 *
 * @author pierfrancesco.vallosio@consulenti.csi.it
 *
 */
class OldFileFilter implements FilenameFilter {

	private String prefix;

	private String dateFormatStr;

	private String extension;

	private int daysOfDataInFile;

	private int daysLimit;

	private DateFormat dateFormat;

	private long now;

	OldFileFilter(String prefix, String dateFormatStr, String extension,
			int daysOfDataInFile, int daysLimit) {
		if (prefix == null || dateFormatStr == null || extension == null)
			throw new IllegalArgumentException("Arguments must not be null");
		if (daysOfDataInFile <= 0)
			throw new IllegalArgumentException(
					"Parameter daysOfDataInFile must be positive");
		if (daysLimit <= 0)
			throw new IllegalArgumentException(
					"Parameter daysLimit must be positive");
		this.prefix = prefix;
		this.dateFormatStr = dateFormatStr;
		this.extension = extension;
		this.daysOfDataInFile = daysOfDataInFile;
		this.daysLimit = daysLimit;
		dateFormat = new SimpleDateFormat(dateFormatStr);
		now = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
	}

	public boolean accept(File dir, String name) {
		try {
			if (!name.startsWith(prefix))
				return false;
			name = name.substring(prefix.length());
			if (!name.endsWith(extension))
				return false;
			int endIndex = name.length() - extension.length();
			if (endIndex < 0)
				return false;
			name = name.substring(0, endIndex);
			if (name.length() != dateFormatStr.length())
				return false;
			long fileDate = dateFormat.parse(name).getTime()
					/ (1000 * 60 * 60 * 24);
			return now - (fileDate + daysOfDataInFile) > daysLimit;
		} catch (Exception ex) {
			return false;
		}
	}

}
