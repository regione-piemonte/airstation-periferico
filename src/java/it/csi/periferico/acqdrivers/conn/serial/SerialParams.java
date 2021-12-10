/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents the parameters for opening a serial port
// Change log:
//   2008-10-09: initial version
// ----------------------------------------------------------------------------
// $Id: SerialParams.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.conn.serial;

/**
 * Represents the parameters for opening a serial port
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class SerialParams {

	private Parity parity;

	private FlowControl flowControl;

	private DataBits dataBits;

	private StopBits stopBits;

	public SerialParams(String params) throws IllegalArgumentException {
		if (params == null)
			throw new IllegalArgumentException(
					"Serial parameters string cannot be null");
		params = params.trim();
		if (params.length() != 4)
			throw new IllegalArgumentException(
					"Serial parameters string should be 4 characters long");
		String strDataBits = params.substring(0, 1);
		String strParity = params.substring(1, 2);
		String strStopBits = params.substring(2, 3);
		String strFlowControl = params.substring(3, 4);
		if (strDataBits.equals("5"))
			dataBits = DataBits._5;
		else if (strDataBits.equals("6"))
			dataBits = DataBits._6;
		else if (strDataBits.equals("7"))
			dataBits = DataBits._7;
		else if (strDataBits.equals("8"))
			dataBits = DataBits._8;
		else
			throw new IllegalArgumentException(
					"Data bits should be an integer between 5 and 8");
		if (strParity.equalsIgnoreCase("O"))
			parity = Parity.ODD;
		else if (strParity.equalsIgnoreCase("E"))
			parity = Parity.EVEN;
		else if (strParity.equalsIgnoreCase("N"))
			parity = Parity.NONE;
		else if (strParity.equalsIgnoreCase("M"))
			parity = Parity.MARK;
		else if (strParity.equalsIgnoreCase("S"))
			parity = Parity.SPACE;
		else
			throw new IllegalArgumentException("Parity should be one character"
					+ " of: O=odd, E=even, N=none, M=mark, S=space");
		if (strStopBits.equals("1"))
			stopBits = StopBits._1;
		else if (strStopBits.equals("."))
			stopBits = StopBits._1_5;
		else if (strStopBits.equals("2"))
			stopBits = StopBits._2;
		else
			throw new IllegalArgumentException("Stop bits should be one "
					+ "character of: 1 . 2 (. stands for 1.5)");
		if (strFlowControl.equalsIgnoreCase("H"))
			flowControl = FlowControl.RTSCTS;
		else if (strFlowControl.equalsIgnoreCase("S"))
			flowControl = FlowControl.XONXOFF;
		else if (strFlowControl.equalsIgnoreCase("N"))
			flowControl = FlowControl.NONE;
		else
			throw new IllegalArgumentException("Flow control should be one "
					+ "character of: H=hardware S=software N=none");
	}

	public SerialParams(DataBits dataBits, StopBits stopBits, Parity parity,
			FlowControl flowControl) {
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		this.flowControl = flowControl;
	}

	public DataBits getDataBits() {
		return dataBits;
	}

	public Parity getParity() {
		return parity;
	}

	public StopBits getStopBits() {
		return stopBits;
	}

	public FlowControl getFlowControl() {
		return flowControl;
	}

}
