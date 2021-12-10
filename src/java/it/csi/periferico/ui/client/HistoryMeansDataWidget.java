/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: page that shows aggregate values
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: HistoryMeansDataWidget.java,v 1.28 2013/06/12 08:10:37 pfvallosio Exp $
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
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * Widget that displays history means data.
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class HistoryMeansDataWidget extends UIPage {

	private Label lblTitle = new Label();

	private FlexTable table;

	private FlexTable headerTable;

	private String[][] fieldsMatrix;

	private String analyzerIdString;

	private String elementIdString;

	private String description;

	private String[] avgPeriodStrings;

	private Hidden analyzerIdHidden;

	private Hidden elementIdHidden;

	private Hidden periodStrHidden;

	private Hidden chooseTypeHidden;

	private Hidden halfDayStrHidden;

	private Hidden startDateStrHidden;

	private Hidden startHourStrHidden;

	private Hidden endDateStrHidden;

	private Hidden endHourStrHidden;

	private Hidden maxDaysHidden;

	private String measureUnit = "";

	private boolean typeTotal = false;

	private FormPanel formPanel = new FormPanel();

	public HistoryMeansDataWidget() {

		PanelButtonWidget panelButton = new PanelButtonWidget();

		panelButton.addButton(PerifericoUI.makeChartButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (fieldsMatrix == null)
					Window.alert(PerifericoUI.getMessages().error_open_chart());
				else {
					String[][] matrix = new String[fieldsMatrix.length - 1][2];
					for (int i = 0; i < matrix.length; i++) {
						matrix[i][0] = fieldsMatrix[i + 1][1];
						matrix[i][1] = fieldsMatrix[i + 1][0];
					}
					// TODO modificare codice per farlo funzionare
					PerifericoUI.getShowChartWidget().title
							.setText(PerifericoUI.getMessages().means_data());
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

					perifService.generateChart(lblTitle.getText(),
							lblTitle.getText(), measureUnit, matrix, false,
							!typeTotal, callback);
				}
			}
		}));

		panelButton.addButton(PerifericoUI.makeCsvButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				formPanel.submit();
			}
		}));

		panelButton.addButton(PerifericoUI.makeUpButtonBlue());
		panelButton.addButton(PerifericoUI
				.makeViewHelpButton("historyMeansDataWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().means_data_title(), panelButton);
		// Label and panel for means data
		lblTitle.setStyleName("gwt-Label-title-blue");

		// Vertical panel contaning header table and table
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.setStyleName("gwt-post-boxed-blue");

		// Prepare info table's title
		headerTable = new FlexTable();
		headerTable.setText(0, 0, PerifericoUI.getMessages().data());
		headerTable.setText(0, 1, PerifericoUI.getMessages().value());
		headerTable.setText(0, 2, PerifericoUI.getMessages().flag());
		headerTable.setText(0, 3, PerifericoUI.getMessages().multiple_flag());

		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "386px");
		headerTable.getCellFormatter().setWidth(0, 1, "193px");
		headerTable.getCellFormatter().setWidth(0, 2, "193px");
		headerTable.getCellFormatter().setWidth(0, 3, "208px");
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
		functionHidden.setValue("getMeansData");
		vPanelForForm.add(functionHidden);
		analyzerIdHidden = new Hidden();
		analyzerIdHidden.setName("analyzerIdStr");
		vPanelForForm.add(analyzerIdHidden);
		elementIdHidden = new Hidden();
		elementIdHidden.setName("elementIdStr");
		vPanelForForm.add(elementIdHidden);
		periodStrHidden = new Hidden();
		periodStrHidden.setName("periodStr");
		vPanelForForm.add(periodStrHidden);
		chooseTypeHidden = new Hidden();
		chooseTypeHidden.setName("chooseType");
		vPanelForForm.add(chooseTypeHidden);
		halfDayStrHidden = new Hidden();
		halfDayStrHidden.setName("halfDayStr");
		vPanelForForm.add(halfDayStrHidden);
		startDateStrHidden = new Hidden();
		startDateStrHidden.setName("startDateStr");
		vPanelForForm.add(startDateStrHidden);
		startHourStrHidden = new Hidden();
		startHourStrHidden.setName("startHourStr");
		vPanelForForm.add(startHourStrHidden);
		endDateStrHidden = new Hidden();
		endDateStrHidden.setName("endDateStr");
		vPanelForForm.add(endDateStrHidden);
		endHourStrHidden = new Hidden();
		endHourStrHidden.setName("endHourStr");
		vPanelForForm.add(endHourStrHidden);
		maxDaysHidden = new Hidden();
		maxDaysHidden.setName("maxDays");
		vPanelForForm.add(maxDaysHidden);

		formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String msg = event.getResults().substring("<pre>".length(),
						event.getResults().length() - "</pre>".length());
				Window.alert(msg);
			}
		});
		vPanel.add(formPanel);

		// Prepare table for history means data in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("400px");
		vPanel.add(scrollPanel);
		externalPanel.add(lblTitle);
		externalPanel.add(vPanel);
		// externalPanel.add(hPanel2);

		initWidget(externalPanel);
	}

	void setFields(String analyzerIdStr, String elementIdStr, String periodStr,
			String[] avgPeriods, String chooseType, String halfDayStr,
			String title, String startDateStr, String startHourStr,
			String endDateStr, String endHourStr, String maxDays) {
		analyzerIdString = analyzerIdStr;
		elementIdString = elementIdStr;
		description = title;
		avgPeriodStrings = avgPeriods;
		// set hidden values for form to create csv
		analyzerIdHidden.setValue(analyzerIdStr);
		elementIdHidden.setValue(elementIdStr);
		periodStrHidden.setValue(periodStr);
		chooseTypeHidden.setValue(chooseType);
		halfDayStrHidden.setValue(halfDayStr);
		startDateStrHidden.setValue(startDateStr);
		startHourStrHidden.setValue(startHourStr);
		endDateStrHidden.setValue(endDateStr);
		endHourStrHidden.setValue(endHourStr);
		maxDaysHidden.setValue(maxDays);
	}

	private void readFields() {
		lblTitle.setText(description);
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
								elementIdString, description, avgPeriodStrings,
								ChoosePeriodWidget.MEANS_HISTORY, null, null,
								PerifericoUI.HISTORY_MEANS_DATA_WIDGET);
						Window.alert(fieldsMatrix[0][0]);
					}
				} else {
					if (fieldsMatrix.length > 0) {
						if (fieldsMatrix[0].length >= 1) {
							measureUnit = fieldsMatrix[0][0];
							headerTable.setText(0, 1,
									PerifericoUI.getMessages().value() + " ["
											+ measureUnit + "]");
						}
						if (fieldsMatrix[0].length >= 2)
							lblTitle.setText(description + " - "
									+ fieldsMatrix[0][1] + " min");
						if (fieldsMatrix[0].length >= 3)
							typeTotal = "total".equals(fieldsMatrix[0][2]);
					}
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
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				Utils.unlockForPopup("loading");
				super.onFailure(caught);
			}
		};

		Utils.blockForPopup("loading");
		perifService.getHistoryMeansDataFields(analyzerIdString,
				elementIdString, periodStrHidden.getValue(),
				chooseTypeHidden.getValue(), halfDayStrHidden.getValue(),
				startDateStrHidden.getValue(), startHourStrHidden.getValue(),
				endDateStrHidden.getValue(), endHourStrHidden.getValue(),
				maxDaysHidden.getValue(), callback);
	}

	@Override
	protected void reset() {
		Utils.clearTable(table);
		table.getCellFormatter().setWidth(0, 0, "386px");
		table.getCellFormatter().setWidth(0, 1, "193px");
		table.getCellFormatter().setWidth(0, 2, "193px");
		table.getCellFormatter().setWidth(0, 3, "193px");
	}

	@Override
	protected void loadContent() {
		readFields();
	}

}
