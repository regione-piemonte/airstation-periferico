/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: information for a custom command for a data port analyzer
// Change log:
//   2015-04-09: initial version
// ----------------------------------------------------------------------------
// $Id: CustomCommand.java,v 1.2 2015/04/17 16:03:51 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.itf;

/**
 * Information for a custom command for a data port analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class CustomCommand {

	private String name;
	private String id;
	private String argsDescription;

	public CustomCommand(String name, String id, String argsDescription) {
		this.name = name;
		this.id = id;
		this.argsDescription = argsDescription;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getArgsDescription() {
		return argsDescription;
	}

	@Override
	public String toString() {
		return "CustomCommand [name=" + name + ", id=" + id
				+ ", argsDescription=" + argsDescription + "]";
	}

}
