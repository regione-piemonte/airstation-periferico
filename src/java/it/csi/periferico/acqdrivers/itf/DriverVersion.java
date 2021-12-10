/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: holds the version of the application
// Change log:
//   2015-09-17: initial version
// ----------------------------------------------------------------------------
// $Id: DriverVersion.java,v 1.1 2015/09/18 16:07:53 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Holds the version of the application
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DriverVersion {

	private int major;
	private int minor;
	private int bugfix;

	public DriverVersion(int major, int minor, int bugfix) {
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

}
