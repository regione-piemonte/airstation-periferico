/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: page that shows real time data
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: HistoryRealTimeDataWidget.java,v 1.38 2013/06/12 08:10:37 pfvallosio Exp $
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
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * Widget that displays history real time data.
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class HistoryRealTimeDataWidget extends UIPage {

	private Label title = new Label();

	private FlexTable table;

	private FlexTable headerTable;

	private String[][] fieldsMatrix;

	private String analyzerIdString;

	private String elementIdString;

	private String description;

	private String requestedDateString;

	private VerticalPanel hourlyChoicePanel;

	private HorizontalPanel minutes10ChoicePanel;

	private String hourSelectedStr = null;

	private FormPanel formPanel = new FormPanel();

	private Hidden analyzerIdHidden;

	private Hidden elementIdHidden;

	private Hidden reqDate;

	private Hidden reqHour;

	private Hidden minutesStrHidden;

	private String reqHourStr;

	private String reqMinutesStr;

	private Label lblNoData;

	public HistoryRealTimeDataWidget() {

		PanelButtonWidget panelButton = new PanelButtonWidget();

		panelButton.addButton(PerifericoUI.makeChartButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (fieldsMatrix == null)
					Window.alert(PerifericoUI.getMessages().error_open_chart());
				else {
					String[][] matrix = new String[fieldsMatrix.length - 1][2];
					String measureUnit = fieldsMatrix[0][0];
					String periodic = fieldsMatrix[0][1];
					for (int i = 0; i < matrix.length; i++) {
						matrix[i][0] = fieldsMatrix[i + 1][1];
						matrix[i][1] = fieldsMatrix[i + 1][0];
					}
					PerifericoUI.getShowChartWidget().title
							.setText(PerifericoUI.getMessages().real_time_data());
					PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
							.create(PerifericoUIService.class);
					ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
					endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
							+ "uiservice");
					AsyncCallback<String> callback = new UIAsyncCallback<String>() {

						public void onSuccess(String chartName) {
							// TODO: capire come fare per intercettare
							// stringa di errore restituita
							PerifericoUI.getShowChartWidget()
									.setChartName(chartName);
							PerifericoUI
									.setCurrentPage(PerifericoUI.getShowChartWidget());
						}
					};

					perifService.generateChart(title.getText(),
							title.getText(), measureUnit, matrix, true,
							"true".equals(periodic), callback);
				}// end else
			}// end onClick
		}));

		panelButton.addButton(PerifericoUI.makeCsvButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				formPanel.submit();
			}// end onClick
		}));

		panelButton.addButton(PerifericoUI.makeUpButtonBlue());
		panelButton.addButton(PerifericoUI
				.makeViewHelpButton("historyRealTimeDataWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().instant_data(), panelButton);
		// Label and panel for sub device info
		title.setStyleName("gwt-Label-title-blue");

		// panel that contains sub device info and buttons
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		// horizontal panel for table and hour buttons
		HorizontalPanel hPanel = new HorizontalPanel();
		panel.add(hPanel);

		// Vertical panel containing header table and table
		VerticalPanel vPanel = new VerticalPanel();

		// Prepare info table's title
		headerTable = new FlexTable();
		headerTable.setText(0, 0, PerifericoUI.getMessages().data());
		headerTable.setText(0, 1, PerifericoUI.getMessages().value());
		headerTable.setText(0, 2, PerifericoUI.getMessages().flag());
		headerTable.setText(0, 3, PerifericoUI.getMessages().multiple_flag());

		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "356px");
		headerTable.getCellFormatter().setWidth(0, 1, "178px");
		headerTable.getCellFormatter().setWidth(0, 2, "178px");
		headerTable.getCellFormatter().setWidth(0, 3, "193px");
		for (int j = 0; j < headerTable.getCellCount(0); j++) {
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}
		vPanel.add(headerTable);

		// form panel that contains hidden fields for csv
		formPanel.setAction("./exportcsvService");
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		formPanel.setMethod(FormPanel.METHOD_GET);
		VerticalPanel vPanelForForm = new VerticalPanel();
		formPanel.setWidget(vPanelForForm);
		Hidden localeHidden = new Hidden();
		localeHidden.setName("locale");
		localeHidden.setValue(PerifericoUI.getLocale());
		vPanelForForm.add(localeHidden);
		Hidden functionHidden = new Hidden();
		functionHidden.setName("function");
		functionHidden.setValue("getRealTimeData");
		vPanelForForm.add(functionHidden);
		analyzerIdHidden = new Hidden();
		analyzerIdHidden.setName("analyzerIdStr");
		vPanelForForm.add(analyzerIdHidden);
		elementIdHidden = new Hidden();
		elementIdHidden.setName("elementIdStr");
		vPanelForForm.add(elementIdHidden);
		reqDate = new Hidden();
		reqDate.setName("reqDate");
		vPanelForForm.add(reqDate);
		reqHour = new Hidden();
		reqHour.setName("hourStr");
		vPanelForForm.add(reqHour);
		minutesStrHidden = new Hidden();
		minutesStrHidden.setName("minutesStr");
		vPanelForForm.add(minutesStrHidden);

		formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String msg = event.getResults().substring("<pre>".length(),
						event.getResults().length() - "</pre>".length());
				Window.alert(msg);
			}
		});
		panel.add(formPanel);

		// Prepare hourly choice buttons
		hourlyChoicePanel = new VerticalPanel();
		hourlyChoicePanel.setVisible(false);
		for (int i = 0; i < 24; i++) {
			StringBuffer buttonValue = new StringBuffer(
					new Integer(i).toString());
			if (i < 10)
				buttonValue.insert(0, "0");
			Button hourButton = new Button(buttonValue.toString());
			hourButton.setStyleName("gwt-button-hour");
			hourlyChoicePanel.add(hourButton);
			hourButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					Button pressedButton = (Button) event.getSource();
					clearButtons(true, null);
					pressedButton.removeStyleName("gwt-button-hour");
					pressedButton.setStyleName("gwt-button-hour-selected");
					// TODO: capire se va bene cosi' per capire che bottone e'
					getData(pressedButton.getText(), null);
				}
			});
		}
		hPanel.add(hourlyChoicePanel);

		// Prepare 10 minutes choice buttons
		minutes10ChoicePanel = new HorizontalPanel();
		minutes10ChoicePanel.setVisible(false);
		VerticalPanel hourPanel = new VerticalPanel();
		VerticalPanel minutesPanel = new VerticalPanel();
		minutes10ChoicePanel.add(hourPanel);
		minutes10ChoicePanel.add(minutesPanel);
		for (int i = 0; i < 24; i++) {
			StringBuffer buttonValue = new StringBuffer(
					new Integer(i).toString());
			if (i < 10)
				buttonValue.insert(0, "0");
			Button hourButton = new Button(buttonValue.toString());
			hourButton.setStyleName("gwt-button-hour");
			hourPanel.add(hourButton);
			hourButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					Button pressedButton = (Button) event.getSource();
					clearButtons(true, new Integer(0));
					clearButtons(true, new Integer(1));
					pressedButton.removeStyleName("gwt-button-hour");
					pressedButton.setStyleName("gwt-button-hour-selected");
					// TODO: capire se va bene
					hourSelectedStr = pressedButton.getText();
				}
			});
		}
		for (int i = 0; i < 60; i += 10) {
			StringBuffer buttonValue = new StringBuffer(
					new Integer(i).toString());
			if (i < 10)
				buttonValue.insert(0, "0");
			Button minutesButton = new Button(buttonValue.toString());
			minutesButton.setStyleName("gwt-button-hour");
			minutesPanel.add(minutesButton);
			minutesButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					Button pressedButton = (Button) event.getSource();
					clearButtons(true, new Integer(1));
					pressedButton.removeStyleName("gwt-button-hour");
					pressedButton.setStyleName("gwt-button-hour-selected");
					// TODO: capire se va bene cosi' per capire che bottone e'
					if (hourSelectedStr != null)
						getData(hourSelectedStr, pressedButton.getText());
				}
			});
		}
		hPanel.add(minutes10ChoicePanel);

		// Prepare table for history real time in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("400px");
		vPanel.add(scrollPanel);
		hPanel.add(vPanel);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		externalPanel.add(title);
		externalPanel.add(panel);

		lblNoData = new Label();
		lblNoData.setText(PerifericoUI.getMessages().no_data());
		lblNoData.setVisible(false);
		externalPanel.add(lblNoData);
		initWidget(externalPanel);
	}

	void setFields(String analyzerIdStr, String elementIdStr, String title,
			String reqDateStr) {
		analyzerIdString = analyzerIdStr;
		elementIdString = elementIdStr;
		description = title;
		requestedDateString = reqDateStr;
	}

	private void readFields() {
		title.setText(description);
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resultString) {
				if (resultString.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else {
					Integer acqPeriod = null;
					if (!resultString.isEmpty())
						acqPeriod = new Integer(resultString);
					// clear table, all buttons selected and matrix for chart
					fieldsMatrix = null;
					// case of hourly data matrix
					if (acqPeriod == null || acqPeriod.intValue() >= 60) {
						minutes10ChoicePanel.setVisible(false);
						hourlyChoicePanel.setVisible(true);
					}
					// case of 10 minutes data matrix
					else {
						hourlyChoicePanel.setVisible(false);
						minutes10ChoicePanel.setVisible(true);
					}
				}

			}
		};

		perifService.getAcqPeriod(analyzerIdString, elementIdString, callback);
	}

	private void getData(String hourStr, String minutesStr) {
		reqHourStr = hourStr;
		reqMinutesStr = minutesStr;
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] result) {
				Utils.unlockForPopup("loading");
				fieldsMatrix = result;
				if (fieldsMatrix.length == 1 && fieldsMatrix[0].length == 1) {
					if (PerifericoUI.SESSION_ENDED.equals(fieldsMatrix[0][0]))
						PerifericoUI.sessionEnded();
					else {
						PerifericoUI.goToUpperLevelPage();
						Utils.blockForPopup("popup");
						// create and show popup of ChoosePeriodWidget
						new ChoosePeriodWidget(false, true, analyzerIdString,
								elementIdString, description, null,
								ChoosePeriodWidget.SAMPLE_HISTORY, null, null,
								PerifericoUI.HISTORY_REAL_TIME_DATA_WIDGET);
						Window.alert(fieldsMatrix[0][0]);
					}
				} else {
					Utils.clearTable(table);
					table.getCellFormatter().setWidth(0, 0, "356px");
					table.getCellFormatter().setWidth(0, 1, "178px");
					table.getCellFormatter().setWidth(0, 2, "178px");
					table.getCellFormatter().setWidth(0, 3, "178px");
					if (fieldsMatrix.length <= 1)
						lblNoData.setVisible(true);
					else
						lblNoData.setVisible(false);
					if (fieldsMatrix.length > 0 && fieldsMatrix[0].length > 0)
						headerTable.setText(0, 1, PerifericoUI.getMessages().value()
								+ " [" + fieldsMatrix[0][0] + "]");
					// write content into table
					for (int i = 1; i < fieldsMatrix.length; i++) {
						for (int j = 0; j < 4; j++) {
							String cellText = fieldsMatrix[i][j];
							// Hack to display correctly in IE
							if (cellText == null || cellText.isEmpty())
								cellText = " ";
							if (j == 2) { // not valid flag
								if ("true".equals(cellText))
									cellText = PerifericoUI.getMessages()
											.not_valid();
								else if ("false".equals(cellText))
									cellText = PerifericoUI.getMessages().valid();
								table.setText(i, j, cellText);
							} else if (j == 3) { // multiple flag
								if (fieldsMatrix[i].length >= 5) {
									ClickWidget clickWidget = new ClickWidget(
											(fieldsMatrix[i][4] != null ? fieldsMatrix[i][4]
													: ""), cellText);
									table.setWidget(i, j, clickWidget);
								} else
									table.setText(i, j, cellText);
							} else
								table.setText(i, j, cellText);
							table.getCellFormatter().setStyleName(i, j,
									"gwt-table-data");
						}
					}
					// set visible panel for chart and csv button and set
					// hidden value for csv
					analyzerIdHidden.setValue(analyzerIdString);
					elementIdHidden.setValue(elementIdString);
					reqDate.setValue(requestedDateString);
					reqHour.setValue(reqHourStr);
					minutesStrHidden.setValue(reqMinutesStr);
				}

			}

			@Override
			public void onFailure(Throwable caught) {
				Utils.unlockForPopup("loading");
				super.onFailure(caught);
			}
		};
		Utils.blockForPopup("loading");
		perifService.getHistoryRealTimeDataFields(analyzerIdString,
				elementIdString, requestedDateString, hourStr, minutesStr,
				callback);
	}

	private void clearButtons(boolean onlyVisible, Integer numWidget) {
		if (!onlyVisible || (onlyVisible && hourlyChoicePanel.isVisible())) {
			for (int i = 0; i < 24; i++) {
				Button button = (Button) hourlyChoicePanel.getWidget(i);
				button.removeStyleName("gwt-button-hour-selected");
				button.setStyleName("gwt-button-hour");
			}
		}
		if (!onlyVisible || (onlyVisible && minutes10ChoicePanel.isVisible())) {
			VerticalPanel vPanel = (VerticalPanel) minutes10ChoicePanel
					.getWidget(numWidget.intValue());
			int numButtons = (numWidget.intValue() == 0 ? 24 : 6);
			for (int j = 0; j < numButtons; j++) {
				Button button = (Button) vPanel.getWidget(j);
				button.removeStyleName("gwt-button-hour-selected");
				button.setStyleName("gwt-button-hour");
			}

		}
	}

	@Override
	protected void reset() {
		Utils.clearTable(table);
		clearButtons(false, new Integer(0));
		clearButtons(false, new Integer(1));
		hourSelectedStr = null;
	}

	@Override
	protected void loadContent() {
		readFields();
	}
}
