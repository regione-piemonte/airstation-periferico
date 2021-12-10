/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: page that shows the informatic status of the application
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: InformaticStatusWidget.java,v 1.34 2015/10/26 17:24:40 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.pagecontrol.UIPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * Widget that display application status.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class InformaticStatusWidget extends UIPage {

	// private static final String GPS_APP_ERROR = "GPS_APP_ERROR";

	private static final String GPS_READ_ERROR = "GPS_READ_ERROR";

	private static final String NO_FIX = "NO_FIX";

	private static final String FIX_2D = "FIX_2D";

	private static final String FIX_3D = "FIX_3D";

	private Label title = new Label();

	// Grids for global information.
	private FlexTable globalStatusGrid = new FlexTable();

	// Grids for confBoard information.
	private FlexTable confBoardGrid = new FlexTable();

	// Grids for boardInitialize information.
	private FlexTable boardInitializeStatusGrid = new FlexTable();

	// Grids for dpa analyzer status information.
	private FlexTable dpaStatusGrid = new FlexTable();

	// Grids for configuration information.
	private FlexTable configurationStatusGrid = new FlexTable();
	private FlexTable configurationStatusSubGrid = new FlexTable();

	// Grids for file system information.
	private FlexTable fileSystemStatusGrid = new FlexTable();

	// Grids for gps information.
	private FlexTable gpsInfoGrid = new FlexTable();

	// Grids for gps status information.
	private FlexTable gpsStatusGrid = new FlexTable();

	// Grids for gps position information.
	private FlexTable gpsPositionGrid = new FlexTable();

	private IconImageBundle iconImageBundle;

	public InformaticStatusWidget() {

		iconImageBundle = (IconImageBundle) GWT.create(IconImageBundle.class);
		PanelButtonWidget panelButtonRefresh = new PanelButtonWidget();
		panelButtonRefresh.addButton(PerifericoUI.makeRefreshButton());
		panelButtonRefresh.addButton(PerifericoUI
				.makeViewHelpButton("informaticStatusWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().informatic_status_title(),
				panelButtonRefresh);

		// Label and panel for application status info
		title.setStyleName("gwt-Label-title-blue");
		title.setText(PerifericoUI.getMessages().informatic_status_application());

		// panel that contains application info
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		panel.add(globalStatusGrid);

		panel.add(confBoardGrid);

		// title for initialize boards
		Label boardTitle = new Label();
		boardTitle.setText(PerifericoUI.getMessages().informatic_board_title());
		boardTitle.setStyleName("gwt-informatic-title");
		confBoardGrid.setWidget(0, 0, boardTitle);
		confBoardGrid.setWidget(1, 0, boardInitializeStatusGrid);
		confBoardGrid.getCellFormatter().setVerticalAlignment(1, 0,
				HasVerticalAlignment.ALIGN_TOP);

		// title for initialize boards
		Label confTitle = new Label();
		confTitle.setText(PerifericoUI.getMessages()
				.informatic_configuration_title());
		confTitle.setStyleName("gwt-informatic-title");
		confBoardGrid.setWidget(0, 1, confTitle);
		confBoardGrid.setWidget(1, 1, configurationStatusGrid);
		confBoardGrid.getCellFormatter().setVerticalAlignment(1, 1,
				HasVerticalAlignment.ALIGN_TOP);

		Label dpaStatuTitle = new Label();
		dpaStatuTitle.setText(PerifericoUI.getMessages().informatic_dpa_status());
		dpaStatuTitle.setStyleName("gwt-informatic-title");
		confBoardGrid.setWidget(2, 0, dpaStatuTitle);
		confBoardGrid.setWidget(3, 0, dpaStatusGrid);
		confBoardGrid.getCellFormatter().setVerticalAlignment(3, 0,
				HasVerticalAlignment.ALIGN_TOP);
		confBoardGrid.getFlexCellFormatter().setColSpan(3, 0, 2);

		confBoardGrid.setCellSpacing(10);

		externalPanel.add(title);
		externalPanel.add(panel);

		// Label and panel for file system info
		Label title2 = new Label();
		title2.setStyleName("gwt-Label-title-blue");
		title2.setText(PerifericoUI.getMessages().informatic_file_system_status());

		// panel that contains file system info
		VerticalPanel panel2 = new VerticalPanel();
		panel2.setStyleName("gwt-post-boxed-blue");
		panel2.add(fileSystemStatusGrid);

		externalPanel.add(title2);
		externalPanel.add(panel2);

		// Label and panel for gps info
		Label title3 = new Label();
		title3.setStyleName("gwt-Label-title-blue");
		title3.setText(PerifericoUI.getMessages().informatic_gps_status());

		// panel that contains file gps info
		VerticalPanel panel3 = new VerticalPanel();
		panel3.setStyleName("gwt-post-boxed-blue");
		panel3.add(gpsInfoGrid);

		gpsInfoGrid.setWidget(0, 0, gpsStatusGrid);
		gpsInfoGrid.getCellFormatter().setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_TOP);

		gpsInfoGrid.setWidget(0, 1, gpsPositionGrid);
		gpsInfoGrid.getCellFormatter().setVerticalAlignment(0, 1,
				HasVerticalAlignment.ALIGN_TOP);

		externalPanel.add(title3);
		externalPanel.add(panel3);

		initWidget(externalPanel);
	}

	private void setFields() {
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
			public void onSuccess(String[] perifericoStatus) {
				// perifericoStatus:
				// [0] : isOk
				// [1] : boardManagerInitStatus
				// [2] : configuredBoardsNumber
				// [3] : initializedBoardsNumber
				// [4] : loadConfigurationStatus
				// [5] : saveNewConfigurationStatus
				// [6] : acquisitionStarted
				// [7] : rootFsUsedSpacePercent
				// [8] : tmpFsUsedSpacePercent
				// [9] : dataFsUsedSpacePercent
				// [10] : usedSpaceAlarmThresholdPercent
				// [11] : usedSpaceWarningThresholdPercent
				// [12] : SMART status
				// [13] : RAID status
				// [14] : failedBoardBindingsNumber
				// [15] : enabledDataPortAnalyzersNumber
				// [16] : initializedDataPortDriversNumber
				// [17] : failedDataPortThreadsNumber
				// [18] : dpaOk
				// [19] : driverConfigsOK
				// [20] : dataWriteErrorCount
				// [21] : totalThreadFailures
				// [22] : currentThreadFailures
				// [23] : commonConfigFromCopStatus
				// [24] : dataInTheFuture
				// [25] : altitude
				// [26] : latitude
				// [27] : longitude
				// [28] : fix
				// [29] : timestamp
				// [30] : link to google maps

				// clear table
				Utils.clearTable(globalStatusGrid);
				Utils.clearTable(boardInitializeStatusGrid);
				Utils.clearTable(configurationStatusGrid);
				Utils.clearTable(configurationStatusSubGrid);
				Utils.clearTable(dpaStatusGrid);

				// isOk
				if (new Boolean(perifericoStatus[0])) {
					globalStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGreen()));
					globalStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().ok());
				} else {
					globalStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledRed()));
					globalStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().error());
				}
				globalStatusGrid.setText(0, 1,
						PerifericoUI.getMessages().informatic_global_status());
				globalStatusGrid.getCellFormatter().setAlignment(0, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				globalStatusGrid.getCellFormatter().setAlignment(0, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				globalStatusGrid.getCellFormatter().setWidth(0, 0, "62px");
				globalStatusGrid.setCellPadding(5);
				globalStatusGrid.setCellSpacing(5);

				// boardManagerInitStatus
				if (perifericoStatus[1] == null) {
					boardInitializeStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGray()));
					boardInitializeStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().no_info());
				} else if (new Boolean(perifericoStatus[1])) {
					boardInitializeStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGreen()));
					boardInitializeStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().ok());
				} else {
					boardInitializeStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledRed()));
					boardInitializeStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().alarm());
				}
				boardInitializeStatusGrid.setText(0, 1, PerifericoUI.getMessages()
						.informatic_boardManagerInitStatus());
				boardInitializeStatusGrid.getCellFormatter().setAlignment(0, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				boardInitializeStatusGrid.getCellFormatter().setAlignment(0, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				// configuredBoardsNumber and initializedBoardsNumber
				if (perifericoStatus[3] == null || perifericoStatus[2] == null) {
					boardInitializeStatusGrid.setWidget(1, 0,
							newImage(iconImageBundle.ledGray()));
					boardInitializeStatusGrid.getWidget(1, 0).setTitle(
							PerifericoUI.getMessages().no_info());
					boardInitializeStatusGrid
							.setText(
									1,
									1,
									PerifericoUI.getMessages()
											.informatic_configured_initializedBoardsNumber()
											+ PerifericoUI.getMessages().no_info());
				} else {
					if (new Integer(perifericoStatus[2]).equals(new Integer(
							perifericoStatus[3]))) {
						boardInitializeStatusGrid.setWidget(1, 0,
								newImage(iconImageBundle.ledGreen()));
						boardInitializeStatusGrid.getWidget(1, 0).setTitle(
								PerifericoUI.getMessages().ok());
					} else {
						boardInitializeStatusGrid.setWidget(1, 0,
								newImage(iconImageBundle.ledRed()));
						boardInitializeStatusGrid.getWidget(1, 0).setTitle(
								PerifericoUI.getMessages().alarm());
					}
					boardInitializeStatusGrid
							.setText(
									1,
									1,
									PerifericoUI.getMessages()
											.informatic_configured_initializedBoardsNumber()
											+ " "
											+ perifericoStatus[2]
											+ "/"
											+ perifericoStatus[3]);
				}
				boardInitializeStatusGrid.getCellFormatter().setAlignment(1, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				boardInitializeStatusGrid.getCellFormatter().setAlignment(1, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				// failed initialized board
				String number = "";
				if (perifericoStatus[14] == null) {
					boardInitializeStatusGrid.setWidget(2, 0,
							newImage(iconImageBundle.ledGray()));
					boardInitializeStatusGrid.getWidget(2, 0).setTitle(
							PerifericoUI.getMessages().no_info());
				} else {
					number = perifericoStatus[14];
					if (new Integer(perifericoStatus[14]) == 0) {
						boardInitializeStatusGrid.setWidget(2, 0,
								newImage(iconImageBundle.ledGreen()));
						boardInitializeStatusGrid.getWidget(2, 0).setTitle(
								PerifericoUI.getMessages().ok());
					} else {
						boardInitializeStatusGrid.setWidget(2, 0,
								newImage(iconImageBundle.ledRed()));
						boardInitializeStatusGrid.getWidget(2, 0).setTitle(
								PerifericoUI.getMessages().alarm());
					}
				}// end else
				boardInitializeStatusGrid.setText(
						2,
						1,
						PerifericoUI.getMessages()
								.informatic_board_failed_initialize()
								+ " "
								+ number);
				boardInitializeStatusGrid.getCellFormatter().setAlignment(2, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				boardInitializeStatusGrid.getCellFormatter().setAlignment(2, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				boardInitializeStatusGrid.setStyleName("gwt-Grid-blue");
				boardInitializeStatusGrid.getCellFormatter().setWidth(0, 0,
						"40px");
				boardInitializeStatusGrid.getCellFormatter().setWidth(0, 1,
						"400px");
				boardInitializeStatusGrid.setWidth("455px");
				boardInitializeStatusGrid.setCellPadding(4);
				boardInitializeStatusGrid.setCellSpacing(4);

				// loadConfigurationStatus
				if (perifericoStatus[4] == null) {
					configurationStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGray()));
					configurationStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().no_info());
				} else if (perifericoStatus[4].equals("OK")) {
					configurationStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGreen()));
					configurationStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().ok());
				} else {
					configurationStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledRed()));
					configurationStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().alarm());
				}
				configurationStatusGrid.setWidget(0, 1,
						configurationStatusSubGrid);
				configurationStatusSubGrid.setText(
						0,
						0,
						PerifericoUI.getMessages()
								.informatic_loadConfigurationStatus()
								+ " "
								+ perifericoStatus[4]);

				// saveNewConfigurationStatus
				if (perifericoStatus[5] == null) {
					configurationStatusGrid.setWidget(1, 0,
							newImage(iconImageBundle.ledGray()));
					configurationStatusGrid.getWidget(1, 0).setTitle(
							PerifericoUI.getMessages().no_info());
				} else if (new Boolean(perifericoStatus[5])) {
					configurationStatusGrid.setWidget(1, 0,
							newImage(iconImageBundle.ledGreen()));
					configurationStatusGrid.getWidget(1, 0).setTitle(
							PerifericoUI.getMessages().ok());
				} else {
					configurationStatusGrid.setWidget(1, 0,
							newImage(iconImageBundle.ledRed()));
					configurationStatusGrid.getWidget(1, 0).setTitle(
							PerifericoUI.getMessages().alarm());
				}
				configurationStatusGrid.setText(1, 1, PerifericoUI.getMessages()
						.informatic_saveNewConfigurationStatus());

				// acquisitionStarted
				String msg = "";
				if (perifericoStatus[6] == null) {
					configurationStatusSubGrid.setWidget(0, 1,
							newImage(iconImageBundle.ledYellow()));
					msg = PerifericoUI.getMessages().activation();
					configurationStatusSubGrid.getWidget(0, 1).setTitle(msg);
				} else if (new Boolean(perifericoStatus[6])) {
					configurationStatusSubGrid.setWidget(0, 1,
							newImage(iconImageBundle.ledGreen()));
					msg = PerifericoUI.getMessages().active();
					configurationStatusSubGrid.getWidget(0, 1).setTitle(msg);
				} else {
					configurationStatusSubGrid.setWidget(0, 1,
							newImage(iconImageBundle.ledRed()));
					msg = PerifericoUI.getMessages().not_active();
					configurationStatusSubGrid.getWidget(0, 1).setTitle(msg);
				}
				configurationStatusSubGrid.setText(0, 2,
						PerifericoUI.getMessages().informatic_acquisitionStarted()
								+ " " + msg);

				// commonConfigFromCopStatus
				msg = PerifericoUI.getMessages().common_cfg_status() + ": ";
				if (perifericoStatus[23] == null) {
					configurationStatusGrid.setWidget(2, 0,
							newImage(iconImageBundle.ledGray()));
					msg += PerifericoUI.getMessages().lbl_none().toLowerCase();
				} else if ("OK".equals(perifericoStatus[23])) {
					configurationStatusGrid.setWidget(2, 0,
							newImage(iconImageBundle.ledGreen()));
					msg += PerifericoUI.getMessages().ok();
				} else if ("UNPARSABLE".equals(perifericoStatus[23])) {
					configurationStatusGrid.setWidget(2, 0,
							newImage(iconImageBundle.ledYellow()));
					msg += PerifericoUI.getMessages().unparsable();
				} else if ("CONSISTENCY_ERROR".equals(perifericoStatus[23])) {
					configurationStatusGrid.setWidget(2, 0,
							newImage(iconImageBundle.ledYellow()));
					msg += PerifericoUI.getMessages().consistency_error();
				} else {
					configurationStatusGrid.setWidget(2, 0,
							newImage(iconImageBundle.ledRed()));
					if ("SAVE_ERROR".equals(perifericoStatus[23])) {
						msg += PerifericoUI.getMessages().save_error();
					} else if ("LOAD_ERROR".equals(perifericoStatus[23])) {
						msg += PerifericoUI.getMessages().load_error();
					} else if ("INCOMPATIBLE".equals(perifericoStatus[23])) {
						msg += PerifericoUI.getMessages().incompatible();
					} else if ("CONFIG_LOAD_ERROR".equals(perifericoStatus[23])) {
						msg += PerifericoUI.getMessages().config_load_error();
					} else if ("CONFIG_START_ERROR"
							.equals(perifericoStatus[23])) {
						msg += PerifericoUI.getMessages().config_start_error();
					} else {
						msg += PerifericoUI.getMessages().unknown();
					}
				}
				configurationStatusGrid.setText(2, 1, msg);

				configurationStatusSubGrid.setCellPadding(4);
				configurationStatusSubGrid.setCellSpacing(0);
				configurationStatusSubGrid.setWidth("455px");
				configurationStatusSubGrid.getCellFormatter().setWidth(0, 0,
						"240px");
				configurationStatusSubGrid.getCellFormatter().setWidth(0, 1,
						"40px");
				configurationStatusSubGrid.getCellFormatter().setWidth(0, 2,
						"175px");
				configurationStatusSubGrid.getCellFormatter().setAlignment(0,
						0, HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				configurationStatusSubGrid.getCellFormatter().setAlignment(0,
						1, HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				configurationStatusSubGrid.getCellFormatter().setAlignment(0,
						2, HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				configurationStatusGrid.setStyleName("gwt-Grid-blue");
				configurationStatusGrid.setWidth("510px");
				configurationStatusGrid.setCellPadding(4);
				configurationStatusGrid.setCellSpacing(4);
				configurationStatusGrid.getCellFormatter().setWidth(0, 0,
						"40px");
				configurationStatusGrid.getCellFormatter().setWidth(0, 1,
						"455px");
				configurationStatusGrid.getCellFormatter().setWidth(1, 0,
						"40px");
				configurationStatusGrid.getCellFormatter().setWidth(1, 1,
						"455px");
				configurationStatusGrid.getCellFormatter().setWidth(2, 0,
						"40px");
				configurationStatusGrid.getCellFormatter().setWidth(2, 1,
						"455px");
				configurationStatusGrid.getCellFormatter().setAlignment(0, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				configurationStatusGrid.getCellFormatter().setAlignment(0, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				configurationStatusGrid.getCellFormatter().setAlignment(1, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				configurationStatusGrid.getCellFormatter().setAlignment(1, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				configurationStatusGrid.getCellFormatter().setAlignment(2, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				configurationStatusGrid.getCellFormatter().setAlignment(2, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				// application services status
				if (perifericoStatus[18] == null) {
					dpaStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGray()));
					dpaStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().no_info());
				} else if (new Boolean(perifericoStatus[18])) {
					if (perifericoStatus[19] != null
							&& !(new Boolean(perifericoStatus[19]))) {
						dpaStatusGrid.setWidget(0, 0,
								newImage(iconImageBundle.ledYellow()));
						dpaStatusGrid.getWidget(0, 0).setTitle(
								PerifericoUI.getMessages()
										.drv_configs_locally_changed());
					} else {
						dpaStatusGrid.setWidget(0, 0,
								newImage(iconImageBundle.ledGreen()));
						dpaStatusGrid.getWidget(0, 0).setTitle(
								PerifericoUI.getMessages().ok());
					}
				} else {
					dpaStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledRed()));
					dpaStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().alarm());
				}
				String enabled = "-";
				String initialized = "-";
				String failed = "-";
				if (perifericoStatus[15] != null)
					enabled = perifericoStatus[15];
				if (perifericoStatus[16] != null)
					initialized = perifericoStatus[16];
				if (perifericoStatus[17] != null)
					failed = perifericoStatus[17];
				dpaStatusGrid.setText(0, 1,
						PerifericoUI.getMessages().informatic_dpa_number() + " "
								+ enabled + "/" + initialized + "/" + failed);

				dpaStatusGrid.getCellFormatter().setAlignment(0, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				dpaStatusGrid.getCellFormatter().setAlignment(0, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				dpaStatusGrid.setStyleName("gwt-Grid-blue");
				dpaStatusGrid.getCellFormatter().setWidth(0, 0, "40px");
				dpaStatusGrid.getCellFormatter().setWidth(0, 1, "400px");

				if (new Integer(perifericoStatus[20]).intValue() > 0) {
					dpaStatusGrid.setWidget(0, 2,
							newImage(iconImageBundle.ledRed()));
					dpaStatusGrid.getWidget(0, 2).setTitle(
							PerifericoUI.getMessages().alarm());
				} else {
					dpaStatusGrid.setWidget(0, 2,
							newImage(iconImageBundle.ledGreen()));
					dpaStatusGrid.getWidget(0, 2).setTitle(
							PerifericoUI.getMessages().ok());
				}
				dpaStatusGrid.setText(0, 3,
						PerifericoUI.getMessages().informatic_error_number() + " "
								+ perifericoStatus[20]);

				dpaStatusGrid.getCellFormatter().setAlignment(0, 2,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				dpaStatusGrid.getCellFormatter().setAlignment(0, 3,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				dpaStatusGrid.setStyleName("gwt-Grid-blue");
				dpaStatusGrid.getCellFormatter().setWidth(0, 2, "40px");
				dpaStatusGrid.getCellFormatter().setWidth(0, 3, "435px");

				if (new Integer(perifericoStatus[22]).intValue() > 0) {
					dpaStatusGrid.setWidget(1, 0,
							newImage(iconImageBundle.ledRed()));
					dpaStatusGrid.getWidget(1, 0).setTitle(
							PerifericoUI.getMessages().alarm());
				} else if (new Integer(perifericoStatus[21]).intValue() > 0) {
					dpaStatusGrid.setWidget(1, 0,
							newImage(iconImageBundle.ledYellow()));
					dpaStatusGrid.getWidget(1, 0).setTitle(
							PerifericoUI.getMessages().warning());
				} else {
					dpaStatusGrid.setWidget(1, 0,
							newImage(iconImageBundle.ledGreen()));
					dpaStatusGrid.getWidget(1, 0).setTitle(
							PerifericoUI.getMessages().ok());
				}
				dpaStatusGrid.setText(
						1,
						1,
						PerifericoUI.getMessages()
								.informatic_error_number_application()
								+ " "
								+ perifericoStatus[21]
								+ "/"
								+ perifericoStatus[22]);
				dpaStatusGrid.getCellFormatter().setAlignment(1, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				dpaStatusGrid.getCellFormatter().setAlignment(1, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				dpaStatusGrid.setStyleName("gwt-Grid-blue");
				dpaStatusGrid.getCellFormatter().setWidth(1, 0, "40px");
				dpaStatusGrid.getCellFormatter().setWidth(1, 1, "400px");

				String futureMsg;
				if (new Boolean(perifericoStatus[24])) {
					dpaStatusGrid.setWidget(1, 2,
							newImage(iconImageBundle.ledRed()));
					dpaStatusGrid.getWidget(1, 2).setTitle(
							PerifericoUI.getMessages().alarm());
					futureMsg = PerifericoUI.getMessages().yes().toLowerCase();
				} else {
					dpaStatusGrid.setWidget(1, 2,
							newImage(iconImageBundle.ledGreen()));
					dpaStatusGrid.getWidget(1, 0).setTitle(
							PerifericoUI.getMessages().ok());
					futureMsg = PerifericoUI.getMessages().no().toLowerCase();
				}
				dpaStatusGrid.setText(1, 3, PerifericoUI.getMessages().future_data()
						+ " " + futureMsg);
				dpaStatusGrid.getCellFormatter().setAlignment(1, 2,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				dpaStatusGrid.getCellFormatter().setAlignment(1, 3,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);
				dpaStatusGrid.setStyleName("gwt-Grid-blue");
				dpaStatusGrid.getCellFormatter().setWidth(1, 2, "40px");
				dpaStatusGrid.getCellFormatter().setWidth(1, 3, "435px");

				dpaStatusGrid.setWidth("100%");
				dpaStatusGrid.setCellPadding(4);
				dpaStatusGrid.setCellSpacing(4);

				// rootFsUsedSpacePercent
				if (perifericoStatus[7] == null) {
					fileSystemStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGray()));
					fileSystemStatusGrid.getWidget(0, 0).setTitle(
							PerifericoUI.getMessages().no_info());
					fileSystemStatusGrid.setText(0, 1,
							PerifericoUI.getMessages().informatic_root()
									+ PerifericoUI.getMessages().no_info());
				} else {
					if (getThresholdIcon(
							new Integer(perifericoStatus[7]).intValue(),
							new Integer(perifericoStatus[11]).intValue(),
							new Integer(perifericoStatus[10]).intValue()) == OK) {
						fileSystemStatusGrid.setWidget(0, 0,
								newImage(iconImageBundle.ledGreen()));
						fileSystemStatusGrid.getWidget(0, 0).setTitle(
								PerifericoUI.getMessages().ok());
					} else if (getThresholdIcon(
							new Integer(perifericoStatus[7]).intValue(),
							new Integer(perifericoStatus[11]).intValue(),
							new Integer(perifericoStatus[10]).intValue()) == WARNING) {
						fileSystemStatusGrid.setWidget(0, 0,
								newImage(iconImageBundle.ledYellow()));
						fileSystemStatusGrid.getWidget(0, 0).setTitle(
								PerifericoUI.getMessages().warning());
					} else {
						fileSystemStatusGrid.setWidget(0, 0,
								newImage(iconImageBundle.ledRed()));
						fileSystemStatusGrid.getWidget(0, 0).setTitle(
								PerifericoUI.getMessages().alarm());
					}
					fileSystemStatusGrid.setText(0, 1,
							PerifericoUI.getMessages().informatic_root() + " "
									+ perifericoStatus[7] + "%");
				}// end else
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 0,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 1,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				// tmpFsUsedSpacePercent
				if (perifericoStatus[8] == null) {
					fileSystemStatusGrid.setWidget(0, 2,
							newImage(iconImageBundle.ledGray()));
					fileSystemStatusGrid.getWidget(0, 2).setTitle(
							PerifericoUI.getMessages().no_info());
					fileSystemStatusGrid.setText(0, 3,
							PerifericoUI.getMessages().informatic_tmp()
									+ PerifericoUI.getMessages().no_info());
				} else {
					if (getThresholdIcon(
							new Integer(perifericoStatus[8]).intValue(),
							new Integer(perifericoStatus[11]).intValue(),
							new Integer(perifericoStatus[10]).intValue()) == OK) {
						fileSystemStatusGrid.setWidget(0, 2,
								newImage(iconImageBundle.ledGreen()));
						fileSystemStatusGrid.getWidget(0, 2).setTitle(
								PerifericoUI.getMessages().ok());
					} else if (getThresholdIcon(
							new Integer(perifericoStatus[8]).intValue(),
							new Integer(perifericoStatus[11]).intValue(),
							new Integer(perifericoStatus[10]).intValue()) == WARNING) {
						fileSystemStatusGrid.setWidget(0, 2,
								newImage(iconImageBundle.ledYellow()));
						fileSystemStatusGrid.getWidget(0, 2).setTitle(
								PerifericoUI.getMessages().warning());
					} else {
						fileSystemStatusGrid.setWidget(0, 2,
								newImage(iconImageBundle.ledRed()));
						fileSystemStatusGrid.getWidget(0, 2).setTitle(
								PerifericoUI.getMessages().alarm());
					}
					fileSystemStatusGrid.setText(0, 3,
							PerifericoUI.getMessages().informatic_tmp() + " "
									+ perifericoStatus[8] + "%");
				}// end else
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 2,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 3,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				// dataFsUsedSpacePercent
				if (perifericoStatus[9] == null) {
					fileSystemStatusGrid.setWidget(0, 4,
							newImage(iconImageBundle.ledGray()));
					fileSystemStatusGrid.getWidget(0, 4).setTitle(
							PerifericoUI.getMessages().no_info());
					fileSystemStatusGrid.setText(0, 5,
							PerifericoUI.getMessages().informatic_data()
									+ PerifericoUI.getMessages().no_info());
				} else {
					if (getThresholdIcon(
							new Integer(perifericoStatus[9]).intValue(),
							new Integer(perifericoStatus[11]).intValue(),
							new Integer(perifericoStatus[10]).intValue()) == OK) {
						fileSystemStatusGrid.setWidget(0, 4,
								newImage(iconImageBundle.ledGreen()));
						fileSystemStatusGrid.getWidget(0, 4).setTitle(
								PerifericoUI.getMessages().ok());
					} else if (getThresholdIcon(
							new Integer(perifericoStatus[9]).intValue(),
							new Integer(perifericoStatus[11]).intValue(),
							new Integer(perifericoStatus[10]).intValue()) == WARNING) {
						fileSystemStatusGrid.setWidget(0, 4,
								newImage(iconImageBundle.ledYellow()));
						fileSystemStatusGrid.getWidget(0, 4).setTitle(
								PerifericoUI.getMessages().warning());
					} else {
						fileSystemStatusGrid.setWidget(0, 4,
								newImage(iconImageBundle.ledRed()));
						fileSystemStatusGrid.getWidget(0, 4).setTitle(
								PerifericoUI.getMessages().alarm());
					}
					fileSystemStatusGrid.setText(0, 5,
							PerifericoUI.getMessages().informatic_data() + " "
									+ perifericoStatus[9] + "%");
				}// end else
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 4,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 5,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				// SMART
				if (perifericoStatus[12] == null) {
					fileSystemStatusGrid.setWidget(0, 6,
							newImage(iconImageBundle.ledGray()));
					fileSystemStatusGrid.getWidget(0, 6).setTitle(
							PerifericoUI.getMessages().no_info());
					fileSystemStatusGrid.setText(0, 7,
							PerifericoUI.getMessages().smart() + ": "
									+ PerifericoUI.getMessages().no_info());
				} else {
					if ("UNAVAILABLE".equals(perifericoStatus[12])) {
						fileSystemStatusGrid.setWidget(0, 6,
								newImage(iconImageBundle.ledGray()));
						fileSystemStatusGrid.getWidget(0, 6).setTitle(
								PerifericoUI.getMessages().not_available());
						fileSystemStatusGrid
								.setText(
										0,
										7,
										PerifericoUI.getMessages().smart()
												+ ": "
												+ PerifericoUI.getMessages()
														.not_available());
					} else if ("OK".equals(perifericoStatus[12])) {
						fileSystemStatusGrid.setWidget(0, 6,
								newImage(iconImageBundle.ledGreen()));
						fileSystemStatusGrid.getWidget(0, 6).setTitle(
								PerifericoUI.getMessages().ok());
						fileSystemStatusGrid.setText(0, 7,
								PerifericoUI.getMessages().smart() + ": "
										+ PerifericoUI.getMessages().ok());
					} else if ("WARNING".equals(perifericoStatus[12])) {
						fileSystemStatusGrid.setWidget(0, 6,
								newImage(iconImageBundle.ledYellow()));
						fileSystemStatusGrid.getWidget(0, 6).setTitle(
								PerifericoUI.getMessages().warning());
						fileSystemStatusGrid.setText(0, 7,
								PerifericoUI.getMessages().smart() + ": "
										+ PerifericoUI.getMessages().warning());
					} else {
						fileSystemStatusGrid.setWidget(0, 6,
								newImage(iconImageBundle.ledRed()));
						fileSystemStatusGrid.getWidget(0, 6).setTitle(
								PerifericoUI.getMessages().alarm());
						fileSystemStatusGrid.setText(0, 7,
								PerifericoUI.getMessages().smart() + ": "
										+ PerifericoUI.getMessages().alarm());
					}
				}
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 6,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 7,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				// RAID
				if (perifericoStatus[13] == null) {
					fileSystemStatusGrid.setWidget(0, 8,
							newImage(iconImageBundle.ledGray()));
					fileSystemStatusGrid.getWidget(0, 8).setTitle(
							PerifericoUI.getMessages().no_info());
					fileSystemStatusGrid.setText(0, 9,
							PerifericoUI.getMessages().raid() + ": "
									+ PerifericoUI.getMessages().no_info());
				} else {
					if ("UNAVAILABLE".equals(perifericoStatus[13])) {
						fileSystemStatusGrid.setWidget(0, 8,
								newImage(iconImageBundle.ledGray()));
						fileSystemStatusGrid.getWidget(0, 8).setTitle(
								PerifericoUI.getMessages().not_available());
						fileSystemStatusGrid
								.setText(
										0,
										9,
										PerifericoUI.getMessages().raid()
												+ ": "
												+ PerifericoUI.getMessages()
														.not_available());
					} else if ("OK".equals(perifericoStatus[13])) {
						fileSystemStatusGrid.setWidget(0, 8,
								newImage(iconImageBundle.ledGreen()));
						fileSystemStatusGrid.getWidget(0, 8).setTitle(
								PerifericoUI.getMessages().ok());
						fileSystemStatusGrid.setText(0, 9,
								PerifericoUI.getMessages().raid() + ": "
										+ PerifericoUI.getMessages().ok());
					} else if ("WARNING".equals(perifericoStatus[13])) {
						fileSystemStatusGrid.setWidget(0, 8,
								newImage(iconImageBundle.ledYellow()));
						fileSystemStatusGrid.getWidget(0, 8).setTitle(
								PerifericoUI.getMessages().warning());
						fileSystemStatusGrid.setText(0, 9,
								PerifericoUI.getMessages().raid() + ": "
										+ PerifericoUI.getMessages().warning());
					} else {
						fileSystemStatusGrid.setWidget(0, 8,
								newImage(iconImageBundle.ledRed()));
						fileSystemStatusGrid.getWidget(0, 8).setTitle(
								PerifericoUI.getMessages().alarm());
						fileSystemStatusGrid.setText(0, 9,
								PerifericoUI.getMessages().raid() + ": "
										+ PerifericoUI.getMessages().alarm());
					}
				}
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 8,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				fileSystemStatusGrid.getCellFormatter().setAlignment(0, 9,
						HasHorizontalAlignment.ALIGN_LEFT,
						HasVerticalAlignment.ALIGN_MIDDLE);

				fileSystemStatusGrid.setWidth("100%");
				fileSystemStatusGrid.setCellPadding(5);
				fileSystemStatusGrid.setCellSpacing(5);

				// load gps information
				if (perifericoStatus.length == 25) {
					// gps not installed
					if (gpsStatusGrid.getRowCount() > 1) {
						gpsStatusGrid.clear();
						gpsPositionGrid.clear();
						gpsStatusGrid.removeAllRows();
						gpsPositionGrid.removeAllRows();
					}
					gpsStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGray()));
					gpsStatusGrid.setText(0, 1,
							PerifericoUI.getMessages().gps_not_installed());
					gpsStatusGrid.getCellFormatter().setWidth(0, 0, "62px");
					gpsStatusGrid.getCellFormatter().setAlignment(0, 0,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					gpsStatusGrid.setCellPadding(5);
					gpsStatusGrid.setCellSpacing(5);
				} else {
					// gps is installed
					gpsStatusGrid.setWidget(0, 0,
							newImage(iconImageBundle.ledGreen()));
					gpsStatusGrid.setText(0, 1,
							PerifericoUI.getMessages().gps_installed());
					gpsStatusGrid.getCellFormatter().setWidth(0, 0, "62px");
					gpsStatusGrid.getCellFormatter().setAlignment(0, 0,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					gpsStatusGrid.getCellFormatter().setAlignment(0, 1,
							HasHorizontalAlignment.ALIGN_LEFT,
							HasVerticalAlignment.ALIGN_MIDDLE);
					String fix = null;
					if (perifericoStatus[28].equals(FIX_2D)
							|| perifericoStatus[28].equals(FIX_3D)) {
						gpsStatusGrid.setWidget(1, 0,
								newImage(iconImageBundle.ledGreen()));
						if (perifericoStatus[28].equals(FIX_2D))
							fix = PerifericoUI.getMessages().gps_2d_fix();
						else
							fix = PerifericoUI.getMessages().gps_3d_fix();
					} else if (perifericoStatus[28].equals(NO_FIX)) {
						gpsStatusGrid.setWidget(1, 0,
								newImage(iconImageBundle.ledYellow()));
						fix = PerifericoUI.getMessages().gps_no_fix();
					} else {
						gpsStatusGrid.setWidget(1, 0,
								newImage(iconImageBundle.ledRed()));
						if (perifericoStatus[28].equals(GPS_READ_ERROR))
							fix = PerifericoUI.getMessages().gps_read_error();
						else
							fix = PerifericoUI.getMessages().gps_app_error();
					}
					gpsStatusGrid.setText(1, 1, PerifericoUI.getMessages().gps_fix()
							+ " " + fix);
					gpsStatusGrid.getCellFormatter().setAlignment(1, 0,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
					gpsStatusGrid.getCellFormatter().setAlignment(1, 1,
							HasHorizontalAlignment.ALIGN_LEFT,
							HasVerticalAlignment.ALIGN_MIDDLE);

					gpsStatusGrid.setWidth("480px");
					gpsStatusGrid.setCellPadding(4);
					gpsStatusGrid.setCellSpacing(4);

					gpsPositionGrid.setText(0, 0,
							PerifericoUI.getMessages().gps_data());
					gpsPositionGrid.setText(0, 1, perifericoStatus[29]);
					gpsPositionGrid.setText(1, 0,
							PerifericoUI.getMessages().gps_altitude());
					gpsPositionGrid.setText(1, 1, perifericoStatus[25]);
					gpsPositionGrid.setText(0, 2,
							PerifericoUI.getMessages().gps_latitude());
					gpsPositionGrid.setText(1, 2,
							PerifericoUI.getMessages().gps_longitude());
					if (perifericoStatus[30].equals("")) {
						// no link
						gpsPositionGrid.setText(0, 3, perifericoStatus[26]);
						gpsPositionGrid.setText(1, 3, perifericoStatus[27]);
					} else {
						// link to Google maps
						gpsPositionGrid
								.setWidget(
										0,
										3,
										new HTML(
												"<a href='javascript:urlWin=window.open(\""
														+ perifericoStatus[30]
														+ "\"); window[\"urlWin\"].focus()' title='"
														+ PerifericoUI.getMessages()
																.url() + "'>"
														+ perifericoStatus[26]
														+ "</a>"));
						gpsPositionGrid
								.setWidget(
										1,
										3,
										new HTML(
												"<a href='javascript:urlWin=window.open(\""
														+ perifericoStatus[30]
														+ "\"); window[\"urlWin\"].focus()' title='"
														+ PerifericoUI.getMessages()
																.url() + "'>"
														+ perifericoStatus[27]
														+ "</a>"));
					}
					gpsPositionGrid.getCellFormatter().setAlignment(0, 1,
							HasHorizontalAlignment.ALIGN_LEFT,
							HasVerticalAlignment.ALIGN_MIDDLE);
					gpsPositionGrid.getCellFormatter().setAlignment(0, 3,
							HasHorizontalAlignment.ALIGN_LEFT,
							HasVerticalAlignment.ALIGN_MIDDLE);
					gpsPositionGrid.getCellFormatter().setAlignment(1, 1,
							HasHorizontalAlignment.ALIGN_LEFT,
							HasVerticalAlignment.ALIGN_MIDDLE);
					gpsPositionGrid.getCellFormatter().setAlignment(1, 3,
							HasHorizontalAlignment.ALIGN_LEFT,
							HasVerticalAlignment.ALIGN_MIDDLE);
					gpsPositionGrid.setWidth("480px");
					gpsPositionGrid.setCellPadding(4);
					gpsPositionGrid.setCellSpacing(4);

				}// end else

			}// end onSuccess

			private static final int OK = 0;

			private static final int WARNING = 1;

			private static final int ALARM = 2;

			private int getThresholdIcon(int value, int warning, int alarm) {
				if (value <= warning)
					return OK;
				if (value > warning && value <= alarm)
					return WARNING;
				else
					return ALARM;
			}
		};

		perifService.getPerifericoStatusFields(callback);

	}

	@Override
	protected void loadContent() {
		setFields();
	}

	private Image newImage(ImageResource resource) {
		Image image = new Image();
		image.setResource(resource);
		return image;
	}

}// end class
