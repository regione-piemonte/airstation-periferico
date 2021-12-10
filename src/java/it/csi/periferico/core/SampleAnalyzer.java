/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: implements a sample analyzer
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: SampleAnalyzer.java,v 1.33 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.boards.IOProvider;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.Parameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a sample analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class SampleAnalyzer extends AnalogItfAnalyzer implements
		ObservableAnalyzer {

	private static final long serialVersionUID = -5013759706195149809L;

	private List<SampleElement> listElements = new ArrayList<SampleElement>();

	private SampleElement newElement = null;

	private CalibAutoCheck calibrationCheck = null;

	public List<SampleElement> getListElements() {
		return listElements;
	}

	public void setListElements(List<SampleElement> listElements) {
		this.listElements = listElements;
		if (listElements != null)
			for (SampleElement se : listElements)
				se.setSampleElementHolder(this);
	}

	public SampleElement makeNewElement(Parameter param) throws ConfigException {
		for (SampleElement element : listElements) {
			if (param.getId().equals(element.getParameterId()))
				throw new ConfigException("error_insert_duplicate");
		}
		newElement = new SampleElement();
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

	public SampleElement getElement(String paramId) {
		paramId = trim(paramId);
		for (SampleElement element : listElements) {
			if (paramId.equals(element.getParameterId()))
				return element;
		}
		if (newElement != null && paramId.equals(newElement.getParameterId()))
			return newElement;
		return null;
	}

	public boolean deleteElement(String paramId) {
		paramId = trim(paramId);
		Iterator<SampleElement> it = listElements.iterator();
		while (it.hasNext()) {
			SampleElement element = it.next();
			if (paramId.equals(element.getParameterId())) {
				it.remove();
				element.unbindObservers();
				return true;
			}
		}
		if (newElement != null && paramId.equals(newElement.getParameterId())) {
			newElement.unbindObservers();
			newElement = null;
			return true;
		}
		return false;
	}

	public CalibAutoCheck getCalibrationCheck() {
		return calibrationCheck;
	}

	public void setCalibrationCheck(CalibAutoCheck calibrationCheck) {
		this.calibrationCheck = calibrationCheck;
	}

	public CalibAutoCheck makeNewCalibrationCheck() {
		return new CalibAutoCheck();
	}

	@Override
	public void checkConfig() throws ConfigException {
		if (getStatus() == Analyzer.Status.DELETED)
			return;
		super.checkConfig();
		for (SampleElement se : listElements)
			se.checkConfig();
		if (calibrationCheck != null)
			calibrationCheck.checkConfig();
	}

	@Override
	public void initConfig() {
		super.initConfig();
		if (listElements != null) {
			for (SampleElement se : listElements) {
				se.setSampleElementHolder(this);
				se.setAnalyzerId(getId());
			}
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
			for (SampleElement se : listElements) {
				se.unbindObservers();
				IOProvider ioProvider = se.getIOProvider();
				if (ioProvider != null)
					ioProvider.unbind();
			}
		}
	}

	@Override
	void isRangeSupportedByIOProviders(double minVoltage, double maxVoltage,
			boolean minRangeExtension, boolean maxRangeExtension)
			throws ConfigException {
		for (SampleElement se : listElements)
			se.isRangeSupportedByIOProvider(minVoltage, maxVoltage,
					minRangeExtension, maxRangeExtension);
	}

	@Override
	public boolean hasBindedElements() {
		if (listElements == null || listElements.isEmpty())
			return false;
		for (SampleElement se : listElements)
			if (se.getBoardBindInfo() != null)
				return true;
		return false;
	}

}
