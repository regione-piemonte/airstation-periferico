/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: exception for configuration management
// Change log:
//   2008-03-06: initial version
// ----------------------------------------------------------------------------
// $Id: ConfigException.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Exception for configuration management
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ConfigException extends Exception {

	private static final long serialVersionUID = -2519634117224007464L;
	private String localMessage = null;
	private Object[] messageArgs = null;

	public ConfigException() {
	}

	public ConfigException(String message) {
		super(message);
		this.localMessage = message;
	}

	public ConfigException(Throwable cause) {
		super(cause);
	}

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
		this.localMessage = message;
	}

	public ConfigException(String message, Object... args) {
		super(makeFallbackMessage(message, args));
		this.localMessage = message;
		this.messageArgs = args;
	}

	@Override
	public String toString() {
		if (localMessage == null)
			return super.toString();
		return getLocalizedMessage("en");
	}

	@Override
	public String getMessage() {
		if (localMessage == null)
			return super.getMessage();
		return getLocalizedMessage("en");
	}

	@Override
	public String getLocalizedMessage() {
		if (localMessage == null)
			return super.getLocalizedMessage();
		return getLocalizedMessage("en");
	}

	public String getLocalizedMessage(String localeId) {
		if (localMessage == null)
			return super.getMessage();
		Locale locale;
		if (localeId == null)
			locale = Locale.getDefault();
		else
			locale = new Locale(localeId.equals("en") ? "" : localeId);
		ResourceBundle bundle;
		String msgBundle;
		try {
			bundle = ResourceBundle.getBundle(
					"it/csi/periferico/MessageBundleCore", locale);
			msgBundle = bundle.getString(localMessage);
		} catch (MissingResourceException mrex) {
			return super.getMessage();
		}
		if (messageArgs == null)
			return msgBundle;
		Object[] translatedArgs = new Object[messageArgs.length];
		for (int i = 0; i < messageArgs.length; i++) {
			if (!(messageArgs[i] instanceof String))
				translatedArgs[i] = messageArgs[i];
			else {
				try {
					translatedArgs[i] = bundle.getString(messageArgs[i]
							.toString());
				} catch (MissingResourceException mrex) {
					translatedArgs[i] = messageArgs[i];
				}
			}
		}
		try {
			return MessageFormat.format(msgBundle, translatedArgs);
		} catch (Exception ex) {
			return makeFallbackMessage(msgBundle, translatedArgs);
		}
	}

	private static String makeFallbackMessage(String message, Object[] args) {
		StringBuilder sb = new StringBuilder();
		if (message != null)
			sb.append(message);
		if (args != null)
			for (Object obj : args)
				if (obj != null)
					sb.append(", ").append(obj);
		return sb.toString();
	}

}
