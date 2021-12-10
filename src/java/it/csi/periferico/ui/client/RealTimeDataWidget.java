/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: displays real time data
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: RealTimeDataWidget.java,v 1.38 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.UIPage;

import java.util.HashMap;
import java.util.Map;

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * Widget that displays real time data.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class RealTimeDataWidget extends UIPage {

	private Label title = new Label();

	private String[] analyzersId;

	private String[] analyzersType;

	private String[] elementsId;

	private String[] measureUnit;

	private String analyzerIdString;

	private String elementIdString;

	private String measureUnitString;

	private String description;

	private FlexTable headerTable;

	private boolean viewDeletedAnalyzers = false;

	public final FlexTable table;

	public RealTimeDataWidget() {

		PanelButtonWidget panelButtonRefresh = new PanelButtonWidget();
		panelButtonRefresh.addButton(PerifericoUI.makeRefreshButton());
		panelButtonRefresh.addButton(PerifericoUI
				.makeViewDeletedAnalyzersButton(new ClickHandler() {
					public void onClick(ClickEvent event) {
						viewDeletedAnalyzers = true;
						PerifericoUI.updateCurrentPage(null);
					}
				}));
		panelButtonRefresh.addButton(PerifericoUI
				.makeViewHelpButton("realTimeDataWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().real_time_title(), panelButtonRefresh);

		// Label and panel for subdevice info
		title.setStyleName("gwt-Label-title-blue");
		title.setText(PerifericoUI.getMessages().real_time_title());

		// panel that contains subdevice info and buttons
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		// Prepare info table's title
		headerTable = new FlexTable();
		headerTable.setText(0, 0,
				PerifericoUI.getMessages().real_time_link_history());
		headerTable.setText(0, 1, PerifericoUI.getMessages().real_time_link_means());
		headerTable.setText(0, 2, PerifericoUI.getMessages().real_time_analyzer());
		headerTable.setText(0, 3, PerifericoUI.getMessages().last_istant_value());
		headerTable.setText(0, 4, PerifericoUI.getMessages().measure_unit());
		headerTable.setText(0, 5, PerifericoUI.getMessages().data());
		headerTable.setText(0, 6, PerifericoUI.getMessages().flag());
		headerTable.setText(0, 7, PerifericoUI.getMessages().multiple_flag());
		headerTable.setText(0, 8, PerifericoUI.getMessages().periodicity());

		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "100px");
		headerTable.getCellFormatter().setWidth(0, 1, "100px");
		headerTable.getCellFormatter().setWidth(0, 2, "165px");
		headerTable.getCellFormatter().setWidth(0, 3, "105px");
		headerTable.getCellFormatter().setWidth(0, 4, "90px");
		headerTable.getCellFormatter().setWidth(0, 5, "150px");
		headerTable.getCellFormatter().setWidth(0, 6, "80px");
		headerTable.getCellFormatter().setWidth(0, 7, "75px");
		headerTable.getCellFormatter().setWidth(0, 8, "105px");

		for (int j = 0; j < 9; j++) {
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

	private void readFields() {
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
					analyzersId = new String[fieldsMatrix.length];
					analyzersType = new String[fieldsMatrix.length];
					elementsId = new String[fieldsMatrix.length];
					measureUnit = new String[fieldsMatrix.length];
					table.getCellFormatter().setWidth(0, 0, "100px");
					table.getCellFormatter().setWidth(0, 1, "100px");
					table.getCellFormatter().setWidth(0, 2, "165px");
					table.getCellFormatter().setWidth(0, 3, "105px");
					table.getCellFormatter().setWidth(0, 4, "90px");
					table.getCellFormatter().setWidth(0, 5, "150px");
					table.getCellFormatter().setWidth(0, 6, "80px");
					table.getCellFormatter().setWidth(0, 7, "75px");
					if (fieldsMatrix.length <= 12)
						table.getCellFormatter().setWidth(0, 8, "105px");
					else
						table.getCellFormatter().setWidth(0, 8, "95px");
					final Map<Button, Integer> csvButtonIndex_rt = new HashMap<Button, Integer>();
					final Map<Button, Integer> chartButtonIndex_rt = new HashMap<Button, Integer>();
					final Map<Button, Integer> tableButtonIndex_rt = new HashMap<Button, Integer>();
					final Map<Button, Integer> csvButtonIndex_means = new HashMap<Button, Integer>();
					final Map<Button, Integer> chartButtonIndex_means = new HashMap<Button, Integer>();
					final Map<Button, Integer> tableButtonIndex_means = new HashMap<Button, Integer>();
					for (int i = 0; i < fieldsMatrix.length; i++) {
						analyzersId[i] = fieldsMatrix[i][0];
						analyzersType[i] = fieldsMatrix[i][10];
						elementsId[i] = fieldsMatrix[i][1];
						measureUnit[i] = fieldsMatrix[i][4];
						// create buttons for history
						PanelButtonWidget panelButton1 = new PanelButtonWidget(
								3);
						// csv button
						Button csvButton1 = PerifericoUI
								.makeCsvSmallButton(new ClickHandler() {
									public void onClick(ClickEvent event) {
										Button button = (Button) event
												.getSource();
										Integer k = csvButtonIndex_rt
												.get(button);
										if (k == null)
											return;
										analyzerIdString = analyzersId[k];
										elementIdString = elementsId[k];
										description = table.getText(k, 2);
										// getting acqPeriod to decide how
										// ChoosePeriodWidget should be created
										PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
												.create(PerifericoUIService.class);
										ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
										endpoint2.setServiceEntryPoint(GWT
												.getModuleBaseURL()
												+ "uiservice");
										AsyncCallback<String> callback2 = new UIAsyncCallback<String>() {
											public void onSuccess(
													String resultString) {
												if (resultString
														.equals(PerifericoUI.SESSION_ENDED)) {
													PerifericoUI.sessionEnded();
													return;
												}
												String maxPeriod = "1"
														.equals(resultString) ? ChoosePeriodWidget.HALF_DAY
														: ChoosePeriodWidget.ONE_DAY;
												Utils.blockForPopup("popup");
												new ChoosePeriodWidget(
														false,
														true,
														analyzerIdString,
														elementIdString,
														description,
														null,
														ChoosePeriodWidget.SAMPLE_DOWNLOAD_CSV,
														maxPeriod,
														null,
														PerifericoUI.REAL_TIME_DATA_WIDGET);
											}
										};

										perifService2.getAcqPeriod(
												analyzerIdString,
												elementIdString, callback2);
									}
								});
						csvButtonIndex_rt.put(csvButton1, i);
						panelButton1.addButton(csvButton1);

						// chart button
						Button chartButton1 = PerifericoUI
								.makeChartSmallButton(new ClickHandler() {
									public void onClick(ClickEvent event) {
										// TODO modificare la scelta: max 1
										// giorno, se invece acquisisce a 1
										// secondo solo mezza giornata
										Button button = (Button) event
												.getSource();
										Integer k = chartButtonIndex_rt
												.get(button);
										if (k == null)
											return;
										analyzerIdString = analyzersId[k];
										elementIdString = elementsId[k];
										measureUnitString = measureUnit[k];
										description = table.getText(k, 2);
										// getting acqPeriod to decide what
										// ChoosePeriodWidget must create
										PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
												.create(PerifericoUIService.class);
										ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
										endpoint2.setServiceEntryPoint(GWT
												.getModuleBaseURL()
												+ "uiservice");
										AsyncCallback<String> callback2 = new UIAsyncCallback<String>() {
											public void onSuccess(
													String resultString) {
												if (resultString
														.equals(PerifericoUI.SESSION_ENDED)) {
													PerifericoUI.sessionEnded();
													return;
												}
												Integer acqPeriod = null;
												if (!resultString.isEmpty())
													acqPeriod = new Integer(
															resultString);
												String maxPeriod = ChoosePeriodWidget.ONE_DAY;
												if (acqPeriod != null
														&& acqPeriod.intValue() == 1)
													maxPeriod = ChoosePeriodWidget.HALF_DAY;
												Utils.blockForPopup("popup");
												new ChoosePeriodWidget(
														false,
														true,
														analyzerIdString,
														elementIdString,
														description,
														null,
														ChoosePeriodWidget.SAMPLE_DOWNLOAD_CHART,
														maxPeriod,
														measureUnitString,
														PerifericoUI.REAL_TIME_DATA_WIDGET);
											}
										};

										perifService2.getAcqPeriod(
												analyzerIdString,
												elementIdString, callback2);
									}
								});
						chartButtonIndex_rt.put(chartButton1, i);
						panelButton1.addButton(chartButton1);

						// table button
						Button tableButton1 = PerifericoUI
								.makeTableSmallButton(new ClickHandler() {
									public void onClick(ClickEvent event) {
										Button button = (Button) event
												.getSource();
										Integer k = tableButtonIndex_rt
												.get(button);
										if (k == null)
											return;
										analyzerIdString = analyzersId[k];
										elementIdString = elementsId[k];
										description = table.getText(k, 2);
										Utils.blockForPopup("popup");
										new ChoosePeriodWidget(
												false,
												true,
												analyzerIdString,
												elementIdString,
												description,
												null,
												ChoosePeriodWidget.SAMPLE_HISTORY,
												null,
												null,
												PerifericoUI.REAL_TIME_DATA_WIDGET);
									}
								});
						tableButtonIndex_rt.put(tableButton1, i);
						panelButton1.addButton(tableButton1);

						table.setWidget(i, 0, panelButton1);
						table.getCellFormatter().setAlignment(i, 0,
								HasHorizontalAlignment.ALIGN_CENTER,
								HasVerticalAlignment.ALIGN_MIDDLE);
						table.getCellFormatter().setStyleName(i, 0,
								"gwt-table-data");

						// create button for means
						PanelButtonWidget panelButton2 = new PanelButtonWidget(
								3);

						// csv button
						Button csvButton2 = PerifericoUI
								.makeCsvSmallButton(new ClickHandler() {
									public void onClick(ClickEvent event) {
										// TODO mettere la possibilita' di fare
										// 31 giorni al massimo
										Button button = (Button) event
												.getSource();
										Integer k = csvButtonIndex_means
												.get(button);
										if (k == null)
											return;
										analyzerIdString = analyzersId[k];
										elementIdString = elementsId[k];
										description = table.getText(k, 2);

										PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
												.create(PerifericoUIService.class);
										ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
										endpoint2.setServiceEntryPoint(GWT
												.getModuleBaseURL()
												+ "uiservice");
										AsyncCallback<String[]> callback2 = new UIAsyncCallback<String[]>() {
											public void onSuccess(
													String[] avgPeriods) {
												if (avgPeriods.length == 1
														&& PerifericoUI.SESSION_ENDED
																.equals(avgPeriods[0])) {
													PerifericoUI.sessionEnded();
													return;
												}
												Utils.blockForPopup("popup");
												new ChoosePeriodWidget(
														false,
														true,
														analyzerIdString,
														elementIdString,
														description,
														avgPeriods,
														ChoosePeriodWidget.MEANS_DOWNLOAD_CSV,
														null,
														null,
														PerifericoUI.REAL_TIME_DATA_WIDGET);
											}
										};
										perifService2.getAvgPeriods(
												analyzerIdString,
												elementIdString, callback2);

									}
								});
						csvButtonIndex_means.put(csvButton2, i);
						panelButton2.addButton(csvButton2);

						// chart button
						Button chartButton2 = PerifericoUI
								.makeChartSmallButton(new ClickHandler() {
									// TODO mettere la possibilita' di fare 31
									// giorni al
									// massimo
									public void onClick(ClickEvent event) {
										Button button = (Button) event
												.getSource();
										Integer k = chartButtonIndex_means
												.get(button);
										if (k == null)
											return;
										if ("WIND".equals(analyzersType[k])) {
											Window.alert(PerifericoUI.getMessages()
													.msg_chart_not_implemented());
											return;
										}
										analyzerIdString = analyzersId[k];
										elementIdString = elementsId[k];
										measureUnitString = measureUnit[k];
										description = table.getText(k, 2);

										PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
												.create(PerifericoUIService.class);
										ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
										endpoint2.setServiceEntryPoint(GWT
												.getModuleBaseURL()
												+ "uiservice");
										AsyncCallback<String[]> callback2 = new UIAsyncCallback<String[]>() {
											public void onSuccess(
													String[] avgPeriods) {
												if (avgPeriods.length == 1
														&& PerifericoUI.SESSION_ENDED
																.equals(avgPeriods[0])) {
													PerifericoUI.sessionEnded();
													return;
												}
												Utils.blockForPopup("popup");
												new ChoosePeriodWidget(
														false,
														true,
														analyzerIdString,
														elementIdString,
														description,
														avgPeriods,
														ChoosePeriodWidget.MEANS_DOWNLOAD_CHART,
														null,
														measureUnitString,
														PerifericoUI.REAL_TIME_DATA_WIDGET);
											}
										};
										perifService2.getAvgPeriods(
												analyzerIdString,
												elementIdString, callback2);
									}
								});
						chartButtonIndex_means.put(chartButton2, i);
						panelButton2.addButton(chartButton2);

						// table button
						Button tableButton2 = PerifericoUI
								.makeTableSmallButton(new ClickHandler() {
									// TODO mettere se orario: 1 anno
									// se no 1 mese
									public void onClick(ClickEvent event) {
										Button button = (Button) event
												.getSource();
										Integer k = tableButtonIndex_means
												.get(button);
										if (k == null)
											return;

										analyzerIdString = analyzersId[k];
										elementIdString = elementsId[k];
										description = table.getText(k, 2);
										final String analyzerType = analyzersType[k];

										PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
												.create(PerifericoUIService.class);
										ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
										endpoint2.setServiceEntryPoint(GWT
												.getModuleBaseURL()
												+ "uiservice");
										AsyncCallback<String[]> callback2 = new UIAsyncCallback<String[]>() {
											public void onSuccess(
													String[] avgPeriods) {
												if (avgPeriods.length == 1
														&& PerifericoUI.SESSION_ENDED
																.equals(avgPeriods[0])) {
													PerifericoUI.sessionEnded();
													return;
												}
												Utils.blockForPopup("popup");
												new ChoosePeriodWidget(
														false,
														true,
														analyzerIdString,
														elementIdString,
														description,
														avgPeriods,
														"WIND".equals(analyzerType) ? ChoosePeriodWidget.WIND_HISTORY
																: ChoosePeriodWidget.MEANS_HISTORY,
														null,
														null,
														PerifericoUI.REAL_TIME_DATA_WIDGET);
											}
										};
										perifService2.getAvgPeriods(
												analyzerIdString,
												elementIdString, callback2);
									}
								});
						tableButtonIndex_means.put(tableButton2, i);
						panelButton2.addButton(tableButton2);

						table.setWidget(i, 1, panelButton2);
						table.getCellFormatter().setAlignment(i, 1,
								HasHorizontalAlignment.ALIGN_CENTER,
								HasVerticalAlignment.ALIGN_MIDDLE);
						table.getCellFormatter().setStyleName(i, 1,
								"gwt-table-data");
						for (int j = 2; j < 9; j++) {
							if (j == 6) {
								// flag valid/not valid
								String strValid = "";
								if (fieldsMatrix[i][j] != null) {
									if ("true".equals(fieldsMatrix[i][j]))
										strValid = PerifericoUI.getMessages()
												.not_valid();
									else if ("false".equals(fieldsMatrix[i][j]))
										strValid = PerifericoUI.getMessages()
												.valid();
									else {
										strValid = fieldsMatrix[i][j];
										if (strValid == null
												|| strValid.isEmpty())
											strValid = " ";
									}
								}
								table.setText(i, j, strValid);
							} else if (j == 7) {
								// multiple flag column
								if (fieldsMatrix[i].length == 11) {
									ClickWidget clickWidget = new ClickWidget(
											(fieldsMatrix[i][9] != null ? fieldsMatrix[i][9]
													: ""), fieldsMatrix[i][j]);
									table.setWidget(i, j, clickWidget);
								} else
									table.setText(i, j, fieldsMatrix[i][j]);
							} else {
								String value = fieldsMatrix[i][j];
								if (value == null || value.isEmpty())
									value = " ";
								table.setText(i, j, value);
							}

							table.getCellFormatter().setStyleName(i, j,
									"gwt-table-data");
						}// end for
					}// end for fieldsMatrix.lenght
				}

			}
		};

		perifService.getRealTimeDataFields(viewDeletedAnalyzers, callback);
	}

	@Override
	protected void loadContent() {
		readFields();
		viewDeletedAnalyzers = false;
	}

}
