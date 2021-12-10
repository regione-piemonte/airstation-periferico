/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: user interface internationalizable messages
 * Change log:
 *   2008-02-26: initial version
 * ----------------------------------------------------------------------------
 * $Id: MessageBundleClient.java,v 1.150 2015/10/16 12:58:49 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */

package it.csi.periferico.ui.client;

import com.google.gwt.i18n.client.Messages;

/**
 * 
 * Java interface in the same package as property files
 * (MessageBuncleClient.properties) that extends Messages and defines methods
 * (name matches the property entries) that all return string.
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public interface MessageBundleClient extends Messages {

	String application_title();

	String application_title_version();

	String locale();

	String lbl_language_italian();

	String lbl_language_english();

	String lbl_language_french();

	String lbl_language_german();

	String lbl_login_error();

	String lbl_login_title();

	String button_login();

	String lbl_station_title();

	String lbl_station_id();

	String lbl_station_nickname();

	String lbl_station_name();

	String lbl_station_location();

	String lbl_station_address();

	String lbl_station_city();

	String lbl_station_province();

	String lbl_station_userNotes();

	String lbl_station_title_connection();

	String lbl_station_cop_ip();

	String lbl_station_outgoing_call_enabled();

	String lbl_station_max_connection_retry();

	String lbl_id();

	String lbl_alarm_description();

	String lbl_alarm_abilitation();

	String lbl_modify();

	String lbl_delete();

	String lbl_unbind();

	String lbl_analyzer_title();

	String lbl_elements_title();

	String lbl_analyzer_name();

	String lbl_brand();

	String lbl_model();

	String lbl_version();

	String lbl_analyzer_description();

	String lbl_analyzer_serialNumber();

	String lbl_status();

	String lbl_analyzer_uiURL();

	String lbl_analyzer_userNotes();

	String link_station_conf();

	String link_alarm_conf();

	String link_instrument_conf();

	String link_board_conf();

	String link_view();

	String link_conf();

	String button_save();

	String button_new();

	String lbl_alarm_title();

	String lbl_alarm_new();

	String select_alarm_name();

	String select_analyzer_name();

	String button_new_alarm();

	String error_select_element();

	String lbl_alarm_title_modify();

	String lbl_button_close();

	String alarm_lbl_type_alarm();

	String alarm_lbl_description();

	String alarm_button_undo();

	String alarm_button_modify();

	String alarm_button_delete();

	String lbl_active_high();

	String lbl_discard_data();

	String alarm_lbl_threshold_high();

	String alarm_lbl_threshold_low();

	String alarm_lbl_warning_high();

	String alarm_lbl_warning_low();

	String confirm_abandon_page();

	String confirm_reload_page();

	String button_send_verify();

	String button_reload();

	String button_undo();

	String station_title();

	String alarm_title();

	String lbl_enabled();

	String button_enabled();

	String button_disabled();

	String board_title();

	String list_board_title();

	String lbl_board_index();

	String lbl_ISAboard_new();

	String button_new_ISAboard();

	String lbl_PCIdevice_title();

	String lbl_PCIdevice_busid();

	String lbl_PCIdevice_new();

	String lbl_PCIdevice_association();

	String board_button_modify();

	String board_button_delete();

	String board_button_unbind();

	String select_brand_board_name();

	String select_model_board_name();

	String device_button_new_board();

	String device_button_association_board();

	String select_bindable_board_index();

	String list_analyzer_title();

	String analyzer_button_new();

	String analyzer_error_select_item();

	String analyzer_lbl_name();

	String analyzer_button_delete();

	String analyzer_button_modify();

	String analyzer_title();

	String lbl_analyzer_new();

	String analyzer_list_title();

	String element_button_delete();

	String element_button_modify();

	String lbl_list_board_title();

	String lbl_isa_board();

	String lbl_pci_board();

	String lbl_board_io_base();

	String lbl_board_pci_device();

	String lbl_board_info_title();

	String lbl_digital_output();

	String lbl_digital_input();

	String select_analyzer_status();

	String lbl_digital_input_output();

	String lbl_channel_number();

	String lbl_type();

	String lbl_configure();

	String lbl_analog_input();

	String lbl_driver_name();

	String lbl_driver_value();

	String lbl_info_for();

	String configure_board_io();

	String button_init();

	String analyzer_lbl_fault_title();

	String analyzer_button_new_fault();

	String analyzer_lbl_connection();

	String analyzer_not_connected();

	String subdevice_title();

	String lbl_iouser();

	String lbl_range();

	String element_lbl_title();

	String button_list_element();

	String list_element_title();

	String lbl_elements_operation_title();

	String subdevice_button_unbind_iouser();

	String subdevice_button_open_list_iouser();

	String button_back();

	String iouser_error_select_item();

	String select_iouser();

	String confirm_unbind_channel();

	String lbl_channel_info_title();

	String lbl_element_new();

	String element_button_new();

	String element_lbl_title_configuration();

	String lbl_element_generic_title();

	String element_lbl_paramId();

	String element_lbl_numDec();

	String element_lbl_min();

	String element_lbl_measureUnit();

	String element_lbl_max();

	String element_lbl_avgPeriod();

	String element_lbl_avgPeriods();

	String lbl_acqPeriod();

	String element_boardBindInfo();

	String element_lbl_startOfScale();

	String element_maxVoltage();

	String element_lbl_endOfScale();

	String element_minVoltage();

	String session_ended();

	String select_element_name();

	String select_avg();

	String element_lbl_new_avgPeriod();

	String select_measure();

	String error_insert_avg();

	String alert_send_verify_ok();

	String alert_save();

	String alert_reload();

	String link_station_status();

	String link_analyzers_status();

	String link_informatic_status();

	String link_real_time_data();

	String station_status_title();

	String station_status_alarm_title();

	String station_status_alarm_id();

	String station_status_alarm_description();

	String station_status_alarm_value();

	String station_status_alarm_timestamp();

	String analyzers_status_title();

	String analyzers_status_fault_title();

	String analyzers_status_id();

	String analyzers_status_fault();

	String analyzers_status_data_valid();

	String analyzers_status_maintenance();

	String analyzers_status_calibration_manual();

	String analyzers_status_autocalibration();

	String analyzers_status_autocalibration_failure();

	String informatic_status_title();

	String informatic_status_software();

	String informatic_status_config();

	String informatic_status_boards();

	String informatic_status_disk_space();

	String real_time_title();

	String real_time_analyzer();

	String value();

	String last_istant_value();

	String instant_data();

	String data();

	String periodicity();

	// TODO: capire cosa mettere nelle stringhe nelle properties
	String alarm_low();

	String alarm_high();

	String warning_low();

	String warning_high();

	String warning();

	String ok();

	String alarm();

	String not_enabled();

	String measure_unit();

	String flag();

	String multiple_flag();

	String station_status_link_history();

	String lbl_start_date();

	String lbl_end_date();

	String analyzer_status_link_history();

	String analyzer_status_alarm_timestamp();

	String real_time_link_history();

	String chart();

	String chart_title();

	String analyzers_status_brand_model();

	String lbl_day();

	String error_open_chart();

	String lbl_alarm_notes();

	String real_time_link_means();

	String lbl_half_day();

	String lbl_avg_periods();

	String lbl_start_hour();

	String lbl_end_hour();

	String means_data_title();

	String lbl_csv();

	String lbl_table();

	String real_time_data();

	String means_data();

	String lbl_autoscale();

	String lbl_show_minmax();

	String lbl_field_separator();

	String lbl_decimal_separator();

	String fault_not_deleted();

	String data_valid_not_deleted();

	String element_lbl_speed_numDec();

	String element_lbl_speed_precision();

	String element_lbl_speed_max();

	String element_lbl_direction_numDec();

	String element_lbl_direction_precision();

	String element_lbl_speedRangeHigh();

	String element_lbl_speedBoardBindInfo();

	String element_directionBoardBindInfo();

	String element_lbl_speed_measureUnit();

	String element_lbl_acq_delay();

	String element_lbl_acq_duration();

	String element_lbl_valueForEvent();

	String element_lbl_acqOnRisingEdge();

	String lbl_alarm_confirm_delete();

	String lbl_analyzer_confirm_delete();

	String lbl_element_confirm_delete();

	String lbl_boards_confirm_delete();

	String lbl_fault_confirm_delete();

	String lbl_data_valid_confirm_delete();

	String lbl_boards_confirm_unbind();

	String trigger_alarm_button_unbind();

	String trigger_alarm_button_open_list_iouser();

	String select_trigger_alarm();

	String trigger_alarm_error_select_item();

	String confirm_unbind_trigger_alarm();

	String binding_ok();

	String binding_failed();

	String avg_button_delete();

	String rain_button_delete();

	String informatic_boardManagerInitStatus();

	String informatic_configured_initializedBoardsNumber();

	String informatic_loadConfigurationStatus();

	String informatic_saveNewConfigurationStatus();

	String informatic_acquisitionStarted();

	String informatic_global_status();

	String informatic_board_title();

	String informatic_configuration_title();

	String no_info();

	String not_available();

	String informatic_status_application();

	String informatic_file_system_status();

	String informatic_root();

	String informatic_tmp();

	String informatic_data();

	String lbl_station_gps();

	String informatic_gps_status();

	String gps_not_installed();

	String gps_installed();

	String gps_fix();

	String gps_2d_fix();

	String gps_3d_fix();

	String gps_app_error();

	String gps_read_error();

	String gps_no_fix();

	String gps_data();

	String gps_altitude();

	String gps_latitude();

	String gps_longitude();

	String lbl_analyzer_ip_port();

	String lbl_analyzer_network();

	String lbl_analyzer_tty_params();

	String lbl_analyzer_baud_rate();

	String dpa_analyzer_button_new();

	String lbl_dpa_analyzer();

	String select_brand();

	String brand_error_select_item();

	String select_model();

	String model_error_select_item();

	String lbl_generic_analyzer();

	String lbl_analyzer_host_name();

	String lbl_analyzer_tty_device();

	String element_lbl_acquisition_measureUnit();

	String element_lbl_range_low();

	String element_lbl_range_high();

	String interface_network();

	String interface_serial();

	String button_refresh();

	String element_lbl_analyzer_measureUnit();

	String element_error_select_item();

	String button_help();

	String element_lbl_linearizationCoefficient();

	String element_lbl_rangeLow();

	String element_lbl_correctionOffset();

	String element_lbl_rangeHigh();

	String element_lbl_correctionCoefficient();

	String element_lbl_linearizationOffset();

	String element_lbl_directionCorrectionOffset();

	String element_lbl_directionCorrectionCoefficient();

	String element_lbl_speedCorrectionOffset();

	String element_lbl_speedCorrectionCoefficient();

	String button_forward();

	String button_prev();

	String element_lbl_conversionCoefficient();

	String element_lbl_newAvgPeriod();

	String element_lbl_direction_measureUnit();

	String analyzer_min_voltage_extension();

	String analyzer_max_voltage_extension();

	String analyzer_differential();

	String lbl_station_connection_type();

	String lbl_station_time_out();

	String link_load_conf();

	String lbl_local_configuration();

	String lbl_storic();

	String button_load_local();

	String load_conf_title();

	String alert_activation_error();

	String alert_obsolete();

	String alert_historic();

	String alert_check_error();

	String conf_loaded();

	String lbl_limit();

	String lbl_insert_comment();

	String lbl_comment();

	String lbl_author();

	String error_conn_param();

	String lbl_loaded();

	String lbl_date();

	String lbl_from_cop();

	String lbl_list_station();

	String lbl_list_conf();

	String connection_error();

	String unexpected_error();

	String connet_cop();

	String informatic_board_failed_initialize();

	String informatic_dpa_status();

	String informatic_dpa_number();

	String activation();

	String active();

	String not_active();

	String lbl_analyzer_element_operation();

	String lbl_analyzer_calibration_status();

	String lbl_analyzer_maintenace_status();

	String calib_in_progress();

	String calib_not_in_progress();

	String maintenance_in_progress();

	String maintenance_not_in_progress();

	String active_maintenance();

	String disactive_maintenance();

	String disactive_calibration();

	String active_calibration();

	String lbl_element_calibration_title();

	String lbl_element_voltage();

	String lbl_element_read_value();

	String lbl_element_correct_value();

	String lbl_element_expected_value();

	String lbl_element_first();

	String lbl_element_second();

	String lbl_element_calculate();

	String lbl_element_acquire();

	String alert_correction();

	String alert_m();

	String alert_q();

	String alert_save_correction();

	String alert_correction_1_point();

	String alert_save_correction_1_point();

	String alert_use_old_coefficient();

	String alert_read_value_missing();

	String informatic_error_number();

	String informatic_error_number_application();

	String not_valid();

	String valid();

	String no_calibration();

	String no_data();

	String open_folder();

	String button_load_remote();

	String url();

	String read_value();

	String cannot_activate_calib();

	String cannot_activate_maintenance();

	String yes();

	String no();

	String msg_chart_not_implemented();

	String lbl_vect_speed();

	String lbl_vect_dir();

	String lbl_dev_std();

	String lbl_scalar_speed();

	String lbl_gust_speed();

	String lbl_gust_dir();

	String lbl_is_calm();

	String lbl_calm_percent();

	String lbl_station();

	String save_forbidden();

	String alert_pci_board_versions();

	String unexpected_server_error();

	String alert_save_cfg_when_page_is_modified();

	String alert_about_to_reload_cfg();

	String lbl_none();

	String warn_avg_periods();

	String view_deleted_analyzers();

	String raid();

	String smart();

	String future_data();

	String common_cfg_status();

	String unparsable();

	String consistency_error();

	String save_error();

	String load_error();

	String incompatible();

	String config_load_error();

	String config_start_error();

	String unknown();

	String error();

	String digital_outputs();

	String data_valid();

	String new_data_valid();

	String link_dgt_analyzers();

	String dgt_analyzers_title();

	String dgt_settings();

	String button_dgt_alarm();

	String button_dgt_parameter();

	String button_measure_parameter();

	String button_reset();

	String button_list_fault();

	String command();

	String arguments();

	String button_exec_button();

	String select_command();

	String read_date();

	String set_date();

	String reload_display();

	String not_enabled_function();

	String not_play();

	String execution_failure();

	String button_serial_number();

	String send_command();

	String err_not_numeric();

	String discard_not_valid();

	String lbl_driver_params();

	String drv_configs_locally_changed();

}
