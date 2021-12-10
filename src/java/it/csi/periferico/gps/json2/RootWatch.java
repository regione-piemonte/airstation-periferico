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

/**
 * Client for the gpsd dameon
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class RootWatch extends Root {

	private boolean enable;
	private boolean json;
	private boolean nmea;
	private int raw;
	private boolean scaled;
	private boolean timing;
	private boolean split24;
	private boolean pps;

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean getEnable() {
		return this.enable;
	}

	public void setJson(boolean json) {
		this.json = json;
	}

	public boolean getJson() {
		return this.json;
	}

	public void setNmea(boolean nmea) {
		this.nmea = nmea;
	}

	public boolean getNmea() {
		return this.nmea;
	}

	public void setRaw(int raw) {
		this.raw = raw;
	}

	public int getRaw() {
		return this.raw;
	}

	public void setScaled(boolean scaled) {
		this.scaled = scaled;
	}

	public boolean getScaled() {
		return this.scaled;
	}

	public void setTiming(boolean timing) {
		this.timing = timing;
	}

	public boolean getTiming() {
		return this.timing;
	}

	public void setSplit24(boolean split24) {
		this.split24 = split24;
	}

	public boolean getSplit24() {
		return this.split24;
	}

	public void setPps(boolean pps) {
		this.pps = pps;
	}

	public boolean getPps() {
		return this.pps;
	}

}
