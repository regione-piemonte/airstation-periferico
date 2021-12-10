/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: station configuration page
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: StationWidget.java,v 1.67 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */

package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.AsyncPageOperation;
import it.csi.periferico.ui.client.pagecontrol.UIPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A composite Widget that implements the station interface for the periferico.
 * 
 * @author Isabella Vespa - CSI Piemonte (isabella.vespa@csi.it)
 * 
 */

public class StationWidget extends UIPage {

	private TextBox textNickname;

	private TextBox textName;

	private TextBox textLocation;

	private TextBox textAddress;

	private TextBox textCity;

	private TextBox textProvince;

	private TextArea textUserNotes;

	private CheckBox checkUseGps;

	private ListBox connectionType;

	private TextBox textTimeOut;

	private NumericKeyPressHandler positiveKeyboardListener = new NumericKeyPressHandler(
			true, true);

	// Grids for station information.
	private final FlexTable anagraphicGrid = new FlexTable();

	// Grids for connection information.
	private final FlexTable connectionGrid = new FlexTable();

	private TextBox textCopIp;

	private CheckBox checkBoxOutgoingCallEnabled;

	private TextBox textMaxConnectionRetry;

	public StationWidget() {

		PanelButtonWidget panelButton1 = new PanelButtonWidget();
		panelButton1.addButton(PerifericoUI.makeSaveButton());
		panelButton1.addButton(PerifericoUI.makeReloadButton());
		panelButton1.addButton(PerifericoUI
				.makeConfHelpButton("stationWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().station_title(), panelButton1);

		/*
		 * Anagraphic info
		 */

		// Label and panel for anagraphic info
		Label anagraphicTitle = new Label();
		anagraphicTitle.setText(PerifericoUI.getMessages().lbl_station_title());
		anagraphicTitle.setStyleName("gwt-Label-title");

		// panel that contains the anagraphic grid
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed");
		panel.add(anagraphicGrid);

		// format the grid for anagraphic info
		anagraphicGrid.getCellFormatter().setWidth(0, 0, "150px");
		anagraphicGrid.getCellFormatter().setWidth(0, 1, "320px");
		anagraphicGrid.getCellFormatter().setWidth(0, 2, "150px");
		anagraphicGrid.getCellFormatter().setWidth(0, 3, "320px");
		anagraphicGrid.setCellPadding(5);
		anagraphicGrid.setCellSpacing(5);

		// Put values in the anagraphic grid cells.
		String message = PerifericoUI.getMessages().lbl_station_nickname();
		anagraphicGrid.setText(0, 0, message);
		textNickname = new TextBox();
		textNickname.setStyleName("gwt-bg-text-orange");
		textNickname.setWidth("270px");
		textNickname.setMaxLength(16);
		anagraphicGrid.setWidget(0, 1, textNickname);
		textNickname.setName("nickname");

		message = PerifericoUI.getMessages().lbl_station_name();
		anagraphicGrid.setText(1, 0, message);
		textName = new TextBox();
		textName.setStyleName("gwt-bg-text-orange");
		textName.setWidth("270px");
		anagraphicGrid.setWidget(1, 1, textName);
		textName.setName("name");

		message = PerifericoUI.getMessages().lbl_station_location();
		anagraphicGrid.setText(2, 0, message);
		textLocation = new TextBox();
		textLocation.setStyleName("gwt-bg-text-orange");
		textLocation.setWidth("270px");
		anagraphicGrid.setWidget(2, 1, textLocation);
		textLocation.setName("location");

		message = PerifericoUI.getMessages().lbl_station_address();
		anagraphicGrid.setText(3, 0, message);
		textAddress = new TextBox();
		textAddress.setStyleName("gwt-bg-text-orange");
		textAddress.setWidth("270px");
		anagraphicGrid.setWidget(3, 1, textAddress);
		textAddress.setName("address");

		message = PerifericoUI.getMessages().lbl_station_city();
		anagraphicGrid.setText(4, 0, message);
		textCity = new TextBox();
		textCity.setStyleName("gwt-bg-text-orange");
		textCity.setWidth("270px");
		anagraphicGrid.setWidget(4, 1, textCity);
		textCity.setName("city");

		message = PerifericoUI.getMessages().lbl_station_province();
		anagraphicGrid.setText(5, 0, message);
		textProvince = new TextBox();
		textProvince.setStyleName("gwt-bg-text-orange");
		textProvince.setWidth("270px");
		anagraphicGrid.setWidget(5, 1, textProvince);
		textProvince.setName("province");

		message = PerifericoUI.getMessages().lbl_station_userNotes();
		anagraphicGrid.setText(0, 2, message);
		textUserNotes = new TextArea();
		textUserNotes.setStyleName("gwt-bg-text-orange");
		anagraphicGrid.setWidget(0, 3, textUserNotes);
		anagraphicGrid.getFlexCellFormatter().setRowSpan(0, 3, 5);
		anagraphicGrid.getFlexCellFormatter().setAlignment(0, 3,
				HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_TOP);
		textUserNotes.setName("usernotes");
		textUserNotes.setWidth("300px");
		textUserNotes.setHeight("120px");

		message = PerifericoUI.getMessages().lbl_station_gps();
		anagraphicGrid.setText(5, 2, message);
		checkUseGps = new CheckBox();
		anagraphicGrid.setWidget(5, 3, checkUseGps);

		// undo send panel for anagraphic info
		PanelButtonWidget panelButton = PerifericoUI.makeUndoSendPanelButton(
				new ClickHandler() {
					public void onClick(ClickEvent event) {
						// check if session is ended
						PerifericoUIServiceAsync perifServiceSession = (PerifericoUIServiceAsync) GWT
								.create(PerifericoUIService.class);
						ServiceDefTarget endpointSession = (ServiceDefTarget) perifServiceSession;
						endpointSession.setServiceEntryPoint(GWT
								.getModuleBaseURL() + "uiservice");
						AsyncCallback<Boolean> callbackSession = new UIAsyncCallback<Boolean>() {
							public void onSuccess(Boolean sessionEnded) {
								if (sessionEnded.booleanValue()) {
									PerifericoUI.sessionEnded();
								} else {
									getAnagraphicInfoStationCfg();
								}
							}
						};
						perifServiceSession.verifySessionEnded(callbackSession);
					}
				}, new ClickHandler() {
					public void onClick(ClickEvent event) {
						verifyStationFields();
					}
				});

		// panel that contains button for anagraphic info
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setStyleName("gwt-button-panel");

		hPanel.add(panelButton);
		hPanel.setWidth("100%");
		hPanel.setCellHorizontalAlignment(panelButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		panel.add(hPanel);

		externalPanel.add(anagraphicTitle);
		externalPanel.add(panel);

		/*
		 * Connection info
		 */

		// Label and panel for connection info
		Label connectionTitle = new Label();
		connectionTitle.setText(PerifericoUI.getMessages()
				.lbl_station_title_connection());
		connectionTitle.setStyleName("gwt-Label-title");

		// panel that contains the connection grid
		VerticalPanel panel2 = new VerticalPanel();
		panel2.setStyleName("gwt-post-boxed");
		panel2.add(connectionGrid);

		// format the grid connection info
		connectionGrid.getCellFormatter().setWidth(0, 0, "270px");
		connectionGrid.getCellFormatter().setWidth(0, 1, "200px");
		connectionGrid.getCellFormatter().setWidth(0, 2, "200px");
		connectionGrid.getCellFormatter().setWidth(0, 3, "270px");
		connectionGrid.setCellSpacing(5);
		connectionGrid.setCellPadding(5);

		// Put values in the connection grid cells.
		message = PerifericoUI.getMessages().lbl_station_cop_ip();
		connectionGrid.setText(0, 0, message);

		textCopIp = new TextBox();
		textCopIp.setStyleName("gwt-bg-text-orange");
		textCopIp.setWidth("150px");
		connectionGrid.setWidget(0, 1, textCopIp);
		textCopIp.setName("copip");

		message = PerifericoUI.getMessages().lbl_station_outgoing_call_enabled();
		connectionGrid.setText(1, 0, message);
		checkBoxOutgoingCallEnabled = new CheckBox();
		connectionGrid.setWidget(1, 1, checkBoxOutgoingCallEnabled);
		checkBoxOutgoingCallEnabled.setName("outgoingcallenabled");

		message = PerifericoUI.getMessages().lbl_station_max_connection_retry();
		connectionGrid.setText(2, 0, message);
		textMaxConnectionRetry = new TextBox();
		textMaxConnectionRetry.setStyleName("gwt-bg-text-orange");
		textMaxConnectionRetry.setMaxLength(1);
		textMaxConnectionRetry.setWidth("150px");
		connectionGrid.setWidget(2, 1, textMaxConnectionRetry);
		textMaxConnectionRetry.setName("maxconnectionretry");
		textMaxConnectionRetry.addKeyPressHandler(positiveKeyboardListener);

		message = PerifericoUI.getMessages().lbl_station_connection_type();
		connectionGrid.setText(0, 2, message);
		connectionType = new ListBox();
		connectionType.setWidth("150px");
		connectionType.setVisibleItemCount(1);
		connectionType.setStyleName("gwt-bg-text-orange");
		connectionType.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpointSession = (ServiceDefTarget) perifService;
				endpointSession.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String> callback = new UIAsyncCallback<String>() {
					public void onSuccess(String timeOut) {
						if (timeOut == null)
							Window.alert(PerifericoUI.getMessages()
									.error_conn_param());
						else
							textTimeOut.setText(timeOut);
					}
				};
				perifService.createNewConnectionParam(connectionType
						.getItemText(connectionType.getSelectedIndex()),
						callback);
			}

		});
		connectionGrid.setWidget(0, 3, connectionType);

		message = PerifericoUI.getMessages().lbl_station_time_out();
		connectionGrid.setText(1, 2, message);
		textTimeOut = new TextBox();
		textTimeOut.setStyleName("gwt-bg-text-orange");
		textTimeOut.setMaxLength(5);
		textTimeOut.setWidth("150px");
		connectionGrid.setWidget(1, 3, textTimeOut);
		textTimeOut.addKeyPressHandler(positiveKeyboardListener);

		PanelButtonWidget panelButton2 = PerifericoUI.makeUndoSendPanelButton(
				new ClickHandler() {
					public void onClick(ClickEvent event) {
						// check if session is ended
						PerifericoUIServiceAsync perifServiceSession = (PerifericoUIServiceAsync) GWT
								.create(PerifericoUIService.class);
						ServiceDefTarget endpointSession = (ServiceDefTarget) perifServiceSession;
						endpointSession.setServiceEntryPoint(GWT
								.getModuleBaseURL() + "uiservice");
						AsyncCallback<Boolean> callbackSession = new UIAsyncCallback<Boolean>() {
							public void onSuccess(Boolean sessionEnded) {
								if (sessionEnded.booleanValue()) {
									PerifericoUI.sessionEnded();
								} else {
									getConnectionInfoStationCfg();
								}
							}
						};
						perifServiceSession.verifySessionEnded(callbackSession);
					}
				}, new ClickHandler() {
					public void onClick(ClickEvent event) {
						verifyConnectionStationField();
					}
				});

		// panel that contains button for connection info
		HorizontalPanel hPanel2 = new HorizontalPanel();
		hPanel2.setStyleName("gwt-button-panel");
		hPanel2.add(panelButton2);
		hPanel2.setWidth("100%");
		hPanel2.setCellHorizontalAlignment(panelButton2,
				HasHorizontalAlignment.ALIGN_CENTER);
		panel2.add(hPanel2);

		externalPanel.add(connectionTitle);
		externalPanel.add(panel2);

		initWidget(externalPanel);

	}// end constructor

