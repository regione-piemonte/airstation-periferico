/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: defines the asynchronous interface to the user interface
 *                  service to be called from the client-side code
 * Change log:
 *   2008-02-26: initial version
 * ----------------------------------------------------------------------------
 * $Id: PerifericoUIServiceAsync.java,v 1.127 2015/05/14 17:45:58 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */

package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.data.ElementInfo;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Define asynchronous interface to the service to be called from the
 * client-side code.
 * 
 * @author isabella.vespa@csi.it
 */
public interface PerifericoUIServiceAsync {

	public void getConfig(String locale, AsyncCallback<Object> callback);

	public void setStationCfg(Boolean saveForced, String comment, String user,
			AsyncCallback<String[]> callback);

	public void getNickname(AsyncCallback<String> callback);

	public void getName(AsyncCallback<String> callback);

	public void getLocation(AsyncCallback<String> callback);

	public void getAddress(AsyncCallback<String> callback);

	public void getCity(AsyncCallback<String> callback);

	public void getProvince(AsyncCallback<String> callback);

	public void getUserNotes(AsyncCallback<String> callback);

	public void getCopIp(AsyncCallback<String> callback);

	public void getMaxConnectionRetry(AsyncCallback<String> callback);

	public void isOutgoingCallEnabled(AsyncCallback<String> callback);

	public void getAlarms(AsyncCallback<String[][]> callback);

	public void getAlarmNamesCfg(AsyncCallback<String[]> callback);

	public void setNewAlarmId(String alarmNameStr,
			AsyncCallback<String[]> callback);

	public void deleteAlarm(String id, AsyncCallback<Boolean> callback);

	public void getAlarmDetails(String id, AsyncCallback<String[]> callback);

	public void setVerifyAlarmFields(String id, String[] alarmFields,
			AsyncCallback<String> callback);

	public void verifySameAlarmConfig(String id, String[] alarmFields,
			AsyncCallback<Boolean> callback);

	public void getAnalyzersCfg(AsyncCallback<String[][]> callback);

	public void getAnalyzerInfo(String id, AsyncCallback<String[]> callback);

	public void getAnalyzersTypeCfg(AsyncCallback<String[][]> callback);

	public void setNewAnalyzer(String type, String brand, String model,
			AsyncCallback<String[]> callback);

	public void deleteAnalyzer(String id, AsyncCallback<String> callback);

	public void verifySameAnalyzerConfig(String id, String[] infoFields,
			String[] faultFields, String[] dataValidFields,
			AsyncCallback<Boolean> callback);

	public void setVerifyAnalyzerFields(String id, String[] analyzerFields,
			AsyncCallback<Object> callback);

	public void getBoards(AsyncCallback<String[][]> callback);

	public void getBoardBrandsCfg(AsyncCallback<String[]> callback);

	public void getBoardModelsCfg(String brand, AsyncCallback<String[]> callback);

	public void getDetectedDevice(AsyncCallback<String[][]> callback);

	public void getPciDevice(String id, AsyncCallback<String[]> callback);

	public void deleteBoard(String id, AsyncCallback<String> callback);

	public void unbindBoard(String id, AsyncCallback<String> callback);

	public void associateBoard(Integer boardIndex, String deviceStrId,
			AsyncCallback<String> callback);

	public void getBindableBoardsIndexes(String deviceStrId,
			AsyncCallback<String[]> callback);

	public void getBoardStatus(String id, AsyncCallback<String> callback);

	public void getBoardChannelInfo(String id,
			AsyncCallback<String[][]> callback);

	public void getDriverParams(String brand, String model, String id,
			AsyncCallback<String[][]> callback);

	public void getPCIBoardVersionList(String deviceStrId,
			AsyncCallback<String[]> callback);

	public void getPCIBoardVersion(String boardId,
			AsyncCallback<String> callback);

	public void verifyLogin(String password, AsyncCallback<Boolean> callback);

	public void verifyInsertNewAlarm(String id, AsyncCallback<Object> callback);

	public void setVerifyStationFields(String[] anagraphicInfo,
			AsyncCallback<Object> callback);

	public void setVerifyConnectionStationField(String[] connectionInfo,
			AsyncCallback<String> callback);

	public void verifySameStationConfig(String[] fieldsValue,
			AsyncCallback<Boolean> callback);

	public void getAnalyzerStatusList(AsyncCallback<String[][]> callback);

	public void setVerifyPCIBoardFields(String id, String brand, String model,
			String version, String[][] driverParamsMatrix,
			AsyncCallback<String> callback);

	public void setVerifyBoardFields(String id, String brand, String model,
			String[][] driverParamsMatrix, AsyncCallback<Object> callback);

	public void verifyInsertNewBoard(String id, AsyncCallback<String> callback);

	public void setNewBoard(Boolean autoDetectable, String brand, String model,
			String autoDetectStr, AsyncCallback<String> callback);

