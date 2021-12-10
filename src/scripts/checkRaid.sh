#!/bin/sh

if [ -f /usr/local/bin/check_raid.sh ]; then
  /usr/local/bin/check_raid.sh $* 2>&1
else
  return 0
fi