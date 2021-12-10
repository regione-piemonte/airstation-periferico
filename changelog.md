# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep Changelog](https://keepachangelog.com/en/1.0.0/)

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [3.5.0] - 2021-11-20
### Added
- Added support for running on Raspberry PI platform
- Added support for running on Windows 10 platform
- Added function to proxy the web UI of analyzers using Jetty proxy function
  of Periferico and Centrale (only for http protocol)

###Changed
- Replaced obsolete RXTX serial communication library with jSerialComm

## [3.4.0] - 2021-05-26
### Added
- Added support for analyzers with graphical display
- Added support for newer gpsd with Json protocol
- Added support for running on AriaLinux based on 
  Ubuntu 18.04 64-bit and 20.04 64-bit

## [3.3.0] - 2016-12-16
- Added support for Advantech Adam serial boards

## [3.2.1] - 2016-11-04
### Added
- Added support for digital analyzers with automatic calibration function

### Changed
- Station alarms are now shown in alphabetic order

### Fixed
- Bug fix: Driver Manager returns no driver configurations for analyzers 
  without elements, when requested for equivalent configurations
- Bug fix: data not valid for analyzer in AvgElement does not set mean data
  as not valid
- Bug fix: negative linearization offsets for AVG analyzers not accepted by UI
- Bug fix: negative correction offsets not possible for Wind analyzers
- Bug fix: when digital analyzer has clock in the future of at least one period
  of average, data are written in files more than once for DataPortAvgElement
- Bug fix: hexadecimal fault values equivalent to negative integers are not
  parsed back from data files
- Bug fix: in case of error during analog acquisition every error is written
  to logs, when only the first one should be written to avoid log saturation

## [3.2.0] - 2015-11-19
### Added
- Implemented a plugin API for the drivers of data port analyzers
- Added function to COP Service (now v1.0.3) to query driver versions and
  added driver configuration status to informatic status function
  
### Changed
- API driver removed from Periferico sources and ported to the new plugin API
- Update of third party libraries to conform with CSI Repart repository
- Common configuration classes are now packaged in a separate jar

### Fixed
- Fix for new compilation warnings discovered by Eclipse Luna
- Updated RXTX to latest available version, added open/close logs and 3.5s
  delay after close

## [3.1.3] - 2015-02-19
### Changed
- For Old API Driver, the fault definition list is not read any longer when
  fault read is disabled
- The list of analyzers in the UI is now ordered by analyzer name

### Fixed
- Bug fix: when Common Config is sent from COP, the auto reset period for
  manual operations is not updated until Periferico restarts
- Bug fix: when an invalid Common Config is sent from COP and then a valid
  Common Config is sent from COP, Periferico continues to show an error status
  
### Added
- Added configuration files for API M300 family

## [3.1.2] - 2014-01-24
### Added
- Added support for data validity signal available for some analyzers with
  the choice to use it to discard data or tag with warning only
- The list of status information for the analyzers now uses human readable names
- Added system property "periferico.proxypatch.regex" to configure the patch
  needed to have UI working correctly behind proxy
- Added system property "periferico.gc" to force periodic garbage collecting
- Added memory usage logs

### Changed
- Api driver: improvement to extend support for various firmware releases
- Api driver: implemented read from analyzer of the warning definition list
- Real time data view is now ordered by analyzer name
- Changed some logs from info to debug

### Fixed
- Bug fix: added missing auto deletion for old wind data files

## [3.1.1] - 2013-06-12
### Added
- Browser default language is now used correctly with modern browsers
- Session cookie has now a unique name for each station, allowing to use the
  web interface for more than one Periferico at a time, from Centrale web
  interface
- User interface improvement to have error management working with Centrale's
  reverse proxy
- Added support for configuration conversion, needed to recycle current
  configuration for a new station
    
### Fixed
- Bug fix: the name for the download of csv files is incomplete when contains
  spaces
