/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: specific acquisition boards management for user interface
// Change log:
//   2008-04-10: initial version
// ----------------------------------------------------------------------------
// $Id: BoardManagementForUI.java,v 1.22 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.Periferico;
import it.csi.periferico.boards.Board.BoardStatus;
import it.csi.periferico.config.common.ConfigException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Specific acquisition boards management for user interface
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class BoardManagementForUI {

	private BoardManager boardManager;

	private BoardDescriptors boardDescriptors;
	
	public BoardManagementForUI(BoardDescriptors boardDescriptors) {
		this.boardDescriptors = boardDescriptors;
		this.boardManager = null;
	}

	public void setBoardManager(BoardManager boardManager) {
		if (boardManager == null)
			throw new IllegalArgumentException("BoardManager must not be null");
		this.boardManager = boardManager;
	}

	public List<PCIDevice> getNotConfiguredDevices(BoardList boardList)
			throws BoardsException {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (boardManager == null)
			throw new IllegalStateException("BoardManager not set yet");
		List<PCIDevice> listDetectedPCIAcqDevices = boardManager
				.detectPCIAcqDevices();
		if (listDetectedPCIAcqDevices == null) {
			throw new BoardsException("error_listing_pci_devices");
		}
		for (Board board : boardList.getBoards()) {
			if (board instanceof PCIBoard) {
				PCIBoard pciBoard = (PCIBoard) board;
				PCIDevice boardDevice = pciBoard.getPciDevice();
				Iterator<PCIDevice> itDetectedDevices = listDetectedPCIAcqDevices
						.iterator();
				while (itDetectedDevices.hasNext()) {
					if (boardDevice.equals(itDetectedDevices.next())) {
						itDetectedDevices.remove();
						break;
					}
				}
			}
		}
		return listDetectedPCIAcqDevices;
	}

	public PCIBoard newPCIBoard(BoardList boardList, PCIDevice device)
			throws BoardsException {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (device == null)
			throw new IllegalArgumentException("PCIDevice must not be null");
		checkForDeviceAlreadyBound(boardList, device);
		checkForExistentDevice(device);
		PCIBoard board = new PCIBoard(device);
		PCIBoardDescriptor pbd = boardDescriptors.get(device.getVendorId(),
				device.getDeviceId());
		if (pbd == null)
			throw new BoardsException("board_descriptor_pci_not_found",
					device.toString());
		board.setBrand(pbd.getBrand());
		board.setModel(pbd.getModel());
		board.setBoardStatus(BoardStatus.DETECTED);
		boardList.setNewBoard(board);
		return board;
	}

	public PCIBoardDescriptor getPCIBoardDescriptor(PCIDevice device) {
		if (device == null)
			throw new IllegalArgumentException("PCIDevice must not be null");
		return boardDescriptors.get(device.getVendorId(), device.getDeviceId());
	}

	public void initPCIBoard(BoardList boardList, PCIBoard board)
			throws BoardsException {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (board == null)
			throw new IllegalArgumentException("PCIBoard must not be null");
		if (boardManager == null)
			throw new IllegalStateException("BoardManager not set yet");
		checkForBoardUsedInRunningConf(board);
		synchronized (Periferico.getInstance()) {
			boardManager.initPCIBoard(board);
			boardManager.dismissBoard(board);
		}
	}

	public Board newBoard(BoardList boardList, String brand, String model)
			throws BoardsException {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (brand == null)
			throw new IllegalArgumentException("Board's brand must not be null");
		if (model == null)
			throw new IllegalArgumentException("Board's model must not be null");
		BoardDescriptor pbd = boardDescriptors.get(brand, model);
		if (pbd == null)
			throw new BoardsException("board_not_found_brand_model", brand,
					model);
		Board board = pbd.newBoard();
		boardList.setNewBoard(board);
		return board;
	}

	public void initBoard(BoardList boardList, Board board)
			throws BoardsException {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (board == null)
			throw new IllegalArgumentException("Board must not be null");
		if (boardManager == null)
			throw new IllegalStateException("BoardManager not set yet");
		synchronized (Periferico.getInstance()) {
			boardManager.initBoard(board);
			boardManager.dismissBoard(board);
		}
	}

	public boolean deleteBoard(BoardList boardList, String boardId,
			List<IOUser> listIOUser) {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (listIOUser == null)
			throw new IllegalArgumentException("IOUser list must not be null");
		Board board = boardList.getBoard(boardId);
		if (board == null)
			return false;
		for (IOUser ioUser : listIOUser) {
			BoardBindInfo bbi = ioUser.getBoardBindInfo();
			if (bbi != null && bbi.getBoardId().equals(board.getId()))
				ioUser.setBoardBindInfo(null);
		}
		return boardList.deleteBoard(boardId);
	}

	public boolean unbindBoard(BoardList boardList, String boardId,
			List<IOUser> listIOUser) {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (listIOUser == null)
			throw new IllegalArgumentException("IOUser list must not be null");
		Board board = boardList.getBoard(boardId);
		if (board == null)
			return false;
		if (!(board instanceof PCIBoard))
			throw new IllegalArgumentException("Only PCI boards can be unbound");
		PCIBoard pciBoard = (PCIBoard) board;
		for (IOUser ioUser : listIOUser) {
			BoardBindInfo bbi = ioUser.getBoardBindInfo();
			if (bbi == null)
				continue;
			if (!board.getId().equals(bbi.getBoardId()))
				continue;
			Subdevice subDev = board.getSubdevice(bbi.getSubDevice());
			if (subDev == null)
				continue;
			Channel channel = subDev.getChannel(bbi.getChannel());
			if (channel == null)
				continue;
			ioUser.setIOProvider(null);
			channel.setIOUser(null);
		}
		PCIDevice device = pciBoard.getPciDevice();
		PCIDevice dummyDevice = new PCIDevice(new PCILocation(),
				device.getVendorId(), device.getDeviceId());
		pciBoard.setPciDevice(dummyDevice);
		board.setBoardStatus(Board.BoardStatus.NOT_DETECTED);
		return true;
	}

	public void bindPCIBoardToDevice(BoardList boardList, Integer boardIndex,
			PCIDevice device, List<IOUser> listIOUser) throws BoardsException {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (boardIndex == null)
			throw new IllegalArgumentException("Board index must not be null");
		if (boardIndex < 0 || boardIndex >= boardList.getBoards().size())
			throw new IllegalArgumentException("Board index out of range");
		if (device == null)
			throw new IllegalArgumentException("PCIDevice must not be null");
		if (boardManager == null)
			throw new IllegalStateException("BoardManager not set yet");
		checkForDeviceAlreadyBound(boardList, device);
		checkForExistentDevice(device);
		Board board = boardList.getBoards().get(boardIndex);
		if (!(board instanceof PCIBoard))
			throw new IllegalArgumentException(
					"Cannot bind ISA Board to PCI Device");
		if (board.getBoardStatus() != Board.BoardStatus.NOT_DETECTED)
			throw new BoardsException("board_not_bound_device");
		PCIBoard pciBoard = (PCIBoard) board;
		PCIDevice boardDevice = pciBoard.getPciDevice();
		if (!boardDevice.getVendorId().equals(device.getVendorId())
				|| !boardDevice.getDeviceId().equals(device.getDeviceId())) {
			throw new BoardsException("bound_same_type_device");
		}
		pciBoard.setPciDevice(device);
		checkForBoardUsedInRunningConf(pciBoard);
		synchronized (Periferico.getInstance()) {
			boardManager.initPCIBoard(pciBoard);
			boardManager.dismissBoard(board);
		}
		// Do IOBinding for this board only at UI level, for the configuration
		// belonging to the user's session
		if (listIOUser == null)
			return;
		if (board.getBoardStatus() != BoardStatus.INIT_OK)
			return;
		for (IOUser ioUser : listIOUser) {
			BoardBindInfo bbi = ioUser.getBoardBindInfo();
			if (bbi == null)
				continue;
			if (!board.getId().equals(bbi.getBoardId()))
				continue;
			Subdevice subDev = board.getSubdevice(bbi.getSubDevice());
			if (subDev == null)
				continue;
			Channel channel = subDev.getChannel(bbi.getChannel());
			if (channel == null)
				continue;
			try {
				channel.bindIOUser(ioUser);
			} catch (BoardsException ex) {
				continue;
			}
		}
	}

	public List<Integer> getBindableBoardIndexes(BoardList boardList,
			PCIDevice device) throws BoardsException {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (device == null)
			throw new IllegalArgumentException("PCIDevice must not be null");
		if (device.getVendorId() == null || device.getDeviceId() == null)
			throw new IllegalArgumentException(
					"PCIDevice vendorId or deviceId " + "must not be null");
		List<Integer> bindableBoardIndexes = new ArrayList<Integer>();
		for (int i = 0; i < boardList.getBoards().size(); i++) {
			Board b = boardList.getBoards().get(i);
			if (b instanceof PCIBoard) {
				PCIBoard pciBoard = (PCIBoard) b;
				PCIDevice boardDevice = pciBoard.getPciDevice();
				if (device.getVendorId().equals(boardDevice.getVendorId())
						&& device.getDeviceId().equals(
								boardDevice.getDeviceId())
						&& pciBoard.getBoardStatus() == Board.BoardStatus.NOT_DETECTED) {
					bindableBoardIndexes.add(new Integer(i));
				}
			}
		}
		return bindableBoardIndexes;
	}

	public IOUser getBindedIOUser(IOProvider ioProvider) {
		if (ioProvider == null)
			throw new IllegalArgumentException("IOProvider must not be null");
		return ioProvider.getBindedIOUser();
	}

	public List<IOUser> getBindableIOUsers(IOProvider ioProvider,
			List<IOUser> listIOUser) {
		if (listIOUser == null)
			throw new IllegalArgumentException("IOUser list must not be null");
		if (ioProvider == null)
			throw new IllegalArgumentException("IOProvider must not be null");
		List<IOUser> listBindableIOUser = new ArrayList<IOUser>();
		if (ioProvider.isBinded())
			return listBindableIOUser;
		for (IOUser ioUser : listIOUser) {
			if (ioUser.getBoardBindInfo() != null)
				continue;
			if (!isIOCompatible(ioProvider, ioUser))
				continue;
			listBindableIOUser.add(ioUser);
		}
		return listBindableIOUser;
	}

	public void bindIOUser(IOProvider ioProvider, IOUser ioUser) {
		if (ioProvider == null)
			throw new IllegalArgumentException("IOProvider must not be null");
		if (ioUser == null)
			throw new IllegalArgumentException("IOUser must not be null");
		if (ioProvider.isBinded())
			throw new IllegalStateException("IOProvider already binded");
		if (ioUser.getBoardBindInfo() != null)
			throw new IllegalStateException("IOUser already binded");
		if (!isIOCompatible(ioProvider, ioUser))
			throw new IllegalStateException(
					"IOProvider and IOUser are not compatible");
		try {
			ioProvider.bindIOUser(ioUser);
		} catch (BoardsException ex) {
			throw new IllegalStateException("Bind failed", ex);
		}
	}

	public void unbind(IOProvider ioProvider) {
		if (ioProvider == null)
			throw new IllegalArgumentException("IOProvider must not be null");
		ioProvider.unbind();
	}

	public IOUser findIOUser(List<IOUser> listIOUser, String bindIdentifier) {
		if (listIOUser == null || bindIdentifier == null)
			return null;
		for (IOUser ioUser : listIOUser)
			if (bindIdentifier.equals(ioUser.getBindIdentifier()))
				return ioUser;
		return null;
	}

	public String getBoardBindLabel(BoardList boardList, BoardBindInfo bbi) {
		if (boardList == null)
			throw new IllegalArgumentException("BoardList must not be null");
		if (bbi == null)
			return "";
		int index = 0;
		for (Board board : boardList.getBoards()) {
			index++;
			if (board.getId().equals(bbi.getBoardId())) {
				return board.getBrand() + " " + board.getModel() + " (" + index
						+ "), " + bbi.getSubDevice() + ", " + bbi.getChannel();
			}
		}
		return "";
	}

	private void checkForDeviceAlreadyBound(BoardList boardList,
			PCIDevice device) throws BoardsException {
		for (Board b : boardList.getBoards()) {
			if (b instanceof PCIBoard) {
				PCIBoard pciBoard = (PCIBoard) b;
				PCIDevice boardDevice = pciBoard.getPciDevice();
				if (device.equals(boardDevice)) {
					throw new BoardsException("device_already_bound",
							device.toString());
				}
			}
		}
	}

	private void checkForBoardUsedInRunningConf(PCIBoard board)
			throws BoardsException {
		if (board == null)
			throw new IllegalArgumentException("PCIBoard must not be null");
		if (board.getPciDevice() == null)
			throw new IllegalArgumentException(
					"Board's device must not be null");
		if (boardManager == null)
			throw new IllegalStateException("BoardManager not set yet");
		if (boardManager.isDeviceInitialized(board.getPciDevice()))
			throw new BoardsException("cannot_initialize_pci_board",
					board.toString());
	}

	private void checkForExistentDevice(PCIDevice device)
			throws BoardsException {
		if (boardManager == null)
			throw new IllegalStateException("BoardManager not set yet");
		if (!boardManager.isDeviceExistent(device))
			throw new BoardsException("pci_device_not_exist", device.toString());
	}

	private boolean isIOCompatible(IOProvider ioProvider, IOUser ioUser) {
		if (ioProvider instanceof AnalogInput) {
			if (!(ioUser instanceof AIUser))
				return false;
			return isAICompatible((AnalogInput) ioProvider, (AIUser) ioUser);
		}
		if (ioProvider instanceof DigitalInput)
			return ioUser instanceof DIUser;
		if (ioProvider instanceof DigitalIO)
			return (ioUser instanceof DIUser) || (ioUser instanceof DOUser);
		if (ioProvider instanceof DigitalOutput)
			return ioUser instanceof DOUser;
		return false;
	}

	private boolean isAICompatible(AnalogInput ai, AIUser aiUser) {
		Range range = null;
		try {
			range = new Range(aiUser.getMinVoltage(), aiUser.getMaxVoltage(),
					aiUser.getMinRangeExtension(),
					aiUser.getMaxRangeExtension());
		} catch (ConfigException e) {
		}
		if (ai.findCompatibleRange(range) == null)
			return false;
		if (aiUser.isDifferentialModeNeeded()) {
			if (!ai.isDifferentialModeSupported())
				return false;
			if (!ai.isDifferentialModeAvailable())
				return false;
		}
		return true;
	}

}
