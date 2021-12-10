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

import com.google.gson.annotations.SerializedName;

/**
 * Client for the gpsd dameon
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class Fixes {

	@SerializedName("class")
	private String class_;
	private String tag;
	private String device;
	private String time;
	private double ept;
	private double lat;
	private double lon;
	private double alt;
	private double epx;
	private double epy;
	private double epv;
	private double track;
	private double speed;
	private double climb;
	private double eps;
	private int mode;

	public void setClass_(String class_) {
		this.class_ = class_;
	}

	public String getClass_() {
		return this.class_;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return this.tag;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getDevice() {
		return this.device;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTime() {
		return this.time;
	}

	public void setEpt(double ept) {
		this.ept = ept;
	}

	public double getEpt() {
		return this.ept;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLat() {
		return this.lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLon() {
		return this.lon;
	}

	public void setAlt(double alt) {
		this.alt = alt;
	}

	public double getAlt() {
		return this.alt;
	}

	public void setEpx(double epx) {
		this.epx = epx;
	}

	public double getEpx() {
		return this.epx;
	}

	public void setEpy(double epy) {
		this.epy = epy;
	}

	public double getEpy() {
		return this.epy;
	}

	public void setEpv(double epv) {
		this.epv = epv;
	}

	public double getEpv() {
		return this.epv;
	}

	public void setTrack(double track) {
		this.track = track;
	}

	public double getTrack() {
		return this.track;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setClimb(double climb) {
		this.climb = climb;
	}

	public double getClimb() {
		return this.climb;
	}

	public void setEps(double eps) {
		this.eps = eps;
	}

	public double getEps() {
		return this.eps;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return this.mode;
	}

}
