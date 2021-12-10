/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: shows and manages a list of boards
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: ListBoardWidget.java,v 1.44 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.UIPage;

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
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Shows and manages a list of boards
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class ListBoardWidget extends UIPage implements ChangeHandler {

	// Grids for board information.
	private final FlexTable table;

	private final FlexTable table2;

	private final ListBox brandListBox = new ListBox();

	private final ListBox modelListBox = new ListBox();

	private final Hidden boardId = new Hidden();

	private String[] boardsId;

	private String[] boardsType;

	private Label titleNewISABoard;

	private HorizontalPanel newISABoardPanel;

	private Button senderButtonModify;

	private Button senderButtonNewBoard;

	public ListBoardWidget() {

		boardId.setValue("");

		PanelButtonWidget panelButton1 = new PanelButtonWidget();
		panelButton1.addButton(PerifericoUI.makeSaveButton());
		panelButton1.addButton(PerifericoUI.makeReloadButton());
		panelButton1.addButton(PerifericoUI
				.makeConfHelpButton("listBoardWidget"));
		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().list_board_title(), panelButton1);

		/*
		 * Configured board table
		 */
		// Prepare panel for board list
		Label title = new Label();
		title.setText(PerifericoUI.getMessages().lbl_list_board_title());
		title.setStyleName("gwt-Label-title");

		// panel that contains the grid board list
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed");

		// Prepare table of titles
		FlexTable headerTable = new FlexTable();
		headerTable.setText(0, 0, PerifericoUI.getMessages().lbl_board_index());
		headerTable.setText(0, 1, PerifericoUI.getMessages().lbl_brand());
		headerTable.setText(0, 2, PerifericoUI.getMessages().lbl_model());
		headerTable.setText(0, 3, PerifericoUI.getMessages().lbl_status());
		headerTable.setText(0, 4, PerifericoUI.getMessages().lbl_modify());
		headerTable.setText(0, 5, PerifericoUI.getMessages().lbl_delete());
		headerTable.setText(0, 6, PerifericoUI.getMessages().lbl_unbind());
		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "80px");
		headerTable.getCellFormatter().setWidth(0, 1, "250px");
		headerTable.getCellFormatter().setWidth(0, 2, "250px");
		headerTable.getCellFormatter().setWidth(0, 3, "100px");
		headerTable.getCellFormatter().setWidth(0, 4, "100px");
		headerTable.getCellFormatter().setWidth(0, 5, "100px");
		headerTable.getCellFormatter().setWidth(0, 6, "100px");
		for (int j = 0; j < 7; j++) {
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}
		panel.add(headerTable);

		// Prepare table for boards info in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("140px");
		panel.add(scrollPanel);

		externalPanel.add(title);
		externalPanel.add(panel);

		/*
		 * Configured PCIDevice
		 */
		// Prepare panel for PCI device list
		title = new Label();
		title.setText(PerifericoUI.getMessages().lbl_PCIdevice_title());
		title.setStyleName("gwt-Label-title");

		// panel that contains the grid board list
		panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed");

		// Prepare table of titles
		FlexTable headerTable2 = new FlexTable();
		headerTable2.setText(0, 0, PerifericoUI.getMessages().lbl_PCIdevice_busid());
		headerTable2.setText(0, 1, PerifericoUI.getMessages().lbl_brand());
		headerTable2.setText(0, 2, PerifericoUI.getMessages().lbl_model());
		headerTable2.setText(0, 3, PerifericoUI.getMessages().lbl_PCIdevice_new());
		headerTable2.setText(0, 4,
				PerifericoUI.getMessages().lbl_PCIdevice_association());
		headerTable2.setStyleName("gwt-table-header");
		headerTable2.setWidth("100%");
		headerTable2.getCellFormatter().setWidth(0, 0, "150px");
		headerTable2.getCellFormatter().setWidth(0, 1, "240px");
		headerTable2.getCellFormatter().setWidth(0, 2, "240px");
		headerTable2.getCellFormatter().setWidth(0, 3, "100px");
		headerTable2.getCellFormatter().setWidth(0, 4, "250px");
		for (int j = 0; j < 5; j++) {
			headerTable2.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}
		panel.add(headerTable2);

		// Prepare table for detected devices info in a ScrollPanel
		ScrollPanel scrollPanel2 = new ScrollPanel();
		table2 = new FlexTable();
		table2.setStyleName("gwt-table-data");
		table2.setWidth("100%");
		scrollPanel2.add(table2);
		scrollPanel2.setHeight("90px");
		panel.add(scrollPanel2);

		externalPanel.add(title);
		externalPanel.add(panel);

		/*
		 * New ISA board panel
		 */
		// Prepare panel for new ISA board
		titleNewISABoard = new Label();
		titleNewISABoard.setText(PerifericoUI.getMessages().lbl_ISAboard_new());
		titleNewISABoard.setStyleName("gwt-Label-title");

		newISABoardPanel = new HorizontalPanel();
		newISABoardPanel.setStyleName("gwt-post-boxed");
		newISABoardPanel.setSpacing(10);

		Button newISABoardButton = new Button();
		newISABoardButton.setStyleName("gwt-button-new-orange");
		newISABoardButton.setTitle(PerifericoUI.getMessages().button_new_ISAboard());
		newISABoardButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int brandIndexSelected = brandListBox.getSelectedIndex();
				String brandSelectedValue = brandListBox
						.getValue(brandIndexSelected);
				int modelIndexSelected = modelListBox.getSelectedIndex();
				String modelSelectedValue = modelListBox
						.getValue(modelIndexSelected);
				if (brandSelectedValue.equals("0")
						|| modelSelectedValue.equals("0"))
					Window.alert(PerifericoUI.getMessages().error_select_element());
				else {
					// check if session is ended, before change page to
					// BoardWidget
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
								int brandIndexSelected = brandListBox
										.getSelectedIndex();
								String brandSelectedValue = brandListBox
										.getValue(brandIndexSelected);
								int modelIndexSelected = modelListBox
										.getSelectedIndex();
								String modelSelectedValue = modelListBox
										.getValue(modelIndexSelected);
								PerifericoUI.getBoardWidget().setBoardInfo(false,
										brandSelectedValue, modelSelectedValue,
										null, null, true);
								PerifericoUI
										.setCurrentPage(PerifericoUI.getBoardWidget());
							}
						}
					};
					perifServiceSession.verifySessionEnded(callbackSession);
				}
			}
		});

		// Setting list box
		brandListBox.setWidth("200px");
		brandListBox.setVisibleItemCount(1);
		brandListBox.addChangeHandler(this);
		modelListBox.setWidth("200px");
		modelListBox.setVisibleItemCount(1);

		newISABoardPanel.add(brandListBox);
		newISABoardPanel.add(modelListBox);
		newISABoardPanel.add(newISABoardButton);

		// setting the alignement
		newISABoardPanel.setCellHorizontalAlignment(newISABoardButton,
				HasHorizontalAlignment.ALIGN_LEFT);
		newISABoardPanel.setCellHorizontalAlignment(brandListBox,
				HasHorizontalAlignment.ALIGN_LEFT);
		newISABoardPanel.setCellHorizontalAlignment(modelListBox,
				HasHorizontalAlignment.ALIGN_LEFT);
		newISABoardPanel.setCellVerticalAlignment(newISABoardButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newISABoardPanel.setCellVerticalAlignment(brandListBox,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newISABoardPanel.setCellVerticalAlignment(modelListBox,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newISABoardPanel.setCellWidth(brandListBox, "210px");
		newISABoardPanel.setCellWidth(modelListBox, "210px");

		externalPanel.add(titleNewISABoard);
		externalPanel.add(newISABoardPanel);

		initWidget(externalPanel);

	}// end constructor

	private void getInfoBoardCfg() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		// get all board fields to display
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] matrix) {
				// TODO: capire se verificare che le dimensioni
				// della matrice siano corrette
				if (matrix.length == 1 && matrix[0].length == 1
						&& matrix[0][0].equals(PerifericoUI.SESSION_ENDED)) {
					PerifericoUI.sessionEnded();
					return;
				}
				boardsId = new String[matrix.length];
				boardsType = new String[matrix.length];
				// clear table
				Utils.clearTable(table);
				table.getCellFormatter().setWidth(0, 0, "80px");
				table.getCellFormatter().setWidth(0, 1, "250px");
				table.getCellFormatter().setWidth(0, 2, "250px");
				table.getCellFormatter().setWidth(0, 3, "100px");
				table.getCellFormatter().setWidth(0, 4, "100px");
				table.getCellFormatter().setWidth(0, 5, "100px");
				table.getCellFormatter().setWidth(0, 6, "100px");
				for (int i = 0; i < matrix.length; i++) {
					table.setText(i, 0, new Integer(i + 1).toString());
					table.getCellFormatter().setStyleName(i, 0,
							"gwt-table-data");
					for (int j = 0; j < 2; j++) {
						table.setText(i, j + 1, matrix[i][j]);
						table.getCellFormatter().setStyleName(i, j + 1,
								"gwt-table-data");
					}
					// TODO capire come mettere queste
					// costanti
					// set icon for status
					IconImageBundle iconImageBundle = (IconImageBundle) GWT
							.create(IconImageBundle.class);
					if (matrix[i][2].equals(BoardWidget.PRE_INIT)) {
						// PRE_INIT
						Image pre_init = new Image();
						pre_init.setResource(iconImageBundle.pre_init());
						table.setWidget(i, 3, pre_init);
					} else if (matrix[i][2].equals(BoardWidget.DETECTED)) {
						// DETECTED
						Image detected = new Image();
						detected.setResource(iconImageBundle.detected());
						table.setWidget(i, 3, detected);
					} else if (matrix[i][2].equals(BoardWidget.NOT_DETECTED)) {
						// NOT_DETECTED
						Image not_detected = new Image();
						not_detected
								.setResource(iconImageBundle.not_detected());
						table.setWidget(i, 3, not_detected);
					} else if (matrix[i][2].equals(BoardWidget.INIT_FAILED)) {
						// INIT_FAILED
						Image init_failed = new Image();
						init_failed.setResource(iconImageBundle.init_failed());
						table.setWidget(i, 3, init_failed);
					} else if (matrix[i][2].equals(BoardWidget.INIT_OK)) {
						// INIT_OK
						Image init_ok = new Image();
						init_ok.setResource(iconImageBundle.init_ok());
						table.setWidget(i, 3, init_ok);
					}

					table.getCellFormatter().setAlignment(i, 3,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 3,
							"gwt-table-data");
					table.getWidget(i, 3).setTitle(matrix[i][2]);

					boardsId[i] = matrix[i][3];
					boardsType[i] = matrix[i][4];
					Button modifyButton = new Button();
					modifyButton.setStyleName("gwt-button-modify");
					modifyButton.setTitle(PerifericoUI.getMessages()
							.board_button_modify());
					table.setWidget(i, 4, modifyButton);
					table.getCellFormatter().setAlignment(i, 4,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 4,
							"gwt-table-data");
					modifyButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							senderButtonModify = (Button) event.getSource();
							// check if session is
							// ended, before change
							// page to BoardWidget
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

											boolean pciBoard = new Boolean(
													boardsType[k])
													.booleanValue();
											if (pciBoard) {
												PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
														.create(PerifericoUIService.class);
												ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
												endpoint2.setServiceEntryPoint(GWT
														.getModuleBaseURL()
														+ "uiservice");
												AsyncCallback<String[]> callback2 = new UIAsyncCallback<String[]>() {
													public void onSuccess(
															String[] resStr) {
														if (resStr.length == 1
																&& resStr[0]
																		.equals(PerifericoUI.SESSION_ENDED)) {
															PerifericoUI
																	.sessionEnded();
															return;
														}
														PerifericoUI.getBoardWidget()
																.setBoardInfo(
																		true,
																		resStr[0],
																		resStr[1],
																		resStr[2],
																		resStr[3],
																		false);
														PerifericoUI
																.setCurrentPage(PerifericoUI.getBoardWidget());
													}
												};

												perifService2.getPciDevice(
														boardsId[k], callback2);
											} else {
												PerifericoUI.getBoardWidget()
														.setBoardInfo(false,
																table.getText(
																		k, 1),
																table.getText(
																		k, 2),
																null,
																boardsId[k],
																false);
												PerifericoUI
														.setCurrentPage(PerifericoUI.getBoardWidget());
											}
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
							.board_button_delete());
					table.setWidget((i), 5, deleteButton);
					table.getCellFormatter().setAlignment(i, 5,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 5,
							"gwt-table-data");
					deleteButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							boolean confirmResult = Window
									.confirm(PerifericoUI.getMessages()
											.lbl_boards_confirm_delete());
							if (confirmResult) {
								Button button = (Button) event.getSource();
								for (int k = 0; k < table.getRowCount(); k++) {
									if (((Button) (table.getWidget(k, 5)))
											.equals(button)) {
										String boardIdStr = boardsId[k];
										deleteBoard(boardIdStr);
									}// end if
								}// end for
							}// end confirmresult
						}// end onclick
					});
					if ("true".equals(boardsType[i])) {
						Button unbindButton = new Button();
						unbindButton.setStyleName("gwt-button-unbind");
						unbindButton.setTitle(PerifericoUI.getMessages()
								.board_button_unbind());
						table.setWidget((i), 6, unbindButton);
						unbindButton.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								boolean confirmResult = Window
										.confirm(PerifericoUI.getMessages()
												.lbl_boards_confirm_unbind());
								if (confirmResult) {
									Button button = (Button) event.getSource();
									for (int k = 0; k < table.getRowCount(); k++) {
										if (((Button) (table.getWidget(k, 6)))
												.equals(button)) {
											String boardIdStr = boardsId[k];
											unbindBoard(boardIdStr);
										}// end if
									}// end for
								}// end confirmresult
							}// end onclick
						});
					} else {
						table.setText(i, 6, " ");
					}
					table.getCellFormatter().setAlignment(i, 6,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 6,
							"gwt-table-data");
				}
			}
		};

		perifService.getBoards(callback);

	} // end getInfoBoardCfg

	private void getDetectedDeviceCfg() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		// get all device fields to display
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] matrix) {
				// TODO: capire se verificare che le dimensioni
				// della matrice siano corrette
				if (matrix.length == 1 && matrix[0].length == 1
						&& matrix[0][0] != null) {
					if (matrix[0][0].equals(PerifericoUI.SESSION_ENDED)) {
						PerifericoUI.sessionEnded();
					} else
						Window.alert(matrix[0][0]);
					return;
				}
				// clear table
				Utils.clearTable(table2);
				table2.getCellFormatter().setWidth(0, 0, "150px");
				table2.getCellFormatter().setWidth(0, 1, "240px");
				table2.getCellFormatter().setWidth(0, 2, "240px");
				table2.getCellFormatter().setWidth(0, 3, "100px");
				table2.getCellFormatter().setWidth(0, 4, "250px");
				for (int i = 0; i < matrix.length; i++) {
					for (int j = 0; j < 3; j++) {
						table2.setText(i, j, matrix[i][j]);
						table2.getCellFormatter().setStyleName(i, j,
								"gwt-table-data");
					}
					Button newBoardButton = new Button();
					newBoardButton.setStyleName("gwt-button-new-riquadro");
					newBoardButton.setTitle(PerifericoUI.getMessages()
							.device_button_new_board());
					table2.setWidget(i, 3, newBoardButton);
					table2.getCellFormatter().setAlignment(i, 3,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table2.getCellFormatter().setStyleName(i, 3,
							"gwt-table-data");
					newBoardButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							senderButtonNewBoard = (Button) event.getSource();
							// check if session is ended, before change
							// page to BoardWidget
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
									for (int k = 0; k < table2.getRowCount(); k++) {
										if (((Button) (table2.getWidget(k, 3)))
												.equals(senderButtonNewBoard)) {
											PerifericoUI.getBoardWidget().setBoardInfo(
													true, table2.getText(k, 1),
													table2.getText(k, 2),
													table2.getText(k, 0), null,
													true);
											PerifericoUI
													.setCurrentPage(PerifericoUI.getBoardWidget());
										}
									}
								}
							};
							perifServiceSession
									.verifySessionEnded(callbackSession);
						}
					});
					HorizontalPanel hPanel = new HorizontalPanel();
					hPanel.setStyleName("gwt-no-border-panel");
					final ListBox bindableBoardIndexesListBox = new ListBox();
					bindableBoardIndexesListBox.setWidth("150px");
					bindableBoardIndexesListBox.setVisibleItemCount(1);
					hPanel.add(bindableBoardIndexesListBox);
					PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
							.create(PerifericoUIService.class);
					ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
					endpoint2.setServiceEntryPoint(GWT.getModuleBaseURL()
							+ "uiservice");
					AsyncCallback<String[]> callback2 = new UIAsyncCallback<String[]>() {
						public void onSuccess(String[] indexes) {
							if (indexes.length == 1 && indexes[0] != null) {
								if (indexes[0]
										.equals(PerifericoUI.SESSION_ENDED)) {
									PerifericoUI.sessionEnded();
								} else
									Window.alert(indexes[0]);
							} else {
								bindableBoardIndexesListBox.clear();
								bindableBoardIndexesListBox.addItem(
										PerifericoUI.getMessages()
												.select_bindable_board_index(),
										new String("0"));
								for (int i = 1; i < indexes.length; i++) {
									bindableBoardIndexesListBox.addItem(Integer
											.toString((new Integer(indexes[i])
													.intValue() + 1)));
								}
							}
						}
					};
					perifService2.getBindableBoardsIndexes(
							table2.getText(i, 0), callback2);

					Button associationButton = new Button();
					associationButton.setStyleName("gwt-button-associate");
					associationButton.setTitle(PerifericoUI.getMessages()
							.device_button_association_board());
					hPanel.add(associationButton);
					hPanel.setCellHorizontalAlignment(associationButton,
							HasHorizontalAlignment.ALIGN_CENTER);
					table2.setWidget(i, 4, hPanel);
					table2.getCellFormatter().setStyleName(i, 4,
							"gwt-table-data");
					associationButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							Button button = (Button) event.getSource();
							for (int k = 0; k < table2.getRowCount(); k++) {

								HorizontalPanel hPanel2 = (HorizontalPanel) table2
										.getWidget(k, 4);
								if (((Button) hPanel2.getWidget(1))
										.equals(button)) {
									int boardIndexSelected = bindableBoardIndexesListBox
											.getSelectedIndex();
									if (boardIndexSelected == 0)
										Window.alert(PerifericoUI.getMessages()
												.error_select_element());
									else {
										String boardIndexSelectedValue = bindableBoardIndexesListBox
												.getValue(boardIndexSelected);
										int selectedValue = new Integer(
												boardIndexSelectedValue)
												.intValue();
										associateBoard(new Integer(
												selectedValue - 1), table2
												.getText(k, 0));
									}
								}
							}
						}
					});
				}

			}
		};

		perifService.getDetectedDevice(callback);

	} // end getDetectedDeviceCfg

	private void getBoardBrandsCfg() {

		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] brands) {
				brandListBox.clear();
				brandListBox.addItem(
						PerifericoUI.getMessages().select_brand_board_name(),
						new String("0"));
				for (int i = 0; i < brands.length; i++) {
					brandListBox.addItem(brands[i]);
				}
				modelListBox.clear();
			}
		};
		perifService.getBoardBrandsCfg(callback);
	} // end getBoardBrandsCfg

	private void deleteBoard(String boardIdStr) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resultString) {
				if (resultString.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else {
					// TODO: capire cosa fare del valore di ritorno
					Boolean resValue = new Boolean(resultString);
					if (resValue.booleanValue()) {
						getInfoBoardCfg();
						getDetectedDeviceCfg();
						// clearGridRowFormatter();
					}
				}
			}
		};

		perifService.deleteBoard(boardIdStr, callback);
	} // end deleteBoard

	private void unbindBoard(String boardIdStr) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resultString) {
				if (resultString.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else {
					// TODO: capire cosa fare del valore di ritorno
					Boolean resValue = new Boolean(resultString);
					if (resValue.booleanValue()) {
						getInfoBoardCfg();
						getDetectedDeviceCfg();
						// clearGridRowFormatter();
					}
				}
			}
		};

		perifService.unbindBoard(boardIdStr, callback);
	}

	private void associateBoard(Integer boardIndex, String deviceStrId) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resValue) {
				if (resValue == null) {
					getInfoBoardCfg();
					getDetectedDeviceCfg();
					getBoardBrandsCfg();
					boardId.setValue("");
					// clearGridRowFormatter();
				} else if (resValue.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else
					Window.alert(resValue);
			}
		};

		perifService.associateBoard(boardIndex, deviceStrId, callback);
	} // end associateBoard

	@Override
	public void onChange(ChangeEvent event) {
		ListBox listBoxOfBrand = (ListBox) event.getSource();
		int indexSelected = listBoxOfBrand.getSelectedIndex();
		String selectedValue = listBoxOfBrand.getValue(indexSelected);
		if (!selectedValue.equals("0")) {
			PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
					.create(PerifericoUIService.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
			endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
			AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
				public void onSuccess(String[] models) {
					modelListBox.clear();
					modelListBox.addItem(
							PerifericoUI.getMessages().select_model_board_name(),
							new String("0"));
					for (int i = 0; i < models.length; i++) {
						modelListBox.addItem(models[i]);
					}
				}
			};

			perifService.getBoardModelsCfg(selectedValue, callback);
		} else
			modelListBox.clear();

	}

	@Override
	protected void loadContent() {
		getInfoBoardCfg();
		getDetectedDeviceCfg();
		getBoardBrandsCfg();
		boardId.setValue("");
	}

	@Override
	protected void reset() {
		clearGridRowFormatter(table);
		clearGridRowFormatter(table2);
	}

}// end class
