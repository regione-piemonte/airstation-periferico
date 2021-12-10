/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class for the drivers of the analyzers with data port
// Change log:
//   2008-09-29: initial version
// ----------------------------------------------------------------------------
// $Id: Driver.java,v 1.14 2015/11/17 15:35:47 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.impl;

import it.csi.periferico.acqdrivers.itf.AlarmInterface;
import it.csi.periferico.acqdrivers.itf.AnalyzerFault;
import it.csi.periferico.acqdrivers.itf.AnalyzerInterface;
import it.csi.periferico.acqdrivers.itf.AnalyzerParameter;
import it.csi.periferico.acqdrivers.itf.ConnectionClosedException;
import it.csi.periferico.acqdrivers.itf.ConnectionException;
import it.csi.periferico.acqdrivers.itf.DriverCallback;
import it.csi.periferico.acqdrivers.itf.DriverCfg;
import it.csi.periferico.acqdrivers.itf.DriverInterface;
import it.csi.periferico.acqdrivers.itf.ElementCfg;
import it.csi.periferico.acqdrivers.itf.ElementInterface;
import it.csi.periferico.acqdrivers.itf.ElementValue;
import it.csi.periferico.acqdrivers.itf.FaultValue;
import it.csi.periferico.acqdrivers.itf.InvalidParamException;
import it.csi.periferico.acqdrivers.itf.NoReplyException;
import it.csi.periferico.acqdrivers.itf.ProtocolException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Base class for the drivers of the analyzers with data port. La classe Driver
 * implementa l’interfaccia DriverInterface e include un thread di acquisizione
 * dati con le funzioni necessarie per l’avvio e l’arresto dell’acquisizione. La
 * connessione verso l’analizzatore deve essere aperta e chiusa da chi
 * implementa il driver, estendendo la classe Driver , eventualmente sfruttando
 * le funzionalità offerte dalla libreria di comunicazione.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class Driver implements DriverInterface, Runnable {

	protected static final String MSG_CONN_DROPPED = "Connection to the analyzer dropped";
	protected static final String MSG_CONN_CLOSED = "Connection closed on the analyzer side";
	private static final int ACQ_THREAD_PRIORITY = Thread.NORM_PRIORITY + 1;
	private static final int CB_THREAD_PRIORITY = Thread.NORM_PRIORITY;
	private static final int ACQ_THREAD_PERIOD_ms = 1000; // milliseconds
	private static final int ACQ_THREAD_RESYNC_THRESHOLD_ms = 3000; // milliseconds
	private static final int ACQ_THREADS_TIME_SHIFT_ms = 19; // milliseconds
	private static final int MIN_TIME_FOR_CMD_EXEC_ms = 250; // milliseconds
	private static final int MAX_CMD_WAIT_TIME_ms = 50; // milliseconds
	private static final int MAX_CB_WAIT_TIME_ms = 100; // milliseconds
	private static final int MAX_CMD_QUEUE_SIZE = 100;
	private static final int MAX_CB_QUEUE_SIZE = 100;
	private static int threadCount = 0;

	private enum Cmd {
		TEST_COMMUNICATION, GET_SERIAL_NUMBER, GET_DATE, SET_DATE, //
		GET_PARAMETERS, SHOW_MEASURED_PARAMETERS, SHOW_PROCESS_PARAMETERS, //
		READ_PARAMETER, GET_FAULTS, SHOW_FAULTS, IS_FAULT_ACTIVE, RESET_FAULT, //
		READ_FAULT_STATUS, RESET_ALL_FAULTS, GET_KEYLIST, SEND_KEY, //
		READ_DISPLAY, READ_DISPLAY_IMAGE, GET_COMMANDLIST, SEND_COMMAND
	};

	private Logger logger;
	private int threadIndex;
	private volatile Thread acquisitionThread = null;
	private volatile CallBackRunner callbackRunner = new CallBackRunner();
	private volatile boolean acquisitionEnabled = false;
	private AnalyzerInterface analyzerItf = null;
	private AlarmInterface alarmItf = null;
	private List<ElementInfoHolder> elementInfoHolders = null;
	private DriverCfg config;
	private int maxConsecutiveConnectErrors;
	private long autoReconnectInterval;
	private long lastConnectionAttemptTime_ms;
	private int connectErrors;
	private boolean protocolError;
	private boolean lastAcquisitionSucceded;
	private boolean stoppedOnFatalError = false;
	private FaultValue lastFaultValue = null;
	private BlockingQueue<Command<?, ?>> commandQueue;
	private BlockingQueue<Command<?, ?>> callbackQueue;
	private int commandQueueOverruns = 0;
	private int callbackQueueOverruns = 0;
	private Integer lastFaultRead = null;
	private long lastFaultReadTime = 0;

	// NOTE: this class should be instantiated only by DriverMananger
	protected Driver(AnalyzerInterface analyzerInterface, DriverCfg config,
			int maxConsecutiveConnectErrors, long autoReconnectInterval) {
		threadIndex = ++threadCount;
		logger = Logger.getLogger(getLoggerBaseName() + "."
				+ getClass().getSimpleName() + "-" + threadIndex);
		this.config = config;
		this.analyzerItf = analyzerInterface;
		this.maxConsecutiveConnectErrors = maxConsecutiveConnectErrors;
		this.autoReconnectInterval = autoReconnectInterval;
		commandQueue = new ArrayBlockingQueue<Command<?, ?>>(MAX_CMD_QUEUE_SIZE);
		callbackQueue = new ArrayBlockingQueue<Command<?, ?>>(MAX_CB_QUEUE_SIZE);
	}

	protected final Logger getLogger() {
		return logger;
	}

	public static void resetThreadCount() {
		threadCount = 0;
	}

	private void initElementInfoHolders() {
		elementInfoHolders = new ArrayList<ElementInfoHolder>();
		try {
			for (ElementInterface ei : analyzerItf.getElementInterfaces()) {
				String paramId = ei.getParameterId();
				ElementCfg elementCfg = config.getElementCfg(paramId);
				elementInfoHolders.add(new ElementInfoHolder(elementCfg, ei));
			}
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Cannot initialize element configuration", ex);
		}
	}

	protected Map<String, String> getDriverParams() {
		Map<String, String> map = new HashMap<String, String>();
		String params = analyzerItf.getDriverParams();
		if (params == null)
			return map;
		params = params.trim();
		String[] fields = params.split(",");
		for (String field : fields) {
			field = field.trim();
			String[] kv = field.split("=", 2);
			map.put(kv[0].trim(), kv.length == 2 ? kv[1].trim() : "");
		}
		return map;
	}

	@Override
	public final void start() {
		if (!analyzerItf.isActive())
			return;
		commandQueue.clear();
		callbackQueue.clear();
		acquisitionThread = new Thread(this, "acq_driver_" + threadIndex);
		acquisitionThread.setDaemon(true);
		acquisitionThread.setPriority(ACQ_THREAD_PRIORITY);
		acquisitionThread.start();
	}

	@Override
	public final void stop() {
		Thread tmpThread = acquisitionThread;
		acquisitionThread = null;
		if (tmpThread != null) {
			try {
				int maxWaitTime_s = 8;
				logger.info("Waiting up to " + maxWaitTime_s
						+ "s for acquisition driver thread "
						+ tmpThread.getName() + " to terminate...");
				tmpThread.join(maxWaitTime_s * 1000);
				logger.info("Finished waiting for acquisition driver thread "
						+ tmpThread.getName() + " to terminate");
			} catch (InterruptedException ie) {
				logger.error("Wait for acquisition driver thread "
						+ tmpThread.getName() + " to terminate interrupted");
			}
		}
	}

	@Override
	public final boolean isRunning() {
		return acquisitionThread != null;
	}

	@Override
	public final boolean isFailed() {
		return stoppedOnFatalError;
	}

	@Override
	public final boolean isConnectionOK() {
		return connectErrors == 0;
	}

	@Override
	public final void enableAcquisition() {
		if (!isRunning())
			throw new IllegalStateException("Cannot enable acquisition when "
					+ "the driver is not running");
		initElementInfoHolders();
		alarmItf = analyzerItf.getAlarmInterface();
		if (alarmItf == null)
			throw new IllegalStateException("Alarm interface not found");
		lastFaultValue = null;
		acquisitionEnabled = true;
	}

	@Override
	public final void disableAcquisition() {
		acquisitionEnabled = false;
	}

	@Override
	public final boolean isAcquisitionEnabled() {
		return acquisitionEnabled;
	}

	public final int getCommandQueueOverruns() {
		return commandQueueOverruns;
	}

	public final int getCallbackQueueOverruns() {
		return callbackQueueOverruns;
	}

	private long computeNextRunTime() {
		long theoricTime = (System.currentTimeMillis() / ACQ_THREAD_PERIOD_ms)
				* ACQ_THREAD_PERIOD_ms + ACQ_THREAD_PERIOD_ms;
		long threadTimeShift = (ACQ_THREADS_TIME_SHIFT_ms * threadIndex)
				% (ACQ_THREAD_PERIOD_ms / 4);
		return theoricTime + threadTimeShift;
	}

	private long now() {
		return System.currentTimeMillis();
	}

	@Override
	public final void run() {
		logger.info("Acquisition driver thread " + threadIndex
				+ " started for analyzer " + getAnalyzerName()
				+ " using connection " + getConnectionId());
		callbackRunner.start();
		onDriverStart();
		long nextRunTime = computeNextRunTime();
		stoppedOnFatalError = false;
		try {
			while (Thread.currentThread() == acquisitionThread) {
				try {
					while (nextRunTime - now() > MIN_TIME_FOR_CMD_EXEC_ms) {
						Command<?, ?> command = commandQueue.poll(
								MAX_CMD_WAIT_TIME_ms, TimeUnit.MILLISECONDS);
						if (command != null) {
							preReadData();
							execute(command);
							postReadData();
						}
						// This may happen only in case of clock adjustment
						if (nextRunTime - now() > ACQ_THREAD_PERIOD_ms)
							break;
					}
					long timeToAcq;
					while ((timeToAcq = nextRunTime - now()) > 0) {
						Thread.sleep(Math.min(MAX_CMD_WAIT_TIME_ms, timeToAcq));
						// This may happen only in case of clock adjustment
						if (nextRunTime - now() > ACQ_THREAD_PERIOD_ms)
							break;
					}
				} catch (InterruptedException e) {
					logger.warn("Interruption requested for acquisition driver"
							+ " thread " + threadIndex);
					break;
				}
				long timeDiff = now() - nextRunTime;
				if (Math.abs(timeDiff) >= ACQ_THREAD_RESYNC_THRESHOLD_ms) {
					logger.warn("Time discontinuity detected ("
							+ (timeDiff / 1000) + "s) for acquisition driver "
							+ "thread " + threadIndex + ". Resync done.");
					nextRunTime = computeNextRunTime();
					continue;
				}
				if (timeDiff >= 0) {
					Date theoricAcqTime = new Date(nextRunTime);
					nextRunTime += ACQ_THREAD_PERIOD_ms;
					if (acquisitionEnabled) {
						preReadData();
						readData(theoricAcqTime);
						postReadData();
					}
				}
			}
		} catch (Exception ex) {
			stoppedOnFatalError = true;
			logger.error("Fatal error occurred in acquisition driver thread "
					+ threadIndex + ": acquisition aborted", ex);
		}
		logger.info("Acquisition driver thread " + threadIndex
				+ " shutdown in progress...");
		onAcquisitionStop();
		callbackRunner.stop();
		logger.info("Acquisition driver thread " + threadIndex + " stopped");
	}

	private void onDriverStart() {
		protocolError = false;
		connectErrors = 0;
		lastAcquisitionSucceded = false;
		logger.info("Opening connection to data port for analyzer "
				+ getAnalyzerName() + ", thread " + threadIndex);
		try {
			lastConnectionAttemptTime_ms = System.currentTimeMillis();
			connect();
			logger.debug("Doing first protocol initialization");
			initProtocolImpl(false);
		} catch (ConnectionException ex) {
			connectErrors++;
			disconnect();
			logger.error("Opening connection to data port failed for analyzer "
					+ getAnalyzerName() + ", thread " + threadIndex, ex);
		} catch (ProtocolException e) {
			protocolError = true;
			logger.debug("First protocol init failed for " + getAnalyzerName(),
					e);
		} catch (Exception ex) {
			connectErrors++;
			disconnect();
			logger.error("Initializing connection to data port failed for "
					+ "analyzer " + getAnalyzerName() + ", thread "
					+ threadIndex, ex);
		}
	}

	private void onAcquisitionStop() {
		disconnect();
		logger.info("Connection to data port closed for analyzer "
				+ getAnalyzerName() + ", thread " + threadIndex + ", uptime "
				+ getConnectionUptime() / 1000 + "s");
	}

	private void preReadData() {
		if (connectErrors > 0 || protocolError) {
			long timeFromLastConnectionAttempt = System.currentTimeMillis()
					- lastConnectionAttemptTime_ms;
			if (connectErrors >= maxConsecutiveConnectErrors
					&& timeFromLastConnectionAttempt < autoReconnectInterval)
				return;
			try {
				logger.info("Reopening connection to data port for analyzer "
						+ getAnalyzerName() + ", thread " + threadIndex);
				disconnect();
				lastConnectionAttemptTime_ms = System.currentTimeMillis();
				connect();
				logger.debug("Doing protocol initialization: quickMode="
						+ lastAcquisitionSucceded + ", connectErrors="
						+ connectErrors + ", protocolError=" + protocolError);
				initProtocolImpl(lastAcquisitionSucceded);
				protocolError = false;
				connectErrors = 0;
			} catch (ConnectionException ex) {
				connectErrors++;
				disconnect();
				logger.error("Reopening connection to data port failed for"
						+ " analyzer " + getAnalyzerName() + ", thread "
						+ threadIndex, ex);
			} catch (ProtocolException e) {
				protocolError = true;
				connectErrors = 0;
				logger.debug("Protocol init failed for " + getAnalyzerName(), e);
			} catch (Exception ex) {
				connectErrors++;
				disconnect();
				logger.error("Re-initializing connection to data port failed "
						+ "for analyzer " + getAnalyzerName() + ", thread "
						+ threadIndex, ex);
			}
		}
	}

	private void postReadData() {
		if (connectErrors > 0) {
			if (!isConnectionClosed()) {
				logger.error("Connection to data port lost for analyzer "
						+ getAnalyzerName() + ", thread " + threadIndex);
				disconnect();
			}
		}
		if (protocolError) {
			if (!isConnectionClosed()) {
				logger.error("Protocol sync lost for data port analyzer "
						+ getAnalyzerName() + ", thread " + threadIndex
						+ ": closing connection now");
				disconnect();
			}
		}
	}

	private void readData(Date theoricAcqTime) {
		onReadDataBegin(theoricAcqTime);
		readFault(theoricAcqTime);
		for (ElementInfoHolder elementInfo : elementInfoHolders) {
			readElement(elementInfo, theoricAcqTime);
		}
		onReadDataEnd();
	}

	private void onReadDataBegin(Date readTime) {
		if (connectErrors > 0)
			return;
		try {
			readDataInit(readTime);
		} catch (ProtocolException ex) {
			protocolError = true;
			logger.debug("Read data init failed for " + getAnalyzerName(), ex);
		} catch (Exception ex) {
			connectErrors++;
			logger.debug("Read data init failed for " + getAnalyzerName(), ex);
		}
	}

	protected void readDataInit(Date readTime) throws NoReplyException,
			ProtocolException, IOException, ConnectionException,
			InterruptedException {
	}

	private void onReadDataEnd() {
		if (connectErrors > 0)
			return;
		try {
			readDataCleanup();
		} catch (ProtocolException ex) {
			protocolError = true;
			logger.debug("Read data cleanup failed for " + getAnalyzerName(),
					ex);
		} catch (Exception ex) {
			connectErrors++;
			logger.debug("Read data cleanup failed for " + getAnalyzerName(),
					ex);
		}
	}

	protected void readDataCleanup() throws NoReplyException,
			ProtocolException, IOException, ConnectionException,
			InterruptedException {
	}

	private void readFault(Date readTime) {
		if (!alarmItf.isActive())
			return;
		int faultAcqPeriod = Math.max(alarmItf.getAcqPeriod(),
				getMinFaultAcqPeriod());
		if ((readTime.getTime() / 1000) % faultAcqPeriod != 0)
			return;
		Integer faultValue = null;
		if (config.getIgnoreCommFailureInterval_s() != null
				&& (System.currentTimeMillis() - lastFaultReadTime) <= config
						.getIgnoreCommFailureInterval_s() * 1000)
			faultValue = lastFaultRead;
		if (connectErrors > 0) {
			onFaultRead(faultValue, readTime);
			return;
		}
		try {
			lastFaultRead = faultValue = readFaultImpl();
			lastFaultReadTime = System.currentTimeMillis();
			lastAcquisitionSucceded = true;
		} catch (ProtocolException ex) {
			protocolError = true;
			lastAcquisitionSucceded = false;
			logger.debug("Fault read failed for " + getAnalyzerName(), ex);
		} catch (Exception ex) {
			connectErrors++;
			lastAcquisitionSucceded = false;
			logger.debug("Fault read failed for " + getAnalyzerName(), ex);
		} finally {
			onFaultRead(faultValue, readTime);
		}
	}

	private void onFaultRead(Integer value, Date readTime) {
		FaultValue faultValue = null;
		if (value != null) {
			faultValue = new FaultValue(readTime, value,
					config.getFaultCodeForOK(), config.getFaultIgnoreMask(),
					config.getAlternateFaultCodeForOK());
		} else {
			faultValue = new FaultValue(readTime);
		}
		if (faultValue.isStatusUnchanged(lastFaultValue))
			return;
		lastFaultValue = faultValue;
		alarmItf.deliver(faultValue);
	}

	protected FaultValue newFaultValue(Date timestamp, Integer value) {
		return new FaultValue(timestamp, value, config.getFaultCodeForOK(),
				config.getFaultIgnoreMask(),
				config.getAlternateFaultCodeForOK());
	}

	private void readElement(ElementInfoHolder elementInfo, Date readTime) {
		ElementInterface eItf = elementInfo.getElementInterface();
		if (!eItf.isActive())
			return;
		if (!eItf.isReady())
			return;
		int acqPeriod = Math.max(eItf.getAcqPeriod(), getMinDataAcqPeriod());
		if ((readTime.getTime() / 1000) % acqPeriod != 0)
			return;
		ElementCfg eCfg = elementInfo.getElementCfg();
		ElementValue elementValue = new ElementValue(readTime);
		if (connectErrors > 0) {
			onElementRead(elementInfo, elementValue);
			return;
		}
		try {
			elementValue = readElementImpl(eCfg, readTime);
			lastAcquisitionSucceded = true;
		} catch (ProtocolException ex) {
			protocolError = true;
			lastAcquisitionSucceded = false;
			logger.debug(eCfg.getParameterId() + " element read failed for "
					+ getAnalyzerName(), ex);
		} catch (Exception ex) {
			connectErrors++;
			lastAcquisitionSucceded = false;
			logger.debug(eCfg.getParameterId() + " element read failed for "
					+ getAnalyzerName(), ex);
		} finally {
			onElementRead(elementInfo, elementValue);
		}
	}

	private void onElementRead(ElementInfoHolder elementInfo,
			ElementValue elementValue) {
		ElementInterface ei = elementInfo.getElementInterface();
		ei.deliver(elementValue);
	}

	private String getAnalyzerName() {
		return analyzerItf.getBrand() + " - " + analyzerItf.getModel();
	}

	protected abstract String getLoggerBaseName();

	protected abstract void connect() throws ConnectionException;

	protected abstract void disconnect();

	protected abstract boolean isConnected();

	protected abstract boolean isConnectionClosed();

	protected abstract long getConnectionId();

	protected abstract long getConnectionUptime();

	protected abstract void initProtocolImpl(boolean quickMode)
			throws NoReplyException, ProtocolException, IOException,
			ConnectionException, ConnectionClosedException,
			InterruptedException;

	protected abstract Integer readFaultImpl() throws NoReplyException,
			ProtocolException, IOException, ConnectionException,
			InterruptedException;

	protected abstract ElementValue readElementImpl(ElementCfg elementCfg,
			Date expectedReadTime) throws NoReplyException, ProtocolException,
			IOException, ConnectionException, InterruptedException;

	protected abstract int getMinDataAcqPeriod();

	protected abstract int getMinFaultAcqPeriod();

	@Override
	public final void testCommunication(DriverCallback<Boolean> callback) {
		toCmdQueue(new Command<Object, Boolean>(Cmd.TEST_COMMUNICATION, null,
				callback));
	}

	protected abstract Boolean testCommunication() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException;

	@Override
	public final void getDate(DriverCallback<Date> callback) {
		toCmdQueue(new Command<Object, Date>(Cmd.GET_DATE, null, callback));
	}

	protected abstract Date getDate() throws IOException, InterruptedException,
			ConnectionException, ProtocolException, NoReplyException;

	@Override
	public final void setDate(DriverCallback<Date> callback) {
		toCmdQueue(new Command<Object, Date>(Cmd.SET_DATE, null, callback));
	}

	protected abstract Date setDate() throws IOException, InterruptedException,
			ConnectionException, ProtocolException, NoReplyException;

	@Override
	public final void getSerialNumber(DriverCallback<String> callback) {
		toCmdQueue(new Command<Object, String>(Cmd.GET_SERIAL_NUMBER, null,
				callback));
	}

	protected abstract String getSerialNumber() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException;

	@Override
	public final void getParameters(
			DriverCallback<List<AnalyzerParameter>> callback) {
		toCmdQueue(new Command<Object, List<AnalyzerParameter>>(
				Cmd.GET_PARAMETERS, null, callback));
	}

	protected abstract List<AnalyzerParameter> getParameters()
			throws IOException, InterruptedException, ConnectionException,
			ProtocolException, NoReplyException;

	@Override
	public final void showMeasuredParameters(
			DriverCallback<List<String>> callback) {
		toCmdQueue(new Command<Object, List<String>>(
				Cmd.SHOW_MEASURED_PARAMETERS, null, callback));
	}

	protected abstract List<String> showMeasuredParameters()
			throws IOException, InterruptedException, ConnectionException,
			ProtocolException, NoReplyException;

	@Override
	public final void showProcessParameters(
			DriverCallback<List<String>> callback) {
		toCmdQueue(new Command<Object, List<String>>(
				Cmd.SHOW_PROCESS_PARAMETERS, null, callback));
	}

	protected abstract List<String> showProcessParameters() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException;

	@Override
	public final void readParameter(String id,
			DriverCallback<ElementValue> callback) {
		toCmdQueue(new Command<String, ElementValue>(Cmd.READ_PARAMETER, id,
				callback));
	}

	protected abstract ElementValue readParameter(String id)
			throws IOException, InterruptedException, ConnectionException,
			ProtocolException, NoReplyException, InvalidParamException;

	@Override
	public final void getFaults(DriverCallback<List<AnalyzerFault>> callback) {
		toCmdQueue(new Command<Object, List<AnalyzerFault>>(Cmd.GET_FAULTS,
				null, callback));
	}

	protected abstract List<AnalyzerFault> getFaults() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException;

	@Override
	public final void showFaults(DriverCallback<List<String>> callback) {
		toCmdQueue(new Command<Object, List<String>>(Cmd.SHOW_FAULTS, null,
				callback));
	}

	protected abstract List<String> showFaults() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException;

	@Override
	public final void isFaultActive(String id, DriverCallback<Boolean> callback) {
		toCmdQueue(new Command<String, Boolean>(Cmd.IS_FAULT_ACTIVE, id,
				callback));
	}

	protected abstract Boolean isFaultActive(String id) throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException, InvalidParamException;

	@Override
	public final void resetFault(String id, DriverCallback<Boolean> callback) {
		toCmdQueue(new Command<String, Boolean>(Cmd.RESET_FAULT, id, callback));
	}

	protected abstract Boolean resetFault(String id) throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException, InvalidParamException;

	@Override
	public final void readFaultStatus(DriverCallback<FaultValue> callback) {
		toCmdQueue(new Command<Object, FaultValue>(Cmd.READ_FAULT_STATUS, null,
				callback));
	}

	protected abstract FaultValue readFaultStatus() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException;

	@Override
	public final void resetAllFaults(DriverCallback<FaultValue> callback) {
		toCmdQueue(new Command<Object, FaultValue>(Cmd.RESET_ALL_FAULTS, null,
				callback));
	}

	protected abstract FaultValue resetAllFaults() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException;

	@Override
	public final void sendKey(String key, DriverCallback<List<String>> callback) {
		toCmdQueue(new Command<String, List<String>>(Cmd.SEND_KEY, key,
				callback));
	}

	protected abstract List<String> sendKey(String key) throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException, InvalidParamException;

	@Override
	public String isRemoteGUISupported() {
		return null;
	}

	@Override
	public final void readDisplay(DriverCallback<List<String>> callback) {
		toCmdQueue(new Command<Object, List<String>>(Cmd.READ_DISPLAY, null,
				callback));
	}

	protected abstract List<String> readDisplay() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException;

	@Override
	public final void readDisplayImage(DriverCallback<byte[]> callback) {
		toCmdQueue(new Command<Object, byte[]>(Cmd.READ_DISPLAY_IMAGE, null,
				callback));
	}

	protected byte[] readDisplayImage() throws IOException,
			InterruptedException, ConnectionException, ProtocolException,
			NoReplyException {
		return null;
	}

	@Override
	public final void sendCommand(String[] command,
			DriverCallback<List<String>> callback) {
		toCmdQueue(new Command<String[], List<String>>(Cmd.SEND_COMMAND,
				command, callback));
	}

	protected abstract List<String> sendCommand(String[] command)
			throws IOException, InterruptedException, ConnectionException,
			ProtocolException, NoReplyException, InvalidParamException;

	@SuppressWarnings("unchecked")
	private void execute(Command<?, ?> cmd) {
		logger.debug("Processing command with id " + cmd.getId() + " for "
				+ getAnalyzerName());
		try {
			switch (cmd.getId()) {
			case TEST_COMMUNICATION:
				((Command<Object, Boolean>) cmd).setResult(testCommunication());
				break;
			case GET_DATE:
				((Command<Object, Date>) cmd).setResult(getDate());
				break;
			case SET_DATE:
				((Command<Object, Date>) cmd).setResult(setDate());
				break;
			case GET_SERIAL_NUMBER:
				((Command<Object, String>) cmd).setResult(getSerialNumber());
				break;
			case GET_PARAMETERS:
				((Command<Object, List<AnalyzerParameter>>) cmd)
						.setResult(getParameters());
				break;
			case SHOW_MEASURED_PARAMETERS:
				((Command<Object, List<String>>) cmd)
						.setResult(showMeasuredParameters());
				break;
			case SHOW_PROCESS_PARAMETERS:
				((Command<Object, List<String>>) cmd)
						.setResult(showProcessParameters());
				break;
			case READ_PARAMETER: {
				Command<String, ElementValue> c = (Command<String, ElementValue>) cmd;
				c.setResult(readParameter(c.getArg()));
				break;
			}
			case GET_FAULTS:
				((Command<Object, List<AnalyzerFault>>) cmd)
						.setResult(getFaults());
				break;
			case SHOW_FAULTS:
				((Command<Object, List<String>>) cmd).setResult(showFaults());
				break;
			case IS_FAULT_ACTIVE: {
				Command<String, Boolean> c = (Command<String, Boolean>) cmd;
				c.setResult(isFaultActive(c.getArg()));
				break;
			}
			case RESET_FAULT: {
				Command<String, Boolean> c = (Command<String, Boolean>) cmd;
				c.setResult(resetFault(c.getArg()));
				break;
			}
			case READ_FAULT_STATUS:
				((Command<Object, FaultValue>) cmd)
						.setResult(readFaultStatus());
				break;
			case RESET_ALL_FAULTS:
				((Command<Object, FaultValue>) cmd).setResult(resetAllFaults());
				break;
			case GET_KEYLIST:
				((Command<Object, List<String>>) cmd).setResult(getKeyList());
				break;
			case SEND_KEY: {
				Command<String, List<String>> c = (Command<String, List<String>>) cmd;
				c.setResult(sendKey(c.getArg()));
				break;
			}
			case READ_DISPLAY:
				((Command<Object, List<String>>) cmd).setResult(readDisplay());
				break;
			case READ_DISPLAY_IMAGE:
				((Command<Object, byte[]>) cmd).setResult(readDisplayImage());
				break;
			case SEND_COMMAND: {
				Command<String[], List<String>> c = (Command<String[], List<String>>) cmd;
				c.setResult(sendCommand(c.getArg()));
				break;
			}
			default:
				throw new IllegalStateException("Cannot execute command with "
						+ "unmanaged ID: " + cmd.getId());
			}
			logger.debug("Command " + cmd.getId() + " executed for "
					+ getAnalyzerName());
		} catch (IllegalStateException ex) {
			logger.error("Command " + cmd.getId() + " failed for "
					+ getAnalyzerName(), ex);
			cmd.setFailure(ex);
		} catch (ProtocolException ex) {
			protocolError = true;
			logger.debug("Command " + cmd.getId() + " failed for "
					+ getAnalyzerName(), ex);
			cmd.setFailure(ex);
		} catch (Exception ex) {
			connectErrors++;
			logger.debug("Command " + cmd.getId() + " failed for "
					+ getAnalyzerName(), ex);
			cmd.setFailure(ex);
		} finally {
			toCbQueue(cmd);
		}
	}

	private void toCmdQueue(Command<?, ?> command) {
		try {
			commandQueue.add(command);
			logger.debug("Enqueued command with id " + command.getId()
					+ " for " + getAnalyzerName());
		} catch (IllegalStateException ex) {
			commandQueueOverruns++;
			logger.error("Command queue is full. Total number of discarded "
					+ "commands: " + commandQueueOverruns);
			command.setFailure(ex);
			toCbQueue(command);
		}
	}

	private void toCbQueue(Command<?, ?> command) {
		if (!callbackQueue.offer(command))
			callbackQueueOverruns++;
	}

	private class Command<T, R> {

		private Cmd id;
		private T arg;
		private DriverCallback<R> callback;
		private R result;
		private Throwable throwable;

		Command(Cmd id, T arg, DriverCallback<R> callback) {
			this.id = id;
			this.arg = arg;
			this.callback = callback;
		}

		Cmd getId() {
			return id;
		}

		T getArg() {
			return arg;
		}

		void setResult(R result) {
			this.result = result;
		}

		void setFailure(Throwable throwable) {
			this.throwable = throwable;
		}

		void deliverResult() {
			if (throwable != null)
				callback.onFailure(throwable);
			else
				callback.onSuccess(result);
		}
	}

	private class CallBackRunner implements Runnable {

		private volatile Thread cbRunnerThread = null;

		void start() {
			cbRunnerThread = new Thread(this, "cb_runner_" + threadIndex);
			cbRunnerThread.setDaemon(true);
			cbRunnerThread.setPriority(CB_THREAD_PRIORITY);
			cbRunnerThread.start();
		}

		void stop() {
			Thread tmpThread = cbRunnerThread;
			cbRunnerThread = null;
			if (tmpThread != null) {
				try {
					int maxWaitTime_s = 3;
					logger.info("Waiting up to " + maxWaitTime_s
							+ "s for call back runner thread "
							+ tmpThread.getName() + " to terminate...");
					tmpThread.join(maxWaitTime_s * 1000);
					logger.info("Finished waiting for call back runner thread "
							+ tmpThread.getName() + " to terminate");
				} catch (InterruptedException ie) {
					logger.error("Wait for call back runner thread "
							+ tmpThread.getName() + " to terminate interrupted");
				}
			}
		}

		@Override
		public void run() {
			logger.info("Callback runner thread " + threadIndex
					+ " started for analyzer " + getAnalyzerName());
			Command<?, ?> command;
			while (Thread.currentThread() == cbRunnerThread) {
				try {
					command = callbackQueue.poll(MAX_CB_WAIT_TIME_ms,
							TimeUnit.MILLISECONDS);
					if (command == null)
						continue;
					logger.debug("Delivering result for command with id "
							+ command.getId() + " for " + getAnalyzerName());
				} catch (InterruptedException e) {
					logger.warn("Interruption requested for callback runner"
							+ " thread " + threadIndex);
					break;
				}
				try {
					command.deliverResult();
				} catch (Exception ex) {
					logger.error("An error occurred during driver callback", ex);
				}
			}
			logger.info("Callback runner thread " + threadIndex
					+ " shutdown in progress...");
			logger.info("Callback runner thread " + threadIndex + " stopped");
		}
	}

}
