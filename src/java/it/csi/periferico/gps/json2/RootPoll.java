/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: client for the gpsd dameon
// Change log:
//   2021-04-15: initial version
// ----------------------------------------------------------------------------
// $Id: $
// ----------------------------------------------------------------------------
package it.csi.periferico.gps.json2;

import java.util.List;

/**
 * Client for the gpsd dameon
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class RootPoll extends Root {

	private String time;
	private int active;
	private List<Tpv> tpv;

	public void setTime(String time) {
		this.time = time;
	}

	public String getTime() {
		return this.time;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public int getActive() {
		return this.active;
	}

	public List<Tpv> getTpv() {
		return tpv;
	}

	public void setTpv(List<Tpv> tpv) {
		this.tpv = tpv;
	}

}
