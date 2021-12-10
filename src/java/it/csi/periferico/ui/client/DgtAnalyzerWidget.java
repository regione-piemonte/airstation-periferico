/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.periferico.ui.client;

import it.csi.periferico.ui.client.DgtAnalyzerException.Type;
import it.csi.periferico.ui.client.pagecontrol.UIPage;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * Widget to configure Digital Analyzer
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class DgtAnalyzerWidget extends UIPage {

	private PerifericoUIServiceAsync perifService = PerifericoUI.getService();
	private Label title = new Label();
	private TextArea messageArea;
	private Image guiDisplay;
	private ListBox listBoxCommand;
	private TextBox textBoxArguments;
	private HorizontalPanel panelCommands;
	private PanelButtonWidget panelKeys;
	private String analyzerId;
	private Map<String, String> mapArgsDescription = new HashMap<String, String>();
	private AsyncCallback<List<String>> callbackMessages;
	private AsyncCallback<String> callbackDisplayImage;
	private AsyncCallback<String> callbackCommand;
	private AsyncCallback<List<String>> callbackCommandResult;
	private ClickHandler handlerReadDate;
	private ClickHandler handlerSetDate;
	private ClickHandler handlerAlarms;
	private ClickHandler handlerProcessParams;
	private ClickHandler handlerMeasures;
	private ClickHandler handlerAlarmList;
	private ClickHandler handlerSendCommand;
	private ClickHandler handlerReadSerialNum;
	private ClickHandler handlerClearAlarms;
	private ClickHandler handlerReadDisplay;
	private ClickHandler handlerReadDisplayImage;
	private ChangeHandler handlerSelectCommand;
	private String whiteBase64 = "data:image/png;base64," //
			+ "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAABhGlDQ1BJQ0MgcHJvZmlsZQAAKJF9\n"
			+ "kT1Iw0AcxV/TSqVUHOwg6pChOlkRFXHUKhShQqgVWnUwufQLmjQkKS6OgmvBwY/FqoOLs64OroIg\n"
			+ "+AHi5uak6CIl/q8ptIjx4Lgf7+497t4BQr3MNCswDmi6baYScTGTXRWDrwggiBAGMSYzy5iTpCQ8\n"
			+ "x9c9fHy9i/Es73N/jh41ZzHAJxLPMsO0iTeIpzdtg/M+cYQVZZX4nHjUpAsSP3JdcfmNc6HJAs+M\n"
			+ "mOnUPHGEWCx0sNLBrGhqxFPEUVXTKV/IuKxy3uKslausdU/+wnBOX1nmOs0hJLCIJUgQoaCKEsqw\n"
			+ "EaNVJ8VCivbjHv6Bpl8il0KuEhg5FlCBBrnpB/+D391a+ckJNykcB7peHOdjGAjuAo2a43wfO07j\n"
			+ "BPA/A1d621+pAzOfpNfaWvQI6N0GLq7bmrIHXO4A/U+GbMpNyU9TyOeB9zP6pizQdwuE1tzeWvs4\n"
			+ "fQDS1FXyBjg4BEYKlL3u8e7uzt7+PdPq7wdXNHKcisIh9QAAAAlwSFlzAAALEwAACxMBAJqcGAAA\n"
			+ "AAd0SU1FB+UEBwocKtJRr7UAAAAZdEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4XAAAA\n"
			+ "GklEQVQoz2P8//8/AymAiYFEMKphVMPQ0QAAVW0DHZ8uFaIAAAAASUVORK5CYII=";

	public DgtAnalyzerWidget() {

		callbackMessages = new DgtCallback<List<String>>() {
			public void onSuccess(List<String> listMessages) {
				StringBuffer textBuffer = new StringBuffer();
				for (String line : listMessages)
					textBuffer.append(line).append("\n");
				onRequestSucceded(textBuffer.toString().trim());
			}
		};

		callbackDisplayImage = new DgtCallback<String>() {
			public void onSuccess(String displayBase64) {
				messageArea.setText("");
				if (displayBase64 != null)
					guiDisplay.setUrl(displayBase64);
				else
					guiDisplay.setUrl(whiteBase64);
				Utils.unlockForPopup("loading");
			}
		};

		callbackCommand = new DgtCallback<String>() {
			public void onSuccess(final String commandId) {
				callbackCommandResult = new DgtCallback<List<String>>() {
					public void onSuccess(List<String> listMessages) {
						if (listMessages == null) {
							onRequestRunning("Command is still running, please wait...");
							perifService.getCommandResult(commandId,
									callbackCommandResult);
						}
						StringBuffer textBuffer = new StringBuffer();
						for (String line : listMessages)
							textBuffer.append(line).append("\n");
						onRequestSucceded(textBuffer.toString().trim());
					}
				};
				perifService.getCommandResult(commandId, callbackCommandResult);
			}
		};

		initHandlers();

		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();
		panelButtonWidget.addButton(PerifericoUI.makeUpButtonBlue());
		// TODO fare pagina di help
		panelButtonWidget.addButton(PerifericoUI
				.makeViewHelpButton("dgtAnalyzersWidget"));

		VerticalPanel externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().dgt_analyzers_title(), panelButtonWidget);

		// Label and panel for settings
		title.setStyleName("gwt-Label-title-blue");

		// panel that contains application info
		VerticalPanel panel = new VerticalPanel();
		panel.setStyleName("gwt-post-boxed-blue");

		// panel for common commands
		PanelButtonWidget panelButton = new PanelButtonWidget();
		panelButton.addButton(PerifericoUI.makeBlueButton(handlerReadSerialNum,
				PerifericoUI.getMessages().button_serial_number()));
		panelButton.addButton(PerifericoUI.makeBlueButton(handlerMeasures,
				PerifericoUI.getMessages().button_measure_parameter()));
		panelButton.addButton(PerifericoUI.makeBlueButton(handlerProcessParams,
				PerifericoUI.getMessages().button_dgt_parameter()));
		panelButton.addButton(PerifericoUI.makeBlueButton(handlerAlarms,
				PerifericoUI.getMessages().button_dgt_alarm()));
		panelButton.addButton(PerifericoUI.makeBlueButton(handlerAlarmList,
				PerifericoUI.getMessages().button_list_fault()));
		panelButton.addButton(PerifericoUI.makeBlueButton(handlerClearAlarms,
				PerifericoUI.getMessages().button_reset()));
		panelButton.addButton(PerifericoUI.makeBlueButton(handlerReadDate,
				PerifericoUI.getMessages().read_date()));
		panelButton.addButton(PerifericoUI.makeBlueButton(handlerSetDate,
				PerifericoUI.getMessages().set_date()));
		panel.add(panelButton);

		// panel for custom commands
		panelCommands = new HorizontalPanel();
		panelCommands.setSpacing(10);
		Label lblCommand = new Label(PerifericoUI.getMessages().command());
		panelCommands.add(lblCommand);
		panelCommands.setCellVerticalAlignment(lblCommand,
				HasVerticalAlignment.ALIGN_MIDDLE);
		listBoxCommand = new ListBox();
		listBoxCommand.setStyleName("gwt-bg-text-blue-custom-font");
		listBoxCommand.setHeight("32px");
		listBoxCommand.addChangeHandler(handlerSelectCommand);
		panelCommands.add(listBoxCommand);
		panelCommands.setCellVerticalAlignment(listBoxCommand,
				HasVerticalAlignment.ALIGN_MIDDLE);
		Label lblArgs = new Label(PerifericoUI.getMessages().arguments());
		panelCommands.add(lblArgs);
		panelCommands.setCellVerticalAlignment(lblArgs,
				HasVerticalAlignment.ALIGN_MIDDLE);
		textBoxArguments = new TextBox();
		textBoxArguments.setWidth("200px");
		textBoxArguments.setStyleName("gwt-bg-text-blue-custom-font");
		textBoxArguments.setHeight("28px");
		textBoxArguments.setEnabled(false);
		panelCommands.add(textBoxArguments);
		panelCommands.setCellVerticalAlignment(textBoxArguments,
				HasVerticalAlignment.ALIGN_MIDDLE);
		Button btnSend = PerifericoUI.makeBlueButton(handlerSendCommand,
				PerifericoUI.getMessages().send_command());
		panelCommands.add(btnSend);
		panelCommands.setCellVerticalAlignment(btnSend,
				HasVerticalAlignment.ALIGN_MIDDLE);
		panelCommands.setVisible(false);
		panel.add(panelCommands);

		// panel for analyzer's remote UI
		panelKeys = new PanelButtonWidget(HasVerticalAlignment.ALIGN_BOTTOM);
		panelKeys.setVisible(false);
		panel.add(panelKeys);

		FlexTable settingsGrid = new FlexTable();
		messageArea = new TextArea();
		messageArea.setWidth("990px");
		messageArea.setHeight("300px");
		messageArea.setStyleName("gwt-bg-text-blue-fixed-space");
		messageArea.setReadOnly(true);
		settingsGrid.setWidget(0, 0, messageArea);
		guiDisplay = new Image(whiteBase64);
		guiDisplay.setWidth("0px");
		guiDisplay.setHeight("300px");
		guiDisplay.setVisible(false);
		settingsGrid.setWidget(0, 1, guiDisplay);
		panel.add(settingsGrid);

		externalPanel.add(title);
		externalPanel.add(panel);
		initWidget(externalPanel);
	}

	void setAnalyzerName(String description) {
		title.setText(description);
	}

	void setAnalyzerId(String analyzerId) {
		this.analyzerId = analyzerId;
	}

	private String getAnalyzerId() {
		return this.analyzerId;
	}

	@Override
	protected void loadContent() {
		messageArea.setText("");
		initCustomCommands();
		initRemoteUI();
	}

	private void initCustomCommands() {
		panelCommands.setVisible(false);
		listBoxCommand.clear();
		mapArgsDescription.clear();
		textBoxArguments.setTitle(null);
		textBoxArguments.setText(null);
		textBoxArguments.setEnabled(false);
		AsyncCallback<Boolean> callback = new DgtCallback<Boolean>() {
			public void onSuccess(Boolean supported) {
				if (supported) {
					AsyncCallback<List<String[]>> callback2 = new DgtCallback<List<String[]>>() {
						public void onSuccess(List<String[]> commandList) {
							boolean first = true;
							for (String[] command : commandList) {
								listBoxCommand.addItem(command[1], command[0]);
								mapArgsDescription.put(command[0], command[2]);
								if (first) {
									if (command[2] != null
											&& !command[2].isEmpty()) {
										textBoxArguments.setTitle(command[2]);
										textBoxArguments.setEnabled(true);
									}
								}
								first = false;
							}
							panelCommands.setVisible(true);
						}
					};
					perifService.getCommandList(getAnalyzerId(), callback2);
				}
			}
		};
		perifService.isCustomCommandSupported(getAnalyzerId(), callback);
	}

	private void initRemoteUI() {
		guiDisplay.setUrl(whiteBase64);
		guiDisplay.setVisible(false);
		guiDisplay.setWidth("0px");
		messageArea.setWidth("990px");
		panelKeys.setVisible(false);
		panelKeys.removeButtons();
		final ClickHandler handlerSendKeyUI = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				perifService.sendKey(getAnalyzerId(),
						((Button) event.getSource()).getText(),
						callbackMessages);
			}
		};
		final ClickHandler handlerSendKeyGUI = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				perifService.sendKey(getAnalyzerId(),
						((Button) event.getSource()).getText(),
						new DgtCallback<List<String>>() {
							public void onSuccess(List<String> listMessages) {
								StringBuffer textBuffer = new StringBuffer();
								for (String line : listMessages)
									textBuffer.append(line).append("\n");
								messageArea.setText(textBuffer.toString());
								perifService.readDisplayImage(getAnalyzerId(),
										callbackDisplayImage);
							}
						});
			}
		};
		AsyncCallback<String> callbackGUI = new DgtCallback<String>() {
			public void onSuccess(String format) {
				if (format != null) {
					panelKeys.addButton(PerifericoUI
							.makeReloadDisplayButton(handlerReadDisplayImage));
					panelKeys.setVisible(true);
					guiDisplay.setVisible(true);
					messageArea.setWidth("495px");
					guiDisplay.setWidth("495px");
					perifService.getKeyList(getAnalyzerId(),
							new DgtCallback<List<String>>() {
								public void onSuccess(List<String> keyList) {
									for (String key : keyList)
										panelKeys.addButton(new Button(key,
												handlerSendKeyGUI));
								}
							});
				} else {
					AsyncCallback<Boolean> callbackUI = new DgtCallback<Boolean>() {
						public void onSuccess(Boolean supported) {
							if (supported) {
								panelKeys
										.addButton(PerifericoUI
												.makeReloadDisplayButton(handlerReadDisplay));
								panelKeys.setVisible(true);
								perifService.getKeyList(getAnalyzerId(),
										new DgtCallback<List<String>>() {
											public void onSuccess(
													List<String> keyList) {
												for (String key : keyList)
													panelKeys
															.addButton(new Button(
																	key,
																	handlerSendKeyUI));
											}
										});
							}
						}
					};
					perifService.isRemoteUISupported(getAnalyzerId(),
							callbackUI);
				}
			}
		};
		perifService.isRemoteGUISupported(getAnalyzerId(), callbackGUI);
	}

	private void initHandlers() {

		handlerReadDate = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				AsyncCallback<Date> callback = new DgtCallback<Date>() {
					public void onSuccess(Date date) {
						onRequestSucceded(date.toString());
					}
				};
				perifService.getAnalyzerDate(getAnalyzerId(), callback);
			}
		};

		handlerSetDate = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				AsyncCallback<Date> callback = new DgtCallback<Date>() {
					public void onSuccess(Date result) {
						onRequestSucceded(result.toString());
					}
				};
				perifService.setAnalyzerDate(getAnalyzerId(), callback);
			}
		};

		handlerAlarms = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				perifService.getAlarmValues(getAnalyzerId(), callbackMessages);
			}

		};

		handlerProcessParams = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				perifService.getProcessParameterValues(getAnalyzerId(),
						callbackMessages);
			}
		};

		handlerMeasures = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				perifService
						.getMeasureValues(getAnalyzerId(), callbackMessages);
			}
		};

		handlerAlarmList = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				perifService.getAlarmList(getAnalyzerId(), callbackMessages);
			}
		};

		handlerSendCommand = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				String cmd = listBoxCommand.getValue(listBoxCommand
						.getSelectedIndex());
				String args = textBoxArguments.getText();
				String[] cmdAndArgs;
				if (args != null && !args.isEmpty())
					cmdAndArgs = new String[] { cmd, args };
				else
					cmdAndArgs = new String[] { cmd };
				perifService.sendCommand(getAnalyzerId(), cmdAndArgs,
						callbackCommand);
			}
		};

		handlerReadSerialNum = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				AsyncCallback<String> callback = new DgtCallback<String>() {
					public void onSuccess(String result) {
						onRequestSucceded(result.toString());
					}
				};
				perifService.getSerialNumber(getAnalyzerId(), callback);
			}
		};

		handlerClearAlarms = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				AsyncCallback<Integer> callback = new DgtCallback<Integer>() {
					public void onSuccess(Integer result) {
						onRequestSucceded(result.toString());
					}
				};
				perifService.resetAllFaults(getAnalyzerId(), callback);
			}
		};

		handlerReadDisplay = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				perifService.readDisplay(getAnalyzerId(), callbackMessages);
			}
		};

		handlerReadDisplayImage = new ClickHandler() {
			public void onClick(ClickEvent event) {
				onStartOfRequest();
				perifService.readDisplayImage(getAnalyzerId(),
						callbackDisplayImage);
			}
		};

		handlerSelectCommand = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String cmd = listBoxCommand.getValue(listBoxCommand
						.getSelectedIndex());
				String tooltip = mapArgsDescription.get(cmd);
				textBoxArguments.setTitle(tooltip);
				textBoxArguments.setText(null);
				textBoxArguments.setEnabled(tooltip != null
						&& !tooltip.isEmpty());
			}
		};

	}

	private void onStartOfRequest() {
		Utils.blockForPopup("loading");
		// TODO: usare i bundle
		messageArea.setText("Command sent to analyzer, please wait...");
	}

	private void onRequestSucceded(String result) {
		messageArea.setText(result);
		Utils.unlockForPopup("loading");
	}

	private void onRequestRunning(String message) {
		String text = messageArea.getText() + "\n" + message;
		messageArea.setText(text);
	}

	private void onRequestFailed() {
		Utils.unlockForPopup("loading");
		messageArea.setText("");
	}

	private abstract class DgtCallback<T> extends UIAsyncCallback<T> {

		public void onFailure(Throwable caught) {
			onRequestFailed();
			if (caught instanceof DgtAnalyzerException) {
				DgtAnalyzerException de = (DgtAnalyzerException) caught;
				if (de.getType() == Type.NOT_SUPPORTED) {
					Window.alert(PerifericoUI.getMessages().not_enabled_function()
							+ " '" + caught.getMessage() + "'");
				} else if (de.getType() == Type.NOT_CONNECTED) {
					Window.alert(PerifericoUI.getMessages().not_play() + " '"
							+ caught.getMessage() + "'");
				} else {
					Window.alert(PerifericoUI.getMessages().execution_failure()
							+ " '" + caught.getMessage() + "'");
				}
			} else {
				super.onFailure(caught);
			}
		}
	}

}
