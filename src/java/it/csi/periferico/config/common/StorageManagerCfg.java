/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: configuration for storage manager
// Change log:
//   2008-09-18: initial version
// ----------------------------------------------------------------------------
// $Id: StorageManagerCfg.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common;

/**
 * Configuration for storage manager
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class StorageManagerCfg {

	public static final int DEF_MAX_DAYS_OF_DATA = 31;

	public static final int DEF_MAX_DAYS_OF_AGGREGATE_DATA = 366;

	public static final int DEF_DISK_FULL_WARNING_THRESHOLD_PERCENT = 80;

	public static final int DEF_DISK_FULL_ALARM_THRESHOLD_PERCENT = 90;

	private int maxDaysOfData = DEF_MAX_DAYS_OF_DATA;

	private int maxDaysOfAggregateData = DEF_MAX_DAYS_OF_AGGREGATE_DATA;

	private int diskFullWarningThresholdPercent = DEF_DISK_FULL_WARNING_THRESHOLD_PERCENT;

	private int diskFullAlarmThresholdPercent = DEF_DISK_FULL_ALARM_THRESHOLD_PERCENT;

	public int getDiskFullAlarmThresholdPercent() {
		return diskFullAlarmThresholdPercent;
	}

	public void setDiskFullAlarmThresholdPercent(
			int diskFullAlarmThresholdPercent) {
		if (diskFullAlarmThresholdPercent < 1)
			diskFullAlarmThresholdPercent = 1;
		else if (diskFullAlarmThresholdPercent > 100)
			diskFullAlarmThresholdPercent = 100;
		this.diskFullAlarmThresholdPercent = diskFullAlarmThresholdPercent;
	}

	public int getDiskFullWarningThresholdPercent() {
		return diskFullWarningThresholdPercent;
	}

	public void setDiskFullWarningThresholdPercent(
			int diskFullWarningThresholdPercent) {
		if (diskFullWarningThresholdPercent < 1)
			diskFullWarningThresholdPercent = 1;
		else if (diskFullWarningThresholdPercent > 100)
			diskFullWarningThresholdPercent = 100;
		this.diskFullWarningThresholdPercent = diskFullWarningThresholdPercent;
	}

	public int getMaxDaysOfAggregateData() {
		return maxDaysOfAggregateData;
	}

	public void setMaxDaysOfAggregateData(int maxDaysOfAggregateData) {
		if (maxDaysOfAggregateData < 10)
			maxDaysOfAggregateData = 10;
		this.maxDaysOfAggregateData = maxDaysOfAggregateData;
	}

	public int getMaxDaysOfData() {
		return maxDaysOfData;
	}

	public void setMaxDaysOfData(int maxDaysOfData) {
		if (maxDaysOfData < 1)
			maxDaysOfData = 1;
		this.maxDaysOfData = maxDaysOfData;
	}

}
