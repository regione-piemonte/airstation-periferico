/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: configuration page for an element of an analyzer with
 *                  data port
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: DataPortElementWidget.java,v 1.38 2015/10/26 17:03:32 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.data.DigitalElementInfo;
import it.csi.periferico.ui.client.data.ElementInfo;
import it.csi.periferico.ui.client.pagecontrol.AsyncPageOperation;
import it.csi.periferico.ui.client.pagecontrol.PageUpdateAction;

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
 * Widget for dispaly data port element. Configuration page for an element of an
 * analyzer with data port
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class DataPortElementWidget extends ElementPanelWidget {

	private TextBox textNumDec;
	private TextBox textMin;
	private ListBox listAnalyzerMeasureUnit;
	private ListBox listAcquisitionMeasureUnit;
	private TextBox textMax;
	private ListBox listBoxAvgPeriod;
	private TextBox textAcqPeriod;
	private TextBox textRangeLow;
	private TextBox textRangeHigh;
	private TextBox textCorrectionOffset;
	private TextBox textCorrectionCoefficient;
	private TextBox textLinearizationOffset;
	private TextBox textLinearizationCoefficient;
	private CheckBox cbDiscardNotValidData;
	private Button newAvgPeriod;
	private FlexTable avgTable;
	private Label genericTitle;
	private Label prevalidationMinLabel;
	private Label prevalidationMaxLabel;
	private Label rangeLowLabel;
	private Label rangeHighLabel;
	private Label conversionCoefficientLabel;
	// Grids for dpa element information.
	private FlexTable dpaGrid = new FlexTable();

	public DataPortElementWidget() {

		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();

		prevButton = PerifericoUI.makePrevButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (paramsId == null || paramsId.length == 0)
					return;
				if (currentIndex > 0) {
					PerifericoUI.updateCurrentPage(new PageUpdateAction() {
						@Override
						public void action() {
							currentIndex--;
							paramId = paramsId[currentIndex];
							updatePage();
						}
					});
				}
			}
		});
		panelButtonWidget.addButton(prevButton);

		nextButton = PerifericoUI.makeForwardButton(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (paramsId == null || paramsId.length == 0)
					return;
				if (currentIndex < paramsId.length - 1) {
					PerifericoUI.updateCurrentPage(new PageUpdateAction() {
						@Override
						public void action() {
							currentIndex++;
							paramId = paramsId[currentIndex];
							updatePage();
						}
					});
				}
			}
		});
		panelButtonWidget.addButton(nextButton);

		panelButtonWidget.addButton(PerifericoUI.makeUpButton());
		panelButtonWidget.addButton(PerifericoUI.makeSaveButton());
		panelButtonWidget.addButton(PerifericoUI.makeReloadButton());
		panelButtonWidget.addButton(PerifericoUI
				.makeConfHelpButton("dataPortElementWidget"));

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

		// format the grid for specific element info
		dpaGrid.getCellFormatter().setWidth(0, 0, "210px");
		dpaGrid.getCellFormatter().setWidth(0, 1, "180px");
		dpaGrid.getCellFormatter().setWidth(0, 2, "250px");
		dpaGrid.getCellFormatter().setWidth(0, 3, "320px");
		dpaGrid.setCellPadding(2);
		dpaGrid.setCellSpacing(2);

		// Put values in the specific element cells.
		dpaGrid.setText(0, 0, PerifericoUI.getMessages().lbl_enabled());
		dpaGrid.setWidget(0, 1, elementEnabled);

		NumericKeyPressHandler negativeKeyboardListener = new NumericKeyPressHandler(
				false);
		NumericKeyPressHandler positiveKeyboardListener = new NumericKeyPressHandler(
				true);

		String message = PerifericoUI.getMessages()
				.element_lbl_acquisition_measureUnit();
		dpaGrid.setText(0, 2, message);
		listAcquisitionMeasureUnit = new ListBox();
		listAcquisitionMeasureUnit.setWidth("100px");
		listAcquisitionMeasureUnit.setStyleName("gwt-bg-text-orange");
		listAcquisitionMeasureUnit.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");

				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] resultString) {
						if (resultString[0].equals(PerifericoUI.SESSION_ENDED))
							Window.alert(PerifericoUI.getMessages().session_ended());
						else
							conversionCoefficientLabel.setText(resultString[2]);
					}
				};
				int acquisitionIndexSelected = listAcquisitionMeasureUnit
						.getSelectedIndex();
				String acquisitionSelectedValue = listAcquisitionMeasureUnit
						.getValue(acquisitionIndexSelected);
				String acquisitionSelectedItemText = listAcquisitionMeasureUnit
						.getItemText(acquisitionIndexSelected);
				int analyzerIndexSelected = listAnalyzerMeasureUnit
						.getSelectedIndex();
				String analyzerSelectedValue = listAnalyzerMeasureUnit
						.getValue(analyzerIndexSelected);
				String analyzerSelectedItemText = listAnalyzerMeasureUnit
						.getItemText(analyzerIndexSelected);
				if (acquisitionSelectedValue.equals("0")) {
					setAcquisitionMeasureUnits("");
					conversionCoefficientLabel.setText("");
					Window.alert(PerifericoUI.getMessages().error_select_element());
				} else {
					setAcquisitionMeasureUnits(acquisitionSelectedItemText);
					if (!analyzerSelectedValue.equals("0")) {
						perifService.getConversionCoefficient(analyzerId,
								hiddenParamId.getValue(),
								acquisitionSelectedItemText,
								analyzerSelectedItemText, callback);
					}
				}
			}

		});
		dpaGrid.setWidget(0, 3, listAcquisitionMeasureUnit);

		message = PerifericoUI.getMessages().element_lbl_analyzer_measureUnit();
		dpaGrid.setText(1, 0, message);
		listAnalyzerMeasureUnit = new ListBox();
		listAnalyzerMeasureUnit.setWidth("100px");
		listAnalyzerMeasureUnit.setStyleName("gwt-bg-text-orange");
		listAnalyzerMeasureUnit.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");

				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] resultString) {
						if (resultString[0].equals(PerifericoUI.SESSION_ENDED))
							Window.alert(PerifericoUI.getMessages().session_ended());
						else
							conversionCoefficientLabel.setText(resultString[2]);
					}
				};
				int acquisitionIndexSelected = listAcquisitionMeasureUnit
						.getSelectedIndex();
				String acquisitionSelectedValue = listAcquisitionMeasureUnit
						.getValue(acquisitionIndexSelected);
				String acquisitionSelectedItemText = listAcquisitionMeasureUnit
						.getItemText(acquisitionIndexSelected);
				int analyzerIndexSelected = listAnalyzerMeasureUnit
						.getSelectedIndex();
				String analyzerSelectedValue = listAnalyzerMeasureUnit
						.getValue(analyzerIndexSelected);
				String analyzerSelectedItemText = listAnalyzerMeasureUnit
						.getItemText(analyzerIndexSelected);
				if (analyzerSelectedValue.equals("0")) {
					setAnalyzerMeasureUnits("");
					conversionCoefficientLabel.setText("");
					Window.alert(PerifericoUI.getMessages().error_select_element());
				} else {
					setAnalyzerMeasureUnits(analyzerSelectedItemText);
					if (!acquisitionSelectedValue.equals("0")) {
						perifService.getConversionCoefficient(analyzerId,
								hiddenParamId.getValue(),
								acquisitionSelectedItemText,
								analyzerSelectedItemText, callback);
					}
				}
			}

		});
		dpaGrid.setWidget(1, 1, listAnalyzerMeasureUnit);

		message = PerifericoUI.getMessages().element_lbl_conversionCoefficient();
		dpaGrid.setText(1, 2, message);
		conversionCoefficientLabel = new Label();
		dpaGrid.setWidget(1, 3, conversionCoefficientLabel);

		rangeLowLabel = new Label();
		dpaGrid.setWidget(2, 0, rangeLowLabel);
		textRangeLow = new TextBox();
		textRangeLow.setWidth("100px");
		textRangeLow.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(2, 1, textRangeLow);
		textRangeLow.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_numDec();
		dpaGrid.setText(2, 2, message);
		textNumDec = new TextBox();
		textNumDec.setWidth("100px");
		textNumDec.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(2, 3, textNumDec);
		textNumDec.addKeyPressHandler(positiveKeyboardListener);

		rangeHighLabel = new Label();
		dpaGrid.setWidget(3, 0, rangeHighLabel);
		textRangeHigh = new TextBox();
		textRangeHigh.setWidth("100px");
		textRangeHigh.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(3, 1, textRangeHigh);
		textRangeHigh.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_linearizationCoefficient();
		dpaGrid.setText(3, 2, message);
		textLinearizationCoefficient = new TextBox();
		textLinearizationCoefficient.setWidth("100px");
		textLinearizationCoefficient.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(3, 3, textLinearizationCoefficient);
		textLinearizationCoefficient
				.addKeyPressHandler(positiveKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_correctionCoefficient();
		dpaGrid.setText(4, 0, message);
		textCorrectionCoefficient = new TextBox();
		textCorrectionCoefficient.setWidth("100px");
		textCorrectionCoefficient.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(4, 1, textCorrectionCoefficient);
		textCorrectionCoefficient.addKeyPressHandler(positiveKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_linearizationOffset();
		dpaGrid.setText(4, 2, message);
		textLinearizationOffset = new TextBox();
		textLinearizationOffset.setWidth("100px");
		textLinearizationOffset.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(4, 3, textLinearizationOffset);
		textLinearizationOffset.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_correctionOffset();
		dpaGrid.setText(5, 0, message);
		textCorrectionOffset = new TextBox();
		textCorrectionOffset.setWidth("100px");
		textCorrectionOffset.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(5, 1, textCorrectionOffset);
		textCorrectionOffset.addKeyPressHandler(negativeKeyboardListener);

		prevalidationMinLabel = new Label();
		dpaGrid.setWidget(5, 2, prevalidationMinLabel);
		textMin = new TextBox();
		textMin.setWidth("100px");
		textMin.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(5, 3, textMin);
		textMin.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().lbl_acqPeriod();
		dpaGrid.setText(6, 0, message);
		textAcqPeriod = new TextBox();
		textAcqPeriod.setWidth("100px");
		textAcqPeriod.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(6, 1, textAcqPeriod);
		textAcqPeriod.addKeyPressHandler(positiveKeyboardListener);

		prevalidationMaxLabel = new Label();
		dpaGrid.setWidget(6, 2, prevalidationMaxLabel);
		textMax = new TextBox();
		textMax.setWidth("100px");
		textMax.setStyleName("gwt-bg-text-orange");
		dpaGrid.setWidget(6, 3, textMax);
		textMax.addKeyPressHandler(negativeKeyboardListener);

		dpaGrid.setText(7, 0, PerifericoUI.getMessages().discard_not_valid());
		cbDiscardNotValidData = new CheckBox();
		dpaGrid.setWidget(7, 1, cbDiscardNotValidData);

		HorizontalPanel newAvgPanel = new HorizontalPanel();
		Label avgPeriodsLabel = new Label(
				PerifericoUI.getMessages().element_lbl_newAvgPeriod());
		dpaGrid.setWidget(7, 2, avgPeriodsLabel);
		listBoxAvgPeriod = new ListBox();
		listBoxAvgPeriod.setWidth("150px");
		listBoxAvgPeriod.setStyleName("gwt-bg-text-orange");
		newAvgPanel.add(listBoxAvgPeriod);
		newAvgPeriod = new Button();
		newAvgPeriod.setTitle(message);
		newAvgPeriod.setStyleName("gwt-button-new-small");
		newAvgPeriod.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addNewAverage();
			}// end onClick
		});
		newAvgPanel.add(newAvgPeriod);
		dpaGrid.setWidget(7, 3, newAvgPanel);

		message = PerifericoUI.getMessages().element_lbl_avgPeriods();
		dpaGrid.setText(8, 2, message);
		dpaGrid.getCellFormatter().setVerticalAlignment(8, 2,
				HasVerticalAlignment.ALIGN_TOP);
		// Table for selected avgPeriod
		ScrollPanel scrollPanel = new ScrollPanel();
		avgTable = new FlexTable();
		avgTable.setStyleName("gwt-table-data");
		avgTable.setWidth("50px");
		scrollPanel.add(avgTable);
		scrollPanel.setHeight("50px");
		scrollPanel.setWidth("70px");
		dpaGrid.setWidget(8, 3, scrollPanel);
		dpaGrid.getCellFormatter().setHorizontalAlignment(8, 3,
				HasHorizontalAlignment.ALIGN_LEFT);

		hiddenParamId = new Hidden();

		infoPanel.add(dpaGrid);

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
		setPanelForCalibration();
		initWidget(externalPanel);

	}// end constructor

	protected void verifyElementFields() {
		listBoxAvgPeriod.setSelectedIndex(0);

		AsyncCallback<Object> callback = new UIAsyncCallback<Object>() {
			public void onSuccess(Object empty) {
				AsyncCallback<Boolean> callback2 = new UIAsyncCallback<Boolean>() {
					public void onSuccess(Boolean active) {
						Utils.sendVerifyOk();
						if (PerifericoUI.getMessages().lbl_none().equals(
								avgTable.getText(0, 0)))
							Window.alert(PerifericoUI.getMessages()
									.warn_avg_periods());
						showCalibrationPanel(active);
					}
				};

				PerifericoUI.getService().verifyDpaIsActive(analyzerId,
						hiddenParamId.getValue(), callback2);
			}
		};

		int indexSelected = listAnalyzerMeasureUnit.getSelectedIndex();
		String selectedValue1 = listAnalyzerMeasureUnit.getValue(indexSelected);
		String selectedAnalyzerMeasureUnit = listAnalyzerMeasureUnit
				.getItemText(indexSelected);
		indexSelected = listAcquisitionMeasureUnit.getSelectedIndex();
		String selectedValue2 = listAcquisitionMeasureUnit
				.getValue(indexSelected);
		String selectedUserMeasureUnit = listAcquisitionMeasureUnit
				.getItemText(indexSelected);
		if (selectedValue1.equals("0") || selectedValue2.equals("0"))
			Window.alert(PerifericoUI.getMessages().error_select_element());
		else {
			try {
				DigitalElementInfo info = setDpaParameter(
						selectedUserMeasureUnit, selectedAnalyzerMeasureUnit);
				PerifericoUI.getService().setVerifyElementFields(analyzerId,
						info, callback);
			} catch (UserParamsException e) {
				Window.alert(e.getMessage());
			}
		}
	}// end verifyElementFields

	private DigitalElementInfo setDpaParameter(String acquisitionMeasureUnit,
			String analyzerMeasureUnit) throws UserParamsException {
		DigitalElementInfo info = new DigitalElementInfo();
		info.setParameterId(hiddenParamId.getValue());
		info.setAcqMeasureUnit(acquisitionMeasureUnit);
		info.setMaxValue(toDouble(textMax.getText()));
		info.setMinValue(toDouble(textMin.getText()));
		info.setNumDec(toInt(textNumDec.getText()));
		info.setEnabled(!elementEnabled.getTitle().equals(
				PerifericoUI.getMessages().button_disabled()));
		info.setAcqPeriod(toInt(textAcqPeriod.getText()));
		info.setCorrectionCoefficient(toDouble(textCorrectionCoefficient
				.getText()));
		info.setCorrectionOffset(toDouble(textCorrectionOffset.getText()));
		info.setRangeHigh(toDouble(textRangeHigh.getText()));
		info.setRangeLow(toDouble(textRangeLow.getText()));
		info.setLinearizationCoefficient(toDouble(textLinearizationCoefficient
				.getText()));
		info.setLinearizationOffset(toDouble(textLinearizationOffset.getText()));
		info.setAnalyzerMeasureUnit(analyzerMeasureUnit);
		info.setDiscardDataNotValidForAnalyzer(cbDiscardNotValidData
				.isEnabled() ? cbDiscardNotValidData.getValue() : null);
		return info;
	}

	@Override
	protected void dismissContent(AsyncPageOperation asyncPageOperation) {
		int indexSelected = listAnalyzerMeasureUnit.getSelectedIndex();
		String selectedValue = listAnalyzerMeasureUnit.getValue(indexSelected);
		String selectedAnalyzerMeasureUnit = listAnalyzerMeasureUnit
				.getItemText(indexSelected);
		indexSelected = listAcquisitionMeasureUnit.getSelectedIndex();
		selectedValue = listAcquisitionMeasureUnit.getValue(indexSelected);
		String selectedUserMeasureUnit = listAcquisitionMeasureUnit
				.getItemText(indexSelected);
		if (selectedValue.equals("0"))
			Window.alert(PerifericoUI.getMessages().error_select_element());
		else {
			DigitalElementInfo info;
			try {
				info = setDpaParameter(selectedUserMeasureUnit,
						selectedAnalyzerMeasureUnit);
			} catch (UserParamsException e) {
				Window.alert(e.getMessage());
				info = new DigitalElementInfo();
				// Note: if some fields are not parseable config is certainly
				// different, but we need to check if session is expired
			}
			PerifericoUI.getService().verifySameElementConfig(analyzerId, info,
					asyncPageOperation);
		}
	}

	void setElements(String[] params, int index, String analyzerId,
			String analyzerType) {
		this.paramsId = params;
		this.currentIndex = index;
		this.analyzerId = analyzerId;
		this.paramId = params[index];
		this.analyzerType = analyzerType;
	}

	private void readElementInfo() {
		AsyncCallback<ElementInfo> callback = new UIAsyncCallback<ElementInfo>() {
			public void onSuccess(ElementInfo elemInfo) {
				if (elemInfo instanceof DigitalElementInfo) {
					setElementInfo((DigitalElementInfo) elemInfo);
				} else {
					throw new IllegalStateException("Unexpected element type: "
							+ elemInfo);
				}
			}// end onSuccess
		};

		PerifericoUI.getService().getElementInfo(analyzerId, paramId, callback);
	}

	private void setElementInfo(DigitalElementInfo info) {
		genericTitle.setText(PerifericoUI.getMessages().lbl_element_generic_title()
				+ " " + info.getParameterId());
		setStatus(info.isEnabled());
		hiddenParamId.setValue(info.getParameterId());
		PerifericoUI.setTitle(externalPanel, info.getAnalyzerName());
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
			deleteAvgButton.setTitle(PerifericoUI.getMessages().avg_button_delete());
			avgTable.setWidget(i, 1, deleteAvgButton);
			avgTable.getCellFormatter().setAlignment(i, 1,
					HasHorizontalAlignment.ALIGN_CENTER,
					HasVerticalAlignment.ALIGN_MIDDLE);
			deleteAvgButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					deleteAvgPeriod((Widget) event.getSource());
				}// end onClick
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

		listAnalyzerMeasureUnit.clear();
		listAnalyzerMeasureUnit.addItem(PerifericoUI.getMessages().select_measure(),
				new String("0"));
		int anaIndex = 0;
		for (String anaMU : info.getAnalyzerMeasureUnits()) {
			if (anaMU == null)
				continue;
			listAnalyzerMeasureUnit.addItem(anaMU);
			anaIndex++;
			if (anaMU.equals(info.getAnalyzerMeasureUnit()))
				listAnalyzerMeasureUnit.setItemSelected(anaIndex, true);
		}

		listAcquisitionMeasureUnit.clear();
		listAcquisitionMeasureUnit.addItem(
				PerifericoUI.getMessages().select_measure(), new String("0"));
		int acqIndex = 0;
		for (String acqMU : info.getAcqMeasureUnits()) {
			if (acqMU == null)
				continue;
			listAcquisitionMeasureUnit.addItem(acqMU);
			acqIndex++;
			if (acqMU.equals(info.getAcqMeasureUnit()))
				listAcquisitionMeasureUnit.setItemSelected(acqIndex, true);
		}

		textAcqPeriod.setText(toStr(info.getAcqPeriod()));
		textRangeHigh.setText(toStr(info.getRangeHigh()));
		textRangeLow.setText(toStr(info.getRangeLow()));
		textCorrectionCoefficient
				.setText(toStr(info.getCorrectionCoefficient()));
		textCorrectionOffset.setText(toStr(info.getCorrectionOffset()));
		textLinearizationCoefficient.setText(toStr(info
				.getLinearizationCoefficient()));
		textLinearizationOffset.setText(toStr(info.getLinearizationOffset()));
		setAnalyzerMeasureUnits(info.getAnalyzerMeasureUnit());
		setAcquisitionMeasureUnits(info.getAcqMeasureUnit());
		conversionCoefficientLabel.setText(info.getConversionInfo());
		Boolean discardData = info.getDiscardDataNotValidForAnalyzer();
		cbDiscardNotValidData.setEnabled(discardData != null);
		cbDiscardNotValidData.setValue(Boolean.TRUE.equals(discardData));
		if (info.isCalibrationRunning() != null) {
			isCalibrationRunning = info.isCalibrationRunning();
			setPanelForCalibrationRunning();
		}
		showCalibrationPanel(info.isActiveInRunningCfg());
	} // end setElementInfo

	private void setAnalyzerMeasureUnits(String measureUnitName) {
		String muLabel = ":";
		if (measureUnitName != null && !measureUnitName.isEmpty())
			muLabel = " [" + measureUnitName + "]:";
		rangeHighLabel.setText(PerifericoUI.getMessages().element_lbl_rangeHigh()
				+ muLabel);
		rangeLowLabel.setText(PerifericoUI.getMessages().element_lbl_rangeLow()
				+ muLabel);
	}

	private void setAcquisitionMeasureUnits(String measureUnitName) {
		String muLabel = ":";
		if (measureUnitName != null && !measureUnitName.isEmpty())
			muLabel = " [" + measureUnitName + "]:";
		prevalidationMinLabel.setText(PerifericoUI.getMessages().element_lbl_min()
				+ muLabel);
		prevalidationMaxLabel.setText(PerifericoUI.getMessages().element_lbl_max()
				+ muLabel);
	}

	private void addNewAverage() {
		if (listBoxAvgPeriod.getSelectedIndex() == 0) {
			Window.alert(PerifericoUI.getMessages().iouser_error_select_item());
			return;
		}
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
		PerifericoUI.getService().setAvgPeriod(
				analyzerId,
				hiddenParamId.getValue(),
				listBoxAvgPeriod.getItemText(listBoxAvgPeriod
						.getSelectedIndex()), callback);
	}

	private void deleteAvgPeriod(Widget sender) {
		Button button = (Button) sender;
		for (int k = 0; k < avgTable.getRowCount(); k++) {
			if (((Button) (avgTable.getWidget(k, 1))).equals(button)) {
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
				PerifericoUI.getService().deleteAvgPeriod(analyzerId,
						hiddenParamId.getValue(), avgTable.getText(k, 0),
						callback);
			}// end if
		}// end for
	}

	@Override
	protected void loadContent() {
		readElementInfo();
	}

}
