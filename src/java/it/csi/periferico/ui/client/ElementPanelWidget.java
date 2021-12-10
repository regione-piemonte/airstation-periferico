/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: base class for element configuration pages
 * Change log:
 *   2008-12-01: initial version
 * ----------------------------------------------------------------------------
 * $Id: ElementPanelWidget.java,v 1.19 2015/05/13 16:22:11 pfvallosio Exp $
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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * Generic widget for dispaly element.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public abstract class ElementPanelWidget extends UIPage {

	Hidden hiddenParamId;

	private Label textVoltage;

	private Label textReadValue;

	private Label textCorrectValue;

	private TextBox textExpectedValue1;

	private TextBox textExpectedValue2;

	private Label textReadValue1;

	private Label textReadValue2;

	TextBox textCorrectionOffset;

	TextBox textCorrectionCoefficient;

	String analyzerId;

	String analyzerType;

	boolean isCalibrationRunning = false;

	Button elementEnabled;

	private boolean modifyElementEnabled;

	VerticalPanel externalPanel;

	private FlexTable readValue = new FlexTable();

	private FlexTable calibration = new FlexTable();

	private Label calibrationTitle;

	private VerticalPanel calibrationPanel;

	private Button calculateButton;

	private Button acquireButton1;

	private Button acquireButton2;

	Button prevButton = null;

	Button nextButton = null;

	String paramId;

	String[] paramsId;

	Integer currentIndex;

	Label lblNoCalibration;

	void setPanelForCalibration() {
		/*
		 * Calibration support
		 */
		NumericKeyPressHandler negativeKeyboardListener = new NumericKeyPressHandler(
				false);

		// Label and panel for calibration info
		calibrationTitle = new Label();
		calibrationTitle.setText(PerifericoUI.getMessages()
				.lbl_element_calibration_title());
		calibrationTitle.setStyleName("gwt-Label-title");

		// panel that contains the calibration info grid
		calibrationPanel = new VerticalPanel();
		calibrationPanel.setStyleName("gwt-post-boxed");

		externalPanel.add(calibrationTitle);
		externalPanel.add(calibrationPanel);

		// format the grid for read value info
		readValue.getCellFormatter().setWidth(0, 0, "280px");
		readValue.getCellFormatter().setWidth(0, 1, "280px");
		readValue.getCellFormatter().setWidth(0, 2, "280px");
		readValue.getCellFormatter().setWidth(0, 3, "120px");
		readValue.setCellPadding(1);
		readValue.setCellSpacing(1);
		readValue.setWidth("100%");

		HorizontalPanel hPanel1 = new HorizontalPanel();
		hPanel1.setWidth("100%");
		readValue.setWidget(0, 0, hPanel1);

		Label voltage = new Label();
		voltage.setText(PerifericoUI.getMessages().lbl_element_voltage());
		hPanel1.add(voltage);

		textVoltage = new Label();
		hPanel1.add(textVoltage);

		HorizontalPanel hPanel2 = new HorizontalPanel();
		hPanel2.setWidth("100%");
		readValue.setWidget(0, 1, hPanel2);

		final Label readValueLbl = new Label();
		readValueLbl.setText(PerifericoUI.getMessages().lbl_element_read_value()
				+ ":");
		hPanel2.add(readValueLbl);

		textReadValue = new Label();
		hPanel2.add(textReadValue);

		HorizontalPanel hPanel3 = new HorizontalPanel();
		hPanel3.setWidth("100%");
		readValue.setWidget(0, 2, hPanel3);

		final Label correctValue = new Label();
		correctValue.setText(PerifericoUI.getMessages().lbl_element_correct_value()
				+ ":");
		hPanel3.add(correctValue);

		textCorrectValue = new Label();
		hPanel3.add(textCorrectValue);

		final Label readabel1 = new Label();
		final Label readabel2 = new Label();
		final Label expectedValueLabel = new Label();
		final Label expectedValue2Label = new Label();

		Button buttonRead = new Button();
		buttonRead.setStyleName("gwt-button-reload-value");
		buttonRead.setTitle(PerifericoUI.getMessages().read_value());
		buttonRead.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] resultString) {
						if (resultString.length == 1) {
							Window.alert(resultString[0]);

						} else {
							/*
							 * result:
							 * 
							 * [0] : voltage
							 * 
							 * [1] : rawValue
							 * 
							 * [2] : correctedValue
							 * 
							 * [3] : measureUnit
							 */
							textVoltage.setText(resultString[0]);
							textReadValue.setText(resultString[1]);
							textCorrectValue.setText(resultString[2]);
							String measureUnit = " [" + resultString[3] + "]:";
							readValueLbl.setText(PerifericoUI.getMessages()
									.lbl_element_read_value() + measureUnit);
							correctValue.setText(PerifericoUI.getMessages()
									.lbl_element_correct_value() + measureUnit);
							readabel1.setText(PerifericoUI.getMessages()
									.lbl_element_read_value() + measureUnit);
							readabel2.setText(PerifericoUI.getMessages()
									.lbl_element_read_value() + measureUnit);
							expectedValueLabel.setText(PerifericoUI.getMessages()
									.lbl_element_expected_value() + measureUnit);
							expectedValue2Label.setText(PerifericoUI.getMessages()
									.lbl_element_expected_value() + measureUnit);
						}
					}
				};

				perifService.readValueForCalibration(analyzerId,
						hiddenParamId.getValue(), callback);

			}// end onClick

		});
		readValue.setWidget(0, 3, buttonRead);
		readValue.getCellFormatter().setAlignment(1, 3,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);
		calibrationPanel.add(readValue);

		// format the grid for calibration info
		calibration.getCellFormatter().setWidth(0, 0, "100px");
		calibration.getCellFormatter().setWidth(0, 1, "280px");
		calibration.getCellFormatter().setWidth(0, 2, "150px");
		calibration.getCellFormatter().setWidth(0, 3, "300px");
		calibration.getCellFormatter().setWidth(0, 4, "100px");
		calibration.setCellPadding(1);
		calibration.setCellSpacing(1);
		calibration.setWidth("100%");
		calibration.setStyleName("gwt-Grid");

		Label firstLabel = new Label();
		firstLabel.setText(PerifericoUI.getMessages().lbl_element_first());
		calibration.setWidget(0, 0, firstLabel);

		HorizontalPanel hPanel4 = new HorizontalPanel();
		hPanel4.setWidth("100%");
		calibration.setWidget(0, 1, hPanel4);
		calibration.getCellFormatter().setAlignment(0, 1,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);

		expectedValueLabel.setText(PerifericoUI.getMessages()
				.lbl_element_expected_value() + ":");
		hPanel4.add(expectedValueLabel);

		textExpectedValue1 = new TextBox();
		textExpectedValue1.setWidth("100px");
		textExpectedValue1.setStyleName("gwt-bg-text-orange");
		textExpectedValue1.addKeyPressHandler(negativeKeyboardListener);
		hPanel4.add(textExpectedValue1);

		acquireButton1 = new Button();
		if (isCalibrationRunning) {
			acquireButton1.setStyleName("gwt-button-acquire");
			acquireButton1.setEnabled(true);
		} else {
			acquireButton1.setStyleName("gwt-button-acquire-gray");
			acquireButton1.setEnabled(false);
		}
		acquireButton1.setTitle(PerifericoUI.getMessages().lbl_element_acquire());
		acquireButton1.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				String readValue = textReadValue.getText();
				if ("".equals(readValue))
					Window.alert(PerifericoUI.getMessages()
							.alert_read_value_missing());
				textReadValue1.setText(readValue);
			}// end onClick

		});
		calibration.setWidget(0, 2, acquireButton1);
		calibration.getCellFormatter().setAlignment(0, 2,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel hPanel5 = new HorizontalPanel();
		hPanel5.setWidth("100%");
		calibration.setWidget(0, 3, hPanel5);
		calibration.getCellFormatter().setAlignment(0, 3,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);

		readabel1.setText(PerifericoUI.getMessages().lbl_element_read_value() + ":");
		hPanel5.add(readabel1);

		textReadValue1 = new Label();
		hPanel5.add(textReadValue1);

		Label secondLabel = new Label();
		secondLabel.setText(PerifericoUI.getMessages().lbl_element_second());
		calibration.setWidget(1, 0, secondLabel);

		HorizontalPanel hPanel6 = new HorizontalPanel();
		hPanel6.setWidth("100%");
		calibration.setWidget(1, 1, hPanel6);
		calibration.getCellFormatter().setAlignment(1, 1,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);

		expectedValue2Label.setText(PerifericoUI.getMessages()
				.lbl_element_expected_value() + ":");
		hPanel6.add(expectedValue2Label);

		textExpectedValue2 = new TextBox();
		textExpectedValue2.setWidth("100px");
		textExpectedValue2.setStyleName("gwt-bg-text-orange");
		textExpectedValue2.addKeyPressHandler(negativeKeyboardListener);
		hPanel6.add(textExpectedValue2);

		acquireButton2 = new Button();
		if (isCalibrationRunning) {
			acquireButton2.setStyleName("gwt-button-acquire");
			acquireButton2.setEnabled(true);
		} else {
			acquireButton2.setStyleName("gwt-button-acquire-gray");
			acquireButton2.setEnabled(false);
		}
		acquireButton2.setTitle(PerifericoUI.getMessages().lbl_element_acquire());
		acquireButton2.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				String readValue = textReadValue.getText();
				if ("".equals(readValue))
					Window.alert(PerifericoUI.getMessages()
							.alert_read_value_missing());
				textReadValue2.setText(readValue);
			}

		});
		calibration.setWidget(1, 2, acquireButton2);
		calibration.getCellFormatter().setAlignment(1, 2,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel hPanel7 = new HorizontalPanel();
		hPanel7.setWidth("100%");
		calibration.setWidget(1, 3, hPanel7);
		calibration.getCellFormatter().setAlignment(1, 3,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);

		readabel2.setText(PerifericoUI.getMessages().lbl_element_read_value() + ":");
		hPanel7.add(readabel2);

		textReadValue2 = new Label();
		hPanel7.add(textReadValue2);

		calculateButton = new Button();
		if (isCalibrationRunning) {
			calculateButton.setStyleName("gwt-button-calculate");
			calculateButton.setEnabled(true);
		} else {
			calculateButton.setStyleName("gwt-button-calculate-gray");
			calculateButton.setEnabled(false);
		}
		calculateButton.setStyleName("gwt-button-calculate");
		calculateButton.setTitle(PerifericoUI.getMessages().lbl_element_calculate());
		calculateButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				String expectedValue1 = textExpectedValue1.getText();
				String expectedValue2 = textExpectedValue2.getText();
				String readValue1 = textReadValue1.getText();
				String readValue2 = textReadValue2.getText();

				if (!expectedValue1.isEmpty() && readValue1.isEmpty()) {
					Window.alert(PerifericoUI.getMessages()
							.alert_read_value_missing());
					return;
				}
				if (!expectedValue2.isEmpty() && readValue2.isEmpty()) {
					Window.alert(PerifericoUI.getMessages()
							.alert_read_value_missing());
					return;
				}

				PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");

				AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] result) {
						/*
						 * result: [0]=coefficient(m), [1]=offset(q)
						 */
						if (result[0].equals("")) {
							// one point correction
							boolean confirm = Window.confirm(PerifericoUI.getMessages()
									.alert_correction_1_point()
									+ ": "
									+ result[1]
									+ ". "
									+ PerifericoUI.getMessages()
											.alert_save_correction_1_point());
							if (confirm) {
								// insert in configuration
								boolean confirm2 = Window
										.confirm(PerifericoUI.getMessages()
												.alert_use_old_coefficient());
								if (!confirm2) {
									// set m = 1
									textCorrectionCoefficient.setText("1");
								}
								textCorrectionOffset.setText(result[1]);

							}// end if confirm
						} else {
							// Two points correction
							boolean confirm = Window
									.confirm(PerifericoUI.getMessages()
											.alert_correction()
											+ ": "
											+ PerifericoUI.getMessages().alert_m()
											+ " = "
											+ result[0]
											+ ", "
											+ PerifericoUI.getMessages().alert_q()
											+ " = "
											+ result[1]
											+ ". "
											+ PerifericoUI.getMessages()
													.alert_save_correction());
							if (confirm) {
								textCorrectionCoefficient.setText(result[0]);
								textCorrectionOffset.setText(result[1]);
							}// end if confirm
						}// end else
					}// end onSuccess
				};

				perifService.calculate(analyzerId, hiddenParamId.getValue(),
						expectedValue1, expectedValue2, readValue1, readValue2,
						callback);
			}// end onClick

		});
		calibration.setWidget(0, 4, calculateButton);
		calibration.getFlexCellFormatter().setRowSpan(0, 4, 2);
		calibration.getCellFormatter().setAlignment(0, 4,
				HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);

		calibrationPanel.add(calibration);

		lblNoCalibration = new Label();
		lblNoCalibration.setText(PerifericoUI.getMessages().no_calibration());
		externalPanel.add(lblNoCalibration);

		showCalibrationPanel(false);

	}// end setPanelForCalibration

	void showCalibrationPanel(boolean visible) {
		if (visible) {
			lblNoCalibration.setVisible(false);
			calibrationTitle.setVisible(true);
			calibrationPanel.setVisible(true);
		} else {
			calibrationTitle.setVisible(false);
			calibrationPanel.setVisible(false);
			lblNoCalibration.setVisible(true);
		}
	}

	void changeStatus() {
		if (modifyElementEnabled) {
			// case of alarm enabled and must to set to disabled
			elementEnabled.setStyleName("gwt-button-disabled");
			elementEnabled.setTitle(PerifericoUI.getMessages().button_disabled());
			modifyElementEnabled = false;

		} else {
			// case of alarm disabled and must to set to enabled
			elementEnabled.setStyleName("gwt-button-enabled");
			elementEnabled.setTitle(PerifericoUI.getMessages().button_enabled());
			modifyElementEnabled = true;
		}

	}// end changeStatus

	void setStatus(boolean enabled) {
		modifyElementEnabled = !enabled;
		changeStatus();
	}// end changeStatus(enabled)

	void setPanelForCalibrationRunning() {
		if (isCalibrationRunning) {
			acquireButton1.setStyleName("gwt-button-acquire");
			acquireButton1.setEnabled(true);
			acquireButton2.setStyleName("gwt-button-acquire");
			acquireButton2.setEnabled(true);
			calculateButton.setStyleName("gwt-button-calculate");
			calculateButton.setEnabled(true);
		} else {
			acquireButton1.setStyleName("gwt-button-acquire-gray");
			acquireButton1.setEnabled(false);
			acquireButton2.setStyleName("gwt-button-acquire-gray");
			acquireButton2.setEnabled(false);
			calculateButton.setStyleName("gwt-button-calculate-gray");
			calculateButton.setEnabled(false);
		}
	}

	@Override
	protected void reset() {
		if (textVoltage != null)
			textVoltage.setText("");
		if (textReadValue != null)
			textReadValue.setText("");
		if (textCorrectValue != null)
			textCorrectValue.setText("");
		if (textReadValue1 != null)
			textReadValue1.setText("");
		if (textReadValue2 != null)
			textReadValue2.setText("");
		if (textExpectedValue1 != null)
			textExpectedValue1.setText("");
		if (textExpectedValue2 != null)
			textExpectedValue2.setText("");
		boolean nextPrevEnabled = paramsId != null && paramsId.length > 0;
		if (prevButton != null)
			prevButton.setEnabled(nextPrevEnabled && currentIndex > 0);
		if (nextButton != null)
			nextButton.setEnabled(nextPrevEnabled
					&& currentIndex < paramsId.length - 1);
	}

	protected String toStr(Object object) {
		return object == null ? "" : object.toString();
	}

	protected Integer toInt(String value) throws UserParamsException {
		if (value == null || (value = value.trim()).isEmpty())
			return null;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new UserParamsException(
					PerifericoUI.getMessages().err_not_numeric());
		}
	}

	protected Double toDouble(String value) throws UserParamsException {
		if (value == null || (value = value.trim()).isEmpty())
			return null;
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new UserParamsException(
					PerifericoUI.getMessages().err_not_numeric());
		}
	}

}// end class
