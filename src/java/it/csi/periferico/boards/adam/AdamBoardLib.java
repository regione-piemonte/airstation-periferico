/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: board library implementation based on Advantech Adam
// Change log:
//   2016-10-10: initial version
// ----------------------------------------------------------------------------
// $Id:$
// ----------------------------------------------------------------------------

package it.csi.periferico.boards.adam;

import it.csi.periferico.acqdrivers.conn.ConnectionClosedException;
import it.csi.periferico.boards.AISubdevice;
import it.csi.periferico.boards.AnalogInput;
import it.csi.periferico.boards.Board;
import it.csi.periferico.boards.Board.BoardStatus;
import it.csi.periferico.boards.BoardDescriptor;
import it.csi.periferico.boards.BoardLibItf;
import it.csi.periferico.boards.BoardsException;
import it.csi.periferico.boards.DIOSubdevice;
import it.csi.periferico.boards.DISubdevice;
import it.csi.periferico.boards.DOSubdevice;
import it.csi.periferico.boards.DigitalIO;
import it.csi.periferico.boards.DigitalInput;
import it.csi.periferico.boards.DigitalOutput;
import it.csi.periferico.boards.DriverParam;
import it.csi.periferico.boards.DriverParamDescriptor;
import it.csi.periferico.boards.Range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Board library implementation based on Advantech Adam
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AdamBoardLib implements BoardLibItf {

	private static Logger logger = Logger.getLogger("periferico.adam."
			+ AdamBoardLib.class.getSimpleName());
	private Map<String, AdamModuleInfo> mapModuleInfo = new HashMap<String, AdamModuleInfo>();

	public AdamBoardLib(List<AdamModuleInfo> listAdamModuleInfo) {
		for (AdamModuleInfo abi : listAdamModuleInfo)
			mapModuleInfo.put(abi.getModelName(), abi);
	}

	@Override
	public void init() throws BoardsException {
		logger.info("Initializing Advantech Adam driver");
	}

	@Override
	public void cleanup() throws BoardsException {
		logger.info("Advantech Adam driver cleanup");
	}

	@Override
	public boolean isSupported(Board board) {
		return BoardLibItf.ADAM.equals(board.getSupportingLibName());
	}

	private void checkValidBoard(Board board) {
		if (board == null)
			throw new IllegalArgumentException(
					"Board argument should not be null");
		if (!isSupported(board))
			throw new IllegalArgumentException("The board " + board + " is not"
					+ " supported by Advantech Adam driver");
	}

	@Override
	public void initBoard(Board board, BoardDescriptor desc)
			throws BoardsException {
		logger.info("Initializing board using Advantech Adam driver");
		checkValidBoard(board);
		board.setDriverHandle(null);
		board.setBoardStatus(BoardStatus.INIT_FAILED);
		if (desc == null)
			throw new IllegalArgumentException(
					"BoardDescriptor argument should not be null");
		Map<Integer, DriverParam> mapParams = new HashMap<Integer, DriverParam>();
		StringBuffer strParams = new StringBuffer();
		for (DriverParam dp : board.getDriverParams()) {
			String name = dp.getName();
			String value = dp.getValue();
			DriverParamDescriptor dpd = desc.getDriverParamDescriptor(name);
			if (dpd == null) {
				logger.error("The parameter " + name
						+ " is unknown for board: " + board);
				throw new BoardsException("param_unknown", name,
						board.toString());
			}
			mapParams.put(dpd.getIndex(), dp);
			strParams.append(" " + name + "=" + value);
		}
		logger.info("Initializing Advantech Adam driver for "
				+ board.getModel() + " with parameters:" + strParams);
		AdamHandle handle;
		try {
			handle = HandleFactory.newInstance(desc.getDriverName(), mapParams);
		} catch (BoardsException ex) {
			logger.error("Adam driver for " + board.getModel()
					+ " initialization failed", ex);
			throw new BoardsException("adam_driver_init_error", ex.getMessage());
		}
		logger.info("Initializing adam board " + board.getModel()
				+ " using resource: " + handle);
		try {
			handle.init(mapModuleInfo);
			setSubdevices(board, handle);
			board.setDriverHandle(handle);
		} catch (Exception ex) {
			logger.error("Initialization failed", ex);
			logger.info("Freeing resource: " + handle);
			handle.dispose();
			throw new BoardsException("adam_board_init_failed",
					board.getModel());
		}
	}

	@Override
	public void startAcquisition(Board board) {
		logger.info("Starting acquisition for board " + board);
		checkValidBoard(board);
		Object handle = board.getDriverHandle();
		if (!(handle instanceof AdamHandle))
			throw new IllegalStateException("Board " + board
					+ " does not have a valid Adam driver handle");
		AdamHandle adamHandle = (AdamHandle) handle;
		adamHandle.startAcquisition(board);
	}

	@Override
	public void dismissBoard(Board board) throws BoardsException {
		logger.info("Dismissing board using Advantech Adam driver");
		if (board == null)
			throw new IllegalArgumentException(
					"Board argument should not be null");
		Object handle = board.getDriverHandle();
		if (handle == null) {
			logger.error("Handle argument should not be null");
			throw new BoardsException("cannot_dismiss_board");
		}
		if (!(handle instanceof AdamHandle)) {
			logger.error("Handle argument should be of class AdamHandle");
			throw new BoardsException("cannot_dismiss_board");
		}
		AdamHandle adamHandle = (AdamHandle) handle;
		try {
			adamHandle.stopAcquisition();
			adamHandle.dispose();
			board.setDriverHandle(null);
		} catch (Exception e) {
			logger.error("Board driver cleanup failed", e);
			throw new BoardsException("board_driver_cleanup_failed");
		}
	}

	private void setSubdevices(Board board, AdamHandle handle)
			throws BoardsException, ConnectionClosedException, IOException,
			InterruptedException {
		List<AISubdevice> listAISubdevice = new ArrayList<AISubdevice>();
		List<DISubdevice> listDISubdevice = new ArrayList<DISubdevice>();
		List<DOSubdevice> listDOSubdevice = new ArrayList<DOSubdevice>();
		List<DIOSubdevice> listDIOSubdevice = new ArrayList<DIOSubdevice>();
		board.setListAISubdevice(listAISubdevice);
		board.setListDISubdevice(listDISubdevice);
		board.setListDOSubdevice(listDOSubdevice);
		board.setListDIOSubdevice(listDIOSubdevice);
		for (Integer address : handle.getModuleAddresses()) {
			AcquisitionModule module = handle.getAcquisitionModule(address);
			int numChannels = module.getNumAI();
			if (numChannels > 0) {
				AISubdevice aiSub = new AISubdevice(board, address);
				listAISubdevice.add(aiSub);
				List<AnalogInput> listAI = new ArrayList<AnalogInput>();
				aiSub.setListAI(listAI);
				for (int chan = 0; chan < numChannels; chan++) {
					AnalogInput ai = new AnalogInput(aiSub, chan);
					ai.setListRange(new ArrayList<Range>(module.getListRange()));
					int code = handle.readAIRangeCode(address, chan);
					AdamRange currentRange = module.getRange(code);
					if (currentRange == null)
						throw new BoardsException("Cannot find range with "
								+ "code '" + String.format("%02X", code)
								+ "' for AI (subdevice/channel): " + address
								+ "/" + chan);
					ai.setActiveRange(currentRange);
					ai.onRangeSetToBoard();
					listAI.add(ai);
					logger.info("AI (subdevice/channel/range): " + address
							+ "/" + chan + "/" + currentRange);
					// Print current AI values
					if (logger.isDebugEnabled()) {
						Double val = handle.readAI(address, chan, currentRange);
						logger.debug("AI (subdevice/channel/voltage): "
								+ address + "/" + chan + "/"
								+ (val == null ? "out_of_range" : val));
					}
				}
			}
			numChannels = module.getNumDI();
			if (numChannels > 0) {
				DISubdevice diSub = new DISubdevice(board, address);
				listDISubdevice.add(diSub);
				List<DigitalInput> listDI = new ArrayList<DigitalInput>();
				diSub.setListDI(listDI);
				for (int chan = 0; chan < numChannels; chan++) {
					DigitalInput di = new DigitalInput(diSub, chan);
					listDI.add(di);
					// Print current DI values
					if (logger.isDebugEnabled()) {
						int val = handle.readDI(address, chan) ? 1 : 0;
						logger.debug("DI (subdevice/channel/status): "
								+ address + "/" + chan + "/" + val);
					}
				}
			}
			numChannels = module.getNumDO();
			if (numChannels > 0) {
				DOSubdevice doSub = new DOSubdevice(board, address);
				listDOSubdevice.add(doSub);
				List<DigitalOutput> listDO = new ArrayList<DigitalOutput>();
				doSub.setListDO(listDO);
				for (int chan = 0; chan < numChannels; chan++) {
					DigitalOutput dout = new DigitalOutput(doSub, chan);
					listDO.add(dout);
					// Set DO at random values
					// WARNING: uncomment the following code only for tests
					// purposes and NOT in production environment
					// if (logger.isDebugEnabled()) {
					// long value = Math.round(Math.random());
					// handle.writeDO(address, chan, value != 0);
					// logger.debug("DO (subdevice/channel/status): "
					// + address + "/" + chan + "/" + value);
					// }
				}
			}
			numChannels = module.getNumDIO();
			if (numChannels > 0) {
				DIOSubdevice dioSub = new DIOSubdevice(board, address);
				listDIOSubdevice.add(dioSub);
				List<DigitalIO> listDIO = new ArrayList<DigitalIO>();
				dioSub.setListDIO(listDIO);
				for (int chan = 0; chan < numChannels; chan++) {
					DigitalIO dio = new DigitalIO(dioSub, chan);
					listDIO.add(dio);
				}
			}
		}
		board.setBoardStatus(BoardStatus.INIT_OK);
	}

}
