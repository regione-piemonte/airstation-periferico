/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: voltage range for analog input
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: Range.java,v 1.9 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

/**
 * Voltage range for analog input
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class Range extends ConfigItem {

	private static final long serialVersionUID = -2725376179664894517L;

	private static final double EXTENSION_COEFFICIENT = 0.1;

	private double min;

	private double max;

	public Range() {
		min = max = 0;
	}

	public Range(double min, double max) throws ConfigException {
		check(min, max);
		this.min = min;
		this.max = max;
	}

	public Range(double min, double max, boolean minExtension,
			boolean maxExtension) throws ConfigException {
		check(min, max);
		double rangeExtension = (max - min) * EXTENSION_COEFFICIENT;
		this.min = min - (minExtension ? rangeExtension : 0.0);
		this.max = max + (maxExtension ? rangeExtension : 0.0);
	}

	public double getMin() {
		return (min);
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return (max);
	}

	public void setMax(double max) {
		this.max = max;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Range other = (Range) obj;
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
			return false;
		return Double.doubleToLongBits(min) == Double.doubleToLongBits(other.min);
	}

	public void setConfig(double min, double max) throws ConfigException {
		check(min, max);
		setMin(min);
		setMax(max);
	}

	public boolean isSameConfig(double min, double max) {
		return (this.min == min && this.max == max);
	}

	public double getWidth() {
		return max - min;
	}

	public boolean contains(Range other) {
		return this.min <= other.getMin() && this.max >= other.getMax();
	}

	@Override
	public void checkConfig() throws ConfigException {
		check(min, max);
	}

	private void check(double min, double max) throws ConfigException {
		if (min >= max)
			throw new ConfigException("max_min_value");
	}

	@Override
	public String toString() {
		return "[" + min + " ÷ " + max + "]";
	}

}
