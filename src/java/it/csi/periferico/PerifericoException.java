/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for application's exceptions
// Change log:
//   2008-04-11: initial version
// ----------------------------------------------------------------------------
// $Id: PerifericoException.java,v 1.11 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico;

import java.text.MessageFormat;
import java.util.MissingResourceException;

/**
 * Base class for application's exceptions
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class PerifericoException extends Exception {

	private static final long serialVersionUID = -6292288720142066744L;

	private Object[] args;

	private String msg;

	private PropertyUtil propertyUtil;

	/**
	 * 
	 */
	public PerifericoException() {
	}

	/**
	 * @param message
	 */
	public PerifericoException(String message) {
		super(message);
		this.msg = message;
		propertyUtil = new PropertyUtil("it/csi/periferico/MessageBundleCore");
	}

	/**
	 * @param cause
	 */
	public PerifericoException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PerifericoException(String message, Throwable cause) {
		super(message, cause);
		this.msg = message;
		propertyUtil = new PropertyUtil("it/csi/periferico/MessageBundleCore");
	}

	/**
	 * @param msg
	 * @param args
	 */
	public PerifericoException(String msg, Object... args) {
		this.msg = msg;
		this.args = args;
		propertyUtil = new PropertyUtil("it/csi/periferico/MessageBundleCore");
	}

	@Override
	public String toString() {
		if (msg == null)
			return super.toString();
		return getLocalizedMessage("en");
	}

	@Override
	public String getMessage() {
		if (msg == null)
			return super.getMessage();
		return getLocalizedMessage("en");
	}

	@Override
	public String getLocalizedMessage() {
		if (msg == null)
			return super.getLocalizedMessage();
		return getLocalizedMessage("en");
	}

	public String getLocalizedMessage(String locale) {
		if (msg == null)
			return getMessage();
		String msgBundle = null;
		try {
			msgBundle = propertyUtil.getProperty(locale, msg);
		} catch (MissingResourceException mrex) {
			return super.getMessage();
		}
		if (args == null)
			return msgBundle;
		Object[] translatedArgs = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			if (!(args[i] instanceof String))
				translatedArgs[i] = args[i];
			else {
				try {
					translatedArgs[i] = propertyUtil.getProperty(locale,
							args[i].toString());
				} catch (MissingResourceException mrex) {
					translatedArgs[i] = args[i];
				}
			}
		}
		try {
			return MessageFormat.format(msgBundle, translatedArgs);
		} catch (Exception ex) {
			return msgBundle;
		}
	}

}
