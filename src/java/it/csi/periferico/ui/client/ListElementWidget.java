/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: shows and manages a list of elements
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: ListElementWidget.java,v 1.46 2015/09/09 08:07:33 pfvallosio Exp $
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Shows and manages a list of elements
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public class ListElementWidget extends UIPage {

	private Hidden analyzerType = new Hidden();

	private String analyzerId = null;

	private String[] elementsId = null;

	private HorizontalPanel newElementPanel;

	private VerticalPanel externalPanel = null;

	private Label titleNew = null;

	private Label elementsTitle = null;

	private VerticalPanel elementPanel = null;

	private ListBox elementsListBox = new ListBox();

	// Table for element information.
	private FlexTable headerTable;

	private FlexTable table;

	private Button senderButtonModify;

	private String[] paramsId;

	private Boolean[] sampleTypeId;

	public ListElementWidget() {

		PanelButtonWidget panelButtonWidget = new PanelButtonWidget();
		panelButtonWidget.addButton(PerifericoUI.makeUpButton());
		panelButtonWidget.addButton(PerifericoUI.makeSaveButton());
		panelButtonWidget.addButton(PerifericoUI.makeReloadButton());
		panelButtonWidget.addButton(PerifericoUI
				.makeConfHelpButton("listElementWidget"));

		externalPanel = PerifericoUI.getTitledExternalPanel(
				PerifericoUI.getMessages().list_element_title(), panelButtonWidget);

		/*
		 * New element panel
		 */
		// Prepare panel for new element
		titleNew = new Label();
		titleNew.setText(PerifericoUI.getMessages().lbl_element_new());
		titleNew.setStyleName("gwt-Label-title");

		newElementPanel = new HorizontalPanel();
		newElementPanel.setStyleName("gwt-post-boxed");
		newElementPanel.setSpacing(10);

		// Setting elements list box
		elementsListBox.setWidth("300px");
		elementsListBox.setVisibleItemCount(1);

		Button newElementButton = new Button();
		newElementButton.setStyleName("gwt-button-new-orange");
		newElementButton.setTitle(PerifericoUI.getMessages().element_button_new());
		newElementButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int indexSelected = elementsListBox.getSelectedIndex();
				String selectedValue = elementsListBox.getValue(indexSelected);
				if (selectedValue.equals("0"))
					Window.alert(PerifericoUI.getMessages()
							.element_error_select_item());
				else {
					if (analyzerType.getValue().equals(Utils.SAMPLE)) {
						PerifericoUI.getSampleElementWidget().setNewElement(
								analyzerId, analyzerType.getValue(),
								selectedValue);
						PerifericoUI
								.setCurrentPage(PerifericoUI.getSampleElementWidget());
					}
					if (analyzerType.getValue().equals(Utils.AVG)) {
						PerifericoUI.getAvgElementWidget().setNewElement(analyzerId,
								analyzerType.getValue(), selectedValue);
						PerifericoUI
								.setCurrentPage(PerifericoUI.getAvgElementWidget());
					}
				}
			}
		});
		newElementPanel.add(elementsListBox);
		newElementPanel.add(newElementButton);

		// setting the alignment
		newElementPanel.setCellHorizontalAlignment(newElementButton,
				HasHorizontalAlignment.ALIGN_LEFT);
		newElementPanel.setCellHorizontalAlignment(elementsListBox,
				HasHorizontalAlignment.ALIGN_LEFT);
		newElementPanel.setCellVerticalAlignment(newElementButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newElementPanel.setCellVerticalAlignment(elementsListBox,
				HasVerticalAlignment.ALIGN_MIDDLE);
		newElementPanel.setCellWidth(elementsListBox, "310px");

		/*
		 * Element info
		 */
		// Label and panel for element info
		elementsTitle = new Label();
		elementsTitle.setText(PerifericoUI.getMessages().lbl_elements_title());
		elementsTitle.setStyleName("gwt-Label-title");

		// panel that contains the grid alarm list
		elementPanel = new VerticalPanel();
		elementPanel.setStyleName("gwt-post-boxed");

		// Prepare table of titles
		headerTable = new FlexTable();
		headerTable.setText(0, 0, PerifericoUI.getMessages().element_lbl_title());
		headerTable.setText(0, 1, PerifericoUI.getMessages().lbl_modify());
		headerTable.setText(0, 2, PerifericoUI.getMessages().lbl_delete());
		headerTable.setStyleName("gwt-table-header");
		headerTable.setWidth("100%");
		headerTable.getCellFormatter().setWidth(0, 0, "600px");
		headerTable.getCellFormatter().setWidth(0, 1, "190px");
		headerTable.getCellFormatter().setWidth(0, 2, "190px");
		for (int j = 0; j < 3; j++) {
			headerTable.getCellFormatter().setStyleName(0, j,
					"gwt-table-header");
		}
		elementPanel.add(headerTable);

		// Prepare table for elements info in a ScrollPanel
		ScrollPanel scrollPanel = new ScrollPanel();
		table = new FlexTable();
		table.setStyleName("gwt-table-data");
		table.setWidth("100%");
		scrollPanel.add(table);
		scrollPanel.setHeight("300px");
		elementPanel.add(scrollPanel);

		initWidget(externalPanel);

	}// end constructor

	void setAnalyzer(String analyzerId, String type) {
		this.analyzerId = analyzerId;
		analyzerType.setValue(type);
	}

	private void getElementsCfg() {
		if (!analyzerType.getValue().equals(Utils.DPA)) {
			// show also new element panel
			externalPanel.add(titleNew);
			externalPanel.add(newElementPanel);
		} else {
			// case of dpa
			if (externalPanel.getWidgetCount() == 5) {
				externalPanel.remove(1);
				externalPanel.remove(1);
			}
		}
		externalPanel.add(elementsTitle);
		externalPanel.add(elementPanel);

		PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
				.create(PerifericoUIService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
		AsyncCallback<String[][]> callback = new UIAsyncCallback<String[][]>() {
			public void onSuccess(String[][] resultString) {
				if (resultString != null)
					setElementInfo(resultString);
				else {
					// caso nuovo?????
					// TODO da implementare ???
					if (analyzerType.getValue().equals(Utils.SAMPLE)) {
						/*
						 * elementInfo for sample analyzer:
						 * 
						 * [0][0] : bindLabel
						 * 
						 * [0][1] : paramId
						 */

					}
					if (analyzerType.getValue().equals(Utils.DPA)) {
						// TODO implementare

					}
					if (analyzerType.getValue().equals(Utils.AVG)) {
						// TODO implementare
					}
					// WARNING: WIND and RAIN cannot create new element

				}// end else
			}// end onSuccess
		};
		if (analyzerId != null) {
			perifService.getElementsCfg(analyzerId, callback);
		} else {
			// TODO capire cosa fare
		}

	}// end getElementsCfg

	private void setElementInfo(String[][] elementFields) {
		/*
		 * elementInfo for analyzer:
		 * 
		 * [0][0] : bindLabel | not used for DPA analyzer
		 * 
		 * [0][1] : paramId
		 * 
		 * [0][2] : isSampleType
		 */
		elementsId = new String[elementFields.length];
		Utils.clearTable(table);
		if (!analyzerType.getValue().equals(Utils.DPA)) {
			table.getCellFormatter().setWidth(0, 0, "600px");
			table.getCellFormatter().setWidth(0, 1, "190px");
			table.getCellFormatter().setWidth(0, 2, "190px");
			Utils.clearTable(headerTable);
			headerTable
					.setText(0, 0, PerifericoUI.getMessages().element_lbl_title());
			headerTable.setText(0, 1, PerifericoUI.getMessages().lbl_modify());
			headerTable.setText(0, 2, PerifericoUI.getMessages().lbl_delete());
			headerTable.setStyleName("gwt-table-header");
			headerTable.setWidth("100%");
			headerTable.getCellFormatter().setWidth(0, 0, "600px");
			headerTable.getCellFormatter().setWidth(0, 1, "190px");
			headerTable.getCellFormatter().setWidth(0, 2, "190px");
			for (int j = 0; j < 3; j++) {
				headerTable.getCellFormatter().setStyleName(0, j,
						"gwt-table-header");
			}
		} else {
			table.getCellFormatter().setWidth(0, 0, "600px");
			table.getCellFormatter().setWidth(0, 1, "380px");
			Utils.clearTable(headerTable);
			headerTable
					.setText(0, 0, PerifericoUI.getMessages().element_lbl_title());
			headerTable.setText(0, 1, PerifericoUI.getMessages().lbl_modify());
			headerTable.getCellFormatter().setWidth(0, 0, "600px");
			headerTable.getCellFormatter().setWidth(0, 1, "380px");
			for (int j = 0; j < 2; j++) {
				headerTable.getCellFormatter().setStyleName(0, j,
						"gwt-table-header");
			}
		}
		paramsId = new String[elementFields.length];
		for (int i = 0; i < elementFields.length; i++) {
			paramsId[i] = elementFields[i][1];

		}
		sampleTypeId = new Boolean[elementFields.length];
		for (int i = 0; i < elementFields.length; i++) {
			sampleTypeId[i] = new Boolean(elementFields[i][2]);

		}
		for (int i = 0; i < elementFields.length; i++) {

			elementsId[i] = elementFields[i][1];
			table.setText(i, 0, elementFields[i][0]);
			table.getCellFormatter().setStyleName(i, 0, "gwt-table-data");
			Button modifyButton = new Button();
			modifyButton.setStyleName("gwt-button-modify");
			modifyButton
					.setTitle(PerifericoUI.getMessages().element_button_modify());
			table.setWidget(i, 1, modifyButton);
			table.getCellFormatter().setAlignment(i, 1,
					HasHorizontalAlignment.ALIGN_CENTER,
					HasVerticalAlignment.ALIGN_MIDDLE);
			table.getCellFormatter().setStyleName(i, 1, "gwt-table-data");
			modifyButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					senderButtonModify = (Button) event.getSource();

					// check if session is ended
					PerifericoUIServiceAsync perifServiceSession = (PerifericoUIServiceAsync) GWT
							.create(PerifericoUIService.class);
					ServiceDefTarget endpointSession = (ServiceDefTarget) perifServiceSession;
					endpointSession.setServiceEntryPoint(GWT.getModuleBaseURL()
							+ "uiservice");
					AsyncCallback<Boolean> callbackSession = new UIAsyncCallback<Boolean>() {
						public void onSuccess(Boolean sessionEnded) {
							if (sessionEnded.booleanValue()) {
								PerifericoUI.sessionEnded();
								return;
							}
							for (int k = 0; k < table.getRowCount(); k++) {
								if (((Button) (table.getWidget(k, 1)))
										.equals(senderButtonModify)) {
									UIPage newPage = null;
									// load element info
									if (analyzerType.getValue().equals(
											Utils.SAMPLE)
											&& sampleTypeId[k]) {
										// analyzer SAMPLE and elemantType
										// SAMPLE
										PerifericoUI.getSampleElementWidget()
												.setElements(paramsId, k,
														analyzerId,
														analyzerType.getValue());
										newPage = PerifericoUI.getSampleElementWidget();
									} else if (analyzerType.getValue().equals(
											Utils.AVG)
											&& sampleTypeId[k]) {
										// analyzer AVG and elemantType SAMPLE
										PerifericoUI.getSampleElementWidget()
												.setElements(paramsId, k,
														analyzerId,
														analyzerType.getValue());
										newPage = PerifericoUI.getSampleElementWidget();
									} else if (analyzerType.getValue().equals(
											Utils.AVG)
											&& !sampleTypeId[k]) {
										// analyzer AVG and elemantType AVG
										PerifericoUI.getAvgElementWidget()
												.setElements(paramsId, k,
														analyzerId,
														analyzerType.getValue());
										newPage = PerifericoUI.getAvgElementWidget();
									} else if (analyzerType.getValue().equals(
											Utils.DPA)
											&& sampleTypeId[k]) {
										// analyzer DPA and elemantType SAMPLE
										PerifericoUI.getDataPortElementWidget()
												.setElements(paramsId, k,
														analyzerId,
														analyzerType.getValue());
										newPage = PerifericoUI.getDataPortElementWidget();
									} else if (analyzerType.getValue().equals(
											Utils.DPA)
											&& !sampleTypeId[k]) {
										// analyzer DPA and elemantType AVG
										PerifericoUI.getDataPortAvgElementWidget()
												.setElements(paramsId, k,
														analyzerId,
														analyzerType.getValue());
										newPage = PerifericoUI.getDataPortAvgElementWidget();
									}
									PerifericoUI.setCurrentPage(newPage);
									break;
								}// end if
							}// end for
						}
					};
					perifServiceSession.verifySessionEnded(callbackSession);

				}// end onClick
			});
			if (!analyzerType.getValue().equals(Utils.DPA)) {
				Button deleteButton = new Button();
				deleteButton.setStyleName("gwt-button-delete");
				deleteButton.setTitle(PerifericoUI.getMessages()
						.element_button_delete());
				table.setWidget(i, 2, deleteButton);
				table.getCellFormatter().setAlignment(i, 2,
						HasHorizontalAlignment.ALIGN_CENTER,
						HasVerticalAlignment.ALIGN_MIDDLE);
				table.getCellFormatter().setStyleName(i, 2, "gwt-table-data");
				deleteButton.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						boolean confirmResult = Window
								.confirm(PerifericoUI.getMessages()
										.lbl_element_confirm_delete());
						if (confirmResult) {

							Button button = (Button) event.getSource();
							for (int k = 0; k < table.getRowCount(); k++) {
								if (((Button) (table.getWidget(k, 2)))
										.equals(button)) {
									PerifericoUIServiceAsync perifService = (PerifericoUIServiceAsync) GWT
											.create(PerifericoUIService.class);
									ServiceDefTarget endpoint = (ServiceDefTarget) perifService;
									endpoint.setServiceEntryPoint(GWT
											.getModuleBaseURL() + "uiservice");
									AsyncCallback<String> callback = new UIAsyncCallback<String>() {
										public void onSuccess(
												String resultString) {
											// TODO: capire cosa fare se c'e'
											// errore
											if (resultString
													.equals(PerifericoUI.SESSION_ENDED))
												PerifericoUI.sessionEnded();
											else {
												Boolean resultValue = new Boolean(
														resultString);
												if (resultValue.booleanValue()) {
													PerifericoUI
															.updateCurrentPage(null);
												} else {
													// TODO capire cosa fare se
													// non
													// viene cancellato
													Window.alert("Non cancellato");
												}
											}
										}// end onSuccess
									};
									perifService.deleteElement(analyzerId,
											elementsId[k], callback);
								}// end if
							}// end for
						}// end confirmresult
					}// end onClick
				});
			} // end if not DPA
		}// end for

		if (!analyzerType.getValue().equals(Utils.DPA)) {
			// if not DPA it's possible to select parameter to create new
			// parameter
			PerifericoUIServiceAsync perifService2 = (PerifericoUIServiceAsync) GWT
					.create(PerifericoUIService.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) perifService2;
			endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "uiservice");
			AsyncCallback<String[][]> callback2 = new UIAsyncCallback<String[][]>() {
				public void onSuccess(String[][] elementsMatrix) {
					// set elementsListBox calling perifService
					elementsListBox.clear();
					elementsListBox.addItem(
							PerifericoUI.getMessages().select_element_name(),
							new String("0"));
					for (int z = 0; z < elementsMatrix.length; z++) {
						/*
						 * elementsMatrix[0][0] : paramId
						 * 
						 * elementsMatrix[0][1] : parameter name
						 */
						if (elementsMatrix[z][0].equals("0"))
							elementsListBox.addItem(elementsMatrix[z][1],
									elementsMatrix[z][0]);
						else
							elementsListBox.addItem(elementsMatrix[z][0]
									+ " - " + elementsMatrix[z][1],
									elementsMatrix[z][0]);
					}// end for
				}// end onSuccess
			};
			perifService2.getElementsParam(analyzerId, callback2);
		}

	}// end setElementInfo

	@Override
	protected void loadContent() {
		getElementsCfg();
	}

	@Override
	protected void reset() {
		clearGridRowFormatter(table);
	}

}// end class
