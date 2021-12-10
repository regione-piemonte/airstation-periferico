/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: alarms configuration page
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: AlarmWidget.java,v 1.74 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.AsyncPageOperation;
import it.csi.periferico.ui.client.pagecontrol.PageUpdateAction;
import it.csi.periferico.ui.client.pagecontrol.UIPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Alarms configuration page
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class AlarmWidget extends UIPage {

	// Table for alarm information.
	private final FlexTable table;

	private final ListBox alarmListBox = new ListBox();

	private final Grid gridModifyAlarm = new Grid(1, 4);

	private String alarmId = null;

	private String alarmType = null;

	private String[] alarmsId;

	private String[] alarmsType;

	private String[] alarmDetails;

	private TextBox textNotes;

	private VerticalPanel modifyPanel;

	private CheckBox checkBoxActiveHigh;

	private TextBox textAlarmThresholdHigh;

	private TextBox textAlarmThresholdLow;

	private TextBox textWarningThresholdHigh;

	private TextBox textWarningThresholdLow;

	private Label titleModify;

	private Label titleNew;

	private Button alarmEnabled;

	private HorizontalPanel newAlarmPanel;

	private boolean newAlarmClicked = false;

	private boolean modifyAlarmEnabled;

	public AlarmWidget() {

		// panel that represents the page
		PanelButtonWidget panelButton1 = new PanelButtonWidget();
		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().alarm_title(), panelButton1);
		panelButton1.addButton(PerifericoUI.makeSaveButton());
		panelButton1.addButton(PerifericoUI.makeReloadButton());
		panelButton1.addButton(PerifericoUI
				.makeConfHelpButton("alarmWidget"));
		/*
		 * Alarm table
		 */
		// Prepare panel for alarm list
		Label title = new Label();
		title.setText(PerifericoUI.getMessages().lbl_alarm_title());
		title.setStyleName("gwt-Label-title");

		// panel that contains the grid alarm list
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed");

		// Prepare table of titles
		FlexTable headerTable = new FlexTable();
		headerTable
				.setText(0, 0, PerifericoUI.getMessages().lbl_alarm_abilitation());
		headerTable.setText(0, 1, PerifericoUI.getMessages().lbl_id());
		headerTable
				.setText(0, 2, PerifericoUI.getMessages().lbl_alarm_description());
		headerTable.setText(0, 3, PerifericoUI.getMessages().lbl_alarm_notes());
		headerTable.setText(0, 4, PerifericoUI.getMessages().lbl_modify());
		headerTable.setText(0, 5, PerifericoUI.getMessages().lbl_delete());
		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "80px");
		headerTable.getCellFormatter().setWidth(0, 1, "150px");
		headerTable.getCellFormatter().setWidth(0, 2, "250px");
		headerTable.getCellFormatter().setWidth(0, 3, "250px");
		headerTable.getCellFormatter().setWidth(0, 4, "125px");
		headerTable.getCellFormatter().setWidth(0, 5, "125px");
		for (int j = 0; j < 6; j++) {
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}

		panel.add(headerTable);

		// Prepare table for alarm info in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("220px");
		panel.add(scrollPanel);

		externalPanel.add(title);
		externalPanel.add(panel);

		/*
		 * New alarm panel
		 */
		// Prepare panel for new alarm
		titleNew = new Label();
		titleNew.setText(PerifericoUI.getMessages().lbl_alarm_new());
		titleNew.setStyleName("gwt-Label-title");

		newAlarmPanel = new HorizontalPanel();
		newAlarmPanel.setStyleName("gwt-post-boxed");
		newAlarmPanel.setSpacing(10);

		Button newAlarmButton = new Button();
		newAlarmButton.setStyleName("gwt-button-new-orange");
		newAlarmButton.setTitle(PerifericoUI.getMessages().button_new_alarm());
		newAlarmButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int indexSelected = alarmListBox.getSelectedIndex();
				String selectedValue = alarmListBox.getValue(indexSelected);
				String selectedItemText = alarmListBox
						.getItemText(indexSelected);
				if (selectedValue.equals("0"))
					Window.alert(PerifericoUI.getMessages().error_select_element());
				else {
					newAlarmClicked = true;
					setNewAlarmId(selectedItemText);
				}
			}
		});

		// Setting list box
		alarmListBox.setWidth("300px");
		alarmListBox.setVisibleItemCount(1);

		newAlarmPanel.add(alarmListBox);
		newAlarmPanel.add(newAlarmButton);

		// setting the alignement
		newAlarmPanel.setCellHorizontalAlignment(newAlarmButton,
				HasHorizontalAlignment.ALIGN_LEFT);
		newAlarmPanel.setCellHorizontalAlignment(alarmListBox,
				HasHorizontalAlignment.ALIGN_LEFT);
		newAlarmPanel.setCellVerticalAlignment(newAlarmButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newAlarmPanel.setCellVerticalAlignment(alarmListBox,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newAlarmPanel.setCellWidth(alarmListBox, "310px");

		externalPanel.add(titleNew);
		externalPanel.add(newAlarmPanel);

		/*
		 * Modify/insert alarm panel
		 */
		// Prepare panel that contains fields for modify/insert alarm
		// information
		titleModify = new Label();
		titleModify.setText(PerifericoUI.getMessages().lbl_alarm_title_modify());
		titleModify.setStyleName("gwt-Label-title");

		modifyPanel = new VerticalPanel();
		modifyPanel.setStyleName("gwt-post-boxed");

		// format the grid for insert/modify panel
		gridModifyAlarm.setWidth("100%");
		gridModifyAlarm.getCellFormatter().setWidth(0, 0, "180px");
		gridModifyAlarm.getCellFormatter().setWidth(0, 1, "260px");
		gridModifyAlarm.getCellFormatter().setWidth(0, 2, "180px");
		gridModifyAlarm.getCellFormatter().setWidth(0, 3, "280px");
		gridModifyAlarm.setCellPadding(2);
		gridModifyAlarm.setCellSpacing(2);

		PanelButtonWidget panelButton = PerifericoUI.makeUndoSendPanelButton(
				new ClickHandler() {
					public void onClick(ClickEvent event) {
						reset();
					}
				}, new ClickHandler() {
					public void onClick(ClickEvent event) {
						verifyAlarmFields();
					}
				});
		modifyPanel.add(gridModifyAlarm);

		// panel that contains button for cancel/send-verify
		HorizontalPanel hPanel2 = new HorizontalPanel();
		hPanel2.setStyleName("gwt-button-panel");
		hPanel2.add(panelButton);
		hPanel2.setWidth("100%");
		hPanel2.setCellHorizontalAlignment(panelButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		modifyPanel.add(hPanel2);

		modifyPanel.setVisible(false);
		titleModify.setVisible(false);
		titleModify.setText(PerifericoUI.getMessages().lbl_alarm_title_modify());

		externalPanel.add(titleModify);
		externalPanel.add(modifyPanel);

		initWidget(externalPanel);

	}// end constructor

	private void getInfoAlarmCfg() {

		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		// get all alarm fields to display

		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] matrix) {
				// TODO: capire se verificare che le dimensioni
				// della matrice siano corrette
				// clear table
				Utils.clearTable(table);
				table.getCellFormatter().setWidth(0, 0, "80px");
				table.getCellFormatter().setWidth(0, 1, "150px");
				table.getCellFormatter().setWidth(0, 2, "250px");
				table.getCellFormatter().setWidth(0, 3, "250px");
				table.getCellFormatter().setWidth(0, 4, "125px");
				table.getCellFormatter().setWidth(0, 5, "125px");
				alarmsId = new String[matrix.length];
				alarmsType = new String[matrix.length];
				for (int i = 0; i < matrix.length; i++) {
					IconImageBundle iconImageBundle = (IconImageBundle) GWT
							.create(IconImageBundle.class);
					if (new Boolean(matrix[i][0]).booleanValue()) {
						// the alarm is enabled -> show green led
						Image ledGreen = new Image();
						ledGreen.setResource(iconImageBundle.ledGreen());
						table.setWidget(i, 0, ledGreen);
					} else {
						// the alarm is not enabled -> show gray led
						Image ledGray = new Image();
						ledGray.setResource(iconImageBundle.ledGray());
						table.setWidget(i, 0, ledGray);
					}
					table.getCellFormatter().setAlignment(i, 0,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 0,
							"gwt-table-data");
					for (int j = 1; j < 4; j++) {
						table.getCellFormatter().setStyleName(i, j,
								"gwt-table-data");
						table.setText((i), j, matrix[i][j]);
					}
					alarmsId[i] = matrix[i][4];
					alarmsType[i] = matrix[i][5];
					Button modifyButton = new Button();
					modifyButton.setStyleName("gwt-button-modify");
					modifyButton.setTitle(PerifericoUI.getMessages()
							.alarm_button_modify());
					table.setWidget((i), 4, modifyButton);
					table.getCellFormatter().setAlignment(i, 4,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 4,
							"gwt-table-data");
					modifyButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							newAlarmClicked = false;
							alarmListBox.setSelectedIndex(0);
							Button button = (Button) event.getSource();
							for (int k = 0; k < table.getRowCount(); k++) {
								if (((Button) (table.getWidget(k, 4)))
										.equals(button)) {
									getAlarmDetails(alarmsId[k]);
								}
							}
						}
					});
					Button deleteButton = new Button();
					deleteButton.setStyleName("gwt-button-delete");
					deleteButton.setTitle(PerifericoUI.getMessages()
							.alarm_button_delete());
					table.setWidget((i), 5, deleteButton);
					table.getCellFormatter().setAlignment(i, 5,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 5,
							"gwt-table-data");
					deleteButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Button button = (Button) event.getSource();
							for (int k = 0; k < table.getRowCount(); k++) {
								if (((Button) (table.getWidget(k, 5)))
										.equals(button)) {
									boolean confirmResult = Window.confirm(PerifericoUI.getMessages()
											.lbl_alarm_confirm_delete());
									if (confirmResult) {
										String alarmIdStr = alarmsId[k];
										deleteAlarm(alarmIdStr);
									}
								}
							}
						}
					});
				}
			}
		};

		perifService.getAlarms(callback);
	} // end getInfoAlarmCfg

	private void getAlarmNamesCfg() {

		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] alarms) {
				alarmListBox.clear();
				alarmListBox.addItem(PerifericoUI.getMessages().select_alarm_name(),
						new String("0"));
				for (int i = 0; i < alarms.length; i++) {
					alarmListBox.addItem(alarms[i]);
				}
			}
		};
		perifService.getAlarmNamesCfg(callback);
	} // end getAlarmNamesCfg

	private void verifyAlarmFields() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resultString) {
				if (resultString != null) {
					Window.alert(resultString);
				} else {
					PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
							.create(PerifericoUIService.class);
					ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
					endpoint2.setServiceEntryPoint(GWT.getModuleBaseURL()
							+ "uiservice");
					AsyncCallback<Object> callback2 = new UIAsyncCallback<Object>() {
						public void onSuccess(Object result) {
							// TODO: capire se bisogna fare qualcosa
							reset();
							getInfoAlarmCfg();
							Utils.sendVerifyOk();
						}
					};

					perifService2.verifyInsertNewAlarm(alarmId, callback2);
				}
			}
		};

		String[] fieldsValue;
		if (new Boolean(alarmType).booleanValue()) {
			// case of digital alarm
			fieldsValue = new String[3];
			fieldsValue[0] = new Boolean(modifyAlarmEnabled).toString();
			fieldsValue[1] = textNotes.getText();
			fieldsValue[2] = checkBoxActiveHigh.getValue().toString();
		} else {
			// case of trigger alarm
			fieldsValue = new String[6];
			fieldsValue[0] = new Boolean(modifyAlarmEnabled).toString();
			fieldsValue[1] = textNotes.getText();
			fieldsValue[2] = textAlarmThresholdHigh.getText();
			fieldsValue[3] = textAlarmThresholdLow.getText();
			fieldsValue[4] = textWarningThresholdHigh.getText();
			fieldsValue[5] = textWarningThresholdLow.getText();
		}

		perifService.setVerifyAlarmFields(alarmId, fieldsValue, callback);

	}// end verifyAlarmFields

	private void deleteAlarm(final String alarmIdStr) {
		final PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		PageUpdateAction deleteAlarmAction = new PageUpdateAction() {
			@Override
			public void action() {
				AsyncCallback<Boolean> acb_deleteAlarm = new UIAsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
						// TODO: capire cosa fare del valore di ritorno
						if (result) {
							updatePage();
						}
					}
				};

				perifService.deleteAlarm(alarmIdStr, acb_deleteAlarm);
			}
		};

		PerifericoUI.updateCurrentPage(deleteAlarmAction);
	}

	// get all alarm fields to display
	private void getAlarmDetails(String alarmId) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] result) {
				alarmDetails = result;
				setGridModifyAlarm(alarmDetails);
			}
		};

		perifService.getAlarmDetails(alarmId, callback);
	}

	/**
	 * Show the new alarm or modify alarm panel
	 * 
	 * @param alarmFields
	 *            fields to show
	 */
	private void setGridModifyAlarm(String[] alarmFields) {

		// Clear row of specific alarm
		gridModifyAlarm.resize(2, 4);

		if (newAlarmClicked) {
			// show the "new alarm" title and the alarm type
			titleNew.setText(PerifericoUI.getMessages().lbl_alarm_new() + " - "
					+ alarmFields[2]);
			titleNew.setVisible(true);
			titleModify.setVisible(false);
			titleModify.setText(PerifericoUI.getMessages().lbl_alarm_title_modify());
		} else {
			// show the "modify alarm" title and the alarm type
			titleNew.setVisible(false);
			titleNew.setText(PerifericoUI.getMessages().lbl_alarm_new());
			titleModify.setText(PerifericoUI.getMessages().lbl_alarm_title_modify()
					+ " - " + alarmFields[2]);
			titleModify.setVisible(true);
		}

		// set invisible the new panel
		newAlarmPanel.setVisible(false);

		// set visible the modify panel
		modifyPanel.setVisible(true);

		alarmEnabled = new Button();
		if (new Boolean(alarmFields[0]).booleanValue()) {
			// case of alarm enabled
			alarmEnabled.setStyleName("gwt-button-enabled");
			alarmEnabled.setTitle(PerifericoUI.getMessages().button_enabled());
			modifyAlarmEnabled = true;
		} else {
			// case of alarm disabled
			alarmEnabled.setStyleName("gwt-button-disabled");
			alarmEnabled.setTitle(PerifericoUI.getMessages().button_disabled());
			modifyAlarmEnabled = false;
		}
		alarmEnabled.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				changeStatus();
			}
		});

		gridModifyAlarm.setText(0, 0, PerifericoUI.getMessages().lbl_enabled());
		gridModifyAlarm.setWidget(0, 1, alarmEnabled);

		alarmId = alarmFields[1];
		alarmType = alarmFields[5];

		gridModifyAlarm.setText(0, 2,
				PerifericoUI.getMessages().alarm_lbl_description());
		Label description = new Label();
		description.setStyleName("gwt-bg-text-orange");
		description.setText(alarmFields[3]);
		gridModifyAlarm.setWidget(0, 3, description);

		gridModifyAlarm.setText(1, 0, PerifericoUI.getMessages().lbl_alarm_notes()
				+ ":");
		textNotes = new TextBox();
		textNotes.setName("notes");
		textNotes.setWidth("250px");
		textNotes.setStyleName("gwt-bg-text-orange");
		textNotes.setText(alarmFields[4]);
		gridModifyAlarm.setWidget(1, 1, textNotes);

		if (new Boolean(alarmFields[5]).booleanValue()) {
			// case of digital alarm
			gridModifyAlarm.resize(3, 4);
			gridModifyAlarm.setText(1, 2,
					PerifericoUI.getMessages().analyzer_lbl_connection());
			Label connection = new Label();
			connection.setStyleName("gwt-bg-text-orange");
			if (alarmFields[6] == null) {
				connection.setText(PerifericoUI.getMessages()
						.analyzer_not_connected());
			} else {
				connection.setText(alarmFields[6]);
			}
			gridModifyAlarm.setWidget(1, 3, connection);
			gridModifyAlarm.setText(2, 0,
					PerifericoUI.getMessages().lbl_active_high());
			checkBoxActiveHigh = new CheckBox();
			gridModifyAlarm.setWidget(2, 1, checkBoxActiveHigh);
			checkBoxActiveHigh.setName("activehigh");
			checkBoxActiveHigh.setValue(new Boolean(alarmFields[7]));
		} else {
			// case of trigger alarm
			gridModifyAlarm.resize(4, 4);
			gridModifyAlarm.clearCell(1, 2);
			gridModifyAlarm.clearCell(1, 3);
			gridModifyAlarm.setText(2, 0,
					PerifericoUI.getMessages().alarm_lbl_threshold_high());
			textAlarmThresholdHigh = new TextBox();
			textAlarmThresholdHigh.setStyleName("gwt-bg-text-orange");
			NumericKeyPressHandler negativeKeyboardListener = new NumericKeyPressHandler(
					false);
			textAlarmThresholdHigh.addKeyPressHandler(negativeKeyboardListener);
			gridModifyAlarm.setWidget(2, 1, textAlarmThresholdHigh);
			textAlarmThresholdHigh.setName("alarmthresholdhigh");
			gridModifyAlarm.setText(3, 0,
					PerifericoUI.getMessages().alarm_lbl_threshold_low());
			textAlarmThresholdLow = new TextBox();
			textAlarmThresholdLow.setStyleName("gwt-bg-text-orange");
			textAlarmThresholdLow.addKeyPressHandler(negativeKeyboardListener);
			gridModifyAlarm.setWidget(3, 1, textAlarmThresholdLow);
			textAlarmThresholdLow.setName("alarmthresholdlow");
			gridModifyAlarm.setText(2, 2,
					PerifericoUI.getMessages().alarm_lbl_warning_high());
			textWarningThresholdHigh = new TextBox();
			textWarningThresholdHigh.setStyleName("gwt-bg-text-orange");
			textWarningThresholdHigh
					.addKeyPressHandler(negativeKeyboardListener);
			gridModifyAlarm.setWidget(2, 3, textWarningThresholdHigh);
			textWarningThresholdHigh.setName("warningthresholdhigh");
			gridModifyAlarm.setText(3, 2,
					PerifericoUI.getMessages().alarm_lbl_warning_low());
			textWarningThresholdLow = new TextBox();
			textWarningThresholdLow.setStyleName("gwt-bg-text-orange");
			textWarningThresholdLow
					.addKeyPressHandler(negativeKeyboardListener);
			gridModifyAlarm.setWidget(3, 3, textWarningThresholdLow);
			textWarningThresholdLow.setName("warningthresholdlow");

			// case of existing alarm
			if (alarmFields.length == 11) {
				textAlarmThresholdHigh.setText(alarmFields[6]);
				textAlarmThresholdLow.setText(alarmFields[7]);
				textWarningThresholdHigh.setText(alarmFields[8]);
				textWarningThresholdLow.setText(alarmFields[9]);

				// case of binded trigger alarm
				if (alarmFields[10] != null) {
					setPanelForUnbindTriggerAlarm(alarmFields[10]);
				}
				// case of unbinded trigger alarm
				else {
					setPanelForOpenPopupBindableSampleElement();
				}
			}
			// case of new alarm
			else {
				setPanelForOpenPopupBindableSampleElement();
			}
		}
		// TODO fare gli altri casi: apialarm ecc

		// change background color in row of gridAlarm of the modifying
		// alarm
		if (!newAlarmClicked) {
			for (int i = 0; i < alarmsId.length; i++) {
				if (alarmsId[i].equals(alarmId))
					table.getRowFormatter().setStyleName((i),
							"gwt-row-selected");
				else
					table.getRowFormatter().removeStyleName((i),
							"gwt-row-selected");
			}
		}// end if !newAlarmClicked
	}// end setGridModifyAlarm

	private void setPanelForOpenPopupBindableSampleElement() {
		HorizontalPanel hPanelIOUser = new HorizontalPanel();
		gridModifyAlarm.setWidget(1, 2, hPanelIOUser);
		Button openListIOUserButton = new Button();
		openListIOUserButton.setStyleName("gwt-button-list_iouser-medium");
		openListIOUserButton.setTitle(PerifericoUI.getMessages()
				.trigger_alarm_button_open_list_iouser());
		hPanelIOUser.add(openListIOUserButton);
		openListIOUserButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// check if session is ended
				PerifericoUIServiceAsync perifServiceSession = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpointSession = (ServiceDefTarget) perifServiceSession;
				endpointSession.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<Boolean> callbackSession = new UIAsyncCallback<Boolean>() {
					public void onSuccess(Boolean sessionEnded) {
						if (sessionEnded.booleanValue()) {
							PerifericoUI.sessionEnded();
						} else {
							Utils.blockForPopup("popup");
							new TriggerAlarmBindingWidget(false, true, alarmId);
						}
					}
				};
				perifServiceSession.verifySessionEnded(callbackSession);
			}
		});
	}

	private void changeStatus() {
		if (modifyAlarmEnabled) {
			// case of alarm enabled and must to set to disabled
			alarmEnabled.setStyleName("gwt-button-disabled");
			alarmEnabled.setTitle(PerifericoUI.getMessages().button_disabled());
			modifyAlarmEnabled = false;

		} else {
			// case of alarm disabled and must to set to enabled
			alarmEnabled.setStyleName("gwt-button-enabled");
			alarmEnabled.setTitle(PerifericoUI.getMessages().button_enabled());
			modifyAlarmEnabled = true;
		}

	}// end changeStatus

	private void setNewAlarmId(String selectedItemText) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] resultString) {
				if (resultString[0] != null && resultString[1] != null
						&& resultString[2] != null) {
					alarmDetails = null;
					setGridModifyAlarm(resultString);
				} else {
					// TODO: capire cosa fare
				}
			}
		};

		perifService.setNewAlarmId(selectedItemText, callback);
	}

	void setPanelForUnbindTriggerAlarm(String label) {
		HorizontalPanel hPanelIOUser = new HorizontalPanel();
		gridModifyAlarm.setWidget(1, 2, hPanelIOUser);
		Label ioUserLabel = new Label(label);
		hPanelIOUser.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		Button unbindButton = new Button();
		unbindButton.setStyleName("gwt-button-delete-medium");
		unbindButton.setTitle(PerifericoUI.getMessages()
				.trigger_alarm_button_unbind());
		hPanelIOUser.add(unbindButton);
		hPanelIOUser.add(ioUserLabel);
		unbindButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<Object> callback = new UIAsyncCallback<Object>() {
					public void onSuccess(Object result) {
						setPanelForOpenPopupBindableSampleElement();
					}
				};

				boolean confirmResult = Window.confirm(PerifericoUI.getMessages()
						.confirm_unbind_trigger_alarm());
				if (confirmResult)
					perifService.unbindTriggerAlarm(alarmId, callback);
			}
		});
	} // end unbindChannel

	@Override
	protected void dismissContent(final AsyncPageOperation asyncPageOperation) {
		if (alarmId == null || alarmId.isEmpty() || newAlarmClicked) {
			asyncPageOperation.complete();
		} else {
			PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
					.create(PerifericoUIService.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
			endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

			String[] fieldsValue;
			if (new Boolean(alarmType).booleanValue()) {
				// case of digital alarm
				fieldsValue = new String[3];
				fieldsValue[0] = new Boolean(modifyAlarmEnabled).toString();
				fieldsValue[1] = textNotes.getText();
				fieldsValue[2] = checkBoxActiveHigh.getValue().toString();
			} else {
				// case of trigger alarm
				fieldsValue = new String[6];
				fieldsValue[0] = new Boolean(modifyAlarmEnabled).toString();
				fieldsValue[1] = textNotes.getText();
				fieldsValue[2] = textAlarmThresholdHigh.getText();
				fieldsValue[3] = textAlarmThresholdLow.getText();
				fieldsValue[4] = textWarningThresholdHigh.getText();
				fieldsValue[5] = textWarningThresholdLow.getText();
			}
			perifService.verifySameAlarmConfig(alarmId, fieldsValue,
					asyncPageOperation);
		}

	}

	@Override
	protected void loadContent() {
		getInfoAlarmCfg();
		getAlarmNamesCfg();
	}

	@Override
	protected void reset() {
		alarmId = null;
		alarmType = null;
		titleModify.setVisible(false);
		modifyPanel.setVisible(false);
		titleNew.setVisible(true);
		newAlarmPanel.setVisible(true);
		for (int i = 0; i < table.getRowCount(); i++)
			table.getRowFormatter().removeStyleName(i, "gwt-row-selected");
	}

}// end class
