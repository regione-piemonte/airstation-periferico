/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for container alarms
// Change log:
//   2008-03-20: initial version
// ----------------------------------------------------------------------------
// $Id: ContainerAlarm.java,v 1.11 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.config.common.AlarmName;
import it.csi.periferico.config.common.AlarmName.AlarmType;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;
import it.csi.periferico.storage.StorageException;

import java.util.UUID;

/**
 * Base class for container alarms
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ContainerAlarm extends ConfigItem {

	private static final long serialVersionUID = -5781454254954368926L;

	private UUID id;

	private String description = "";

	public static ContainerAlarm getInstance(AlarmName an) {
		if (an == null)
			throw new IllegalArgumentException("Null argument not allowed");
		if (an.getType() == AlarmType.DIGITAL) {
			DigitalContainerAlarm dca = new DigitalContainerAlarm();
			dca.setDigitalAlarm(new DigitalAlarm(an.getId()));
			return dca;
		}
		if (an.getType() == AlarmType.TRIGGER) {
			TriggerContainerAlarm tca = new TriggerContainerAlarm();
			tca.setTriggerAlarm(new TriggerAlarm(an.getId()));
			return tca;
		}
		throw new IllegalStateException("Undefined alarm type");
	}

	public ContainerAlarm() {
		id = UUID.randomUUID();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getIdAsString() {
		if (id == null)
			return "";
		return id.toString();
	}

	public void setIdAsString(String strId) {
		this.id = UUID.fromString(trim(strId));
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = trim(description);
	}

	public abstract Alarm getAlarm();

	public String getName() {
		return "Container";
	}

	public boolean isEnabled() {
		return true;
	}

	public void setConfig(String description) {
		this.description = description;
	}

	public boolean isSameConfig(String description) {
		return this.description.equals(trim(description));
	}

	@Override
	public void checkConfig() throws ConfigException {
		Alarm alarm = getAlarm();
		if (alarm != null) {
			alarm.checkConfig();
			CommonCfg cc = Periferico.getInstance().getCommonCfg();
			if (cc.getAlarmName(alarm.getAlarmNameId()) == null) {
				throw new ConfigException("alarmname_not_in_cc",
						alarm.getAlarmNameId());
			}
		}
	}

	abstract void saveData() throws StorageException;

	public int compareNameAndDescription(ContainerAlarm other) {
		if (other == null)
			return 1;
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		String name1 = getAlarmName(cc) + " " + getDescription();
		String name2 = other.getAlarmName(cc) + " " + other.getDescription();
		return name1.compareToIgnoreCase(name2);
	}

	private String getAlarmName(CommonCfg cc) {
		Alarm alarm = getAlarm();
		if (alarm == null || alarm.getAlarmNameId() == null)
			return "";
		AlarmName an = cc.getAlarmName(alarm.getAlarmNameId());
		if (an == null)
			return "";
		return an.getName() == null ? "" : an.getName();
	}

}
