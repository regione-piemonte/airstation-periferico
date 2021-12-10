/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: page that represents a subdevice of an acquisition board
 * Change log:
 *   2008-04-18: initial version
 * ----------------------------------------------------------------------------
 * $Id: SubdeviceWidget.java,v 1.29 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.UIPage;

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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
/**
 * Page that represents a subdevice of an acquisition board
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class SubdeviceWidget extends UIPage {

	private String boardId = "";

	private String type = "";

	private String deviceId = "";

	private Label title = new Label();

	private final FlexTable table;

	private int selectedRow = -1;

	private int selectedColumn = -1;

	private Button senderButtonOpenListIOUser;

	public SubdeviceWidget() {

		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();

		panelButtonWidget.addButton(PerifericoUI.makeUpButton());
		panelButtonWidget.addButton(PerifericoUI.makeSaveButton());
		panelButtonWidget.addButton(PerifericoUI.makeReloadButton());
		panelButtonWidget.addButton(PerifericoUI
				.makeConfHelpButton("subdeviceWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().subdevice_title(), panelButtonWidget);

		// Label and panel for subdevice info
		title.setStyleName("gwt-Label-title");
		title.setText(PerifericoUI.getMessages().lbl_channel_info_title());

		// panel that contains subdevice info and buttons
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed");

		// Prepare info table's title
		FlexTable headerTable = new FlexTable();
		headerTable.setText(0, 0, PerifericoUI.getMessages().lbl_id());
		headerTable.setText(0, 1, PerifericoUI.getMessages().lbl_iouser());
		// headerTable.setText(0, 2, PerifericoUI.getMessages().lbl_range());
		headerTable.setText(0, 2, "");
		headerTable.setText(0, 3, PerifericoUI.getMessages().lbl_id());
		headerTable.setText(0, 4, PerifericoUI.getMessages().lbl_iouser());
		// headerTable.setText(0, 5, PerifericoUI.getMessages().lbl_range());
		headerTable.setText(0, 5, "");
		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "60px");
		headerTable.getCellFormatter().setWidth(0, 1, "230px");
		headerTable.getCellFormatter().setWidth(0, 2, "200px");
		headerTable.getCellFormatter().setWidth(0, 3, "60px");
		headerTable.getCellFormatter().setWidth(0, 4, "230px");
		headerTable.getCellFormatter().setWidth(0, 5, "200px");
		for (int j = 0; j < 6; j++)
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		panel.add(headerTable);

		// Prepare table for channel info in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("350px");
		panel.add(scrollPanel);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		externalPanel.add(title);
		externalPanel.add(panel);

		initWidget(externalPanel);

	} // end constructor

	void setFields(String boardIdStr, String typeStr, String deviceIdStr) {
		this.boardId = boardIdStr;
		this.type = typeStr;
		this.deviceId = deviceIdStr;
	}

	private void readFields() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] fieldsMatrix) {
				if (fieldsMatrix.length == 1
						&& fieldsMatrix[0].length == 1
						&& PerifericoUI.SESSION_ENDED
								.equals(fieldsMatrix[0][0])) {
					PerifericoUI.sessionEnded();
					return;
				}
				for (int i = 0; i < fieldsMatrix.length; i++) {
					for (int j = 0; j < 3; j++) {
						String value = (fieldsMatrix[i][j] != null ? fieldsMatrix[i][j]
								: " ");
						int column = j;
						if (i % 2 == 1)
							column = j + 3;
						// manage column that contains information about IOUser
						if (column == 1 || column == 4) {
							HorizontalPanel hPanelIOUser = new HorizontalPanel();
							/*
							 * hPanelIOUser
							 * .setStyleName("gwt-no-border-panel");
							 */
							table.setWidget((i / 2), column, hPanelIOUser);
							table.getCellFormatter().setAlignment((i / 2),
									column, HasHorizontalAlignment.ALIGN_LEFT,
									HasVerticalAlignment.ALIGN_MIDDLE);
							table.getCellFormatter().setStyleName((i / 2),
									column, "gwt-table-data");
							table.getCellFormatter().setWidth((i / 2), column,
									"230px");
							// case of value not null
							if (fieldsMatrix[i][j] != null) {
								Label ioUserLabel = new Label(value);
								hPanelIOUser
										.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
								Button unbindButton = new Button();
								unbindButton.setStyleName("gwt-button-delete");
								unbindButton.setTitle(PerifericoUI.getMessages()
										.subdevice_button_unbind_iouser());
								hPanelIOUser.add(unbindButton);
								hPanelIOUser.add(ioUserLabel);
								unbindButton
										.addClickHandler(new ClickHandler() {
											public void onClick(ClickEvent event) {
												Button button = (Button) event
														.getSource();
												boolean found = false;
												for (int k = 0; k < table
														.getRowCount()
														&& !found; k++) {
													HorizontalPanel hPanel2 = (HorizontalPanel) table
															.getWidget(k, 1);
													HorizontalPanel hPanel3 = (HorizontalPanel) table
															.getWidget(k, 4);
													if (hPanel2
															.getWidgetCount() == 2
															&& ((Button) hPanel2
																	.getWidget(0))
																	.equals(button)) {
														found = true;
														selectedRow = k;
														selectedColumn = 1;
														unbindChannel(table
																.getText(k, 0));
													} else if (hPanel3
															.getWidgetCount() == 2
															&& ((Button) hPanel3
																	.getWidget(0))
																	.equals(button)) {
														found = true;
														selectedRow = k;
														selectedColumn = 4;
														unbindChannel(table
																.getText(k, 3));
													}
												}
											}
										});
							} else {
								// create button for open list of bindable
								// IOUser
								Button openListIOUserButton = new Button();
								openListIOUserButton
										.setStyleName("gwt-button-list_iouser");
								openListIOUserButton
										.setTitle(PerifericoUI.getMessages()
												.subdevice_button_open_list_iouser());
								hPanelIOUser.add(openListIOUserButton);
								ClickHandler openListIOUserClickHandler = new ClickHandler() {
									public void onClick(ClickEvent event) {
										senderButtonOpenListIOUser = (Button) event
												.getSource();
										// check if session is ended
										PerifericoUIServiceAsync perifServiceSession = (PerifericoUIServiceAsync) GWT
												.create(PerifericoUIService.class);
										ServiceDefTarget endpointSession = (ServiceDefTarget) perifServiceSession;
										endpointSession
												.setServiceEntryPoint(GWT
														.getModuleBaseURL()
														+ "uiservice");
										AsyncCallback<Boolean> callbackSession = new UIAsyncCallback<Boolean>() {
											public void onSuccess(
													Boolean sessionEnded) {
												if (sessionEnded.booleanValue()) {
													PerifericoUI.sessionEnded();
												} else {
													Utils.blockForPopup("popup");
													boolean found = false;
													for (int k = 0; k < table
															.getRowCount()
															&& !found; k++) {
														HorizontalPanel hPanel2 = (HorizontalPanel) table
																.getWidget(k, 1);
														HorizontalPanel hPanel3 = (HorizontalPanel) table
																.getWidget(k, 4);
														if (hPanel2
																.getWidgetCount() == 1
																&& ((Button) hPanel2
																		.getWidget(0))
																		.equals(senderButtonOpenListIOUser)) {
															// Utils.alert("Ho
															// trovato
															// con
															// hPanel2");
															found = true;
															selectedRow = k;
															selectedColumn = 1;
														} else if (hPanel3
																.getWidgetCount() == 1
																&& ((Button) hPanel3
																		.getWidget(0))
																		.equals(senderButtonOpenListIOUser)) {
															// Utils.alert("Ho
															// trovato
															// con
															// hPanel3");
															found = true;
															selectedRow = k;
															selectedColumn = 4;
														}
													}
													// create and
													// show popup of
													// IOUser
													new IOUserWidget(
															false,
															true,
															boardId,
															type,
															deviceId,
															table.getText(
																	selectedRow,
																	(selectedColumn - 1)));
												}
											}
										};
										perifServiceSession
												.verifySessionEnded(callbackSession);
									}
								};
								openListIOUserButton
										.addClickHandler(openListIOUserClickHandler);
							}
						} else
							table.setText((i / 2), column, value);
						table.getCellFormatter().setStyleName((i / 2), column,
								"gwt-table-data");
						if (column == 0 || column == 3)
							table.getCellFormatter().setWidth((i / 2), column,
									"60px");
						else if (column == 1 || column == 4)
							table.getCellFormatter().setWidth((i / 2), column,
									"230px");
						else if (column == 2 || column == 5)
							table.getCellFormatter().setWidth((i / 2), column,
									"200px");
					}
				}
			}
		};

		perifService.setSubdeviceFields(boardId, type, deviceId, callback);
	}

	private void unbindChannel(String channelId) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resultString) {
				if (resultString != null
						&& resultString.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else {
					readFields();
				}
			}
		};

		boolean confirmResult = Window.confirm(PerifericoUI.getMessages()
				.confirm_unbind_channel());
		if (confirmResult)
			perifService.unbindChannel(boardId, type, deviceId, channelId,
					callback);
	} // end unbindChannel

	@Override
	protected void loadContent() {
		readFields();
	}

	@Override
	protected void reset() {
		selectedRow = -1;
		selectedColumn = -1;
		if (table == null)
			return;
		for (int i = 0; i < table.getRowCount(); i++)
			table.removeRow(0);
	}

}
