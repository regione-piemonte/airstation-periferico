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
package it.csi.periferico.gps.json1;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Client for the gpsd dameon
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class RootPoll {

	@SerializedName("class")
	private String class_;
	private String time;
	private int active;
	private List<Fixes> fixes;

	public void setClass_(String class_) {
		this.class_ = class_;
	}

	public String getClass_() {
		return this.class_;
	}

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

	public void setFixes(List<Fixes> fixes) {
		this.fixes = fixes;
	}

	public List<Fixes> getFixes() {
		return this.fixes;
	}

}
