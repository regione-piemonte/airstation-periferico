/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: analyzer configuration page
 * Change log:
 *   2008-01-10: initial version
 *   2011-12-01: full code rewrite by pierfrancesco.vallosio@consulenti.csi.it
 * ----------------------------------------------------------------------------
 * $Id: AnalyzerWidget.java,v 1.90 2015/09/17 11:08:58 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.AsyncPageOperation;
import it.csi.periferico.ui.client.pagecontrol.PageUpdateAction;
import it.csi.periferico.ui.client.pagecontrol.UIPage;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * Widget that displays analyzer's information
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class AnalyzerWidget extends UIPage {

	private Button prevButton;

	private Button nextButton;

	private AnalyzerPanel analyzer;

	private VerticalPanel analyzerInfoContainer;

	private FaultPanel faultPanel;

	private VerticalPanel faultContainer;

	private OperationPanel operationPanel;

	private VerticalPanel operationContainer;

	// Begin: page action handlers
	private ClickHandler prevPageHandler;

	private ClickHandler nextPageHandler;

	private ClickHandler listElementHandler;

	private ClickHandler faultUndoHandler;

	private ClickHandler faultSendVerifyHandler;

	private ClickHandler faultNewHandler;

	private ClickHandler faultDeleteHandler;

	private ClickHandler dataValidNewHandler;

	private ClickHandler dataValidDeleteHandler;

	private ClickHandler infoUndoHandler;

	private ClickHandler infoSendVerifyHandler;

	private ClickHandler calibStartHandler;

	private ClickHandler calibStopHandler;

	private ClickHandler maintenanceStartHandler;

	private ClickHandler maintenanceStopHandler;
	// End: page action handlers

	private String[][] matrixIdType = null;

	private Integer currentIndex = null;

	private String analyzerId = null;

	private boolean newAnalyzerMode = false;

	private boolean newFault = false;

	private boolean newDataValid = false;

	private String[][] analyzerStatusValues = new String[0][];

	public AnalyzerWidget() {
		initPageHandlers();

		prevButton = PerifericoUI.makePrevButton(prevPageHandler);
		nextButton = PerifericoUI.makeForwardButton(nextPageHandler);
		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();
		panelButtonWidget.addButton(prevButton);
		panelButtonWidget.addButton(nextButton);
		panelButtonWidget.addButton(PerifericoUI.makeUpButton());
		panelButtonWidget.addButton(PerifericoUI.makeSaveButton());
		panelButtonWidget.addButton(PerifericoUI.makeReloadButton());
		panelButtonWidget.addButton(PerifericoUI
				.makeConfHelpButton("analyzerWidget"));
		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().analyzer_title(), panelButtonWidget);

		analyzerInfoContainer = new VerticalPanel();
		externalPanel.add(analyzerInfoContainer);

		faultContainer = new VerticalPanel();
		externalPanel.add(faultContainer);

		operationContainer = new VerticalPanel();
		externalPanel.add(operationContainer);
		operationPanel = new OperationPanel(operationContainer,
				listElementHandler, calibStartHandler, calibStopHandler,
				maintenanceStartHandler, maintenanceStopHandler);

		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] result) {
				analyzerStatusValues = result;
			}
		};
		PerifericoUI.getService().getAnalyzerStatusList(callback);

		initWidget(externalPanel);
	}

	private void initPageHandlers() {

		prevPageHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (matrixIdType == null || matrixIdType.length == 0)
					return;
				if (currentIndex > 0) {
					PerifericoUI.updateCurrentPage(new PageUpdateAction() {
						@Override
						public void action() {
							currentIndex--;
							analyzerId = matrixIdType[currentIndex][0];
							setAnalyzerImpl(matrixIdType[currentIndex][1]);
							updatePage();
						}
					});
				}
			}
		};

		nextPageHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (matrixIdType == null || matrixIdType.length == 0)
					return;
				if (currentIndex < matrixIdType.length - 1) {
					PerifericoUI.updateCurrentPage(new PageUpdateAction() {
						@Override
						public void action() {
							currentIndex++;
							analyzerId = matrixIdType[currentIndex][0];
							setAnalyzerImpl(matrixIdType[currentIndex][1]);
							updatePage();
						}
					});
				}
			}
		};

		listElementHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String anType = analyzer.getType();
				if (Utils.WIND.equals(anType)) {
					PerifericoUI.getWindElementWidget().setAnalyzer(analyzerId,
							anType);
					PerifericoUI.setCurrentPage(PerifericoUI.getWindElementWidget());
				} else if (Utils.RAIN.equals(anType)) {
					PerifericoUI.getRainElementWidget().setAnalyzer(analyzerId,
							anType);
					PerifericoUI.setCurrentPage(PerifericoUI.getRainElementWidget());
				} else {
					PerifericoUI.getListElementWidget().setAnalyzer(analyzerId,
							anType);
					PerifericoUI.setCurrentPage(PerifericoUI.getListElementWidget());
				}
			}
		};

		faultUndoHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				getFaultInfo(newFault);
				getDataValidInfo(newDataValid);
			}
		};

		faultSendVerifyHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				verifyFaultAndDataValidFields();
			}
		};

		faultNewHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (faultPanel instanceof DigitalFaultPanel) {
					newFault = true;
					getFaultInfo(true);
				}
			}
		};

		faultDeleteHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!Window.confirm(PerifericoUI.getMessages()
						.lbl_fault_confirm_delete()))
					return;
				deleteFaultInfo();
			}
		};

		dataValidNewHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (faultPanel instanceof DigitalFaultPanel) {
					newDataValid = true;
					getDataValidInfo(true);
				}
			}
		};

		dataValidDeleteHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!Window.confirm(PerifericoUI.getMessages()
						.lbl_data_valid_confirm_delete()))
					return;
				deleteDataValidInfo();
			}
		};

		infoUndoHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (newAnalyzerMode)
					PerifericoUI.goToUpperLevelPage();
				else
					getAnalyzerInfo();
			}
		};

		infoSendVerifyHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				verifyAnalyzerFields();
			}
		};

		calibStartHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AsyncCallback<Boolean[]> callback = new UIAsyncCallback<Boolean[]>() {
					public void onSuccess(Boolean[] result) {
						operationPanel.setCalibStatus(result[1]);
						if (!result[0])
							Window.alert(PerifericoUI.getMessages()
									.cannot_activate_calib());
					}
				};
				PerifericoUI.getService().activeCalibration(analyzerId,
						callback);
			}
		};

		calibStopHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AsyncCallback<String> callback = new UIAsyncCallback<String>() {
					public void onSuccess(String status) {
						operationPanel.setCalibStatus(new Boolean(status));
					}
				};
				PerifericoUI.getService().disactiveCalibration(analyzerId,
						callback);
			}

		};

		maintenanceStartHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AsyncCallback<Boolean[]> callback = new UIAsyncCallback<Boolean[]>() {
					public void onSuccess(Boolean[] result) {
						operationPanel.setMaintenanceStatus(result[1]);
						if (!result[0])
							Window.alert(PerifericoUI.getMessages()
									.cannot_activate_maintenance());
					}
				};
				PerifericoUI.getService().activeMaintenance(analyzerId,
						callback);
			}
		};

		maintenanceStopHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AsyncCallback<String> callback = new UIAsyncCallback<String>() {
					public void onSuccess(String status) {
						operationPanel
								.setMaintenanceStatus(new Boolean(status));
					}
				};
				PerifericoUI.getService().disactiveMaintenance(analyzerId,
						callback);
			}
		};

	}

	private void verifyAnalyzerFields() {
		if (analyzer.getStatus() == null) {
			Window.alert(PerifericoUI.getMessages().error_select_element());
			return;
		}
		AsyncCallback<Object> cBack = new UIAsyncCallback<Object>() {
			public void onSuccess(Object noresult) {
				Utils.sendVerifyOk();
				faultPanel.setVisible(true);
				operationPanel.show(analyzer.isEnabled());
			}
		};
		AsyncCallback<Object> cBackNew = new UIAsyncCallback<Object>() {
			public void onSuccess(Object noresult) {
				PerifericoUI.getService().verifyInsertNewAnalyzer(analyzerId,
						new UIAsyncCallback<String>() {
							public void onSuccess(String result) {
								newAnalyzerMode = false;
								Utils.sendVerifyOk();
								getFaultInfo(false);
								getDataValidInfo(false);
								operationPanel.show(false);
							}
						});
			}
		};

		// Analyzer info array:
		// [0]: name
		// [1]: brand
		// [2]: model
		// [3]: description
		// [4]: serial number
		// [5]: user notes
		// [6]: status
		// [7]: uiURL
		// [8]: min voltage for sample/wind/avg analyzers | portType for dpa
		// [9]: max voltage for sample/wind/avg analyzers | hostName for dpa
		// [10]: min range ext for sample/wind/avg analyzers | ipPort for dpa
		// [11]: max range ext for sample/wind/avg analyzers | ttyDevice for dpa
		// [12]: diff. mode for sample/wind/avg analyzers | ttyBaudRate for dpa
		// [13]: ttyParams for dpa analyzer only
		// [14]: driverParams for dpa analyzer only
		PerifericoUI.getService().setVerifyAnalyzerFields(analyzerId,
				analyzer.getInfoArray(), newAnalyzerMode ? cBackNew : cBack);
	}

	private void getNewAnalyzerDefaultInfo() {
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] result) {
				analyzer.setDefaultInfo(result);
				analyzerId = result[0];
			}
		};
		PerifericoUI.getService().setNewAnalyzer(analyzer.getType(),
				analyzer.getBrand(), analyzer.getModel(), callback);
	}

	private void getAnalyzerInfo() {
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] result) {
				// result[ 0]: analyzerId
				// result[10]: manualCalibrationRunning
				// result[11]: maintenanceInProgress
				if (!analyzerId.equals(result[0]))
					throw new IllegalStateException("Analyzer id mismatch: "
							+ "expected " + analyzerId + " got " + result[0]);
				analyzer.setInfo(result);
				operationPanel.setCalibStatus(new Boolean(result[10]));
				operationPanel.setMaintenanceStatus(new Boolean(result[11]));
				operationPanel.show(analyzer.isEnabled() && !newAnalyzerMode);
			}
		};
		PerifericoUI.getService().getAnalyzerInfo(analyzerId, callback);
	}

	private void getFaultInfo(final boolean forceEditView) {
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] result) {
				// result[0]: analyzer type
				// result[1]: fault enabled/disabled/missing
				if (faultPanel instanceof DigitalFaultPanel)
					((DigitalFaultPanel) faultPanel)
							.setNoFaultView(result[1] == null && !forceEditView);
				faultPanel.setFaultInfo(result);
				faultPanel.setVisible(true);
			}
		};
		if (analyzerId == null)
			throw new IllegalStateException("Analyzer id should not be null");
		PerifericoUI.getService().getAnalyzerFaultInfo(analyzerId, callback);
	}

	private void deleteFaultInfo() {
		if (!(faultPanel instanceof DigitalFaultPanel))
			return;
		if (newFault) {
			((DigitalFaultPanel) faultPanel).setNoFaultView(true);
			newFault = false;
			return;
		}
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String result) {
				if (new Boolean(result)) {
					newFault = false;
					((DigitalFaultPanel) faultPanel).setNoFaultView(true);
				} else {
					Window.alert(PerifericoUI.getMessages().fault_not_deleted());
				}
			}
		};
		PerifericoUI.getService().deleteFault(analyzerId, callback);
	}

	private void verifyFaultAndDataValidFields() {
		final String[] faultInfo = faultPanel.getFaultInfoArray();
		final String[] dataValidInfo = faultPanel instanceof DigitalFaultPanel ? ((DigitalFaultPanel) faultPanel)
				.getDataValidInfoArray() : null;
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String result) {
				if (result != null) {
					Window.alert(result);
				} else {
					if (faultInfo != null)
						newFault = false;
					if (dataValidInfo != null)
						newDataValid = false;
					Utils.sendVerifyOk();
				}
			}
		};
		PerifericoUI.getService().setVerifyAnalyzerFaultAndDataValidFields(
				analyzerId, faultInfo, dataValidInfo, callback);
	}

	private void getDataValidInfo(final boolean forceEditView) {
		if (!(faultPanel instanceof DigitalFaultPanel))
			return;
		final DigitalFaultPanel dfp = (DigitalFaultPanel) faultPanel;
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] result) {
				// result[0]: analyzer type
				// result[1]: data valid enabled/disabled/missing
				dfp.setNoDataValidView(result[1] == null && !forceEditView);
				dfp.setDataValidInfo(result);
				dfp.setVisible(true);
			}
		};
		if (analyzerId == null)
			throw new IllegalStateException("Analyzer id should not be null");
		PerifericoUI.getService()
				.getAnalyzerDataValidInfo(analyzerId, callback);
	}

	private void deleteDataValidInfo() {
		if (!(faultPanel instanceof DigitalFaultPanel))
			return;
		final DigitalFaultPanel dfp = (DigitalFaultPanel) faultPanel;
		if (newDataValid) {
			dfp.setNoDataValidView(true);
			newDataValid = false;
			return;
		}
		AsyncCallback<String> callback = new UIAsyncCallback<String>() {
			public void onSuccess(String result) {
				if (new Boolean(result)) {
					newDataValid = false;
					dfp.setNoDataValidView(true);
				} else {
					Window.alert(PerifericoUI.getMessages().data_valid_not_deleted());
				}
			}
		};
		PerifericoUI.getService().deleteDataValid(analyzerId, callback);
	}

	private void setAnalyzerImpl(String type) {
		if (Utils.SAMPLE.equals(type) || Utils.AVG.equals(type)
				|| Utils.WIND.equals(type)) {
			analyzer = new AnalogAnalyzerPanel(type, analyzerInfoContainer,
					analyzerStatusValues, infoUndoHandler,
					infoSendVerifyHandler);
			faultPanel = new DigitalFaultPanel(faultContainer,
					faultUndoHandler, faultSendVerifyHandler, faultNewHandler,
					faultDeleteHandler, dataValidNewHandler,
					dataValidDeleteHandler);
		} else if (Utils.RAIN.equals(type)) {
			analyzer = new CounterAnalyzerPanel(analyzerInfoContainer,
					analyzerStatusValues, infoUndoHandler,
					infoSendVerifyHandler);
			faultPanel = new DigitalFaultPanel(faultContainer,
					faultUndoHandler, faultSendVerifyHandler, faultNewHandler,
					faultDeleteHandler, dataValidNewHandler,
					dataValidDeleteHandler);
		} else if (Utils.DPA.equals(type)) {
			analyzer = new DpaAnalyzerPanel(analyzerInfoContainer,
					analyzerStatusValues, infoUndoHandler,
					infoSendVerifyHandler);
			faultPanel = new DpaFaultPanel(faultContainer, faultUndoHandler,
					faultSendVerifyHandler);
		} else
			throw new IllegalStateException("Unknown analyzer type: " + type);
	}

	void setAnalyzers(String[][] matrixIdType, int currentIndex) {
		this.matrixIdType = matrixIdType;
		this.currentIndex = currentIndex;
		newAnalyzerMode = false;
		analyzerId = matrixIdType[currentIndex][0];
		setAnalyzerImpl(matrixIdType[currentIndex][1]);
	}

	void setNewAnalyzer(String type) {
		matrixIdType = null;
		currentIndex = null;
		analyzerId = null;
		newAnalyzerMode = true;
		setAnalyzerImpl(type);
	}

	void setNewDpaAnalyzer(String brand, String model) {
		matrixIdType = null;
		currentIndex = null;
		analyzerId = null;
		newAnalyzerMode = true;
		analyzer = new DpaAnalyzerPanel(brand, model, analyzerInfoContainer,
				analyzerStatusValues, infoUndoHandler, infoSendVerifyHandler);
		faultPanel = new DpaFaultPanel(faultContainer, faultUndoHandler,
				faultSendVerifyHandler);
	}

	@Override
	protected void dismissContent(AsyncPageOperation asyncPageOperation) {
		if (newAnalyzerMode) {
			asyncPageOperation.complete();
			return;
		}
		String selectedValue = analyzer.getStatus();
		if (selectedValue.equals("0"))
			Window.alert(PerifericoUI.getMessages().error_select_element());
		else {
			// infoFields:
			// [0]: name
			// [1]: brand
			// [2]: model
			// [3]: description
			// [4]: serial number
			// [5]: user notes
			// [6]: status
			// [7]: uiURL
			// [8]: min voltage for sample/wind/avg analyzer only |
			// _____ portType for dpa analyzer only
			// [9]: max voltage for sample/wind/avg analyzer only |
			// _____ hostName for dpa analyzer only
			// [10]: min range extension for sample/wind/avg analyzer only |
			// _____ ipPort for dpa analyzer only
			// [11]: max range extension for sample/wind/avg analyzer only |
			// _____ ttyDevice for dpa analyzer only
			// [12]: differential mode for sample/wind/avg analyzer only |
			// _____ ttyBaudRate for dpa analyzer only
			// [13]: ttyParams for dpa analyzer only
			// [14]: driverParams for dpa analyzer only
			// faultFields:
			// [0]: fault enabled
			// [1]: fault active high | acquisition period for dpa analyzer only
			// dataValidFields:
			// [0]: dataValid enabled
			// [1]: dataValid active high
			// [2]: dataValid discard data
			PerifericoUI.getService().verifySameAnalyzerConfig(analyzerId,
					analyzer.getInfoArray(), faultPanel.getFaultInfoArray(),
					faultPanel.getDataValidInfoArray(), asyncPageOperation);
		}
	}

	@Override
	protected void reset() {
		boolean nextPrevEnabled = matrixIdType != null
				&& matrixIdType.length > 0;
		prevButton.setEnabled(nextPrevEnabled && currentIndex > 0);
		nextButton.setEnabled(nextPrevEnabled
				&& currentIndex < matrixIdType.length - 1);
	}

	@Override
	protected void loadContent() {
		if (analyzerId == null) {
			getNewAnalyzerDefaultInfo();
			faultPanel.setVisible(false);
			operationPanel.hide();
		} else {
			getAnalyzerInfo();
			getFaultInfo(false);
			getDataValidInfo(false);
		}
	}

}

