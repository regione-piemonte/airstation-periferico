/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: holds information for elements in data port drivers
// Change log:
//   2008-10-14: initial version
// ----------------------------------------------------------------------------
// $Id: ElementInfoHolder.java,v 1.1 2015/04/15 14:54:32 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.impl;

import it.csi.periferico.acqdrivers.itf.ElementCfg;
import it.csi.periferico.acqdrivers.itf.ElementInterface;

/**
 * Holds information for elements in data port drivers
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ElementInfoHolder {

	private ElementCfg elementCfg;

	private ElementInterface elementInterface;

	ElementInfoHolder(ElementCfg elementCfg, ElementInterface elementInterface) {
		this.elementCfg = elementCfg;
		this.elementInterface = elementInterface;
	}

	public ElementCfg getElementCfg() {
		return elementCfg;
	}

	public ElementInterface getElementInterface() {
		return elementInterface;
	}

}
