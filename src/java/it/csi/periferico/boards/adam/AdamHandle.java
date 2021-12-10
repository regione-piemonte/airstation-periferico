/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: board library implementation based on Advantech Adam
// Change log:
//   2016-10-12: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------
package it.csi.periferico.boards.adam;

import it.csi.periferico.acqdrivers.conn.Connection;
import it.csi.periferico.acqdrivers.conn.ConnectionClosedException;
import it.csi.periferico.acqdrivers.conn.ConnectionException;
import it.csi.periferico.boards.AISubdevice;
import it.csi.periferico.boards.AIUser;
import it.csi.periferico.boards.AIValue;
import it.csi.periferico.boards.AcqStats;
import it.csi.periferico.boards.AcqStatsProvider;
import it.csi.periferico.boards.AnalogInput;
import it.csi.periferico.boards.Board;
import it.csi.periferico.boards.BoardsException;
import it.csi.periferico.boards.DIEvent;
import it.csi.periferico.boards.DISubdevice;
import it.csi.periferico.boards.DIUser;
import it.csi.periferico.boards.DOSubdevice;
import it.csi.periferico.boards.DOUser;
import it.csi.periferico.boards.DigitalInput;
import it.csi.periferico.boards.DigitalOutput;
import it.csi.periferico.boards.Dispatchable;
import it.csi.periferico.boards.IOUser;
import it.csi.periferico.boards.Range;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Board library implementation based on Advantech Adam
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
abstract class AdamHandle implements Runnable, AcqStatsProvider {

	private static Logger logger = Logger.getLogger("periferico.adam."
			+ AdamHandle.class.getSimpleName());
	static final String CMD_SEPARATOR = "\r";
	static final String REPLY_DELIMITERS = "\r";
	private static final int THREAD_PRIORITY = Thread.NORM_PRIORITY + 2;
	private static final int DI_POLL_PERIOD_MS = 50;
	private static final int AI_ACQ_PERIOD_MS = 1000;
	private static final int DO_SET_PERIOD_MS = 1000;
	private static final int AI_ACQ_RESYNC_THRESHOLD_ms = 3000;
	private static final int DO_SET_RESYNC_THRESHOLD_ms = 3000;

	private volatile Connection connection;
	private volatile Thread acquisitionThread = null;
	private volatile Board board;
	private volatile AcqStats acqStats;

	AdamHandle(Connection connection) {
		this.connection = connection;
		acqStats = new AcqStats(DI_POLL_PERIOD_MS, AI_ACQ_PERIOD_MS);
	}

	void startAcquisition(Board board) {
		logger.info("Starting acquisition from " + this + "...");
		this.board = board;
		acquisitionThread = new Thread(this, this.toString());
		acquisitionThread.setDaemon(true);
		acquisitionThread.setPriority(THREAD_PRIORITY);
		acquisitionThread.start();
	}

	void stopAcquisition() {
		Thread tmpThread = acquisitionThread;
		acquisitionThread = null;
		logger.info("Stopping acquisition from " + this + "...");
		if (tmpThread != null) {
			try {
				int maxWaitTime_s = 2;
				logger.info("Waiting up to " + maxWaitTime_s
						+ "s for acquisition from " + this + " to terminate...");
				// NOTE: join is needed before boardManager.destroy() is called
				tmpThread.join(maxWaitTime_s * 1000);
				logger.info("Finished waiting for acquisition from " + this
						+ " to terminate");
			} catch (InterruptedException ie) {
				logger.error("Wait for acquisition from " + this
						+ " to terminate" + " interrupted");
			}
		}
	}

	final void init(Map<String, AdamModuleInfo> mapBoardInfo)
			throws IOException, InterruptedException, BoardsException,
			ConnectionException {
		connection.open();
		initImpl(mapBoardInfo);
	}

	void dispose() {
		if (connection.getStatus() != Connection.Status.CLOSED)
			connection.close();
	}

	@Override
	public AcqStats getAcqStats() {
		return acqStats;
	}

