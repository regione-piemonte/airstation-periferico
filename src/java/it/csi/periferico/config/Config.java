/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: the configuration of the measurement station
// Change log:
//   2008-01-21: initial version
// ----------------------------------------------------------------------------
// $Id: Config.java,v 1.18 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config;

import it.csi.periferico.boards.Board;
import it.csi.periferico.boards.BoardBindInfo;
import it.csi.periferico.boards.BoardList;
import it.csi.periferico.boards.IOUser;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;
import it.csi.periferico.core.Alarm;
import it.csi.periferico.core.Analyzer;
import it.csi.periferico.core.Container;
import it.csi.periferico.core.ContainerAlarm;
import it.csi.periferico.core.Station;
import it.csi.periferico.core.TriggerAlarm;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The configuration of the measurement station
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Config extends ConfigItem {

	private static final long serialVersionUID = 2326442016279116709L;

	public enum Status {
		MISSING, PARSE_ERROR, UNCHECKED, CHECK_ERROR, OK
	}

	private UUID id;

	private Station station;

	private BoardList boardList;

	private Date date;

	private String author = "";

	private String comment = "";

	private Status status;

	private boolean historic = false;

	public Config() {
		this(Status.UNCHECKED);
	}

	public Config(Status status) {
		id = null;
		station = new Station();
		boardList = new BoardList();
		this.status = status;
	}

	public UUID getId() {
		return id;
	}

	void setNewId() {
		id = UUID.randomUUID();
	}

	public String getIdAsString() {
		if (id == null)
			return "";
		return id.toString();
	}

	public void setIdAsString(String strId) {
		this.id = UUID.fromString(trim(strId));
	}

	public Station getStation() {
		return station;
	}

	public void setStation(Station station) {
		this.station = station;
	}

	public BoardList getBoardList() {
		return boardList;
	}

	public void setBoardList(BoardList boardList) {
		this.boardList = boardList;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	void setNewDate() {
		this.date = new Date();
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = trim(author);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = trim(comment);
	}

	public Status getStatus() {
		return status;
	}

	public boolean isHistoric() {
		return historic;
	}

	void setHistoric(boolean historic) {
		this.historic = historic;
	}

	@Override
	public void checkConfig() throws ConfigException {
		Status tmpStatus = status;
		try {
			tmpStatus = Status.CHECK_ERROR;
			station.checkConfig();
			boardList.checkConfig();
			tmpStatus = Status.OK;
		} finally {
			status = tmpStatus;
		}
	}

	@Override
	public void initConfig() {
		station.initConfig();
	}

	public static String statusToString(Config.Status status) {
		if (status == Status.MISSING)
			return "missing";
		else if (status == Status.PARSE_ERROR)
			return "corrupted";
		else if (status == Status.UNCHECKED)
			return "not checked";
		else if (status == Status.CHECK_ERROR)
			return "with errors";
		else if (status == Status.OK)
			return "ok";
		return "in unknown status";
	}

	public void changeAllUUIDs() {
		setNewId();
		setNewDate();
		setAuthor("Periferico application");
		setComment("All UUIDs replaced");
		Map<UUID, UUID> mapBoardUUIDConversion = new HashMap<UUID, UUID>();
		Map<UUID, UUID> mapAnalyzerUUIDConversion = new HashMap<UUID, UUID>();
		if (boardList != null) {
			List<Board> listBoards = boardList.getBoards();
			if (listBoards != null) {
				for (Board board : listBoards) {
					UUID oldUUID = board.getId();
					UUID newUUID = UUID.randomUUID();
					board.setId(newUUID);
					mapBoardUUIDConversion.put(oldUUID, newUUID);
				}
			}
		}
		if (station != null) {
			station.setId(UUID.randomUUID());
			List<Analyzer> listAnalyzers = station.getListAnalyzer();
			if (listAnalyzers != null) {
				for (Analyzer analyzer : listAnalyzers) {
					UUID oldUUID = analyzer.getId();
					UUID newUUID = UUID.randomUUID();
					analyzer.setId(newUUID);
					mapAnalyzerUUIDConversion.put(oldUUID, newUUID);
				}
			}
			Container container = station.getContainer();
			if (container != null) {
				List<ContainerAlarm> listAlarms = container.getListAlarm();
				if (listAlarms != null) {
					for (ContainerAlarm contAlarm : listAlarms) {
						contAlarm.setId(UUID.randomUUID());
						Alarm alarm = contAlarm.getAlarm();
						if (alarm instanceof TriggerAlarm) {
							TriggerAlarm ta = (TriggerAlarm) alarm;
							UUID newAnalyzerId = mapAnalyzerUUIDConversion
									.get(ta.getObservedAnalyzerId());
							ta.setObservedAnalyzerId(newAnalyzerId);
						}
					}
				}
			}
			List<IOUser> listIOUsers = station.getListIOUser();
			for (IOUser ioUser : listIOUsers) {
				BoardBindInfo bbi = ioUser.getBoardBindInfo();
				if (bbi == null)
					continue;
				UUID newBoardId = mapBoardUUIDConversion.get(bbi.getBoardId());
				if (newBoardId != null)
					bbi.setBoardId(newBoardId);
			}
		}
	}

}
