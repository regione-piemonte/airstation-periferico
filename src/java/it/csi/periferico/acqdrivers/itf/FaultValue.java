/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: represents a value acquired for an analyzer's fault,
//                  with timestamp
// Change log:
//   2008-10-07: initial version
// ----------------------------------------------------------------------------
// $Id: FaultValue.java,v 1.3 2015/06/23 15:53:03 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

import java.util.Date;
import java.util.List;

/**
 * Represents a value acquired for an analyzer's fault, with timestamp
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class FaultValue {

	private Date timestamp;

	private Boolean fault = null;

	private Integer value = null;

	public FaultValue(Date timestamp) {
		this(timestamp, null, 0, null, null);
	}

	public FaultValue(Date timestamp, Integer value, int codeForOK,
			Integer faultIgnoreMask, List<Integer> alternateCodesForOK) {
		this.timestamp = timestamp;
		this.value = value;
		if (value != null) {
			if (faultIgnoreMask != null)
				value = value & ~faultIgnoreMask;
			fault = value != codeForOK;
			if (alternateCodesForOK != null)
				for (Integer altCodeOK : alternateCodesForOK)
					if (value.equals(altCodeOK))
						fault = false;
		}
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Boolean getFault() {
		return fault;
	}

	public Integer getValue() {
		return value;
	}

	public boolean isStatusUnchanged(FaultValue other) {
		return other != null
				&& (fault == other.fault || (fault != null
						&& other.fault != null && fault.equals(other.fault)))
				&& (value == other.value || (value != null
						&& other.value != null && value.equals(other.value)));
	}

}
