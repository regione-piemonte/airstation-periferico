/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/* ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: represents a password and the associated permissions
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: Password.java,v 1.5 2009/04/14 16:06:46 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.config;

import org.apache.log4j.Logger;

/**
 * Represents a password and the associated permissions
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class Password {

	// variables corresponding to element password of xml file
	private String type;
	private String value;
	private String mode;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	// Define a static logger variable
	private static Logger logger = Logger.getLogger("periferico."
			+ LoginCfg.class.getSimpleName());

	public void printPassword() {

		String print = "\nStampa del contenuto del file di configurazione di login:\n";
		print += "  type: " + this.type + "\n";
		print += "  value: " + this.value + "\n";
		print += "  mode: " + this.mode + "\n";

		logger.debug(print);
	}

}
