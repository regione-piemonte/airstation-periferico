/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: board configuration page
 * Change log:
 *   2008-04-18: initial version
 * ----------------------------------------------------------------------------
 * $Id: BoardWidget.java,v 1.57 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.AsyncPageOperation;
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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Board configuration page
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class BoardWidget extends UIPage {

	final static String PRE_INIT = "PRE_INIT";

	final static String DETECTED = "DETECTED";

	final static String NOT_DETECTED = "NOT_DETECTED";

	final static String INIT_FAILED = "INIT_FAILED";

	final static String INIT_OK = "INIT_OK";

	private final static String ANALOG_INPUT = "ANALOG_INPUT";

	private final static String DIGITAL_INPUT = "DIGITAL_INPUT";

	private final static String DIGITAL_INPUT_OUTPUT = "DIGITAL_INPUT_OUTPUT";

	private final static String DIGITAL_OUTPUT = "DIGITAL_OUTPUT";

	private String boardId = null;

	private String brandId = "";

	private String modelId = "";

	private String pciDeviceId = null;

	private Label anagraphicTitle = new Label();

	// Grids for board information.
	private final FlexTable anagraphicGrid = new FlexTable();

	private int driverParamsNumber = 0;

	private boolean pciBoardVersionActive = false;

	private Integer[] dParamIndexes;

	private Boolean[] dParamIsOptional;

	private ListBox[] allowedValuesListBoxes;

	private ListBox pciBoardVersionListBox;

	private PanelButtonWidget panelButton;

	private boolean pciBoard = true;

	private final FlexTable table;

	private final FlexTable driverTable;

	private final FlexTable driverHeaderTable;

	private final FlexTable headerTable;

	private Button senderButtonConfigure;

	private boolean newBoard;

	public BoardWidget() {

		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();
		panelButtonWidget.addButton(PerifericoUI.makeUpButton());
		panelButtonWidget.addButton(PerifericoUI.makeSaveButton());
		panelButtonWidget.addButton(PerifericoUI.makeReloadButton());
		panelButtonWidget.addButton(PerifericoUI
				.makeConfHelpButton("boardWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().board_title(), panelButtonWidget);
		// Label and panel for anagraphic info
		anagraphicTitle.setStyleName("gwt-Label-title");

		// panel that contains anagraphic info and buttons
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed");

		// panel that contains anagraphic info
		HorizontalPanel anagraphicInfoPanel = new HorizontalPanel();
		anagraphicInfoPanel.add(anagraphicGrid);

		// format the grid for anagraphic info
		anagraphicGrid.getCellFormatter().setWidth(0, 0, "150px");
		anagraphicGrid.getCellFormatter().setWidth(0, 1, "150px");
		anagraphicGrid.setCellPadding(5);
		anagraphicGrid.setCellSpacing(5);

		// panel that contains driver info
		VerticalPanel driverPanel = new VerticalPanel();
		driverHeaderTable = new FlexTable();
		driverHeaderTable
				.setText(0, 0, PerifericoUI.getMessages().lbl_driver_name());
		driverHeaderTable.setText(0, 1,
				PerifericoUI.getMessages().lbl_driver_value());
		driverHeaderTable.setStyleName("gwt-table-header");
		driverHeaderTable.setWidth("100%");
		driverHeaderTable.getCellFormatter().setWidth(0, 0, "245px");
		driverHeaderTable.getCellFormatter().setWidth(0, 1, "245px");
		for (int j = 0; j < 2; j++) {
			driverHeaderTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}
		driverPanel.add(driverHeaderTable);

		ScrollPanel driverScrollPanel = new ScrollPanel();
		driverTable = new FlexTable();
		driverTable.setStyleName("gwt-table-data");
		driverTable.setWidth("100%");
		driverScrollPanel.add(driverTable);
		driverScrollPanel.setHeight("100px");
		driverPanel.add(driverScrollPanel);

		anagraphicInfoPanel.add(driverPanel);

		anagraphicInfoPanel.setWidth("100%");
		anagraphicInfoPanel.getWidget(0).setWidth("480px");
		anagraphicInfoPanel.getWidget(1).setWidth("480px");

		panel.add(anagraphicInfoPanel);

		panelButton = PerifericoUI.makeUndoSendPanelButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
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
							reset();
							readBoardInfo();
						}
					}
				};
				perifServiceSession.verifySessionEnded(callbackSession);
			}
		}, new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (pciBoard)
					verifyPCIBoardFields();
				else
					verifyBoardFields();
			}
		});

		// panel that contains button for anagraphic info
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setStyleName("gwt-button-panel");
		hPanel.add(panelButton);
		hPanel.setCellHorizontalAlignment(panelButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		hPanel.setWidth("100%");
		panel.add(hPanel);

		externalPanel.add(anagraphicTitle);
		externalPanel.add(panel);

		// Label and panel for input info
		Label inputTitle = new Label();
		inputTitle.setStyleName("gwt-Label-title");
		inputTitle.setText(PerifericoUI.getMessages().lbl_board_info_title());

		// panel that contains the grid for input info
		VerticalPanel inputInfoPanel = new VerticalPanel();
		inputInfoPanel.setStyleName("gwt-post-boxed");

		// Prepare info table's title
		headerTable = new FlexTable();
		headerTable.setText(0, 0, PerifericoUI.getMessages().lbl_type());
		headerTable.setText(0, 1, PerifericoUI.getMessages().lbl_id());
		headerTable.setText(0, 2, PerifericoUI.getMessages().lbl_channel_number());
		headerTable.setText(0, 3, PerifericoUI.getMessages().lbl_configure());
		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "430px");
		headerTable.getCellFormatter().setWidth(0, 1, "200px");
		headerTable.getCellFormatter().setWidth(0, 2, "200px");
		headerTable.getCellFormatter().setWidth(0, 3, "150px");
		for (int j = 0; j < 4; j++) {
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}
		inputInfoPanel.add(headerTable);

		// Prepare table for board info in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("190px");
		inputInfoPanel.add(scrollPanel);

		externalPanel.add(inputTitle);
		externalPanel.add(inputInfoPanel);

		initWidget(externalPanel);

		// Put values in the anagraphic grid cells.
		String message = PerifericoUI.getMessages().lbl_brand() + ":";
		anagraphicGrid.setText(0, 0, message);

		message = PerifericoUI.getMessages().lbl_model() + ":";
		anagraphicGrid.setText(1, 0, message);

		anagraphicGrid.setText(2, 0, "");
		pciBoardVersionListBox = new ListBox();
		pciBoardVersionListBox.setWidth("150px");
		pciBoardVersionListBox.setStyleName("gwt-bg-text-orange");
		pciBoardVersionListBox.setVisible(false);
		pciBoardVersionListBox.setTitle(PerifericoUI.getMessages()
				.alert_pci_board_versions());
		anagraphicGrid.setWidget(2, 1, pciBoardVersionListBox);

		message = PerifericoUI.getMessages().lbl_status() + ":";
		anagraphicGrid.setText(3, 0, message);

	} // end constructor

	void setBoardInfo(boolean pciBoard, String brand, String model,
			String pciDeviceStr, String boardId, boolean newBoard) {
		this.pciBoard = pciBoard;
		this.brandId = brand;
		this.modelId = model;
		this.pciDeviceId = pciDeviceStr;
		this.boardId = boardId;
		this.newBoard = newBoard;
	}

	private void readBoardInfo() {
		anagraphicGrid.setText(0, 1, brandId);
		anagraphicGrid.setText(1, 1, modelId);
		anagraphicGrid.setText(2, 0, "");
		pciBoardVersionListBox.clear();
		pciBoardVersionActive = false;
		if (!pciBoard)
			pciBoardVersionListBox.setVisible(false);
		setBoardType(pciBoard, pciDeviceId);
		setBoardStatus(boardId);
		setBoardChannelInfo(boardId);
		final PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		final AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] dParamMatrix) {
				if (dParamMatrix.length == 1
						&& dParamMatrix[0].length == 1
						&& PerifericoUI.SESSION_ENDED
								.equals(dParamMatrix[0][0])) {
					PerifericoUI.sessionEnded();
					return;
				}
				driverParamsNumber = dParamMatrix.length;
				dParamIndexes = new Integer[driverParamsNumber];
				dParamIsOptional = new Boolean[driverParamsNumber];
				allowedValuesListBoxes = new ListBox[driverParamsNumber];
				if (driverParamsNumber == 0) {
					driverHeaderTable.setVisible(false);
					driverTable.setVisible(false);
					panelButton.setVisible(pciBoardVersionActive || !pciBoard);
				} else {
					driverHeaderTable.setVisible(true);
					driverTable.setVisible(true);
					panelButton.setVisible(true);

					Utils.clearTable(driverTable);
					driverTable.getCellFormatter().setWidth(0, 0, "245px");
					driverTable.getCellFormatter().setWidth(0, 1, "245px");

					for (int i = 0; i < driverParamsNumber; i++) {
						String[] dParamFields = dParamMatrix[i];
						dParamIndexes[i] = new Integer(dParamFields[0]);
						Label info = new Label();
						info.setText(dParamFields[1]);
						info.setTitle(dParamFields[3]);
						driverTable.setWidget(i, 0, info);
						driverTable.getCellFormatter().setStyleName(i, 0,
								"gwt-table-data");
						dParamIsOptional[i] = new Boolean(dParamFields[4]);
						allowedValuesListBoxes[i] = new ListBox();
						allowedValuesListBoxes[i].setWidth("100px");
						if (dParamFields.length > 5 && dParamFields[5] != null) {
							// case of list of allowed values
							boolean lastValue = false;
							for (int j = 0; j < (dParamFields.length - 5)
									&& !lastValue; j++) {
								if (dParamFields[j + 5] != null) {
									allowedValuesListBoxes[i]
											.addItem(dParamFields[j + 5]);
								} else
									lastValue = true;
							}
							// case of showing one value of the listbox just
							// selected
							if (dParamFields[2] != null) {
								for (int k = 0; k < allowedValuesListBoxes[i]
										.getItemCount(); k++) {
									if (allowedValuesListBoxes[i]
											.getItemText(k).equals(
													dParamFields[2]))
										allowedValuesListBoxes[i]
												.setItemSelected(k, true);
								}
							}
							driverTable.getCellFormatter().setStyleName(i, 1,
									"gwt-table-data");
							driverTable.setWidget(i, 1,
									allowedValuesListBoxes[i]);
						} else {
							// case of text box
							TextBox editTextBox = new TextBox();
							// case of showing a valuein the edit box
							if (dParamFields[2] != null)
								editTextBox.setText(dParamFields[2]);
							driverTable.getCellFormatter().setStyleName(i, 1,
									"gwt-table-data");
							driverTable.setWidget(i, 1, editTextBox);
						}
					}
				}
			}
		};

		AsyncCallback<String[]> acb_getPCIBoardVersions = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] versions) {
				anagraphicGrid.setText(2, 0, "");
				pciBoardVersionListBox.clear();
				pciBoardVersionActive = false;
				if (versions.length == 1
						&& PerifericoUI.SESSION_ENDED.equals(versions[0])) {
					PerifericoUI.sessionEnded();
				} else {
					for (int i = 0; i < versions.length; i++)
						pciBoardVersionListBox.addItem(versions[i]);
					if (versions.length > 0) {
						anagraphicGrid.setText(2, 0,
								PerifericoUI.getMessages().lbl_version());
						pciBoardVersionActive = true;
						if (!newBoard)
							getPCIBoardVersion();
					}
				}
				pciBoardVersionListBox.setVisible(pciBoardVersionActive);
				perifService.getDriverParams(brandId, modelId, boardId,
						callback);
			}
		};

		if (pciBoard)
			perifService.getPCIBoardVersionList(pciDeviceId,
					acb_getPCIBoardVersions);
		else
			perifService.getDriverParams(brandId, modelId, boardId, callback);
	}

	private void setBoardChannelInfo(String boardIdStr) {
		Utils.clearTable(table);
		table.getCellFormatter().setWidth(0, 0, "430px");
		table.getCellFormatter().setWidth(0, 1, "200px");
		table.getCellFormatter().setWidth(0, 2, "200px");
		table.getCellFormatter().setWidth(0, 3, "150px");
		if (boardIdStr == null || boardIdStr.isEmpty())
			return;
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] resMatrix) {
				if (resMatrix.length == 1 && resMatrix[0].length == 1
						&& PerifericoUI.SESSION_ENDED.equals(resMatrix[0][0])) {
					PerifericoUI.sessionEnded();
					return;
				}
				for (int i = 0; i < resMatrix.length; i++) {
					for (int j = 0; j < 3; j++) {

						if (j == 0) {
							if (resMatrix[i][j].equals(ANALOG_INPUT))
								table.setText(i, j, PerifericoUI.getMessages()
										.lbl_analog_input());
							else if (resMatrix[i][j].equals(DIGITAL_INPUT))
								table.setText(i, j, PerifericoUI.getMessages()
										.lbl_digital_input());
							else if (resMatrix[i][j]
									.equals(DIGITAL_INPUT_OUTPUT))
								table.setText(i, j, PerifericoUI.getMessages()
										.lbl_digital_input_output());
							else if (resMatrix[i][j].equals(DIGITAL_OUTPUT))
								table.setText(i, j, PerifericoUI.getMessages()
										.lbl_digital_output());
						} else
							table.setText(i, j, resMatrix[i][j]);
						table.getCellFormatter().setStyleName(i, j,
								"gwt-table-data");
					}

					Button configureButton = new Button();
					configureButton.setStyleName("gwt-button-configure");
					configureButton.setTitle(PerifericoUI.getMessages()
							.configure_board_io());
					table.setWidget(i, 3, configureButton);
					table.getCellFormatter().setAlignment(i, 3,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					table.getCellFormatter().setStyleName(i, 3,
							"gwt-table-data");
					configureButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							senderButtonConfigure = (Button) event.getSource();
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
										if (((Button) table.getWidget(k, 3))
												.equals(senderButtonConfigure)) {
											String type = null;
											if (table
													.getText(k, 0)
													.equals(PerifericoUI.getMessages()
															.lbl_analog_input()))
												type = "ANALOG_INPUT";
											else if (table
													.getText(k, 0)
													.equals(PerifericoUI.getMessages()
															.lbl_digital_input()))
												type = "DIGITAL_INPUT";
											else if (table
													.getText(k, 0)
													.equals(PerifericoUI.getMessages()
															.lbl_digital_input_output()))
												type = "DIGITAL_INPUT_OUTPUT";
											else if (table
													.getText(k, 0)
													.equals(PerifericoUI.getMessages()
															.lbl_digital_output()))
												type = "DIGITAL_OUTPUT";
											PerifericoUI.getSubdeviceWidget()
													.setFields(boardId, type,
															table.getText(k, 1));
											PerifericoUI
													.setCurrentPage(PerifericoUI.getSubdeviceWidget());
										}
									}
								}
							};
							perifServiceSession
									.verifySessionEnded(callbackSession);

						}
					});
				}
			}
		};

		if (boardIdStr != null)
			perifService.getBoardChannelInfo(boardIdStr, callback);
	}

	private void setBoardStatus(String boardId) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String status) {
				if (PerifericoUI.SESSION_ENDED.equals(status)) {
					PerifericoUI.sessionEnded();
				} else
					anagraphicGrid.setText(3, 1, status);
			}
		};

		if (boardId != null && !boardId.isEmpty()) {
			perifService.getBoardStatus(boardId, callback);
		} else {
			anagraphicGrid.setText(3, 1, PRE_INIT);
			makeNewBoard();
		}
	}

	private void makeNewBoard() {
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resultString) {
				boardId = resultString;
			}
		};

		PerifericoUI.getService().setNewBoard(new Boolean(pciBoard),
				anagraphicGrid.getText(0, 1), anagraphicGrid.getText(1, 1),
				anagraphicGrid.getText(4, 1), callback);
	}

	private void setBoardType(boolean pciBoard, String pciDeviceStr) {
		if (pciBoard) {
			anagraphicTitle.setText(PerifericoUI.getMessages().lbl_pci_board());
			String message = PerifericoUI.getMessages().lbl_board_pci_device() + ":";
			anagraphicGrid.setText(4, 0, message);
			anagraphicGrid.setText(4, 1, pciDeviceStr);
		} else {
			anagraphicTitle.setText(PerifericoUI.getMessages().lbl_isa_board());
			anagraphicGrid.setText(4, 0, "");
			anagraphicGrid.setText(4, 1, "");
		}
	}

	private void getPCIBoardVersion() {
		if (!pciBoardVersionActive)
			return;
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String> acb_getPCIBoardVersion = new UIAsyncCallback<String>() {
			public void onSuccess(String version) {
				if (PerifericoUI.SESSION_ENDED.equals(version)) {
					PerifericoUI.sessionEnded();
					return;
				}
				for (int i = 0; i < pciBoardVersionListBox.getItemCount(); i++) {
					String item = pciBoardVersionListBox.getValue(i);
					if (item.equals(version)) {
						pciBoardVersionListBox.setSelectedIndex(i);
						break;
					}
				}
			}
		};

		perifService.getPCIBoardVersion(boardId, acb_getPCIBoardVersion);
	}

	private void verifyInsertNewBoard() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String resultString) {
				if (resultString != null) {
					if (resultString.equals(PerifericoUI.SESSION_ENDED))
						PerifericoUI.sessionEnded();
					else
						// error case
						Window.alert(resultString);
				} else {
					Utils.sendVerifyOk();
					doInitBoard();
				}
			}
		};

		perifService.verifyInsertNewBoard(boardId, callback);
	}

	private void verifyPCIBoardFields() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		AsyncCallback<String> acb_setVerifyPCIBoardFields = new UIAsyncCallback<String>() {
			public void onSuccess(String result) {
				if (result != null) {
					if (result.equals(PerifericoUI.SESSION_ENDED))
						PerifericoUI.sessionEnded();
					else
						Window.alert(result); // error case
				} else {
					verifyInsertNewBoard();
				}
			}
		};

		String boardVersion = "";
		if (pciBoardVersionActive) {
			int index = pciBoardVersionListBox.getSelectedIndex();
			if (index >= 0)
				boardVersion = pciBoardVersionListBox.getItemText(index);
		}
		perifService.setVerifyPCIBoardFields(boardId,
				anagraphicGrid.getText(0, 1), anagraphicGrid.getText(1, 1),
				boardVersion, getDriverParamsMatrix(),
				acb_setVerifyPCIBoardFields);
	}

	private void verifyBoardFields() {
		AsyncCallback<Object> callback = new UIAsyncCallback<Object>() {
			public void onSuccess(Object noresult) {
				verifyInsertNewBoard();
			}
		};

		PerifericoUI.getService().setVerifyBoardFields(boardId,
				anagraphicGrid.getText(0, 1), anagraphicGrid.getText(1, 1),
				getDriverParamsMatrix(), callback);
	}// end verifyBoardFields

	private void doInitBoard() {
		newBoard = false;
		String statusStr = anagraphicGrid.getText(3, 1);
		if (PRE_INIT.equals(statusStr) || DETECTED.equals(statusStr)
				|| INIT_FAILED.equals(statusStr)) {
			AsyncCallback<Object> callback = new UIAsyncCallback<Object>() {
				public void onSuccess(Object noResult) {
					setBoardStatus(boardId);
					clearGridRowFormatter(table);
					setBoardChannelInfo(boardId);
				}
			};

			PerifericoUI.getService().initBoard(new Boolean(pciBoard), boardId,
					callback);
		}

	} // end doInitBoard

	private String[][] getDriverParamsMatrix() {
		String[][] driverParamsMatrix = new String[driverParamsNumber][2];
		for (int i = 0; i < driverParamsNumber; i++) {
			driverParamsMatrix[i][0] = driverTable.getText(i, 0);
			Widget tmpWidget = driverTable.getWidget(i, 1);
			if (tmpWidget instanceof ListBox) {
				ListBox tmpListBox = (ListBox) tmpWidget;
				driverParamsMatrix[i][1] = tmpListBox.getItemText(tmpListBox
						.getSelectedIndex());
			} else if (tmpWidget instanceof TextBox) {
				TextBox tmpTextBox = (TextBox) tmpWidget;
				driverParamsMatrix[i][1] = tmpTextBox.getText();
			}
		}
		return driverParamsMatrix;
	} // end getDriverParamsMatrix

	@Override
	protected void dismissContent(final AsyncPageOperation asyncPageOperation) {
		AsyncCallback<Boolean> callback = new UIAsyncCallback<Boolean>() {
			public void onSuccess(Boolean result) {
				if (!result) {
					boolean confirmResult = Window
							.confirm(PerifericoUI.getMessages()
									.confirm_abandon_page());
					if (confirmResult)
						asyncPageOperation.complete();
				} else
					asyncPageOperation.complete();
			}// end onsucces
		};

		if (pciBoard) {
			String boardVersion = "";
			if (pciBoardVersionActive) {
				int index = pciBoardVersionListBox.getSelectedIndex();
				if (index >= 0)
					boardVersion = pciBoardVersionListBox.getItemText(index);
			}
			PerifericoUI.getService().verifySamePCIBoardConfig(boardId,
					brandId, modelId, boardVersion, getDriverParamsMatrix(),
					callback);
		} else {
			PerifericoUI.getService().verifySameBoardConfig(boardId, brandId,
					modelId, getDriverParamsMatrix(), callback);
		}
	}

	@Override
	protected void loadContent() {
		readBoardInfo();
	}

	@Override
	protected void reset() {
		panelButton.setVisible(false);
		driverTable.setVisible(false);
		clearGridRowFormatter(table);
		clearGridRowFormatter(driverTable);
	}

}
