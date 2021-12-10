/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents an acquisition board's analog input
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: AnalogInput.java,v 1.14 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;

import java.util.List;

/**
 * Represents an acquisition board's analog input
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class AnalogInput extends Channel {

	private static final long serialVersionUID = 5374471206799907401L;

	private List<Range> listRange = null;

	private AnalogInput differentialModeChannelHigh = null;

	private AnalogInput differentialModeChannelLow = null;

	private Range activeRange = null;

	private boolean differentialModeActive = false;

	private volatile boolean rangeSetToBoard = false;

	public AnalogInput(AISubdevice subdevice, int channel) {
		super(subdevice, channel);
	}

	public Range getActiveRange() {
		return activeRange;
	}

	public void setActiveRange(Range range) {
		if (activeRange == null || !activeRange.equals(range)) {
			activeRange = range;
			rangeSetToBoard = false;
		}
	}

	public List<Range> getListRange() {
		return listRange;
	}

	public void setListRange(List<Range> listRange) {
		this.listRange = listRange;
	}

	public Range findCompatibleRange(Range requestedRange) {
		if (listRange == null || requestedRange == null)
			return null;
		double minDiff = Double.MAX_VALUE;
		Range bestRange = null;
		for (Range r : listRange) {
			if (r.contains(requestedRange)) {
				double diff = r.getWidth() - requestedRange.getWidth();
				if (diff < minDiff) {
					minDiff = diff;
					bestRange = r;
				}
			}
		}
		return bestRange;
	}

	public AnalogInput getDifferentialModeChannelHigh() {
		return differentialModeChannelHigh;
	}

	public void setDifferentialModeChannelHigh(
			AnalogInput differentialModeChannelHigh) {
		if (differentialModeChannelLow != null)
			throw new IllegalStateException(
					"Cannot set differential mode channel high on channel high");
		this.differentialModeChannelHigh = differentialModeChannelHigh;
	}

	public AnalogInput getDifferentialModeChannelLow() {
		return differentialModeChannelLow;
	}

	public void setDifferentialModeChannelLow(
			AnalogInput differentialModeChannelLow) {
		if (differentialModeChannelHigh != null)
			throw new IllegalStateException(
					"Cannot set differential mode channel low on channel low");
		this.differentialModeChannelLow = differentialModeChannelLow;
	}

	public boolean isDifferentialModeSupported() {
		return differentialModeChannelHigh != null
				|| differentialModeChannelLow != null;
	}

	public boolean isDifferentialModeAvailable() {
		if (differentialModeChannelHigh == null
				&& differentialModeChannelLow != null)
			return !differentialModeChannelLow.isBinded();
		else if (differentialModeChannelLow == null
				&& differentialModeChannelHigh != null)
			return !differentialModeChannelHigh.isBinded();
		return false;
	}

	public boolean isDifferentialModeActive() {
		return differentialModeActive;
	}

	public void setDifferentialModeActive(boolean differentialModeActive) {
		this.differentialModeActive = differentialModeActive;
	}

	@Override
	public void bindIOUser(IOUser ioUser) throws BoardsException {
		if (ioUser == null)
			throw new IllegalArgumentException("AIUser must not be null");
		if (!(ioUser instanceof AIUser))
			throw new IllegalArgumentException("IOUser must be of calss AIUser");
		AIUser aiUser = (AIUser) ioUser;
		Range range = null;
		try {
			range = new Range(aiUser.getMinVoltage(), aiUser.getMaxVoltage(),
					aiUser.getMinRangeExtension(),
					aiUser.getMaxRangeExtension());
		} catch (ConfigException e) {
			throw new IllegalStateException(
					"Found AIUser with invalid voltage range: min="
							+ aiUser.getMinVoltage() + " max="
							+ aiUser.getMaxVoltage());
		}
		setActiveRange(findCompatibleRange(range));
		if (activeRange == null) {
			throw new BoardsException("The requested range " + range
					+ " is not supported");
		}
		if (aiUser.isDifferentialModeNeeded()) {
			if (!isDifferentialModeSupported()) {
				throw new BoardsException(
						"Differential mode requested but not supported");
			}
			differentialModeActive = true;
			if (differentialModeChannelHigh != null) {
				differentialModeChannelHigh.setIOUser(aiUser);
				differentialModeChannelHigh.setDifferentialModeActive(true);
				differentialModeChannelHigh.setActiveRange(activeRange);
			}
			if (differentialModeChannelLow != null) {
				differentialModeChannelLow.setIOUser(aiUser);
				differentialModeChannelLow.setDifferentialModeActive(true);
				differentialModeChannelLow.setActiveRange(activeRange);
			}
		}
		super.bindIOUser(aiUser);
	}

	@Override
	public void unbind() {
		AIUser aiUser = (AIUser) getBindedIOUser();
		if (aiUser == null)
			return;
		super.unbind();
		if (differentialModeActive) {
			differentialModeActive = false;
			if (differentialModeChannelHigh != null)
				differentialModeChannelHigh.unbind();
			if (differentialModeChannelLow != null)
				differentialModeChannelLow.unbind();
		}
	}

	public boolean isRangeSupported(double rangeLow, double rangeHigh,
			boolean minRangeExtension, boolean maxRangeExtension) {
		try {
			Range range = new Range(rangeLow, rangeHigh, minRangeExtension,
					maxRangeExtension);
			return findCompatibleRange(range) != null;
		} catch (ConfigException e) {
			return false;
		}
	}

	public boolean isRangeSetToBoard() {
		return rangeSetToBoard;
	}

	public void onRangeSetToBoard() {
		rangeSetToBoard = true;
	}

}