abstract class FaultPanel {

	protected Grid componentGrid;

	private Label title;

	private VerticalPanel body;

	private PanelButtonWidget panelButtons;

	private Button cancelButton;

	private Button sendVerifyButton;

	FaultPanel(VerticalPanel container, ClickHandler undoHandler,
			ClickHandler sendVerifyHandler, int gridRows, int gridColumns) {
		container.clear();
		title = new Label();
		title.setStyleName("gwt-Label-title");

		cancelButton = PerifericoUI.makeUndoButton(undoHandler);
		sendVerifyButton = PerifericoUI.makeSendVerifyButton(sendVerifyHandler);

		body = new VerticalPanel();
		body.setStyleName("gwt-post-boxed");

		HorizontalPanel panelButtonsWrapper = new HorizontalPanel();
		panelButtonsWrapper.setStyleName("gwt-button-panel");
		panelButtonsWrapper.setWidth("100%");
		panelButtons = new PanelButtonWidget();
		panelButtons.addButton(cancelButton);
		panelButtons.addButton(sendVerifyButton);
		panelButtonsWrapper.add(panelButtons);
		panelButtonsWrapper.setCellHorizontalAlignment(panelButtons,
				HasHorizontalAlignment.ALIGN_CENTER);

		componentGrid = new Grid(gridRows, gridColumns);
		componentGrid.setWidth("980px");
		componentGrid.setCellPadding(2);
		componentGrid.setCellSpacing(2);
		body.add(componentGrid);
		body.add(panelButtonsWrapper);

		container.add(title);
		container.add(body);
	}