	abstract void initImpl(Map<String, AdamModuleInfo> mapBoardInfo)
			throws IOException, ConnectionClosedException,
			InterruptedException, BoardsException;

	abstract Collection<Integer> getModuleAddresses();

	abstract AcquisitionModule getAcquisitionModule(Integer address);

	private boolean manageDispatchable(Dispatchable dispatchable) {
		if (dispatchable instanceof AIValue) {
			AIValue aiValue = (AIValue) dispatchable;
			AIUser aiUser = (AIUser) aiValue.getDestination();
			aiUser.deliver(aiValue);
		} else if (dispatchable instanceof DIEvent) {
			DIEvent diEvent = (DIEvent) dispatchable;
			DIUser diUser = (DIUser) diEvent.getDestination();
			diUser.deliver(diEvent);
		}
		return true;
	}

	private long computeNextAIAcqTime() {
		return (System.currentTimeMillis() / AI_ACQ_PERIOD_MS)
				* AI_ACQ_PERIOD_MS + AI_ACQ_PERIOD_MS;
	}

	private long computeNextDOSetTime() {
		return (System.currentTimeMillis() / DO_SET_PERIOD_MS)
				* DO_SET_PERIOD_MS + DO_SET_PERIOD_MS + DI_POLL_PERIOD_MS * 5;
	}

	@Override
	public void run() {
		logger.info("Acquisition from " + this + " started");
		long nextTheoricAIAcqTime_ms = computeNextAIAcqTime();
		long nextTheoricDOSetTime_ms = computeNextDOSetTime();
		while (Thread.currentThread() == acquisitionThread) {
			long diStartTime = System.currentTimeMillis();
			int diCount = 0;
			diCount = acqDI(diStartTime, diCount);
			long timeDiff = computeNextAIAcqTime() - nextTheoricAIAcqTime_ms;
			if (Math.abs(timeDiff) >= AI_ACQ_RESYNC_THRESHOLD_ms) {
				logger.warn("Time discontinuity detected (" + (timeDiff / 1000)
						+ "s) for " + this + " AI acquisition. Resync done.");
				acqStats.reset();
				nextTheoricAIAcqTime_ms = computeNextAIAcqTime();
			}
			if (System.currentTimeMillis() >= nextTheoricAIAcqTime_ms) {
				long aiStartTime = System.currentTimeMillis();
				long lastAcqTimestamp_ms = -1;
				for (AISubdevice aiSub : board.getListAISubdevice()) {
					Long acqTimestamp_ms = acqAISubdevice(aiSub,
							nextTheoricAIAcqTime_ms);
					if (acqTimestamp_ms != null)
						lastAcqTimestamp_ms = acqTimestamp_ms;
					diCount = acqDI(diStartTime, diCount);
				}
				if (lastAcqTimestamp_ms >= 0)
					acqStats.putAITime(nextTheoricAIAcqTime_ms, aiStartTime,
							lastAcqTimestamp_ms);
				nextTheoricAIAcqTime_ms += AI_ACQ_PERIOD_MS;
			}
			timeDiff = computeNextDOSetTime() - nextTheoricDOSetTime_ms;
			if (Math.abs(timeDiff) >= DO_SET_RESYNC_THRESHOLD_ms) {
				logger.warn("Time discontinuity detected (" + (timeDiff / 1000)
						+ "s) for " + this + " DO set. Resync done.");
				acqStats.reset();
				nextTheoricDOSetTime_ms = computeNextDOSetTime();
			}
			if (System.currentTimeMillis() >= nextTheoricDOSetTime_ms) {
				for (DOSubdevice doSub : board.getListDOSubdevice()) {
					setDOSubdevice(doSub);
					diCount = acqDI(diStartTime, diCount);
				}
				nextTheoricDOSetTime_ms += DO_SET_PERIOD_MS;
			}
			long sleepTime = (diStartTime + diCount * DI_POLL_PERIOD_MS)
					- System.currentTimeMillis();
			try {
				if (sleepTime > 0 && sleepTime < DI_POLL_PERIOD_MS)
					Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.warn("Acquisition from " + this + " interrupted");
				break;
			}
		}
		board = null;
		logger.info("Acquisition from " + this + " stopped");
	}

