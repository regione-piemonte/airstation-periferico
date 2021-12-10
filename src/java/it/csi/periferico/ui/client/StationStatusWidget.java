/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: page that shows the status of container alarms
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: StationStatusWidget.java,v 1.23 2013/06/12 08:10:38 pfvallosio Exp $
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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Page that shows station status
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class StationStatusWidget extends UIPage {

	private Label title = new Label();

	private FlexTable table;

	private String[] alarmsId;

	public StationStatusWidget() {

		PanelButtonWidget panelButtonRefresh = new PanelButtonWidget();
		panelButtonRefresh.addButton(PerifericoUI.makeRefreshButton());
		panelButtonRefresh.addButton(PerifericoUI
				.makeViewHelpButton("stationStatusWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().station_status_title(),
				panelButtonRefresh);
		// Label and panel for subdevice info
		title.setStyleName("gwt-Label-title-blue");
		title.setText(PerifericoUI.getMessages().station_status_alarm_title());

		// panel that contains subdevice info and buttons
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		// Prepare info table's title
		FlexTable headerTable = new FlexTable();
		headerTable.setText(0, 0,
				PerifericoUI.getMessages().station_status_link_history());
		headerTable.setText(0, 1,
				PerifericoUI.getMessages().station_status_alarm_id());
		headerTable.setText(0, 2,
				PerifericoUI.getMessages().station_status_alarm_description());
		headerTable.setText(0, 3, PerifericoUI.getMessages().lbl_alarm_notes());
		headerTable.setText(0, 4,
				PerifericoUI.getMessages().station_status_alarm_value());
		headerTable.setText(0, 5,
				PerifericoUI.getMessages().station_status_alarm_timestamp());
		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "80px");
		headerTable.getCellFormatter().setWidth(0, 1, "100px");
		headerTable.getCellFormatter().setWidth(0, 2, "250px");
		headerTable.getCellFormatter().setWidth(0, 3, "325px");
		headerTable.getCellFormatter().setWidth(0, 4, "75px");
		headerTable.getCellFormatter().setWidth(0, 5, "150px");
		for (int j = 0; j < 6; j++) {
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}

		panel.add(headerTable);

		// Prepare table for channel info in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("400px");
		panel.add(scrollPanel);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		externalPanel.add(title);
		externalPanel.add(panel);

		initWidget(externalPanel);
	}

	private void setFields() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] fieldsMatrix) {
				if (fieldsMatrix.length == 1 && fieldsMatrix[0].length == 1) {
					if (PerifericoUI.SESSION_ENDED.equals(fieldsMatrix[0][0]))
						PerifericoUI.sessionEnded();
					else
						Window.alert(fieldsMatrix[0][0]);
				} else {
					// clear table
					Utils.clearTable(table);
					alarmsId = new String[fieldsMatrix.length];
					table.getCellFormatter().setWidth(0, 0, "80px");
					table.getCellFormatter().setWidth(0, 1, "100px");
					table.getCellFormatter().setWidth(0, 2, "250px");
					table.getCellFormatter().setWidth(0, 3, "325px");
					table.getCellFormatter().setWidth(0, 4, "75px");
					table.getCellFormatter().setWidth(0, 5, "150px");
					for (int i = 0; i < fieldsMatrix.length; i++) {
						alarmsId[i] = fieldsMatrix[i][0];

						// create button for link to history page
						Button historyButton = new Button();
						historyButton.setStyleName("gwt-button-history");
						historyButton.setTitle(PerifericoUI.getMessages()
								.station_status_link_history());
						table.setWidget(i, 0, historyButton);
						table.getCellFormatter().setAlignment(i, 0,
								HasHorizontalAlignment.ALIGN_CENTER,
								HasVerticalAlignment.ALIGN_MIDDLE);
						table.getCellFormatter().setStyleName(i, 0,
								"gwt-table-data");
						historyButton.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								Button button = (Button) event.getSource();
								for (int k = 0; k < table.getRowCount(); k++) {
									if (((Button) (table.getWidget(k, 0)))
											.equals(button)) {
										String alarmIdStr = alarmsId[k];
										String description = table
												.getText(k, 1)
												+ " - "
												+ table.getText(k, 2);
										Utils.blockForPopup("popup");
										// create and show popup of
										// ChoosePeriodWidget
										new ChoosePeriodWidget(
												false,
												true,
												alarmIdStr,
												null,
												description,
												null,
												ChoosePeriodWidget.ALARM_HISTORY,
												null,
												null,
												PerifericoUI.STATION_STATUS_WIDGET);
									}
								}
							}
						});

						for (int j = 1; j < 6; j++) {
							// set status icons
							if (j == 4) {
								if ("true".equals(fieldsMatrix[i][j])
										|| "ALARM".equals(fieldsMatrix[i][j])) {
									// red icon
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image ledRed = new Image();
									ledRed.setResource(iconImageBundle.ledRed());
									table.setWidget(i, j, ledRed);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages().alarm());
								} else if ("false".equals(fieldsMatrix[i][j])
										|| "OK".equals(fieldsMatrix[i][j])) {
									// green icon
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image ledGreen = new Image();
									ledGreen.setResource(iconImageBundle
											.ledGreen());
									table.setWidget(i, j, ledGreen);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages().ok());
								} else if ("WARNING".equals(fieldsMatrix[i][j])) {
									// yellow icon
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image ledYellow = new Image();
									ledYellow.setResource(iconImageBundle
											.ledYellow());
									table.setWidget(i, j, ledYellow);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages().warning());
								} else if ("WARNING_HIGH"
										.equals(fieldsMatrix[i][j])) {
									// warning high
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image warningHigh = new Image();
									warningHigh.setResource(iconImageBundle
											.warningHigh());
									table.setWidget(i, j, warningHigh);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages()
													.warning_high());
								} else if ("WARNING_LOW"
										.equals(fieldsMatrix[i][j])) {
									// warning low
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image warningLow = new Image();
									warningLow.setResource(iconImageBundle
											.warningLow());
									table.setWidget(i, j, warningLow);
									table.getWidget(i, j)
											.setTitle(
													PerifericoUI.getMessages()
															.warning_low());
								} else if ("ALARM_HIGH"
										.equals(fieldsMatrix[i][j])) {
									// alarm high
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image alarmHigh = new Image();
									alarmHigh.setResource(iconImageBundle
											.alarmHigh());
									table.setWidget(i, j, alarmHigh);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages().alarm_high());
								} else if ("ALARM_LOW"
										.equals(fieldsMatrix[i][j])) {
									// alarm low
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image alarmLow = new Image();
									alarmLow.setResource(iconImageBundle
											.alarmLow());
									table.setWidget(i, j, alarmLow);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages().alarm_low());
								} else {
									String value = fieldsMatrix[i][j];
									if (value == null || value.isEmpty())
										value = " ";
									table.setText(i, j, value);
								}
								table.getCellFormatter().setAlignment(i, j,
										HasHorizontalAlignment.ALIGN_CENTER,
										HasVerticalAlignment.ALIGN_MIDDLE);
							} else {
								String value = fieldsMatrix[i][j];
								if (value == null || value.isEmpty())
									value = " ";
								table.setText(i, j, value);
							}
							table.getCellFormatter().setStyleName(i, j,
									"gwt-table-data");
						}
					}
				}

			}
		};

		perifService.getStationStatusFields(callback);
	}

	@Override
	protected void loadContent() {
		setFields();
	}

}