	protected void setTitle(String title) {
		this.title.setText(title);
	}

	void setVisible(boolean visible) {
		title.setVisible(visible);
		body.setVisible(visible);
	}

	abstract String[] getFaultInfoArray();

	abstract void setFaultInfo(String[] faultFields);

	abstract String[] getDataValidInfoArray();

	abstract void setDataValidInfo(String[] dataValidFields);

}

class DigitalFaultPanel extends FaultPanel {

	private CheckBox faultEnabled;

	private CheckBox faultActiveHigh;

	private Label faultConnection;

	private Button newFaultButton;

	private Button deleteFaultButton;

	private CheckBox dataValidEnabled;

	private CheckBox dataValidActiveHigh;

	private CheckBox dataValidDiscardData;

	private Label dataValidConnection;

	private Button newDataValidButton;

	private Button deleteDataValidButton;

	private Widget faultWidgets[] = new Widget[3];

	private Widget dataValidWidgets[] = new Widget[4];

	private boolean noFault;

	private boolean noDataValid;

	DigitalFaultPanel(VerticalPanel container, ClickHandler undoHandler,
			ClickHandler sendVerifyHandler, ClickHandler newFaultHandler,
			ClickHandler deleteFaultHandler, ClickHandler newDataValidHandler,
			ClickHandler deleteDataValidHandler) {
		super(container, undoHandler, sendVerifyHandler, 2, 6);
		setTitle(PerifericoUI.getMessages().digital_outputs());
		componentGrid.getCellFormatter().setWidth(0, 0, "20px");
		componentGrid.getCellFormatter().setWidth(0, 1, "100px");
		componentGrid.getCellFormatter().setWidth(0, 2, "100px");
		componentGrid.getCellFormatter().setWidth(0, 3, "100px");
		componentGrid.getCellFormatter().setWidth(0, 4, "100px");
		componentGrid.getCellFormatter().setWidth(0, 5, "380px");

		newFaultButton = new StyleButton("gwt-button-new-very-small",
				PerifericoUI.getMessages().analyzer_button_new_fault(),
				newFaultHandler);
		deleteFaultButton = PerifericoUI
				.makeDeleteSmallButton(deleteFaultHandler);
		componentGrid.setText(0, 1,
				PerifericoUI.getMessages().analyzer_lbl_fault_title());
		faultEnabled = new CheckBox();
		faultEnabled.setValue(true, true);
		faultWidgets[0] = makeLabelledWidget(faultEnabled,
				PerifericoUI.getMessages().lbl_enabled());
		componentGrid.setWidget(0, 2, faultWidgets[0]);
		faultActiveHigh = new CheckBox();
		faultActiveHigh.setName("activehigh");
		faultWidgets[1] = makeLabelledWidget(faultActiveHigh,
				PerifericoUI.getMessages().lbl_active_high());
		componentGrid.setWidget(0, 3, faultWidgets[1]);
		faultConnection = new Label();
		faultWidgets[2] = makeLabelledWidget(faultConnection,
				PerifericoUI.getMessages().analyzer_lbl_connection());
		componentGrid.setWidget(0, 5, faultWidgets[2]);

		newDataValidButton = new StyleButton("gwt-button-new-very-small",
				PerifericoUI.getMessages().new_data_valid(), newDataValidHandler);
		deleteDataValidButton = PerifericoUI
				.makeDeleteSmallButton(deleteDataValidHandler);
		componentGrid.setText(1, 1, PerifericoUI.getMessages().data_valid());
		dataValidEnabled = new CheckBox();
		dataValidEnabled.setValue(true, true);
		dataValidWidgets[0] = makeLabelledWidget(dataValidEnabled,
				PerifericoUI.getMessages().lbl_enabled());
		componentGrid.setWidget(1, 2, dataValidWidgets[0]);
		dataValidActiveHigh = new CheckBox();
		dataValidActiveHigh.setName("activehigh");
		dataValidWidgets[1] = makeLabelledWidget(dataValidActiveHigh,
				PerifericoUI.getMessages().lbl_active_high());
		componentGrid.setWidget(1, 3, dataValidWidgets[1]);
		dataValidDiscardData = new CheckBox();
		dataValidDiscardData.setName("discarddata");
		dataValidWidgets[2] = makeLabelledWidget(dataValidDiscardData,
				PerifericoUI.getMessages().lbl_discard_data());
		componentGrid.setWidget(1, 4, dataValidWidgets[2]);
		dataValidConnection = new Label();
		dataValidWidgets[3] = makeLabelledWidget(dataValidConnection,
				PerifericoUI.getMessages().analyzer_lbl_connection());
		componentGrid.setWidget(1, 5, dataValidWidgets[3]);

		for (int i = 0; i < faultWidgets.length; i++)
			faultWidgets[i].setVisible(false);
		for (int i = 0; i < dataValidWidgets.length; i++)
			dataValidWidgets[i].setVisible(false);
	}

