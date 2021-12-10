/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for all elements
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: Element.java,v 1.21 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;
import it.csi.periferico.storage.StorageException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for all elements
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public abstract class Element extends ConfigItem {

	private static final long serialVersionUID = 7276095984218023067L;

	private boolean enabled = true;

	private String parameterId;

	private List<Integer> avgPeriods = new ArrayList<Integer>();

	private transient UUID analyzerId;

	private transient Map<Integer, AggregateValue> lastAggregateValues = new HashMap<Integer, AggregateValue>();

	// TODO: evaluate if this variable should be persistent when application is
	// restarted
	private transient boolean calibAutoCheckFailed = false;

	public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = trim(parameterId);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<Integer> getAvgPeriods() {
		return avgPeriods;
	}

	public void setAvgPeriods(List<Integer> avgPeriods) {
		this.avgPeriods = avgPeriods;
	}

	public boolean insertAvgPeriod(Integer avgPeriod) {
		if (avgPeriods.contains(avgPeriod))
			return false;
		avgPeriods.add(avgPeriod);
		return true;
	}

	public boolean deleteAvgPeriod(Integer avgPeriod) {
		return avgPeriods.remove(avgPeriod);
	}

	public void setConfig(boolean enabled) throws ConfigException {
		setEnabled(enabled);
	}

	public boolean isSameConfig(boolean enabled) {
		return this.enabled == enabled;
	}

	@Override
	public void checkConfig() throws ConfigException {
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		if (cc.getParameter(parameterId) == null)
			throw new ConfigException("parameter_not_in_cc", parameterId);
		for (Integer avgPeriod : avgPeriods) {
			if (!cc.getAvgPeriods().contains(avgPeriod))
				throw new ConfigException("avgperiod_not_in_cc", avgPeriod);
		}
	}

	public boolean isCalibAutoCheckFailed() {
		return calibAutoCheckFailed;
	}

	public void setCalibAutoCheckFailed(boolean calibAutoCheckFailed) {
		this.calibAutoCheckFailed = calibAutoCheckFailed;
	}

	public AggregateValue getLastAggregateValue(Integer avgPeriod) {
		AggregateValue av = lastAggregateValues.get(avgPeriod);
		// This may happen in case of clock adjustment !
		if (av == null || av.getTimestamp().after(new Date()))
			return null;
		return av;
	}

	public void setLastAggregateValue(Integer avgPeriod,
			AggregateValue aggregateValue) {
		lastAggregateValues.put(avgPeriod, aggregateValue);
	}

	List<Date> computeAggregationTimes(Date startTime, Date endTime,
			int period_min) {
		if (period_min <= 0)
			throw new IllegalStateException(
					"The aggregation period must not be less than 1 minute");
		if (period_min > 1440)
			throw new IllegalStateException(
					"The aggregation period must not be  greater than 24 hours");
		List<Date> aggregationTimes = new ArrayList<Date>();
		Calendar aggCal = new GregorianCalendar();
		aggCal.setTime(startTime);
		if (60 % period_min != 0)
			aggCal.set(Calendar.HOUR_OF_DAY, 0);
		if (period_min > 1)
			aggCal.set(Calendar.MINUTE, 0);
		aggCal.set(Calendar.SECOND, 0);
		aggCal.set(Calendar.MILLISECOND, 0);
		Date aggTime = aggCal.getTime();
		while (!aggTime.after(endTime)) {
			if (aggTime.after(startTime))
				aggregationTimes.add(aggTime);
			aggCal.add(Calendar.MINUTE, period_min);
			aggTime = aggCal.getTime();
		}
		return aggregationTimes;
	}

	abstract void computeAndSaveAggregateValues(Date dataManagerStartTime,
			Date aggregationComputeEndTime);

	abstract void readAndSetLastAggregateValues(
			Date aggregationsComputeStartTime);

	abstract void saveData() throws StorageException;

	UUID getAnalyzerId() {
		return analyzerId;
	}

	void setAnalyzerId(UUID analyzerId) {
		this.analyzerId = analyzerId;
	}

}
