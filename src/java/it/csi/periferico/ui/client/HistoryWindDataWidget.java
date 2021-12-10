/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Pierfrancesco Vallosio
 * Purpose of file: page that shows wind vectorial aggregate data
 * Change log:
 *   2009-01-23: initial version
 * ----------------------------------------------------------------------------
 * $Id: HistoryWindDataWidget.java,v 1.11 2013/06/12 08:10:38 pfvallosio Exp $
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
 * Widget that displays a table for wind aggregate data.
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class HistoryWindDataWidget extends UIPage {

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

	private FormPanel formPanel = new FormPanel();

	public HistoryWindDataWidget() {

		PanelButtonWidget panelButton = new PanelButtonWidget();

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
		headerTable.setText(0, 1, PerifericoUI.getMessages().lbl_vect_speed());
		headerTable.setText(0, 2, PerifericoUI.getMessages().lbl_vect_dir());
		headerTable.setText(0, 3, PerifericoUI.getMessages().lbl_dev_std());
		headerTable.setText(0, 4, PerifericoUI.getMessages().lbl_scalar_speed());
		headerTable.setText(0, 5, PerifericoUI.getMessages().lbl_gust_speed());
		headerTable.setText(0, 6, PerifericoUI.getMessages().lbl_gust_dir());
		headerTable.setText(0, 7, PerifericoUI.getMessages().lbl_calm_percent());
		headerTable.setText(0, 8, PerifericoUI.getMessages().lbl_is_calm());
		headerTable.setText(0, 9, PerifericoUI.getMessages().flag());
		headerTable.setText(0, 10, PerifericoUI.getMessages().multiple_flag());

		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "125px");
		headerTable.getCellFormatter().setWidth(0, 1, "75px");
		headerTable.getCellFormatter().setWidth(0, 2, "75px");
		headerTable.getCellFormatter().setWidth(0, 3, "75px");
		headerTable.getCellFormatter().setWidth(0, 4, "75px");
		headerTable.getCellFormatter().setWidth(0, 5, "75px");
		headerTable.getCellFormatter().setWidth(0, 6, "75px");
		headerTable.getCellFormatter().setWidth(0, 7, "75px");
		headerTable.getCellFormatter().setWidth(0, 8, "75px");
		headerTable.getCellFormatter().setWidth(0, 9, "75px");
		headerTable.getCellFormatter().setWidth(0, 10, "80px");
		for (int j = 0; j < 11; j++) {
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}
		vPanel.add(headerTable);

		// Button chartButton = panelButton.getButton(PerifericoUI.CHART);
		// chartButton.addClickHandler(new ClickHandler() {
		// public void onClick(ClickEvent event) {
		// if (fieldsMatrix == null)
		// Window.alert(PerifericoUI.getMessages().error_open_chart());
		// else {
		// String[][] matrix = new String[fieldsMatrix.length][2];
		// for (int i = 0; i < fieldsMatrix.length; i++) {
		// matrix[i][0] = fieldsMatrix[i][0];
		// matrix[i][1] = fieldsMatrix[i][2];
		// }
		// // TODO modificare codice per farlo funzionare
		// PerifericoUI.historyWindDataWidget.setVisible(false);
		// PerifericoUI.showChartWidget.title.setText(title.getText());
		// PerifericoUI.showChartWidget
		// .setBackWidget(PerifericoUI.HISTORY_WIND_DATA_WIDGET);
		// PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync)
		// GWT
		// .create(PerifericoUIService.class);
		// ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		// endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
		// + "uiservice");
		// AsyncCallback<String> callback = new UIAsyncCallback<String>() {
		//
		// public void onSuccess(String chartName) {
		// // TODO: capire come fare per intercettare
		// // stringa di errore restituita
		// PerifericoUI.historyWindDataWidget
		// .setVisible(false);
		// PerifericoUI.showChartWidget.setVisible(true);
		// PerifericoUI.showChartWidget.setImage(chartName);
		// }
		// };
		//
		// // TODO aggiungere measureunits
		// perifService.generateChart(title.getText(),
		// title.getText(), "", matrix, false, true, callback);
		// }
		// }
		// });

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

		initWidget(externalPanel);
	}

	void setFields(String analyzerIdStr, String elementIdStr, String periodStr,
			String[] avgPeriods, String chooseType, String halfDayStr,
			final String title, String startDateStr, String startHourStr,
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
		// Change null with "" because IE7 converts null to the string "null"
		maxDaysHidden.setValue(maxDays == null ? "" : maxDays);
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
								ChoosePeriodWidget.WIND_HISTORY, null, null,
								PerifericoUI.HISTORY_WIND_DATA_WIDGET);
						Window.alert(fieldsMatrix[0][0]);
					}
				} else {
					if (fieldsMatrix.length > 0 && fieldsMatrix[0].length > 2) {
						MessageBundleClient mbc = PerifericoUI.getMessages();
						String mu = " [" + fieldsMatrix[0][0] + "]";
						headerTable.setText(0, 1, mbc.lbl_vect_speed() + mu);
						headerTable.setText(0, 4, mbc.lbl_scalar_speed() + mu);
						headerTable.setText(0, 5, mbc.lbl_gust_speed() + mu);
						mu = " [" + fieldsMatrix[0][1] + "]";
						headerTable.setText(0, 2, mbc.lbl_vect_dir() + mu);
						headerTable.setText(0, 6, mbc.lbl_gust_dir() + mu);
						lblTitle.setText(description + " - "
								+ fieldsMatrix[0][2] + " min");
					}
					// write content into table
					for (int i = 1; i < fieldsMatrix.length; i++) {
						for (int j = 0; j < 11; j++) {
							String cellText = fieldsMatrix[i][j];
							// Hack to display correctly in IE
							if (cellText == null || cellText.isEmpty())
								cellText = " ";
							if (j == 8) {
								// flag calm-not calm
								if ("true".equals(cellText))
									table.setText(i, j,
											PerifericoUI.getMessages().yes());
								else if ("false".equals(cellText))
									table.setText(i, j,
											PerifericoUI.getMessages().no());
								else
									table.setText(i, j, cellText);
							} else if (j == 9) {
								// flag valid-not valid
								if ("true".equals(cellText))
									table.setText(i, j,
											PerifericoUI.getMessages().not_valid());
								else if ("false".equals(cellText))
									table.setText(i, j,
											PerifericoUI.getMessages().valid());
								else
									table.setText(i, j, cellText);
							} else if (j == 10) {
								// multiple flags
								if (fieldsMatrix[i].length == 12) {
									ClickWidget clickWidget = new ClickWidget(
											(fieldsMatrix[i][11] != null ? fieldsMatrix[i][11]
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
		// clear table
		Utils.clearTable(table);
		table.getCellFormatter().setWidth(0, 0, "125px");
		table.getCellFormatter().setWidth(0, 1, "75px");
		table.getCellFormatter().setWidth(0, 2, "75px");
		table.getCellFormatter().setWidth(0, 3, "75px");
		table.getCellFormatter().setWidth(0, 4, "75px");
		table.getCellFormatter().setWidth(0, 5, "75px");
		table.getCellFormatter().setWidth(0, 6, "75px");
		table.getCellFormatter().setWidth(0, 7, "75px");
		table.getCellFormatter().setWidth(0, 8, "75px");
		table.getCellFormatter().setWidth(0, 9, "75px");
		table.getCellFormatter().setWidth(0, 10, "65px");
	}

	@Override
	protected void loadContent() {
		readFields();
	}

}
