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
package it.csi.periferico.gps;

import com.google.gson.annotations.SerializedName;

/**
 * Client for the gpsd dameon
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class JsonProtocolVersion {

	@SerializedName("class")
	private String class_;
	private String release;
	private String rev;
	private int proto_major;
	private int proto_minor;

	public void setClass_(String class_) {
		this.class_ = class_;
	}

	public String getClass_() {
		return this.class_;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public String getRelease() {
		return this.release;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public String getRev() {
		return this.rev;
	}

	public void setProto_major(int proto_major) {
		this.proto_major = proto_major;
	}

	public int getProto_major() {
		return this.proto_major;
	}

	public void setProto_minor(int proto_minor) {
		this.proto_minor = proto_minor;
	}

	public int getProto_minor() {
		return this.proto_minor;
	}

}
