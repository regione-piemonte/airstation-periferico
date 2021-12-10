/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: defines an interface for the user interface service that
 *                  extends RemoteService and lists all RPC methods
 * Change log:
 *   2008-02-26: initial version
 * ----------------------------------------------------------------------------
 * $Id: PerifericoUIService.java,v 1.132 2015/05/14 17:45:58 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */

package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.data.ElementInfo;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * 
 * Define an interface for the service that extends RemoteService and lists all
 * RPC methods.
 * 
 * @author isabella.vespa@csi.it
 */
public interface PerifericoUIService extends RemoteService {

	public void getConfig(String locale);

	public String[] setStationCfg(Boolean saveForced, String comment,
			String user);

	public String getNickname();

	public String getName();

	public String getLocation();

	public String getAddress();

	public String getCity();

	public String getProvince();

	public String getUserNotes();

	public String getCopIp();

	public String getMaxConnectionRetry();

	public String isOutgoingCallEnabled();

	public String[][] getAlarms() throws SessionExpiredException;

	public String[] getAlarmNamesCfg() throws SessionExpiredException;

	public String[] setNewAlarmId(String alarmNameStr)
			throws SessionExpiredException;

	public boolean deleteAlarm(String id) throws SessionExpiredException;

	public String[] getAlarmDetails(String id) throws SessionExpiredException;

	public String setVerifyAlarmFields(String id, String[] alarmFields)
			throws SessionExpiredException;

	public boolean verifySameAlarmConfig(String id, String[] alarmFields)
			throws SessionExpiredException;

	public String[][] getAnalyzersCfg();

	public String[] getAnalyzerInfo(String id) throws SessionExpiredException;

	public String[][] getAnalyzersTypeCfg();

	public String[] setNewAnalyzer(String type, String brand, String model)
			throws SessionExpiredException;

	public String deleteAnalyzer(String id);

	public boolean verifySameAnalyzerConfig(String id, String[] infoFields,
			String[] faultFields, String[] dataValidFields)
			throws SessionExpiredException;

	public void setVerifyAnalyzerFields(String id, String[] analyzerFields)
			throws SessionExpiredException, UserParamsException;

	public String[][] getBoards();

	public String[] getBoardBrandsCfg();

	public String[] getBoardModelsCfg(String brand);

	public String[][] getDetectedDevice();

	public String[] getPciDevice(String id);

	public String deleteBoard(String id);

	public String unbindBoard(String id);

	public String associateBoard(Integer boardIndex, String deviceStrId);

	public String[] getBindableBoardsIndexes(String deviceStrId);

	public String getBoardStatus(String id);

	public String[][] getBoardChannelInfo(String id);

	public String[][] getDriverParams(String brand, String model, String id);

	public String[] getPCIBoardVersionList(String deviceStrId);

	public String getPCIBoardVersion(String boardId);

	public Boolean verifyLogin(String password);

	public void verifyInsertNewAlarm(String id) throws SessionExpiredException;

	public void setVerifyStationFields(String[] anagraphicInfo)
			throws SessionExpiredException, UserParamsException;

	public String setVerifyConnectionStationField(String[] connectionInfo);

	public boolean verifySameStationConfig(String[] fieldsValue)
			throws SessionExpiredException;

	public String[][] getAnalyzerStatusList();

	public String setVerifyPCIBoardFields(String id, String brand,
			String model, String version, String[][] driverParamsMatrix);

	public void setVerifyBoardFields(String id, String brand, String model,
			String[][] driverParamsMatrix) throws SessionExpiredException,
			UserParamsException;

	public String verifyInsertNewBoard(String id);

	public String setNewBoard(Boolean autoDetectable, String brand,
			String model, String autoDetectStr) throws SessionExpiredException,
			UserParamsException;

	public String[] getAnalyzerFaultInfo(String id)
			throws SessionExpiredException;

	public String[] getAnalyzerDataValidInfo(String id)
			throws SessionExpiredException;

	public String setVerifyAnalyzerFaultAndDataValidFields(String value,
			String[] faultInfo, String[] dataValidInfo)
			throws SessionExpiredException;

	public String[][] setSubdeviceFields(String boardId, String type,
			String deviceId);

	public String[][] getElementsCfg(String id) throws SessionExpiredException;

	public String deleteElement(String analyzerId, String paramId);

	public String[][] getBindableIOUsers(String boardId, String type,
			String deviceId, String channelStr);

	public String unbindChannel(String boardId, String type, String deviceId,
			String channelStr);

	public String bindIOUser(String boardId, String type, String deviceId,
			String channelStr, String ioUserStr);

	public void initBoard(Boolean type, String id)
			throws SessionExpiredException, UserParamsException;

	public String deleteFault(String id) throws SessionExpiredException;

	public String deleteDataValid(String id) throws SessionExpiredException;

	public ElementInfo getElementInfo(String analyzerId, String paramId)
			throws SessionExpiredException;

	public ElementInfo makeNewElement(String analyzerId, String analyzerType)
			throws SessionExpiredException, UserParamsException;

	public String[][] getElementsParam(String analyzerId);

	public String[] deleteAvgPeriod(String analyzerId, String paramId,
			String avgPeriod);

	public String[] setAvgPeriod(String analyzerId, String text, String itemText);

	public boolean verifySamePCIBoardConfig(String id, String brand,
			String model, String version, String[][] driverParamsStr)
			throws SessionExpiredException;

