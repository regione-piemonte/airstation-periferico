/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for container alarms data storage
// Change log:
//   2008-06-25: initial version
// ----------------------------------------------------------------------------
// $Id: ContainerAlarmDataStorage.java,v 1.6 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Base class for container alarms data storage
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class ContainerAlarmDataStorage extends DataStorage {

	static final String DIR_CONTAINER_ALARMS = "container_alarms";

	private static Logger logger = Logger.getLogger("periferico."
			+ ContainerAlarmDataStorage.class.getSimpleName());

	private UUID containerAlarmId;

	private String alarmDescription;

	ContainerAlarmDataStorage(NewFileRate newFileRate, UUID stationId,
			String stationName, UUID containerAlarmId, String alarmDescription) {
		super(newFileRate, stationId, stationName);
		if (containerAlarmId == null)
			throw new IllegalArgumentException(
					"Container alarm ID cannot be null");
		this.containerAlarmId = containerAlarmId;
		this.alarmDescription = alarmDescription;
	}

	UUID getContainerAlarmId() {
		return containerAlarmId;
	}

	String getAlarmDescription() {
		return alarmDescription;
	}

	void makeDir() throws StorageException {
		super.makeDir();
		if (alarmDescription == null)
			return;
		String fileName = super.getDirName() + File.separator
				+ DIR_CONTAINER_ALARMS + File.separator + containerAlarmId
				+ File.separator + alarmDescription + ".an";
		File file = new File(fileName);
		try {
			if (file.createNewFile())
				logger.info("Created alarm description file: " + file);
		} catch (IOException e) {
			logger.error("Cannot create alarm description file: " + file, e);
		}
	}

	String getDirName() {
		return super.getDirName() + File.separator + DIR_CONTAINER_ALARMS
				+ File.separator + containerAlarmId;
	}

	static List<UUID> listContainerAlarms() {
		List<UUID> list = new ArrayList<UUID>();
		File dir = new File(DATA_DIR + File.separator + DIR_CONTAINER_ALARMS);
		if (dir.isDirectory()) {
			File[] subdirs = dir.listFiles(new UUIDFilenameFilter());
			if (subdirs != null)
				for (int i = 0; i < subdirs.length; i++)
					if (subdirs[i].isDirectory())
						list.add(UUID.fromString((subdirs[i].getName())));
		}
		return list;
	}

}