	private Widget makeLabelledWidget(Widget widget, String label) {
		HorizontalPanel panel = new HorizontalPanel();
		Label padMin = new Label();
		padMin.setWidth("10px");
		panel.add(new Label(label));
		panel.add(padMin);
		panel.add(widget);
		return panel;
	}

	void setNoFaultView(boolean value) {
		noFault = value;
		for (int i = 0; i < faultWidgets.length; i++)
			faultWidgets[i].setVisible(!value);
		componentGrid.setWidget(0, 0, value ? newFaultButton
				: deleteFaultButton);
	}

	void setNoDataValidView(boolean value) {
		noDataValid = value;
		for (int i = 0; i < dataValidWidgets.length; i++)
			dataValidWidgets[i].setVisible(!value);
		componentGrid.setWidget(1, 0, value ? newDataValidButton
				: deleteDataValidButton);
	}

	@Override
	String[] getFaultInfoArray() {
		if (noFault)
			return null;
		String[] faultInfo = new String[2];
		faultInfo[0] = Boolean.toString(faultEnabled.getValue());
		faultInfo[1] = Boolean.toString(faultActiveHigh.getValue());
		return faultInfo;
	}

	@Override
	void setFaultInfo(String[] fields) {
		// faultFields:
		// [0]: type
		// [1]: enabled/disabled/missing
		// [2]: description NOT USED
		// [3]: activeHigh
		// [4]: bind label
		faultEnabled.setValue(
				fields[1] == null ? true : new Boolean(fields[1]), true);
		faultActiveHigh.setValue(new Boolean(fields[3]));
		faultConnection.setText(fields[4] != null ? fields[4]
				: PerifericoUI.getMessages().analyzer_not_connected());
	}

	@Override
	String[] getDataValidInfoArray() {
		if (noDataValid)
			return null;
		String[] dataValidInfo = new String[2];
		dataValidInfo[0] = Boolean.toString(dataValidEnabled.getValue());
		dataValidInfo[1] = Boolean.toString(dataValidActiveHigh.getValue());
		dataValidInfo[2] = Boolean.toString(dataValidDiscardData.getValue());
		return dataValidInfo;
	}

	@Override
	void setDataValidInfo(String[] fields) {
		// faultFields:
		// [0]: type
		// [1]: enabled/disabled/missing
		// [2]: description NOT USED
		// [3]: activeHigh
		// [4]: discardData
		// [5]: bind label
		dataValidEnabled.setValue(fields[1] == null ? true : new Boolean(
				fields[1]), true);
		dataValidActiveHigh.setValue(new Boolean(fields[3]));
		dataValidDiscardData.setValue(new Boolean(fields[4]));
		dataValidConnection.setText(fields[5] != null ? fields[5]
				: PerifericoUI.getMessages().analyzer_not_connected());
	}

}

class DpaFaultPanel extends FaultPanel {

	private CheckBox faultEnabled;

	private TextBox textAcqPeriod;

	DpaFaultPanel(VerticalPanel container, ClickHandler undoHandler,
			ClickHandler sendVerifyHandler) {
		super(container, undoHandler, sendVerifyHandler, 1, 6);
		setTitle(PerifericoUI.getMessages().analyzer_lbl_fault_title());
		componentGrid.getCellFormatter().setWidth(0, 0, "120px");
		componentGrid.getCellFormatter().setWidth(0, 1, "80px");
		componentGrid.getCellFormatter().setWidth(0, 2, "140px");
		componentGrid.getCellFormatter().setWidth(0, 3, "60px");
		componentGrid.getCellFormatter().setWidth(0, 4, "150px");
		componentGrid.getCellFormatter().setWidth(0, 5, "250px");
		faultEnabled = new CheckBox();
		faultEnabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				faultEnabled.setTitle(event.getValue() ? PerifericoUI.getMessages()
						.button_enabled() : PerifericoUI.getMessages()
						.button_disabled());
			}
		});
		faultEnabled.setValue(true, true);
		componentGrid.setText(0, 0, PerifericoUI.getMessages().lbl_enabled());
		componentGrid.setWidget(0, 1, faultEnabled);
		textAcqPeriod = new TextBox();
		textAcqPeriod.setWidth("50px");
		textAcqPeriod.setStyleName("gwt-bg-text-orange");
		textAcqPeriod.addKeyPressHandler(new NumericKeyPressHandler(true));
		componentGrid.setText(0, 2, PerifericoUI.getMessages().lbl_acqPeriod());
		componentGrid.setWidget(0, 3, textAcqPeriod);
	}

	@Override
	String[] getFaultInfoArray() {
		String[] faultInfo = new String[2];
		faultInfo[0] = Boolean.toString(faultEnabled.getValue());
		faultInfo[1] = textAcqPeriod.getText();
		return faultInfo;
	}

	@Override
	void setFaultInfo(String[] fields) {
		// faultFields:
		// [0]: type
		// [1]: enabled/disabled
		// [2]: acqPeriod
		faultEnabled.setValue(
				fields[1] == null ? true : new Boolean(fields[1]), true);
		textAcqPeriod.setText(fields[2]);
	}

	@Override
	String[] getDataValidInfoArray() {
		return null;
	}

	@Override
	void setDataValidInfo(String[] fields) {
	}

}

abstract class AnalyzerPanel {

	private static final String ENABLED = "ENABLED";

	protected FlexTable componentGrid = new FlexTable();

	private TextBox textAnalyzerName = new TextBox();

	private TextBox textAnalyzerBrand = new TextBox();

	private TextBox textAnalyzerModel = new TextBox();

	private TextBox textAnalyzerDescription = new TextBox();

	private TextBox textAnalyzerSerialNumber = new TextBox();

	private TextArea textAnalyzerUserNotes = new TextArea();

	private ListBox listBoxAnalyzerStatus = new ListBox();
	
	private TextBox textAnalyzerUiURL = new TextBox();

