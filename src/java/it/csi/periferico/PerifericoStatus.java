/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: holds the status of the application
// Change log:
//   2008-04-18: initial version
// ----------------------------------------------------------------------------
// $Id: PerifericoStatus.java,v 1.15 2015/10/16 12:58:49 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico;

import it.csi.periferico.Periferico.CommonCfgResult;
import it.csi.periferico.config.Config;

/**
 * Holds the status of the application
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class PerifericoStatus {

	private Boolean boardManagerInitStatus = null;

	private Integer configuredBoardsNumber = null;

	private Integer initializedBoardsNumber = null;

	private Integer failedBoardBindingsNumber = null;

	private Integer enabledDataPortAnalyzersNumber = null;

	private Integer initializedDataPortDriversNumber = null;

	private Integer failedDataPortThreadsNumber = null;

	private Boolean driverConfigsOK = null;

	private Config.Status loadConfigurationStatus = null;

	private Boolean saveNewConfigurationStatus = null;

	private Boolean configActivationStatus = null;

	private CommonCfgResult commonConfigFromCopStatus = null;

	private boolean dataInTheFuture = false;

	private int totalThreadFailures = 0;

	private int currentThreadFailures = 0;

	private int dataWriteErrorCount = 0;

	public boolean isOK() {
		return isOK(false);
	}

	public boolean isOK(boolean ignoreCommonConfigStatus) {
		if (boardManagerInitStatus == null || !boardManagerInitStatus)
			return false;
		if (configuredBoardsNumber == null || initializedBoardsNumber == null)
			return false;
		if (!initializedBoardsNumber.equals(configuredBoardsNumber))
			return false;
		if (failedBoardBindingsNumber == null || failedBoardBindingsNumber != 0)
			return false;
		if (enabledDataPortAnalyzersNumber == null || initializedDataPortDriversNumber == null)
			return false;
		if (!initializedDataPortDriversNumber.equals(enabledDataPortAnalyzersNumber))
			return false;
		if (failedDataPortThreadsNumber != null && failedDataPortThreadsNumber > 0)
			return false;
		if (loadConfigurationStatus == null || loadConfigurationStatus != Config.Status.OK)
			return false;
		if (configActivationStatus == null || !configActivationStatus)
			return false;
		if (saveNewConfigurationStatus != null && !saveNewConfigurationStatus)
			return false;
		if (currentThreadFailures > 0)
			return false;
		if (dataWriteErrorCount > 0)
			return false;
		if (dataInTheFuture)
			return false;
		if (commonConfigFromCopStatus != null && !ignoreCommonConfigStatus) {
			if (commonConfigFromCopStatus != CommonCfgResult.OK)
				return false;
		}
		return true;
	}

	public Boolean isDataPortAnalyzersOK() {
		if (enabledDataPortAnalyzersNumber == null && initializedDataPortDriversNumber == null)
			return null;
		if (enabledDataPortAnalyzersNumber == null || initializedDataPortDriversNumber == null)
			return false;
		if (!initializedDataPortDriversNumber.equals(enabledDataPortAnalyzersNumber))
			return false;
		if (failedDataPortThreadsNumber != null && failedDataPortThreadsNumber > 0)
			return false;
		return true;
	}

	public Boolean areDriverConfigsOK() {
		return driverConfigsOK;
	}

	public void setDriverConfigsOK(Boolean driverConfigsOK) {
		this.driverConfigsOK = driverConfigsOK;
	}

	public Boolean getBoardManagerInitStatus() {
		return boardManagerInitStatus;
	}

	public void setBoardManagerInitStatus(Boolean boardManagerInitStatus) {
		this.boardManagerInitStatus = boardManagerInitStatus;
	}

	public Integer getConfiguredBoardsNumber() {
		return configuredBoardsNumber;
	}

	public void setConfiguredBoardsNumber(Integer configuredBoardsNumber) {
		this.configuredBoardsNumber = configuredBoardsNumber;
	}

	public Integer getInitializedBoardsNumber() {
		return initializedBoardsNumber;
	}

	public void setInitializedBoardsNumber(Integer initializedBoardsNumber) {
		this.initializedBoardsNumber = initializedBoardsNumber;
	}

	public Config.Status getLoadConfigurationStatus() {
		return loadConfigurationStatus;
	}

	public void setLoadConfigurationStatus(Config.Status loadConfigurationStatus) {
		this.loadConfigurationStatus = loadConfigurationStatus;
	}

	public Boolean getSaveNewConfigurationStatus() {
		return saveNewConfigurationStatus;
	}

	public void setSaveNewConfigurationStatus(Boolean saveNewConfigurationStatus) {
		this.saveNewConfigurationStatus = saveNewConfigurationStatus;
	}

	public Boolean getConfigActivationStatus() {
		return configActivationStatus;
	}

	public void setConfigActivationStatus(Boolean configActivationStatus) {
		this.configActivationStatus = configActivationStatus;
	}

	public Integer getFailedBoardBindingsNumber() {
		return failedBoardBindingsNumber;
	}

	public void setFailedBoardBindingsNumber(Integer failedBoardBindingsNumber) {
		this.failedBoardBindingsNumber = failedBoardBindingsNumber;
	}

	public Integer getEnabledDataPortAnalyzersNumber() {
		return enabledDataPortAnalyzersNumber;
	}

	public void setEnabledDataPortAnalyzersNumber(Integer enabledDataPortAnalyzersNumber) {
		this.enabledDataPortAnalyzersNumber = enabledDataPortAnalyzersNumber;
	}

	public Integer getInitializedDataPortDriversNumber() {
		return initializedDataPortDriversNumber;
	}

	public void setInitializedDataPortDriversNumber(Integer initializedDataPortDriversNumber) {
		this.initializedDataPortDriversNumber = initializedDataPortDriversNumber;
	}

	public Integer getFailedDataPortThreadsNumber() {
		return failedDataPortThreadsNumber;
	}

	public void setFailedDataPortThreadsNumber(Integer failedDataPortThreadsNumber) {
		this.failedDataPortThreadsNumber = failedDataPortThreadsNumber;
	}

	public int getTotalThreadFailures() {
		return totalThreadFailures;
	}

	public CommonCfgResult getCommonConfigFromCopStatus() {
		return commonConfigFromCopStatus;
	}

	public void setCommonConfigFromCopStatus(CommonCfgResult commonConfigFromCopStatus) {
		this.commonConfigFromCopStatus = commonConfigFromCopStatus;
	}

	public boolean isDataInTheFuture() {
		return dataInTheFuture;
	}

	public void setDataInTheFuture(boolean dataInTheFuture) {
		this.dataInTheFuture = dataInTheFuture;
	}

	public int getCurrentThreadFailures() {
		return currentThreadFailures;
	}

	void incrementThreadFailures() {
		totalThreadFailures++;
		currentThreadFailures++;
	}

	void resetCurrentThreadFailures() {
		currentThreadFailures = 0;
	}

	public int getDataWriteErrorCount() {
		return dataWriteErrorCount;
	}

	public void setDataWriteErrorCount(int dataWriteErrorCount) {
		this.dataWriteErrorCount = dataWriteErrorCount;
	}

	@Override
	public String toString() {
		return "PerifericoStatus [boardManagerInitStatus=" + boardManagerInitStatus + ", configuredBoardsNumber="
				+ configuredBoardsNumber + ", initializedBoardsNumber=" + initializedBoardsNumber
				+ ", failedBoardBindingsNumber=" + failedBoardBindingsNumber + ", enabledDataPortAnalyzersNumber="
				+ enabledDataPortAnalyzersNumber + ", initializedDataPortDriversNumber="
				+ initializedDataPortDriversNumber + ", failedDataPortThreadsNumber=" + failedDataPortThreadsNumber
				+ ", loadConfigurationStatus=" + loadConfigurationStatus + ", saveNewConfigurationStatus="
				+ saveNewConfigurationStatus + ", configActivationStatus=" + configActivationStatus
				+ ", commonConfigFromCopStatus=" + commonConfigFromCopStatus + ", dataInTheFuture=" + dataInTheFuture
				+ ", totalThreadFailures=" + totalThreadFailures + ", currentThreadFailures=" + currentThreadFailures
				+ ", dataWriteErrorCount=" + dataWriteErrorCount + "]";
	}

}
