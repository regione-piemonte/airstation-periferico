/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: implements a rain analyzer
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: RainAnalyzer.java,v 1.26 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.Parameter;

import java.util.List;

/**
 * Implements a rain analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class RainAnalyzer extends IOItfAnalyzer implements ElementHolder {

	private static final long serialVersionUID = 5132418754795394175L;

	private CounterElement rainElement;

	public RainAnalyzer() {
		rainElement = new CounterElement();
		rainElement.setElementHolder(this);
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		List<Parameter> list = cc.getParameters(Parameter.ParamType.RAIN);
		// One and only one rain parameter is expected!
		Parameter rainParam = list.get(0);
		rainElement.setParameterId(rainParam.getId());
		List<String> muNames = cc.getMeasureUnitNames(rainParam
				.getPhysicalDimension());
		if (muNames.size() > 0)
			rainElement.setMeasureUnitName(muNames.get(0));
	}

	public CounterElement getRainElement() {
		return rainElement;
	}

	public CounterElement getElement(String paramId) {
		if (paramId == null)
			return null;
		if (paramId.equals(rainElement.getParameterId()))
			return rainElement;
		return null;
	}

	public void setRainElement(CounterElement rainElement) {
		if (rainElement == null)
			throw new IllegalArgumentException("Null argument not allowed");
		rainElement.setElementHolder(this);
		this.rainElement = rainElement;
	}

	@Override
	public void checkConfig() throws ConfigException {
		if (getStatus() == Analyzer.Status.DELETED)
			return;
		super.checkConfig();
		rainElement.checkConfig();
	}

	@Override
	public Element[] getElements() {
		return new Element[] { rainElement };
	}

	@Override
	public void initConfig() {
		super.initConfig();
		rainElement.setAnalyzerId(getId());
	}

	@Override
	void onDelete() {
		super.onDelete();
		if (rainElement != null) {
			IOProvider ioProvider = rainElement.getIOProvider();
			if (ioProvider != null)
				ioProvider.unbind();
		}
	}

	@Override
	public void setConfig(String name, String brand, String description,
			String model, String serialNumber, String userNotes,
			String strStatus, String uiURL) throws ConfigException {
		super.setConfig(name, brand, description, model, serialNumber,
				userNotes, strStatus, uiURL);
	}

	@Override
	public boolean isSameConfig(String name, String brand, String description,
			String model, String serialNumber, String userNotes,
			String strStatus, String uiURL) {
		return super.isSameConfig(name, brand, description, model,
				serialNumber, userNotes, strStatus, uiURL);
	}

}
