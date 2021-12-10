/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception that is thrown by the UI service when an
// operation fails on a Digital Port Analyzer
// Change log:
//   2015-04-21: initial version
// ----------------------------------------------------------------------------
// $Id: DgtAnalyzerException.java,v 1.1 2015/04/29 16:33:37 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client;

/**
 * Exception that is thrown by the UI service when an operation fails on a
 * Digital Port Analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DgtAnalyzerException extends Exception {

	private static final long serialVersionUID = -347702591099513585L;

	public enum Type {
		GENERIC, NOT_CONNECTED, NOT_SUPPORTED
	};

	private Type type;

	public DgtAnalyzerException() {
	}

	/**
	 * @param message
	 */
	public DgtAnalyzerException(String message) {
		this(Type.GENERIC, message);
	}

	/**
	 * @param type
	 * @param cause
	 */
	public DgtAnalyzerException(Type type, String message) {
		super(message);
		this.type = type;
	}

	/**
	 * @param cause
	 */
	public DgtAnalyzerException(Throwable cause) {
		this(Type.GENERIC, cause);
	}

	/**
	 * @param type
	 * @param cause
	 */
	public DgtAnalyzerException(Type type, Throwable cause) {
		super(cause);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

}
