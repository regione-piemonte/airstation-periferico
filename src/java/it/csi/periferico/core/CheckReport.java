/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: calibration check report
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: CheckReport.java,v 1.4 2009/04/15 13:11:55 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import java.util.Date;
import java.util.List;

/**
 * Calibration check report
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public class CheckReport {

	public enum Status {
		OK, CHECK_FAILED, INTERRUPTED
	}

	private Analyzer analyzer;

	private Date startTime;

	private Status status;

	private List<PointReport> listPointReports;

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Status getStatus() {
		return status;
	}

	public List<PointReport> getListPointReports() {
		return listPointReports;
	}

}
