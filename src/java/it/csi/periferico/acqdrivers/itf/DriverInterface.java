/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: interface for the drivers of the analyzers with data port
// Change log:
//   2015-04-09: initial version
// ----------------------------------------------------------------------------
// $Id: DriverInterface.java,v 1.5 2015/09/24 16:19:17 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

import java.util.Date;
import java.util.List;

/**
 *
 * Interface for the drivers of the analyzers with data port. Questa interfaccia
 * definisce le funzioni del driver vero e proprio, ovvero tutte le funzioni che
 * devono essere implementate per leggere i dati e lo stato dagli analizzatori,
 * nonché per leggere i parametri di processo, impostare la data
 * dell’analizzatore, per inviare comandi, visualizzare il contenuto del
 * display, etc... Questa interfaccia definisce anche le funzioni per
 * controllare lo stato di funzionamento del driver, avviare o fermare
 * l’acquisizione dati, verificare lo stato della comunicazione con
 * l’analizzatore.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface DriverInterface {

	public void start();

	public void stop();

	public boolean isRunning();

	public boolean isFailed();

	public boolean isConnectionOK();

	public void enableAcquisition();

	public void disableAcquisition();

	public boolean isAcquisitionEnabled();

	public void testCommunication(DriverCallback<Boolean> callback);

	public void getDate(DriverCallback<Date> callback);

	public void setDate(DriverCallback<Date> callback);

	public void getSerialNumber(DriverCallback<String> callback);

	public void getParameters(DriverCallback<List<AnalyzerParameter>> callback);

	public void showMeasuredParameters(DriverCallback<List<String>> callback);

	public void showProcessParameters(DriverCallback<List<String>> callback);

	public void readParameter(String id, DriverCallback<ElementValue> callback);

	public void getFaults(DriverCallback<List<AnalyzerFault>> callback);

	public void showFaults(DriverCallback<List<String>> callback);

	public void isFaultActive(String id, DriverCallback<Boolean> callback);

	public void resetFault(String id, DriverCallback<Boolean> callback);

	public void readFaultStatus(DriverCallback<FaultValue> callback);

	public void resetAllFaults(DriverCallback<FaultValue> callback);

	public boolean isRemoteUISupported();

	// Returns image file extension or null if Graphical UI is not supported
	public String isRemoteGUISupported();

	public List<String> getKeyList();

	public void sendKey(String key, DriverCallback<List<String>> callback);

	public void readDisplay(DriverCallback<List<String>> callback);

	public void readDisplayImage(DriverCallback<byte[]> callback);

	public boolean isCustomCommandSupported();

	public List<CustomCommand> getCommandList();

	public void sendCommand(String[] command,
			DriverCallback<List<String>> callback);

}
