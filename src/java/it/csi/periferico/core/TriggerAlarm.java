/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: implements an alarm that observes an analog input and
//                  triggers when its thresholds are reached
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: TriggerAlarm.java,v 1.21 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.PerifericoUtil;
import it.csi.periferico.config.common.ConfigException;

import java.util.UUID;

/**
 * Implements an alarm that observes an analog input and triggers when its
 * thresholds are reached
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class TriggerAlarm extends Alarm implements SampleObserver {

	private static final long serialVersionUID = 5221340318635661430L;

	private UUID observedAnalyzerId;

	private String observedParameterId;

	private Double alarmThresholdHigh;

	private Double alarmThresholdLow;

	private Double warningThresholdHigh;

	private Double warningThresholdLow;

	private transient AlarmStatusCollector statusCollector = null;

	private transient boolean active = false;

	public TriggerAlarm() {
		super();
	}

	public TriggerAlarm(String alarmNameId) {
		super(alarmNameId);
	}

	public UUID getObservedAnalyzerId() {
		return observedAnalyzerId;
	}

	public void setObservedAnalyzerId(UUID elementId) {
		this.observedAnalyzerId = elementId;
	}

	public String getObservedAnalyzerIdAsString() {
		if (observedAnalyzerId == null)
			return null;
		return observedAnalyzerId.toString();
	}

	public void setObservedAnalyzerIdAsString(String strId) {
		this.observedAnalyzerId = UUID.fromString(trim(strId));
	}

	public String getObservedParameterId() {
		return observedParameterId;
	}

	public void setObservedParameterId(String observedParameterId) {
		this.observedParameterId = observedParameterId;
	}

	public Double getAlarmThresholdHigh() {
		return alarmThresholdHigh;
	}

	public void setAlarmThresholdHigh(Double alarmThresholdHigh) {
		this.alarmThresholdHigh = alarmThresholdHigh;
	}

	public Double getAlarmThresholdLow() {
		return alarmThresholdLow;
	}

	public void setAlarmThresholdLow(Double alarmThresholdLow) {
		this.alarmThresholdLow = alarmThresholdLow;
	}

	public Double getWarningThresholdHigh() {
		return warningThresholdHigh;
	}

	public void setWarningThresholdHigh(Double warningThresholdHigh) {
		this.warningThresholdHigh = warningThresholdHigh;
	}

	public Double getWarningThresholdLow() {
		return warningThresholdLow;
	}

	public void setWarningThresholdLow(Double warningThresholdLow) {
		this.warningThresholdLow = warningThresholdLow;
	}

	public void setConfig(boolean enabled, Double alarmThresholdHigh,
			Double warningThresholdHigh, Double warningThresholdLow,
			Double alarmThresholdLow) throws ConfigException {
		checkThresholds(alarmThresholdHigh, warningThresholdHigh,
				warningThresholdLow, alarmThresholdLow);
		super.setConfig(enabled);
		setAlarmThresholdHigh(alarmThresholdHigh);
		setWarningThresholdHigh(warningThresholdHigh);
		setWarningThresholdLow(warningThresholdLow);
		setAlarmThresholdLow(alarmThresholdLow);
	}

	public boolean isSameConfig(boolean enabled, Double alarmThresholdHigh,
			Double warningThresholdHigh, Double warningThresholdLow,
			Double alarmThresholdLow) {
		return super.isSameConfig(enabled)
				&& PerifericoUtil.areEqual(this.alarmThresholdHigh,
						alarmThresholdHigh)
				&& PerifericoUtil.areEqual(this.warningThresholdHigh,
						warningThresholdHigh)
				&& PerifericoUtil.areEqual(this.warningThresholdLow,
						warningThresholdLow)
				&& PerifericoUtil.areEqual(this.alarmThresholdLow,
						alarmThresholdLow);
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkThresholds(alarmThresholdHigh, warningThresholdHigh,
				warningThresholdLow, alarmThresholdLow);
	}

	private void checkThresholds(Double alarmThresholdHigh,
			Double warningThresholdHigh, Double warningThresholdLow,
			Double alarmThresholdLow) throws ConfigException {
		String msg = "incoherent_tresholds";
		if (alarmThresholdHigh != null) {
			if (warningThresholdHigh != null
					&& warningThresholdHigh >= alarmThresholdHigh)
				throw new ConfigException(msg);
			if (alarmThresholdLow != null
					&& alarmThresholdLow >= alarmThresholdHigh)
				throw new ConfigException(msg);
			if (warningThresholdLow != null
					&& warningThresholdLow >= alarmThresholdHigh)
				throw new ConfigException(msg);
		}
		if (warningThresholdHigh != null) {
			if (alarmThresholdLow != null
					&& alarmThresholdLow >= warningThresholdHigh)
				throw new ConfigException(msg);
			if (warningThresholdLow != null
					&& warningThresholdLow >= warningThresholdHigh)
				throw new ConfigException(msg);
		}
		if (warningThresholdLow != null) {
			if (alarmThresholdHigh != null
					&& alarmThresholdHigh <= warningThresholdLow)
				throw new ConfigException(msg);
			if (warningThresholdHigh != null
					&& warningThresholdHigh <= warningThresholdLow)
				throw new ConfigException(msg);
		}
		if (alarmThresholdLow != null) {
			if (alarmThresholdHigh != null
					&& alarmThresholdHigh <= alarmThresholdLow)
				throw new ConfigException(msg);
			if (warningThresholdHigh != null
					&& warningThresholdHigh <= alarmThresholdLow)
				throw new ConfigException(msg);
			if (warningThresholdLow != null
					&& warningThresholdLow <= alarmThresholdLow)
				throw new ConfigException(msg);
		}
		if (alarmThresholdHigh == null && warningThresholdHigh == null
				&& warningThresholdLow == null && alarmThresholdLow == null)
			throw new ConfigException("one_threshold");

	}

	AlarmStatusCollector getStatusCollector() {
		return statusCollector;
	}

	void setStatusCollector(AlarmStatusCollector statusCollector) {
		this.statusCollector = statusCollector;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean deliver(Sample sample) {
		if (sample == null)
			return false;
		if (statusCollector == null)
			return false;
		if (sample.getValue() == null)
			return false;
		double value = sample.getValue();
		AlarmStatus.Status status = AlarmStatus.Status.OK;
		if (alarmThresholdHigh != null && value > alarmThresholdHigh)
			status = alarmThresholdLow == null ? AlarmStatus.Status.ALARM
					: AlarmStatus.Status.ALARM_HIGH;
		else if (alarmThresholdLow != null && value < alarmThresholdLow)
			status = alarmThresholdHigh == null ? AlarmStatus.Status.ALARM
					: AlarmStatus.Status.ALARM_LOW;
		else if (warningThresholdHigh != null && value > warningThresholdHigh)
			status = warningThresholdLow == null ? AlarmStatus.Status.WARNING
					: AlarmStatus.Status.WARNING_HIGH;
		else if (warningThresholdLow != null && value < warningThresholdLow)
			status = warningThresholdHigh == null ? AlarmStatus.Status.WARNING
					: AlarmStatus.Status.WARNING_LOW;
		AlarmStatus lastStatus = statusCollector.getLast();
		if (lastStatus != null) {
			if (lastStatus.getStatus().equals(status))
				return true;
		}
		AlarmStatus as = new AlarmStatus(sample.getTimestamp(), status);
		statusCollector.add(as);
		for (StatusObserver so : getListStatusObservers()) {
			so.deliver(as);
		}
		return true;
	}

	public void unbind() {
		observedAnalyzerId = null;
		observedParameterId = null;
	}

}
