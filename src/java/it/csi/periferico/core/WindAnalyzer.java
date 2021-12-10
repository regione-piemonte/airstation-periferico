/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: implements a wind analyzer
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: WindAnalyzer.java,v 1.24 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.config.common.ConfigException;

/**
 * Implements a wind analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class WindAnalyzer extends AnalogItfAnalyzer {

	private static final long serialVersionUID = -2493630944410245663L;

	private WindElement wind;

	public WindAnalyzer() {
		wind = new WindElement();
		wind.setSampleElementHolder(this);
	}

	public WindElement getWind() {
		return wind;
	}

	public WindElement getElement(String paramId) {
		if (paramId == null)
			return null;
		if (paramId.equals(wind.getParameterId()))
			return wind;
		return null;
	}

	public void setWind(WindElement wind) {
		this.wind = wind;
	}

	@Override
	public void checkConfig() throws ConfigException {
		if (getStatus() == Analyzer.Status.DELETED)
			return;
		super.checkConfig();
		wind.checkConfig();
	}

	@Override
	public Element[] getElements() {
		return new Element[] { wind };
	}

	@Override
	public void initConfig() {
		super.initConfig();
		wind.setAnalyzerId(getId());
		wind.setSampleElementHolder(this);
	}

	@Override
	void onDelete() {
		super.onDelete();
		if (wind != null) {
			if (wind.getSpeed() != null) {
				IOProvider ioProvider = wind.getSpeed().getIOProvider();
				if (ioProvider != null)
					ioProvider.unbind();
			}
			if (wind.getDirection() != null) {
				IOProvider ioProvider = wind.getDirection().getIOProvider();
				if (ioProvider != null)
					ioProvider.unbind();
			}
		}
	}

	@Override
	void isRangeSupportedByIOProviders(double minVoltage, double maxVoltage,
			boolean minRangeExtension, boolean maxRangeExtension)
			throws ConfigException {
		if (wind != null) {
			if (wind.getSpeed() != null)
				wind.getSpeed().isRangeSupportedByIOProvider(minVoltage,
						maxVoltage, minRangeExtension, maxRangeExtension);
			if (wind.getDirection() != null)
				wind.getDirection().isRangeSupportedByIOProvider(minVoltage,
						maxVoltage, minRangeExtension, maxRangeExtension);
		}
	}

	@Override
	public boolean hasBindedElements() {
		if (wind != null) {
			if (wind.getSpeed() != null)
				if (wind.getSpeed().getBoardBindInfo() != null)
					return true;
			if (wind.getDirection() != null)
				if (wind.getDirection().getBoardBindInfo() != null)
					return true;
		}
		return false;
	}
}
