/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: dialog box for saving the configuration
 * Change log:
 *   2008-11-21: initial version
 * ----------------------------------------------------------------------------
 * $Id: SaveWidget.java,v 1.15 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dialog box for saving the configuration
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class SaveWidget extends PopupPanel {

	private SaveWidget savePopup;

	private VerticalPanel panel;

	private TextArea textComment;

	private TextBox textUser;

	public SaveWidget(boolean autoHide, boolean modal) {
		super(autoHide, modal);
		savePopup = this;

		// panel that contains IOUser info and buttons
		panel = new VerticalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		Label lblMessage = new Label();
		lblMessage.setText(PerifericoUI.getMessages().lbl_insert_comment());
		panel.add(lblMessage);

		FlexTable table = new FlexTable();
		table.getCellFormatter().setWidth(0, 0, "150px");
		table.getCellFormatter().setWidth(0, 1, "320px");
		table.setCellPadding(2);
		table.setCellSpacing(2);

		Label lblComment = new Label();
		lblComment.setText(PerifericoUI.getMessages().lbl_comment());
		table.setWidget(0, 0, lblComment);
		textComment = new TextArea();
		textComment.setWidth("180px");
		textComment.setHeight("50px");
		textComment.setStyleName("gwt-bg-text-orange");

		table.setWidget(0, 1, textComment);
		panel.add(table);

		Label lblAuthor = new Label();
		lblAuthor.setText(PerifericoUI.getMessages().lbl_author());
		table.setWidget(1, 0, lblAuthor);
		textUser = new TextBox();
		textUser.setStyleName("gwt-bg-text-orange");
		textUser.setWidth("180px");
		table.setWidget(1, 1, textUser);

		PanelButtonWidget panelButton = new PanelButtonWidget();

		final AsyncCallback<String> acb_getStationName = new UIAsyncCallback<String>() {
			public void onSuccess(String stationName) {
				Window.setTitle(PerifericoUI.getMessages().lbl_station() + ": "
						+ stationName);
			}
		};

		// button send/verify
		ClickHandler saveListener = new ClickHandler() {
			public void onClick(ClickEvent event) {
				Utils.unlockForPopup("popup");
				savePopup.hide();
				Utils.blockForPopup("loading");
				final PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] resultString) {
						Utils.unlockForPopup("loading");
						if (resultString[0].equals(PerifericoUI.SESSION_ENDED)) {
							PerifericoUI.sessionEnded();
							return;
						}
						if (resultString[0].equals(ConfigResults.ACTIVATED)) {
							Window.alert(PerifericoUI.getMessages().alert_save());
							perifService.getStationName(acb_getStationName);
							PerifericoUI.updateCurrentPage(null);
						} else if (resultString[0]
								.equals(ConfigResults.ACTIVATION_ERROR)) {
							Window.alert(PerifericoUI.getMessages()
									.alert_activation_error()
									+ (resultString.length == 1 ? "" : " "
											+ resultString[1]));

							PerifericoUI.getNavBar().setBarForView();
							PerifericoUI.getSlotConfig().setVisible(false);
							PerifericoUI.getSlotView().setVisible(true);
							PerifericoUI
									.setCurrentPage(PerifericoUI.getInformaticStatusWidget());
						} else if (resultString[0]
								.equals(ConfigResults.OBSOLETE)) {
							boolean choice = Window
									.confirm(PerifericoUI.getMessages()
											.alert_obsolete()
											+ (resultString.length == 1 ? ""
													: " " + resultString[1]));
							if (choice) {
								perifService.setStationCfg(true,
										textComment.getText(),
										textUser.getText(), this);
							}
						} else if (resultString[0]
								.equals(ConfigResults.HISTORIC)) {
							boolean choice = Window
									.confirm(PerifericoUI.getMessages()
											.alert_historic()
											+ (resultString.length == 1 ? ""
													: " " + resultString[1]));
							if (choice) {
								perifService.setStationCfg(true,
										textComment.getText(),
										textUser.getText(), this);
							}
						} else if (resultString[0]
								.equals(ConfigResults.CHECK_ERROR)) {
							Window.alert(PerifericoUI.getMessages()
									.alert_check_error()
									+ (resultString.length == 1 ? "" : " "
											+ resultString[1]));
						} else if (resultString[0]
								.equals(ConfigResults.SAVE_FORBIDDEN)) {
							Window.alert(PerifericoUI.getMessages().save_forbidden());
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						Utils.unlockForPopup("loading");
						super.onFailure(caught);
					}
				};

				perifService.setStationCfg(false, textComment.getText(),
						textUser.getText(), callback);
			}
		};
		Button sendVerifyButton = PerifericoUI
				.makeSendVerifyButton(saveListener);
		sendVerifyButton.setTitle(PerifericoUI.getMessages().ok());

		// button undo
		Button undoButton = PerifericoUI.makeUndoButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Utils.unlockForPopup("popup");
				savePopup.hide();
			}
		});

		panelButton.addButton(undoButton);
		panelButton.addButton(sendVerifyButton);

		panel.add(panelButton);

		this.add(panel);
		this.show();

	} // end constructor

	public void show() {
		super.show();

		int cWidth = Window.getClientWidth();
		int cHeight = Window.getClientHeight();
		// int myWidth = getOffsetWidth();
		// int myHeight = getOffsetHeight();
		// Utils.alert("clientwidth:"+cWidth+" clientHeight:"+cHeight+"
		// offsetwidth:"+myWidth+" offsetHeight:"+myHeight);
		// setPopupPosition((cWidth-myWidth)/2,(cHeight-myHeight)/2);
		setPopupPosition(((cWidth / 2) - 150), ((cHeight / 2) - 100));
		setWidth("300px");
		setHeight("200px");
		setStyleName("gwt-popup-panel");
		// DOM.setStyleAttribute(getElement(), "border", " 1px solid #FF8D17");
	}

}
