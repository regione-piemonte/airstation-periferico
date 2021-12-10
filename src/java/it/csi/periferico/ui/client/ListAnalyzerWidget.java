/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: shows and manages a list of analyzers
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: ListAnalyzerWidget.java,v 1.45 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */

package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.UIPage;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Shows and manages a list of analyzers
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class ListAnalyzerWidget extends UIPage {

	private final String ENABLED = "ENABLED";

	private final String DISABLED = "DISABLED";

	private final String REMOVED = "REMOVED";

	private final String OUT_OF_ORDER = "OUT_OF_ORDER";

	private final String DELETED = "DELETED";

	private String[] analyzersId;

	private String[] analyzersType;

	private String[][] matrixIdType;

	private final ListBox analyzersListBox = new ListBox();

	private final ListBox brandListBox = new ListBox();

	private final ListBox modelListBox = new ListBox();

	// Table for analyzer information.
	private final FlexTable table;

	private Button senderButtonModify;

	private Map<String, String> analyzerStatusValues = new HashMap<String, String>();

	public ListAnalyzerWidget() {

		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] result) {
				for (int i = 0; i < result.length; i++)
					analyzerStatusValues.put(result[i][0], result[i][1]);
			}
		};
		perifService.getAnalyzerStatusList(callback);

		PanelButtonWidget panelButton1 = new PanelButtonWidget();
		panelButton1.addButton(PerifericoUI.makeSaveButton());
		panelButton1.addButton(PerifericoUI.makeReloadButton());
		panelButton1.addButton(PerifericoUI
				.makeConfHelpButton("listAnalyzerWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().list_analyzer_title(), panelButton1);

		/*
		 * New analyzer panel
		 */
		// Prepare panel for new analyzer and new dpa analyzer
		Label titleNew = new Label();
		titleNew.setText(PerifericoUI.getMessages().lbl_analyzer_new());
		titleNew.setStyleName("gwt-Label-title");

		VerticalPanel newAllAnalyzerPanel = new VerticalPanel();
		newAllAnalyzerPanel.setStyleName("gwt-post-boxed");
		HorizontalPanel newAnalyzerPanel = new HorizontalPanel();
		newAnalyzerPanel.setSpacing(10);
		Label genericAnalyzerLbl = new Label(
				PerifericoUI.getMessages().lbl_generic_analyzer());
		genericAnalyzerLbl.setWidth("210px");
		newAnalyzerPanel.add(genericAnalyzerLbl);
		HorizontalPanel newDpaAnalyzerPanel = new HorizontalPanel();
		newDpaAnalyzerPanel.setSpacing(10);

		// Setting elements list box
		analyzersListBox.setWidth("300px");
		analyzersListBox.setVisibleItemCount(1);
		brandListBox.setWidth("200px");
		brandListBox.setVisibleItemCount(1);
		modelListBox.setWidth("200px");
		modelListBox.setVisibleItemCount(1);

		// create brandListBox changeListener
		brandListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int indexSelected = brandListBox.getSelectedIndex();
				String selectedValue = brandListBox.getValue(indexSelected);
				if (selectedValue.equals("0"))
					Window.alert(PerifericoUI.getMessages()
							.brand_error_select_item());
				else {
					modelListBox.clear();
					modelListBox.addItem(PerifericoUI.getMessages().select_model(),
							new String("0"));
					PerifericoUIServiceAsync perifService4 = (PerifericoUIServiceAsync) GWT
							.create(PerifericoUIService.class);
					ServiceDefTarget endpoint4 = (ServiceDefTarget) perifService4;
					endpoint4.setServiceEntryPoint(GWT.getModuleBaseURL()
							+ "uiservice");
					AsyncCallback<String[]> callback4 = new UIAsyncCallback<String[]>() {
						public void onSuccess(String[] result) {
							if (result.length == 1
									&& PerifericoUI.SESSION_ENDED
											.equals(result[0]))
								PerifericoUI.sessionEnded();
							else {
								for (int q = 0; q < result.length; q++) {
									modelListBox.addItem(result[q]);
								}
							}
						}
					};
					perifService4.getModel(selectedValue, callback4);
				}
			}
		});

		Button chooseAnalyzerButton = new Button();
		chooseAnalyzerButton.setStyleName("gwt-button-new-orange");
		chooseAnalyzerButton.setTitle(PerifericoUI.getMessages()
				.analyzer_button_new());
		chooseAnalyzerButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				int indexSelected = analyzersListBox.getSelectedIndex();
				String selectedValue = analyzersListBox.getValue(indexSelected);
				if (selectedValue.equals("0"))
					Window.alert(PerifericoUI.getMessages()
							.analyzer_error_select_item());
				else {

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
								int indexSelected = analyzersListBox
										.getSelectedIndex();
								String selectedValue = analyzersListBox
										.getValue(indexSelected);
								PerifericoUI.getAnalyzerWidget()
										.setNewAnalyzer(selectedValue);
								PerifericoUI
										.setCurrentPage(PerifericoUI.getAnalyzerWidget());

							}// end else
						}// end onSuccess
					};
					perifServiceSession.verifySessionEnded(callbackSession);
				}
			}
		});
		newAnalyzerPanel.add(analyzersListBox);
		newAnalyzerPanel.add(chooseAnalyzerButton);

		// setting the alignment
		newAnalyzerPanel.setCellHorizontalAlignment(chooseAnalyzerButton,
				HasHorizontalAlignment.ALIGN_LEFT);
		newAnalyzerPanel.setCellHorizontalAlignment(analyzersListBox,
				HasHorizontalAlignment.ALIGN_LEFT);
		newAnalyzerPanel.setCellHorizontalAlignment(genericAnalyzerLbl,
				HasHorizontalAlignment.ALIGN_LEFT);
		newAnalyzerPanel.setCellVerticalAlignment(chooseAnalyzerButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newAnalyzerPanel.setCellVerticalAlignment(genericAnalyzerLbl,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newAnalyzerPanel.setCellVerticalAlignment(analyzersListBox,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newAnalyzerPanel.setCellWidth(analyzersListBox, "310px");
		newAllAnalyzerPanel.add(newAnalyzerPanel);

		// setting button for new dpa analyzer
		Button chooseDpaAnalyzerButton = new Button();
		chooseDpaAnalyzerButton.setStyleName("gwt-button-new-orange");
		chooseDpaAnalyzerButton.setTitle(PerifericoUI.getMessages()
				.dpa_analyzer_button_new());
		chooseDpaAnalyzerButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int indexSelected = modelListBox.getSelectedIndex();
				String selectedValue = modelListBox.getValue(indexSelected);
				if (selectedValue.equals("0"))
					Window.alert(PerifericoUI.getMessages()
							.model_error_select_item());
				else {
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
								PerifericoUI.getAnalyzerWidget().setNewDpaAnalyzer(
										brandListBox.getValue(brandListBox
												.getSelectedIndex()),
										modelListBox.getValue(modelListBox
												.getSelectedIndex()));
								PerifericoUI
										.setCurrentPage(PerifericoUI.getAnalyzerWidget());
							}
						}// end onSuccess
					};
					perifServiceSession.verifySessionEnded(callbackSession);
				}
			}
		});
		Label dpaAnalyzerLabel = new Label(
				PerifericoUI.getMessages().lbl_dpa_analyzer());
		newDpaAnalyzerPanel.add(dpaAnalyzerLabel);
		newDpaAnalyzerPanel.add(brandListBox);
		newDpaAnalyzerPanel.add(modelListBox);
		newDpaAnalyzerPanel.add(chooseDpaAnalyzerButton);

		// setting the alignment
		newDpaAnalyzerPanel.setCellHorizontalAlignment(chooseDpaAnalyzerButton,
				HasHorizontalAlignment.ALIGN_LEFT);
		newDpaAnalyzerPanel.setCellHorizontalAlignment(dpaAnalyzerLabel,
				HasHorizontalAlignment.ALIGN_LEFT);
		newDpaAnalyzerPanel.setCellVerticalAlignment(chooseDpaAnalyzerButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newDpaAnalyzerPanel.setCellVerticalAlignment(dpaAnalyzerLabel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newDpaAnalyzerPanel.setCellWidth(dpaAnalyzerLabel, "210px");
		newDpaAnalyzerPanel.setCellHorizontalAlignment(brandListBox,
				HasHorizontalAlignment.ALIGN_LEFT);
		newDpaAnalyzerPanel.setCellHorizontalAlignment(modelListBox,
				HasHorizontalAlignment.ALIGN_LEFT);
		newAllAnalyzerPanel.add(newDpaAnalyzerPanel);

		externalPanel.add(titleNew);
		externalPanel.add(newAllAnalyzerPanel);

		/*
		 * Analyzer table
		 */
		// Prepare panel for analyzer list
		Label title = new Label();
		title.setText(PerifericoUI.getMessages().analyzer_list_title());
		title.setStyleName("gwt-Label-title");

		// panel that contains the grid analyzer list
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed");

		// Prepare table of titles
		FlexTable headerTable = new FlexTable();
		headerTable.setText(0, 0, PerifericoUI.getMessages().lbl_status());
		headerTable.setText(0, 1, PerifericoUI.getMessages().analyzer_lbl_name());
		headerTable.setText(0, 2, PerifericoUI.getMessages().lbl_brand());
		headerTable.setText(0, 3, PerifericoUI.getMessages().lbl_model());
		headerTable.setText(0, 4, PerifericoUI.getMessages().lbl_modify());
		headerTable.setText(0, 5, PerifericoUI.getMessages().lbl_delete());
		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "80px");
		headerTable.getCellFormatter().setWidth(0, 1, "200px");
		headerTable.getCellFormatter().setWidth(0, 2, "250px");
		headerTable.getCellFormatter().setWidth(0, 3, "250px");
		headerTable.getCellFormatter().setWidth(0, 4, "100px");
		headerTable.getCellFormatter().setWidth(0, 5, "115px");
		for (int j = 0; j < 6; j++) {
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}
		panel.add(headerTable);

		// Prepare table for analyzers info in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("255px");
		panel.add(scrollPanel);

		externalPanel.add(title);
		externalPanel.add(panel);

		initWidget(externalPanel);

	}// end constructor

	private void getAnalyzersCfg() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] analyzers) {
				if (analyzers.length == 1 && analyzers[0].length == 1
						&& PerifericoUI.SESSION_ENDED.equals(analyzers[0][0])) {
					PerifericoUI.sessionEnded();
					return;
				}
				// String[][] analyzers:
				// - one row for each analyzer
				// - columns:
				// 0 = id
				// 1 = type
				// 2 = status
				// 3 = name
				// 4 = brand
				// 5 = model
				int numExistentAnalyzers = 0;
				for (int i = 0; i < analyzers.length; i++)
					if (!analyzers[i][2].equals(DELETED))
						numExistentAnalyzers++;
				analyzersId = new String[numExistentAnalyzers];
				analyzersType = new String[numExistentAnalyzers];
				matrixIdType = new String[numExistentAnalyzers][2];
				// clear table
				Utils.clearTable(table);
				table.getCellFormatter().setWidth(0, 0, "80px");
				table.getCellFormatter().setWidth(0, 1, "200px");
				table.getCellFormatter().setWidth(0, 2, "250px");
				table.getCellFormatter().setWidth(0, 3, "250px");
				table.getCellFormatter().setWidth(0, 4, "100px");
				table.getCellFormatter().setWidth(0, 5, "100px");
				int rowIndex = 0;
				for (int i = 0; i < analyzers.length; i++) {
					if (analyzers[i][2].equals(DELETED))
						continue;
					IconImageBundle iconImageBundle = (IconImageBundle) GWT
							.create(IconImageBundle.class);
					String status = analyzers[i][2];
					if (status.equals(ENABLED)) {
						Image enabled = new Image();
						enabled.setResource(iconImageBundle.enabled());
						table.setWidget(rowIndex, 0, enabled);
					} else if (status.equals(DISABLED)) {
						Image disabled = new Image();
						disabled.setResource(iconImageBundle.disabled());
						table.setWidget(rowIndex, 0, disabled);
					} else if (status.equals(REMOVED)) {
						Image removed = new Image();
						removed.setResource(iconImageBundle.removed());
						table.setWidget(rowIndex, 0, removed);
					} else if (status.equals(OUT_OF_ORDER)) {
						Image out_of_order = new Image();
						out_of_order
								.setResource(iconImageBundle.out_of_order());
						table.setWidget(rowIndex, 0, out_of_order);
					} else {
						table.setWidget(rowIndex, 0, new Label(status));
					}
					String statusName = analyzerStatusValues.get(status);
					if (statusName != null)
						table.getWidget(rowIndex, 0).setTitle(statusName);
					for (int j = 2; j < 6; j++) {
						if (j > 2)
							table.setText(rowIndex, j - 2, analyzers[i][j]);
						table.getCellFormatter().setAlignment(rowIndex, j - 2,
								HasHorizontalAlignment.ALIGN_CENTER,
								HasVerticalAlignment.ALIGN_MIDDLE);
						table.getCellFormatter().setStyleName(rowIndex, j - 2,
								"gwt-table-data");
					}
					analyzersId[rowIndex] = analyzers[i][0];
					analyzersType[rowIndex] = analyzers[i][1];

					matrixIdType[rowIndex][0] = analyzers[i][0];
					matrixIdType[rowIndex][1] = analyzers[i][1];
					Button modifyButton = new Button();
					modifyButton.setStyleName("gwt-button-modify");
					modifyButton.setTitle(PerifericoUI.getMessages()
							.analyzer_button_modify());
					table.setWidget(rowIndex, 4, modifyButton);
					table.getCellFormatter().setAlignment(rowIndex, 4,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(rowIndex, 4,
							"gwt-table-data");
					modifyButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							senderButtonModify = (Button) event.getSource();
							// check if session is ended
							PerifericoUIServiceAsync perifServiceSession = (PerifericoUIServiceAsync) GWT
									.create(PerifericoUIService.class);
							ServiceDefTarget endpointSession = (ServiceDefTarget) perifServiceSession;
							endpointSession.setServiceEntryPoint(GWT
									.getModuleBaseURL() + "uiservice");
							AsyncCallback<Boolean> callbackSession = new UIAsyncCallback<Boolean>() {
								public void onSuccess(Boolean sessionEnded) {
									if (sessionEnded.booleanValue()) {
										PerifericoUI.sessionEnded();
										return;
									}
									for (int k = 0; k < table.getRowCount(); k++) {
										if (((Button) (table.getWidget(k, 4)))
												.equals(senderButtonModify)) {
											PerifericoUI.getAnalyzerWidget()
													.setAnalyzers(matrixIdType,
															k);
											PerifericoUI
													.setCurrentPage(PerifericoUI.getAnalyzerWidget());
											break;
										}
									}
								}
							};
							perifServiceSession
									.verifySessionEnded(callbackSession);

						}
					});
					Button deleteButton = new Button();
					deleteButton.setStyleName("gwt-button-delete");
					deleteButton.setTitle(PerifericoUI.getMessages()
							.analyzer_button_delete());
					table.setWidget(rowIndex, 5, deleteButton);
					table.getCellFormatter().setAlignment(rowIndex, 5,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(rowIndex, 5,
							"gwt-table-data");
					deleteButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							boolean confirmResult = Window
									.confirm(PerifericoUI.getMessages()
											.lbl_analyzer_confirm_delete());
							if (confirmResult) {
								Button button = (Button) event.getSource();
								for (int k = 0; k < table.getRowCount(); k++) {
									if (((Button) (table.getWidget(k, 5)))
											.equals(button)) {
										PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
												.create(PerifericoUIService.class);
										ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
										endpoint.setServiceEntryPoint(GWT
												.getModuleBaseURL()
												+ "uiservice");
										AsyncCallback<String> callback = new UIAsyncCallback<String>() {
											public void onSuccess(
													String resultString) {
												if (resultString
														.equals(PerifericoUI.SESSION_ENDED)) {
													PerifericoUI.sessionEnded();
													return;
												}
												// TODO: capire cosa fare se
												// c'e' errore
												Boolean resultValue = new Boolean(
														resultString);
												if (resultValue.booleanValue()) {
													getAnalyzersCfg();
												}
											}
										};
										perifService.deleteAnalyzer(
												analyzersId[k], callback);
									}// end if
								}// end for
							}// end confirmresult
						}// end onclick
					});
					rowIndex++;
				}// end for analyzer
					// set analyzersListBox and dpaAnalyzersListBox calling
					// perifService
				analyzersListBox.clear();
				analyzersListBox.addItem(
						PerifericoUI.getMessages().select_analyzer_name(),
						new String("0"));
				PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
				endpoint2.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[][]> callback2 = new UIAsyncCallback<String[][]>() {
					public void onSuccess(String[][] analizersTypeMatrix) {
						if (analizersTypeMatrix.length == 1
								&& analizersTypeMatrix[0].length == 1
								&& PerifericoUI.SESSION_ENDED
										.equals(analizersTypeMatrix[0][0])) {
							PerifericoUI.sessionEnded();
							return;
						}
						for (int z = 0; z < analizersTypeMatrix.length; z++) {
							if (!analizersTypeMatrix[z][1].equals(Utils.DPA))
								analyzersListBox.addItem(
										analizersTypeMatrix[z][0],
										analizersTypeMatrix[z][1]);
						}
						// set brandListBox calling perifService
						brandListBox.clear();
						brandListBox.addItem(
								PerifericoUI.getMessages().select_brand(),
								new String("0"));
						modelListBox.clear();
						modelListBox.addItem(
								PerifericoUI.getMessages().select_model(),
								new String("0"));
						PerifericoUIServiceAsync perifService3 = (PerifericoUIServiceAsync) GWT
								.create(PerifericoUIService.class);
						ServiceDefTarget endpoint3 = (ServiceDefTarget) perifService3;
						endpoint3.setServiceEntryPoint(GWT.getModuleBaseURL()
								+ "uiservice");
						AsyncCallback<String[]> callback3 = new UIAsyncCallback<String[]>() {
							public void onSuccess(String[] brands) {
								if (brands.length == 1
										&& PerifericoUI.SESSION_ENDED
												.equals(brands[0]))
									PerifericoUI.sessionEnded();
								else {
									for (int q = 0; q < brands.length; q++) {
										brandListBox.addItem(brands[q]);
									}
								}
							}
						};
						perifService3.getBrand(callback3);
					}
				};
				perifService2.getAnalyzersTypeCfg(callback2);
			}
		};
		perifService.getAnalyzersCfg(callback);
	}// end getAnalyzersCfg

	@Override
	protected void loadContent() {
		getAnalyzersCfg();
	}

	@Override
	protected void reset() {
		clearGridRowFormatter(table);
	}

}// end class
