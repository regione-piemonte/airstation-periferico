/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: the result when a new configuration is set 
// Change log:
//   2008-03-28: initial version
// ----------------------------------------------------------------------------
// $Id: SetConfigResult.java,v 1.5 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config;

import it.csi.periferico.config.common.ConfigException;

/**
 * The result when a new configuration is set
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class SetConfigResult {

	public enum Result {
		ACTIVATED, CHECK_OK, CHECK_ERROR, OBSOLETE, HISTORIC, ACTIVATION_ERROR
	}

	private Result result;

	private ConfigException error = null;

	public SetConfigResult(Result result) {
		this(result, null);
	}

	public SetConfigResult(Result result, ConfigException error) {
		this.result = result;
		this.error = error;
	}

	public Result getResult() {
		return result;
	}

	public ConfigException getError() {
		return error;
	}

}
