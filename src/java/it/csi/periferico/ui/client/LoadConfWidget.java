/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: page for configuration management
 * Change log:
 *   2008-11-18: initial version
 * ----------------------------------------------------------------------------
 * $Id: LoadConfWidget.java,v 1.18 2015/10/15 11:50:45 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.UIPage;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Page for configuration management
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class LoadConfWidget extends UIPage {

	private DateTimeFormat viewConfigDateFormat = DateTimeFormat
			.getFormat("dd/MM/yyyy HH:mm:ss");

	private static final int DEFAULT_LIMIT = 10;

	private ListBox listConfiguration;

	private TextBox startDate;

	private TextBox endDate;

	private TextBox limit;

	private TextBox startDate1;

	private TextBox endDate1;

	private TextBox limit1;

	private Label date;

	private Label comment;

	private Label author;

	private ListBox connectionType;

	private ListBox listStation;

	private ListBox listConf;

	private TextBox textIpAddress;

	public LoadConfWidget() {

		PanelButtonWidget panelButton1 = new PanelButtonWidget();
		panelButton1.addButton(PerifericoUI.makeReloadButton());
		panelButton1.addButton(PerifericoUI
				.makeConfHelpButton("loadConfWidget"));
		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().load_conf_title(), panelButton1);

		/*
		 * Local configuration
		 */

		// Label and panel for local configuration
		Label localConfigration = new Label();
		localConfigration.setText(PerifericoUI.getMessages()
				.lbl_local_configuration());
		localConfigration.setStyleName("gwt-Label-title");

		// panel that contains the local configuration
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed");

		HorizontalPanel datePanel = new HorizontalPanel();
		datePanel.setSpacing(5);
		Label lblStartDate = new Label(PerifericoUI.getMessages().lbl_start_date());
		datePanel.add(lblStartDate);
		startDate = new TextBox();
		startDate.setWidth("100px");
		datePanel.add(startDate);
		Label lblEndDate = new Label(PerifericoUI.getMessages().lbl_end_date());
		datePanel.add(lblEndDate);
		endDate = new TextBox();
		endDate.setWidth("100px");
		datePanel.add(endDate);
		Label lblLimit = new Label(PerifericoUI.getMessages().lbl_limit());
		datePanel.add(lblLimit);
		limit = new TextBox();
		limit.setWidth("100px");
		limit.setMaxLength(2);
		limit.setText(new Integer(DEFAULT_LIMIT).toString());
		NumericKeyPressHandler positiveKeyboardListener = new NumericKeyPressHandler(
				true);
		limit.addKeyPressHandler(positiveKeyboardListener);
		datePanel.add(limit);

		Button reloadButton = new Button();
		reloadButton.setStyleName("gwt-button-open-folder");
		reloadButton.setTitle(PerifericoUI.getMessages().open_folder());
		reloadButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				getConfigurationPage();
			}

		});
		datePanel.add(reloadButton);

		panel.add(datePanel);

		FlexTable localTable = new FlexTable();
		localTable.getCellFormatter().setWidth(0, 0, "150px");
		localTable.getCellFormatter().setWidth(0, 1, "320px");
		localTable.getCellFormatter().setWidth(0, 2, "150px");
		localTable.getCellFormatter().setWidth(0, 3, "320px");
		localTable.setCellPadding(2);
		localTable.setCellSpacing(2);

		Label storicLabel = new Label(PerifericoUI.getMessages().lbl_storic());
		localTable.setWidget(0, 0, storicLabel);
		localTable.getCellFormatter().setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_TOP);
		localTable.getCellFormatter().setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_LEFT);
		listConfiguration = new ListBox();
		listConfiguration.setWidth("300px");
		listConfiguration.setVisibleItemCount(7);
		listConfiguration.setStyleName("gwt-bg-text-orange");
		localTable.setWidget(0, 1, listConfiguration);
		localTable.getCellFormatter().setVerticalAlignment(0, 1,
				HasVerticalAlignment.ALIGN_TOP);
		localTable.getCellFormatter().setHorizontalAlignment(0, 1,
				HasHorizontalAlignment.ALIGN_LEFT);

		Button buttonLoadLocal = new Button();
		buttonLoadLocal.setTitle(PerifericoUI.getMessages().button_load_local());
		buttonLoadLocal.setStyleName("gwt-button-load_local");
		buttonLoadLocal.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] result) {
						if (result.length == 3) {
							// configuration loaded
							/*
							 * result:
							 * 
							 * [0] : date
							 * 
							 * [1] : comment
							 * 
							 * [2] : author
							 */
							Window.alert(PerifericoUI.getMessages().conf_loaded()
									+ " " + result[0]);
							date.setText(result[0]);
							comment.setText(result[1]);
							author.setText(result[2]);
							PerifericoUI.updateCurrentPage(null);
						} else {
							// error case
							Window.alert(result[1] + " " + result[0]);
						}
					}
				};
				if (listConfiguration.getSelectedIndex() == -1)
					Window.alert(PerifericoUI.getMessages().error_select_element());
				else
					perifService.loadHistoricConf(listConfiguration
							.getItemText(listConfiguration.getSelectedIndex()),
							callback);
			}
		});

		localTable.setWidget(0, 2, buttonLoadLocal);
		localTable.getCellFormatter().setVerticalAlignment(0, 2,
				HasVerticalAlignment.ALIGN_TOP);
		localTable.getCellFormatter().setHorizontalAlignment(0, 2,
				HasHorizontalAlignment.ALIGN_LEFT);

		FlexTable confLoadedTable = new FlexTable();
		confLoadedTable.getCellFormatter().setWidth(0, 0, "100px");
		confLoadedTable.getCellFormatter().setWidth(0, 1, "150px");
		confLoadedTable.setCellPadding(2);
		confLoadedTable.setCellSpacing(2);

		localTable.setWidget(0, 3, confLoadedTable);
		localTable.getCellFormatter().setVerticalAlignment(0, 3,
				HasVerticalAlignment.ALIGN_TOP);
		localTable.getCellFormatter().setHorizontalAlignment(0, 3,
				HasHorizontalAlignment.ALIGN_LEFT);

		Label lblLoaded = new Label();
		lblLoaded.setText(PerifericoUI.getMessages().lbl_loaded());
		lblLoaded.setStyleName("gwt-title-orange");
		confLoadedTable.setWidget(0, 0, lblLoaded);
		confLoadedTable.getFlexCellFormatter().setColSpan(0, 0, 2);
		confLoadedTable.getCellFormatter().setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);

		Label lblDate = new Label();
		lblDate.setText(PerifericoUI.getMessages().lbl_date());
		confLoadedTable.setWidget(1, 0, lblDate);
		date = new Label();
		confLoadedTable.setWidget(1, 1, date);

		Label lblComment = new Label();
		lblComment.setText(PerifericoUI.getMessages().lbl_comment());
		confLoadedTable.setWidget(2, 0, lblComment);
		comment = new Label();
		confLoadedTable.setWidget(2, 1, comment);
		Label lblAuthor = new Label();
		lblAuthor.setText(PerifericoUI.getMessages().lbl_author());
		confLoadedTable.setWidget(3, 0, lblAuthor);
		author = new Label();
		confLoadedTable.setWidget(3, 1, author);

		panel.add(localTable);

		externalPanel.add(localConfigration);
		externalPanel.add(panel);

		/*
		 * Configuration from COP
		 */

		// Label and panel for configuration from COP
		Label connectionTitle = new Label();
		connectionTitle.setText(PerifericoUI.getMessages().lbl_from_cop());
		connectionTitle.setStyleName("gwt-Label-title");

		// panel that contains the configuration for connect to COP
		VerticalPanel panel2 = new VerticalPanel();
		panel2.setStyleName("gwt-post-boxed");

		HorizontalPanel datePanel2 = new HorizontalPanel();
		datePanel2.setSpacing(5);
		Label lblStartDate2 = new Label(PerifericoUI.getMessages().lbl_start_date());
		datePanel2.add(lblStartDate2);
		startDate1 = new TextBox();
		startDate1.setWidth("100px");
		datePanel2.add(startDate1);
		Label lblEndDate1 = new Label(PerifericoUI.getMessages().lbl_end_date());
		datePanel2.add(lblEndDate1);
		endDate1 = new TextBox();
		endDate1.setWidth("100px");
		datePanel2.add(endDate1);
		Label lblLimit1 = new Label(PerifericoUI.getMessages().lbl_limit());
		datePanel2.add(lblLimit1);
		limit1 = new TextBox();
		limit1.setWidth("100px");
		limit1.setMaxLength(2);
		limit1.setText(new Integer(DEFAULT_LIMIT).toString());
		NumericKeyPressHandler positiveKeyboardListener1 = new NumericKeyPressHandler(
				true);
		limit1.addKeyPressHandler(positiveKeyboardListener1);
		datePanel2.add(limit1);

		panel2.add(datePanel2);

		HorizontalPanel connectionPanel = new HorizontalPanel();
		connectionPanel.setSpacing(5);

		Label lblIpAddress = new Label();
		lblIpAddress.setText(PerifericoUI.getMessages().lbl_station_cop_ip());
		connectionPanel.add(lblIpAddress);

		textIpAddress = new TextBox();
		textIpAddress.setWidth("200px");
		textIpAddress.setStyleName("gwt-bg-text-orange");
		connectionPanel.add(textIpAddress);

		Label lblDeviceType = new Label();
		lblDeviceType.setText(PerifericoUI.getMessages()
				.lbl_station_connection_type());
		connectionPanel.add(lblDeviceType);

		connectionType = new ListBox();
		connectionType.setWidth("300px");
		connectionType.setVisibleItemCount(1);
		connectionType.setStyleName("gwt-bg-text-orange");
		connectionPanel.add(connectionType);

		Button connectButton = new Button();
		connectButton.setStyleName("gwt-button-connect-cop");
		connectButton.setTitle(PerifericoUI.getMessages().connet_cop());
		connectButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] result) {
						Utils.unlockForPopup("loading");
						listStation.clear();
						for (int i = 0; i < result.length; i++)
							listStation.addItem(result[i]);
					}

					@Override
					public void onFailure(Throwable caught) {
						Utils.unlockForPopup("loading");
						super.onFailure(caught);
					}
				};

				Utils.blockForPopup("loading");
				listStation.clear();
				listConf.clear();
				perifService.connectToCop(textIpAddress.getText(),
						connectionType.getItemText(connectionType
								.getSelectedIndex()), callback);
			}

		});
		connectionPanel.add(connectButton);

		panel2.add(connectionPanel);

		FlexTable listConfTable = new FlexTable();
		// format the grid for anagraphic info
		listConfTable.getCellFormatter().setWidth(0, 0, "150px");
		listConfTable.getCellFormatter().setWidth(0, 1, "320px");
		listConfTable.getCellFormatter().setWidth(0, 2, "150px");
		listConfTable.getCellFormatter().setWidth(0, 3, "320px");
		listConfTable.setCellPadding(5);
		listConfTable.setCellSpacing(5);

		Label lblListStation = new Label();
		lblListStation.setText(PerifericoUI.getMessages().lbl_list_station());
		listConfTable.setWidget(0, 0, lblListStation);

		Label lblConf = new Label();
		lblConf.setText(PerifericoUI.getMessages().lbl_list_conf());
		listConfTable.setWidget(0, 2, lblConf);

		listStation = new ListBox();
		listStation.setWidth("300px");
		listStation.setVisibleItemCount(1);
		listStation.setStyleName("gwt-bg-text-orange");
		listStation.setVisibleItemCount(7);
		listStation.addChangeHandler(new ChangeHandler() {

			public void onChange(ChangeEvent event) {
				listConf.clear();
			}

		});
		listConfTable.setWidget(1, 0, listStation);

		Button buttonLoadListConf = new Button();
		buttonLoadListConf.setStyleName("gwt-button-open-folder");
		buttonLoadListConf.setTitle(PerifericoUI.getMessages().open_folder());
		buttonLoadListConf.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] result) {
						listConf.clear();
						for (int i = 0; i < result.length; i++) {
							try {
								Date cfgDate = new Date(Long
										.parseLong(result[i]));
								listConf.addItem(
										viewConfigDateFormat.format(cfgDate),
										result[i]);
							} catch (Exception ex) {
								listConf.addItem(result[i], result[i]);
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						listStation.clear();
						super.onFailure(caught);
					}
				};

				if (listStation.getSelectedIndex() == -1)
					Window.alert(PerifericoUI.getMessages().error_select_element());
				else
					perifService.loadListConf(listStation
							.getItemText(listStation.getSelectedIndex()),
							startDate1.getText(), endDate1.getText(),
							new Integer(limit1.getText()), callback);

			}

		});
		listConfTable.getCellFormatter().setVerticalAlignment(1, 1,
				HasVerticalAlignment.ALIGN_TOP);
		listConfTable.setWidget(1, 1, buttonLoadListConf);

		listConf = new ListBox();
		listConf.setWidth("300px");
		listConf.setVisibleItemCount(1);
		listConf.setStyleName("gwt-bg-text-orange");
		listConf.setVisibleItemCount(7);
		listConfTable.setWidget(1, 2, listConf);

		Button buttonLoadConf = new Button();
		buttonLoadConf.setStyleName("gwt-button-load_local");
		buttonLoadConf.setTitle(PerifericoUI.getMessages().button_load_remote());
		buttonLoadConf.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] result) {
						/*
						 * returnParameter:
						 * 
						 * [0][0]: date
						 * 
						 * [1][0]: comment
						 * 
						 * [2][0]: author
						 */

						date.setText(result[0]);
						comment.setText(result[1]);
						author.setText(result[2]);

						Window.alert(PerifericoUI.getMessages().conf_loaded() + " "
								+ result[0]);
						listStation.clear();
						listConf.clear();
						PerifericoUI.updateCurrentPage(null);
					}

					@Override
					public void onFailure(Throwable caught) {
						listStation.clear();
						listConf.clear();
						super.onFailure(caught);
					}
				};
				if (listStation.getSelectedIndex() == -1
						|| listConf.getSelectedIndex() == -1)
					Window.alert(PerifericoUI.getMessages().error_select_element());
				else
					perifService.readConf(listStation.getItemText(listStation
							.getSelectedIndex()), listConf.getValue(listConf
							.getSelectedIndex()), callback);
			}

		});
		listConfTable.setWidget(1, 3, buttonLoadConf);
		listConfTable.getCellFormatter().setVerticalAlignment(1, 3,
				HasVerticalAlignment.ALIGN_TOP);

		panel2.add(listConfTable);

		externalPanel.add(connectionTitle);
		externalPanel.add(panel2);

		initWidget(externalPanel);

	}// end constructor

	private void getConfigurationPage() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] listHistoric) {
				if (listHistoric[0][0] != PerifericoUI.SESSION_ENDED) {

					/*
					 * pageParameter:
					 * 
					 * [0][0]: date
					 * 
					 * [1][0]: comment
					 * 
					 * [2][0]: author
					 * 
					 * [3][0]: ipAddress
					 * 
					 * [4][0]: deviceType
					 * 
					 * [5][0] -> [5][...]: listDeviceType
					 * 
					 * [6][0] -> [6][...]: listLocalConfiguration
					 */
					date.setText(listHistoric[0][0]);
					comment.setText(listHistoric[1][0]);
					author.setText(listHistoric[2][0]);
					textIpAddress.setText(listHistoric[3][0]);

					connectionType.clear();
					for (int i = 0; i < listHistoric[5].length; i++) {
						if (listHistoric[5][i] != null) {
							connectionType.addItem(listHistoric[5][i]);
							if (listHistoric[5][i].equals(listHistoric[4][0]))
								connectionType.setSelectedIndex(i);
						}
					}// end for

					listConfiguration.clear();
					for (int i = 0; i < listHistoric[6].length; i++)
						if (listHistoric[6][i] != null)
							listConfiguration.addItem(listHistoric[6][i]);
				} else {
					// session ended
					PerifericoUI.sessionEnded();
				}
			}
		};

		perifService.getConfigurationPage(startDate.getText(),
				endDate.getText(), new Integer(limit.getText()), callback);
	}// end getListHistoricConf

	@Override
	protected void loadContent() {
		getConfigurationPage();
	}

}// end class
