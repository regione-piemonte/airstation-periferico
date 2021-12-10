/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: navigation bar
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: NavBar.java,v 1.77 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Define the menu' bar.
 * 
 * @author Isabella Vespa - CSI Piemonte (isabella.vespa@csi.it)
 * 
 */
class NavBar extends Composite {

	private final DockPanel bar = new DockPanel();

	// Create hyperlink to english language
	private final ImageHyperlink linkToEnglish = new ImageHyperlink(new Image(
			"images/flag_en.png"));

	// Create hyperlink to italian language
	private final ImageHyperlink linkToItalian = new ImageHyperlink(new Image(
			"images/flag_it.png"));

	// Create hyperlink to french language
	private final ImageHyperlink linkToFrench = new ImageHyperlink(new Image(
			"images/flag_fr.png"));

	// Create hyperlink to german language
	private final ImageHyperlink linkToGerman = new ImageHyperlink(new Image(
			"images/flag_de.png"));

	// Create hyperlink to station's configuration
	private final ImageHyperlink linkToStationCfg = new ImageHyperlink(
			new Image("images/conf_station.png"));

	// Create hyperlink to alarm's configuration
	private final ImageHyperlink linkToAlarmCfg = new ImageHyperlink(new Image(
			"images/conf_alarms.png"));

	// Create hyperlink to instrument's configuration
	private final ImageHyperlink linkToInstrumentCfg = new ImageHyperlink(
			new Image("images/conf_analyzers.png"));

	// Create hyperlink to board's configuration
	private final ImageHyperlink linkToBoardCfg = new ImageHyperlink(new Image(
			"images/conf_boards.png"));

	// Create hyperlink to board's configuration
	private final ImageHyperlink linkToLoadConf = new ImageHyperlink(new Image(
			"images/network.png"));

	// Create hyperlink to configuration view
	private final ImageHyperlink linkToConfiguration = new ImageHyperlink(
			new Image("images/section_config.png"));

	// Create hyperlink to visualization view
	private final ImageHyperlink linkToView = new ImageHyperlink(new Image(
			"images/section_view.png"));

	// Create hyperlink to station status
	private final ImageHyperlink linkToStationStatus = new ImageHyperlink(
			new Image("images/status_alarms.png"));

	// Create hyperlink to analyzers status
	private final ImageHyperlink linkToAnalyzersStatus = new ImageHyperlink(
			new Image("images/status_analyzers.png"));

	// Create hyperlink to informatic status
	private final ImageHyperlink linkToInformaticStatus = new ImageHyperlink(
			new Image("images/status_application.png"));

	// Create hyperlink to real time data
	private final ImageHyperlink linkToRealTimeData = new ImageHyperlink(
			new Image("images/view_data.png"));

	private final HTML status = new HTML();

