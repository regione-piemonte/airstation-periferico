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

import com.google.gson.annotations.SerializedName;

/**
 * Client for the gpsd dameon
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class Devices {

	@SerializedName("class")
	private String class_;
	private String path;
	private String driver;
	private String subtype;
	private String activated;
	private int flags;
	@SerializedName("native")
	private int native_;
	private int bps;
	private String parity;
	private int stopbits;
	private int cycle;

	public void setClass_(String class_) {
		this.class_ = class_;
	}

	public String getClass_() {
		return this.class_;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getDriver() {
		return this.driver;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getSubtype() {
		return this.subtype;
	}

	public void setActivated(String activated) {
		this.activated = activated;
	}

	public String getActivated() {
		return this.activated;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getFlags() {
		return this.flags;
	}

	public void setNative_(int native_) {
		this.native_ = native_;
	}

	public int getNative_() {
		return this.native_;
	}

	public void setBps(int bps) {
		this.bps = bps;
	}

	public int getBps() {
		return this.bps;
	}

	public void setParity(String parity) {
		this.parity = parity;
	}

	public String getParity() {
		return this.parity;
	}

	public void setStopbits(int stopbits) {
		this.stopbits = stopbits;
	}

	public int getStopbits() {
		return this.stopbits;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public int getCycle() {
		return this.cycle;
	}

}
