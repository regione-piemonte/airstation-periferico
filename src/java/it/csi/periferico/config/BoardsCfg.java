/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: configuration for Comedi library
// Change log:
//   2008-07-31: initial version
// ----------------------------------------------------------------------------
// $Id: ComediCfg.java,v 1.3 2009/10/26 16:55:54 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config;

import it.csi.periferico.boards.BoardDescriptors;
import it.csi.periferico.boards.adam.AdamModuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for Comedi library
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class BoardsCfg {

	private List<String> comediOptions = new ArrayList<String>();
	private BoardDescriptors boardDescriptors = new BoardDescriptors();
	private List<AdamModuleInfo> adamBoardsInfo = new ArrayList<AdamModuleInfo>();

	public List<String> getComediOptions() {
		return comediOptions;
	}

	public void setComediOptions(List<String> comediOptions) {
		this.comediOptions = comediOptions;
	}

	public BoardDescriptors getBoardDescriptors() {
		return boardDescriptors;
	}

	public void setBoardDescriptors(BoardDescriptors boardDescriptors) {
		this.boardDescriptors = boardDescriptors;
	}

	public List<AdamModuleInfo> getAdamBoardsInfo() {
		return adamBoardsInfo;
	}

	public void setAdamBoardsInfo(List<AdamModuleInfo> adamBoardsInfo) {
		this.adamBoardsInfo = adamBoardsInfo;
	}

}
