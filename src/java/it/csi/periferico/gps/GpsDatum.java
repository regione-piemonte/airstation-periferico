/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a gps datum
// Change log:
//   2008-09-26: initial version
// ----------------------------------------------------------------------------
// $Id: GpsDatum.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.gps;

import it.csi.periferico.core.Value;

import java.util.Date;

/**
 * Represents a gps datum
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class GpsDatum extends Value {

	public enum Fix {
		GPS_APP_ERROR, GPS_READ_ERROR, NO_FIX, FIX_2D, FIX_3D
	}

	private Double latitude = null;

	private Double longitude = null;

	private Double altitude = null;

	private Fix fix;

	public GpsDatum(Fix fix) {
		this(new Date(), null, null, null, fix);
	}

	public GpsDatum(Double latitude, Double longitude) {
		this(new Date(), latitude, longitude, null, Fix.FIX_2D);
	}

	public GpsDatum(Double latitude, Double longitude, Double altitude) {
		this(new Date(), latitude, longitude, altitude, Fix.FIX_3D);
	}

	public GpsDatum(Date timestamp, Double latitude, Double longitude,
			Double altitude, Fix fix) {
		super(timestamp);
		if (timestamp == null)
			throw new IllegalArgumentException(
					"Null timestamp not allowed in gps datum");
		if (fix == null) {
			throw new IllegalArgumentException(
					"Null fix not allowed in gps datum");
		} else if (fix == Fix.FIX_3D) {
			if (latitude == null || longitude == null || altitude == null)
				throw new IllegalArgumentException(
						"Null position values not allowed in 3D-FIX datum");
		} else if (fix == Fix.FIX_2D) {
			if (latitude == null || longitude == null)
				throw new IllegalArgumentException(
						"Null latitude or longitude not allowed in 2D-FIX datum");
		} else {
			if (latitude != null || longitude != null || altitude != null)
				throw new IllegalArgumentException(
						"Position values must be null in " + fix + " datum");
		}
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.fix = fix;
	}

	public Double getAltitude() {
		return altitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Fix getFix() {
		return fix;
	}

	@Override
	public String toString() {
		return "[timestamp=" + getTimestamp() + ",fix=" + fix + ",latitude="
				+ latitude + ",longitude=" + longitude + ",altitude="
				+ altitude + "]";
	}

}
