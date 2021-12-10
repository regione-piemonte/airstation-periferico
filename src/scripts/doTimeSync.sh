#!/bin/sh

NTP_SERVER_IP=$1
NTP_SERVER_TIMEOUT=5

sudo /usr/sbin/ntpdate -t $NTP_SERVER_TIMEOUT $NTP_SERVER_IP
result=$?
if [ $result -eq 0 ] ; then
  sudo /sbin/hwclock -w -u
else
  return $result
fi
