#!/bin/bash

# Periferico startup script

#### User configurable variables - BEGIN
IP_PORT=55000
COMEDI_ENABLE="true"
#### User configurable variables - END

export LANG=it_IT.UTF-8
export PATH=./bin:$PATH
ARC=`uname -m`
if [ "$ARC" = "x86_64" ]; then
  export LD_LIBRARY_PATH=bin/lib64
elif [ "$ARC" = "i686" ]; then
  export LD_LIBRARY_PATH=bin
else
  COMEDI_ENABLE="false"
fi
JAVAVER=`javap -verbose java.lang.String | grep "major version" | cut -d " " -f5`
if [[ $JAVAVER -gt 52 ]]; then
    JAVA_EXTRA_CP="bin/jaxb-api-2.3.1.jar:bin/jaxb-core-2.3.0.1.jar:bin/jaxb-runtime-2.3.0.1.jar:"
else
    JAVA_EXTRA_CP=""
fi
cd `dirname $0`
mkdir -p log
exec java\
 -cp\
 "$JAVA_EXTRA_CP"\
bin/servlet-api-2.5.jar\
:bin/jetty-6.1.26.jar\
:bin/jetty-util-6.1.26.jar\
:bin/log4j-1.2.16.jar\
:bin/slf4j-api-1.5.11.jar\
:bin/slf4j-log4j12-1.5.11.jar\
:bin/castor-core-1.3.3.jar\
:bin/castor-xml-1.3.3.jar\
:bin/commons-logging-1.1.3.jar\
:bin/commons-lang-2.6.jar\
:bin/commons-codec-1.7.jar\
:bin/xercesImpl-2.9.0.jar\
:bin/jfreechart-1.0.12.jar\
:bin/jcommon-1.0.15.jar\
:bin/gwt-servlet-2.7.0.jar\
:bin/jSerialComm-2.7.0.jar\
:bin/gson-2.8.6.jar\
:bin/jcomedilib-1.1.0.jar\
:bin/periferico-common-1.0.0.jar\
:bin/periferico-drv-conn-1.1.0.jar\
:bin/periferico-drv-itf-1.1.0.jar\
:bin/periferico-drv-impl-1.1.0.jar\
:bin/periferico.jar\
:bin/drivers/*\
 -Dfile.encoding=UTF-8\
 -Djava.awt.headless=true\
 -Duser.timezone=GMT+1\
 -Dperiferico.port=$IP_PORT\
 -Dperiferico.comedi=$COMEDI_ENABLE\
 it.csi.periferico.Periferico "$@" > log/startup.log 2>&1