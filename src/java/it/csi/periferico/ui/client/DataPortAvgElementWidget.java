/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: element configuration page for digital analyzers that
 *                   compute mean values
 * Change log:
 *   2015-05-11: initial version
 * ----------------------------------------------------------------------------
 * $Id: DataPortAvgElementWidget.java,v 1.7 2015/05/14 15:35:56 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.data.DigitalAvgElementInfo;
import it.csi.periferico.ui.client.data.ElementInfo;
import it.csi.periferico.ui.client.pagecontrol.AsyncPageOperation;
import it.csi.periferico.ui.client.pagecontrol.PageUpdateAction;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * Widget for dispaly Data Port average element.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class DataPortAvgElementWidget extends ElementPanelWidget {

	private TextBox textNumDec;
	private TextBox textMin;
	private ListBox listAcquisitionMeasureUnit;
	private ListBox listAnalyzerMeasureUnit;
	private TextBox textMax;
	private TextBox textAcqPeriodForAvg;
	private TextBox textRangeLow;
	private TextBox textRangeHigh;
	private TextBox textCorrectionOffset;
	private TextBox textCorrectionCoefficient;
	private TextBox textAcqPeriod;
	private TextBox textLinearizationOffset;
	private TextBox textLinearizationCoefficient;
	private TextBox textAcqDelay;
	private TextBox textAcqDuration;
	private CheckBox cbDiscardNotValidData;
	private Label conversionCoefficientLabel;
	private Label genericTitle;
	private Label prevalidationMinLabel;
	private Label prevalidationMaxLabel;
	private Label rangeLowLabel;
	private Label rangeHighLabel;

	// Grids for avg element information.
	private FlexTable avgGrid = new FlexTable();

	public DataPortAvgElementWidget() {

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
				.makeConfHelpButton("avgElementWidget"));

		externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().element_lbl_title_configuration()
						+ "NUIOVO", panelButtonWidget);

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

		// format the grid for specific element info
		avgGrid.getCellFormatter().setWidth(0, 0, "210px");
		avgGrid.getCellFormatter().setWidth(0, 1, "180px");
		avgGrid.getCellFormatter().setWidth(0, 2, "250px");
		avgGrid.getCellFormatter().setWidth(0, 3, "320px");
		avgGrid.setCellPadding(2);
		avgGrid.setCellSpacing(2);

		// Put values in the specific element cells.
		avgGrid.setText(0, 0, PerifericoUI.getMessages().lbl_enabled());
		avgGrid.setWidget(0, 1, elementEnabled);

		NumericKeyPressHandler negativeKeyboardListener = new NumericKeyPressHandler(
				false);
		NumericKeyPressHandler positiveKeyboardListener = new NumericKeyPressHandler(
				true);

		String message = PerifericoUI.getMessages()
				.element_lbl_acquisition_measureUnit();
		avgGrid.setText(0, 2, message);
		listAcquisitionMeasureUnit = new ListBox();
		listAcquisitionMeasureUnit.setWidth("100px");
		listAcquisitionMeasureUnit.setStyleName("gwt-bg-text-orange");
		listAcquisitionMeasureUnit.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
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
						PerifericoUI.getService().getConversionCoefficient(
								analyzerId, hiddenParamId.getValue(),
								acquisitionSelectedItemText,
								analyzerSelectedItemText, callback);
					}
				}
			}

		});
		avgGrid.setWidget(0, 3, listAcquisitionMeasureUnit);

		message = PerifericoUI.getMessages().element_lbl_analyzer_measureUnit();
		avgGrid.setText(1, 0, message);
		listAnalyzerMeasureUnit = new ListBox();
		listAnalyzerMeasureUnit.setWidth("100px");
		listAnalyzerMeasureUnit.setStyleName("gwt-bg-text-orange");
		listAnalyzerMeasureUnit.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
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
						PerifericoUI.getService().getConversionCoefficient(
								analyzerId, hiddenParamId.getValue(),
								acquisitionSelectedItemText,
								analyzerSelectedItemText, callback);
					}
				}
			}

		});
		avgGrid.setWidget(1, 1, listAnalyzerMeasureUnit);

		message = PerifericoUI.getMessages().element_lbl_conversionCoefficient();
		avgGrid.setText(1, 2, message);
		conversionCoefficientLabel = new Label();
		avgGrid.setWidget(1, 3, conversionCoefficientLabel);

		rangeLowLabel = new Label();
		avgGrid.setWidget(2, 0, rangeLowLabel);
		textRangeLow = new TextBox();
		textRangeLow.setWidth("100px");
		textRangeLow.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(2, 1, textRangeLow);
		textRangeLow.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_numDec();
		avgGrid.setText(2, 2, message);
		textNumDec = new TextBox();
		textNumDec.setWidth("100px");
		textNumDec.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(2, 3, textNumDec);
		textNumDec.addKeyPressHandler(positiveKeyboardListener);

		rangeHighLabel = new Label();
		avgGrid.setWidget(3, 0, rangeHighLabel);
		textRangeHigh = new TextBox();
		textRangeHigh.setWidth("100px");
		textRangeHigh.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(3, 1, textRangeHigh);
		textRangeHigh.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_linearizationCoefficient();
		avgGrid.setText(3, 2, message);
		textLinearizationCoefficient = new TextBox();
		textLinearizationCoefficient.setWidth("100px");
		textLinearizationCoefficient.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(3, 3, textLinearizationCoefficient);
		textLinearizationCoefficient
				.addKeyPressHandler(positiveKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_correctionCoefficient();
		avgGrid.setText(4, 0, message);
		textCorrectionCoefficient = new TextBox();
		textCorrectionCoefficient.setWidth("100px");
		textCorrectionCoefficient.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(4, 1, textCorrectionCoefficient);
		textCorrectionCoefficient.addKeyPressHandler(positiveKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_linearizationOffset();
		avgGrid.setText(4, 2, message);
		textLinearizationOffset = new TextBox();
		textLinearizationOffset.setWidth("100px");
		textLinearizationOffset.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(4, 3, textLinearizationOffset);
		textLinearizationOffset.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_correctionOffset();
		avgGrid.setText(5, 0, message);
		textCorrectionOffset = new TextBox();
		textCorrectionOffset.setWidth("100px");
		textCorrectionOffset.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(5, 1, textCorrectionOffset);
		textCorrectionOffset.addKeyPressHandler(negativeKeyboardListener);

		prevalidationMinLabel = new Label();
		avgGrid.setWidget(5, 2, prevalidationMinLabel);
		textMin = new TextBox();
		textMin.setWidth("100px");
		textMin.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(5, 3, textMin);
		textMin.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().lbl_acqPeriod();
		avgGrid.setText(6, 0, message);
		textAcqPeriod = new TextBox();
		textAcqPeriod.setWidth("100px");
		textAcqPeriod.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(6, 1, textAcqPeriod);
		textAcqPeriod.addKeyPressHandler(positiveKeyboardListener);

		prevalidationMaxLabel = new Label();
		avgGrid.setWidget(6, 2, prevalidationMaxLabel);
		textMax = new TextBox();
		textMax.setWidth("100px");
		textMax.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(6, 3, textMax);
		textMax.addKeyPressHandler(negativeKeyboardListener);

		message = PerifericoUI.getMessages().element_lbl_acq_duration();
		avgGrid.setText(7, 0, message);
		textAcqDuration = new TextBox();
		textAcqDuration.setWidth("100px");
		textAcqDuration.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(7, 1, textAcqDuration);

		message = PerifericoUI.getMessages().element_lbl_avgPeriod();
		avgGrid.setText(7, 2, message);
		textAcqPeriodForAvg = new TextBox();
		textAcqPeriodForAvg.setWidth("150px");
		textAcqPeriodForAvg.setStyleName("gwt-bg-text-orange");
		textAcqPeriodForAvg.addKeyPressHandler(positiveKeyboardListener);
		avgGrid.setWidget(7, 3, textAcqPeriodForAvg);

		message = PerifericoUI.getMessages().element_lbl_acq_delay();
		avgGrid.setText(8, 0, message);
		textAcqDelay = new TextBox();
		textAcqDelay.setWidth("100px");
		textAcqDelay.setStyleName("gwt-bg-text-orange");
		avgGrid.setWidget(8, 1, textAcqDelay);
		textAcqDelay.addKeyPressHandler(positiveKeyboardListener);

		avgGrid.setText(9, 0, PerifericoUI.getMessages().discard_not_valid());
		cbDiscardNotValidData = new CheckBox();
		avgGrid.setWidget(9, 1, cbDiscardNotValidData);

		infoPanel.add(avgGrid);

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
		hPanel.setVisible(true);
		infoPanel.add(hPanel);

		setPanelForCalibration();

		initWidget(externalPanel);

	}// end constructor

	private void verifyElementFields() {
		AsyncCallback<Object> callback = new UIAsyncCallback<Object>() {
			public void onSuccess(Object empty) {
				AsyncCallback<Boolean> callback2 = new UIAsyncCallback<Boolean>() {
					public void onSuccess(Boolean active) {
						Utils.sendVerifyOk();
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
		String selectedAcqMeasureUnit = listAcquisitionMeasureUnit
				.getItemText(indexSelected);
		if (selectedValue1.equals("0") || selectedValue2.equals("0"))
			Window.alert(PerifericoUI.getMessages().error_select_element());
		else {
			try {
				DigitalAvgElementInfo info = setDpaAvgParameter(
						selectedAcqMeasureUnit, selectedAnalyzerMeasureUnit);
				PerifericoUI.getService().setVerifyElementFields(analyzerId,
						info, callback);
			} catch (UserParamsException e) {
				Window.alert(e.getMessage());
			}
		}
	}// end verifyElementFields

	private DigitalAvgElementInfo setDpaAvgParameter(
			String acquisitionMeasureUnit, String analyzerMeasureUnit)
			throws UserParamsException {
		DigitalAvgElementInfo info = new DigitalAvgElementInfo();
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
		info.setAcqDelay(toInt(textAcqDelay.getText()));
		info.setAcqDuration(toInt(textAcqDuration.getText()));
		info.setAvgPeriod(toInt(textAcqPeriodForAvg.getText()));
		info.setAnalyzerMeasureUnit(analyzerMeasureUnit);
		info.setDiscardDataNotValidForAnalyzer(cbDiscardNotValidData
				.isEnabled() ? cbDiscardNotValidData.getValue() : null);
		return info;
	}

	@Override
	protected void dismissContent(AsyncPageOperation asyncPageOperation) {
		int acquisitionIndexSelected = listAcquisitionMeasureUnit
				.getSelectedIndex();
		String acquisitionSelectedValue = listAcquisitionMeasureUnit
				.getValue(acquisitionIndexSelected);
		String acquisitionSelectedItemText = listAcquisitionMeasureUnit
				.getItemText(acquisitionIndexSelected);
		int analyzerIndexSelected = listAnalyzerMeasureUnit.getSelectedIndex();
		String analyzerSelectedValue = listAnalyzerMeasureUnit
				.getValue(analyzerIndexSelected);
		String analyzerSelectedItemText = listAnalyzerMeasureUnit
				.getItemText(analyzerIndexSelected);
		if (acquisitionSelectedValue.equals("0")
				|| analyzerSelectedValue.equals("0"))
			Window.alert(PerifericoUI.getMessages().error_select_element());
		else {
			DigitalAvgElementInfo info;
			try {
				info = setDpaAvgParameter(acquisitionSelectedItemText,
						analyzerSelectedItemText);
			} catch (UserParamsException e) {
				Window.alert(e.getMessage());
				info = new DigitalAvgElementInfo();
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

	void setNewElement(String analyzerId, String analyzerType, String paramId) {
		this.paramsId = null;
		this.currentIndex = null;
		this.analyzerId = analyzerId;
		this.analyzerType = analyzerType;
		this.paramId = paramId;
	}

	private void readElementInfo() {
		AsyncCallback<ElementInfo> callback = new UIAsyncCallback<ElementInfo>() {
			public void onSuccess(ElementInfo elemInfo) {
				if (elemInfo instanceof DigitalAvgElementInfo) {
					setElementInfo((DigitalAvgElementInfo) elemInfo);
				} else {
					throw new IllegalStateException("Unexpected element type: "
							+ elemInfo);
				}
			}// end onSuccess
		};

		PerifericoUI.getService().getElementInfo(analyzerId, paramId, callback);
	}

	private void setElementInfo(DigitalAvgElementInfo info) {

		lblNoCalibration.setVisible(false);
		genericTitle.setText(PerifericoUI.getMessages().lbl_element_generic_title()
				+ " " + info.getParameterId());
		setStatus(info.isEnabled());
		hiddenParamId.setValue(info.getParameterId());
		PerifericoUI.setTitle(externalPanel, info.getAnalyzerName());

		textMax.setText(toStr(info.getMaxValue()));
		textNumDec.setText(toStr(info.getNumDec()));
		textMin.setText(toStr(info.getMinValue()));

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

		textAcqPeriodForAvg.setText(toStr(info.getAvgPeriod()));// TODO verifica
		// campo giusto
		textAcqDelay.setText(toStr(info.getAcqDelay()));
		textAcqPeriod.setText(toStr(info.getAcqPeriod()));
		textCorrectionCoefficient
				.setText(toStr(info.getCorrectionCoefficient()));
		textCorrectionOffset.setText(toStr(info.getCorrectionOffset()));
		textRangeHigh.setText(toStr(info.getRangeHigh()));
		textRangeLow.setText(toStr(info.getRangeLow()));
		textLinearizationCoefficient.setText(toStr(info
				.getLinearizationCoefficient()));
		textLinearizationOffset.setText(toStr(info.getLinearizationOffset()));
		textAcqDuration.setText(toStr(info.getAcqDuration()));

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

	@Override
	protected void loadContent() {
		readElementInfo();
	}

}// end class