	AnalyzerPanel(VerticalPanel container, String[][] statusValues,
			ClickHandler undoHandler, ClickHandler sendVerifyHandler) {
		container.clear();
		Label title = new Label();
		title.setText(PerifericoUI.getMessages().lbl_analyzer_title());
		title.setStyleName("gwt-Label-title");

		VerticalPanel body = new VerticalPanel();
		body.setStyleName("gwt-post-boxed");
		body.add(componentGrid);

		PanelButtonWidget panelButton = PerifericoUI.makeUndoSendPanelButton(
				undoHandler, sendVerifyHandler);

		HorizontalPanel panelButtonWrapper = new HorizontalPanel();
		panelButtonWrapper.setStyleName("gwt-button-panel");
		panelButtonWrapper.add(panelButton);
		panelButtonWrapper.setWidth("100%");
		panelButtonWrapper.setCellHorizontalAlignment(panelButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		panelButtonWrapper.setVisible(true);
		body.add(panelButtonWrapper);

		container.add(title);
		container.add(body);

		// format the grid for analyzer info
		componentGrid.getCellFormatter().setWidth(0, 0, "150px");
		componentGrid.getCellFormatter().setWidth(0, 1, "320px");
		componentGrid.getCellFormatter().setWidth(0, 2, "150px");
		componentGrid.getCellFormatter().setWidth(0, 3, "320px");
		componentGrid.setCellPadding(2);
		componentGrid.setCellSpacing(2);

		// Put components into analyzer grid cells.
		String message = PerifericoUI.getMessages().lbl_analyzer_name() + ":";
		componentGrid.setText(0, 0, message);
		textAnalyzerName.setWidth("270px");
		textAnalyzerName.setStyleName("gwt-bg-text-orange");
		textAnalyzerName.setMaxLength(16);
		textAnalyzerName.setName("analyzername");
		componentGrid.setWidget(0, 1, textAnalyzerName);

		message = PerifericoUI.getMessages().lbl_brand() + ":";
		componentGrid.setText(1, 0, message);
		textAnalyzerBrand.setWidth("270px");
		textAnalyzerBrand.setStyleName("gwt-bg-text-orange");
		textAnalyzerBrand.setName("analyzerbrand");
		componentGrid.setWidget(1, 1, textAnalyzerBrand);

		message = PerifericoUI.getMessages().lbl_model() + ":";
		componentGrid.setText(2, 0, message);
		textAnalyzerModel.setWidth("270px");
		textAnalyzerModel.setStyleName("gwt-bg-text-orange");
		textAnalyzerModel.setName("analyzermodel");
		componentGrid.setWidget(2, 1, textAnalyzerModel);

		message = PerifericoUI.getMessages().lbl_analyzer_description();
		componentGrid.setText(3, 0, message);
		textAnalyzerDescription.setWidth("270px");
		textAnalyzerDescription.setStyleName("gwt-bg-text-orange");
		textAnalyzerDescription.setName("analyzerdescription");
		componentGrid.setWidget(3, 1, textAnalyzerDescription);

		message = PerifericoUI.getMessages().lbl_analyzer_serialNumber();
		componentGrid.setText(4, 0, message);
		textAnalyzerSerialNumber.setWidth("270px");
		textAnalyzerSerialNumber.setStyleName("gwt-bg-text-orange");
		textAnalyzerSerialNumber.setName("analyzerserialnumber");
		componentGrid.setWidget(4, 1, textAnalyzerSerialNumber);

		message = PerifericoUI.getMessages().lbl_status() + ":";
		componentGrid.setText(5, 0, message);
		listBoxAnalyzerStatus.setWidth("270px");
		listBoxAnalyzerStatus.setStyleName("gwt-bg-text-orange");
		listBoxAnalyzerStatus.setVisibleItemCount(1);
		listBoxAnalyzerStatus.setName("analyzerstatus");
		componentGrid.setWidget(5, 1, listBoxAnalyzerStatus);

		message = PerifericoUI.getMessages().lbl_analyzer_uiURL();
		componentGrid.setText(6, 0, message);
		textAnalyzerUiURL.setWidth("270px");
		textAnalyzerUiURL.setStyleName("gwt-bg-text-orange");
		textAnalyzerUiURL.setName("analyzeruiurl");
		componentGrid.setWidget(6, 1, textAnalyzerUiURL);

		message = PerifericoUI.getMessages().lbl_analyzer_userNotes();
		componentGrid.setText(0, 2, message);
		textAnalyzerUserNotes.setStyleName("gwt-bg-text-orange");
		componentGrid.setWidget(0, 3, textAnalyzerUserNotes);
		componentGrid.getFlexCellFormatter().setRowSpan(0, 3, 3);
		componentGrid.getFlexCellFormatter().setAlignment(0, 3,
				HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_TOP);
		textAnalyzerUserNotes.setWidth("300px");
		textAnalyzerUserNotes.setHeight("60px");
		textAnalyzerUserNotes.setName("analyzerusernotes");

		listBoxAnalyzerStatus
				.addItem(PerifericoUI.getMessages().select_analyzer_status(),
						new String("0"));
		// WARNING: the last element of analyzer status values is
		// 'DELETED' and it must not be inserted in this list
		for (int i = 0; i < statusValues.length - 1; i++)
			listBoxAnalyzerStatus.addItem(statusValues[i][1],
					statusValues[i][0]);
		listBoxAnalyzerStatus.setSelectedIndex(0);
	}

	protected void disableBrandModel() {
		textAnalyzerBrand.setEnabled(false);
		textAnalyzerModel.setEnabled(false);
	}

	protected void setBrand(String brand) {
		textAnalyzerBrand.setText(brand);
	}

	protected void setModel(String model) {
		textAnalyzerModel.setText(model);
	}

	protected void loadCommonInfo(String[] analyzerInfo) {
		analyzerInfo[0] = textAnalyzerName.getText();
		analyzerInfo[1] = getBrand();
		analyzerInfo[2] = getModel();
		analyzerInfo[3] = textAnalyzerDescription.getText();
		analyzerInfo[4] = textAnalyzerSerialNumber.getText();
		analyzerInfo[5] = textAnalyzerUserNotes.getText();
		analyzerInfo[6] = getStatus();
		analyzerInfo[7] = textAnalyzerUiURL.getText();
	}

	protected void setAnalyzerFields(String name, String brand, String model,
			String description, String serialNumber, String status, String uiURL,
			String userNotes) {
		textAnalyzerName.setText(name);
		setBrand(brand);
		setModel(model);
		textAnalyzerDescription.setText(description);
		textAnalyzerSerialNumber.setText(serialNumber);
		textAnalyzerUserNotes.setText(userNotes);
		int selectedIndex = 0;
		for (int i = 0; i < listBoxAnalyzerStatus.getItemCount(); i++)
			if (listBoxAnalyzerStatus.getValue(i).equals(status))
				selectedIndex = i;
		listBoxAnalyzerStatus.setSelectedIndex(selectedIndex);
		textAnalyzerUiURL.setText(uiURL);
	}

	String getBrand() {
		return textAnalyzerBrand.getText();
	}

	String getModel() {
		return textAnalyzerModel.getText();
	}

	boolean isEnabled() {
		return ENABLED.equals(getStatus());
	}

	String getStatus() {
		int indexSelected = listBoxAnalyzerStatus.getSelectedIndex();
		String status = listBoxAnalyzerStatus.getValue(indexSelected);
		return "0".equals(status) ? null : status;
	}
	
	abstract String getType();

	abstract String[] getInfoArray();

	abstract void setDefaultInfo(String[] infoArray);

	abstract void setInfo(String[] infoArray);

}

class AnalogAnalyzerPanel extends AnalyzerPanel {

	private TextBox textMinVoltage = new TextBox();

	private TextBox textMaxVoltage = new TextBox();

	private CheckBox checkBoxMinVoltageExtension = new CheckBox();

	private CheckBox checkBoxMaxVoltageExtension = new CheckBox();

	private CheckBox checkBoxDifferential = new CheckBox();

	private String type;

	AnalogAnalyzerPanel(String type, VerticalPanel container,
			String[][] statusValues, ClickHandler undoHandler,
			ClickHandler sendVerifyHandler) {
		super(container, statusValues, undoHandler, sendVerifyHandler);
		this.type = type;

		componentGrid.setText(3, 2, PerifericoUI.getMessages().element_minVoltage());
		textMinVoltage.setWidth("50px");
		textMinVoltage.setStyleName("gwt-bg-text-orange");
		textMinVoltage.setName("minvoltage");
		textMinVoltage.addKeyPressHandler(new NumericKeyPressHandler(false));
		checkBoxMinVoltageExtension.setText(" "
				+ PerifericoUI.getMessages().analyzer_min_voltage_extension());
		HorizontalPanel hpMin = new HorizontalPanel();
		Label padMin = new Label();
		padMin.setWidth("10px");
		hpMin.add(textMinVoltage);
		hpMin.add(padMin);
		hpMin.add(checkBoxMinVoltageExtension);
		componentGrid.setWidget(3, 3, hpMin);

		componentGrid.setText(4, 2, PerifericoUI.getMessages().element_maxVoltage());
		textMaxVoltage.setWidth("50px");
		textMaxVoltage.setStyleName("gwt-bg-text-orange");
		textMaxVoltage.setName("maxvoltage");
		textMaxVoltage.addKeyPressHandler(new NumericKeyPressHandler(false));
		checkBoxMaxVoltageExtension.setText(" "
				+ PerifericoUI.getMessages().analyzer_max_voltage_extension());
		HorizontalPanel hpMax = new HorizontalPanel();
		Label padMax = new Label();
		padMax.setWidth("10px");
		hpMax.add(textMaxVoltage);
		hpMax.add(padMax);
		hpMax.add(checkBoxMaxVoltageExtension);
		componentGrid.setWidget(4, 3, hpMax);

		componentGrid.setText(5, 2,
				PerifericoUI.getMessages().analyzer_differential());
		componentGrid.setWidget(5, 3, checkBoxDifferential);
	}

