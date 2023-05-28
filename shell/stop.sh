#!/bin/bash

APPLICATION_DIR=/data/cat
APPLICATION_PID_FILE="$APPLICATION_DIR/run/application.pid"

TIME_WAIT=1
echo "zk remove success, wait for $TIME_WAIT seconds"
sleep $TIME_WAIT


if [ ! -f $APPLICATION_PID_FILE ]; then
    echo "no pid file for this application found, expect $APPLICATION_PID_FILE"
    exit 0
fi

PID=`cat $APPLICATION_PID_FILE`

if [ ! -n "$PID" ]; then
    echo "no pid for this application found"
    exit 0
fi

echo "get pid $PID"

if test $( ps ax | awk '{ print $1 }' | grep -e "^$PID$" |wc -l ) -eq 0
then
    echo "the process $PID is stopped"
    exit 0
fi

echo "kill $PID"

`kill $PID`

start=`date +%s`
max=10
while true
do
    if test $( ps ax | awk '{ print $1 }' | grep -e "^$PID$" |wc -l ) -eq 0
    then
        echo "kill $PID success"
        exit 0
    fi
    sleep 1
    current=`date +%s`
    if (($current-$start>$max))
    then
        break
    fi
done

echo "try to force kill $PID"
`kill -9 $PID`

start=`date +%s`
max=10
while true
do
    if test $( ps ax | awk '{ print $1 }' | grep -e "^$PID$" |wc -l ) -eq 0
    then
        echo "kill $PID success"
        exit 0
    fi
    sleep 1
    current=`date +%s`
    if (($current-$start>$max))
    then
        echo "fail to kill $PID, timeout"
        exit 1
    fi
done