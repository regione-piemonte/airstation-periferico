/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: defines a point for automatic calibration check
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: CheckPoint.java,v 1.10 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;
import it.csi.periferico.config.common.Parameter;

import java.util.Iterator;
import java.util.List;

/**
 * Defines a point for automatic calibration check
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
// TODO: this class is only the starting point for future implementation
public class CheckPoint extends ConfigItem {

	private static final long serialVersionUID = -6397776659686661783L;

	private String description;

	private int cylinderOpeningTime; // in seconds, from start of calibration

	private int valueReadTime;

	private int cylinderClosingTime;

	private boolean commandLogicActiveHigh;

	// TODO: for future implementation
	// private DigitalOutput digitalOutput;

	private List<ElementPoint> listElementPoints;

	private ElementPoint newElementPoint;

	public boolean isCommandLogicActiveHigh() {
		return commandLogicActiveHigh;
	}

	public void setCommandLogicActiveHigh(boolean commandLogicActiveHigh) {
		this.commandLogicActiveHigh = commandLogicActiveHigh;
	}

	public int getCylinderClosingTime() {
		return cylinderClosingTime;
	}

	public void setCylinderClosingTime(int cylinderClosingTime) {
		this.cylinderClosingTime = cylinderClosingTime;
	}

	public int getCylinderOpeningTime() {
		return cylinderOpeningTime;
	}

	public void setCylinderOpeningTime(int cylinderOpeningTime) {
		this.cylinderOpeningTime = cylinderOpeningTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getValueReadTime() {
		return valueReadTime;
	}

	public void setValueReadTime(int valueReadTime) {
		this.valueReadTime = valueReadTime;
	}

	public List<ElementPoint> getListElementPoints() {
		return listElementPoints;
	}

	public void setListElementPoints(List<ElementPoint> listElementPoints) {
		this.listElementPoints = listElementPoints;
	}

	public ElementPoint makeNewElementPoint(Parameter param)
			throws ConfigException {
		for (ElementPoint ep : listElementPoints) {
			if (param.getId().equals(ep.getParameterId()))
				throw new ConfigException("error_insert_duplicate");
		}
		newElementPoint = new ElementPoint();
		newElementPoint.setParameterId(param.getId());
		return newElementPoint;
	}

	public boolean isNewElementPoint(ElementPoint ep) {
		return (ep == newElementPoint);
	}

	public boolean insertNewElementPoint() {
		if (newElementPoint == null)
			throw new IllegalStateException("No new element to insert");
		listElementPoints.add(newElementPoint);
		newElementPoint = null;
		return true;
	}

	public ElementPoint getElementPoint(String paramId) {
		paramId = trim(paramId);
		for (ElementPoint ep : listElementPoints) {
			if (paramId.equals(ep.getParameterId()))
				return ep;
		}
		if (newElementPoint != null
				&& paramId.equals(newElementPoint.getParameterId()))
			return newElementPoint;
		return null;
	}

	public boolean deleteElementPoint(String paramId) {
		paramId = trim(paramId);
		Iterator<ElementPoint> it = listElementPoints.iterator();
		while (it.hasNext()) {
			ElementPoint ep = it.next();
			if (paramId.equals(ep.getParameterId())) {
				it.remove();
				return true;
			}
		}
		if (newElementPoint != null
				&& paramId.equals(newElementPoint.getParameterId())) {
			newElementPoint = null;
			return true;
		}
		return false;
	}

	public void setConfig(String description, int cylinderOpeningTime,
			int valueReadTime, int cylinderClosingTime,
			boolean commandLogicActiveHigh) throws ConfigException {
		checkTimes(cylinderOpeningTime, valueReadTime, cylinderClosingTime);
		setDescription(description);
		setCylinderOpeningTime(cylinderOpeningTime);
		setValueReadTime(valueReadTime);
		setCylinderClosingTime(cylinderClosingTime);
		setCommandLogicActiveHigh(commandLogicActiveHigh);
	}

	public boolean isSameConfig(String description, int cylinderOpeningTime,
			int valueReadTime, int cylinderClosingTime,
			boolean commandLogicActiveHigh) {
		return this.description.equals(trim(description))
				&& this.cylinderOpeningTime == cylinderOpeningTime
				&& this.valueReadTime == valueReadTime
				&& this.cylinderClosingTime == cylinderClosingTime
				&& this.commandLogicActiveHigh == commandLogicActiveHigh;
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkTimes(cylinderOpeningTime, valueReadTime, cylinderClosingTime);
		for (ElementPoint ep : listElementPoints)
			ep.checkConfig();
	}

	private void checkTimes(int cylinderOpeningTime, int valueReadTime,
			int cylinderClosingTime) throws ConfigException {
		if (cylinderOpeningTime < 0 || valueReadTime < 0
				|| cylinderClosingTime < 0
				|| cylinderOpeningTime >= valueReadTime
				|| valueReadTime > cylinderClosingTime)
			throw new ConfigException("incoherent_calib");
	}

}