	private void getAnagraphicInfoStationCfg() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String nickname) {
				if (nickname.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textNickname.setText(nickname);
			}
		};

		perifService.getNickname(callback);

		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String name) {
				if (name.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textName.setText(name);
			}
		};
		perifService.getName(callback);
		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String location) {
				if (location.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textLocation.setText(location);
			}
		};
		perifService.getLocation(callback);
		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String address) {
				if (address.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textAddress.setText(address);
			}
		};
		perifService.getAddress(callback);
		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String city) {
				if (city.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textCity.setText(city);
			}
		};
		perifService.getCity(callback);
		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String province) {
				if (province.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textProvince.setText(province);
			}
		};
		perifService.getProvince(callback);
		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String userNotes) {
				if (userNotes.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textUserNotes.setText(userNotes);
			}
		};
		perifService.getUserNotes(callback);

		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String useGps) {
				if (useGps.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					checkUseGps.setValue(new Boolean(useGps));
			}
		};
		perifService.getUseGps(callback);

	}// end getAnagraphicInfoStationCfg

	private void getConnectionInfoStationCfg() {
		final PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String copIp) {
				if (copIp.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textCopIp.setText(copIp);
			}
		};
		perifService.getCopIp(callback);

		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String outgoingcallenabledStr) {
				if (outgoingcallenabledStr.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					checkBoxOutgoingCallEnabled.setValue(new Boolean(
							outgoingcallenabledStr));
			}
		};
		perifService.isOutgoingCallEnabled(callback);

		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String maxConnRetry) {
				if (maxConnRetry.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textMaxConnectionRetry.setText(maxConnRetry);
			}
		};
		perifService.getMaxConnectionRetry(callback);

		callback = new UIAsyncCallback<String>() {
			public void onSuccess(String timeOut) {
				if (timeOut.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					textTimeOut.setText(timeOut);
			}
		};
		perifService.getTimeOut(callback);

		AsyncCallback<String[]> callback2 = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] connType) {
				if (connType == null)
					Window.alert(PerifericoUI.getMessages().error_conn_param());
				else {
					connectionType.clear();
					for (int i = 0; i < connType.length; i++) {
						connectionType.addItem(connType[i]);
					}
					AsyncCallback<String> cbConnType = new UIAsyncCallback<String>() {
						public void onSuccess(String connType) {
							if (connType.equals(PerifericoUI.SESSION_ENDED))
								PerifericoUI.sessionEnded();
							else {
								for (int i = 0; i < connectionType
										.getItemCount(); i++) {
									String item = connectionType.getValue(i);
									if (item.equals(connType))
										connectionType.setSelectedIndex(i);
								}// end for
							}// end else
						}// end onSuccess
					};
					perifService.getConnectionType(cbConnType);
				}// end else
			}
		};
		perifService.getListConnectionType(callback2);

	}// end getConnectionInfoStationCfg

	private void verifyStationFields() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<Object> callback = new UIAsyncCallback<Object>() {
			public void onSuccess(Object noResult) {
				Utils.sendVerifyOk();
			}
		};

		String[] anagraphicInfo = new String[8];
		anagraphicInfo[0] = textNickname.getText();
		anagraphicInfo[1] = textName.getText();
		anagraphicInfo[2] = textLocation.getText();
		anagraphicInfo[3] = textAddress.getText();
		anagraphicInfo[4] = textCity.getText();
		anagraphicInfo[5] = textProvince.getText();
		anagraphicInfo[6] = textUserNotes.getText();
		anagraphicInfo[7] = checkUseGps.getValue().toString();
		perifService.setVerifyStationFields(anagraphicInfo, callback);

	}// end verifyStationFields

	private void verifyConnectionStationField() {

		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resultString) {
				if (resultString != null) {
					if (resultString.equals(PerifericoUI.SESSION_ENDED))
						PerifericoUI.sessionEnded();
					else
						// error case
						Window.alert(resultString);
				} else
					Utils.sendVerifyOk();
			}
		};

		String[] connectionInfo = new String[5];
		connectionInfo[0] = textCopIp.getText();
		connectionInfo[1] = checkBoxOutgoingCallEnabled.getValue().toString();
		connectionInfo[2] = textMaxConnectionRetry.getText();
		connectionInfo[3] = textTimeOut.getText();
		connectionInfo[4] = connectionType.getItemText(connectionType
				.getSelectedIndex());
		perifService.setVerifyConnectionStationField(connectionInfo, callback);

	}// end verifyConnectionStationField

	@Override
	protected void dismissContent(final AsyncPageOperation asyncPageOperation) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		String[] fieldsValue;
		// fields of anagraphic info
		fieldsValue = new String[13];
		fieldsValue[0] = textNickname.getText();
		fieldsValue[1] = textName.getText();
		fieldsValue[2] = textLocation.getText();
		fieldsValue[3] = textAddress.getText();
		fieldsValue[4] = textCity.getText();
		fieldsValue[5] = textProvince.getText();
		fieldsValue[6] = textUserNotes.getText();
		// fields of connection info
		fieldsValue[7] = textCopIp.getText();
		fieldsValue[8] = checkBoxOutgoingCallEnabled.getValue().toString();
		fieldsValue[9] = textMaxConnectionRetry.getText();
		fieldsValue[10] = checkUseGps.getValue().toString();
		fieldsValue[11] = textTimeOut.getText();
		fieldsValue[12] = connectionType.getItemText(connectionType
				.getSelectedIndex());

		perifService.verifySameStationConfig(fieldsValue, asyncPageOperation);
	}

	@Override
	protected void loadContent() {
		getAnagraphicInfoStationCfg();
		getConnectionInfoStationCfg();
	}

	@Override
	protected void reset() {
		textNickname.setTabIndex(1);
		textName.setTabIndex(2);
		textLocation.setTabIndex(3);
		textAddress.setTabIndex(4);
		textCity.setTabIndex(5);
		textProvince.setTabIndex(6);
		textUserNotes.setTabIndex(7);
		checkUseGps.setTabIndex(8);
		textNickname.setFocus(true);
	}

}// end class
