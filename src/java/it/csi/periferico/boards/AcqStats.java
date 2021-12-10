/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: statistics for data acquisition
// Change log:
//   2008-05-22: initial version
// ----------------------------------------------------------------------------
// $Id: AcqStats.java,v 1.4 2009/04/14 16:06:46 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

/**
 * Statistics for data acquisition
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class AcqStats {

	private long diPeriod;

	private long aiPeriod;

	private long delayedDICount;

	private long maximumDITime;

	private long intimeDICount;

	private long intimeDIAccumulator;

	private long delayedAICount;

	private long maximumAITime;

	private long intimeAICount;

	private long intimeAIAccumulator;

	private long dispatchableLostCount;

	public AcqStats(long diPeriod, long aiPeriod) {
		this.diPeriod = diPeriod;
		this.aiPeriod = aiPeriod;
		reset();
	}

	public void reset() {
		delayedDICount = 0;
		maximumDITime = diPeriod;
		intimeDICount = 0;
		intimeDIAccumulator = 0;
		delayedAICount = 0;
		maximumAITime = aiPeriod;
		intimeAICount = 0;
		intimeAIAccumulator = 0;
		dispatchableLostCount = 0;
	}

	public void dispatchableLost() {
		dispatchableLostCount++;
	}

	public long getDispatchableLostCount() {
		return dispatchableLostCount;
	}

	public void putAITime(long theoricStartTime, long effectiveStartTime,
			long effectiveEndTime) {
		long value = effectiveEndTime - theoricStartTime;
		if (value < aiPeriod) {
			intimeAICount++;
			intimeAIAccumulator += effectiveEndTime - effectiveStartTime;
		} else {
			delayedAICount++;
			if (value > maximumAITime)
				maximumAITime = value;
		}
	}

	public long getIntimeAICount() {
		return intimeAICount;
	}

	public double getIntimeAIAverage() {
		if (intimeAICount == 0)
			return 0.0;
		return ((double) intimeAIAccumulator) / intimeAICount;
	}

	public long getDelayedAICount() {
		return delayedAICount;
	}

	public long getMaximumAIDelay() {
		return maximumAITime - aiPeriod;
	}

	public void putDITime(long value) {
		if (value < diPeriod) {
			intimeDICount++;
			intimeDIAccumulator += value;
		} else {
			delayedDICount++;
			if (value > maximumDITime)
				maximumDITime = value;
		}
	}

	public long getIntimeDICount() {
		return intimeDICount;
	}

	public double getIntimeDIAverage() {
		if (intimeDICount == 0)
			return 0.0;
		return ((double) intimeDIAccumulator) / intimeDICount;
	}

	public long getDelayedDICount() {
		return delayedDICount;
	}

	public long getMaximumDIDelay() {
		return maximumDITime - diPeriod;
	}

}
