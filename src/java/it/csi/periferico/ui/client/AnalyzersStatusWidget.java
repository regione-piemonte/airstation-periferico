/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: page that shows the status of the analyzers
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: AnalyzersStatusWidget.java,v 1.34 2015/05/28 09:30:21 pfvallosio Exp $
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Page that shows the status of the analyzers
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class AnalyzersStatusWidget extends UIPage {

	private Label title = new Label();

	private FlexTable table;

	private String[] analyzersId;

	public AnalyzersStatusWidget() {
		PanelButtonWidget panelButtonRefresh = new PanelButtonWidget();
		panelButtonRefresh.addButton(PerifericoUI.makeRefreshButton());
		panelButtonRefresh.addButton(PerifericoUI
				.makeViewHelpButton("analyzerStatusWidget"));
		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().analyzers_status_title(),
				panelButtonRefresh);

		// Label and panel for subdevice info
		title.setStyleName("gwt-Label-title-blue");
		title.setText(PerifericoUI.getMessages().analyzers_status_fault_title());

		// panel that contains subdevice info and buttons
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		// Prepare info table's title
		FlexTable headerTable = new FlexTable();
		headerTable.setText(0, 0,
				PerifericoUI.getMessages().analyzer_status_link_history());
		headerTable.setText(0, 1, PerifericoUI.getMessages().dgt_settings());
		headerTable.setText(0, 2, PerifericoUI.getMessages().analyzers_status_id());
		headerTable.setText(0, 3,
				PerifericoUI.getMessages().analyzers_status_brand_model());
		headerTable.setText(0, 4,
				PerifericoUI.getMessages().analyzers_status_fault());
		headerTable.setText(0, 5,
				PerifericoUI.getMessages().analyzers_status_data_valid());
		headerTable.setText(0, 6,
				PerifericoUI.getMessages().analyzers_status_maintenance());
		headerTable.setText(0, 7,
				PerifericoUI.getMessages().analyzers_status_calibration_manual());
		headerTable.setText(0, 8,
				PerifericoUI.getMessages().analyzers_status_autocalibration());
		// TODO: predisposizione per futura implementazione
		// headerTable.setText(0, 8, PerifericoUI.getMessages()
		// .analyzers_status_autocalibration_failure());

		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "75px");
		headerTable.getCellFormatter().setWidth(0, 1, "75px");
		headerTable.getCellFormatter().setWidth(0, 2, "100px");
		headerTable.getCellFormatter().setWidth(0, 3, "155px");
		headerTable.getCellFormatter().setWidth(0, 4, "75px");
		headerTable.getCellFormatter().setWidth(0, 5, "75px");
		headerTable.getCellFormatter().setWidth(0, 6, "75px");
		headerTable.getCellFormatter().setWidth(0, 7, "75px");
		headerTable.getCellFormatter().setWidth(0, 8, "75px");
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
					return;
				}

				// fieldsmatrix[i][j]:
				// i: one row for one analyzer
				// j = 0 analyzerId
				// j = 1 type: true = DPA
				// j = 2 name
				// j = 3 brand
				// j = 4 binary status
				// j = 5 validated status
				// j = 6 maintenance in progress
				// j = 7 manual calibration
				// j = 8 auto check
				// j = 9 check failed
				// j =10 web UI URL

				// clear table
				Utils.clearTable(table);
				analyzersId = new String[fieldsMatrix.length];
				table.getCellFormatter().setWidth(0, 0, "75px");
				table.getCellFormatter().setWidth(0, 1, "75px");
				table.getCellFormatter().setWidth(0, 2, "100px");
				table.getCellFormatter().setWidth(0, 3, "155px");
				table.getCellFormatter().setWidth(0, 4, "75px");
				table.getCellFormatter().setWidth(0, 5, "75px");
				table.getCellFormatter().setWidth(0, 6, "75px");
				table.getCellFormatter().setWidth(0, 7, "75px");
				table.getCellFormatter().setWidth(0, 8, "75px");
				for (int i = 0; i < fieldsMatrix.length; i++) {
					analyzersId[i] = fieldsMatrix[i][0];
					// create button for link to history page
					Button historyButton = new Button();
					historyButton.setStyleName("gwt-button-history");
					historyButton.setTitle(PerifericoUI.getMessages()
							.analyzer_status_link_history());
					table.setWidget(i, 0, historyButton);
					table.getCellFormatter().setAlignment(i, 0,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 0,
							"gwt-table-data");
					historyButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Button button = (Button) event.getSource();
							for (int k = 0; k < table.getRowCount(); k++) {
								if (((Button) (table.getWidget(k, 0)))
										.equals(button)) {
									String analyzerIdStr = analyzersId[k];
									String description = table.getText(k, 1)
											+ " " + table.getText(k, 3);
									Utils.blockForPopup("popup");
									// create and show popup of
									// ChoosePeriodWidget
									new ChoosePeriodWidget(
											false,
											true,
											analyzerIdStr,
											null,
											description,
											null,
											ChoosePeriodWidget.ANALYZER_HISTORY,
											null, null,
											PerifericoUI.ANALYZER_STATUS_WIDGET);
								}
							}
						}
					});
					// se e' un analizzatore digitale mostro il pulsante
					if (fieldsMatrix[i][1].equals(new Boolean(true).toString())) {
						// create button for dgt analyzer
						Button dgtButton = new Button();
						dgtButton.setStyleName("gwt-button-dgt");
						dgtButton
								.setTitle(PerifericoUI.getMessages().dgt_settings());
						table.setWidget(i, 1, dgtButton);
						table.getCellFormatter().setAlignment(i, 1,
								HasHorizontalAlignment.ALIGN_CENTER,
								HasVerticalAlignment.ALIGN_MIDDLE);
						table.getCellFormatter().setStyleName(i, 1,
								"gwt-table-data");
						dgtButton.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								Button button = (Button) event.getSource();
								for (int k = 0; k < table.getRowCount(); k++) {
									if (((Button) (table.getWidget(k, 1)))
											.equals(button)) {
										String analyzerIdStr = analyzersId[k];
										String description = table
												.getText(k, 1)
												+ " "
												+ table.getText(k, 3);
										PerifericoUI.getDgtAnalyzerWidget()
												.setAnalyzerId(analyzerIdStr);
										PerifericoUI.getDgtAnalyzerWidget()
												.setAnalyzerName(description);
										PerifericoUI
												.setCurrentPage(PerifericoUI.getDgtAnalyzerWidget());

									}
								}
							}
						});
					}// end if DPA
					else {
						table.setWidget(i, 1, null);
						table.getCellFormatter().setStyleName(i, 1,
								"gwt-table-data");
					}

					for (int j = 2; j < 9; j++) {
						// set fault status icons
						if (j == 4) {
							if (fieldsMatrix[i][j] == null) {
								// gray icon
								IconImageBundle iconImageBundle = (IconImageBundle) GWT
										.create(IconImageBundle.class);
								Image ledGray = new Image();
								ledGray.setResource(iconImageBundle.ledGray());
								table.setWidget(i, j, ledGray);
								table.getWidget(i, j).setTitle(
										PerifericoUI.getMessages().not_enabled());
							} else {
								// case not null
								String flag = "";
								String value = "";
								int indexOf = fieldsMatrix[i][j].indexOf(" - ");
								if (indexOf > 0) {
									flag = fieldsMatrix[i][j].substring(0,
											fieldsMatrix[i][j].indexOf(" - "));
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
									ledRed.setResource(iconImageBundle.ledRed());
									table.setWidget(i, j, ledRed);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages().alarm()
													+ value);
								} else if ("false".equals(flag)) {
									// green icon
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image ledGreen = new Image();
									ledGreen.setResource(iconImageBundle
											.ledGreen());
									table.setWidget(i, j, ledGreen);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages().ok() + value);
								}

							}// end else
							table.getCellFormatter().setAlignment(i, j,
									HasHorizontalAlignment.ALIGN_CENTER,
									HasVerticalAlignment.ALIGN_MIDDLE);
						}
						// set dataValid status icons
						else if (j == 5) {
							if (fieldsMatrix[i][j] == null) {
								// gray icon
								IconImageBundle iconImageBundle = (IconImageBundle) GWT
										.create(IconImageBundle.class);
								Image ledGray = new Image();
								ledGray.setResource(iconImageBundle.ledGray());
								table.setWidget(i, j, ledGray);
								table.getWidget(i, j).setTitle(
										PerifericoUI.getMessages().not_enabled());
							} else {
								// case not null
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
											PerifericoUI.getMessages().valid());
								} else if ("false".equals(flag)) {
									// red icon
									IconImageBundle iconImageBundle = (IconImageBundle) GWT
											.create(IconImageBundle.class);
									Image ledRed = new Image();
									ledRed.setResource(iconImageBundle.ledRed());
									table.setWidget(i, j, ledRed);
									table.getWidget(i, j).setTitle(
											PerifericoUI.getMessages().not_valid());
								}
							}// end else
							table.getCellFormatter().setAlignment(i, j,
									HasHorizontalAlignment.ALIGN_CENTER,
									HasVerticalAlignment.ALIGN_MIDDLE);
						}
						// set status icons for maintenance and manual
						// calibration
						else if (j == 6 || j == 7 || j == 8) {
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
							} else if ("false".equals(fieldsMatrix[i][j])) {
								// gray icon
								IconImageBundle iconImageBundle = (IconImageBundle) GWT
										.create(IconImageBundle.class);
								Image ledGray = new Image();
								ledGray.setResource(iconImageBundle.ledGray());
								table.setWidget(i, j, ledGray);
								table.getWidget(i, j).setTitle(
										PerifericoUI.getMessages().not_active());
							} else {
								String value = fieldsMatrix[i][j];
								if (value == null || value.isEmpty())
									value = " ";
								table.setText(i, j, value);
							}
							table.getCellFormatter().setAlignment(i, j,
									HasHorizontalAlignment.ALIGN_CENTER,
									HasVerticalAlignment.ALIGN_MIDDLE);
						}
						// character fields
						else {

							table.setText(i, j, fieldsMatrix[i][j]);
						}
						table.getCellFormatter().setStyleName(i, j,
								"gwt-table-data");
					}// end for j
					if (fieldsMatrix[i][10] != null) {
						HTML uiLink = new HTML(
								"<a href='javascript:urlWin=window.open(\""
										+ fieldsMatrix[i][10]
										+ "\"); window[\"urlWin\"].focus()' title='"
										+ "Web UI " + fieldsMatrix[i][2] + "'>"
										+ fieldsMatrix[i][2]
										+ "</a>");
						table.setWidget(i, 2, uiLink);
					}
				}// end for i
			}// end onSuccess
		};

		perifService.getAnalyzersStatusFields(callback);
	}

	@Override
	protected void loadContent() {
		readFields();
	}

}
