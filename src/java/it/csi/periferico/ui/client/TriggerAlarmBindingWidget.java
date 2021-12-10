/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Silvia Vergnano
 * Purpose of file: dialog box for trigger alarms binding
 * Change log:
 *   2008-06-06: initial version
 * ----------------------------------------------------------------------------
 * $Id: TriggerAlarmBindingWidget.java,v 1.8 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dialog box for trigger alarms binding
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */
public class TriggerAlarmBindingWidget extends PopupPanel {

	private Hidden alarmId = new Hidden();

	private TriggerAlarmBindingWidget triggerAlarmPopup;

	private VerticalPanel panel;

	private ListBox bindableTriggerAlarmListBox;

	public TriggerAlarmBindingWidget(boolean autoHide, boolean modal,
			String alarmIdStr) {
		super(autoHide, modal);
		triggerAlarmPopup = this;
		alarmId.setValue(alarmIdStr);

		// panel that contains TriggerAlarm Binding info and buttons
		panel = new VerticalPanel();
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		// create listBox of bindable TriggerAlarm, calling
		// PerifericoService
		bindableTriggerAlarmListBox = new ListBox();
		bindableTriggerAlarmListBox.setWidth("300px");
		bindableTriggerAlarmListBox.setVisibleItemCount(7);
		panel.add(bindableTriggerAlarmListBox);
		bindableTriggerAlarmListBox.addItem(PerifericoUI.getMessages()
				.select_trigger_alarm(), "0");
		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] triggerAlarmList) {
				if (triggerAlarmList.length == 1
						&& PerifericoUI.SESSION_ENDED
								.equals(triggerAlarmList[0][0]))
					PerifericoUI.sessionEnded();
				else
					for (int k = 0; k < triggerAlarmList.length; k++)
						bindableTriggerAlarmListBox.addItem(
								triggerAlarmList[k][0], triggerAlarmList[k][1]);
			}
		};

		perifService.getBindableSampleElements(callback);

		PanelButtonWidget panelButton = PerifericoUI.makeUndoSendPanelButton(
				new ClickHandler() {
					public void onClick(ClickEvent event) {
						Utils.unlockForPopup("popup");
						triggerAlarmPopup.hide();
					}
				}, new ClickHandler() {
					public void onClick(ClickEvent event) {

						int indexSelected = bindableTriggerAlarmListBox
								.getSelectedIndex();
						String selectedValue = bindableTriggerAlarmListBox
								.getValue(indexSelected);
						String selectedLabel = bindableTriggerAlarmListBox
								.getItemText(indexSelected);
						if (selectedValue.equals("0"))
							Window.alert(PerifericoUI.getMessages()
									.trigger_alarm_error_select_item());
						else {
							PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
									.create(PerifericoUIService.class);
							ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
							endpoint.setServiceEntryPoint(GWT
									.getModuleBaseURL()
									+ "uiservice");
							AsyncCallback<String[]> callback = new UIAsyncCallback<String[]>() {
								public void onSuccess(String[] resultString) {
									if (resultString[1] != null
											&& resultString[1]
													.equals(PerifericoUI.SESSION_ENDED))
										PerifericoUI.sessionEnded();
									else {
										boolean resValue = new Boolean(
												resultString[1]).booleanValue();
										if (resValue) {
											PerifericoUI.getAlarmWidget()
													.setPanelForUnbindTriggerAlarm(resultString[0]);
											Window.alert(PerifericoUI.getMessages()
													.binding_ok());
										} else
											Window.alert(PerifericoUI.getMessages()
													.binding_failed());
									}
								}
							};

							perifService.bindTriggerAlarm(alarmId.getValue(),
									selectedValue, selectedLabel, callback);
							Utils.unlockForPopup("popup");
							triggerAlarmPopup.hide();
						}
					}
				});

		panel.add(panelButton);

		this.add(panel);
		this.show();

	} // end constructor

	public void show() {
		super.show();

		int cWidth = Window.getClientWidth();
		int cHeight = Window.getClientHeight();
		// int myWidth = getOffsetWidth();
		// int myHeight = getOffsetHeight();
		// Utils.alert("clientwidth:"+cWidth+" clientHeight:"+cHeight+"
		// offsetwidth:"+myWidth+" offsetHeight:"+myHeight);
		// setPopupPosition((cWidth-myWidth)/2,(cHeight-myHeight)/2);
		setPopupPosition(((cWidth / 2) - 180), ((cHeight / 2) - 100));
		setWidth("360px");
		setHeight("200px");
		setStyleName("gwt-popup-panel");
		// DOM.setStyleAttribute(getElement(), "border", " 1px solid #FF8D17");
	}

}
