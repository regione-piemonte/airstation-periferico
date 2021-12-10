/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: utility functions
// Change log:
//   2008-04-09: initial version
// ----------------------------------------------------------------------------
// $Id: PerifericoUtil.java,v 1.5 2011/10/19 10:58:51 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility functions
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class PerifericoUtil {

	public static boolean areEqual(Object obj1, Object obj2) {
		if (obj1 == obj2)
			return true;
		if (obj1 == null || obj2 == null)
			return false;
		return obj1.equals(obj2);
	}

	public static Object copy(Object objOriginal) throws IOException,
			ClassNotFoundException {
		if (objOriginal == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(objOriginal);
		oos.flush();
		oos.close();
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object objCopy = ois.readObject();
		return (objCopy);
	}

	public static boolean areInSameSecond(Date date1, Date date2) {
		if (date1 == null || date2 == null)
			return false;
		Calendar c1 = new GregorianCalendar();
		c1.setTime(date1);
		Calendar c2 = new GregorianCalendar();
		c2.setTime(date2);
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
				&& c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
				&& c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY)
				&& c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE)
				&& c1.get(Calendar.SECOND) == c2.get(Calendar.SECOND);
	}

	public static String formatDouble(Double value, NumberFormat nf) {
		if (value == null)
			return "";
		if (nf == null)
			return value.toString();
		String result = nf.format(value);
		if (result.matches("^-0[\\.,]?0*$"))
			result = result.substring(1);
		return result;
	}

}
