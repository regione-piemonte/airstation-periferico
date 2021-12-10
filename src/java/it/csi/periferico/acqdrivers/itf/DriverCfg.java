/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: configuration for data port driver
// Change log:
//   2008-09-29: initial version
// ----------------------------------------------------------------------------
// $Id: DriverCfg.java,v 1.7 2015/11/17 15:35:47 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration for data port driver
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class DriverCfg {

	private String version = null;
	private List<AnalyzerName> analyzerNameList = new ArrayList<AnalyzerName>();
	private boolean networkInterfaceOnly = false;
	private Integer defaultIpPort = null;
	private Integer defaultTtyBaudRate = null;
	private List<Integer> supportedTtyBaudRates = new ArrayList<Integer>();
	private String defaultTtyParams = "8N1H";
	private String defaultDriverParams = "";
	private int faultCodeForOK = 0;
	private Integer faultIgnoreMask = null;
	private List<Integer> alternateFaultCodeForOK = null;
	private Integer ignoreCommFailureInterval_s = null;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<AnalyzerName> getAnalyzerNameList() {
		return analyzerNameList;
	}

	public void setAnalyzerNameList(List<AnalyzerName> analyzerNameList) {
		this.analyzerNameList = analyzerNameList;
	}

	public abstract List<? extends ElementCfg> getElementCfgList();

	public Integer getDefaultIpPort() {
		return defaultIpPort;
	}

	public void setDefaultIpPort(Integer defaultIpPort) {
		this.defaultIpPort = defaultIpPort;
	}

	public Integer getDefaultTtyBaudRate() {
		return defaultTtyBaudRate;
	}

	public void setDefaultTtyBaudRate(Integer defaultTtyBaudRate) {
		this.defaultTtyBaudRate = defaultTtyBaudRate;
	}

	public String getDefaultTtyParams() {
		return defaultTtyParams;
	}

	public void setDefaultTtyParams(String defaultTtyParams) {
		this.defaultTtyParams = defaultTtyParams;
	}

	public String getDefaultDriverParams() {
		return defaultDriverParams;
	}

	public void setDefaultDriverParams(String defaultDriverParams) {
		this.defaultDriverParams = defaultDriverParams;
	}

	public boolean isNetworkInterfaceOnly() {
		return networkInterfaceOnly;
	}

	public void setNetworkInterfaceOnly(boolean networkInterfaceOnly) {
		this.networkInterfaceOnly = networkInterfaceOnly;
	}

	public List<Integer> getSupportedTtyBaudRates() {
		return supportedTtyBaudRates;
	}

	public void setSupportedTtyBaudRates(List<Integer> supportedTtyBaudRates) {
		this.supportedTtyBaudRates = supportedTtyBaudRates;
	}

	public ElementCfg getElementCfg(String parameterId) {
		for (ElementCfg cfg : getElementCfgList()) {
			if (cfg.getParameterId().equals(parameterId))
				return cfg;
		}
		return null;
	}

	public int getFaultCodeForOK() {
		return faultCodeForOK;
	}

	public void setFaultCodeForOK(int faultCodeForOK) {
		this.faultCodeForOK = faultCodeForOK;
	}

	public Integer getFaultIgnoreMask() {
		return faultIgnoreMask;
	}

	public void setFaultIgnoreMask(Integer faultIgnoreMask) {
		this.faultIgnoreMask = faultIgnoreMask;
	}

	public List<Integer> getAlternateFaultCodeForOK() {
		return alternateFaultCodeForOK;
	}

	public void setAlternateFaultCodeForOK(List<Integer> alternateFaultCodeForOK) {
		this.alternateFaultCodeForOK = alternateFaultCodeForOK;
	}

	public Integer getIgnoreCommFailureInterval_s() {
		return ignoreCommFailureInterval_s;
	}

	public void setIgnoreCommFailureInterval_s(
			Integer ignoreCommFailureInterval_s) {
		this.ignoreCommFailureInterval_s = ignoreCommFailureInterval_s;
	}

	public void check(Set<String> parameterIdSet) throws DriverConfigException {
		for (ElementCfg ec : getElementCfgList()) {
			if (!parameterIdSet.contains(ec.getParameterId())) {
				throw new DriverConfigException("Parameter id "
						+ ec.getParameterId()
						+ " is not contained in common configuration");
			}
		}
	}

	public Set<String> getParameterIdSet() {
		Set<String> set = new HashSet<String>();
		for (ElementCfg ec : getElementCfgList())
			set.add(ec.getParameterId());
		return set;
	}

}