	@Override
	String getType() {
		return type;
	}

	@Override
	String[] getInfoArray() {
		String[] analyzerInfo = new String[13];
		loadCommonInfo(analyzerInfo);
		analyzerInfo[8] = textMinVoltage.getText();
		analyzerInfo[9] = textMaxVoltage.getText();
		analyzerInfo[10] = checkBoxMinVoltageExtension.getValue().toString();
		analyzerInfo[11] = checkBoxMaxVoltageExtension.getValue().toString();
		analyzerInfo[12] = checkBoxDifferential.getValue().toString();
		return analyzerInfo;
	}

	@Override
	void setDefaultInfo(String[] infoArray) {
		// result[0] : id
		// result[1] : min voltage extension
		// result[2] : max voltage extension
		// result[3] : differential mode
		if (infoArray.length != 4)
			throw new IllegalStateException("Unexpected reply length: "
					+ infoArray.length);
		setAnalyzerFields("", "", "", "", "", "", "", "", "", "", infoArray[1],
				infoArray[2], infoArray[3]);
	}

	@Override
	void setInfo(String[] infoArray) {
		// result[ 0]: id
		// result[ 1]: name
		// result[ 2]: brand
		// result[ 3]: model
		// result[ 4]: description
		// result[ 5]: serial number
		// result[ 6]: status
		// result[ 7]: uiURL
		// result[ 8]: user notes
		// result[ 9]: type name
		// result[10]: manualCalibrationRunning
		// result[11]: maintenanceInProgress
		// result[12]: min voltage
		// result[13]: max voltage
		// result[14]: min voltage extension
		// result[15]: max voltage extension
		// result[16]: differential mode
		if (infoArray.length != 17)
			throw new IllegalStateException("Unexpected reply length: "
					+ infoArray.length);
		setAnalyzerFields(infoArray[1], infoArray[2], infoArray[3],
				infoArray[4], infoArray[5], infoArray[6], infoArray[7],
				infoArray[8], infoArray[12], infoArray[13], infoArray[14],
				infoArray[15], infoArray[16]);
		if (!getType().equals(infoArray[9]))
			throw new IllegalStateException("Analyzer type mismatch: "
					+ "expected " + getType() + " got " + infoArray[9]);
	}

	private void setAnalyzerFields(String name, String brand, String model,
			String description, String serialNumber, String status,
			String uiURL, String userNotes, String minVoltage,
			String maxVoltage, String minVoltageExtension,
			String maxVoltageExtension, String differentialMode) {
		super.setAnalyzerFields(name, brand, model, description, serialNumber,
				status, uiURL, userNotes);
		textMinVoltage.setText(minVoltage);
		textMaxVoltage.setText(maxVoltage);
		checkBoxMinVoltageExtension.setValue(new Boolean(minVoltageExtension));
		checkBoxMaxVoltageExtension.setValue(new Boolean(maxVoltageExtension));
		checkBoxDifferential.setValue(new Boolean(differentialMode));
	}

}

class CounterAnalyzerPanel extends AnalyzerPanel {

	CounterAnalyzerPanel(VerticalPanel container, String[][] statusValues,
			ClickHandler undoHandler, ClickHandler sendVerifyHandler) {
		super(container, statusValues, undoHandler, sendVerifyHandler);
	}

	@Override
	String getType() {
		return Utils.RAIN;
	}

	@Override
	String[] getInfoArray() {
		String[] analyzerInfo = new String[8];
		loadCommonInfo(analyzerInfo);
		return analyzerInfo;
	}

	@Override
	void setDefaultInfo(String[] infoArray) {
		// result[0] : id
		// result[1] : unused
		// result[2] : unused
		// result[3] : unused
		if (infoArray.length != 4)
			throw new IllegalStateException("Unexpected reply length: "
					+ infoArray.length);
		setAnalyzerFields("", "", "", "", "", "", "", "");
	}

	@Override
	void setInfo(String[] infoArray) {
		// result[ 0]: id
		// result[ 1]: name
		// result[ 2]: brand
		// result[ 3]: model
		// result[ 4]: description
		// result[ 5]: serial number
		// result[ 6]: status
		// result[ 7]: uiUrl
		// result[ 8]: user notes
		// result[ 9]: type name
		// result[10]: manualCalibrationRunning
		// result[11]: maintenanceInProgress
		if (infoArray.length != 12)
			throw new IllegalStateException("Unexpected reply length: "
					+ infoArray.length);
		setAnalyzerFields(infoArray[1], infoArray[2], infoArray[3],
				infoArray[4], infoArray[5], infoArray[6], infoArray[7],
				infoArray[8]);
		if (!getType().equals(infoArray[9]))
			throw new IllegalStateException("Analyzer type mismatch: "
					+ "expected " + getType() + " got " + infoArray[9]);
	}

}

class DpaAnalyzerPanel extends AnalyzerPanel {

	private ListBox brandList = new ListBox();

	private ListBox modelList = new ListBox();

	private ListBox portTypeList = new ListBox();

	private TextBox textHostName = new TextBox();

	private TextBox textIpPort = new TextBox();

	private TextBox textTtyDevice = new TextBox();

	private TextBox textTtyParams = new TextBox();

	private TextBox textTtyBaudRate = new TextBox();

	private TextBox textDriverParams = new TextBox();

	DpaAnalyzerPanel(String brand, String model, VerticalPanel container,
			String[][] statusValues, ClickHandler undoHandler,
			ClickHandler sendVerifyHandler) {
		this(container, statusValues, undoHandler, sendVerifyHandler);
		setBrand(brand);
		setModel(model);
		updateBrandList(brand, model);
		updateModelList(null, brand, model);
	}