- Fixed all the warnings related to generics for UI code
 
### Changed
- Updated GWT from 2.0.0 to 2.5.1 to improve browser compatibility
- Use of new GWT functions instead of deprecated functions
- Moved gwt-servlet.jar from UI servlet archive to lib dir to reduce servlet
  archive size
- When errors are detected on measure units from common configuration, error
  messages are more detailed


## [3.1.0] - 2012-10-15
### Added
- Added support for SMART and RAID errors detection
- New low level interface to data port analyzers, more robust and flexible with
  a lot of new features useful to develop drivers for a wide range of protocols
- More robust support for serial port communication
- Switched COMM API from Sun to RxTx
- API protocol: added support for reading fault status from analyzers with
  old protocol
- API protocol improvement: unrequested messages from the analyzers are skipped
- API protocol improvement: data with value 'XXXXXX' are read as null data
  instead of throwing a protocol exception
- API protocol improvement: acquisition is now configured in 'Computer Mode',
  but only for analyzers with old protocol
- Changed symmetric range for analog analyzers with lower bound extension for
  voltage range
- Added upper bound extension for voltage range
- Manual calibration improvement: when manual calibration is active for a given
  analyzer, data is acquired at maximum speed to have quick response in
  calibration panel
- Added function to unbind logical acquisition boards from physical hardware
- Added to informatic status an alarm for data in the future
- Added to informatic status the activation status for Common Config sent from
  COP
- Wind analyzer: added precision settings for speed and direction, in order
  to accept slightly negative values and direction above 360 degrees
- Implemented centralized management for data rounding
- Added decimal digits rounding at acquisition level, before comparison with
  prevalidation thresholds
- Decimal digits rounding set to "round towards positive infinity" for
  aggregated values and to "round to even" for sample data, instead of
  "round to even" for all types of data
- Added check for rain analyzer to prevent data loss if the number of decimals
  is not compatible with the value for event
- In the UI negative 0 is now printed without leading '-'
- Added third optional decimal in graph Y scale
- Improved Common Config validation check: when a Common Config for a given
  parameter does not have any measure unit available and enabled for acquisition
  and/or for analyzers it is rejected as invalid
- Improved error message in the element calibration panel
- Added auto deletion for old gps data files, using the same logic of sample
  data files
- The alarm led in "Informatic Status" for gps no fix is now yellow instead of
  red
- Data Port Analyzer UI: enlarged host name / IP field
- Added check for aggregation period in AVG elements
- Added attribute volatile to some thread variables to improve shutdown
  correctness
- Incremented startup and shutdown timeouts in the start/stop script to be able
  to wait more than the maximum startup and shutdown time possible for the
  application
  
### Fixed
- Bug fix: initial status for manual operations on application start is written
  to files for deleted and disabled analyzers
- Bug fix: added pre validation check implementation for counter element
- Bug fix: tooltip correction for disk usage status
- Bug fix: added space in some table cells for correct rendering with IE 6 and 7
- Bug fix: correction of some strings related to decimal digits management
- Bug fix: wind direction is now written to files using its number of decimal
  digits and not speed number
- Bug fix: wind calm percentage is now written to files using one decimal digit
  as UI does instead of using speed number of decimal digits
- Bug fix: italian translation of some alarm related strings
- Bug fix: wind correction coefficients and offsets are now saved into
  configuration file
- Bug fix: when gps is changed from installed to not installed, gps information
  is not cleared from informatic status page
- Bug fix: data file system usage shows 100% before data folder is created
- Bug fix: new analyzer page is now coherent with analyzer type when more than
  one new analyzer is created
- Bug fix: added automatic input unbind when the analyzer's fault is deleted
- Bug fix: added close of not used streams of Process objects

## [3.0.9] - 2011-07-12
### Fixed
- Bug fix: XML files were not closed after write operations

### Added
- Added support for old analyzers to API driver