	public void getAnalyzerFaultInfo(String id, AsyncCallback<String[]> callback);

	public void getAnalyzerDataValidInfo(String id,
			AsyncCallback<String[]> callback);

	public void setVerifyAnalyzerFaultAndDataValidFields(String value,
			String[] faultInfo, String[] dataValidInfo,
			AsyncCallback<String> callback);

	public void setSubdeviceFields(String boardId, String type,
			String deviceId, AsyncCallback<String[][]> callback);

	public void getElementsCfg(String id, AsyncCallback<String[][]> callback);

	public void deleteElement(String analyzerId, String paramId,
			AsyncCallback<String> callback);

	public void getBindableIOUsers(String boardId, String type,
			String deviceId, String channelStr,
			AsyncCallback<String[][]> callback);

	public void unbindChannel(String boardId, String type, String deviceId,
			String channelStr, AsyncCallback<String> callback);

	public void bindIOUser(String boardId, String type, String deviceId,
			String channelStr, String ioUserStr, AsyncCallback<String> callback);

	public void initBoard(Boolean type, String id,
			AsyncCallback<Object> callback);

	public void deleteFault(String id, AsyncCallback<String> callback);

	public void deleteDataValid(String id, AsyncCallback<String> callback);

	public void getElementInfo(String analyzerId, String paramId,
			AsyncCallback<ElementInfo> callback);

	public void makeNewElement(String analyzerId, String analyzerType,
			AsyncCallback<ElementInfo> callback);

	public void getElementsParam(String analyzerId,
			AsyncCallback<String[][]> callback);

	public void deleteAvgPeriod(String analyzerId, String paramId,
			String avgPeriod, AsyncCallback<String[]> callback);

	public void setAvgPeriod(String analyzerId, String text, String itemText,
			AsyncCallback<String[]> callback);

	public void verifySamePCIBoardConfig(String id, String brand, String model,
			String version, String[][] driverParamsStr,
			AsyncCallback<Boolean> callback);

	public void verifySameBoardConfig(String id, String brand, String model,
			String[][] driverParamsStr, AsyncCallback<Boolean> callback);

	public void verifySessionEnded(AsyncCallback<Boolean> callback);

	public void setVerifyElementFields(String analyzerId, ElementInfo info,
			AsyncCallback<Object> callback);

	public void setVerifyElementFields(String analyzerId, String[] info,
			AsyncCallback<String> callback);

	public void verifyInsertNewElement(String analyzerId, String paramId,
			AsyncCallback<String[]> callback);

	public void verifySameElementConfig(String analyzerId, ElementInfo info,
			AsyncCallback<Boolean> callback);

	public void verifySameElementConfig(String analyzerId,
			String[] fieldsValue, AsyncCallback<Boolean> callback);

	public void getStationStatusFields(AsyncCallback<String[][]> callback);

	public void getAnalyzersStatusFields(AsyncCallback<String[][]> callback);

	public void getRealTimeDataFields(Boolean onlyDeletedAnalyzers,
			AsyncCallback<String[][]> callback);

	public void getHistoryStationStatusFields(String alarmIdStr,
			String startDateStr, String startHourStr, String endDateStr,
			String endHourStr, AsyncCallback<String[][]> callback);

	public void getHistoryAnalyzersStatusFields(String analyzerIdStr,
			String startDateStr, String startHourStr, String endDateStr,
			String endHourStr, String evtType,
			AsyncCallback<String[][]> callback);

	public void getCurrentDate(AsyncCallback<String[]> callback);

	public void makeCsv(AsyncCallback<String> callback);

	public void generateChart(String title, String legend, String measureUnit,
			String[][] fieldsMatrix, boolean withSeconds, boolean interpolated,
			AsyncCallback<String> callback);

	public void getHistoryRealTimeDataFields(String analyzerIdStr,
			String elementIdStr, String requestedDateString, String hourStr,
			String minutesStr, AsyncCallback<String[][]> callback);

	public void getAcqPeriod(String analyzerIdStr, String elementIdStr,
			AsyncCallback<String> callback);

	public void getAvgPeriods(String analyzerIdStr, String elementIdStr,
			AsyncCallback<String[]> callback);

	public void getHistoryMeansDataFields(String analyzerIdStr,
			String elementIdStr, String periodStr, String chooseType,
			String halfDayStr, String startDateStr, String startHourStr,
			String endDateStr, String endHourStr, String maxDays,
			AsyncCallback<String[][]> callback);

	public void createAndGetChartNameOfRealTimeData(String analyzerIdStr,
			String elementIdStr, String requestedDateString,
			String startHourStr, String endHourStr, String title,
			String legend, String measureUnit, boolean autoScale,
			boolean showMinMax, AsyncCallback<String[]> callback);

