/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for analyzers' drivers
// Change log:
//   2015-04-01: initial version
// ----------------------------------------------------------------------------
// $Id: DriverService.java,v 1.3 2015/09/24 16:19:17 pfvallosio Exp $
// ----------------------------------------------------------------------------
package it.csi.periferico.acqdrivers.itf;

import java.util.List;

/**
 * 
 * Interface for analyzers' drivers.Questa interfaccia definisce le funzioni
 * necessarie per la gestione del driver da parte del software Periferico.
 * Consente di ottenere il nome e la versione del driver, di leggere le
 * configurazioni del driver, di sapere se un analizzatore è supportato dal
 * driver e di istanziare il driver per ciascun analizzatore che lo deve
 * utilizzare. Infatti, è necessario che l’applicazione Periferico possa avere
 * un thread di gestione per ciascun analizzatore con interfaccia dati, quindi
 * occorre un’istanza del driver per ciascun analizzatore che dovrà essere
 * gestito dal driver.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface DriverService {

	public String getName();

	public DriverVersion getDriverVersion();

	public void readDriverConfigs() throws DriverConfigException;

	public List<? extends DriverCfg> getDriverConfigs();

	public List<ConfigInfo> getConfigsInfo();

	public boolean isSupported(AnalyzerInterface analyzerInterface);

	public DriverInterface newDriverInstance(AnalyzerInterface analyzerInterface)
			throws DriverConfigException;

}
