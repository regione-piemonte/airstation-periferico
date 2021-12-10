/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: the entry point of the user interface
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: PerifericoUI.java,v 1.90 2015/05/11 09:46:20 vespa Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.PageController;
import it.csi.periferico.ui.client.pagecontrol.PageUpdateAction;
import it.csi.periferico.ui.client.pagecontrol.UIPage;
import it.csi.periferico.ui.client.pagecontrol.UserAction;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * @author Isabella Vespa - CSI Piemonte (isabella.vespa@csi.it)
 * 
 */
public class PerifericoUI implements EntryPoint {

	public static final String SESSION_ENDED = "session_ended";

	public static final Integer REAL_TIME_DATA_WIDGET = new Integer(90);

	public static final Integer HISTORY_REAL_TIME_DATA_WIDGET = new Integer(91);

	public static final Integer HISTORY_MEANS_DATA_WIDGET = new Integer(92);

	public static final Integer STATION_STATUS_WIDGET = new Integer(93);

	public static final Integer HISTORY_STATION_STATUS_WIDGET = new Integer(94);

	public static final Integer HISTORY_ANALYZER_STATUS_WIDGET = new Integer(95);

	public static final Integer ANALYZER_STATUS_WIDGET = new Integer(96);

	public static final Integer HISTORY_WIND_DATA_WIDGET = new Integer(97);

	private static NavBar navBar;

	private static LoginWidget loginWidget;

	private static StationWidget stationWidget;

	private static LoadConfWidget loadConfWidget;

	private static AlarmWidget alarmWidget;

	private static ListAnalyzerWidget listAnalyzerWidget;

	private static ListElementWidget listElementWidget;

	private static SampleElementWidget sampleElementWidget;

	private static WindElementWidget windElementWidget;

	private static AvgElementWidget avgElementWidget;

	private static RainElementWidget rainElementWidget;

	private static DataPortElementWidget dataPortElementWidget;

	private static DataPortAvgElementWidget dataPortAvgElementWidget;

	private static AnalyzerWidget analyzerWidget;

	private static ListBoardWidget listBoardWidget;

	private static BoardWidget boardWidget;

	private static SubdeviceWidget subdeviceWidget;

	private static StationStatusWidget stationStatusWidget;

	private static HistoryStationStatusWidget historyStationStatusWidget;

	private static AnalyzersStatusWidget analyzersStatusWidget;

	private static HistoryAnalyzersStatusWidget historyAnalyzersStatusWidget;

	private static InformaticStatusWidget informaticStatusWidget;

	private static RealTimeDataWidget realTimeDataWidget;

	private static DgtAnalyzerWidget dgtAnalyzerWidget;

	private static HistoryRealTimeDataWidget historyRealTimeDataWidget;

	private static HistoryMeansDataWidget historyMeansDataWidget;

	private static HistoryWindDataWidget historyWindDataWidget;

	private static ShowChartWidget showChartWidget;

	private static RootPanel slotConfig = RootPanel.get("config-box");

	private static RootPanel slotView = RootPanel.get("view-box");

	private static RootPanel slotPage = RootPanel.get("login");

	private static String locale;

	private static MessageBundleClient messages = null;

	private static boolean sessionDialogActive = false;

	private static PageController pageController = new PageController();

	private static ClickHandler saveClickListener;

	private static ClickHandler reloadClickListener;

	private static ClickHandler refreshClickListener;

	private static ClickHandler upClickListener;

	private static ClickHandler backClickListener;

