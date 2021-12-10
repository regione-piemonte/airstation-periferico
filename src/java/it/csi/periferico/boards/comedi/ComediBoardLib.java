/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: board library implementation based on Comedi
// Change log:
//   2008-04-17: initial version
// ----------------------------------------------------------------------------
// $Id: ComediBoardLib.java,v 1.21 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards.comedi;

import it.csi.aria.jcomedilib.Comedi;
import it.csi.aria.jcomedilib.ComediChannel;
import it.csi.aria.jcomedilib.ComediDevice;
import it.csi.aria.jcomedilib.ComediException;
import it.csi.aria.jcomedilib.ComediRange;
import it.csi.aria.jcomedilib.ComediSubdevice;
import it.csi.aria.jcomedilib.DriverManager;
import it.csi.aria.jcomedilib.LSamplT;
import it.csi.periferico.boards.AISubdevice;
import it.csi.periferico.boards.AIValue;
import it.csi.periferico.boards.AnalogInput;
import it.csi.periferico.boards.Board;
import it.csi.periferico.boards.Board.BoardStatus;
import it.csi.periferico.boards.BoardDescriptor;
import it.csi.periferico.boards.BoardLibItf;
import it.csi.periferico.boards.BoardsException;
import it.csi.periferico.boards.DIEvent;
import it.csi.periferico.boards.DIOSubdevice;
import it.csi.periferico.boards.DISubdevice;
import it.csi.periferico.boards.DOSubdevice;
import it.csi.periferico.boards.DigitalIO;
import it.csi.periferico.boards.DigitalInput;
import it.csi.periferico.boards.DigitalOutput;
import it.csi.periferico.boards.DriverParam;
import it.csi.periferico.boards.DriverParamDescriptor;
import it.csi.periferico.boards.ExternalAcqItf;
import it.csi.periferico.boards.ISABoard;
import it.csi.periferico.boards.PCIBoard;
import it.csi.periferico.boards.PCIBoardDescriptor;
import it.csi.periferico.boards.PCIDevice;
import it.csi.periferico.boards.Range;
import it.csi.periferico.config.common.ConfigException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Board library implementation based on Comedi
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ComediBoardLib implements BoardLibItf, ExternalAcqItf {

	// This is the settling time needed by the analog input multiplexer of
	// the Advantech PCI 1710 board. It should be adequate for similar boards;
	// if not, a settling time parameter will have to be added to the class
	// BoardDescriptor and configured for each board specified in the common
	// configuration
	public static final int AI_SET_TIME_NS = 15000;

	private DriverManager driverManager;

	private static Logger logger = Logger.getLogger("periferico."
			+ ComediBoardLib.class.getSimpleName());

	private List<String> comediOptions;

	public ComediBoardLib(List<String> comediOptions) {
		this.comediOptions = comediOptions;
	}

	public void init() throws BoardsException {
		try {
			logger.info("Initializing Comedi driver manager");
			driverManager = DriverManager.getInstance(comediOptions);
			logger.info("Initializing Comedi driver library");
			Comedi.init();
			Comedi.set_global_oor_behavior(Comedi.OOR_NAN);
		} catch (ComediException ce) {
			logger.error("Comedi initialization failed", ce);
			throw new BoardsException("board_library_failed");
		}
	}

	public void cleanup() throws BoardsException {
		logger.info("Comedi drivers cleanup");
		if (driverManager == null)
			return;
		try {
			driverManager.clearAllBindings();
		} catch (ComediException ce) {
			logger.error("Comedi drivers cleanup failed", ce);
			throw new BoardsException("board_cleanup_failed");
		}
	}

	@Override
	public boolean isSupported(Board board) {
		return BoardLibItf.COMEDI.equals(board.getSupportingLibName());
	}

	@Override
	public void initBoard(Board board, BoardDescriptor desc)
			throws BoardsException {
		logger.info("Initializing board using Comedi Library");
		if (board == null)
			throw new IllegalArgumentException(
					"Board argument should not be null");
		if (!isSupported(board))
			throw new IllegalArgumentException("The board " + board + " is not"
					+ " supported by Comedi driver library");
		if (driverManager == null) {
			logger.error("Comedi driver manager is not initialized");
			throw new BoardsException("comedi_not_initialized");
		}
		board.setDriverHandle(null);
		board.setBoardStatus(BoardStatus.INIT_FAILED);
		if (desc == null)
			throw new IllegalArgumentException(
					"BoardDescriptor argument must not be null");
		int numParamDescriptors = desc.getDriverParamDescriptors().size();
		String params[] = null;
		int numBoardParams = 0;
		String driverName = null;
		if (board instanceof PCIBoard) {
			numBoardParams = 2;
			PCIDevice dev = ((PCIBoard) board).getPciDevice();
			params = new String[numBoardParams + numParamDescriptors];
			params[0] = Integer.toString(dev.getBus());
			params[1] = Integer.toString(dev.getSlot());
			driverName = ((PCIBoardDescriptor) desc)
					.getDriverName(((PCIBoard) board).getVersion());
		} else if (board instanceof ISABoard) {
			numBoardParams = 0;
			params = new String[numBoardParams + numParamDescriptors];
			driverName = desc.getDriverName();
		} else {
			throw new IllegalStateException("Unknown board type");
		}
		for (int i = numBoardParams; i < params.length; i++)
			params[i] = "";
		for (DriverParam dp : board.getDriverParams()) {
			String name = dp.getName();
			String value = dp.getValue();
			DriverParamDescriptor dpd = desc.getDriverParamDescriptor(name);
			if (dpd == null) {
				String brandModel = board.getBrand() + " " + board.getModel();
				logger.error("The parameter " + name
						+ " is unknown for board: " + brandModel);
				throw new BoardsException("param_unknown", name, brandModel);
			}
			if (dpd.getIndex() >= numParamDescriptors) {
				logger.error("Parameter " + name + " has invalid index");
				throw new BoardsException("param_invalid_index", name);
			}
			params[numBoardParams + dpd.getIndex()] = value;
		}
		StringBuffer strParams = new StringBuffer();
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				strParams.append(params[i]);
				if (i < params.length - 1)
					strParams.append(",");
			}
		}
		String paramString = strParams.toString();
		String kernelModuleName = desc.getKernelModuleName();
		logger.info("Loading/verifying kernel module: " + kernelModuleName);
		try {
			driverManager.loadDriverModule(kernelModuleName);
		} catch (ComediException e) {
			logger.error("Error loading/verifying kernel module: "
					+ kernelModuleName, e);
			throw new BoardsException("comedi_load_module_error",
					kernelModuleName);
		}
		logger.info("Initializing comedi driver " + driverName
				+ " with parameters: " + paramString);
		String devFile;
		try {
			devFile = driverManager.bindDriver(driverName, paramString);
		} catch (ComediException ex) {
			logger.error("Comedi driver " + driverName
					+ " initialization failed", ex);
			throw new BoardsException("comedi_driver_init_error", driverName);
		}
		logger.info("Initializing comedi board " + driverName
				+ " using device file: " + devFile);
		try {
			List<AISubdevice> listAISubdevice = new ArrayList<AISubdevice>();
			List<DISubdevice> listDISubdevice = new ArrayList<DISubdevice>();
			List<DOSubdevice> listDOSubdevice = new ArrayList<DOSubdevice>();
			List<DIOSubdevice> listDIOSubdevice = new ArrayList<DIOSubdevice>();
			board.setListAISubdevice(listAISubdevice);
			board.setListDISubdevice(listDISubdevice);
			board.setListDOSubdevice(listDOSubdevice);
			board.setListDIOSubdevice(listDIOSubdevice);
			ComediDevice dev = new ComediDevice(devFile);
			ComediSubdevice[] subdevs = dev.getSubdevices();
			for (int sub = 0; sub < subdevs.length; sub++) {
				ComediSubdevice subdev = subdevs[sub];
				ComediChannel[] channels = subdev.getChannels();
				switch (subdev.getType()) {
				case Comedi.SUBD_AI:
					AISubdevice aiSub = new AISubdevice(board, sub);
					listAISubdevice.add(aiSub);
					List<AnalogInput> listAI = new ArrayList<AnalogInput>();
					aiSub.setListAI(listAI);
					for (int chan = 0; chan < channels.length; chan++) {
						ComediChannel channel = channels[chan];
						AnalogInput ai = new AnalogInput(aiSub, chan);
						ComediRange[] comediRanges = channel.getRangeInfos();
						List<Range> listRange = new ArrayList<Range>();
						for (int i = 0; i < comediRanges.length; i++) {
							ComediRange cr = comediRanges[i];
							try {
								listRange.add(new Range(cr.getMin(), cr
										.getMax()));
							} catch (ConfigException e) {
								logger.error("Cannot convert ComediRange " + cr
										+ " to Range", e);
							}
						}
						ai.setListRange(listRange);
						listAI.add(ai);
						// Print current AI values
						LSamplT data = new LSamplT(0);
						channel.dataReadDelayed(data, AI_SET_TIME_NS);
						logger.debug("AI (subdevice/channel/DAC_value): " + sub
								+ "/" + chan + "/" + data.getValue());
					}
					if (desc.isDifferentialModeSupported()) {
						Iterator<AnalogInput> itAI = listAI.iterator();
						while (itAI.hasNext()) {
							AnalogInput aiEven = itAI.next();
							if (!itAI.hasNext())
								break;
							AnalogInput aiOdd = itAI.next();
							aiEven.setDifferentialModeChannelLow(aiOdd);
							aiOdd.setDifferentialModeChannelHigh(aiEven);
						}
					}
					break;
				case Comedi.SUBD_AO:
					// Do nothing, we don't need analog outputs
					break;
				case Comedi.SUBD_DI:
					DISubdevice diSub = new DISubdevice(board, sub);
					listDISubdevice.add(diSub);
					List<DigitalInput> listDI = new ArrayList<DigitalInput>();
					diSub.setListDI(listDI);
					for (int chan = 0; chan < channels.length; chan++) {
						DigitalInput di = new DigitalInput(diSub, chan);
						listDI.add(di);
						// Print current DI values
						ComediChannel channel = channels[chan];
						int pvalue[] = new int[1];
						channel.dioRead(pvalue);
						logger.debug("DI (subdevice/channel/status): " + sub
								+ "/" + chan + "/" + pvalue[0]);
					}
					break;
				case Comedi.SUBD_DO:
					DOSubdevice doSub = new DOSubdevice(board, sub);
					listDOSubdevice.add(doSub);
					List<DigitalOutput> listDO = new ArrayList<DigitalOutput>();
					doSub.setListDO(listDO);
					for (int chan = 0; chan < channels.length; chan++) {
						DigitalOutput dout = new DigitalOutput(doSub, chan);
						listDO.add(dout);
					}
					break;
				case Comedi.SUBD_DIO:
					DIOSubdevice dioSub = new DIOSubdevice(board, sub);
					listDIOSubdevice.add(dioSub);
					List<DigitalIO> listDIO = new ArrayList<DigitalIO>();
					dioSub.setListDIO(listDIO);
					for (int chan = 0; chan < channels.length; chan++) {
						DigitalIO dio = new DigitalIO(dioSub, chan);
						listDIO.add(dio);
					}
					break;
				default:
					break;
				}
			}
			board.setBoardStatus(BoardStatus.INIT_OK);
			board.setDriverHandle(dev);
			return;
		} catch (ComediException ex) {
			logger.error("Initialization failed", ex);
			logger.info("Freeing device file: " + devFile);
			try {
				driverManager.unbindDriver(devFile);
			} catch (Exception ex2) {
				logger.error("Cannot free device file: " + devFile, ex2);
			}
			throw new BoardsException("comedi_board_init_failed", driverName);
		}
	}

	@Override
	public void startAcquisition(Board board) {
		// Nothing to do here, because Comedi data acquisition thread is
		// provided by AcqManager
	}

	@Override
	public void dismissBoard(Board board) throws BoardsException {
		logger.info("Dismissing board using Comedi Library");
		if (board == null)
			throw new IllegalArgumentException(
					"Board argument should not be null");
		if (driverManager == null) {
			logger.error("Comedi board library is not initialized");
			throw new BoardsException("board_library_not_initialized");
		}
		Object handle = board.getDriverHandle();
		if (handle == null) {
			logger.error("Handle argument should not be null");
			throw new BoardsException("cannot_dismiss_board");
		}
		if (!(handle instanceof ComediDevice)) {
			logger.error("Handle argument should be of class ComediDevice");
			throw new BoardsException("cannot_dismiss_board_comedi");
		}
		ComediDevice comediDevice = (ComediDevice) handle;
		String devFile = comediDevice.getDeviceName();
		try {
			comediDevice.close();
			driverManager.unbindDriver(devFile);
			board.setDriverHandle(null);
		} catch (ComediException ce) {
			logger.error("Board driver cleanup failed", ce);
			throw new BoardsException("board_driver_cleanup_failed");
		}
	}

	public AIValue readAI(Board board, AISubdevice aiSubdevice, AnalogInput ai) {
		Range aiRange = ai.getActiveRange();
		ComediDevice dev = (ComediDevice) board.getDriverHandle();
		ComediSubdevice sub = dev.getSubdevice(aiSubdevice.getSubdevice());
		ComediChannel channel = sub.getChannel(ai.getChannel());
		double voltage = Double.NaN;
		boolean outOfRange = false;
		boolean error = false;
		Date acqTimestamp = new Date();
		try {
			if (ai.isDifferentialModeActive())
				channel.setAnalogRef(Comedi.AREF_DIFF);
			else
				channel.setAnalogRef(Comedi.AREF_COMMON);
			int range = channel.findRange(Comedi.UNIT_volt, aiRange.getMin(),
					aiRange.getMax());
			ComediRange cRange = channel.getRangeInfo(range);
			channel.setActiveRange(range);
			LSamplT data = new LSamplT(0);
			channel.dataReadDelayed(data, AI_SET_TIME_NS);
			LSamplT maxdata = channel.getMaxdata();
			voltage = Comedi.to_phys(data, cRange, maxdata);
			// ATTENTION: the check Double.isNaN(voltage) should be enough, but
			// it depends on the NAN definition used by Comedi that may be
			// defined in the underlying OS: if NAN is different from Double.NaN
			// the check does not work. The following check is less elegant but
			// 100% reliable:
			outOfRange = Double.isNaN(voltage) || data.getValue() <= 0
					|| data.getValue() >= maxdata.getValue();
		} catch (ComediException ce) {
			error = true;
			if (!ai.isAcqError())
				logger.error(
						"Error reading analog input: " + board.getId() + "/"
								+ aiSubdevice.getSubdevice() + "/"
								+ ai.getChannel(), ce);
		}
		AIValue aiValue = new AIValue(acqTimestamp, voltage, error, outOfRange,
				null);
		return aiValue;
	}

	public DIEvent readDI(Board board, DISubdevice diSubdevice, DigitalInput di) {
		ComediDevice dev = (ComediDevice) board.getDriverHandle();
		ComediSubdevice sub = dev.getSubdevice(diSubdevice.getSubdevice());
		ComediChannel channel = sub.getChannel(di.getChannel());
		boolean error = false;
		boolean value = false;
		Date acqTimestamp = new Date();
		try {
			int pvalue[] = new int[1];
			channel.dioRead(pvalue);
			value = pvalue[0] != 0;
		} catch (ComediException ce) {
			error = true;
			if (!di.isAcqError())
				logger.error(
						"Error reading digital input: " + board.getId() + "/"
								+ diSubdevice.getSubdevice() + "/"
								+ di.getChannel(), ce);
		}
		DIEvent diEvent = new DIEvent(acqTimestamp, value, error, null);
		return diEvent;
	}

	// ATTENTION: if the direction of a digital IO subdevice affects all the
	// channels together, all its channels should be cabled for the same
	// direction
	public DIEvent readDIO(Board board, DIOSubdevice dioSubdevice, DigitalIO dio) {
		ComediDevice dev = (ComediDevice) board.getDriverHandle();
		ComediSubdevice sub = dev.getSubdevice(dioSubdevice.getSubdevice());
		ComediChannel channel = sub.getChannel(dio.getChannel());
		boolean error = false;
		boolean value = false;
		Date acqTimestamp = new Date();
		try {
			int pvalue[] = new int[1];
			channel.dioConfig(Comedi.INPUT);
			channel.dioRead(pvalue);
			value = pvalue[0] != 0;
		} catch (ComediException ce) {
			error = true;
			if (!dio.isAcqError())
				logger.debug(
						"Error reading digital input: " + board.getId() + "/"
								+ dioSubdevice.getSubdevice() + "/"
								+ dio.getChannel(), ce);
		}
		DIEvent diEvent = new DIEvent(acqTimestamp, value, error, null);
		return diEvent;
	}

	public boolean writeDO(Board board, DOSubdevice doSubdevice,
			DigitalOutput dout, boolean value) {
		ComediDevice dev = (ComediDevice) board.getDriverHandle();
		ComediSubdevice sub = dev.getSubdevice(doSubdevice.getSubdevice());
		ComediChannel channel = sub.getChannel(dout.getChannel());
		boolean success = true;
		try {
			channel.dioWrite(value ? 1 : 0);
		} catch (ComediException ce) {
			success = false;
			if (!dout.isAcqError())
				logger.error(
						"Error writing digital output: " + board.getId() + "/"
								+ doSubdevice.getSubdevice() + "/"
								+ dout.getChannel(), ce);
		}
		return success;
	}

	public boolean writeDIO(Board board, DIOSubdevice dioSubdevice,
			DigitalIO dio, boolean value) {
		ComediDevice dev = (ComediDevice) board.getDriverHandle();
		ComediSubdevice sub = dev.getSubdevice(dioSubdevice.getSubdevice());
		ComediChannel channel = sub.getChannel(dio.getChannel());
		boolean success = true;
		try {
			channel.dioConfig(Comedi.OUTPUT);
			channel.dioWrite(value ? 1 : 0);
		} catch (ComediException ce) {
			success = false;
			if (!dio.isAcqError())
				logger.error(
						"Error writing digital output: " + board.getId() + "/"
								+ dioSubdevice.getSubdevice() + "/"
								+ dio.getChannel(), ce);
		}
		return success;
	}

}