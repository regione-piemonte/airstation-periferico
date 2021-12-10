/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: defines a generic sample element
// Change log:
//   2008-10-08: initial version
// ----------------------------------------------------------------------------
// $Id: GenericSampleElement.java,v 1.26 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.Periferico;
import it.csi.periferico.config.common.CommonCfg;
import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.MeasureUnit;
import it.csi.periferico.config.common.Parameter;
import it.csi.periferico.config.common.Standards;
import it.csi.periferico.config.common.converters.Converter;
import it.csi.periferico.config.common.converters.MolecularWeightMissingException;
import it.csi.periferico.storage.StorageException;
import it.csi.periferico.storage.StorageManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * Defines a generic sample element
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class GenericSampleElement extends ScalarElement implements
		ObservableSampleElement {

	private static final long serialVersionUID = 1095337690649273484L;

	private static final int DEFAULT_ACQ_PERIOD = 10; // s

	private static Logger logger = Logger.getLogger("periferico."
			+ GenericSampleElement.class.getSimpleName());

	private String analyzerMeasureUnitName = "";

	private int acqPeriod = DEFAULT_ACQ_PERIOD; // s

	private double correctionCoefficient = 1.0;

	private double correctionOffset = 0.0;

	private double linearizationCoefficient = 1.0;

	private double linearizationOffset = 0.0;

	private Converter converter = null;

	private ElementHolder elementHolder = null;

	private transient List<Sample> listSamples = new ArrayList<Sample>();

	private transient Sample lastSample = null;

	private transient SampleValues lastSampleValues = null;

	private transient Double calib1stPoint = null;

	private transient Double calib2ndPoint = null;

	private transient Double value1stPoint = null;

	private transient Double value2ndPoint = null;

	// NOTE: this list is not transient, because the user interface needs the
	// references to SampleObserver objects, in order to manage unbind
	// operations properly when the Element is deleted
	private List<SampleObserver> listSampleObservers = new ArrayList<SampleObserver>();

	public int getAcqPeriod() {
		return acqPeriod;
	}

	public void setAcqPeriod(int acqPeriod) {
		this.acqPeriod = acqPeriod;
	}

	public Sample getLastSample() {
		return lastSample;
	}

	public SampleValues getLastSampleValues() {
		return lastSampleValues;
	}

	public double getCorrectionCoefficient() {
		return correctionCoefficient;
	}

	public void setCorrectionCoefficient(double correctionCoefficient) {
		this.correctionCoefficient = correctionCoefficient;
	}

	public double getCorrectionOffset() {
		return correctionOffset;
	}

	public void setCorrectionOffset(double correctionOffset) {
		this.correctionOffset = correctionOffset;
	}

	public double getLinearizationCoefficient() {
		return linearizationCoefficient;
	}

	public void setLinearizationCoefficient(double linearizationCoefficient) {
		this.linearizationCoefficient = linearizationCoefficient;
	}

	public double getLinearizationOffset() {
		return linearizationOffset;
	}

	public void setLinearizationOffset(double linearizationOffset) {
		this.linearizationOffset = linearizationOffset;
	}

	public String getAnalyzerMeasureUnitName() {
		return analyzerMeasureUnitName;
	}

	public void setAnalyzerMeasureUnitName(String analyzerMeasureUnitName) {
		this.analyzerMeasureUnitName = trim(analyzerMeasureUnitName);
		updateConverter();
	}

	@Override
	public void setMeasureUnitName(String measureUnitName) {
		super.setMeasureUnitName(measureUnitName);
		updateConverter();
	}

	@Override
	public void setParameterId(String parameterId) {
		super.setParameterId(parameterId);
		updateConverter();
	}

	public Converter getConverter() {
		return converter;
	}

	synchronized List<Sample> getSamplesAndCleanList() {
		List<Sample> tmpList = listSamples;
		listSamples = new ArrayList<Sample>();
		return tmpList;
	}

	void addNewSample(Sample sample, SampleValues sampleValues) {
		addNewSample(sample, sampleValues, false);
	}

	synchronized void addNewSample(Sample sample, SampleValues sampleValues,
			boolean extra) {
		lastSampleValues = sampleValues;
		if (extra)
			return;
		lastSample = sample;
		listSamples.add(sample);
		for (SampleObserver so : listSampleObservers) {
			so.deliver(sample);
		}
	}

	public boolean addSampleObserver(SampleObserver so) {
		if (so == null)
			return false;
		listSampleObservers.add(so);
		return true;
	}

	public void unbindObservers() {
		for (SampleObserver so : listSampleObservers) {
			so.unbind();
		}
		listSampleObservers = new ArrayList<SampleObserver>();
	}

	public void setConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset)
			throws ConfigException {
		checkAcqPeriod(acqPeriod);
		checkCorrectionCoefficient(correctionCoefficient);
		checkLinearizationCoefficient(linearizationCoefficient);
		checkConverter(analyzerMeasureUnitName, measureUnitName);
		super.setConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals);
		setAnalyzerMeasureUnitName(analyzerMeasureUnitName);
		setAcqPeriod(acqPeriod);
		setCorrectionCoefficient(correctionCoefficient);
		setCorrectionOffset(correctionOffset);
		setLinearizationCoefficient(linearizationCoefficient);
		setLinearizationOffset(linearizationOffset);
	}

	public boolean isSameConfig(boolean enabled, String measureUnitName,
			double minValue, double maxValue, int numDecimals,
			String analyzerMeasureUnitName, int acqPeriod,
			double correctionCoefficient, double correctionOffset,
			double linearizationCoefficient, double linearizationOffset) {
		return super.isSameConfig(enabled, measureUnitName, minValue, maxValue,
				numDecimals)
				&& this.analyzerMeasureUnitName
						.equals(trim(analyzerMeasureUnitName))
				&& this.acqPeriod == acqPeriod
				&& this.correctionCoefficient == correctionCoefficient
				&& this.correctionOffset == correctionOffset
				&& this.linearizationCoefficient == linearizationCoefficient
				&& this.linearizationOffset == linearizationOffset;
	}

	@Override
	public void checkConfig() throws ConfigException {
		super.checkConfig();
		checkAcqPeriod(acqPeriod);
		checkCorrectionCoefficient(correctionCoefficient);
		checkLinearizationCoefficient(linearizationCoefficient);
		checkConverter(analyzerMeasureUnitName, getMeasureUnitName());
	}

	void setElementHolder(ElementHolder elementHolder) {
		this.elementHolder = elementHolder;
	}

	private void checkAcqPeriod(int acqPeriod) throws ConfigException {
		if (acqPeriod <= 0)
			throw new ConfigException("acqPeriod_le_0");
	}

	private void checkCorrectionCoefficient(double correctionCoefficient)
			throws ConfigException {
		if (correctionCoefficient <= 0)
			throw new ConfigException("correctionCoefficient_le_0");
	}

	private void checkLinearizationCoefficient(double linearizationCoefficient)
			throws ConfigException {
		if (linearizationCoefficient <= 0)
			throw new ConfigException("linearizationCoefficient_le_0");
	}

	private Converter initConverter(String analyzerMUName,
			String acquisitionMUName) throws Exception {
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		MeasureUnit srcMU = cc.getMeasureUnit(analyzerMUName);
		MeasureUnit destMU = cc.getMeasureUnit(acquisitionMUName);
		Parameter param = cc.getParameter(getParameterId());
		Standards standards = cc.getStandards();
		return new Converter(srcMU, destMU, param, standards);
	}

	private void updateConverter() {
		try {
			converter = initConverter(analyzerMeasureUnitName,
					getMeasureUnitName());
		} catch (Exception ex) {
			converter = null;
		}
	}

	private void checkConverter(String analyzerMUName, String acquisitionMUName)
			throws ConfigException {
		try {
			initConverter(analyzerMUName, acquisitionMUName);
		} catch (MolecularWeightMissingException mwme) {
			throw new ConfigException("no_molecular_weight", mwme);
		} catch (Exception ex) {
			logger.error("Cannot initialize measure units converter [param="
					+ getParameterId() + ", analyzerMeasureUnit="
					+ analyzerMeasureUnitName + ", acquisitionMeasureUnit="
					+ getMeasureUnitName() + "]", ex);
			throw new ConfigException("converter_init_error", ex);
		}
	}

	public String getConversionInfo() {
		return getConversionInfoImpl(converter);
	}

	public String getConversionInfo(String analyzerMUName,
			String acquisitionMUName) {
		Converter conv = null;
		try {
			conv = initConverter(analyzerMUName, acquisitionMUName);
		} catch (Exception ex) {
		}
		return getConversionInfoImpl(conv);
	}

	private String getConversionInfoImpl(Converter conv) {
		Double valueMW = null;
		Double valueT = null;
		Double valueP = null;
		CommonCfg cc = Periferico.getInstance().getCommonCfg();
		Parameter param = cc.getParameter(getParameterId());
		if (param != null)
			valueMW = param.getMolecularWeight();
		Standards standards = cc.getStandards();
		if (standards != null) {
			valueT = standards.getReferenceTemperature_K();
			valueP = standards.getReferencePressure_kPa();
		}
		StringBuffer sb = new StringBuffer();
		sb.append("K=");
		if (conv != null) {
			NumberFormat nf = NumberFormat.getInstance(Locale.US);
			nf.setMaximumFractionDigits(6);
			nf.setGroupingUsed(false);
			sb.append(nf.format(conv.getConversionCoefficient()));
		} else
			sb.append(" ");
		sb.append(" M=" + (valueMW == null ? " " : valueMW + "[g/mole]"));
		sb.append(" T=" + (valueT == null ? " " : valueT + "[K]"));
		sb.append(" P=" + (valueP == null ? " " : valueP + "[kPa]"));
		return sb.toString();
	}

	// NOTE: this algorithm requires the Sample's timestamp to have a second
	// time resolution; millisecond resolution is not acceptable
	@Override
	MeanValue computeAggregateValue(int aggregationPeriod_m,
			Date aggregationTime, List<Sample> data) {
		if (acqPeriod <= 0)
			throw new IllegalStateException(
					"Acquisition period must be greater than 0");
		if (aggregationPeriod_m * 60 < acqPeriod)
			throw new IllegalArgumentException("Aggregation period must not be"
					+ " lower than acquisition period");
		int numExpected = 0;
		int numValid = 0;
		int numNotvalid = 0;
		int numNotValidForAnalyzer = 0;
		int numMissing;
		int numIncoherentTimestamp = 0;
		double accumulator = 0.0;
		int globalFlags = 0;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(aggregationTime);
		calendar.add(Calendar.MINUTE, -aggregationPeriod_m);
		calendar.add(Calendar.SECOND, acqPeriod);
		Date timestamp = calendar.getTime();
		ListIterator<Sample> it = data.listIterator();
		while (!timestamp.after(aggregationTime)) {
			numExpected++;
			while (it.hasNext()) {
				Sample sample = it.next();
				if (sample.getTimestamp().before(timestamp)) {
					numIncoherentTimestamp++;
					continue;
				}
				if (sample.getTimestamp().equals(timestamp)) {
					if (sample.isNotvalid()) {
						numNotvalid++;
					} else {
						numValid++;
						if (sample.getValue() == null)
							throw new IllegalStateException(
									"Valid sample must have not null value");
						accumulator += sample.getValue();
						if ((sample.getFlags() & ValidationFlag.ANALYZER_DATA_NOT_VALID) != 0)
							numNotValidForAnalyzer++;
					}
					globalFlags |= sample.getFlags();
					break;
				}
				it.previous();
				break;
			}
			calendar.add(Calendar.SECOND, acqPeriod);
			timestamp = calendar.getTime();
		}
		if (numExpected == 0)
			throw new IllegalStateException("Zero data expected for mean"
					+ " computation: at least 1 is needed");
		numMissing = numExpected - numValid - numNotvalid;
		Double value = null;
		if (numValid > 0)
			value = Periferico.roundAggregateData(accumulator / numValid,
					getNumDec());
		boolean notvalid = (((numValid - numNotValidForAnalyzer) * 100) / numExpected) < 75;
		if (numMissing > 0)
			globalFlags |= ValidationFlag.MISSING_DATA;
		if (numIncoherentTimestamp > 0)
			logger.warn("Found " + numIncoherentTimestamp
					+ " samples with acquisition timestamp incoherent "
					+ "with acquisition period");
		logger.debug("Mean value computed: value=" + value + ", notvalid="
				+ notvalid + ", flags=" + Integer.toHexString(globalFlags)
				+ " (expected=" + numExpected + ", valid=" + numValid
				+ ", notvalid for analyzer=" + numNotValidForAnalyzer
				+ ", notvalid=" + numNotvalid + ", missing=" + numMissing + ")");
		return new MeanValue(aggregationTime, value, notvalid, globalFlags);
	}

	public Double getCalib1stPoint() {
		return calib1stPoint;
	}

	public void setCalib1stPoint(Double calib1stPoint) throws ConfigException {
		if (calib1stPoint != null && calib2ndPoint != null
				&& calib1stPoint.equals(calib2ndPoint))
			throw new ConfigException("calib_points_should_be_different");
		this.calib1stPoint = calib1stPoint;
	}

	public Double getCalib2ndPoint() {
		return calib2ndPoint;
	}

	public void setCalib2ndPoint(Double calib2ndPoint) throws ConfigException {
		if (calib1stPoint != null && calib2ndPoint != null
				&& calib1stPoint.equals(calib2ndPoint))
			throw new ConfigException("calib_points_should_be_different");
		this.calib2ndPoint = calib2ndPoint;
	}

	public abstract Double getCalib1stPointPercent() throws ConfigException;

	public abstract void setCalib1stPointPercent(Double calib1stPointPercent)
			throws ConfigException;

	public abstract Double getCalib2ndPointPercent() throws ConfigException;

	public abstract void setCalib2ndPointPercent(Double calib2ndPointPercent)
			throws ConfigException;

	public abstract boolean isPercentageUsable();

	public boolean isManualCalibrationRunning() {
		if (elementHolder == null)
			throw new IllegalStateException("ElementHolder must be initialized");
		return elementHolder.isManualCalibrationRunning();
	}

	public Double getValue1stPoint() {
		return value1stPoint;
	}

	public void setValue1stPoint(Double value1stPoint) {
		this.value1stPoint = value1stPoint;
	}

	public Double getValue2ndPoint() {
		return value2ndPoint;
	}

	public void setValue2ndPoint(Double value2ndPoint) {
		this.value2ndPoint = value2ndPoint;
	}

	public Correction computeCorrection() throws ConfigException {
		if (elementHolder == null)
			throw new IllegalStateException("ElementHolder must be initialized");
		if (calib1stPoint == null && calib2ndPoint == null)
			throw new ConfigException("no_points_configured");
		logger.debug("calib1stPoint=" + calib1stPoint);
		logger.debug("value1stPoint=" + value1stPoint);
		logger.debug("calib2ndPoint=" + calib2ndPoint);
		logger.debug("value2ndPoint=" + value2ndPoint);
		if (calib1stPoint == null || calib2ndPoint == null) {
			// Single point correction
			Double calibPoint = null;
			Double valuePoint = null;
			if (calib1stPoint != null) {
				calibPoint = calib1stPoint;
				valuePoint = value1stPoint;
			} else {
				calibPoint = calib2ndPoint;
				valuePoint = value2ndPoint;
			}
			if (valuePoint == null)
				throw new ConfigException("no_points_acquired");
			logger.debug("Single point correction: offset="
					+ (calibPoint - valuePoint));
			return new Correction(calibPoint - valuePoint);
		}
		// Two points correction
		if (value1stPoint == null && value2ndPoint == null)
			throw new ConfigException("no_points_acquired");
		if (value1stPoint == null || value2ndPoint == null)
			throw new ConfigException("only_1_point_acquired");
		double coefficient = (calib2ndPoint - calib1stPoint)
				/ (value2ndPoint - value1stPoint);
		double offset = calib1stPoint - coefficient * value1stPoint;
		logger.debug("Two points correction: offset=" + offset
				+ " coefficient=" + coefficient);
		return new Correction(coefficient, offset);
	}

	@Override
	void readAndSetLastAggregateValues(Date aggregationsComputeStartTime) {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		for (Integer period : getAvgPeriods()) {
			try {
				List<MeanValue> data = storageManager.readMeanData(
						getAnalyzerId(), getParameterId(), period,
						aggregationsComputeStartTime, false, new Date(), true,
						null);
				if (!data.isEmpty()) {
					MeanValue mv = data.get(data.size() - 1);
					setLastAggregateValue(period, mv);
					logger.debug("Last aggregate value for " + getBindLabel()
							+ ", period " + period + " is: " + mv.getValue()
							+ ", " + mv.getTimestamp());
				}
			} catch (StorageException e) {
				logger.error("Error reading last aggregate value " + "for "
						+ getBindLabel() + ", period " + period);
			}
		}
	}

	@Override
	void saveData() throws StorageException {
		StorageManager storageManager = Periferico.getInstance()
				.getStorageManager();
		List<Sample> data = getSamplesAndCleanList();
		storageManager.saveSampleData(getAnalyzerId(), getParameterId(),
				getNumDec(), data);
	}

}
