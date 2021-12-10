/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: analyzer that computes averages
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: AvgAnalyzer.java,v 1.25 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.Parameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Analyzer that computes averages
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class AvgAnalyzer extends AnalogItfAnalyzer implements
		ObservableAnalyzer {

	private static final long serialVersionUID = -2100933318599779681L;

	private List<AvgElement> listElements = new ArrayList<AvgElement>();

	private AvgElement newElement = null;

	public List<AvgElement> getListElements() {
		return listElements;
	}

	public void setListElements(List<AvgElement> listElements) {
		this.listElements = listElements;
		if (listElements != null)
			for (SampleElement se : listElements)
				se.setSampleElementHolder(this);
	}

	public AvgElement makeNewElement(Parameter param) throws ConfigException {
		for (AvgElement element : listElements) {
			if (param.getId().equals(element.getParameterId()))
				throw new ConfigException("error_insert_duplicate");
		}
		newElement = new AvgElement();
		newElement.setParameterId(param.getId());
		newElement.setSampleElementHolder(this);
		return newElement;
	}

	public boolean isNewElement(SampleElement se) {
		return (se == newElement);
	}

	public boolean insertNewElement() {
		if (newElement == null)
			throw new IllegalStateException("No new element to insert");
		listElements.add(newElement);
		newElement = null;
		return true;
	}

	public AvgElement getElement(String paramId) {
		paramId = trim(paramId);
		for (AvgElement element : listElements) {
			if (paramId.equals(element.getParameterId()))
				return element;
		}
		if (newElement != null && paramId.equals(newElement.getParameterId()))
			return newElement;
		return null;
	}

	public boolean deleteElement(String paramId) {
		paramId = trim(paramId);
		Iterator<AvgElement> it = listElements.iterator();
		while (it.hasNext()) {
			AvgElement element = it.next();
			if (paramId.equals(element.getParameterId())) {
				it.remove();
				return true;
			}
		}
		if (newElement != null && paramId.equals(newElement.getParameterId())) {
			newElement = null;
			return true;
		}
		return false;
	}

	@Override
	public void checkConfig() throws ConfigException {
		if (getStatus() == Analyzer.Status.DELETED)
			return;
		super.checkConfig();
		for (AvgElement ae : listElements)
			ae.checkConfig();
	}

	@Override
	public void initConfig() {
		super.initConfig();
		if (listElements != null)
			for (AvgElement ae : listElements) {
				ae.setSampleElementHolder(this);
				ae.setAnalyzerId(getId());
			}
	}

	@Override
	public Element[] getElements() {
		if (listElements == null)
			return new Element[0];
		return listElements.toArray(new Element[listElements.size()]);
	}

	@Override
	void onDelete() {
		super.onDelete();
		if (listElements != null) {
			for (AvgElement ae : listElements) {
				IOProvider ioProvider = ae.getIOProvider();
				if (ioProvider != null)
					ioProvider.unbind();
			}
		}
	}

	@Override
	void isRangeSupportedByIOProviders(double minVoltage, double maxVoltage,
			boolean minRangeExtension, boolean maxRangeExtension)
			throws ConfigException {
		for (AvgElement ae : listElements)
			ae.isRangeSupportedByIOProvider(minVoltage, maxVoltage,
					minRangeExtension, maxRangeExtension);
	}

	@Override
	public boolean hasBindedElements() {
		if (listElements == null || listElements.isEmpty())
			return false;
		for (AvgElement ae : listElements)
			if (ae.getBoardBindInfo() != null)
				return true;
		return false;
	}
}
