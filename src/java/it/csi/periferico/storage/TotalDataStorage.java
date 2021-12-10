/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: file based data storage for total aggregate data
// Change log:
//   2008-08-06: initial version
// ----------------------------------------------------------------------------
// $Id: TotalDataStorage.java,v 1.5 2009/07/06 15:08:25 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.storage;

import it.csi.periferico.core.TotalValue;
import it.csi.periferico.core.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * File based data storage for total aggregate data
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class TotalDataStorage extends ScalarDataStorage {

	static final String FILE_TYPE = "TOTAL";

	static final String FILE_PREFIX = "t_";

	static final String FILE_DIR = "totals";

	public TotalDataStorage(UUID stationId, String stationName,
			UUID analyzerId, String analyzerName, String paramId,
			int numDecimals, int period) {
		super(stationId, stationName, analyzerId, analyzerName, paramId,
				numDecimals, period);
	}

	List<TotalValue> readTotalData(Date startTime, boolean startTimeIncluded,
			Date endTime, boolean endTimeIncluded, Integer maxData)
			throws StorageException {
		List<Value> data = readData(startTime, startTimeIncluded, endTime,
				endTimeIncluded, maxData);
		List<TotalValue> totalData = new ArrayList<TotalValue>();
		for (Value value : data)
			totalData.add((TotalValue) value);
		return totalData;
	}

	@Override
	String getFileType() {
		return FILE_TYPE;
	}

	@Override
	String getFileDir() {
		return FILE_DIR;
	}

	@Override
	String getFilePrefix() {
		return FILE_PREFIX;
	}

	@Override
	TotalValue newAggregateValue(Date timestamp, Double value,
			boolean notvalid, int flags) {
		return new TotalValue(timestamp, value, notvalid, flags);
	}

	static List<TotalDataStorage> listStorages() {
		List<TotalDataStorage> list = new ArrayList<TotalDataStorage>();
		for (UUID analyzer : listAnalyzers()) {
			File dir = new File(DATA_DIR + File.separator + DIR_ANALYZERS
					+ File.separator + analyzer + File.separator + FILE_DIR);
			if (!dir.isDirectory())
				continue;
			File[] paramdirs = dir.listFiles();
			if (paramdirs == null)
				continue;
			for (int i = 0; i < paramdirs.length; i++) {
				if (!paramdirs[i].isDirectory())
					continue;
				File[] aggdirs = paramdirs[i]
						.listFiles(new IntegerFileFilter());
				if (aggdirs == null)
					continue;
				for (int j = 0; j < aggdirs.length; j++) {
					Integer aggTime;
					try {
						aggTime = Integer.parseInt(aggdirs[j].getName());
					} catch (NumberFormatException nfe) {
						continue;
					}
					if (!aggdirs[j].isDirectory())
						continue;
					list.add(new TotalDataStorage(null, null, analyzer, null,
							paramdirs[i].getName(), 0, aggTime));
				}
			}
		}
		return list;
	}

}
