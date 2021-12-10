/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: File filter that finds data file containing data in
// the future or in the current day
// Change log:
//   2009-06-19: initial version
// ----------------------------------------------------------------------------
// $Id: DateFileFilter.java,v 1.1 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * File filter that finds data file containing data in the future or in the
 * current day
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DateFileFilter implements FilenameFilter {

	public enum TimeConstraint {
		PAST, PRESENT, FUTURE
	};

	private String prefix;

	private String dateFormatStr;

	private String extension;

	private Date checkDate;

	private TimeConstraint constraint;

	private DateFormat dateFormat;

	DateFileFilter(String prefix, String dateFormatStr, String extension,
			Date checkDate, TimeConstraint constraint) {
		if (prefix == null || dateFormatStr == null || extension == null)
			throw new IllegalArgumentException("Arguments must not be null");
		this.prefix = prefix;
		this.dateFormatStr = dateFormatStr;
		dateFormat = new SimpleDateFormat(dateFormatStr);
		this.extension = extension;
		try {
			this.checkDate = dateFormat.parse(dateFormat.format(checkDate));
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					"Invalid date format for file names", e);
		}
		this.constraint = constraint;
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
			Date fileDate = dateFormat.parse(name);
			if (constraint == TimeConstraint.PAST)
				return fileDate.before(checkDate);
			if (constraint == TimeConstraint.PRESENT)
				return fileDate.equals(checkDate);
			if (constraint == TimeConstraint.FUTURE)
				return fileDate.after(checkDate);
			return false;
		} catch (Exception ex) {
			return false;
		}
	}

}
