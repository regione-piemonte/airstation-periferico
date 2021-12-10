/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: element configuration page for wind analyzers
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: WindElementWidget.java,v 1.37 2015/05/12 13:01:50 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.data.ElementInfo;
import it.csi.periferico.ui.client.data.WindElementInfo;
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
 * Widget that display wind element.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class WindElementWidget extends ElementPanelWidget {

	private TextBox textSpeedNumDec;

	private TextBox textSpeedPrecision;

	private ListBox listMeasureUnit;

	private TextBox textMax;

	private ListBox listBoxAvgPeriod;

	private TextBox textAcqPeriod;

	private TextBox textDirectionNumDec;

	private TextBox textDirectionPrecision;

	private TextBox textDirectionCorrectionOffset;

	private TextBox textDirectionCorrectionCoefficient;

	private TextBox textSpeedCorrectionOffset;

	private TextBox textSpeedCorrectionCoefficient;

	private TextBox textSpeedRangeHigh;

	private TextBox textSpeedBoardBindInfo;

	private TextBox textDirectionBoardBindInfo;

	private Button newAvgPeriod;

	private FlexTable avgTable;

	private Label genericTitle;

	private Label speedRangeHighLabel;

	private Label directionMeasureUnitLabel;

	private Label prevalidationSpeedMaxLabel;

	private Label speedPrecisionLabel;

	private Label directionPrecisionLabel;

	// Grids for sample element information.
	private FlexTable windGrid = new FlexTable();

	public WindElementWidget() {

		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();

		panelButtonWidget.addButton(PerifericoUI.makeUpButton());
		panelButtonWidget.addButton(PerifericoUI.makeSaveButton());
		panelButtonWidget.addButton(PerifericoUI.makeReloadButton());
		panelButtonWidget.addButton(PerifericoUI
				.makeConfHelpButton("windElementWidget"));

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

		hiddenParamId = new Hidden();

		externalPanel.add(genericTitle);
		externalPanel.add(infoPanel);

		NumericKeyPressHandler positiveKeyHandler = new NumericKeyPressHandler(
				true);
		NumericKeyPressHandler numericKeyHandler = new NumericKeyPressHandler(
				false);

		/*
		 * Wind element info
		 */
		// format the grid for specific element info
		windGrid.getCellFormatter().setWidth(0, 0, "210px");
		windGrid.getCellFormatter().setWidth(0, 1, "270px");
		windGrid.getCellFormatter().setWidth(0, 2, "210px");
		windGrid.getCellFormatter().setWidth(0, 3, "270px");
		windGrid.setCellPadding(2);
		windGrid.setCellSpacing(2);

		// Put values in the specific element cells.
		windGrid.setText(0, 0, PerifericoUI.getMessages().lbl_enabled());
		windGrid.setWidget(0, 1, elementEnabled);

		String message = PerifericoUI.getMessages().element_lbl_speed_measureUnit();
		windGrid.setText(0, 2, message);
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
					speedRangeHighLabel.setText(PerifericoUI.getMessages()
							.element_lbl_speedRangeHigh() + ":");
					prevalidationSpeedMaxLabel.setText(PerifericoUI.getMessages()
							.element_lbl_speed_max() + ":");
					speedPrecisionLabel.setText(PerifericoUI.getMessages()
							.element_lbl_speed_precision() + ":");
					Window.alert(PerifericoUI.getMessages().error_select_element());
				} else {
					StringBuffer strBuf = new StringBuffer(
							PerifericoUI.getMessages().element_lbl_speedRangeHigh());
					String selectedMeasureUnit = " [" + selectedItemText + "]:";
					strBuf.append(selectedMeasureUnit);
					speedRangeHighLabel.setText(strBuf.toString());
					strBuf = new StringBuffer(PerifericoUI.getMessages()
							.element_lbl_speed_max());
					strBuf.append(selectedMeasureUnit);
					prevalidationSpeedMaxLabel.setText(strBuf.toString());
					speedPrecisionLabel.setText(PerifericoUI.getMessages()
							.element_lbl_speed_precision()
							+ selectedMeasureUnit);
				}

			}

		});
		windGrid.setWidget(0, 3, listMeasureUnit);

		speedRangeHighLabel = new Label();
		windGrid.setWidget(1, 0, speedRangeHighLabel);
		textSpeedRangeHigh = new TextBox();
		textSpeedRangeHigh.setWidth("100px");
		textSpeedRangeHigh.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(1, 1, textSpeedRangeHigh);
		textSpeedRangeHigh.addKeyPressHandler(positiveKeyHandler);

		message = PerifericoUI.getMessages().element_lbl_direction_measureUnit();
		windGrid.setText(1, 2, message);
		directionMeasureUnitLabel = new Label();
		windGrid.setWidget(1, 3, directionMeasureUnitLabel);

		message = PerifericoUI.getMessages()
				.element_lbl_speedCorrectionCoefficient();
		windGrid.setText(2, 0, message);
		textSpeedCorrectionCoefficient = new TextBox();
		textSpeedCorrectionCoefficient.setWidth("100px");
		textSpeedCorrectionCoefficient.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(2, 1, textSpeedCorrectionCoefficient);
		textSpeedCorrectionCoefficient.addKeyPressHandler(positiveKeyHandler);

		message = PerifericoUI.getMessages().element_lbl_speed_numDec();
		windGrid.setText(2, 2, message);
		textSpeedNumDec = new TextBox();
		textSpeedNumDec.setWidth("100px");
		textSpeedNumDec.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(2, 3, textSpeedNumDec);
		textSpeedNumDec.addKeyPressHandler(positiveKeyHandler);

		speedPrecisionLabel = new Label();
		windGrid.setWidget(4, 2, speedPrecisionLabel);
		textSpeedPrecision = new TextBox();
		textSpeedPrecision.setWidth("100px");
		textSpeedPrecision.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(4, 3, textSpeedPrecision);
		textSpeedPrecision.addKeyPressHandler(positiveKeyHandler);

		message = PerifericoUI.getMessages().element_lbl_speedCorrectionOffset();
		windGrid.setText(3, 0, message);
		textSpeedCorrectionOffset = new TextBox();
		textSpeedCorrectionOffset.setWidth("100px");
		textSpeedCorrectionOffset.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(3, 1, textSpeedCorrectionOffset);
		textSpeedCorrectionOffset.addKeyPressHandler(numericKeyHandler);

		message = PerifericoUI.getMessages().element_lbl_direction_numDec();
		windGrid.setText(3, 2, message);
		textDirectionNumDec = new TextBox();
		textDirectionNumDec.setWidth("100px");
		textDirectionNumDec.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(3, 3, textDirectionNumDec);
		textDirectionNumDec.addKeyPressHandler(positiveKeyHandler);

		directionPrecisionLabel = new Label();
		directionPrecisionLabel.setText(PerifericoUI.getMessages()
				.element_lbl_direction_precision() + ":");
		windGrid.setWidget(5, 2, directionPrecisionLabel);
		textDirectionPrecision = new TextBox();
		textDirectionPrecision.setWidth("100px");
		textDirectionPrecision.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(5, 3, textDirectionPrecision);
		textDirectionPrecision.addKeyPressHandler(positiveKeyHandler);

		message = PerifericoUI.getMessages()
				.element_lbl_directionCorrectionCoefficient();
		windGrid.setText(4, 0, message);
		textDirectionCorrectionCoefficient = new TextBox();
		textDirectionCorrectionCoefficient.setWidth("100px");
		textDirectionCorrectionCoefficient.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(4, 1, textDirectionCorrectionCoefficient);
		textDirectionCorrectionCoefficient
				.addKeyPressHandler(positiveKeyHandler);

		prevalidationSpeedMaxLabel = new Label();
		windGrid.setWidget(6, 2, prevalidationSpeedMaxLabel);
		textMax = new TextBox();
		textMax.setWidth("100px");
		textMax.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(6, 3, textMax);
		textMax.addKeyPressHandler(positiveKeyHandler);

		message = PerifericoUI.getMessages().element_lbl_directionCorrectionOffset();
		windGrid.setText(5, 0, message);
		textDirectionCorrectionOffset = new TextBox();
		textDirectionCorrectionOffset.setWidth("100px");
		textDirectionCorrectionOffset.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(5, 1, textDirectionCorrectionOffset);
		textDirectionCorrectionOffset.addKeyPressHandler(numericKeyHandler);

		HorizontalPanel newAvgPanel = new HorizontalPanel();
		Label avgPeriodsLabel = new Label(
				PerifericoUI.getMessages().element_lbl_newAvgPeriod());
		windGrid.setWidget(7, 2, avgPeriodsLabel);
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
		windGrid.setWidget(7, 3, newAvgPanel);

		message = PerifericoUI.getMessages().lbl_acqPeriod();
		windGrid.setText(6, 0, message);
		textAcqPeriod = new TextBox();
		textAcqPeriod.setWidth("100px");
		textAcqPeriod.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(6, 1, textAcqPeriod);
		textAcqPeriod.addKeyPressHandler(positiveKeyHandler);

		message = PerifericoUI.getMessages().element_lbl_avgPeriods();
		windGrid.setText(8, 2, message);
		windGrid.getCellFormatter().setVerticalAlignment(6, 2,
				HasVerticalAlignment.ALIGN_TOP);
		// Table for selected avgPeriod
		ScrollPanel scrollPanel = new ScrollPanel();
		avgTable = new FlexTable();
		avgTable.setStyleName("gwt-table-data");
		avgTable.setWidth("50px");
		scrollPanel.add(avgTable);
		scrollPanel.setHeight("50px");
		scrollPanel.setWidth("70px");
		windGrid.setWidget(8, 3, scrollPanel);
		windGrid.getCellFormatter().setHorizontalAlignment(6, 3,
				HasHorizontalAlignment.ALIGN_LEFT);

		message = PerifericoUI.getMessages().element_lbl_speedBoardBindInfo();
		windGrid.setText(7, 0, message);
		textSpeedBoardBindInfo = new TextBox();
		textSpeedBoardBindInfo.setWidth("270px");
		textSpeedBoardBindInfo.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(7, 1, textSpeedBoardBindInfo);
		textSpeedBoardBindInfo.setEnabled(false);

		message = PerifericoUI.getMessages().element_directionBoardBindInfo();
		windGrid.setText(8, 0, message);
		textDirectionBoardBindInfo = new TextBox();
		textDirectionBoardBindInfo.setWidth("270px");
		textDirectionBoardBindInfo.setStyleName("gwt-bg-text-orange");
		windGrid.setWidget(8, 1, textDirectionBoardBindInfo);
		textDirectionBoardBindInfo.setEnabled(false);

		infoPanel.add(windGrid);

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
			info = setWindParameter(selectedItemText);

			perifService.setVerifyElementFields(analyzerId, info, callback);
		}

	}// end verifyElementFields

	private String[] setWindParameter(String selectedItemText) {
		/*
		 * info for wind element :
		 * 
		 * [0] : SpeedMeasureUnitName selected
		 * 
		 * [1] : SpeedMaxValue
		 * 
		 * [2] : SpeedNumDec
		 * 
		 * [3] : SpeedPrecision
		 * 
		 * [4] : DirectionNumDec
		 * 
		 * [5] : DirectionPrecision
		 * 
		 * [6] : enabled
		 * 
		 * [7] : AcqPeriod
		 * 
		 * [8] : SpeedCorrectionCoefficient
		 * 
		 * [9] : SpeedCorrectionOffset
		 * 
		 * [10] : SpeedTheoricRangeHigh
		 * 
		 * [11] : DirectionCorrectionCoefficient
		 * 
		 * [12] : DirectionCorrectionOffset
		 */

		String[] info = new String[13];
		info[0] = selectedItemText;
		info[1] = textMax.getText();
		info[2] = textSpeedNumDec.getText();
		info[3] = textSpeedPrecision.getText();
		info[4] = textDirectionNumDec.getText();
		info[5] = textDirectionPrecision.getText();
		if (elementEnabled.getTitle().equals(
				PerifericoUI.getMessages().button_disabled()))
			info[6] = new Boolean(false).toString();
		else
			info[6] = new Boolean(true).toString();
		info[7] = textAcqPeriod.getText();
		info[8] = textSpeedCorrectionCoefficient.getText();
		info[9] = textSpeedCorrectionOffset.getText();
		info[10] = textSpeedRangeHigh.getText();
		info[11] = textDirectionCorrectionCoefficient.getText();
		info[12] = textDirectionCorrectionOffset.getText();

		return info;
	}// end setWindParameter

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
			fieldsValue = setWindParameter(selectedItemText);
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
				if (elemInfo instanceof WindElementInfo) {
					setElementInfo((WindElementInfo) elemInfo);
				} else {
					throw new IllegalStateException("Unexpected element type: "
							+ elemInfo);
				}
			}// end onSuccess
		};

		perifService.getElementInfo(analyzerId, null, callback);
	}// end setElementInfo

	private void setElementInfo(WindElementInfo info) {
		hiddenParamId.setValue(info.getParameterId());
		genericTitle.setText(PerifericoUI.getMessages().lbl_element_generic_title()
				+ " " + info.getParameterId());
		setStatus(info.isEnabled());
		PerifericoUI.setTitle(externalPanel, info.getAnalyzerName());
		textMax.setText(toStr(info.getSpeedMaxValue()));
		textSpeedNumDec.setText(toStr(info.getSpeedNumDec()));
		textSpeedPrecision.setText(toStr(info.getSpeedPrecision()));
		textDirectionNumDec.setText(toStr(info.getDirectionNumDec()));
		textDirectionPrecision.setText(toStr(info.getDirectionPrecision()));

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
			deleteAvgButton.setTitle(PerifericoUI.getMessages().avg_button_delete());
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
		for (String acqMU : info.getSpeedMeasureUnits()) {
			if (acqMU == null)
				continue;
			listMeasureUnit.addItem(acqMU);
			acqIndex++;
			if (acqMU.equals(info.getSpeedMeasureUnit()))
				listMeasureUnit.setItemSelected(acqIndex, true);
		}

		textAcqPeriod.setText(toStr(info.getAcqPeriod()));
		textSpeedBoardBindInfo.setText(info.getSpeedBoardBindInfo());
		textDirectionBoardBindInfo.setText(info.getDirectionBoardBindInfo());
		textSpeedCorrectionCoefficient.setText(toStr(info
				.getSpeedCorrectionCoefficient()));
		textSpeedCorrectionOffset
				.setText(toStr(info.getSpeedCorrectionOffset()));
		textSpeedRangeHigh.setText(toStr(info.getSpeedRangeHigh()));
		textDirectionCorrectionCoefficient.setText(toStr(info
				.getDirectionCorrectionCoefficient()));
		textDirectionCorrectionOffset.setText(toStr(info
				.getDirectionCorrectionOffset()));
		directionMeasureUnitLabel.setText(info.getDirectionMeasureUnit());

		StringBuffer strBuf = new StringBuffer(
				PerifericoUI.getMessages().element_lbl_speedRangeHigh());
		String selectedMeasureUnit = " [" + info.getSpeedMeasureUnit() + "]:";
		strBuf.append(selectedMeasureUnit);
		speedRangeHighLabel.setText(strBuf.toString());
		strBuf = new StringBuffer(PerifericoUI.getMessages().element_lbl_speed_max());
		strBuf.append(selectedMeasureUnit);
		prevalidationSpeedMaxLabel.setText(strBuf.toString());
		speedPrecisionLabel.setText(PerifericoUI.getMessages()
				.element_lbl_speed_precision() + selectedMeasureUnit);
		directionPrecisionLabel.setText(PerifericoUI.getMessages()
				.element_lbl_direction_precision()
				+ " ["
				+ info.getDirectionMeasureUnit() + "]:");
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
