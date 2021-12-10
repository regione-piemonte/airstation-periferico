/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: element interface for data port drivers
// Change log:
//   2008-10-02: initial version
// ----------------------------------------------------------------------------
// $Id: ElementInterface.java,v 1.2 2015/05/27 15:01:05 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.acqdrivers.itf;

/**
 * Element interface for data port drivers. Questa interfaccia definisce le
 * funzioni per gli elementi (inquinanti o altre grandezze fisiche) da
 * acquisire. Permette di leggere le informazioni relative alle unità di misura,
 * il periodo di acquisizione, la grandezza misurata. Definisce anche la
 * funzione con cui vengono ‘immessi’ nell’applicazione i valori acquisiti per
 * la grandezza misurata.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface ElementInterface {

	public String getParameterId();

	public boolean isActive();

	public boolean isReady();

	public int getAcqPeriod();

	public String getAnalyzerMeasureUnitName();

	public String getMeasureUnitName();

	public boolean deliver(ElementValue value);

}
