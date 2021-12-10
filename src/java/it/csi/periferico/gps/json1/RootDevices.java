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
public class RootDevices {

	@SerializedName("class")
	private String class_;
	private List<Devices> devices;

	public void setClass_(String class_) {
		this.class_ = class_;
	}

	public String getClass_() {
		return this.class_;
	}

	public void setDevices(List<Devices> devices) {
		this.devices = devices;
	}

	public List<Devices> getDevices() {
		return this.devices;
	}

}
