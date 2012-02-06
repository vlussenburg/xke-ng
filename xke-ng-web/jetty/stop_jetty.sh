#!/bin/bash
 
#kill current installation
PIDS=`ps -ef |grep "jetty" | grep -v "grep" | awk '{ print $2 }'`
echo  $PIDS
for PID in $PIDS
do
if ! sudo kill $PID > /dev/null 2>&1; then
    echo "No need to kill process $PID"
else
    echo "Killed process $PID"
fi
done