	public PerifericoUI() {

		// Define generally used ClickListeners

		final UserAction saveAction = new UserAction() {
			public void action() {
				Utils.blockForPopup("popup");
				new SaveWidget(false, true);
			}
		};

		saveClickListener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pageController.doActionOnUnmodifiedPage(
						messages.alert_save_cfg_when_page_is_modified(),
						saveAction);
			}
		};

		reloadClickListener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!Window.confirm(messages.alert_about_to_reload_cfg()))
					return;
				final PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
						.create(PerifericoUIService.class);
				ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
				endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
						+ "uiservice");
				final AsyncCallback<Object> callback = new UIAsyncCallback<Object>() {
					public void onSuccess(Object result) {
						Window.alert(messages.alert_reload());
						pageController.goToTopLevelPage(true);
						AsyncCallback<String> acb_getStationName = new UIAsyncCallback<String>() {
							public void onSuccess(String stationName) {
								Window.setTitle(messages.lbl_station() + ": "
										+ stationName);
							}
						};

						perifService.getStationName(acb_getStationName);
					}
				};

				AsyncCallback<Boolean> acb_verifySessionEnded = new UIAsyncCallback<Boolean>() {
					public void onSuccess(Boolean sessionEnded) {
						if (sessionEnded)
							sessionEnded();
						else
							perifService.getConfig(locale, callback);
					}
				};
				perifService.verifySessionEnded(acb_verifySessionEnded);
			}
		};

		refreshClickListener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateCurrentPage(null);
			}
		};

		upClickListener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PerifericoUI.goToUpperLevelPage();
			}
		};

		backClickListener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PerifericoUI.goToPreviousPage();
			}
		};

	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		// Load internationalization
		messages = (MessageBundleClient) GWT.create(MessageBundleClient.class);
		locale = messages.locale();

		// Define menu' bar
		navBar = new NavBar();
		navBar.setVisible(true);

		// Create loginWidget
		loginWidget = new LoginWidget();
		loginWidget.setVisible(false);
		slotPage.add(loginWidget);

		// Create stationWidget and set it to invisible
		stationWidget = new StationWidget();
		stationWidget.setVisible(false);
		slotConfig.add(stationWidget);

		// Create loadConfWidget and set it to invisible
		loadConfWidget = new LoadConfWidget();
		loadConfWidget.setVisible(false);
		slotConfig.add(loadConfWidget);

		// Create alarmWidget and set it to invisible
		alarmWidget = new AlarmWidget();
		alarmWidget.setVisible(false);
		slotConfig.add(alarmWidget);

		// Create listAnalyzerWidget and set it to invisible
		listAnalyzerWidget = new ListAnalyzerWidget();
		listAnalyzerWidget.setVisible(false);
		slotConfig.add(listAnalyzerWidget);

		// Create listElementWidget and set it to invisible
		listElementWidget = new ListElementWidget();
		listElementWidget.setVisible(false);
		slotConfig.add(listElementWidget);

		// Create sampleElementWidget and set it to invisible
		sampleElementWidget = new SampleElementWidget();
		sampleElementWidget.setVisible(false);
		slotConfig.add(sampleElementWidget);

		// Create windElementWidget and set it to invisible
		windElementWidget = new WindElementWidget();
		windElementWidget.setVisible(false);
		slotConfig.add(windElementWidget);

		// Create avgElementWidget and set it to invisible
		avgElementWidget = new AvgElementWidget();
		avgElementWidget.setVisible(false);
		slotConfig.add(avgElementWidget);

		// Create rainElementWidget and set it to invisible
		rainElementWidget = new RainElementWidget();
		rainElementWidget.setVisible(false);
		slotConfig.add(rainElementWidget);

		// Create dataPortElementWidget and set it to invisible
		dataPortElementWidget = new DataPortElementWidget();
		dataPortElementWidget.setVisible(false);
		slotConfig.add(dataPortElementWidget);

		// Create dataPortAvgElementWidget and set it to invisible
		dataPortAvgElementWidget = new DataPortAvgElementWidget();
		dataPortAvgElementWidget.setVisible(false);
		slotConfig.add(dataPortAvgElementWidget);

		// Create analyzerWidget and set it to invisible
		analyzerWidget = new AnalyzerWidget();
		analyzerWidget.setVisible(false);
		slotConfig.add(analyzerWidget);

		// Create listBoardWidget and set it to invisible
		listBoardWidget = new ListBoardWidget();
		listBoardWidget.setVisible(false);
		slotConfig.add(listBoardWidget);

		// Create boardWidget and set it to invisible
		boardWidget = new BoardWidget();
		boardWidget.setVisible(false);
		slotConfig.add(boardWidget);

		// Create boardWidget and set it to invisible
		subdeviceWidget = new SubdeviceWidget();
		subdeviceWidget.setVisible(false);
		slotConfig.add(subdeviceWidget);

		// Create stationStatusWidget and set it to invisible
		stationStatusWidget = new StationStatusWidget();
		stationStatusWidget.setVisible(false);
		slotView.add(stationStatusWidget);

		// Create stationStatusWidget and set it to invisible
		historyStationStatusWidget = new HistoryStationStatusWidget();
		historyStationStatusWidget.setVisible(false);
		slotView.add(historyStationStatusWidget);

		// Create analyzersStatusWidget and set it to invisible
		analyzersStatusWidget = new AnalyzersStatusWidget();
		analyzersStatusWidget.setVisible(false);
		slotView.add(analyzersStatusWidget);

		// Create historyAnalyzersStatusWidget and set it to invisible
		historyAnalyzersStatusWidget = new HistoryAnalyzersStatusWidget();
		historyAnalyzersStatusWidget.setVisible(false);
		slotView.add(historyAnalyzersStatusWidget);

		// Create informaticStatusWidget and set it to invisible
		informaticStatusWidget = new InformaticStatusWidget();
		informaticStatusWidget.setVisible(false);
		slotView.add(informaticStatusWidget);

		// Create realTimeDataWidget and set it to invisible
		realTimeDataWidget = new RealTimeDataWidget();
		realTimeDataWidget.setVisible(false);
		slotView.add(realTimeDataWidget);

		// Create dgtAnalyzerWidget and set it to invisible
		dgtAnalyzerWidget = new DgtAnalyzerWidget();
		dgtAnalyzerWidget.setVisible(false);
		slotView.add(dgtAnalyzerWidget);

		// Create historyRealTimeDataWidget and set it to invisible
		historyRealTimeDataWidget = new HistoryRealTimeDataWidget();
		historyRealTimeDataWidget.setVisible(false);
		slotView.add(historyRealTimeDataWidget);

		// Create historyMeansDataWidget and set it to invisible
		historyMeansDataWidget = new HistoryMeansDataWidget();
		historyMeansDataWidget.setVisible(false);
		slotView.add(historyMeansDataWidget);

		// Create historyWindDataWidget and set it to invisible
		historyWindDataWidget = new HistoryWindDataWidget();
		historyWindDataWidget.setVisible(false);
		slotView.add(historyWindDataWidget);

		// Create showChartWidget and set it to invisible
		showChartWidget = new ShowChartWidget();
		showChartWidget.setVisible(false);
		slotView.add(showChartWidget);

		/*
		 * WARNING! when create a new widget you must set visible/invisible
		 * widget and then add it to panel
		 */

		analyzerWidget.setUpperLevelPage(listAnalyzerWidget);
		dgtAnalyzerWidget.setUpperLevelPage(analyzersStatusWidget);
		listElementWidget.setUpperLevelPage(analyzerWidget);
		sampleElementWidget.setUpperLevelPage(listElementWidget);
		windElementWidget.setUpperLevelPage(analyzerWidget);
		avgElementWidget.setUpperLevelPage(listElementWidget);
		rainElementWidget.setUpperLevelPage(analyzerWidget);
		dataPortElementWidget.setUpperLevelPage(listElementWidget);
		dataPortAvgElementWidget.setUpperLevelPage(listElementWidget);
		boardWidget.setUpperLevelPage(listBoardWidget);
		subdeviceWidget.setUpperLevelPage(boardWidget);
		historyStationStatusWidget.setUpperLevelPage(stationStatusWidget);
		historyAnalyzersStatusWidget.setUpperLevelPage(analyzersStatusWidget);
		historyRealTimeDataWidget.setUpperLevelPage(realTimeDataWidget);
		historyMeansDataWidget.setUpperLevelPage(realTimeDataWidget);
		historyWindDataWidget.setUpperLevelPage(realTimeDataWidget);
		showChartWidget.setUpperLevelPage(realTimeDataWidget);

		navBar.setPageTabs();
		navBar.showLoginWidget();

		RootPanel.get("menu").add(navBar);

	}// end onModuleLoad

	/**
	 * Create and prepare title for the external panel page
	 * 
	 * @param title
	 *            the title of the page
	 * @param messages
	 *            the messageBundle
	 * @return a verticaPanel that can contains elemnt for the page
	 */
	public static VerticalPanel getTitledExternalPanel(String title,
			PanelButtonWidget panelButton) {
		// panel that rapresents the page
		VerticalPanel externalPanel = new VerticalPanel();
		externalPanel.setStyleName("gwt-page");

		// page title and application button
		Label pageTitle = new Label(title);
		pageTitle.setStyleName("gwt-title-widget");
		HorizontalPanel topPanel = new HorizontalPanel();
		topPanel.add(pageTitle);
		topPanel.setCellWidth(pageTitle, "320px");
		topPanel.setCellHorizontalAlignment(pageTitle,
				HasHorizontalAlignment.ALIGN_LEFT);
		topPanel.setCellVerticalAlignment(pageTitle,
				HasVerticalAlignment.ALIGN_MIDDLE);
		if (panelButton != null) {
			topPanel.add(panelButton);
			topPanel.setCellWidth(panelButton, "680px");
			topPanel.setCellHorizontalAlignment(panelButton,
					HasHorizontalAlignment.ALIGN_RIGHT);
		}
		externalPanel.add(topPanel);
		return externalPanel;
	}

	public static void setTitle(VerticalPanel externalPanel, String title) {
		HorizontalPanel topPanel = (HorizontalPanel) externalPanel.getWidget(0);
		Label titleLabel = (Label) topPanel.getWidget(0);
		titleLabel.setText(title);
	}

	public static void sessionEnded() {
		if (sessionDialogActive)
			return;
		sessionDialogActive = true;
		Window.alert(PerifericoUI.messages.session_ended());
		pageController.setCurrentPage(null, true);
		navBar.showLoginWidget();
		sessionDialogActive = false;
	}

	public static void unexpectedError(Throwable caught) {
		caught.printStackTrace();
		Window.alert(PerifericoUI.messages.unexpected_server_error() + " ("
				+ caught.toString() + ")");
	}

	public static PerifericoUIServiceAsync getService() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		return perifService;
	}

	static void goToUpperLevelPage() {
		pageController.goToUpperLevelPage();
	}

	static void goToTopLevelPage() {
		pageController.goToTopLevelPage();
	}

	static void goToPreviousPage() {
		pageController.goToPreviousPage();
	}

	static void setCurrentPage(UIPage page) {
		pageController.setCurrentPage(page);
	}

	static void updateCurrentPage(PageUpdateAction pageUpdateAction) {
		pageController.updateCurrentPage(pageUpdateAction);
	}

	static Button makeSaveButton() {
		return new StyleButton("gwt-button-save", messages.button_save(),
				saveClickListener);
	}

	static Button makeReloadButton() {
		return new StyleButton("gwt-button-reload", messages.button_reload(),
				reloadClickListener);
	}

	static Button makeConfHelpButton(final String helpAnchor) {
		return new StyleButton("gwt-button-help", messages.button_help(),
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.open("./help/" + locale + "/index.html#conf_"
								+ helpAnchor, "", "");
					}
				});
	}

	static Button makeViewHelpButton(final String helpAnchor) {
		return new StyleButton("gwt-button-help-blue", messages.button_help(),
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.open("./help/" + locale + "/index.html#view_"
								+ helpAnchor, "", "");
					}
				});
	}

	static Button makeNewButton(ClickHandler handler) {
		return new StyleButton("gwt-button-new-orange", messages.button_new(),
				handler);
	}

	static Button makeBackButton() {
		return new StyleButton("gwt-button-back", messages.button_back(),
				backClickListener);
	}

	static Button makeBackButtonBlue() {
		return new StyleButton("gwt-button-back-blue", messages.button_back(),
				backClickListener);
	}

	static Button makeForwardButton(ClickHandler handler) {
		return new MultiStyleButton("gwt-button-forward", "gwt-forward-gray",
				messages.button_forward(), handler);
	}

	static Button makePrevButton(ClickHandler handler) {
		return new MultiStyleButton("gwt-button-back", "gwt-prev-gray",
				messages.button_prev(), handler);
	}

	static Button makeUpButton() {
		return new StyleButton("gwt-button-up", messages.button_back(),
				upClickListener);
	}

	static Button makeUpButtonBlue() {
		return new StyleButton("gwt-button-up-blue", messages.button_back(),
				upClickListener);
	}

	static Button makeRefreshButton() {
		return new StyleButton("gwt-button-reload-blue",
				messages.button_refresh(), refreshClickListener);
	}

	static Button makeListElementButton(ClickHandler handler) {
		return new StyleButton("gwt-button-list-element",
				messages.button_list_element(), handler);
	}

	static Button makeSendVerifyButton(ClickHandler handler) {
		return new StyleButton("gwt-button-send-verify",
				messages.button_send_verify(), handler);
	}

	static Button makeReloadDisplayButton(ClickHandler handler) {
		return new StyleButton("gwt-button-reload-display",
				messages.reload_display(), handler);
	}

	static Button makeUndoButton(ClickHandler handler) {
		return new StyleButton("gwt-button-undo", messages.button_undo(),
				handler);
	}

	static Button makeSubmitButton(ClickHandler handler) {
		return new StyleButton("gwt-button-send-verify-blue",
				messages.button_send_verify(), handler);
	}

	static Button makeExecButton(ClickHandler handler) {
		return new StyleButton("gwt-button-send-verify-blue",
				messages.button_exec_button(), handler);
	}

	static Button makeCancelButtonBlue(ClickHandler handler) {
		return new StyleButton("gwt-button-undo-blue", messages.button_undo(),
				handler);
	}

	static Button makeDeleteButton(ClickHandler handler) {
		return new StyleButton("gwt-button-del", messages.lbl_delete(), handler);
	}

	static Button makeDeleteSmallButton(ClickHandler handler) {
		return new StyleButton("gwt-button-delete-small",
				messages.lbl_delete(), handler);
	}

	static Button makeCsvButton(ClickHandler handler) {
		return new StyleButton("gwt-button-csv", messages.lbl_csv(), handler);
	}

	static Button makeChartButton(ClickHandler handler) {
		return new StyleButton("gwt-button-chart", messages.chart_title(),
				handler);
	}

	static Button makeTableSmallButton(ClickHandler handler) {
		return new StyleButton("gwt-button-table-small", messages.lbl_table(),
				handler);
	}

	static Button makeCsvSmallButton(ClickHandler handler) {
		return new StyleButton("gwt-button-csv-small", messages.lbl_csv(),
				handler);
	}

	static Button makeChartSmallButton(ClickHandler handler) {
		return new StyleButton("gwt-button-chart-small",
				messages.chart_title(), handler);
	}

	static Button makeViewDeletedAnalyzersButton(ClickHandler handler) {
		return new StyleButton("gwt-button-view-deleted-analyzers",
				messages.view_deleted_analyzers(), handler);
	}

	static PanelButtonWidget makeUndoSendPanelButton(ClickHandler undoHandler,
			ClickHandler submitHandler) {
		PanelButtonWidget panelButton = new PanelButtonWidget();
		panelButton.addButton(makeUndoButton(undoHandler));
		panelButton.addButton(makeSendVerifyButton(submitHandler));
		return panelButton;
	}

	static Button makeResetButton(ClickHandler handler) {
		return new StyleButton("gwt-button-send-verify-blue",
				messages.button_reset(), handler);
	}

	public static Button makeBlueButton(ClickHandler handler, String label) {
		Button button = new StyleButton("gwt-button-blue", null, handler);
		button.setText(label);
		return button;
	}

	public static String getLocale() {
		return locale;
	}

	public static void setLocale(String locale) {
		PerifericoUI.locale = locale;
	}

	public static NavBar getNavBar() {
		return navBar;
	}

	public static LoginWidget getLoginWidget() {
		return loginWidget;
	}

	public static StationWidget getStationWidget() {
		return stationWidget;
	}

	public static LoadConfWidget getLoadConfWidget() {
		return loadConfWidget;
	}

	public static AlarmWidget getAlarmWidget() {
		return alarmWidget;
	}

	public static ListAnalyzerWidget getListAnalyzerWidget() {
		return listAnalyzerWidget;
	}

	public static ListElementWidget getListElementWidget() {
		return listElementWidget;
	}

	public static SampleElementWidget getSampleElementWidget() {
		return sampleElementWidget;
	}

	public static WindElementWidget getWindElementWidget() {
		return windElementWidget;
	}

	public static AvgElementWidget getAvgElementWidget() {
		return avgElementWidget;
	}

	public static RainElementWidget getRainElementWidget() {
		return rainElementWidget;
	}

	public static DataPortElementWidget getDataPortElementWidget() {
		return dataPortElementWidget;
	}

	public static DataPortAvgElementWidget getDataPortAvgElementWidget() {
		return dataPortAvgElementWidget;
	}

	public static AnalyzerWidget getAnalyzerWidget() {
		return analyzerWidget;
	}

	public static ListBoardWidget getListBoardWidget() {
		return listBoardWidget;
	}

	public static BoardWidget getBoardWidget() {
		return boardWidget;
	}

	public static SubdeviceWidget getSubdeviceWidget() {
		return subdeviceWidget;
	}

	public static StationStatusWidget getStationStatusWidget() {
		return stationStatusWidget;
	}

	public static HistoryStationStatusWidget getHistoryStationStatusWidget() {
		return historyStationStatusWidget;
	}

	public static AnalyzersStatusWidget getAnalyzersStatusWidget() {
		return analyzersStatusWidget;
	}

	public static HistoryAnalyzersStatusWidget getHistoryAnalyzersStatusWidget() {
		return historyAnalyzersStatusWidget;
	}

	public static InformaticStatusWidget getInformaticStatusWidget() {
		return informaticStatusWidget;
	}

	public static RealTimeDataWidget getRealTimeDataWidget() {
		return realTimeDataWidget;
	}

	public static DgtAnalyzerWidget getDgtAnalyzerWidget() {
		return dgtAnalyzerWidget;
	}

	public static HistoryRealTimeDataWidget getHistoryRealTimeDataWidget() {
		return historyRealTimeDataWidget;
	}

	public static HistoryMeansDataWidget getHistoryMeansDataWidget() {
		return historyMeansDataWidget;
	}

	public static HistoryWindDataWidget getHistoryWindDataWidget() {
		return historyWindDataWidget;
	}

	public static ShowChartWidget getShowChartWidget() {
		return showChartWidget;
	}

	public static RootPanel getSlotConfig() {
		return slotConfig;
	}

	public static RootPanel getSlotView() {
		return slotView;
	}

	public static RootPanel getSlotPage() {
		return slotPage;
	}

	public static MessageBundleClient getMessages() {
		return messages;
	}

}// end class