	private Long acqAISubdevice(AISubdevice aiSub, long theoricAcqTimestamp_ms) {
		String aiValuesBlock = null;
		Exception blockException = null;
		try {
			setAIRanges(aiSub);
			aiValuesBlock = sendAndParse(aiSub.getSubdevice(), "#", "", ">",
					false);
		} catch (Exception ex) {
			blockException = ex;
		}
		long acqTimestamp_ms = System.currentTimeMillis();
		int aiAcqCount = 0;
		for (AnalogInput ai : aiSub.getListAI()) {
			if (!ai.isBinded())
				continue;
			AIUser aiUser = (AIUser) ai.getBindedIOUser();
			if (!aiUser.isActive())
				continue;
			boolean acqNeeded = (theoricAcqTimestamp_ms / 1000)
					% aiUser.getAcqPeriod() == 0;
			if (acqNeeded || aiUser.isCalibActive()) {
				AIValue aiValue;
				try {
					if (blockException != null)
						throw blockException;
					Double value = parseValue(aiSub.getSubdevice(),
							ai.getChannel(), (AdamRange) ai.getActiveRange(),
							aiValuesBlock);
					aiValue = new AIValue(new Date(theoricAcqTimestamp_ms),
							value == null ? Double.NaN : value, false,
							value == null, aiUser);
				} catch (Exception ex) {
					if (!ai.isAcqError())
						logger.error(
								"Error reading analog input: " + board.getId()
										+ "/" + aiSub.getSubdevice() + "/"
										+ ai.getChannel(), ex);
					aiValue = new AIValue(new Date(theoricAcqTimestamp_ms),
							Double.NaN, true, false, aiUser);
				}
				aiValue.setExtra(!acqNeeded);
				ai.setAcqError(aiValue.getError());
				aiAcqCount++;
				if (!manageDispatchable(aiValue))
					acqStats.dispatchableLost();
			}
		}
		return aiAcqCount > 0 ? acqTimestamp_ms : null;
	}

	private Double parseValue(int subdev, int channel, AdamRange range,
			String aiValuesBlock) throws BoardsException {
		int index = channel * 7;
		try {
			String aiValue = aiValuesBlock.substring(index, index + 7);
			return parseValue(aiValue, range);
		} catch (Exception e) {
			throw new BoardsException("Unparsable double value at position "
					+ index + " in reply '" + aiValuesBlock
					+ "' reading subdev/channel " + subdev + "/" + channel, e);
		}
	}

	private Double parseValue(String strValue, AdamRange range)
			throws NumberFormatException {
		if ("+999999".equals(strValue) || "-999999".equals(strValue))
			return null;
		double value = Double.parseDouble(strValue);
		if ("mV".equals(range.getUnit()) || "mA".equals(range.getUnit()))
			value = value / 1000;
		return value;
	}

	private void setAIRanges(AISubdevice aiSub) throws BoardsException,
			ConnectionClosedException, IOException, InterruptedException {
		for (AnalogInput ai : aiSub.getListAI()) {
			if (!ai.isBinded() || !ai.getBindedIOUser().isActive())
				continue;
			Range r = ai.getActiveRange();
			if (!(r instanceof AdamRange))
				throw new IllegalStateException("Expected range of class "
						+ "AdamRange for analog input: " + board.getId() + "/"
						+ aiSub.getSubdevice() + "/" + ai.getChannel());
			if (!ai.isRangeSetToBoard()) {
				setAIRange(aiSub.getSubdevice(), ai.getChannel(), (AdamRange) r);
				ai.onRangeSetToBoard();
			}
		}
	}

