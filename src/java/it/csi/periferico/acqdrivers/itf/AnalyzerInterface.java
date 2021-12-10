/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for analyzers
// Change log:
//   2008-10-02: initial version
// ----------------------------------------------------------------------------
// $Id: AnalyzerInterface.java,v 1.5 2015/09/17 11:08:58 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

import java.util.List;

/**
 * Interface for analyzers. Questa interfaccia definisce le funzioni per
 * ottenere marca e modello dell’analizzatore, le informazioni relative al tipo
 * di connessione dell’analizzatore e ai parametri di configurazione della
 * connessione. Definisce anche le funzioni per ottenere i parametri di
 * inizializzazione del driver. Da notare che questi parametri possono essere
 * diversi per ogni esemplare dell’analizzatore gestito dal driver, per cui non
 * devono far parte dell’eventuale configurazione del driver, ma devono essere
 * associate alla configurazione dell’analizzatore. Il software Periferico è
 * predisposto per gestire i parametri in questo modo e li rende disponibili al
 * driver.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface AnalyzerInterface {

	public String getBrand();

	public String getModel();

	public PortType getPortType();

	public String getHostName();

	public Integer getIpPort();

	public String getTtyDevice();

	public Integer getTtyBaudRate();

	public String getTtyParams();

	public String getDriverParams();

	public String getPassword();

	public AlarmInterface getAlarmInterface();

	public List<ElementInterface> getElementInterfaces();

	public Object getId();

	public boolean isActive();

	public boolean isConnectionUp();

}
