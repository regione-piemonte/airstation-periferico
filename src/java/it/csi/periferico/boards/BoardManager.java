/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: manages acquisition boards
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: BoardManager.java,v 1.25 2012/10/15 12:13:26 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.Periferico;
import it.csi.periferico.boards.Board.BoardStatus;
import it.csi.periferico.boards.adam.AdamBoardLib;
import it.csi.periferico.boards.comedi.ComediBoardLib;
import it.csi.periferico.config.BoardsCfg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Manages acquisition boards
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class BoardManager {

	private static Logger logger = Logger.getLogger("periferico."
			+ BoardManager.class.getSimpleName());

	private BoardDescriptors boardDescriptors;

	private List<Board> boards;

	private List<PCIDevice> unconfiguredDevices;

	private List<BoardLibItf> boardLibItfs;

	private boolean pciBusNeeded = false;

	public BoardManager(BoardDescriptors boardDescriptors, List<Board> boards) {
		if (boardDescriptors == null || boards == null)
			throw new IllegalArgumentException("Null arguments not allowed");
		this.boardDescriptors = boardDescriptors;
		this.boards = boards;
		unconfiguredDevices = null;
		BoardsCfg bc = Periferico.getInstance().getBoardsCfg();
		List<BoardLibItf> listBoardLibs = new ArrayList<BoardLibItf>();
		if (boardDescriptors.isComediEnabled())
			listBoardLibs.add(new ComediBoardLib(bc.getComediOptions()));
		if (boardDescriptors.isAdamEnabled())
			listBoardLibs.add(new AdamBoardLib(bc.getAdamBoardsInfo()));
		boardLibItfs = listBoardLibs;
		if (boardDescriptors != null && boardDescriptors.getList() != null) {
			for (BoardDescriptor desc : boardDescriptors.getList())
				if (desc instanceof PCIBoardDescriptor)
					pciBusNeeded = true;
		}
	}

	/**
	 * Initializes the board driver library and each board
	 * 
	 * @return -2, if the scan of the PCI bus fails -1, if the board driver
	 *         library initialization fails, the number of successfully
	 *         initialized boards, otherwise
	 */
	public int init() {
		unconfiguredDevices = new ArrayList<PCIDevice>();
		List<PCIDevice> listDetectedPCIAcqDevices = detectPCIAcqDevices();
		if (listDetectedPCIAcqDevices == null) {
			return -2;
		}
		for (Board board : boards) {
			board.setBoardStatus(BoardStatus.PRE_INIT);
			if (board instanceof PCIBoard) {
				PCIBoard pciBoard = (PCIBoard) board;
				pciBoard.setBoardStatus(BoardStatus.NOT_DETECTED);
				PCIDevice boardDevice = pciBoard.getPciDevice();
				Iterator<PCIDevice> itDetectedDevices = listDetectedPCIAcqDevices
						.iterator();
				while (itDetectedDevices.hasNext()) {
					if (boardDevice.equals(itDetectedDevices.next())) {
						pciBoard.setBoardStatus(BoardStatus.DETECTED);
						itDetectedDevices.remove();
						break;
					}
				}
			}
		}
		unconfiguredDevices = listDetectedPCIAcqDevices;
		for (PCIDevice udev : unconfiguredDevices) {
			logger.warn("Unconfigured PCI acquisition device detected: " + udev);
		}
		try {
			logger.info("Initializing acquisition board libraries");
			for (BoardLibItf bi : boardLibItfs)
				bi.init();
		} catch (Exception e) {
			logger.error("Error initializing acquisition board libraries", e);
			return -1;
		}
		logger.info("Initializing acquisition boards");
		int initializedBoards = 0;
		for (Board board : boards) {
			if (board instanceof PCIBoard) {
				PCIBoard pciBoard = (PCIBoard) board;
				if (pciBoard.getBoardStatus() == BoardStatus.DETECTED) {
					try {
						initPCIBoard(pciBoard);
						initializedBoards++;
					} catch (BoardsException e) {
						logger.error("Initialization failed for PCI board: "
								+ board, e);
					}
				} else {
					logger.error("PCI board not found: " + pciBoard.getBrand()
							+ " " + pciBoard.getModel() + ", PCI info = "
							+ pciBoard.getPciDevice());
				}
			} else {
				try {
					initBoard(board);
					initializedBoards++;
				} catch (BoardsException e) {
					logger.error("Initialization failed for board: " + board, e);
				}
			}
		}
		return initializedBoards;
	}

	public void start() {
		logger.info("Starting acquisition board libraries");
		for (BoardLibItf itf : boardLibItfs)
			if (!(itf instanceof ExternalAcqItf))
				for (Board board : boards)
					if (itf.isSupported(board))
						itf.startAcquisition(board);
	}

	public void destroy() {
		if (boards != null) {
			int index = 0;
			for (Board board : boards) {
				index++;
				if (board.getBoardStatus() != Board.BoardStatus.INIT_OK)
					continue;
				String boardName = "(" + (index) + ") " + board.getBrand()
						+ " " + board.getModel();
				logger.info("Dismissing " + boardName);
				try {
					for (BoardLibItf bi : boardLibItfs)
						if (bi.isSupported(board))
							bi.dismissBoard(board);
				} catch (BoardsException e) {
					logger.error("Error dismissing board " + boardName, e);
				}
			}
		}
		try {
			logger.info("Acquisition board libraries cleanup");
			for (BoardLibItf bi : boardLibItfs)
				bi.cleanup();
		} catch (Exception e) {
			logger.error("Acquisition board libraries cleanup error", e);
		}
	}

	public int getConfiguredBoardsNumber() {
		if (boards == null)
			return 0;
		return boards.size();
	}

	public List<PCIDevice> getUnconfiguredDevices() {
		return unconfiguredDevices;
	}

	public List<PCIDevice> detectPCIAcqDevices() {
		List<PCIDevice> listPCIAcqDevices = new ArrayList<PCIDevice>();
		if (!pciBusNeeded)
			return listPCIAcqDevices;
		List<PCIDevice> listDevice = listPciDevices();
		if (listDevice == null)
			return null;
		for (PCIDevice device : listDevice) {
			logger.debug("PCI device detected: " + device);
			if (boardDescriptors
					.get(device.getVendorId(), device.getDeviceId()) != null) {
				listPCIAcqDevices.add(device);
				logger.debug("PCI Acquisition device detected: " + device);
			}
		}
		return listPCIAcqDevices;
	}

	public boolean isDeviceExistent(PCIDevice deviceToCheck) {
		if (deviceToCheck == null)
			return false;
		for (Board board : boards) {
			if (board instanceof PCIBoard) {
				PCIBoard pciBoard = (PCIBoard) board;
				if (deviceToCheck.equals(pciBoard.getPciDevice()))
					return true;
			}
		}
		for (PCIDevice device : unconfiguredDevices) {
			if (deviceToCheck.equals(device))
				return true;
		}
		return false;
	}

	public boolean isDeviceInitialized(PCIDevice deviceToCheck) {
		if (deviceToCheck == null)
			return false;
		for (Board board : boards) {
			if (board instanceof PCIBoard) {
				PCIBoard pciBoard = (PCIBoard) board;
				if (deviceToCheck.equals(pciBoard.getPciDevice())
						&& pciBoard.getBoardStatus() == Board.BoardStatus.INIT_OK)
					return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return the number of IOUsers for which the binding is failed
	 */
	public int doIOBinding(List<IOUser> listIOUser) {
		logger.info("Binding IO users to boards'es channels...");
		int failedBindings = 0;
		if (listIOUser == null)
			throw new IllegalArgumentException("Null argument not allowed");
		for (IOUser ioUser : listIOUser) {
			BoardBindInfo bbi = ioUser.getBoardBindInfo();
			if (bbi == null)
				continue;
			logger.info("Binding " + ioUser.getBindLabel());
			Board board = findBoard(bbi.getBoardId());
			if (board == null) {
				logger.error("No board found for binding: " + bbi);
				failedBindings++;
				continue;
			}
			if (board.getBoardStatus() != BoardStatus.INIT_OK) {
				logger.error("Board not initialized for binding: " + bbi);
				failedBindings++;
				continue;
			}
			Subdevice subDev = board.getSubdevice(bbi.getSubDevice());
			if (subDev == null) {
				logger.error("No subdevice found for binding: " + bbi);
				failedBindings++;
				continue;
			}
			Channel channel = subDev.getChannel(bbi.getChannel());
			if (channel == null) {
				logger.error("No channel found for binding: " + bbi);
				failedBindings++;
				continue;
			}
			try {
				channel.bindIOUser(ioUser);
			} catch (BoardsException ex) {
				logger.error("Bind failed for binding: " + bbi, ex);
				failedBindings++;
				continue;
			}
		}
		return failedBindings;
	}

	public void initPCIBoard(PCIBoard board) throws BoardsException {
		logger.info("Initializing PCI board: " + board + ", PCI info = "
				+ board.getPciDevice());
		PCIDevice dev = board.getPciDevice();
		PCIBoardDescriptor bd = boardDescriptors.get(dev.getVendorId(),
				dev.getDeviceId());
		if (bd == null) {
			String brandModel = board.getBrand() + " " + board.getModel();
			logger.error("No board descriptor found for board: " + brandModel);
			throw new BoardsException("board_descriptor_not_found", brandModel);
		}
		for (BoardLibItf bi : boardLibItfs)
			if (bi.isSupported(board))
				bi.initBoard(board, bd);
		logger.info("PCI board: " + board + " succesfully initialized");
	}

	public void initBoard(Board board) throws BoardsException {
		String boardType = board.getClass().getSimpleName();
		logger.info("Initializing " + boardType + ": " + board + ", "
				+ board.printDriverParams());
		BoardDescriptor bd = boardDescriptors.get(board.getBrand(),
				board.getModel());
		if (bd == null) {
			String brandModel = board.getBrand() + " " + board.getModel();
			logger.error("No board descriptor found for board: " + brandModel);
			throw new BoardsException("board_descriptor_not_found", brandModel);
		}
		for (BoardLibItf bi : boardLibItfs)
			if (bi.isSupported(board))
				bi.initBoard(board, bd);
		logger.info(boardType + ": " + board + " succesfully initialized");
	}

	public void dismissBoard(Board board) throws BoardsException {
		logger.info("Dismissing board: " + board);
		for (BoardLibItf bi : boardLibItfs)
			if (bi.isSupported(board))
				bi.dismissBoard(board);
		logger.info("Board: " + board + " succesfully dismissed");
	}

	public List<BoardLibItf> getBoardLibItfs() {
		return boardLibItfs;
	}

	public List<ExternalAcqItf> getExternalAcqItfs() {
		List<ExternalAcqItf> tmpList = new ArrayList<ExternalAcqItf>();
		for (BoardLibItf itf : boardLibItfs)
			if (itf instanceof ExternalAcqItf)
				tmpList.add((ExternalAcqItf) itf);
		return tmpList;
	}

	public Map<Board, ExternalAcqItf> getMapAcquisitionItfs(
			List<Board> listBoard) {
		if (listBoard == null)
			throw new IllegalArgumentException("Null arguments not allowed");
		Map<Board, ExternalAcqItf> tmpMap = new HashMap<Board, ExternalAcqItf>();
		for (BoardLibItf itf : boardLibItfs)
			if (itf instanceof ExternalAcqItf)
				for (Board board : listBoard)
					if (itf.isSupported(board))
						tmpMap.put(board, (ExternalAcqItf) itf);
		return tmpMap;
	}

	public Map<String, AcqStats> getAcqStats() {
		Map<String, AcqStats> map = new HashMap<String, AcqStats>();
		for (Board board : boards) {
			Object dh = board.getDriverHandle();
			if (dh instanceof AcqStatsProvider)
				map.put(dh.toString(), ((AcqStatsProvider) dh).getAcqStats());
		}
		return map;
	}

	private List<PCIDevice> listPciDevices() {
		logger.info("Scanning PCI bus for devices ...");
		List<PCIDevice> listDevice = new ArrayList<PCIDevice>();
		ProcessBuilder pb = new ProcessBuilder("lspci", "-mn");
		Process p = null;
		BufferedReader br = null;
		boolean unparsableLinesFound = false;
		try {
			p = pb.start();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				PCIDevice pciDevice = parsePciRecord(line);
				if (pciDevice != null)
					listDevice.add(pciDevice);
				else {
					logger.warn("Unparsable line: " + line);
					unparsableLinesFound = true;
				}
			}
			logger.info(listDevice.size() + " devices found");
		} catch (IOException e) {
			logger.error("PCI bus scan failed", e);
			return null;
		} finally {
			if (p != null) {
				try {
					p.getOutputStream().close();
				} catch (IOException e) {
				}
				try {
					p.getErrorStream().close();
				} catch (IOException e) {
				}
				try {
					if (br != null)
						br.close();
					else
						p.getInputStream().close();
				} catch (Exception e) {
				}
			}
		}
		if (listDevice.size() == 0 && unparsableLinesFound) {
			logger.error("Unparsable output from PCI scan");
			return null;
		}
		return listDevice;
	}

	private PCIDevice parsePciRecord(String record) {
		// record format from "lspci -mn":
		// slot___ class_ vendor device
		// 00:0b.0 "ff00" "13fe" "1710" -r01 "13fe" "1710"
		if (record == null)
			return null;
		StringTokenizer strTok = new StringTokenizer(record);
		if (strTok.countTokens() < 4)
			return null;
		String slot = strTok.nextToken();
		strTok.nextToken(); // discard device class value
		String vendor = strTok.nextToken();
		String device = strTok.nextToken();

		PCILocation slotId = parseSlot(slot);
		Integer vendorId = parseHexValue(vendor);
		Integer deviceId = parseHexValue(device);
		if (slotId == null || vendorId == null || deviceId == null)
			return null;

		return new PCIDevice(slotId, vendorId, deviceId);
	}

	private PCILocation parseSlot(String strSlot) {
		// format for the slot field:
		// [domain:]bus:device.function
		if (strSlot == null)
			return null;
		strSlot = strSlot.trim();
		StringTokenizer strTok = new StringTokenizer(strSlot, ":");
		int numTokens = strTok.countTokens();
		if (numTokens < 2 || numTokens > 3)
			return null;
		String domain = null;
		if (numTokens == 3)
			domain = strTok.nextToken().trim();
		String bus = strTok.nextToken().trim();
		String deviceAndFunction = strTok.nextToken().trim();
		strTok = new StringTokenizer(deviceAndFunction, ".");
		if (strTok.countTokens() != 2)
			return null;
		String device = strTok.nextToken().trim();
		String function = strTok.nextToken().trim();
		int domainId = 0;
		int busId = 0;
		int deviceId = 0;
		int functionId = 0;
		try {
			if (domain != null)
				domainId = Integer.parseInt(domain, 16);
			busId = Integer.parseInt(bus, 16);
			deviceId = Integer.parseInt(device, 16);
			functionId = Integer.parseInt(function, 16);
		} catch (NumberFormatException e) {
			return null;
		}
		return new PCILocation(domainId, busId, deviceId, functionId);
	}

	private Integer parseHexValue(String strHexValue) {
		// format for the generic hexadecimal value field:
		// "hexvalue"
		if (strHexValue == null)
			return null;
		strHexValue = strHexValue.trim();
		if (strHexValue.length() < 3)
			return null;
		if (!strHexValue.startsWith("\""))
			return null;
		if (!strHexValue.endsWith("\""))
			return null;
		strHexValue = strHexValue.substring(1, strHexValue.length() - 1);
		strHexValue = strHexValue.trim();
		try {
			Integer value = Integer.parseInt(strHexValue, 16);
			return value;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Board findBoard(UUID boardId) {
		if (boardId == null || boards == null)
			return null;
		for (Board b : boards)
			if (b.getId().equals(boardId))
				return b;
		return null;
	}

}