	private int acqDI(long theoricTime, int diCount) {
		long acqTime = theoricTime + diCount * DI_POLL_PERIOD_MS;
		if (System.currentTimeMillis() < acqTime - DI_POLL_PERIOD_MS / 5)
			return diCount;
		for (DISubdevice diSub : board.getListDISubdevice())
			acqDISubdevice(diSub);
		long acqCompletionTime = System.currentTimeMillis() - acqTime;
		// Avoid negative values, if clock adjustment happens!
		if (acqCompletionTime >= 0)
			acqStats.putDITime(acqCompletionTime);
		return diCount++;
	}

	private void acqDISubdevice(DISubdevice diSub) {
		String diValuesBlock = null;
		Exception blockException = null;
		try {
			diValuesBlock = sendAndParse(diSub.getSubdevice(), "$", "6", "!",
					false);
		} catch (Exception ex) {
			blockException = ex;
		}
		Date acqTimestamp = new Date();
		for (DigitalInput di : diSub.getListDI()) {
			if (!di.isBinded() || !di.getBindedIOUser().isActive())
				continue;
			IOUser ioUser = di.getBindedIOUser();
			DIEvent diEvent;
			try {
				if (blockException != null)
					throw blockException;
				int bits = Integer.parseInt(diValuesBlock.substring(0, 4), 16);
				boolean value = (bits & (1 << di.getChannel())) != 0;
				diEvent = new DIEvent(acqTimestamp, value, false, ioUser);
			} catch (Exception ex) {
				if (!di.isAcqError())
					logger.error(
							"Error reading digital input: " + board.getId()
									+ "/" + diSub.getSubdevice() + "/"
									+ di.getChannel(), ex);
				diEvent = new DIEvent(acqTimestamp, false, true, ioUser);
			}
			if (di.getLastValue() != null
					&& di.getLastValue() == diEvent.getValue()
					&& di.isAcqError() == diEvent.getError())
				continue;
			di.setLastValue(diEvent.getValue());
			di.setAcqError(diEvent.getError());
			if (!manageDispatchable(diEvent)) {
				acqStats.dispatchableLost();
				// This forces the dispatch of a new value as soon as
				// possible
				di.setLastValue(null);
			}
		}
	}

	private void setDOSubdevice(DOSubdevice doSub) {
		for (DigitalOutput dout : doSub.getListDO()) {
			if (!dout.isBinded())
				continue;
			DOUser doUser = (DOUser) dout.getBindedIOUser();
			if (!doUser.isActive())
				continue;
			boolean success = true;
			try {
				writeDO(doSub.getSubdevice(), dout.getChannel(),
						doUser.getValue());
			} catch (Exception ex) {
				if (!dout.isAcqError())
					logger.error(
							"Error writing digital output: " + board.getId()
									+ "/" + doSub.getSubdevice() + "/"
									+ dout.getChannel(), ex);
				success = false;
			}
			dout.setAcqError(!success);
			doUser.setLastWriteStatus(success);
		}
	}

	protected String sendAndRead(int address, String prefix, String command,
			long timeout) throws IOException, ConnectionClosedException,
			InterruptedException {
		String hexAddr = String.format("%02X", address);
		connection.send(prefix + hexAddr + command, CMD_SEPARATOR);
		return connection.receive(timeout, REPLY_DELIMITERS);
	}

	protected void clearBuffers() {
		connection.clearReadBuffers();
	}

