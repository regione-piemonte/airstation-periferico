/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: holds the version of the application
// Change log:
//   2008-07-29: initial version
// ----------------------------------------------------------------------------
// $Id: Version.java,v 1.4 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico;

import java.text.ParseException;

/**
 * Holds the version of the application
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class Version implements Comparable<Object> {

	private int major;

	private int minor;

	private int bugfix;

	public Version(int major, int minor, int bugfix) {
		this.major = major;
		this.minor = minor;
		this.bugfix = bugfix;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getBugfix() {
		return bugfix;
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + bugfix;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bugfix;
		result = prime * result + major;
		result = prime * result + minor;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (bugfix != other.bugfix)
			return false;
		if (major != other.major)
			return false;
		return minor == other.minor;
	}

	@Override
	public int compareTo(Object o) {
		Version ver = (Version) o;
		return (major * 100 + minor * 10 + bugfix)
				- (ver.getMajor() * 100 + ver.getMinor() * 10 + ver.getBugfix());
	}

	public static Version valueOf(String string) throws ParseException,
			NumberFormatException {
		String[] fields = string.split("\\.", -1);
		if (fields.length != 3)
			throw new ParseException("Three fields needed, found "
					+ fields.length, 0);
		return new Version(Integer.parseInt(fields[0]),
				Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
	}

}
