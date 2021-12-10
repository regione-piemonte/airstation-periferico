/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for analyzers data storage
// Change log:
//   2008-06-21: initial version
// ----------------------------------------------------------------------------
// $Id: AnalyzerDataStorage.java,v 1.6 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Base class for analyzers data storage
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
abstract class AnalyzerDataStorage extends DataStorage {

	static final String DIR_ANALYZERS = "analyzers";

	private static Logger logger = Logger.getLogger("periferico."
			+ AnalyzerDataStorage.class.getSimpleName());

	private UUID analyzerId;

	private String analyzerName;

	AnalyzerDataStorage(NewFileRate newFileRate, UUID stationId,
			String stationName, UUID analyzerId, String analyzerName) {
		super(newFileRate, stationId, stationName);
		if (analyzerId == null)
			throw new IllegalArgumentException("Analyzer ID cannot be null");
		this.analyzerId = analyzerId;
		this.analyzerName = analyzerName;
	}

	UUID getAnalyzerId() {
		return analyzerId;
	}

	String getAnalyzerName() {
		return analyzerName;
	}

	@Override
	void makeDir() throws StorageException {
		super.makeDir();
		if (analyzerName == null)
			return;
		String fileName = super.getDirName() + File.separator + DIR_ANALYZERS
				+ File.separator + analyzerId + File.separator + analyzerName
				+ ".an";
		File file = new File(fileName);
		try {
			if (file.createNewFile())
				logger.info("Created analyzer name file: " + file);
		} catch (IOException e) {
			logger.error("Cannot create analyzer name file: " + file, e);
		}
	}

	@Override
	String getDirName() {
		return super.getDirName() + File.separator + DIR_ANALYZERS
				+ File.separator + analyzerId;
	}

	static List<UUID> listAnalyzers() {
		List<UUID> list = new ArrayList<UUID>();
		File dir = new File(DATA_DIR + File.separator + DIR_ANALYZERS);
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