	protected String sendAndParse(int address, String prefix, String command,
			String okReplyPrefix, boolean checkAddress) throws IOException,
			ConnectionClosedException, InterruptedException, BoardsException {
		long timeout = 200;
		int maxAttempts = 3;
		String hexAddr = String.format("%02X", address);
		String lastError = null;
		for (int i = 1; i <= maxAttempts; ++i) {
			if (i > 1) {
				Thread.sleep(timeout / 2);
				connection.clearReadBuffers();
			}
			String cmdStr = prefix + hexAddr + command;
			connection.send(cmdStr, CMD_SEPARATOR);
			String reply = connection.receive(timeout, REPLY_DELIMITERS);
			if (reply == null) {
				lastError = "Timeout reading reply to command '" + cmdStr
						+ "' at attempt " + i;
			} else if (reply.startsWith("?")) {
				lastError = "Invalid command reply '" + reply
						+ "' to command '" + cmdStr + "' at attempt " + i;
			} else if (reply.startsWith(okReplyPrefix)) {
				String value = reply.substring(1);
				if (checkAddress) {
					if (value.length() >= 2) {
						String replyAddr = value.substring(0, 2);
						if (!replyAddr.equalsIgnoreCase(hexAddr)) {
							lastError = "Address mismatch in reply '" + reply
									+ "' to command '" + cmdStr
									+ "' at attempt " + i;
							continue;
						}
						value = value.substring(2);
					} else {
						lastError = "Missing address in reply '" + reply
								+ "' to command '" + cmdStr + "' at attempt "
								+ i;
						continue;
					}
				}
				return value;
			} else {
				lastError = "Unexpected reply '" + reply + "' to command '"
						+ cmdStr + "' at attempt " + i;
			}
		}
		throw new BoardsException(lastError);
	}

	Double readAI(int subdevAddr, int chanAddr, AdamRange range)
			throws ConnectionClosedException, BoardsException, IOException,
			InterruptedException {
		String result = sendAndParse(subdevAddr, "#", "" + chanAddr, ">", false);
		try {
			return parseValue(result, range);
		} catch (NumberFormatException e) {
			throw new BoardsException("Unparsable double value in reply '"
					+ result + "' reading subdev/channel " + subdevAddr + "/"
					+ chanAddr);
		}
	}

	boolean readDI(int subdevAddr, int chanAddr)
			throws ConnectionClosedException, BoardsException, IOException,
			InterruptedException {
		String result = sendAndParse(subdevAddr, "$", "6", "!", false);
		int bits = Integer.parseInt(result.substring(0, 4), 16);
		return (bits & (1 << chanAddr)) != 0;
	}

	void writeDO(int subdevAddr, int chanAddr, boolean value)
			throws ConnectionClosedException, BoardsException, IOException,
			InterruptedException {
		String cmd = "1" + String.format("%1X", chanAddr)
				+ (value ? "01" : "00");
		String result = sendAndParse(subdevAddr, "#", cmd, ">", false);
		if (!result.isEmpty())
			throw new BoardsException("Found garbage in reply '" + result
					+ "' to command '" + cmd);
	}

	int readAIRangeCode(int subdevAddr, int chanAddr) throws BoardsException,
			ConnectionClosedException, IOException, InterruptedException {
		String strChan = "C" + String.format("%1X", chanAddr);
		String cmd = "8" + strChan;
		String result = sendAndParse(subdevAddr, "$", cmd, "!", true);
		if (!result.startsWith(strChan + "R"))
			throw new BoardsException("Channel mismatch in reply '" + result
					+ "' to command '" + cmd);
		String rangeCode = result.substring(3);
		try {
			int code = Integer.parseInt(rangeCode, 16);
			logger.trace("AI (subdevice/channel/range_code): " + subdevAddr
					+ "/" + chanAddr + "/" + rangeCode);
			return code;
		} catch (NumberFormatException e) {
			throw new BoardsException("Unparsable range in reply '" + result
					+ "' to command '" + cmd);
		}
	}

	private void setAIRange(int subdevAddr, int chanAddr, AdamRange range)
			throws BoardsException, ConnectionClosedException, IOException,
			InterruptedException {
		String code = String.format("%02X", range.getId());
		String cmd = "7C" + String.format("%1X", chanAddr) + "R" + code;
		String result = sendAndParse(subdevAddr, "$", cmd, "!", true);
		if (!result.isEmpty())
			throw new BoardsException("Found garbage in reply '" + result
					+ "' to command '" + cmd);
		logger.trace("AI (subdevice/channel/range/range_code): " + subdevAddr
				+ "/" + chanAddr + "/" + range + "/" + code);
	}

}