## [3.0.8] - 2010-12-22
### Changed
- Modified the user interface client to avoid having all the client java classes
  in the user interface archive, saving about 500 KB
- Configured Jetty request log using the application's time zone,
  instead of GMT-1
 
### Added
- Log file improvement: added the printout of the exception that may occur
  during loading/parsing of station configuration
  
### Fixed
- Fix for acquisition using differential mode: analog reference differential was
  not set 

## [3.0.7] - 2010-01-19
### Fixed
- Fix for spurious rain event on application startup when rain acquisition
  trigger is configured on the second front of the signal

### Added
- The configuration is now released separately from binary package to avoid
  overwriting the configuration when updating the software
- Increased startup timeout for console log messages from 45 to 60 s

## [3.0.6] - 2010-01-18
### Added
- Rain aggregations are now computed, even when there is no rain
- Wind aggregations are now computed, even when there is less than 75% of 
  expected data
- Modified the usage of SimpleDateFormat parse() and format() functions, that
  may lead to errors when concurrent usage should occur
- Added support for Internet Explorer 8
- In instant data page, corrected error that occurred when only the minutes
  button was pressed
- Enlarged the drop down list of aggregation periods for viewing aggregate data
- Enlarged title field for all pages 
- Improved Internet Explorer support for real time data page
- In analyzer configuration page, the next analyzer arrow is now disabled on
  last analyzer, even when there are deleted analyzers in configuration
- Station configuration page: in connection configuration page the connection
  type does not loose the correct value any longer
- Load configuration page: configuration from COP tab is now updated
  automatically when a configuration is loaded 

### Removed
- Removed a debug print from startup.log

## [3.0.5] - 2009-11-04
### Added
- Added the possibility to specify options for comedi kernel module in
  comedi.xml configuration file: this allows using ISA boards and test boards
  with the latest CVS versions of Comedi, needed to run Comedi on Ubuntu
  8.10 or higher
  
### Fixed
- Corrected maintenance and calibration indicators functionality in analyzer
  configuration page for wind and rain analyzers
- Enlarged acquisition period column in DPA analyzer alarm
- Corrected bug in DPA analyzer driver that leads to stop data acquisition,
  after more than 120 configuration or common configuration updates, without
  restarting the application

## [3.0.4] - 2009-10-19
### Added
- Improved reliability of application threads shutdown, adding wait for each
  thread to terminate, where not implemented, and correcting it, where not
  working correctly
  
### Fixed
- Corrected occasional recomputation of aggregate data when setting new common
  configuration
- In the real time data page, deleted analyzers are shown only pushing a new
  dedicated button
- In the analyzers status page, deleted analyzers are not shown any more

## [3.0.3] - 2009-10-14
### Added
- Added application version message in startup logs
- Added platform character set message in startup log
- Forced UTF-8 encoding for reading and writing XML configuration files
- Forced UTF-8 as default JVM encoding in periferico startup script
- Added automatic backup of common configuration when a new common configuration
  is sent by Centrale application
- Added configuration reload button in "Load Configuration" UI page
- Added warning when no aggregation period is set for an element

### Fixed
- Corrected UI error when entering board page on new board event

## [3.0.2] - 2009-10-06
### Added
- Added missing inversion logic for Digital Alarm

## [3.0.1] - 2009-07-07
### Added
- When no sample data is available for a given aggregation period, the
  aggregation is not computed.
- When the application is started or restarted, it checks if data files
  contain data in the future; in this case a warning message is shown in the
  command shell. If data in the future is found, data in the future can be
  deleted starting or restarting the application with the parameter
  'delete_future_data'.
- In the "Real time data" page the acquisition period is shown immediately
  rather than when the first sample is acquired; the acquisition period is
  shown only for enabled sensors.
- Added execute permission to the scripts in bin folder packaged into the
  release tar archive.

## [3.0.0] - 2009-05-08
- Initial release 