	DpaAnalyzerPanel(VerticalPanel container, String[][] statusValues,
			ClickHandler undoHandler, ClickHandler sendVerifyHandler) {
		super(container, statusValues, undoHandler, sendVerifyHandler);
		disableBrandModel();
		brandList.setWidth("270px");
		brandList.setStyleName("gwt-bg-text-orange");
		brandList.setVisibleItemCount(1);
		brandList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String newBrand = brandList.getValue(brandList
						.getSelectedIndex());
				updateModelList(newBrand, getBrand(), getModel());
				setBrand(newBrand);
			}
		});
		componentGrid.setWidget(1, 1, brandList);
		modelList.setWidth("270px");
		modelList.setStyleName("gwt-bg-text-orange");
		modelList.setVisibleItemCount(1);
		modelList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String newModel = modelList.getValue(modelList
						.getSelectedIndex());
				setModel(newModel);
			}
		});
		componentGrid.setWidget(2, 1, modelList);
		textDriverParams.setWidth("300px");
		textDriverParams.setStyleName("gwt-bg-text-orange");
		componentGrid.setText(3, 2, PerifericoUI.getMessages().lbl_driver_params());
		componentGrid.setWidget(3, 3, textDriverParams);
		componentGrid.setText(4, 2,
				PerifericoUI.getMessages().lbl_analyzer_network());
		portTypeList.setWidth("300px");
		portTypeList.setStyleName("gwt-bg-text-orange");
		portTypeList.setVisibleItemCount(1);
		portTypeList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String selectedValue = portTypeList.getValue(portTypeList
						.getSelectedIndex());
				if (selectedValue.equals(Utils.NETWORK)) {
					makeGrid_Network();
				} else if (selectedValue.equals(Utils.SERIAL)) {
					makeGrid_Serial();
				}
			}
		});
		componentGrid.setText(4, 2,
				PerifericoUI.getMessages().lbl_analyzer_network());
		componentGrid.setWidget(4, 3, portTypeList);
	}

	private void makeGrid_Network() {
		textHostName.setWidth("300px");
		textHostName.setStyleName("gwt-bg-text-orange");
		textIpPort.setWidth("75px");
		textIpPort.setStyleName("gwt-bg-text-orange");
		textIpPort.addKeyPressHandler(new NumericKeyPressHandler(true));
		componentGrid.setText(5, 2,
				PerifericoUI.getMessages().lbl_analyzer_host_name());
		componentGrid.setWidget(5, 3, textHostName);
		componentGrid.setText(6, 2,
				PerifericoUI.getMessages().lbl_analyzer_ip_port());
		componentGrid.setWidget(6, 3, textIpPort);

	}

	private void makeGrid_Serial() {
		textTtyDevice.setWidth("300px");
		textTtyDevice.setStyleName("gwt-bg-text-orange");
		textTtyBaudRate.setWidth("75px");
		textTtyBaudRate.setStyleName("gwt-bg-text-orange");
		textTtyBaudRate.addKeyPressHandler(new NumericKeyPressHandler(true));
		textTtyParams.setWidth("75px");
		textTtyParams.setStyleName("gwt-bg-text-orange");
		componentGrid.setText(5, 2,
				PerifericoUI.getMessages().lbl_analyzer_tty_device());
		componentGrid.setWidget(5, 3, textTtyDevice);
		componentGrid.setText(6, 2,
				PerifericoUI.getMessages().lbl_analyzer_tty_params());
		FlexTable innerGrid = new FlexTable();
		innerGrid.setCellPadding(0);
		innerGrid.setCellSpacing(0);
		innerGrid.getCellFormatter().setWidth(0, 0, "90px");
		innerGrid.getCellFormatter().setWidth(0, 1, "40px");
		innerGrid.getCellFormatter().setWidth(0, 2, "95px");
		innerGrid.getCellFormatter().setWidth(0, 3, "90px");
		innerGrid.setWidget(0, 0, textTtyParams);
		innerGrid.setText(0, 1, "");
		innerGrid.setText(0, 2,
				PerifericoUI.getMessages().lbl_analyzer_baud_rate());
		innerGrid.setWidget(0, 3, textTtyBaudRate);
		componentGrid.setWidget(6, 3, innerGrid);
	}

	private String getPortType() {
		int indexSelected = portTypeList.getSelectedIndex();
		return portTypeList.getValue(indexSelected);
	}

	@Override
	String getType() {
		return Utils.DPA;
	}

	@Override
	String[] getInfoArray() {
		String[] analyzerInfo = new String[15];
		loadCommonInfo(analyzerInfo);
		analyzerInfo[8] = getPortType();
		analyzerInfo[9] = textHostName.getText();
		analyzerInfo[10] = textIpPort.getText();
		analyzerInfo[11] = textTtyDevice.getText();
		analyzerInfo[12] = textTtyBaudRate.getText();
		analyzerInfo[13] = textTtyParams.getText();
		analyzerInfo[14] = textDriverParams.getText();
		return analyzerInfo;
	}

	@Override
	void setDefaultInfo(String[] infoArray) {
		// infoArray[0] : id
		// infoArray[1] : brand
		// infoArray[2] : model
		// infoArray[3] : portType
		// infoArray[4] : defaultIpPort
		// infoArray[5] : defaultTtyBaudRate
		// infoArray[6] : defaultTtyParams
		// infoArray[7] : hasNetworkPortOnly
		// infoArray[8] : driverParams
		if (infoArray.length != 9)
			throw new IllegalStateException("Unexpected reply length: "
					+ infoArray.length);
		setAnalyzerFields("", infoArray[1], infoArray[2], "", "", "", "", "",
				infoArray[3], infoArray[7], "", infoArray[4], "", infoArray[5],
				infoArray[6], infoArray[8]);
	}

	@Override
	void setInfo(String[] infoArray) {
		// infoArray[ 0]: id
		// infoArray[ 1]: name
		// infoArray[ 2]: brand
		// infoArray[ 3]: model
		// infoArray[ 4]: description
		// infoArray[ 5]: serial number
		// infoArray[ 6]: status
		// infoArray[ 7]: uiURL
		// infoArray[ 8]: user notes
		// infoArray[ 9]: type name
		// infoArray[10]: manualCalibrationRunning
		// infoArray[11]: maintenanceInProgress
		// infoArray[12]: portType
		// infoArray[13]: hostName
		// infoArray[14]: IP port
		// infoArray[15]: ttyDevice
		// infoArray[16]: ttyBaudRate
		// infoArray[17]: ttyParams
		// infoArray[18]: hasNetworkPortOnly
		// infoArray[19] : driverParams
		if (infoArray.length != 20)
			throw new IllegalStateException("Unexpected reply length: "
					+ infoArray.length);
		setAnalyzerFields(infoArray[1], infoArray[2], infoArray[3],
				infoArray[4], infoArray[5], infoArray[6], infoArray[7], infoArray[8],
				infoArray[12], infoArray[18], infoArray[13], infoArray[14],
				infoArray[15], infoArray[16], infoArray[17], infoArray[19]);
		if (!getType().equals(infoArray[9]))
			throw new IllegalStateException("Analyzer type mismatch: "
					+ "expected " + getType() + " got " + infoArray[9]);
	}

	private void setAnalyzerFields(String name, String brand, String model,
			String description, String serialNumber, String status, String uiURL,
			String userNotes, String portType, String networkOnly,
			String hostName, String ipPort, String ttyDevice,
			String ttyBaudRate, String ttyParams, String driverParams) {
		super.setAnalyzerFields(name, brand, model, description, serialNumber,
				status, uiURL, userNotes);
		updateBrandList(brand, model);
		updateModelList(null, brand, model);
		textHostName.setText(hostName);
		textIpPort.setText(ipPort);
		textTtyBaudRate.setText(ttyBaudRate);
		textTtyDevice.setText(ttyDevice);
		textTtyParams.setText(ttyParams);
		textDriverParams.setText(driverParams);
		portTypeList.clear();
		if (Utils.NETWORK.equals(portType)) {
			portTypeList.addItem(PerifericoUI.getMessages().interface_network(),
					Utils.NETWORK);
			if (!new Boolean(networkOnly))
				portTypeList.addItem(PerifericoUI.getMessages().interface_serial(),
						Utils.SERIAL);
			portTypeList.setSelectedIndex(0);
			makeGrid_Network();
		} else if (Utils.SERIAL.equals(portType)) {
			portTypeList.addItem(PerifericoUI.getMessages().interface_network(),
					Utils.NETWORK);
			portTypeList.addItem(PerifericoUI.getMessages().interface_serial(),
					Utils.SERIAL);
			portTypeList.setSelectedIndex(1);
			makeGrid_Serial();
		}
	}

	private void updateBrandList(final String brand, final String model) {
		AsyncCallback<List<String>> callback = new UIAsyncCallback<List<String>>() {
			public void onSuccess(List<String> brands) {
				brandList.clear();
				for (String item : brands) {
					brandList.addItem(item);
					if (item.equals(brand))
						brandList
								.setSelectedIndex(brandList.getItemCount() - 1);
				}
			}
		};
		brandList.clear();
		PerifericoUI.getService().getEquivalentBrands(brand, model, callback);
	}

	private void updateModelList(final String newBrand, final String brand,
			final String model) {
		AsyncCallback<List<String>> callback = new UIAsyncCallback<List<String>>() {
			public void onSuccess(List<String> models) {
				modelList.clear();
				int selectedIndex = 0;
				for (String item : models) {
					modelList.addItem(item);
					if (item.equals(model))
						selectedIndex = modelList.getItemCount() - 1;
				}
				modelList.setSelectedIndex(selectedIndex);
				setModel(modelList.getValue(selectedIndex));
			}
		};
		modelList.clear();
		PerifericoUI.getService().getEquivalentModels(newBrand, brand, model,
				callback);
	}

}

class OperationPanel {

	private Label title;

