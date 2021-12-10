/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: possible flags for data prevalidation
// Change log:
//   2008-05-21: initial version
// ----------------------------------------------------------------------------
// $Id: ValidationFlag.java,v 1.6 2013/08/07 08:01:08 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

/**
 * Possible flags for data prevalidation
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ValidationFlag {

	// Bits 00-07: analyzer flags
	// Bits 08-15: element flags
	// Bits 16-23: acquisition flags
	// Bits 24-27: station flags
	// Bits 28-31: aggregation flags

	public static final int ANALYZER_FAULT = 0x00000001;

	public static final int ANALYZER_MAINTENANCE = 0x00000002;

	public static final int ANALYZER_MANUAL_CALIB = 0x00000004;

	public static final int ANALYZER_AUTO_CALIB = 0x00000008;

	public static final int ANALYZER_DATA_NOT_VALID = 0x00000010;

	public static final int VALUE_OUT_OF_RANGE = 0x00000100;

	public static final int CHANNEL_UNTRIMMED = 0x00000200;

	public static final int ACQ_ERROR = 0x00010000;

	public static final int ACQ_OUT_OF_SCALE = 0x00020000;

	public static final int ENVIRONMENT_NOT_OK = 0x01000000;

	public static final int MISSING_DATA = 0x10000000;

	public static final int NOT_CONSTANT_DATA = 0x20000000;

}
