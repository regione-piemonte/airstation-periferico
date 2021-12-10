/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: page that shows the status of the container alarms
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: HistoryStationStatusWidget.java,v 1.24 2013/06/12 08:10:38 pfvallosio Exp $
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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * Widget that display history station status informations.
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class HistoryStationStatusWidget extends UIPage {

	private Label title = new Label();

	private FlexTable table;

	private String alarmIdString;

	private String description;

	private String startDateStr;

	private String startHourStr;

	private String endDateStr;

	private String endHourStr;

	private VerticalPanel externalPanel;

	private String[][] fieldsMatrix;

	public HistoryStationStatusWidget() {

		PanelButtonWidget panelButtonBack = new PanelButtonWidget();

		panelButtonBack.addButton(PerifericoUI.makeUpButtonBlue());
		panelButtonBack.addButton(PerifericoUI
				.makeViewHelpButton("historyStationStatusWidget"));

		externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().station_status_title(), panelButtonBack);
		// Label and panel for sub device info
		title.setStyleName("gwt-Label-title-blue");

		// panel that contains sub device info and buttons
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		// Prepare info table's title
		FlexTable headerTable = new FlexTable();
		headerTable.setText(0, 0,
				PerifericoUI.getMessages().station_status_alarm_timestamp());
		headerTable.setText(0, 1,
				PerifericoUI.getMessages().station_status_alarm_value());
		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("50%");
		headerTable.getCellFormatter().setWidth(0, 0, "245px");
		headerTable.getCellFormatter().setWidth(0, 1, "245px");
		for (int j = 0; j < 2; j++)
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");

		panel.add(headerTable);

		// create button for csv and graphic
		PanelButtonWidget panelButton = new PanelButtonWidget();

		panelButton.addButton(PerifericoUI.makeChartButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				/*
				 * PerifericoUI.historyStationStatusWidget.setVisible(false);
				 * PerifericoUI.chartImage.setVisible(true);
				 * PerifericoUI.chartImage.createChart(PerifericoUI.getMessages()
				 * .chart() + " " + HistoryStationStatusWidget.title.getText(),
				 * HistoryStationStatusWidget.title.getText(), fieldsMatrix);
				 */
			}
		}));

		panelButton.addButton(PerifericoUI.makeCsvButton(new ClickHandler() {
			public void onClick(ClickEvent event) {

				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String> callback = new UIAsyncCallback<String>() {
					public void onSuccess(String fileName) {
						// externalPanel.add(new HTML("<a
						// href='"+fileName+"'>prova</a>"));
						// externalPanel.add(new HTML("<a
						// href='./displayChart?filename=" +
						// fileName+"'>prova</a>"));
					}
				};

				perifService.makeCsv(callback);
			}
		}));

		// panel that contains button for chart and csv
		HorizontalPanel hPanel2 = new HorizontalPanel();
		hPanel2.setStyleName("gwt-button-panel");
		hPanel2.add(panelButton);
		hPanel2.setSpacing(10);
		hPanel2.setCellHorizontalAlignment(panelButton,
				HasHorizontalAlignment.ALIGN_CENTER);

		// Prepare table for history station status in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("50%");
		scrollPanel.add(table);
		scrollPanel.setHeight("400px");
		panel.add(scrollPanel);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		externalPanel.add(title);
		externalPanel.add(panel);
		// TODO: uncomment when CSV and CHART will be implemented
		// externalPanel.add(hPanel2);

		initWidget(externalPanel);
	}

	void setFields(String alarmIdStr, String title, String startDateStr,
			String startHourStr, String endDateStr, String endHourStr) {
		alarmIdString = alarmIdStr;
		description = title;
		this.startDateStr = startDateStr;
		this.startHourStr = startHourStr;
		this.endDateStr = endDateStr;
		this.endHourStr = endHourStr;
	}

	private void readFields() {
		this.title.setText(description);
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] result) {
				fieldsMatrix = result;
				if (fieldsMatrix.length == 1 && fieldsMatrix[0].length == 1) {
					if (PerifericoUI.SESSION_ENDED.equals(fieldsMatrix[0][0]))
						PerifericoUI.sessionEnded();
					else {
						PerifericoUI.goToUpperLevelPage();
						Utils.blockForPopup("popup");
						// create and show popup of ChoosePeriodWidget
						new ChoosePeriodWidget(false, true, alarmIdString,
								null, description, null,
								ChoosePeriodWidget.ALARM_HISTORY, null, null,
								PerifericoUI.HISTORY_STATION_STATUS_WIDGET);
						Window.alert(fieldsMatrix[0][0]);
					}
				} else {
					// clear table
					Utils.clearTable(table);
					table.getCellFormatter().setWidth(0, 0, "245px");
					table.getCellFormatter().setWidth(0, 1, "245px");
					// write content into table
					for (int i = 0; i < fieldsMatrix.length; i++) {
						for (int j = 0; j < 2; j++) {
							if (j == 1) {
								// set color icon
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
								} else
									table.setText(i, j, fieldsMatrix[i][j]);
								table.getCellFormatter().setAlignment(i, j,
										HasHorizontalAlignment.ALIGN_CENTER,
										HasVerticalAlignment.ALIGN_MIDDLE);
							} else
								table.setText(i, j, fieldsMatrix[i][j]);

							table.getCellFormatter().setStyleName(i, j,
									"gwt-table-data");
						}
					}
				}
			}
		};

		perifService.getHistoryStationStatusFields(alarmIdString, startDateStr,
				startHourStr, endDateStr, endHourStr, callback);
	}

	@Override
	protected void loadContent() {
		readFields();
	}

}
