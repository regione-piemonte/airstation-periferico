#!/bin/sh

if [ -f /usr/local/sbin/check_smart.sh ]; then
  sudo /usr/local/sbin/check_smart.sh $* 2>&1
else
  return 0
fi