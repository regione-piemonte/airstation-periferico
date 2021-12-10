/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: base class to represent an acquisition board
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: Board.java,v 1.17 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.Periferico;
import it.csi.periferico.config.BoardsCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base class to represent an acquisition board
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public abstract class Board extends ConfigItem {

	private static final long serialVersionUID = -2378061493691701532L;

	public enum BoardStatus {
		PRE_INIT, DETECTED, NOT_DETECTED, INIT_OK, INIT_FAILED
	}

	private UUID id = UUID.randomUUID();

	private String brand = "";

	private String model = "";

	private List<DriverParam> driverParams = new ArrayList<DriverParam>();

	private BoardStatus boardStatus = BoardStatus.PRE_INIT;

	private List<AISubdevice> listAISubdevice = new ArrayList<AISubdevice>();

	private List<DISubdevice> listDISubdevice = new ArrayList<DISubdevice>();

	private List<DOSubdevice> listDOSubdevice = new ArrayList<DOSubdevice>();

	private List<DIOSubdevice> listDIOSubdevice = new ArrayList<DIOSubdevice>();

	private BoardDescriptor boardDescriptor = null;

	private transient Object driverHandle = null;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getIdAsString() {
		if (id == null)
			return "";
		return id.toString();
	}

	public void setIdAsString(String strId) {
		this.id = UUID.fromString(trim(strId));
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = trim(brand);
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = trim(model);
	}

	public List<AISubdevice> getListAISubdevice() {
		return listAISubdevice;
	}

	public void setListAISubdevice(List<AISubdevice> listAISubdevice) {
		this.listAISubdevice = listAISubdevice;
	}

	public AISubdevice getAISubdevice(int subdevice) {
		if (listAISubdevice == null)
			return null;
		for (AISubdevice aiSub : listAISubdevice)
			if (aiSub.getSubdevice() == subdevice)
				return aiSub;
		return null;
	}

	public List<DISubdevice> getListDISubdevice() {
		return listDISubdevice;
	}

	public void setListDISubdevice(List<DISubdevice> listDISubdevice) {
		this.listDISubdevice = listDISubdevice;
	}

	public DISubdevice getDISubdevice(int subdevice) {
		if (listDISubdevice == null)
			return null;
		for (DISubdevice diSub : listDISubdevice)
			if (diSub.getSubdevice() == subdevice)
				return diSub;
		return null;
	}

	public List<DOSubdevice> getListDOSubdevice() {
		return listDOSubdevice;
	}

	public void setListDOSubdevice(List<DOSubdevice> listDOSubdevice) {
		this.listDOSubdevice = listDOSubdevice;
	}

	public DOSubdevice getDOSubdevice(int subdevice) {
		if (listDOSubdevice == null)
			return null;
		for (DOSubdevice doSub : listDOSubdevice)
			if (doSub.getSubdevice() == subdevice)
				return doSub;
		return null;
	}

	public List<DIOSubdevice> getListDIOSubdevice() {
		return listDIOSubdevice;
	}

	public void setListDIOSubdevice(List<DIOSubdevice> listDIOSubdevice) {
		this.listDIOSubdevice = listDIOSubdevice;
	}

	public DIOSubdevice getDIOSubdevice(int subdevice) {
		if (listDIOSubdevice == null)
			return null;
		for (DIOSubdevice dioSub : listDIOSubdevice)
			if (dioSub.getSubdevice() == subdevice)
				return dioSub;
		return null;
	}

	public Subdevice getSubdevice(int subdevice) {
		Subdevice s = getAISubdevice(subdevice);
		if (s != null)
			return s;
		s = getDISubdevice(subdevice);
		if (s != null)
			return s;
		s = getDIOSubdevice(subdevice);
		if (s != null)
			return s;
		s = getDOSubdevice(subdevice);
		if (s != null)
			return s;
		return null;
	}

	public void setConfig(String brand, String model,
			List<DriverParam> driverParams) throws ConfigException {
		checkBrandModel(brand, model);
		setBrand(brand);
		setModel(model);
		setAndCheckDriverParams(driverParams);
	}

	public boolean isSameConfig(String brand, String model,
			List<DriverParam> driverParams) {
		return this.brand.equals(trim(brand)) && this.model.equals(trim(model))
				&& this.driverParams.equals(driverParams);
	}

	@Override
	public void checkConfig() throws ConfigException {
		checkBrandModel(brand, model);
	}

	private void checkBrandModel(String brand, String model)
			throws ConfigException {
		BoardsCfg bc = Periferico.getInstance().getBoardsCfg();
		BoardDescriptors bd = bc.getBoardDescriptors();
		BoardDescriptor descriptor = bd.get(brand, model);
		if (descriptor == null)
			throw new ConfigException("no_board_descriptor", brand, model);
		boardDescriptor = descriptor;
	}

	public BoardStatus getBoardStatus() {
		return boardStatus;
	}

	public void setBoardStatus(BoardStatus boardStatus) {
		this.boardStatus = boardStatus;
	}

	public List<DriverParam> getDriverParams() {
		return driverParams;
	}

	public void setDriverParams(List<DriverParam> driverParams) {
		this.driverParams = driverParams;
	}

	public void setAndCheckDriverParams(List<DriverParam> driverParams)
			throws ConfigException {
		if (driverParams != null)
			for (DriverParam dp : driverParams)
				checkDriverParam(dp);
		this.driverParams = driverParams;
	}

	private void checkDriverParam(DriverParam param) throws ConfigException {
		if (boardDescriptor == null)
			return;
		DriverParamDescriptor paramDescriptor = boardDescriptor
				.getDriverParamDescriptor(param.getName());
		DriverParamDescriptor.Type type = paramDescriptor.getType();

		if (type == DriverParamDescriptor.Type.DECIMAL
				|| type == DriverParamDescriptor.Type.IRQ
				|| type == DriverParamDescriptor.Type.DMA0
				|| type == DriverParamDescriptor.Type.DMA1) {
			try {
				Integer.parseInt(param.getValue());
			} catch (NumberFormatException nfe) {
				throw new ConfigException("param_not_int", param.getName());
			}
		} else if (type == DriverParamDescriptor.Type.HEXADECIMAL
				|| type == DriverParamDescriptor.Type.IOBASE) {
			String strVal = param.getValue();
			if (!strVal.startsWith("0x") && !strVal.startsWith("0X"))
				throw new ConfigException("param_not_hex", param.getName());
			strVal = strVal.substring(2);
			try {
				int value = Integer.parseInt(strVal, 16);
				if (type == DriverParamDescriptor.Type.IOBASE && value <= 0)
					throw new ConfigException("io_base_should_be_gt_0",
							param.getName());
			} catch (NumberFormatException nfe) {
				throw new ConfigException("param_not_hex", param.getName());
			}
		}
	}

	public DriverParam getDriverParamFromName(String driverParamName) {
		DriverParam dParam = null;
		for (DriverParam driverParam : driverParams) {
			if (driverParam.getName().equals(driverParamName))
				dParam = driverParam;
		}
		return dParam;
	}

	public String getSupportingLibName() {
		return boardDescriptor == null ? null : boardDescriptor.getLibName();
	}

	@Override
	public String toString() {
		return brand + " " + model;
	}

	public Object getDriverHandle() {
		return driverHandle;
	}

	public void setDriverHandle(Object driverHandle) {
		this.driverHandle = driverHandle;
	}

	public String printDriverParams() {
		StringBuilder sb = new StringBuilder();
		for (DriverParam driverParam : driverParams) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(driverParam.getName()).append(" = ")
					.append(driverParam.getValue());
		}
		return sb.toString();
	}

}