	public NavBar() {

		initWidget(bar);

		// hyperlink to italian language
		linkToItalian.setTitle(PerifericoUI.getMessages().lbl_language_italian());
		linkToItalian.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() != null) {
					setLocale("it");
					showLoginWidget();
				}
			}
		}, ClickEvent.getType());

		// hyperlink to english language
		linkToEnglish.setTitle(PerifericoUI.getMessages().lbl_language_english());
		linkToEnglish.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() != null) {
					setLocale("en");
					showLoginWidget();
				}
			}
		}, ClickEvent.getType());

		// hyperlink to french language
		linkToFrench.setTitle(PerifericoUI.getMessages().lbl_language_french());
		linkToFrench.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() != null) {
					setLocale("fr");
					showLoginWidget();
				}
			}
		}, ClickEvent.getType());

		// hyperlink to german language
		linkToGerman.setTitle(PerifericoUI.getMessages().lbl_language_german());
		linkToGerman.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() != null) {
					setLocale("de");
					showLoginWidget();
				}
			}
		}, ClickEvent.getType());

		// hyperlink to station's configuration
		linkToStationCfg.setTitle(PerifericoUI.getMessages().link_station_conf());
		linkToStationCfg.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() == null)
					return;
				PerifericoUI.getSlotConfig().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getStationWidget());
			}
		}, ClickEvent.getType());

		// hyperlink to alarm's configuration
		linkToAlarmCfg.setTitle(PerifericoUI.getMessages().link_alarm_conf());
		linkToAlarmCfg.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() == null)
					return;
				PerifericoUI.getSlotConfig().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getAlarmWidget());
			}
		}, ClickEvent.getType());

		// hyperlink to analyzer's configuration
		linkToInstrumentCfg.setTitle(PerifericoUI.getMessages()
				.link_instrument_conf());
		linkToInstrumentCfg.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() == null)
					return;
				PerifericoUI.getSlotConfig().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getListAnalyzerWidget());
			}
		}, ClickEvent.getType());

		// Create hyperlink to board's configuration
		linkToBoardCfg.setTitle(PerifericoUI.getMessages().link_board_conf());
		linkToBoardCfg.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() == null)
					return;
				PerifericoUI.getSlotConfig().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getListBoardWidget());
			}
		}, ClickEvent.getType());

		// Create hyperlink to load from centrale configuration
		linkToLoadConf.setTitle(PerifericoUI.getMessages().link_load_conf());
		linkToLoadConf.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotConfig() == null)
					return;
				PerifericoUI.getSlotConfig().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getLoadConfWidget());
			}
		}, ClickEvent.getType());

		// Create hyperlink to station status
		linkToStationStatus.setTitle(PerifericoUI.getMessages()
				.link_station_status());
		linkToStationStatus.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotView() == null)
					return;
				PerifericoUI.getSlotView().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getStationStatusWidget());
			}
		}, ClickEvent.getType());

		// Create hyperlink to analyzers status
		linkToAnalyzersStatus.setTitle(PerifericoUI.getMessages()
				.link_analyzers_status());
		linkToAnalyzersStatus.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotView() == null)
					return;
				PerifericoUI.getSlotView().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getAnalyzersStatusWidget());
			}
		}, ClickEvent.getType());

		// Create hyperlink to informatic status
		linkToInformaticStatus.setTitle(PerifericoUI.getMessages()
				.link_informatic_status());
		linkToInformaticStatus.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotView() == null)
					return;
				PerifericoUI.getSlotView().setVisible(true);
				PerifericoUI
						.setCurrentPage(PerifericoUI.getInformaticStatusWidget());
			}
		}, ClickEvent.getType());

		// Create hyperlink to real time data
		linkToRealTimeData
				.setTitle(PerifericoUI.getMessages().link_real_time_data());
		linkToRealTimeData.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (PerifericoUI.getSlotView() == null)
					return;
				PerifericoUI.getSlotView().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getRealTimeDataWidget());
			}
		}, ClickEvent.getType());

		// TODO mettere a posto l'internazionalizzaione
		linkToView.setTitle(PerifericoUI.getMessages().link_view());
		linkToView.setStyleName("right");
		linkToView.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// TODO: capire se chiamare la verifySessionEnded di
				// PerifericoUIServiceImpl oppure no
				setBarForView();
				if (PerifericoUI.getSlotView() == null)
					return;
				PerifericoUI.getSlotView().setVisible(true);
				PerifericoUI
						.setCurrentPage(PerifericoUI.getInformaticStatusWidget());
			}
		}, ClickEvent.getType());

		linkToConfiguration.setTitle(PerifericoUI.getMessages().link_conf());
		linkToConfiguration.setStyleName("right");
		linkToConfiguration.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setBarForConfiguration();
				if (PerifericoUI.getSlotConfig() == null)
					return;
				PerifericoUI.getSlotConfig().setVisible(true);
				PerifericoUI.setCurrentPage(PerifericoUI.getStationWidget());
			}
		}, ClickEvent.getType());

		HorizontalPanel links = new HorizontalPanel();
		links.setSpacing(10);

		links.add(linkToItalian);
		links.add(linkToEnglish);
		// TODO: enable french and german when translation will be completed
		// links.add(linkToFrench);
		// links.add(linkToGerman);
		links.add(linkToStationCfg);
		links.add(linkToAlarmCfg);
		links.add(linkToInstrumentCfg);
		links.add(linkToBoardCfg);
		links.add(linkToLoadConf);
		links.add(linkToInformaticStatus);
		links.add(linkToStationStatus);
		links.add(linkToAnalyzersStatus);
		links.add(linkToRealTimeData);

		bar.add(links, DockPanel.WEST);
		bar.setCellHorizontalAlignment(links, DockPanel.ALIGN_RIGHT);

		HorizontalPanel changeView = new HorizontalPanel();
		changeView.setSpacing(10);
		changeView.add(linkToConfiguration);
		changeView.add(linkToView);

		bar.add(changeView, DockPanel.EAST);
		bar.add(status, DockPanel.CENTER);
		bar.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);
		bar.setCellHorizontalAlignment(status, HasAlignment.ALIGN_RIGHT);
		bar.setCellVerticalAlignment(status, HasAlignment.ALIGN_MIDDLE);
		bar.setCellWidth(status, "100%");

	}

	void setPageTabs() {
		PerifericoUI.getStationWidget().setPageTab(linkToStationCfg);
		PerifericoUI.getAlarmWidget().setPageTab(linkToAlarmCfg);
		PerifericoUI.getListAnalyzerWidget().setPageTab(linkToInstrumentCfg);
		PerifericoUI.getListBoardWidget().setPageTab(linkToBoardCfg);
		PerifericoUI.getLoadConfWidget().setPageTab(linkToLoadConf);
		PerifericoUI.getStationStatusWidget().setPageTab(linkToStationStatus);
		PerifericoUI.getAnalyzersStatusWidget().setPageTab(linkToAnalyzersStatus);
		PerifericoUI.getInformaticStatusWidget().setPageTab(linkToInformaticStatus);
		PerifericoUI.getRealTimeDataWidget().setPageTab(linkToRealTimeData);
	}

	private void setLocale(String locale) {
		PerifericoUI.setLocale(locale);
		Utils.changeLocale(locale);
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<Object> callback = new UIAsyncCallback<Object>() {
			public void onSuccess(Object result) {
			}
		};
		perifService.setLocale(locale, callback);
	}

	void showLoginWidget() {
		if (PerifericoUI.getSlotPage() != null) {
			setBarForLogin();
			PerifericoUI.getSlotPage().setVisible(true);
			PerifericoUI.setCurrentPage(PerifericoUI.getLoginWidget());
		}
	}

	private void setBarForLogin() {
		linkToStationCfg.setVisible(false);
		linkToAlarmCfg.setVisible(false);
		linkToInstrumentCfg.setVisible(false);
		linkToBoardCfg.setVisible(false);
		linkToLoadConf.setVisible(false);
		linkToInformaticStatus.setVisible(false);
		linkToStationStatus.setVisible(false);
		linkToAnalyzersStatus.setVisible(false);
		linkToRealTimeData.setVisible(false);
		linkToConfiguration.setVisible(false);
		linkToView.setVisible(false);
		linkToEnglish.setVisible(!PerifericoUI.getLocale().equals("en"));
		linkToItalian.setVisible(!PerifericoUI.getLocale().equals("it"));
		linkToFrench.setVisible(!PerifericoUI.getLocale().equals("fr"));
		linkToGerman.setVisible(!PerifericoUI.getLocale().equals("de"));
		linkToStationCfg.removeStyleName("active");
		linkToAlarmCfg.removeStyleName("active");
		linkToInstrumentCfg.removeStyleName("active");
		linkToBoardCfg.removeStyleName("active");
		linkToLoadConf.removeStyleName("active");
	}

	void setBarAfterLogin() {
		linkToEnglish.setVisible(false);
		linkToItalian.setVisible(false);
		linkToFrench.setVisible(false);
		linkToGerman.setVisible(false);
		linkToAlarmCfg.setVisible(false);
		linkToBoardCfg.setVisible(false);
		linkToInstrumentCfg.setVisible(false);
		linkToStationCfg.setVisible(false);
		linkToLoadConf.setVisible(false);
		linkToConfiguration.setVisible(true);
		linkToView.setVisible(true);
	}

	private void setBarForConfiguration() {
		linkToEnglish.setVisible(false);
		linkToItalian.setVisible(false);
		linkToFrench.setVisible(false);
		linkToGerman.setVisible(false);
		linkToStationStatus.setVisible(false);
		linkToAnalyzersStatus.setVisible(false);
		linkToInformaticStatus.setVisible(false);
		linkToRealTimeData.setVisible(false);
		linkToAlarmCfg.setVisible(true);
		linkToBoardCfg.setVisible(true);
		linkToInstrumentCfg.setVisible(true);
		linkToStationCfg.setVisible(true);
		linkToLoadConf.setVisible(true);
		linkToConfiguration.setVisible(false);
		linkToView.setVisible(true);
		linkToStationCfg.removeStyleName("active");
		linkToAlarmCfg.removeStyleName("active");
		linkToInstrumentCfg.removeStyleName("active");
		linkToBoardCfg.removeStyleName("active");
		linkToLoadConf.removeStyleName("active");
	}

	void setBarForView() {
		linkToEnglish.setVisible(false);
		linkToItalian.setVisible(false);
		linkToFrench.setVisible(false);
		linkToGerman.setVisible(false);
		linkToAlarmCfg.setVisible(false);
		linkToBoardCfg.setVisible(false);
		linkToInstrumentCfg.setVisible(false);
		linkToStationCfg.setVisible(false);
		linkToLoadConf.setVisible(false);
		linkToConfiguration.setVisible(true);
		linkToView.setVisible(false);
		linkToStationStatus.setVisible(true);
		linkToAnalyzersStatus.setVisible(true);
		linkToInformaticStatus.setVisible(true);
		linkToRealTimeData.setVisible(true);
		linkToStationStatus.removeStyleName("active");
		linkToAnalyzersStatus.removeStyleName("active");
		linkToInformaticStatus.removeStyleName("active");
		linkToRealTimeData.removeStyleName("active");
	}

}// end class
