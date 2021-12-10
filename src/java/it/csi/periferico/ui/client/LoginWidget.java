/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: login page
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: LoginWidget.java,v 1.21 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.UIPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A composite Widget that implements the login interface for the periferico.
 * 
 * @author Isabella Vespa - CSI Piemonte (isabella.vespa@csi.it)
 * 
 */

public class LoginWidget extends UIPage {

	private PasswordTextBox passwordTextBox;

	private Button loginButton;

	public LoginWidget() {

		final PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		((ServiceDefTarget) perifService).setServiceEntryPoint(GWT
				.getModuleBaseURL() + "uiservice");

		DockPanel panelPage = new DockPanel();
		panelPage.setWidth("960px");
		panelPage.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final Label titleLabel = new Label();
		titleLabel.setText(PerifericoUI.getMessages().application_title());
		titleLabel.setStyleName("gwt-h1");
		panelPage.add(titleLabel, DockPanel.NORTH);

		AsyncCallback<String> acb_getPerifericoVersion = new UIAsyncCallback<String>() {
			public void onSuccess(String version) {
				titleLabel.setText(PerifericoUI.getMessages()
						.application_title_version() + " " + version);
			}
		};

		perifService.getPerifericoVersion(acb_getPerifericoVersion);

		AsyncCallback<String> acb_getStationName = new UIAsyncCallback<String>() {
			public void onSuccess(String stationName) {
				Window.setTitle(PerifericoUI.getMessages().lbl_station() + ": "
						+ stationName);
			}
		};

		perifService.getStationName(acb_getStationName);

		final AsyncCallback<Boolean> acb_verifyLogin = new UIAsyncCallback<Boolean>() {
			public void onSuccess(Boolean result) {
				if (result.booleanValue()) {
					PerifericoUI.getNavBar().setVisible(true);
					PerifericoUI.getNavBar().setBarForView();
					PerifericoUI.getSlotPage().setVisible(false);
					if (PerifericoUI.getSlotView() != null) {
						PerifericoUI.getSlotView().setVisible(true);
						PerifericoUI
								.setCurrentPage(PerifericoUI.getInformaticStatusWidget());
					}
					// PerifericoUI.footerWidget.setVisible(false);
					AsyncCallback<Object> acb_getConfig = new UIAsyncCallback<Object>() {
						public void onSuccess(Object result) {
						}
					};
					perifService.getConfig(PerifericoUI.getLocale(), acb_getConfig);
				} else
					Window.alert(PerifericoUI.getMessages().lbl_login_error());
			}
		};

		Label loginLabel = new Label();
		loginLabel.setText(PerifericoUI.getMessages().lbl_login_title());
		loginLabel.setStyleName("gwt-label-login");
		VerticalPanel externalPanel = new VerticalPanel();
		externalPanel.add(loginLabel);

		VerticalPanel vPanel = new VerticalPanel();
		vPanel.setWidth("300px");
		vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vPanel.setStyleName("gwt-login-boxed");

		passwordTextBox = new PasswordTextBox();
		passwordTextBox.setStyleName("gwt-bg-text-blue");
		// passwordTextBox.setTabIndex(1);
		// passwordTextBox.setFocus(true);
		passwordTextBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				int nativeCode = event.getNativeEvent().getKeyCode();
				if (nativeCode == KeyCodes.KEY_ENTER) {
					perifService.verifyLogin(passwordTextBox.getText(),
							acb_verifyLogin);
					passwordTextBox.setText("");
				}
			}
		});
		vPanel.add(passwordTextBox);
		vPanel.setCellHeight(passwordTextBox, "30px");

		loginButton = new Button();
		loginButton.setStyleName("gwt-button-login");
		loginButton.setTitle(PerifericoUI.getMessages().button_login());
		loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				perifService.verifyLogin(passwordTextBox.getText(),
						acb_verifyLogin);
				passwordTextBox.setText("");
			}
		});

		// loginButton.setTabIndex(2);
		vPanel.add(loginButton);
		vPanel.setCellHeight(loginButton, "50px");
		vPanel.setCellVerticalAlignment(loginButton,
				HasVerticalAlignment.ALIGN_MIDDLE);

		externalPanel.add(vPanel);

		panelPage.add(externalPanel, DockPanel.CENTER);

		initWidget(panelPage);

	}

	@Override
	protected void reset() {
		passwordTextBox.setFocus(true);
		passwordTextBox.setTabIndex(1);
		loginButton.setTabIndex(2);
	}

	@Override
	protected void loadContent() {
	}

}// end class
