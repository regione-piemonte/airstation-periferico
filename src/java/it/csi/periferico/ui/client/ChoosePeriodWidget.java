/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: dialog box used to choose time intervals
 * Change log:
 *   2008-06-06: initial version
 * ----------------------------------------------------------------------------
 * $Id: ChoosePeriodWidget.java,v 1.37 2013/08/07 08:01:07 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * This class provides popup for choose date. Dialog box used to choose time
 * intervals
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class ChoosePeriodWidget extends PopupPanel {

	public static final int ALARM_HISTORY = 1;

	public static final int ANALYZER_HISTORY = 2;

	public static final int SAMPLE_HISTORY = 3;

	public static final int MEANS_HISTORY = 4;

	public static final int SAMPLE_DOWNLOAD_CSV = 5;

	public static final int SAMPLE_DOWNLOAD_CHART = 6;

	public static final int MEANS_DOWNLOAD_CSV = 7;

	public static final int MEANS_DOWNLOAD_CHART = 8;

	public static final int WIND_HISTORY = 9;

	public static final String ONE_DAY = "ONE_DAY";

	public static final String ONE_MONTH = "ONE_MONTH";

	public static final String ONE_YEAR = "ONE_YEAR";

	public static final String HALF_DAY = "HALF_DAY";

	private Hidden objectId = new Hidden();

	private Hidden extraObjectId = new Hidden();

	private Hidden description = new Hidden();

	private Hidden maxPeriod = new Hidden();

	private ChoosePeriodWidget choosePeriodPopup;

	private VerticalPanel panel;

	private HorizontalPanel hPanel1;

	private TextBox startDate = new TextBox();

	private TextBox endDate = new TextBox();

	private TextBox startHour = new TextBox();

	private TextBox endHour = new TextBox();

	private ListBox halfDayListBox = new ListBox();

	private ListBox avgPeriodsListBox = new ListBox();

	private String chooseType = null;

	private String measureUnitStr = null;

	private int pageType = 0;

	private Hidden maxDays = null;

	private Button sendVerifyButton;

	private FormPanel formPanel = new FormPanel();

	private CheckBox autoScale = new CheckBox();

	private CheckBox showMinMax = new CheckBox();

	private ListBox fieldSeparator = new ListBox();

	private ListBox decimalSeparator = new ListBox();

	private ListBox evtTypeList = new ListBox();

	private String[] evtArray;

	private boolean choosePanel = true;

	public ChoosePeriodWidget(boolean autoHide, boolean modal, String objectId,
			String extraObjectId, String description, String[] avgPeriods,
			int pageType, String maxPeriod, String measureUnit,
			Integer bckWidget) {
		super(autoHide, modal);
		choosePeriodPopup = this;
		this.objectId.setValue(objectId);
		this.extraObjectId.setValue(extraObjectId);
		this.pageType = pageType;
		this.description.setValue(description);
		this.measureUnitStr = measureUnit;
		if (maxPeriod != null)
			this.maxPeriod.setValue(maxPeriod);

		// panel that contains choose period and buttons
		panel = new VerticalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		startDate.setWidth("100px");
		endDate.setWidth("100px");
		startHour.setWidth("60px");
		endHour.setWidth("60px");
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] resultString) {
				startDate.setText(resultString[0]);
				endDate.setText(resultString[0]);
				startHour.setText("00:00");
				endHour.setText(resultString[1]);
			}
		};
		perifService.getCurrentDate(callback);

		PanelButtonWidget panelButton = new PanelButtonWidget();
		// button cancel
		panelButton.addButton(PerifericoUI
				.makeCancelButtonBlue(new ClickHandler() {
					public void onClick(ClickEvent event) {
						Utils.unlockForPopup("popup");
						choosePeriodPopup.hide();
					}
				}));
		sendVerifyButton = PerifericoUI.makeSubmitButton(null);
		panelButton.addButton(sendVerifyButton);

		// create label and textbox for choosing period
		hPanel1 = new HorizontalPanel();
		// case of sample history
		if (pageType == SAMPLE_HISTORY) {
			Label startDateLabel = new Label(PerifericoUI.getMessages().lbl_day());
			hPanel1.add(startDateLabel);
			hPanel1.add(startDate);
			panel.add(hPanel1);
			// case of means history
		} else if (pageType == MEANS_HISTORY || pageType == MEANS_DOWNLOAD_CSV
				|| pageType == MEANS_DOWNLOAD_CHART || pageType == WIND_HISTORY) {
			avgPeriodsListBox.setVisibleItemCount(1);
			avgPeriodsListBox.setWidth("80px");
			Label avgPeriodsLabel = new Label(
					PerifericoUI.getMessages().lbl_avg_periods());
			for (int i = 0; i < avgPeriods.length; i++) {
				String avgPeriod = avgPeriods[i];
				avgPeriodsListBox.addItem(avgPeriod);
			}

			hPanel1.add(avgPeriodsLabel);
			hPanel1.add(avgPeriodsListBox);
			panel.add(hPanel1);
			// case of download sample for creating csv or chart
		} else if (pageType == SAMPLE_DOWNLOAD_CSV
				|| pageType == SAMPLE_DOWNLOAD_CHART) {

			HorizontalPanel halfDayPanel = new HorizontalPanel();
			if (ONE_DAY.equals(maxPeriod)) {
				Label startDateLabel = new Label(
						PerifericoUI.getMessages().lbl_day());
				hPanel1.add(startDateLabel);
				startDate.setName("reqDate");
				hPanel1.add(startDate);
			} else if (HALF_DAY.equals(maxPeriod)) {
				Label startDateLabel = new Label(
						PerifericoUI.getMessages().lbl_day());
				hPanel1.add(startDateLabel);
				startDate.setName("reqDate");
				hPanel1.add(startDate);
				halfDayPanel.setSpacing(10);
				Label halfDayLabel = new Label(
						PerifericoUI.getMessages().lbl_half_day());
				halfDayListBox.setVisibleItemCount(1);
				halfDayListBox.setWidth("60px");
				halfDayListBox.setName("halfDay");
				halfDayListBox.addItem("0-12", "0");
				halfDayListBox.addItem("12-24", "1");
				halfDayPanel.add(halfDayLabel);
				halfDayPanel.add(halfDayListBox);
			}
			if (pageType == SAMPLE_DOWNLOAD_CSV) {
				formPanel.setAction("./exportcsvService");
				formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
				formPanel.setMethod(FormPanel.METHOD_GET);
				VerticalPanel vPanelForForm = new VerticalPanel();
				vPanelForForm
						.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				vPanelForForm.setSpacing(10);
				formPanel.setWidget(vPanelForForm);
				Hidden localeHidden = new Hidden();
				localeHidden.setName("locale");
				localeHidden.setValue(PerifericoUI.getLocale());
				vPanelForForm.add(localeHidden);
				Hidden functionHidden = new Hidden();
				functionHidden.setName("function");
				functionHidden.setValue("getRealTimeData");
				hPanel1.add(functionHidden);
				Hidden analyzerIdHidden = new Hidden();
				analyzerIdHidden.setName("analyzerIdStr");
				analyzerIdHidden.setValue(objectId);
				hPanel1.add(analyzerIdHidden);
				Hidden elementIdHidden = new Hidden();
				elementIdHidden.setName("elementIdStr");
				elementIdHidden.setValue(extraObjectId);
				hPanel1.add(elementIdHidden);

				vPanelForForm.add(hPanel1);
				vPanelForForm.add(halfDayPanel);
				HorizontalPanel optPanel1 = new HorizontalPanel();
				optPanel1.setSpacing(10);
				Label fieldSeparatorLabel = new Label(
						PerifericoUI.getMessages().lbl_field_separator());
				fieldSeparator.setWidth("40px");
				fieldSeparator.setVisibleItemCount(1);
				fieldSeparator.setName("fieldSeparator");
				fieldSeparator.addItem(",");
				fieldSeparator.addItem(";");
				optPanel1.add(fieldSeparatorLabel);
				optPanel1.add(fieldSeparator);
				vPanelForForm.add(optPanel1);
				HorizontalPanel optPanel2 = new HorizontalPanel();
				optPanel2.setSpacing(10);
				Label decimalSeparatorLabel = new Label(
						PerifericoUI.getMessages().lbl_decimal_separator());
				optPanel2.add(decimalSeparatorLabel);
				decimalSeparator.setWidth("40px");
				decimalSeparator.setVisibleItemCount(1);
				decimalSeparator.setName("decimalSeparator");
				decimalSeparator.addItem(".");
				decimalSeparator.addItem(",");
				optPanel2.add(decimalSeparator);
				vPanelForForm.add(optPanel2);

				formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
					@Override
					public void onSubmitComplete(SubmitCompleteEvent event) {
						String msg = event.getResults()
								.substring(
										"<pre>".length(),
										event.getResults().length()
												- "</pre>".length());
						Window.alert(msg);
					}
				});

				panel.add(formPanel);
			}
			// case of SAMPLE_DOWNLOAD_CHART
			else {
				panel.add(hPanel1);
				panel.add(halfDayPanel);
				HorizontalPanel optPanel1 = new HorizontalPanel();
				Label autoScaleLabel = new Label(
						PerifericoUI.getMessages().lbl_autoscale());
				autoScale.setValue(true);
				optPanel1.add(autoScaleLabel);
				optPanel1.add(autoScale);
				panel.add(optPanel1);
				HorizontalPanel optPanel2 = new HorizontalPanel();
				Label showMinMaxLabel = new Label(
						PerifericoUI.getMessages().lbl_show_minmax());
				optPanel2.add(showMinMaxLabel);
				optPanel2.add(showMinMax);
				panel.add(optPanel2);
			}

			// case of analyzer or alarm status history
		} else {
			Label startDateLabel = new Label(
					PerifericoUI.getMessages().lbl_start_date());
			hPanel1.add(startDateLabel);
			hPanel1.add(startDate);
			Label startHourLabel = new Label(
					PerifericoUI.getMessages().lbl_start_hour());
			hPanel1.add(startHourLabel);
			hPanel1.add(startHour);
			HorizontalPanel hPanel2 = new HorizontalPanel();
			Label endDateLabel = new Label(PerifericoUI.getMessages().lbl_end_date());
			hPanel2.add(endDateLabel);
			hPanel2.add(endDate);
			Label endHourLabel = new Label(PerifericoUI.getMessages().lbl_end_hour());
			hPanel2.add(endHourLabel);
			hPanel2.add(endHour);
			panel.add(hPanel1);
			panel.add(hPanel2);

			// case of analyzer history
			if (pageType == ANALYZER_HISTORY) {
				PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint2 = (ServiceDefTarget) perifService2;
				endpoint2.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				AsyncCallback<String[]> callback2 = new UIAsyncCallback<String[]>() {
					public void onSuccess(String[] evtTypeArray) {
						evtArray = evtTypeArray;
						for (int i = 0; i < evtTypeArray.length; i++) {
							String text;
							String value = evtTypeArray[i];
							if ("fault".equals(value))
								text = PerifericoUI.getMessages()
										.analyzers_status_fault();
							else if ("data_valid".equals(value))
								text = PerifericoUI.getMessages()
										.analyzers_status_data_valid();
							else if ("maintenance".equals(value))
								text = PerifericoUI.getMessages()
										.analyzers_status_maintenance();
							else if ("man_calib".equals(value))
								text = PerifericoUI.getMessages()
										.analyzers_status_calibration_manual();
							else if ("achk_run".equals(value))
								text = PerifericoUI.getMessages()
										.analyzers_status_autocalibration();
							else if ("achk_fail".equals(value))
								text = PerifericoUI.getMessages()
										.analyzers_status_autocalibration_failure();
							else
								text = value;
							evtTypeList.addItem(text, value);
						}
						panel.insert(evtTypeList, 2);
					}
				};

				perifService2.getAnalyzerEvtArray(callback2);
			}
		}

		panel.add(panelButton);
		// button send/verify

		sendVerifyButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				if (choosePeriodPopup.pageType == MEANS_HISTORY
						|| choosePeriodPopup.pageType == MEANS_DOWNLOAD_CSV
						|| choosePeriodPopup.pageType == MEANS_DOWNLOAD_CHART
						|| choosePeriodPopup.pageType == WIND_HISTORY) {

					if (choosePeriodPopup.choosePanel) {
						// show first page of select
						int selectedIndex = avgPeriodsListBox
								.getSelectedIndex();
						int acqPeriod = new Integer(avgPeriodsListBox
								.getValue(selectedIndex)).intValue();
						if (choosePeriodPopup.pageType == MEANS_HISTORY
								|| choosePeriodPopup.pageType == WIND_HISTORY) {
							chooseType = ChoosePeriodWidget.ONE_DAY;
							if (acqPeriod <= 1)
								chooseType = ChoosePeriodWidget.HALF_DAY;
							else if (acqPeriod >= 60 && acqPeriod < 720)
								chooseType = ChoosePeriodWidget.ONE_MONTH;
							else if (acqPeriod >= 720)
								chooseType = ChoosePeriodWidget.ONE_YEAR;
						} else {
							chooseType = ChoosePeriodWidget.ONE_YEAR;
							if (acqPeriod < 60)
								chooseType = ChoosePeriodWidget.ONE_MONTH;
						}

						hPanel1.clear();

						if (ONE_DAY.equals(chooseType)) {
							Label startDateLabel = new Label(
									PerifericoUI.getMessages().lbl_day());
							hPanel1.add(startDateLabel);
							hPanel1.add(startDate);
						} else if (HALF_DAY.equals(chooseType)) {
							Label startDateLabel = new Label(
									PerifericoUI.getMessages().lbl_day());
							hPanel1.add(startDateLabel);
							hPanel1.add(startDate);
							HorizontalPanel halfDayPanel = new HorizontalPanel();
							halfDayPanel.setSpacing(10);
							Label halfDayLabel = new Label(
									PerifericoUI.getMessages().lbl_half_day());
							halfDayListBox.setVisibleItemCount(1);
							halfDayListBox.setWidth("60px");
							halfDayListBox.addItem("0-12", "0");
							halfDayListBox.addItem("12-24", "1");
							halfDayPanel.add(halfDayLabel);
							halfDayPanel.add(halfDayListBox);
							panel.insert(halfDayPanel, 1);
						} else if (ONE_MONTH.equals(chooseType)
								|| ONE_YEAR.equals(chooseType)) {
							Label startDateLabel = new Label(
									PerifericoUI.getMessages().lbl_start_date());
							startDate.setName("startDateStr");
							hPanel1.add(startDateLabel);
							hPanel1.add(startDate);
							Label startHourLabel = new Label(
									PerifericoUI.getMessages().lbl_start_hour());
							startHour.setName("startHourStr");
							hPanel1.add(startHourLabel);
							hPanel1.add(startHour);
							HorizontalPanel hPanel2 = new HorizontalPanel();
							Label endDateLabel = new Label(
									PerifericoUI.getMessages().lbl_end_date());
							endDate.setName("endDateStr");
							hPanel2.add(endDateLabel);
							hPanel2.add(endDate);
							Label endHourLabel = new Label(
									PerifericoUI.getMessages().lbl_end_hour());
							endHour.setName("endHourStr");
							hPanel2.add(endHourLabel);
							hPanel2.add(endHour);
							maxDays = new Hidden();
							maxDays.setName("maxDays");
							if (ONE_MONTH.equals(chooseType))
								maxDays.setValue("31");
							else
								maxDays.setValue("366");
							if (choosePeriodPopup.pageType == MEANS_HISTORY
									|| choosePeriodPopup.pageType == WIND_HISTORY) {
								panel.insert(hPanel2, 1);
								panel.add(maxDays);
							} else {
								if (choosePeriodPopup.pageType == MEANS_DOWNLOAD_CHART) {
									panel.insert(hPanel2, 1);
									panel.add(maxDays);
									HorizontalPanel optPanel1 = new HorizontalPanel();
									Label autoScaleLabel = new Label(
											PerifericoUI.getMessages()
													.lbl_autoscale());
									autoScale.setValue(true);
									optPanel1.add(autoScaleLabel);
									optPanel1.add(autoScale);
									panel.insert(optPanel1, 2);
									HorizontalPanel optPanel2 = new HorizontalPanel();
									Label showMinMaxLabel = new Label(
											PerifericoUI.getMessages()
													.lbl_show_minmax());
									optPanel2.add(showMinMaxLabel);
									optPanel2.add(showMinMax);
									panel.insert(optPanel2, 3);
								}
								if (choosePeriodPopup.pageType == MEANS_DOWNLOAD_CSV) {
									formPanel.setAction("./exportcsvService");
									formPanel
											.setEncoding(FormPanel.ENCODING_MULTIPART);
									formPanel.setMethod(FormPanel.METHOD_GET);
									VerticalPanel vPanelForForm = new VerticalPanel();
									vPanelForForm
											.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
									vPanelForForm.setSpacing(5);
									formPanel.setWidget(vPanelForForm);
									vPanelForForm.add(hPanel1);
									vPanelForForm.add(hPanel2);
									vPanelForForm.add(maxDays);
									Hidden localeHidden = new Hidden();
									localeHidden.setName("locale");
									localeHidden.setValue(PerifericoUI.getLocale());
									vPanelForForm.add(localeHidden);
									Hidden functionHidden = new Hidden();
									functionHidden.setName("function");
									functionHidden.setValue("getMeansData");
									vPanelForForm.add(functionHidden);
									Hidden analyzerIdHidden = new Hidden();
									analyzerIdHidden.setName("analyzerIdStr");
									analyzerIdHidden
											.setValue(choosePeriodPopup.objectId
													.getValue());
									vPanelForForm.add(analyzerIdHidden);
									Hidden elementIdHidden = new Hidden();
									elementIdHidden.setName("elementIdStr");
									elementIdHidden
											.setValue(choosePeriodPopup.extraObjectId
													.getValue());
									vPanelForForm.add(elementIdHidden);
									HorizontalPanel optPanel1 = new HorizontalPanel();
									optPanel1.setSpacing(10);
									Label fieldSeparatorLabel = new Label(
											PerifericoUI.getMessages()
													.lbl_field_separator());
									fieldSeparator.setWidth("40px");
									fieldSeparator.setVisibleItemCount(1);
									fieldSeparator.setName("fieldSeparator");
									fieldSeparator.addItem(",");
									fieldSeparator.addItem(";");
									optPanel1.add(fieldSeparatorLabel);
									optPanel1.add(fieldSeparator);
									vPanelForForm.add(optPanel1);
									HorizontalPanel optPanel2 = new HorizontalPanel();
									optPanel2.setSpacing(10);
									Label decimalSeparatorLabel = new Label(
											PerifericoUI.getMessages()
													.lbl_decimal_separator());
									optPanel2.add(decimalSeparatorLabel);
									decimalSeparator.setWidth("40px");
									decimalSeparator.setVisibleItemCount(1);
									decimalSeparator
											.setName("decimalSeparator");
									decimalSeparator.addItem(".");
									decimalSeparator.addItem(",");
									optPanel2.add(decimalSeparator);
									vPanelForForm.add(optPanel2);
									Hidden periodStrHidden = new Hidden();
									periodStrHidden.setName("periodStr");
									periodStrHidden.setValue(new Integer(
											acqPeriod).toString());
									vPanelForForm.add(periodStrHidden);

									formPanel
											.addSubmitCompleteHandler(new SubmitCompleteHandler() {
												@Override
												public void onSubmitComplete(
														SubmitCompleteEvent event) {
													String msg = event
															.getResults()
															.substring(
																	"<pre>".length(),
																	event.getResults()
																			.length()
																			- "</pre>"
																					.length());
													Window.alert(msg);
												}
											});

									panel.insert(formPanel, 0);
								}
							}
						}
						sendVerifyButton.setVisible(true);
						choosePeriodPopup.choosePanel = false;

					} else {
						// show second page of select
						if (choosePeriodPopup.pageType == MEANS_DOWNLOAD_CSV) {
							formPanel.submit();
							// TODO: capire come fare per nascondere popup
							// perche' se
							// si fa hide non chiama piu' il servlet del csv
						}
						// verify if already choose type
						if (choosePeriodPopup.pageType == MEANS_HISTORY) {
							if (chooseType == null) {
								// TODO: internazionalizzare
								Window.alert("bisogna prima scegliere il tipo");
							}

							Utils.unlockForPopup("popup");
							choosePeriodPopup.hide();
							int selectedHalfDay = choosePeriodPopup.halfDayListBox
									.getSelectedIndex();
							String halfDayStr = (selectedHalfDay != -1) ? choosePeriodPopup.halfDayListBox
									.getValue(selectedHalfDay) : null;
							int selectedAvgPeriod = avgPeriodsListBox
									.getSelectedIndex();
							String[] avgPeriodsStrings = new String[avgPeriodsListBox
									.getItemCount()];
							for (int i = 0; i < avgPeriodsListBox
									.getItemCount(); i++)
								avgPeriodsStrings[i] = avgPeriodsListBox
										.getItemText(i);
							String maxDaysStr = maxDays != null ? maxDays
									.getValue() : null;
							PerifericoUI.getHistoryMeansDataWidget().setFields(
									choosePeriodPopup.objectId.getValue(),
									choosePeriodPopup.extraObjectId.getValue(),
									choosePeriodPopup.avgPeriodsListBox
											.getValue(selectedAvgPeriod),
									avgPeriodsStrings,
									choosePeriodPopup.chooseType, halfDayStr,
									choosePeriodPopup.description.getValue(),
									startDate.getText(), startHour.getText(),
									endDate.getText(), endHour.getText(),
									maxDaysStr);
							PerifericoUI
									.setCurrentPage(PerifericoUI.getHistoryMeansDataWidget());
							choosePeriodPopup.choosePanel = true;
						}// end MEANS_HISTORY
						if (choosePeriodPopup.pageType == WIND_HISTORY) {
							if (chooseType == null) {
								// TODO: internazionalizzare
								Window.alert("bisogna prima scegliere il tipo");
							}

							Utils.unlockForPopup("popup");
							choosePeriodPopup.hide();
							int selectedHalfDay = choosePeriodPopup.halfDayListBox
									.getSelectedIndex();
							String halfDayStr = (selectedHalfDay != -1) ? choosePeriodPopup.halfDayListBox
									.getValue(selectedHalfDay) : null;
							int selectedAvgPeriod = avgPeriodsListBox
									.getSelectedIndex();
							String[] avgPeriodsStrings = new String[avgPeriodsListBox
									.getItemCount()];
							for (int i = 0; i < avgPeriodsListBox
									.getItemCount(); i++)
								avgPeriodsStrings[i] = avgPeriodsListBox
										.getItemText(i);
							String maxDaysStr = maxDays != null ? maxDays
									.getValue() : null;
							PerifericoUI.getHistoryWindDataWidget().setFields(
									choosePeriodPopup.objectId.getValue(),
									choosePeriodPopup.extraObjectId.getValue(),
									choosePeriodPopup.avgPeriodsListBox
											.getValue(selectedAvgPeriod),
									avgPeriodsStrings,
									choosePeriodPopup.chooseType, halfDayStr,
									choosePeriodPopup.description.getValue(),
									startDate.getText(), startHour.getText(),
									endDate.getText(), endHour.getText(),
									maxDaysStr);
							PerifericoUI
									.setCurrentPage(PerifericoUI.getHistoryWindDataWidget());
							choosePeriodPopup.choosePanel = true;
						}// end WIND_HISTORY
						if (choosePeriodPopup.pageType == MEANS_DOWNLOAD_CHART) {

							PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
									.create(PerifericoUIService.class);
							ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
							endpoint.setServiceEntryPoint(GWT
									.getModuleBaseURL() + "uiservice");
							AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
								public void onSuccess(String[] resultString) {
									Utils.unlockForPopup("loading");
									if (new Boolean(resultString[0])
											.booleanValue()) {
										Window.alert(resultString[1]);
										PerifericoUI
												.setCurrentPage(PerifericoUI.getRealTimeDataWidget());
									} else {
										PerifericoUI.getShowChartWidget().title
												.setText(PerifericoUI.getMessages()
														.means_data());
										PerifericoUI.getShowChartWidget()
												.setChartName(resultString[1]);
										PerifericoUI
												.setCurrentPage(PerifericoUI.getShowChartWidget());
									}
								}

								@Override
								public void onFailure(Throwable caught) {
									Utils.unlockForPopup("loading");
									super.onFailure(caught);
								}
							};

							int selectedIndex = avgPeriodsListBox
									.getSelectedIndex();
							int acqPeriod = new Integer(avgPeriodsListBox
									.getValue(selectedIndex)).intValue();
							Utils.unlockForPopup("popup");
							choosePeriodPopup.hide();
							Utils.blockForPopup("loading");
							perifService.createAndGetChartNameOfMeansData(
									choosePeriodPopup.objectId.getValue(),
									choosePeriodPopup.extraObjectId.getValue(),
									startDate.getText(),
									startHour.getText(),
									endDate.getText(),
									endHour.getText(),
									maxDays.getValue(),
									new Integer(acqPeriod).toString(),
									PerifericoUI.getMessages().chart()
											+ choosePeriodPopup.description
													.getValue(),
									choosePeriodPopup.description.getValue()
											+ " - " + acqPeriod + " min",
									measureUnitStr, autoScale.getValue(),
									showMinMax.getValue(), callback);
							choosePeriodPopup.choosePanel = true;
						}// end MEANS_DOWNLOAD_CHART
					}// end else second page of select

				}// end if MEANS_HISTORY || MEANS_DOWNLOAD_CSV ||
					// MEANS_DOWNLOAD_CHART || WIND_HISTORY

				else

				if (choosePeriodPopup.pageType != SAMPLE_DOWNLOAD_CSV
						&& choosePeriodPopup.pageType != MEANS_DOWNLOAD_CSV) {

					Utils.unlockForPopup("popup");
					choosePeriodPopup.hide();
					if (choosePeriodPopup.pageType == ALARM_HISTORY) {
						PerifericoUI.getHistoryStationStatusWidget().setFields(
								choosePeriodPopup.objectId.getValue(),
								choosePeriodPopup.description.getValue(),
								startDate.getText(), startHour.getText(),
								endDate.getText(), endHour.getText());
						PerifericoUI
								.setCurrentPage(PerifericoUI.getHistoryStationStatusWidget());
					}
					if (choosePeriodPopup.pageType == ANALYZER_HISTORY) {
						int evtSelIndex = evtTypeList.getSelectedIndex();
						PerifericoUI.getHistoryAnalyzersStatusWidget().setFields(
								choosePeriodPopup.objectId.getValue(),
								choosePeriodPopup.description.getValue(),
								startDate.getText(), startHour.getText(),
								endDate.getText(), endHour.getText(),
								evtTypeList.getValue(evtSelIndex), evtArray);
						PerifericoUI
								.setCurrentPage(PerifericoUI.getHistoryAnalyzersStatusWidget());
					}
					if (choosePeriodPopup.pageType == SAMPLE_HISTORY) {
						PerifericoUI.getHistoryRealTimeDataWidget().setFields(
								choosePeriodPopup.objectId.getValue(),
								choosePeriodPopup.extraObjectId.getValue(),
								choosePeriodPopup.description.getValue(),
								startDate.getText());
						PerifericoUI
								.setCurrentPage(PerifericoUI.getHistoryRealTimeDataWidget());
					}

					if (choosePeriodPopup.pageType == SAMPLE_DOWNLOAD_CHART) {

						PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
								.create(PerifericoUIService.class);
						ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
						endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
								+ "uiservice");
						AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
							public void onSuccess(String[] resultString) {
								Utils.unlockForPopup("loading");
								if (new Boolean(resultString[0]).booleanValue()) {
									Window.alert(resultString[1]);
									PerifericoUI
											.setCurrentPage(PerifericoUI.getRealTimeDataWidget());
								} else {
									PerifericoUI.getShowChartWidget().title
											.setText(PerifericoUI.getMessages()
													.real_time_data());
									PerifericoUI.getShowChartWidget()
											.setChartName(resultString[1]);
									PerifericoUI
											.setCurrentPage(PerifericoUI.getShowChartWidget());
								}
							}

							@Override
							public void onFailure(Throwable caught) {
								Utils.unlockForPopup("loading");
								super.onFailure(caught);
							}
						};

						String startHourStr = "0";
						String endHourStr = "24";
						if (halfDayListBox.getItemCount() > 0) {
							int halfDaySelected = choosePeriodPopup.halfDayListBox
									.getSelectedIndex();
							String halfDayStr = choosePeriodPopup.halfDayListBox
									.getValue(halfDaySelected);
							if ("0".equals(halfDayStr))
								endHourStr = "12";
							else if ("1".equals(halfDayStr))
								startHourStr = "12";
						}
						Utils.unlockForPopup("popup");
						choosePeriodPopup.hide();
						Utils.blockForPopup("loading");
						perifService.createAndGetChartNameOfRealTimeData(
								choosePeriodPopup.objectId.getValue(),
								choosePeriodPopup.extraObjectId.getValue(),
								startDate.getText(),
								startHourStr,
								endHourStr,
								PerifericoUI.getMessages().chart()
										+ choosePeriodPopup.description
												.getValue(),
								choosePeriodPopup.description.getValue(),
								measureUnitStr, autoScale.getValue(),
								showMinMax.getValue(), callback);
					}

				} else {
					formPanel.submit();
					// TODO: capire come fare per nascondere popup perche' se
					// si fa hide non chiama piu' il servlet del csv
				}

			}
		});

		/*
		 * if (pageType == MEANS_HISTORY || pageType == MEANS_DOWNLOAD_CSV ||
		 * pageType == MEANS_DOWNLOAD_CHART || pageType == WIND_HISTORY)
		 * sendVerifyButton.setVisible(false);
		 */

		this.add(panel);
		this.show();

	}// end constructor

	public void show() {
		super.show();

		int cWidth = Window.getClientWidth();
		int cHeight = Window.getClientHeight();
		// int myWidth = getOffsetWidth();
		// int myHeight = getOffsetHeight();
		// Utils.alert("clientwidth:"+cWidth+" clientHeight:"+cHeight+"
		// offsetwidth:"+myWidth+" offsetHeight:"+myHeight);
		// setPopupPosition((cWidth-myWidth)/2,(cHeight-myHeight)/2);
		setPopupPosition(((cWidth / 2) - 200), ((cHeight / 2) - 100));
		setWidth("500px");
		setHeight("200px");
		setStyleName("gwt-popup-panel-blue");
		// DOM.setStyleAttribute(getElement(), "border", " 1px solid #FF8D17");
	}

}
