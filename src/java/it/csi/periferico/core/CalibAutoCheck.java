/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: automatic calibration check
// Change log:
//   2008-01-11: initial version
// ----------------------------------------------------------------------------
// $Id: CalibAutoCheck.java,v 1.10 2015/10/15 11:47:02 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.core;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.List;

/**
 * Automatic calibration check
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
// TODO: this class is only the starting point for future implementation
public class CalibAutoCheck extends ConfigItem {

	private static final long serialVersionUID = -8248020179461791065L;

	public enum Period {
		DAILY, WEEKLY, MONTHLY
	};

	private boolean enabled;

	private Period checkPeriod;

	private int dayOfCheck;

	private int startTime; // minute of day

	private List<CheckPoint> listPoints;

	private List<CheckReport> listReports;

	public Period getCheckPeriod() {
		return checkPeriod;
	}

	public void setCheckPeriod(Period checkPeriod) {
		this.checkPeriod = checkPeriod;
	}

	public String getCheckPeriodAsString() {
		return checkPeriod.toString().toLowerCase();
	}

	public void setCheckPeriodAsString(String str) {
		str = trim(str);
		this.checkPeriod = Period.valueOf(str.toUpperCase());
	}

	public int getDayOfCheck() {
		return dayOfCheck;
	}

	public void setDayOfCheck(int dayOfCheck) {
		this.dayOfCheck = dayOfCheck;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public List<CheckPoint> getListPoints() {
		return listPoints;
	}

	public void setListPoints(List<CheckPoint> listPoints) {
		this.listPoints = listPoints;
	}

	public boolean insertNewPoint(CheckPoint newPoint) {
		if (newPoint == null)
			throw new IllegalStateException("No new check point to insert");
		listPoints.add(newPoint);
		newPoint = null;
		return true;
	}

	public CheckPoint getPoint(int index) {
		if (index < 0 || index >= listPoints.size())
			return null;
		return listPoints.get(index);
	}

	public boolean deletePoint(int index) {
		if (index < 0 || index >= listPoints.size())
			return false;
		listPoints.remove(index);
		return true;
	}

	public void setConfig(boolean enabled, String strCheckPeriod,
			int dayOfCheck, int startTime) {
		setEnabled(enabled);
		setCheckPeriodAsString(strCheckPeriod);
		setDayOfCheck(dayOfCheck);
		setStartTime(startTime);
	}

	public boolean isSameConfig(boolean enabled, String strCheckPeriod,
			int dayOfCheck, int startTime) {
		return this.enabled == enabled
				&& this.checkPeriod.toString().toLowerCase() == trim(
						strCheckPeriod).toLowerCase()
				&& this.dayOfCheck == dayOfCheck && this.startTime == startTime;
	}

	@Override
	public void checkConfig() throws ConfigException {
		for (CheckPoint cp : listPoints)
			cp.checkConfig();
	}

	public List<CheckReport> getListReports() {
		return listReports;
	}

}

/*
 * TODO: Lista delle funzioni:
 * 
 * Esecuzione operazione di check della calibrazione: ad ogni passo eseguito da
 * questa funzione si controlla lo stato strumento e, in caso di attivazione di
 * operazioni manuali, si interrompe immediatamente la procedura, avendo cura di
 * riportare le elettrovalvole in posizione normale. La procedura opera nel modo
 * seguente: effettua un ciclo sui punti di calibrazione (CheckPoint, che sono
 * uno per bombola), apre la bombola, attende il tempo stabilito, effettua un
 * ciclo sui punti elemento (ElementPoint, uno per ogni elemento misurato dallo
 * strumento) leggendo il valore istantaneo per l'elemento, chiude la bombola.
 * La lettura del valore istantaneo avviene con la cadenza normalmente prevista
 * per l'elemento in questione. Durante tutta l'operazione di check della
 * calibrazione il flag “stato di check automatico in corso” dell'analizzatore
 * sarà attivo. Se anche uno solo degli elementi misurati risulta fuori
 * tolleranza su almeno un ElementPoint allora nell'analizzatore viene impostato
 * il flag “stato di strumento non tarato”; l'analizzatore può uscire da questo
 * stato dopo un nuovo check di calibrazione riuscito, dopo una calibrazione
 * manuale, dopo una manutenzione. Nota: la lettura dei dati dagli elementi
 * avviene registrando un ElementListener al momento di iniziare la lettura ed
 * effettuando la de-registrazione al momento di terminare la lettura.
 * 
 * Preparazione del report risultato controllo calibrazione: per ogni operazione
 * di check della calibrazione devono essere salvate le seguenti informazioni:
 * ora inizio, risultato (ok, fuori taratura, interrotta), per ogni CheckPoint
 * ora lettura, coppie valore letto / bombola, stato. I report di calibrazione
 * vengono gestiti dal DataManager come gli eventi di allarme.
 */