	private Button listElementButton;

	private VerticalPanel body;

	private HorizontalPanel calibPanel;

	private HorizontalPanel maintenancePanel;

	private Label lblCalibStatus;

	private Label lblMaintenanceStatus;

	private Label lblNoCalibration;

	private IconImageBundle ledIcon;

	OperationPanel(VerticalPanel container, ClickHandler listElementHandler,
			ClickHandler calibStartHandler, ClickHandler calibStopHandler,
			ClickHandler maintenanceStartHandler,
			ClickHandler maintenanceStopHandler) {
		container.clear();
		// Label and panel for operation
		title = new Label();
		title.setText(PerifericoUI.getMessages().lbl_elements_operation_title());
		title.setStyleName("gwt-Label-title");

		// panel that contains the grid for operation
		body = new VerticalPanel();
		body.setStyleName("gwt-post-boxed");

		FlexTable operationTable = new FlexTable();
		// format the grid for operation
		operationTable.getCellFormatter().setWidth(0, 0, "250px");
		operationTable.getCellFormatter().setWidth(0, 1, "100px");
		operationTable.getCellFormatter().setWidth(0, 2, "250px");
		operationTable.getCellFormatter().setWidth(0, 3, "100px");
		operationTable.getCellFormatter().setWidth(0, 4, "250px");
		operationTable.setCellPadding(2);
		operationTable.setCellSpacing(2);

		Label lblElement = new Label();
		lblElement.setText(PerifericoUI.getMessages()
				.lbl_analyzer_element_operation());
		lblElement.setStyleName("gwt-analyzer-title-orange");
		operationTable.setWidget(0, 0, lblElement);
		operationTable.getCellFormatter().setAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_MIDDLE);

		// button only for sample/avg analyzer
		listElementButton = new Button();
		listElementButton.setTitle(PerifericoUI.getMessages().button_list_element());
		listElementButton.setStyleName("gwt-button-list-element");
		listElementButton.addClickHandler(listElementHandler);
		operationTable.setWidget(1, 0, listElementButton);
		operationTable.getCellFormatter().setAlignment(1, 0,
				HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_MIDDLE);

		body.add(operationTable);

		lblNoCalibration = new Label();
		lblNoCalibration.setText(PerifericoUI.getMessages().no_calibration());
		operationTable.setWidget(2, 0, lblNoCalibration);
		operationTable.getFlexCellFormatter().setColSpan(2, 0, 5);

		lblCalibStatus = new Label();
		lblCalibStatus.setStyleName("gwt-analyzer-title-orange");
		lblCalibStatus.setText(PerifericoUI.getMessages()
				.lbl_analyzer_calibration_status());
		operationTable.setWidget(0, 2, lblCalibStatus);
		operationTable.getCellFormatter().setAlignment(0, 1,
				HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_MIDDLE);

		lblMaintenanceStatus = new Label();
		lblMaintenanceStatus.setStyleName("gwt-analyzer-title-orange");
		lblMaintenanceStatus.setText(PerifericoUI.getMessages()
				.lbl_analyzer_maintenace_status());
		operationTable.setWidget(0, 4, lblMaintenanceStatus);
		operationTable.getCellFormatter().setAlignment(0, 2,
				HasHorizontalAlignment.ALIGN_LEFT,
				HasVerticalAlignment.ALIGN_MIDDLE);
		calibPanel = new HorizontalPanel();
		calibPanel.setWidth("100%");
		calibPanel.setSpacing(5);
		calibPanel.setStyleName("gwt-Grid");
		operationTable.setWidget(1, 2, calibPanel);

		// set icon for calibration and maintenance
		ledIcon = (IconImageBundle) GWT.create(IconImageBundle.class);
		Image ledGray = new Image();
		ledGray.setResource(ledIcon.ledGray());
		calibPanel.add(ledGray);
		calibPanel.getWidget(0).setTitle(
				PerifericoUI.getMessages().calib_not_in_progress());
		calibPanel.setCellVerticalAlignment(calibPanel.getWidget(0),
				HasVerticalAlignment.ALIGN_MIDDLE);

		Button buttonActiveCalibration = new Button();
		buttonActiveCalibration.setStyleName("gwt-button-start");
		buttonActiveCalibration.setTitle(PerifericoUI.getMessages()
				.active_calibration());
		buttonActiveCalibration.addClickHandler(calibStartHandler);
		calibPanel.add(buttonActiveCalibration);

		Button buttonDisactiveCalibration = new Button();
		buttonDisactiveCalibration.setStyleName("gwt-button-stop");
		buttonDisactiveCalibration.setTitle(PerifericoUI.getMessages()
				.disactive_calibration());
		buttonDisactiveCalibration.addClickHandler(calibStopHandler);
		calibPanel.add(buttonDisactiveCalibration);

		maintenancePanel = new HorizontalPanel();
		maintenancePanel.setWidth("100%");
		maintenancePanel.setSpacing(5);
		maintenancePanel.setStyleName("gwt-Grid");
		operationTable.setWidget(1, 4, maintenancePanel);

		Image ledGray2 = new Image();
		ledGray2.setResource(ledIcon.ledGray());
		maintenancePanel.add(ledGray2);
		maintenancePanel.getWidget(0).setTitle(
				PerifericoUI.getMessages().maintenance_not_in_progress());

		maintenancePanel.setCellVerticalAlignment(
				maintenancePanel.getWidget(0),
				HasVerticalAlignment.ALIGN_MIDDLE);

		Button buttonActiveCalibration2 = new Button();
		buttonActiveCalibration2.setStyleName("gwt-button-start");
		buttonActiveCalibration2.setTitle(PerifericoUI.getMessages()
				.active_maintenance());
		buttonActiveCalibration2.addClickHandler(maintenanceStartHandler);
		maintenancePanel.add(buttonActiveCalibration2);

		Button buttonDisactiveCalibration2 = new Button();
		buttonDisactiveCalibration2.setStyleName("gwt-button-stop");
		buttonDisactiveCalibration2.setTitle(PerifericoUI.getMessages()
				.disactive_maintenance());
		buttonDisactiveCalibration2.addClickHandler(maintenanceStopHandler);
		maintenancePanel.add(buttonDisactiveCalibration2);
		hide();

		container.add(title);
		container.add(body);
	}

	void hide() {
		title.setVisible(false);
		body.setVisible(false);
	}

	void show(boolean operationEnabled) {
		lblCalibStatus.setVisible(operationEnabled);
		calibPanel.setVisible(operationEnabled);
		lblMaintenanceStatus.setVisible(operationEnabled);
		maintenancePanel.setVisible(operationEnabled);
		lblNoCalibration.setVisible(!operationEnabled);
		title.setVisible(true);
		body.setVisible(true);
	}

	void setCalibStatus(Boolean status) {
		calibPanel.remove(0);
		if (status) {
			// calibration in progress
			Image ledYellow = new Image();
			ledYellow.setResource(ledIcon.ledYellow());
			calibPanel.insert(ledYellow, 0);
			calibPanel.getWidget(0).setTitle(
					PerifericoUI.getMessages().calib_in_progress());
		} else {
			Image ledGray = new Image();
			ledGray.setResource(ledIcon.ledGray());
			calibPanel.insert(ledGray, 0);
			calibPanel.getWidget(0).setTitle(
					PerifericoUI.getMessages().calib_not_in_progress());
		}
	}

	void setMaintenanceStatus(Boolean status) {
		maintenancePanel.remove(0);
		if (status) {
			// maintenance in progress
			Image ledYellow = new Image();
			ledYellow.setResource(ledIcon.ledYellow());
			maintenancePanel.insert(ledYellow, 0);
			maintenancePanel.getWidget(0).setTitle(
					PerifericoUI.getMessages().maintenance_in_progress());
		} else {
			Image ledGray = new Image();
			ledGray.setResource(ledIcon.ledGray());
			maintenancePanel.insert(ledGray, 0);
			maintenancePanel.getWidget(0).setTitle(
					PerifericoUI.getMessages().maintenance_not_in_progress());
		}
	}

}