	public boolean verifySameBoardConfig(String id, String brand, String model,
			String[][] driverParamsStr) throws SessionExpiredException;

	public Boolean verifySessionEnded();

	public void setVerifyElementFields(String analyzerId, ElementInfo info)
			throws SessionExpiredException, UserParamsException;

	public String setVerifyElementFields(String analyzerId, String[] info);

	public String[] verifyInsertNewElement(String analyzerId, String paramId);

	public boolean verifySameElementConfig(String analyzerId, ElementInfo info)
			throws SessionExpiredException;

	public boolean verifySameElementConfig(String analyzerId,
			String[] fieldsValue) throws SessionExpiredException;

	public String[][] getStationStatusFields();

	public String[][] getAnalyzersStatusFields();

	public String[][] getRealTimeDataFields(Boolean onlyDeletedAnalyzers);

	public String[][] getHistoryStationStatusFields(String alarmIdStr,
			String startDateStr, String startHourStr, String endDateStr,
			String endHourStr);

	public String[][] getHistoryAnalyzersStatusFields(String analyzerIdStr,
			String startDateStr, String startHourStr, String endDateStr,
			String endHourStr, String evtType);

	public String[] getCurrentDate();

	public String makeCsv();

	public String generateChart(String title, String legend,
			String measureUnit, String[][] fieldsMatrix, boolean withSeconds,
			boolean interpolated);

	public String[][] getHistoryRealTimeDataFields(String analyzerIdStr,
			String elementIdStr, String requestedDateString, String hourStr,
			String minutesStr);

	public String getAcqPeriod(String analyzerIdStr, String elementIdStr);

	public String[] getAvgPeriods(String analyzerIdStr, String elementIdStr);

	public String[][] getHistoryMeansDataFields(String analyzerIdStr,
			String elementIdStr, String periodStr, String chooseType,
			String halfDayStr, String startDateStr, String startHourStr,
			String endDateStr, String endHourStr, String maxDays);

	public String[] createAndGetChartNameOfRealTimeData(String analyzerIdStr,
			String elementIdStr, String requestedDateString,
			String startHourStr, String endHourStr, String title,
			String legend, String measureUnit, boolean autoScale,
			boolean showMinMax);

	public String[] createAndGetChartNameOfMeansData(String analyzerIdStr,
			String elementIdStr, String startDateStr, String startHourStr,
			String endDateStr, String endHourStr, String maxDays,
			String periodStr, String title, String legend, String measureUnit,
			boolean autoScale, boolean showMinMax);

	public void setLocale(String locale);

	public String[] getAnalyzerEvtArray();

	public void unbindTriggerAlarm(String id) throws SessionExpiredException;

	public String[][] getBindableSampleElements();

	public String[] bindTriggerAlarm(String id, String elementId,
			String elementLbl);

	public String[] getPerifericoStatusFields();

	public String getUseGps();

	public String[] getBrand();

	public String[] getModel(String brand);

	public String[] getConversionCoefficient(String analyzerId, String paramId,
			String acquisitionSelectedItemText, String analyzerSelectedItemText);

	public String getTimeOut();

	public String getConnectionType();

	public String[][] getConfigurationPage(String startDate, String endDate,
			Integer limit);

	public String[] loadHistoricConf(String date);

	public String createNewConnectionParam(String type);

	public String[] getListConnectionType();

	public String[] connectToCop(String text, String itemText)
			throws SessionExpiredException, UserParamsException;

	public String[] loadListConf(String stationName, String start, String end,
			Integer limit) throws SessionExpiredException, UserParamsException;

	public String[] readConf(String stationName, String confName)
			throws SessionExpiredException, UserParamsException;

	public String disactiveMaintenance(String id)
			throws SessionExpiredException;

	public Boolean[] activeMaintenance(String id)
			throws SessionExpiredException;

	public String disactiveCalibration(String id)
			throws SessionExpiredException;

	public Boolean[] activeCalibration(String id)
			throws SessionExpiredException;

	public String[] readValueForCalibration(String analyzerId, String value);

	public String[] calculate(String analyzerId, String paramId,
			String expectedValue1, String expectedValue2, String readValue1,
			String readValue2) throws SessionExpiredException,
			UserParamsException;

	public String verifyInsertNewAnalyzer(String analyzerId)
			throws SessionExpiredException;

	public boolean verifyDpaIsActive(String analyzerId, String value)
			throws SessionExpiredException;

	public Boolean isToSave();

	public String getPerifericoVersion();

	public String getStationName();

	public List<String[]> getCommandList(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public String sendCommand(String analyzerId, String[] command)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> getCommandResult(String commandId)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> getAlarmValues(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> getProcessParameterValues(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> getMeasureValues(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> getAlarmList(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public Date getAnalyzerDate(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public Date setAnalyzerDate(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public String getSerialNumber(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public Integer resetAllFaults(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public Boolean isRemoteUISupported(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public String isRemoteGUISupported(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> getKeyList(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> readDisplay(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public String readDisplayImage(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> sendKey(String analyzerId, String key)
			throws SessionExpiredException, DgtAnalyzerException;

	public Boolean isCustomCommandSupported(String analyzerId)
			throws SessionExpiredException, DgtAnalyzerException;

	public List<String> getEquivalentBrands(String brand, String model)
			throws SessionExpiredException;

	public List<String> getEquivalentModels(String newBrand, String brand,
			String model) throws SessionExpiredException;

}
