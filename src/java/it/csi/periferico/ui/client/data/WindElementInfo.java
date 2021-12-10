/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: data type for elements for UI
// Change log:
//   2015-05-08: initial version
// ----------------------------------------------------------------------------
// $Id: WindElementInfo.java,v 1.3 2015/05/13 16:22:21 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.ui.client.data;

import java.util.List;

/**
 * Data type for elements for UI
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class WindElementInfo extends ElementInfo {

	private static final long serialVersionUID = -6450622383739780923L;
	private Integer acqPeriod;
	private Double speedMaxValue;
	private Double speedCorrectionCoefficient;
	private Double speedCorrectionOffset;
	private String speedBoardBindInfo;
	private String speedMeasureUnit;
	private List<String> speedMeasureUnits;
	private Integer speedNumDec;
	private Double speedPrecision;
	private Double speedRangeHigh;
	private Double directionCorrectionCoefficient;
	private Double directionCorrectionOffset;
	private String directionBoardBindInfo;
	private String directionMeasureUnit;
	private Integer directionNumDec;
	private Double directionPrecision;

	public Integer getAcqPeriod() {
		return acqPeriod;
	}

	public void setAcqPeriod(Integer acqPeriod) {
		this.acqPeriod = acqPeriod;
	}

	public Double getSpeedMaxValue() {
		return speedMaxValue;
	}

	public void setSpeedMaxValue(Double speedMaxValue) {
		this.speedMaxValue = speedMaxValue;
	}

	public Double getSpeedCorrectionCoefficient() {
		return speedCorrectionCoefficient;
	}

	public void setSpeedCorrectionCoefficient(Double speedCorrectionCoefficient) {
		this.speedCorrectionCoefficient = speedCorrectionCoefficient;
	}

	public Double getSpeedCorrectionOffset() {
		return speedCorrectionOffset;
	}

	public void setSpeedCorrectionOffset(Double speedCorrectionOffset) {
		this.speedCorrectionOffset = speedCorrectionOffset;
	}

	public String getSpeedBoardBindInfo() {
		return speedBoardBindInfo;
	}

	public void setSpeedBoardBindInfo(String speedBoardBindInfo) {
		this.speedBoardBindInfo = speedBoardBindInfo;
	}

	public String getSpeedMeasureUnit() {
		return speedMeasureUnit;
	}

	public void setSpeedMeasureUnit(String speedMeasureUnit) {
		this.speedMeasureUnit = speedMeasureUnit;
	}

	public List<String> getSpeedMeasureUnits() {
		return speedMeasureUnits;
	}

	public void setSpeedMeasureUnits(List<String> speedMeasureUnits) {
		this.speedMeasureUnits = speedMeasureUnits;
	}

	public Integer getSpeedNumDec() {
		return speedNumDec;
	}

	public void setSpeedNumDec(Integer speedNumDec) {
		this.speedNumDec = speedNumDec;
	}

	public Double getSpeedPrecision() {
		return speedPrecision;
	}

	public void setSpeedPrecision(Double speedPrecision) {
		this.speedPrecision = speedPrecision;
	}

	public Double getSpeedRangeHigh() {
		return speedRangeHigh;
	}

	public void setSpeedRangeHigh(Double speedRangeHigh) {
		this.speedRangeHigh = speedRangeHigh;
	}

	public Double getDirectionCorrectionCoefficient() {
		return directionCorrectionCoefficient;
	}

	public void setDirectionCorrectionCoefficient(
			Double directionCorrectionCoefficient) {
		this.directionCorrectionCoefficient = directionCorrectionCoefficient;
	}

	public Double getDirectionCorrectionOffset() {
		return directionCorrectionOffset;
	}

	public void setDirectionCorrectionOffset(Double directionCorrectionOffset) {
		this.directionCorrectionOffset = directionCorrectionOffset;
	}

	public String getDirectionBoardBindInfo() {
		return directionBoardBindInfo;
	}

	public void setDirectionBoardBindInfo(String directionBoardBindInfo) {
		this.directionBoardBindInfo = directionBoardBindInfo;
	}

	public String getDirectionMeasureUnit() {
		return directionMeasureUnit;
	}

	public void setDirectionMeasureUnit(String directionMeasureUnit) {
		this.directionMeasureUnit = directionMeasureUnit;
	}

	public Integer getDirectionNumDec() {
		return directionNumDec;
	}

	public void setDirectionNumDec(Integer directionNumDec) {
		this.directionNumDec = directionNumDec;
	}

	public Double getDirectionPrecision() {
		return directionPrecision;
	}

	public void setDirectionPrecision(Double directionPrecision) {
		this.directionPrecision = directionPrecision;
	}

	@Override
	public String toString() {
		return "WindElementInfo [acqPeriod=" + acqPeriod + ", speedMaxValue="
				+ speedMaxValue + ", speedCorrectionCoefficient="
				+ speedCorrectionCoefficient + ", speedCorrectionOffset="
				+ speedCorrectionOffset + ", speedBoardBindInfo="
				+ speedBoardBindInfo + ", speedMeasureUnit=" + speedMeasureUnit
				+ ", speedMeasureUnits=" + speedMeasureUnits + ", speedNumDec="
				+ speedNumDec + ", speedPrecision=" + speedPrecision
				+ ", speedRangeHigh=" + speedRangeHigh
				+ ", directionCorrectionCoefficient="
				+ directionCorrectionCoefficient
				+ ", directionCorrectionOffset=" + directionCorrectionOffset
				+ ", directionBoardBindInfo=" + directionBoardBindInfo
				+ ", directionMeasureUnit=" + directionMeasureUnit
				+ ", directionNumDec=" + directionNumDec
				+ ", directionPrecision=" + directionPrecision
				+ ", getParameterId()=" + getParameterId()
				+ ", getAnalyzerName()=" + getAnalyzerName() + ", isEnabled()="
				+ isEnabled() + ", getAvailableAvgPeriods()="
				+ getAvailableAvgPeriods() + ", getSelectedAvgPeriods()="
				+ getSelectedAvgPeriods() + ", isCalibrationRunning()="
				+ isCalibrationRunning() + ", isMaintenanceRunning()="
				+ isMaintenanceRunning() + ", isActiveInRunningCfg()="
				+ isActiveInRunningCfg() + "]";
	}

}
