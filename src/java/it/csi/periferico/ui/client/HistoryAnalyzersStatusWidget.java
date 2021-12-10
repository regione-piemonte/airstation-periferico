/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: page that shows the status of the analyzers
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: HistoryAnalyzersStatusWidget.java,v 1.24 2013/11/08 16:17:00 pfvallosio Exp $
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
 * Widget that displays history analyzer status informations.
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class HistoryAnalyzersStatusWidget extends UIPage {

	private Label title = new Label();

	private final FlexTable table;

	private FlexTable headerTable;

	private String analyzerIdString;

	private String description;

	private String startDateStr;

	private String startHourStr;

	private String endDateStr;

	private String endHourStr;

	private String evtTypeStr;

	private String[] evtTypeArray;

	private Label lblNoData;

	public HistoryAnalyzersStatusWidget() {

		PanelButtonWidget panelButtonBack = new PanelButtonWidget();

		panelButtonBack.addButton(PerifericoUI.makeUpButtonBlue());
		panelButtonBack.addButton(PerifericoUI
				.makeViewHelpButton("historyAnalyzersStatusWidget"));

		VerticalPanel externalPanel = PerifericoUI
				.getTitledExternalPanel(
						PerifericoUI.getMessages().analyzers_status_title(),
						panelButtonBack);
		// Label and panel for sub device info
		title.setStyleName("gwt-Label-title-blue");

		// panel that contains sub device info and buttons
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		// Prepare info table's title
		headerTable = new FlexTable();
		headerTable.setText(0, 0,
				PerifericoUI.getMessages().analyzer_status_alarm_timestamp());
		headerTable.setText(0, 1,
				PerifericoUI.getMessages().analyzers_status_fault());

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
				// TODO: implementare grafico
			}
		}));

		panelButton.addButton(PerifericoUI.makeCsvButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// TODO: implementare csv
			}
		}));

		// panel that contains button for chart and csv
		HorizontalPanel hPanel2 = new HorizontalPanel();
		hPanel2.setStyleName("gwt-button-panel");
		hPanel2.add(panelButton);
		hPanel2.setSpacing(10);
		hPanel2.setCellHorizontalAlignment(panelButton,
				HasHorizontalAlignment.ALIGN_CENTER);

		// Prepare table for history analyzer status in a ScrollPanel
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

		lblNoData = new Label();
		lblNoData.setText(PerifericoUI.getMessages().no_data());
		lblNoData.setVisible(false);
		externalPanel.add(lblNoData);

		initWidget(externalPanel);
	}

	void setFields(String analyzerIdStr, String title, String startDateStr,
			String startHourStr, String endDateStr, String endHourStr,
			String evtType, String[] evtArray) {
		analyzerIdString = analyzerIdStr;
		description = title;
		this.startDateStr = startDateStr;
		this.startHourStr = startHourStr;
		this.endDateStr = endDateStr;
		this.endHourStr = endHourStr;
		evtTypeStr = evtType;
		evtTypeArray = evtArray;

	}

	private void readFields() {
		this.title.setText(description);
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] fieldsMatrix) {
				Utils.unlockForPopup("loading");
				if (fieldsMatrix.length == 1 && fieldsMatrix[0].length == 1) {
					if (PerifericoUI.SESSION_ENDED.equals(fieldsMatrix[0][0]))
						PerifericoUI.sessionEnded();
					else {
						PerifericoUI.goToUpperLevelPage();
						Utils.blockForPopup("popup");
						// create and show popup of ChoosePeriodWidget
						new ChoosePeriodWidget(false, true, analyzerIdString,
								null, description, null,
								ChoosePeriodWidget.ANALYZER_HISTORY, null,
								null,
								PerifericoUI.HISTORY_ANALYZER_STATUS_WIDGET);
						Window.alert(fieldsMatrix[0][0]);
					}
				} else {
					// set header table evt description
					if (evtTypeStr.equals(evtTypeArray[0]))
						headerTable.setText(0, 1,
								PerifericoUI.getMessages().analyzers_status_fault());
					else if (evtTypeStr.equals(evtTypeArray[1]))
						headerTable.setText(0, 1, PerifericoUI.getMessages()
								.analyzers_status_data_valid());
					else if (evtTypeStr.equals(evtTypeArray[2]))
						headerTable.setText(0, 1, PerifericoUI.getMessages()
								.analyzers_status_maintenance());
					else if (evtTypeStr.equals(evtTypeArray[3]))
						headerTable.setText(0, 1, PerifericoUI.getMessages()
								.analyzers_status_calibration_manual());
					else if (evtTypeStr.equals(evtTypeArray[4]))
						headerTable.setText(0, 1, PerifericoUI.getMessages()
								.analyzers_status_autocalibration());
					else if (evtTypeStr.equals(evtTypeArray[5]))
						headerTable.setText(0, 1, PerifericoUI.getMessages()
								.analyzers_status_autocalibration_failure());

					if (fieldsMatrix.length == 0)
						lblNoData.setVisible(true);
					else
						lblNoData.setVisible(false);
					// write content into table
					for (int i = 0; i < fieldsMatrix.length; i++) {
						for (int j = 0; j < 2; j++) {
							// set fault status icons
							if (j == 1) {
								// set fault status icons
								if (evtTypeStr.equals(evtTypeArray[0])) {
									if (fieldsMatrix[i][j] == null) {
										// gray icon
										IconImageBundle iconImageBundle = (IconImageBundle) GWT
												.create(IconImageBundle.class);
										Image ledGray = new Image();
										ledGray.setResource(iconImageBundle
												.ledGray());
										table.setWidget(i, j, ledGray);
										table.getWidget(i, j).setTitle(
												PerifericoUI.getMessages()
														.not_enabled());
									} else {
										// case not gray
										String flag = "";
										String value = "";
										int indexOf = fieldsMatrix[i][j]
												.indexOf(" - ");
										if (indexOf > 0) {
											flag = fieldsMatrix[i][j]
													.substring(
															0,
															fieldsMatrix[i][j]
																	.indexOf(" - "));
											value = " - "
													+ fieldsMatrix[i][j]
															.substring(
																	fieldsMatrix[i][j]
																			.indexOf(" - ") + 3,
																	fieldsMatrix[i][j]
																			.length());
										} else
											flag = fieldsMatrix[i][j];
										if ("true".equals(flag)) {
											// red icon
											IconImageBundle iconImageBundle = (IconImageBundle) GWT
													.create(IconImageBundle.class);
											Image ledRed = new Image();
											ledRed.setResource(iconImageBundle
													.ledRed());
											table.setWidget(i, j, ledRed);
											table.getWidget(i, j).setTitle(
													PerifericoUI.getMessages()
															.alarm() + value);
										} else if ("false".equals(flag)) {
											// green icon
											IconImageBundle iconImageBundle = (IconImageBundle) GWT
													.create(IconImageBundle.class);
											Image ledGreen = new Image();
											ledGreen.setResource(iconImageBundle
													.ledGreen());
											table.setWidget(i, j, ledGreen);
											table.getWidget(i, j).setTitle(
													PerifericoUI.getMessages().ok()
															+ value);
										}
									}
								}
								// set dataValid status icons
								else if (evtTypeStr.equals(evtTypeArray[1])) {
									if (fieldsMatrix[i][j] == null) {
										// gray icon
										IconImageBundle iconImageBundle = (IconImageBundle) GWT
												.create(IconImageBundle.class);
										Image ledGray = new Image();
										ledGray.setResource(iconImageBundle
												.ledGray());
										table.setWidget(i, j, ledGray);
										table.getWidget(i, j).setTitle(
												PerifericoUI.getMessages()
														.not_enabled());
									} else {
										// case not gray
										String flag = "";
										flag = fieldsMatrix[i][j];
										if ("true".equals(flag)) {
											// green icon
											IconImageBundle iconImageBundle = (IconImageBundle) GWT
													.create(IconImageBundle.class);
											Image ledGreen = new Image();
											ledGreen.setResource(iconImageBundle
													.ledGreen());
											table.setWidget(i, j, ledGreen);
											table.getWidget(i, j).setTitle(
													PerifericoUI.getMessages()
															.valid());
										} else if ("false".equals(flag)) {
											// red icon
											IconImageBundle iconImageBundle = (IconImageBundle) GWT
													.create(IconImageBundle.class);
											Image ledRed = new Image();
											ledRed.setResource(iconImageBundle
													.ledRed());
											table.setWidget(i, j, ledRed);
											table.getWidget(i, j).setTitle(
													PerifericoUI.getMessages()
															.not_valid());
										}
									}
								}
								// set status icons for maintenance and manual
								// calibration
								else if (evtTypeStr.equals(evtTypeArray[2])
										|| evtTypeStr.equals(evtTypeArray[3])) {
									if ("true".equals(fieldsMatrix[i][j])) {
										// yellow icon
										IconImageBundle iconImageBundle = (IconImageBundle) GWT
												.create(IconImageBundle.class);
										Image ledYellow = new Image();
										ledYellow.setResource(iconImageBundle
												.ledYellow());
										table.setWidget(i, j, ledYellow);
										table.getWidget(i, j).setTitle(
												PerifericoUI.getMessages().active());
									} else if ("false"
											.equals(fieldsMatrix[i][j])) {
										// gray icon
										IconImageBundle iconImageBundle = (IconImageBundle) GWT
												.create(IconImageBundle.class);
										Image ledGray = new Image();
										ledGray.setResource(iconImageBundle
												.ledGray());
										table.setWidget(i, j, ledGray);
										table.getWidget(i, j).setTitle(
												PerifericoUI.getMessages()
														.not_active());
									}
								}
								// set status icons for running calibration
								else if (evtTypeStr.equals(evtTypeArray[4])) {
									if ("true".equals(fieldsMatrix[i][j])) {
										// yellow icon
										IconImageBundle iconImageBundle = (IconImageBundle) GWT
												.create(IconImageBundle.class);
										Image ledYellow = new Image();
										ledYellow.setResource(iconImageBundle
												.ledYellow());
										table.setWidget(i, j, ledYellow);
										table.getWidget(i, j).setTitle(
												PerifericoUI.getMessages().active());
									} else if ("false"
											.equals(fieldsMatrix[i][j])) {
										// gray icon
										IconImageBundle iconImageBundle = (IconImageBundle) GWT
												.create(IconImageBundle.class);
										Image ledGray = new Image();
										ledGray.setResource(iconImageBundle
												.ledGray());
										table.setWidget(i, j, ledGray);
										table.getWidget(i, j).setTitle(
												PerifericoUI.getMessages()
														.not_active());
									}
								}
								// set status icons for failed auto calibration
								else if (evtTypeStr.equals(evtTypeArray[5])) {
									if ("true".equals(fieldsMatrix[i][j])) {
										// red icon
										IconImageBundle iconImageBundle = (IconImageBundle) GWT
												.create(IconImageBundle.class);
										Image ledRed = new Image();
										ledRed.setResource(iconImageBundle
												.ledRed());
										table.setWidget(i, j, ledRed);
										table.getWidget(i, j).setTitle(
												PerifericoUI.getMessages().alarm());
									} else if ("false"
											.equals(fieldsMatrix[i][j])) {
										// gray icon
										IconImageBundle iconImageBundle = (IconImageBundle) GWT
												.create(IconImageBundle.class);
										Image ledGray = new Image();
										ledGray.setResource(iconImageBundle
												.ledGray());
										table.setWidget(i, j, ledGray);
										table.getWidget(i, j).setTitle(
												PerifericoUI.getMessages().ok());
									}
								}

								table.getCellFormatter().setAlignment(i, j,
										HasHorizontalAlignment.ALIGN_CENTER,
										HasVerticalAlignment.ALIGN_MIDDLE);
							}
							// character fields
							else
								table.setText(i, j, fieldsMatrix[i][j]);
							table.getCellFormatter().setStyleName(i, j,
									"gwt-table-data");
						}
					}
				}
			}// end onSuccess

			@Override
			public void onFailure(Throwable caught) {
				Utils.unlockForPopup("loading");
				super.onFailure(caught);
			}
		};

		Utils.blockForPopup("loading");
		perifService.getHistoryAnalyzersStatusFields(analyzerIdString,
				startDateStr, startHourStr, endDateStr, endHourStr, evtTypeStr,
				callback);
	}

	@Override
	protected void reset() {
		Utils.clearTable(table);
		table.getCellFormatter().setWidth(0, 0, "245px");
		table.getCellFormatter().setWidth(0, 1, "245px");
	}

	@Override
	protected void loadContent() {
		readFields();
	}

}
