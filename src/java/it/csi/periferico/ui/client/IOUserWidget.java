/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: dialog box for input/output association
 * Change log:
 *   2008-06-06: initial version
 * ----------------------------------------------------------------------------
 * $Id: IOUserWidget.java,v 1.14 2013/06/12 08:10:37 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dialog box for input/output association
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class IOUserWidget extends PopupPanel {

	private String boardId = "";

	private String type = "";

	private String deviceId = "";

	private String channelId = "";

	private IOUserWidget IOUserPopup;

	private VerticalPanel panel;

	private ListBox bindableIOUserListBox;

	public IOUserWidget(boolean autoHide, boolean modal, String boardIdSstr,
			String typeStr, String deviceIdStr, String channelIdStr) {
		super(autoHide, modal);
		IOUserPopup = this;
		boardId = boardIdSstr;
		type = typeStr;
		deviceId = deviceIdStr;
		channelId = channelIdStr;

		// panel that contains IOUser info and buttons
		panel = new VerticalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		// create listBox of bindable IOUser, calling
		// PerifericoService
		bindableIOUserListBox = new ListBox();
		bindableIOUserListBox.setWidth("300px");
		bindableIOUserListBox.setVisibleItemCount(7);
		panel.add(bindableIOUserListBox);
		bindableIOUserListBox.addItem(PerifericoUI.getMessages().select_iouser(),
				"0");
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] ioUsersList) {
				if (ioUsersList.length == 1
						&& PerifericoUI.SESSION_ENDED.equals(ioUsersList[0][0]))
					PerifericoUI.sessionEnded();
				else
					for (int k = 0; k < ioUsersList.length; k++)
						bindableIOUserListBox.addItem(ioUsersList[k][0],
								ioUsersList[k][1]);
			}
		};

		perifService.getBindableIOUsers(boardId, type, deviceId, channelId,
				callback);

		PanelButtonWidget panelButton = PerifericoUI.makeUndoSendPanelButton(
				new ClickHandler() {
					public void onClick(ClickEvent event) {
						Utils.unlockForPopup("popup");
						IOUserPopup.hide();
					}
				}, new ClickHandler() {
					public void onClick(ClickEvent event) {

						int indexSelected = bindableIOUserListBox
								.getSelectedIndex();
						String selectedValue = bindableIOUserListBox
								.getValue(indexSelected);
						if (selectedValue.equals("0"))
							Window.alert(PerifericoUI.getMessages()
									.iouser_error_select_item());
						else {
							PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
									.create(PerifericoUIService.class);
							ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
							endpoint.setServiceEntryPoint(GWT
									.getModuleBaseURL() + "uiservice");
							AsyncCallback<String> callback = new UIAsyncCallback<String>() {
								public void onSuccess(String resultString) {
									if (resultString != null
											&& resultString
													.equals(PerifericoUI.SESSION_ENDED))
										PerifericoUI.sessionEnded();
									else {
										PerifericoUI.getSubdeviceWidget().setFields(
												boardId, type, deviceId);
										PerifericoUI.getSubdeviceWidget()
												.loadContent();
									}
									Utils.sendVerifyOk();
								}
							};

							perifService.bindIOUser(boardId, type, deviceId,
									channelId, selectedValue, callback);
							Utils.unlockForPopup("popup");
							IOUserPopup.hide();
						}
					}
				});

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
		setPopupPosition(((cWidth / 2) - 180), ((cHeight / 2) - 100));
		setWidth("360px");
		setHeight("200px");
		setStyleName("gwt-popup-panel");
		// DOM.setStyleAttribute(getElement(), "border", " 1px solid #FF8D17");
	}

}
