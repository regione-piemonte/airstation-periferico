/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: element configuration page for rain analyzers
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: RainElementWidget.java,v 1.35 2015/05/12 11:26:59 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.data.ElementInfo;
import it.csi.periferico.ui.client.data.RainElementInfo;
import it.csi.periferico.ui.client.pagecontrol.AsyncPageOperation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * Widget that display rain element.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class RainElementWidget extends ElementPanelWidget {

	private TextBox textNumDec;

	private TextBox textMin;

	private ListBox listMeasureUnit;

	private TextBox textMax;

	private ListBox listBoxAvgPeriod;

	private TextBox textBoardBindInfo;

	private TextBox textValueForEvent;

	private CheckBox checkAcqOnRisingEdge;

	private Button newAvgPeriod;

	private FlexTable avgTable;

	private Label genericTitle;

	private Label prevalidationMinLabel;

	private Label prevalidationMaxLabel;

	private Label valueForEventLabel;

	// Grids for rain element information.
	private FlexTable rainGrid = new FlexTable();

	public RainElementWidget() {

		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();

		panelButtonWidget.addButton(PerifericoUI.makeUpButton());
		panelButtonWidget.addButton(PerifericoUI.makeSaveButton());
		panelButtonWidget.addButton(PerifericoUI.makeReloadButton());
		panelButtonWidget.addButton(PerifericoUI
				.makeConfHelpButton("rainElementWidget"));

		externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().element_lbl_title_configuration(),
				panelButtonWidget);

		// Label and panel for generic element info
		genericTitle = new Label();
		genericTitle.setText(PerifericoUI.getMessages().lbl_element_generic_title());
		genericTitle.setStyleName("gwt-Label-title");

		// panel that contains the anagraphic analyzer grid
		VerticalPanel infoPanel = new VerticalPanel();
		infoPanel.setStyleName("gwt-post-boxed");

		// Put values in the generic element cells.
		elementEnabled = new Button();
		elementEnabled.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				changeStatus();
			}
		});

		externalPanel.add(genericTitle);
		externalPanel.add(infoPanel);

		NumericKeyPressHandler negativeKeyboardListener = new NumericKeyPressHandler(
				false);
		NumericKeyPressHandler positiveKeyboardListener = new NumericKeyPressHandler(
				true);

		/*
		 * Rain element info
		 */
		// format the grid for specific element info
		rainGrid.getCellFormatter().setWidth(0, 0, "210px");
		rainGrid.getCellFormatter().setWidth(0, 1, "270px");
		rainGrid.getCellFormatter().setWidth(0, 2, "210px");
		rainGrid.getCellFormatter().setWidth(0, 3, "270px");
		rainGrid.setCellPadding(2);
		rainGrid.setCellSpacing(2);

		// Put values in the specific element cells.
		rainGrid.setText(0, 0, PerifericoUI.getMessages().lbl_enabled());
		rainGrid.setWidget(0, 1, elementEnabled);

		String message = PerifericoUI.getMessages().element_lbl_measureUnit();
		rainGrid.setText(0, 2, message);
		listMeasureUnit = new ListBox();
		listMeasureUnit.setWidth("100px");
		listMeasureUnit.setStyleName("gwt-bg-text-orange");
		listMeasureUnit.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int indexSelected = listMeasureUnit.getSelectedIndex();
				String selectedValue = listMeasureUnit.getValue(indexSelected);
				String selectedItemText = listMeasureUnit
						.getItemText(indexSelected);
				if (selectedValue.equals("0")) {
					prevalidationMinLabel.setText(PerifericoUI.getMessages()
							.element_lbl_min() + ":");
					prevalidationMaxLabel.setText(PerifericoUI.getMessages()
							.element_lbl_max() + ":");
					valueForEventLabel.setText(PerifericoUI.getMessages()
							.element_lbl_valueForEvent() + ":");
					Window.alert(PerifericoUI.getMessages().error_select_element());
				} else {
					StringBuffer strBuf = new StringBuffer(
							PerifericoUI.getMessages().element_lbl_min());
					String selectedMeasureUnit = " [" + selectedItemText + "]:";
					strBuf.append(selectedMeasureUnit);
					prevalidationMinLabel.setText(strBuf.toString());
					strBuf = new StringBuffer(PerifericoUI.getMessages()
							.element_lbl_max());
					strBuf.append(selectedMeasureUnit);
					prevalidationMaxLabel.setText(strBuf.toString());
					strBuf = new StringBuffer(PerifericoUI.getMessages()
							.element_lbl_valueForEvent());
					strBuf.append(selectedMeasureUnit);
					valueForEventLabel.setText(strBuf.toString());
				}

			}

		});
		rainGrid.setWidget(0, 3, listMeasureUnit);

		valueForEventLabel = new Label();
		rainGrid.setWidget(1, 0, valueForEventLabel);
		textValueForEvent = new TextBox();
		textValueForEvent.setWidth("100px");
		textValueForEvent.setStyleName("gwt-bg-text-orange");
		rainGrid.setWidget(1, 1, textValueForEvent);
		textValueForEvent.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_numDec();
		rainGrid.setText(1, 2, message);
		textNumDec = new TextBox();
		textNumDec.setWidth("100px");
		textNumDec.setStyleName("gwt-bg-text-orange");
		rainGrid.setWidget(1, 3, textNumDec);
		textNumDec.addKeyPressHandler(positiveKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_acqOnRisingEdge();
		rainGrid.setText(2, 0, message);
		checkAcqOnRisingEdge = new CheckBox();
		rainGrid.setWidget(2, 1, checkAcqOnRisingEdge);

		prevalidationMinLabel = new Label();
		rainGrid.setWidget(2, 2, prevalidationMinLabel);
		textMin = new TextBox();
		textMin.setWidth("100px");
		textMin.setStyleName("gwt-bg-text-orange");
		rainGrid.setWidget(2, 3, textMin);
		textMin.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_boardBindInfo();
		rainGrid.setText(3, 0, message);
		textBoardBindInfo = new TextBox();
		textBoardBindInfo.setWidth("270px");
		textBoardBindInfo.setStyleName("gwt-bg-text-orange");
		rainGrid.setWidget(3, 1, textBoardBindInfo);
		textBoardBindInfo.setEnabled(false);

		prevalidationMaxLabel = new Label();
		rainGrid.setWidget(3, 2, prevalidationMaxLabel);
		textMax = new TextBox();
		textMax.setWidth("100px");
		textMax.setStyleName("gwt-bg-text-orange");
		rainGrid.setWidget(3, 3, textMax);
		textMax.addKeyPressHandler(negativeKeyboardListener);

		HorizontalPanel newAvgPanel = new HorizontalPanel();
		Label avgPeriodsLabel = new Label(
				PerifericoUI.getMessages().element_lbl_newAvgPeriod());
		rainGrid.setWidget(4, 2, avgPeriodsLabel);
		listBoxAvgPeriod = new ListBox();
		listBoxAvgPeriod.setWidth("150px");
		listBoxAvgPeriod.setStyleName("gwt-bg-text-orange");
		newAvgPanel.add(listBoxAvgPeriod);
		newAvgPeriod = new Button();
		newAvgPeriod
				.setTitle(PerifericoUI.getMessages().element_lbl_new_avgPeriod());
		newAvgPeriod.setStyleName("gwt-button-new-small");
		newAvgPeriod.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addNewAverage();
			}// end onClick
		});
		newAvgPanel.add(newAvgPeriod);
		rainGrid.setWidget(4, 3, newAvgPanel);

		message = PerifericoUI.getMessages().element_lbl_avgPeriods();
		rainGrid.setText(5, 2, message);
		rainGrid.getCellFormatter().setVerticalAlignment(5, 2,
				HasVerticalAlignment.ALIGN_TOP);
		// Table for selected avgPeriod
		ScrollPanel scrollPanel = new ScrollPanel();
		avgTable = new FlexTable();
		avgTable.setStyleName("gwt-table-data");
		avgTable.setWidth("50px");
		scrollPanel.add(avgTable);
		scrollPanel.setHeight("50px");
		scrollPanel.setWidth("70px");
		rainGrid.setWidget(5, 3, scrollPanel);
		rainGrid.getCellFormatter().setHorizontalAlignment(5, 3,
				HasHorizontalAlignment.ALIGN_LEFT);

		infoPanel.add(rainGrid);

		hiddenParamId = new Hidden();

		PanelButtonWidget panelButton = PerifericoUI.makeUndoSendPanelButton(
				new ClickHandler() {
					public void onClick(ClickEvent event) {
						reset();
						readElementInfo();
					}
				}, new ClickHandler() {
					public void onClick(ClickEvent event) {
						verifyElementFields();
					}
				});

		// panel that contains button for generic element info
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setStyleName("gwt-button-panel");

		hPanel.add(panelButton);
		hPanel.setWidth("100%");
		hPanel.setCellHorizontalAlignment(panelButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		infoPanel.add(hPanel);

		initWidget(externalPanel);

	}// end constructor

	private void verifyElementFields() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		listBoxAvgPeriod.setSelectedIndex(0);

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
					if (PerifericoUI.getMessages().lbl_none().equals(
							avgTable.getText(0, 0)))
						Window.alert(PerifericoUI.getMessages().warn_avg_periods());
				}
			}
		};

		int indexSelected = listMeasureUnit.getSelectedIndex();
		String selectedValue = listMeasureUnit.getValue(indexSelected);
		String selectedItemText = listMeasureUnit.getItemText(indexSelected);
		if (selectedValue.equals("0"))
			Window.alert(PerifericoUI.getMessages().error_select_element());
		else {

			String[] info = null;
			info = setRainParameter(selectedItemText);

			perifService.setVerifyElementFields(analyzerId, info, callback);
		}

	}// end verifyElementFields

	private String[] setRainParameter(String selectedItemText) {
		/*
		 * info for rain element :
		 * 
		 * [0] : paramId
		 * 
		 * [1] : MeasureUnitName selected
		 * 
		 * [2] : MaxValue
		 * 
		 * [3] : MinValue
		 * 
		 * [4] : NumDec
		 * 
		 * [5] : enabled
		 * 
		 * [6] : ValueForEvent
		 * 
		 * [7] : AcqOnRisingEdge
		 */

		String[] info = new String[8];
		info[0] = hiddenParamId.getValue();
		info[1] = selectedItemText;
		info[2] = textMax.getText();
		info[3] = textMin.getText();
		info[4] = textNumDec.getText();
		if (elementEnabled.getTitle().equals(
				PerifericoUI.getMessages().button_disabled()))
			info[5] = new Boolean(false).toString();
		else
			info[5] = new Boolean(true).toString();
		info[6] = textValueForEvent.getText();
		info[7] = checkAcqOnRisingEdge.getValue().toString();

		return info;
	}// end setRainParameter

	@Override
	protected void dismissContent(AsyncPageOperation asyncPageOperation) {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");

		int indexSelected = listMeasureUnit.getSelectedIndex();
		String selectedValue = listMeasureUnit.getValue(indexSelected);
		String selectedItemText = listMeasureUnit.getItemText(indexSelected);
		if (selectedValue.equals("0"))
			Window.alert(PerifericoUI.getMessages().error_select_element());
		else {
			String fieldsValue[] = new String[0];
			fieldsValue = setRainParameter(selectedItemText);
			perifService.verifySameElementConfig(analyzerId, fieldsValue,
					asyncPageOperation);
		}
	}

	void setAnalyzer(String analyzerId, String type) {
		this.analyzerId = analyzerId;
		this.analyzerType = type;
	}

	private void readElementInfo() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<ElementInfo> callback = new UIAsyncCallback<ElementInfo>() {
			public void onSuccess(ElementInfo elemInfo) {
				if (elemInfo instanceof RainElementInfo) {
					setElementInfo((RainElementInfo) elemInfo);
				} else {
					throw new IllegalStateException("Unexpected element type: "
							+ elemInfo);
				}
			}
		};

		perifService.getElementInfo(analyzerId, null, callback);
	}// end setElementInfo

	private void setElementInfo(RainElementInfo info) {
		genericTitle.setText(PerifericoUI.getMessages().lbl_element_generic_title()
				+ " " + info.getParameterId());
		setStatus(info.isEnabled());
		PerifericoUI.setTitle(externalPanel, info.getAnalyzerName());
		hiddenParamId.setValue(info.getParameterId());
		textMax.setText(toStr(info.getMaxValue()));
		textNumDec.setText(toStr(info.getNumDec()));
		textMin.setText(toStr(info.getMinValue()));

		Utils.clearTable(avgTable);
		int periodsCount = 0;
		for (int i = 0; i < info.getSelectedAvgPeriods().size(); i++) {
			Integer avgPeriod = info.getSelectedAvgPeriods().get(i);
			if (avgPeriod == null)
				continue;
			periodsCount++;
			avgTable.setText(i, 0, toStr(avgPeriod));
			Button deleteAvgButton = new Button();
			deleteAvgButton.setStyleName("gwt-button-delete-small");
			deleteAvgButton
					.setTitle(PerifericoUI.getMessages().rain_button_delete());
			avgTable.setWidget(i, 1, deleteAvgButton);
			avgTable.getCellFormatter().setAlignment(i, 1,
					HasHorizontalAlignment.ALIGN_CENTER,
					HasVerticalAlignment.ALIGN_MIDDLE);
			deleteAvgButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					deleteAvgPeriod((Widget) event.getSource());
				}
			});
		}// end for i
		if (periodsCount == 0)
			avgTable.setText(0, 0, PerifericoUI.getMessages().lbl_none());

		listBoxAvgPeriod.clear();
		listBoxAvgPeriod.addItem(PerifericoUI.getMessages().select_avg(),
				new String("-1"));
		for (Integer avgPeriod : info.getAvailableAvgPeriods())
			if (avgPeriod != null)
				listBoxAvgPeriod.addItem(toStr(avgPeriod));

		listMeasureUnit.clear();
		listMeasureUnit.addItem(PerifericoUI.getMessages().select_measure(),
				new String("0"));
		int acqIndex = 0;
		for (String acqMU : info.getAcqMeasureUnits()) {
			if (acqMU == null)
				continue;
			listMeasureUnit.addItem(acqMU);
			acqIndex++;
			if (acqMU.equals(info.getAcqMeasureUnit()))
				listMeasureUnit.setItemSelected(acqIndex, true);
		}

		textValueForEvent.setText(toStr(info.getValueForEvent()));
		textBoardBindInfo.setText(info.getBoardBindInfo());
		checkAcqOnRisingEdge.setValue(info.isAcqOnRisingEdge());

		StringBuffer strBuf = new StringBuffer(
				PerifericoUI.getMessages().element_lbl_min());
		String selectedMeasureUnit = " [" + info.getAcqMeasureUnit() + "]:";
		strBuf.append(selectedMeasureUnit);
		prevalidationMinLabel.setText(strBuf.toString());
		strBuf = new StringBuffer(PerifericoUI.getMessages().element_lbl_max());
		strBuf.append(selectedMeasureUnit);
		prevalidationMaxLabel.setText(strBuf.toString());
		strBuf = new StringBuffer(
				PerifericoUI.getMessages().element_lbl_valueForEvent());
		strBuf.append(selectedMeasureUnit);
		valueForEventLabel.setText(strBuf.toString());
	} // end setElementInfo

	private void addNewAverage() {
		if (listBoxAvgPeriod.getSelectedIndex() == 0) {
			Window.alert(PerifericoUI.getMessages().iouser_error_select_item());
			return;
		}
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] resultString) {
				if (resultString != null
						&& resultString.equals(PerifericoUI.SESSION_ENDED))
					PerifericoUI.sessionEnded();
				else if (new Boolean(resultString[0]).booleanValue()) {
					int newRow = avgTable.getRowCount();
					if (newRow == 1
							&& PerifericoUI.getMessages().lbl_none().equals(
									avgTable.getText(0, 0))) {
						newRow = 0;
					}
					avgTable.setText(newRow, 0, resultString[1]);
					Button deleteAvgButton = new Button();
					deleteAvgButton.setStyleName("gwt-button-delete-small");
					deleteAvgButton.setTitle(PerifericoUI.getMessages()
							.avg_button_delete());
					avgTable.setWidget(newRow, 1, deleteAvgButton);
					avgTable.getCellFormatter().setAlignment(newRow, 1,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					deleteAvgButton.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							deleteAvgPeriod((Widget) event.getSource());
						}// end onClick
					});
					listBoxAvgPeriod.setSelectedIndex(0);
				} else
					Window.alert(PerifericoUI.getMessages().error_insert_avg());
			}// end onSuccess
		};
		perifService.setAvgPeriod(analyzerId, null, listBoxAvgPeriod
				.getItemText(listBoxAvgPeriod.getSelectedIndex()), callback);
	}

	private void deleteAvgPeriod(Widget sender) {
		Button button = (Button) sender;
		for (int k = 0; k < avgTable.getRowCount(); k++) {
			if (((Button) (avgTable.getWidget(k, 1))).equals(button)) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] resultString) {
						if (resultString.equals(PerifericoUI.SESSION_ENDED))
							PerifericoUI.sessionEnded();
						else {
							Boolean resultValue = new Boolean(resultString[0]);
							if (resultValue.booleanValue()) {
								for (int i = 0; i < avgTable.getRowCount(); i++) {
									if (avgTable.getText(i, 0).equals(
											resultString[1])) {
										avgTable.removeRow(i);
									}// end if
								}// end for
								if (avgTable.getRowCount() == 0)
									avgTable.setText(0, 0,
											PerifericoUI.getMessages().lbl_none());
							} else {
								// TODO capire cosa fare se non
								// viene cancellato
								Window.alert("Non cancellato");
							}
						}
					}// end onSuccess
				};
				perifService.deleteAvgPeriod(analyzerId, null,
						avgTable.getText(k, 0), callback);
			}// end if
		}// end for
	}

	@Override
	protected void loadContent() {
		readElementInfo();
	}

}// end class