	public void createAndGetChartNameOfMeansData(String analyzerIdStr,
			String elementIdStr, String startDateStr, String startHourStr,
			String endDateStr, String endHourStr, String maxDays,
			String periodStr, String title, String legend, String measureUnit,
			boolean autoScale, boolean showMinMax,
			AsyncCallback<String[]> callback);

	public void setLocale(String locale, AsyncCallback<Object> callback);

	public void getAnalyzerEvtArray(AsyncCallback<String[]> callback);

	public void unbindTriggerAlarm(String id, AsyncCallback<Object> callback);

	public void getBindableSampleElements(AsyncCallback<String[][]> callback);

	public void bindTriggerAlarm(String id, String elementId,
			String elementLbl, AsyncCallback<String[]> callback);

	public void getPerifericoStatusFields(AsyncCallback<String[]> callback);

	public void getUseGps(AsyncCallback<String> callback);

	public void getBrand(AsyncCallback<String[]> callbackSession);

	public void getModel(String brand, AsyncCallback<String[]> callbackSession);

	public void getConversionCoefficient(String analyzerId, String paramId,
			String acquisitionSelectedItemText,
			String analyzerSelectedItemText,
			AsyncCallback<String[]> callbackSession);

	public void getTimeOut(AsyncCallback<String> callback);

	public void getConnectionType(AsyncCallback<String> callback);

	public void getConfigurationPage(String startDate, String endDate,
			Integer limit, AsyncCallback<String[][]> callback);

	public void loadHistoricConf(String date, AsyncCallback<String[]> callback);

	public void createNewConnectionParam(String type,
			AsyncCallback<String> callback);

	public void getListConnectionType(AsyncCallback<String[]> callback2);

	public void connectToCop(String text, String itemText,
			AsyncCallback<String[]> callback);

	public void loadListConf(String stationName, String start, String end,
			Integer limit, AsyncCallback<String[]> callback);

	public void readConf(String itemText, String itemText2,
			AsyncCallback<String[]> callback);

	public void disactiveMaintenance(String value,
			AsyncCallback<String> callback);

	public void activeMaintenance(String value,
			AsyncCallback<Boolean[]> callback);

	public void disactiveCalibration(String value,
			AsyncCallback<String> callback);

	public void activeCalibration(String value,
			AsyncCallback<Boolean[]> callback);

	public void readValueForCalibration(String analyzerId, String value,
			AsyncCallback<String[]> callback);

	public void calculate(String analyzerId, String paramId,
			String expectedValue1, String expectedValue2, String readValue1,
			String readValue2, AsyncCallback<String[]> callback);

	public void verifyInsertNewAnalyzer(String analyzerId,
			AsyncCallback<String> callback);

	public void verifyDpaIsActive(String analyzerId, String value,
			AsyncCallback<Boolean> callback);

	public void isToSave(AsyncCallback<Boolean> callback);

	public void getPerifericoVersion(AsyncCallback<String> callback);

	public void getStationName(AsyncCallback<String> callback);

	public void getCommandList(String analyzerId,
			AsyncCallback<List<String[]>> callback);

	public void sendCommand(String analyzerId, String command[],
			AsyncCallback<String> callback);

	public void getCommandResult(String commandId,
			AsyncCallback<List<String>> callback);

	public void getAlarmValues(String analyzerId,
			AsyncCallback<List<String>> callback);

	public void getProcessParameterValues(String analyzerId,
			AsyncCallback<List<String>> callback);

	public void getMeasureValues(String analyzerId,
			AsyncCallback<List<String>> callback);

	public void getAlarmList(String analyzerId,
			AsyncCallback<List<String>> callback);

	public void getAnalyzerDate(String analyzerId, AsyncCallback<Date> callback);

	public void setAnalyzerDate(String analyzerId, AsyncCallback<Date> callback);

	public void getSerialNumber(String analyzerId,
			AsyncCallback<String> callback);

	public void resetAllFaults(String analyzerId,
			AsyncCallback<Integer> callback);

	public void isRemoteUISupported(String analyzerId,
			AsyncCallback<Boolean> callback);

	public void isRemoteGUISupported(String analyzerId,
			AsyncCallback<String> callback);

	public void getKeyList(String analyzerId,
			AsyncCallback<List<String>> callback);

	public void readDisplay(String analyzerId,
			AsyncCallback<List<String>> callback);

	public void readDisplayImage(String analyzerId,
			AsyncCallback<String> callback);

	public void sendKey(String analyzerId, String key,
			AsyncCallback<List<String>> callback);

	public void isCustomCommandSupported(String analyzerId,
			AsyncCallback<Boolean> callback);

	public void getEquivalentBrands(String brand, String model,
			AsyncCallback<List<String>> callback);

	public void getEquivalentModels(String newBrand, String brand,
			String model, AsyncCallback<List<String>> callback);

}
