/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: data acquisition logic from IO boards (extracted from
// AcqManager to use also for Adam boards)
// Change log:
//   2016-10-26: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------
package it.csi.periferico.boards;

import java.util.Date;

/**
 * Data acquisition logic from IO boards (extracted from AcqManager to use also
 * for Adam boards)
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class AcqLogic {

	private AcqStats acqStats;

	protected AcqLogic(AcqStats acqStats) {
		this.acqStats = acqStats;
	}

	public AcqStats getAcqStats() {
		return acqStats;
	}

	protected abstract boolean manageDispatchable(Dispatchable dispatchable);

	// If an active DIUser is deactivated and then activated again, the initial
	// value should be sent as soon as it is activated again: in the following
	// implementation the initial value is sent only at configuration startup,
	// but this is not a problem because currently, when a DIUser is
	// deactivated, the whole configuration is stopped and then restarted.
	protected void acqDI(ExternalAcqItf acquisitionItf, Board board) {
		for (DISubdevice diSub : board.getListDISubdevice()) {
			for (DigitalInput di : diSub.getListDI()) {
				if (!di.isBinded())
					continue;
				DIUser diUser = (DIUser) di.getBindedIOUser();
				if (!diUser.isActive())
					continue;
				DIEvent diEvent = acquisitionItf.readDI(board, diSub, di);
				if (di.getLastValue() != null
						&& di.getLastValue() == diEvent.getValue()
						&& di.isAcqError() == diEvent.getError())
					continue;
				di.setLastValue(diEvent.getValue());
				di.setAcqError(diEvent.getError());
				diEvent.setDestination(diUser);
				if (!manageDispatchable(diEvent)) {
					acqStats.dispatchableLost();
					// This forces the dispatch of a new value as soon as
					// possible
					di.setLastValue(null);
				}
			}
		}
		for (DIOSubdevice dioSub : board.getListDIOSubdevice()) {
			for (DigitalIO dio : dioSub.getListDIO()) {
				if (!dio.isBinded())
					continue;
				IOUser ioUser = dio.getBindedIOUser();
				if (!(ioUser instanceof DIUser))
					continue;
				DIUser diUser = (DIUser) ioUser;
				if (!diUser.isActive())
					continue;
				DIEvent diEvent = acquisitionItf.readDIO(board, dioSub, dio);
				if (dio.getLastValue() != null
						&& dio.getLastValue() == diEvent.getValue()
						&& dio.isAcqError() == diEvent.getError())
					continue;
				dio.setLastValue(diEvent.getValue());
				dio.setAcqError(diEvent.getError());
				diEvent.setDestination(diUser);
				if (!manageDispatchable(diEvent)) {
					acqStats.dispatchableLost();
					// This forces the dispatch of a new value as soon as
					// possible
					dio.setLastValue(null);
				}
			}
		}
	}

	protected void acqAI(ExternalAcqItf acquisitionItf, Board board,
			long theoricAcqTimestamp_ms, long startTime_ms) {
		long lastAcqTimestamp_ms = -1;
		long theoricAcqTimestamp_s = theoricAcqTimestamp_ms / 1000;
		for (AISubdevice aiSub : board.getListAISubdevice()) {
			for (AnalogInput ai : aiSub.getListAI()) {
				if (!ai.isBinded())
					continue;
				if (ai.isDifferentialModeActive()
						&& ai.getDifferentialModeChannelHigh() != null)
					continue;
				AIUser aiUser = (AIUser) ai.getBindedIOUser();
				if (!aiUser.isActive())
					continue;
				boolean acqNeeded = theoricAcqTimestamp_s
						% aiUser.getAcqPeriod() == 0;
				if (acqNeeded || aiUser.isCalibActive()) {
					AIValue aiValue = acquisitionItf.readAI(board, aiSub, ai);
					aiValue.setExtra(!acqNeeded);
					Date acqTimestamp = aiValue.getTimestamp();
					lastAcqTimestamp_ms = acqTimestamp.getTime();
					acqTimestamp.setTime(theoricAcqTimestamp_ms);
					ai.setAcqError(aiValue.getError());
					aiValue.setDestination(aiUser);
					if (!manageDispatchable(aiValue))
						acqStats.dispatchableLost();
				}
			}
		}
		if (lastAcqTimestamp_ms >= 0)
			acqStats.putAITime(theoricAcqTimestamp_ms, startTime_ms,
					lastAcqTimestamp_ms);
	}

	protected void setDO(ExternalAcqItf acquisitionItf, Board board) {
		for (DOSubdevice doSub : board.getListDOSubdevice()) {
			for (DigitalOutput dout : doSub.getListDO()) {
				if (!dout.isBinded())
					continue;
				DOUser doUser = (DOUser) dout.getBindedIOUser();
				if (!doUser.isActive())
					continue;
				boolean value = doUser.getValue();
				boolean success = acquisitionItf.writeDO(board, doSub, dout,
						value);
				dout.setAcqError(!success);
				doUser.setLastWriteStatus(success);
			}
		}
		for (DIOSubdevice dioSub : board.getListDIOSubdevice()) {
			for (DigitalIO dio : dioSub.getListDIO()) {
				if (!dio.isBinded())
					continue;
				IOUser ioUser = dio.getBindedIOUser();
				if (!(ioUser instanceof DOUser))
					continue;
				DOUser doUser = (DOUser) ioUser;
				if (!doUser.isActive())
					continue;
				boolean value = doUser.getValue();
				boolean success = acquisitionItf.writeDIO(board, dioSub, dio,
						value);
				dio.setAcqError(!success);
				doUser.setLastWriteStatus(success);
			}
		}
	}

}
