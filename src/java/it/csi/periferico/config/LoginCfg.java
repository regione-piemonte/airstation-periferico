/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
/* ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: login configuration
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: LoginCfg.java,v 1.3 2009/04/14 16:06:46 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Login configuration
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class LoginCfg {

	private List<Password> passwordList = new ArrayList<Password>();

	public List<Password> getPasswordList() {
		return passwordList;
	}

	public void setPassword(List<Password> passwordList) {
		this.passwordList = passwordList;
	}

	public void printLoginCfg() {
		for (int i = 0; i < passwordList.size(); i++) {
			Password pwd = passwordList.get(i);
			pwd.printPassword();
		}
	}

}
