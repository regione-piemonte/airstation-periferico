/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: configuration common to all the measurement stations of a 
//                  given measurement network
// Change log:
//   2008-02-26: initial version
// ----------------------------------------------------------------------------
// $Id: CommonCfg.java,v 1.1 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.config.common;

import it.csi.periferico.config.common.converters.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration common to all the measurement stations of a given measurement
 * network
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class CommonCfg {

	public static final String WIND_DIRECTION_MEASURE_UNIT_NAME = "°";

	public static final String WIND_SPEED_MPS_MEASURE_UNIT_NAME = "m/s";

	private static final String DEFAULT_MAPS_SITE_URL_FORMATTER = "http://maps.google.com/maps?q=%f,%f&hl=%s";

	private Integer defaultAvgPeriod = null;

	private List<Integer> avgPeriods = new ArrayList<Integer>();

	private Integer dataWriteToDiskPeriod = null;

	private Integer manualOperationsAutoResetPeriod = null;

	private Integer copServicePort = null;

	private String mapsSiteURLFormatter = DEFAULT_MAPS_SITE_URL_FORMATTER;

	private StorageManagerCfg storageManagerCfg = null;

	private Standards standards = null;

	private List<AlarmName> alarmNames = new ArrayList<AlarmName>();

	private HashMap<String, AlarmName> hmAlarmNames;

	private List<Parameter> parameters = new ArrayList<Parameter>();

	private HashMap<String, Parameter> hmParameters;

	private List<MeasureUnit> measureUnits = new ArrayList<MeasureUnit>();

	private HashMap<String, MeasureUnit> hmMeasureUnits;

	public Integer getDefaultAvgPeriod() {
		return defaultAvgPeriod;
	}

	public void setDefaultAvgPeriod(Integer defaultAvgPeriod) {
		this.defaultAvgPeriod = defaultAvgPeriod;
	}

	public List<Integer> getAvgPeriods() {
		return avgPeriods;
	}

	public void setAvgPeriods(List<Integer> avgPeriods) {
		this.avgPeriods = avgPeriods;
	}

	public Integer getDataWriteToDiskPeriod() {
		return dataWriteToDiskPeriod;
	}

	public void setDataWriteToDiskPeriod(Integer dataWriteToDiskPeriod) {
		this.dataWriteToDiskPeriod = dataWriteToDiskPeriod;
	}

	public Integer getManualOperationsAutoResetPeriod() {
		return manualOperationsAutoResetPeriod;
	}

	public void setManualOperationsAutoResetPeriod(
			Integer manualOperationsAutoResetPeriod) {
		this.manualOperationsAutoResetPeriod = manualOperationsAutoResetPeriod;
	}

	public Integer getCopServicePort() {
		return copServicePort;
	}

	public String getMapsSiteURLFormatter() {
		return mapsSiteURLFormatter;
	}

	public void setMapsSiteURLFormatter(String mapsSiteURLFormatter) {
		this.mapsSiteURLFormatter = mapsSiteURLFormatter;
	}

	public void setCopServicePort(Integer copServicePort) {
		this.copServicePort = copServicePort;
	}

	public StorageManagerCfg getStorageManagerCfg() {
		return storageManagerCfg;
	}

	public void setStorageManagerCfg(StorageManagerCfg storageManagerCfg) {
		this.storageManagerCfg = storageManagerCfg;
	}

	public Standards getStandards() {
		return standards;
	}

	public void setStandards(Standards standards) {
		this.standards = standards;
	}

	public List<AlarmName> getAlarmNames() {
		return alarmNames;
	}

	public void setAlarmNames(List<AlarmName> alarmNames) {
		this.alarmNames = alarmNames;
	}

	public AlarmName getAlarmName(String id) {
		if (hmAlarmNames == null)
			throw new IllegalStateException("AlarmNames map not initialized");
		return (hmAlarmNames.get(id));
	}

	public List<String> getAlarmNamesAsStrings() {
		ArrayList<String> strings = new ArrayList<String>();
		for (AlarmName an : alarmNames) {
			strings.add(an.getName() + " (" + an.getId() + ")");
		}
		return strings;
	}

	public AlarmName getAlarmNameFromString(String str) {
		if (str == null)
			return null;
		int lastOpenBracketIndex = str.lastIndexOf('(');
		if (lastOpenBracketIndex < 0)
			return null;
		String id = str.substring(lastOpenBracketIndex + 1, str.length() - 1);
		return getAlarmName(id);
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public Parameter getParameter(String id) {
		if (hmParameters == null)
			throw new IllegalStateException("Parameters map not initialized");
		return (hmParameters.get(id));
	}

	public List<Parameter> getParameters(Parameter.ParamType type) {
		List<Parameter> list = new ArrayList<Parameter>();
		for (Parameter p : parameters)
			if (p.getType() == type)
				list.add(p);
		return list;
	}

	public List<String> getParametersAsStrings() {
		ArrayList<String> strings = new ArrayList<String>();
		for (Parameter p : parameters) {
			strings.add(p.getId() + " (" + p.getName() + ")");
		}
		return strings;
	}

	public Parameter getParameterFromString(String str) {
		if (str == null)
			return null;
		int lastOpenBracketIndex = str.lastIndexOf(" (");
		if (lastOpenBracketIndex < 0)
			return null;
		String id = str.substring(0, lastOpenBracketIndex);
		return getParameter(id);
	}

	public List<MeasureUnit> getMeasureUnits() {
		return measureUnits;
	}

	public void setMeasureUnits(List<MeasureUnit> measureUnits) {
		this.measureUnits = measureUnits;
	}

	public MeasureUnit getMeasureUnit(String name) {
		if (hmMeasureUnits == null)
			throw new IllegalStateException("MeasureUnits map not initialized");
		return (hmMeasureUnits.get(name));
	}

	public List<String> getMeasureUnitNames() {
		ArrayList<String> strings = new ArrayList<String>();
		for (MeasureUnit mu : measureUnits) {
			strings.add(mu.getName());
		}
		return strings;
	}

	public List<String> getMeasureUnitNames(String physicalDimension) {
		ArrayList<String> strings = new ArrayList<String>();
		for (MeasureUnit mu : measureUnits) {
			if (physicalDimension != null && mu.getPhysicalDimension() != null
					&& physicalDimension.equals(mu.getPhysicalDimension()))
				strings.add(mu.getName());
		}
		return strings;
	}

	public List<String> getAnalyzerMeasureUnitNames(String physicalDimension) {
		ArrayList<String> strings = new ArrayList<String>();
		for (MeasureUnit mu : measureUnits) {
			if (physicalDimension != null && mu.getPhysicalDimension() != null
					&& physicalDimension.equals(mu.getPhysicalDimension())
					&& mu.isAllowedForAnalyzer())
				strings.add(mu.getName());
		}
		return strings;
	}

	public List<String> getAcquisitionMeasureUnitNames(String physicalDimension) {
		ArrayList<String> strings = new ArrayList<String>();
		for (MeasureUnit mu : measureUnits) {
			if (physicalDimension != null && mu.getPhysicalDimension() != null
					&& physicalDimension.equals(mu.getPhysicalDimension())
					&& mu.isAllowedForAcquisition())
				strings.add(mu.getName());
		}
		return strings;
	}

	public void checkAndInit() throws ConfigException {
		if (defaultAvgPeriod != null && !avgPeriods.contains(defaultAvgPeriod))
			avgPeriods.add(defaultAvgPeriod);
		hmAlarmNames = new HashMap<String, AlarmName>();
		hmParameters = new HashMap<String, Parameter>();
		hmMeasureUnits = new HashMap<String, MeasureUnit>();
		if (standards == null)
			throw new ConfigException("Standard reference values missing");
		else if (standards.getReferencePressure_kPa() == null)
			throw new ConfigException("Reference pressure missing");
		else if (standards.getReferenceTemperature_K() == null)
			throw new ConfigException("Reference temperature missing");
		if (standards.getReferencePressure_kPa() <= 0)
			throw new ConfigException(
					"Reference pressure [kPa] must be a positive value");
		if (standards.getReferenceTemperature_K() <= 0)
			throw new ConfigException(
					"Reference temperature [K] must be a positive value");
		if (measureUnits == null)
			throw new ConfigException("No measure unit definitions found");
		Set<String> physicalDimensionsForAnalyzers = new HashSet<String>();
		Set<String> physicalDimensionsForAcquisition = new HashSet<String>();
		for (MeasureUnit mu : measureUnits) {
			hmMeasureUnits.put(mu.getName(), mu);
			if (mu.isAllowedForAnalyzer())
				physicalDimensionsForAnalyzers.add(mu.getPhysicalDimension());
			if (mu.isAllowedForAcquisition())
				physicalDimensionsForAcquisition.add(mu.getPhysicalDimension());
			if (mu.getConversionMultiplyer() == 0)
				throw new ConfigException("Measure unit " + mu.getName()
						+ ": conversion multiplyer cannot be 0");
			if (!Converter.isConversionFormulaSupported(mu
					.getConversionFormula()))
				throw new ConfigException("Measure unit " + mu.getName()
						+ ": conversion formula " + mu.getConversionFormula()
						+ " is not supported");
		}
		if (parameters == null)
			throw new ConfigException("No parameter definitions found");
		int numWind = 0;
		int numWindDir = 0;
		int numWindVel = 0;
		int numRain = 0;
		for (Parameter pa : parameters) {
			hmParameters.put(pa.getId(), pa);
			if (pa.getType() == Parameter.ParamType.WIND)
				numWind++;
			else if (pa.getType() == Parameter.ParamType.WIND_DIR)
				numWindDir++;
			else if (pa.getType() == Parameter.ParamType.WIND_VEL)
				numWindVel++;
			else if (pa.getType() == Parameter.ParamType.RAIN)
				numRain++;
			if (pa.getMolecularWeight() != null && pa.getMolecularWeight() <= 0)
				throw new ConfigException(
						"Molecular weight [grams/mole] must be a positive value");
			if (!physicalDimensionsForAcquisition.contains(pa
					.getPhysicalDimension()))
				throw new ConfigException("Physical dimension "
						+ pa.getPhysicalDimension() + " of parameter "
						+ pa.getId() + " is not available for acquisition in "
						+ "any measure unit");
			if (!physicalDimensionsForAnalyzers.contains(pa
					.getPhysicalDimension()))
				throw new ConfigException("Physical dimension "
						+ pa.getPhysicalDimension() + " of parameter "
						+ pa.getId() + " is not available for analyzers in "
						+ "any measure unit");
		}
		if (numWind != 1)
			throw new ConfigException(
					"One and only one vectorial wind parameter expected"
							+ ", found " + numWind);
		if (numWindDir != 1)
			throw new ConfigException(
					"One and only one wind direction parameter expected"
							+ ", found " + numWindDir);
		if (numWindVel != 1)
			throw new ConfigException(
					"One and only one wind speed parameter expected"
							+ ", found " + numWindVel);
		if (numRain != 1)
			throw new ConfigException(
					"One and only one rain parameter expected" + ", found "
							+ numRain);
		if (alarmNames != null) {
			for (AlarmName an : alarmNames)
				hmAlarmNames.put(an.getId(), an);
		}
		Parameter windDirParam = getParameters(Parameter.ParamType.WIND_DIR)
				.get(0);
		MeasureUnit windDirMU = getMeasureUnit(WIND_DIRECTION_MEASURE_UNIT_NAME);
		if (windDirMU == null)
			throw new ConfigException("Wind direction parameter "
					+ windDirParam.getId()
					+ " requires a measure unit with name: "
					+ WIND_DIRECTION_MEASURE_UNIT_NAME);
		String windDirParamDimension = windDirParam.getPhysicalDimension();
		if (!windDirParamDimension.equals(windDirMU.getPhysicalDimension()))
			throw new ConfigException("Wind direction parameter "
					+ windDirParam.getId() + " and its measure unit "
					+ WIND_DIRECTION_MEASURE_UNIT_NAME
					+ " do not have the same physical dimension");
		Parameter windSpeedParam = getParameters(Parameter.ParamType.WIND_VEL)
				.get(0);
		MeasureUnit windSpeedMU = getMeasureUnit(WIND_SPEED_MPS_MEASURE_UNIT_NAME);
		if (windSpeedMU == null)
			throw new ConfigException("Wind speed parameter "
					+ windSpeedParam.getId()
					+ " requires a measure unit with name: "
					+ WIND_SPEED_MPS_MEASURE_UNIT_NAME);
		String windSpeedParamDimension = windSpeedParam.getPhysicalDimension();
		if (!windSpeedParamDimension.equals(windSpeedMU.getPhysicalDimension()))
			throw new ConfigException("Wind speed parameter "
					+ windSpeedParam.getId() + " and its default measure unit "
					+ WIND_SPEED_MPS_MEASURE_UNIT_NAME
					+ " do not have the same physical dimension");
	}

	public Set<String> getParameterIdSet() {
		if (hmParameters == null)
			throw new IllegalStateException("Parameters map not initialized");
		return hmParameters.keySet();
	}

	public Set<String> getMeasureUnitNameSet() {
		if (hmMeasureUnits == null)
			throw new IllegalStateException("MeasureUnits map not initialized");
		return hmMeasureUnits.keySet();
	}

}
