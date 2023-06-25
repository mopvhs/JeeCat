#!/bin/sh

#JAVA=/opt/jdk-17/bin/java
JAVA=java
APPLICATION_JAR=cat.jar
APPLICATION_DIR=/data/cat
APPLICATION_LOG_DIR="$APPLICATION_DIR/logs"
APPLICATION_PID_FILE="$APPLICATION_DIR/run/application.pid"

HERA_OPTS="-Dxxl.job.ip=${EXTRANET} -Dspring.profiles.active=prod -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

export JAVA_OPTS="${JAVA_OPTS} \
-server -Xmx5120m -Xms5120m -Xmn1g -XX:MaxTenuringThreshold=15 -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -Xss512k -XX:SurvivorRatio=8 \
-XX:ConcGCThreads=2 -XX:+UseG1GC -XX:G1HeapRegionSize=4m -XX:MaxGCPauseMillis=200 \
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${APPLICATION_LOG_DIR} \
"

#SPRING_PROFILES="--spring.pid.file=$APPLICATION_PID_FILE"

#nohup ${JAVA} ${JAVA_OPTS} ${HERA_OPTS} -jar ${APPLICATION_JAR} $SPRING_PROFILES 1>/dev/null 2>&1 &
nohup ${JAVA} ${JAVA_OPTS} ${HERA_OPTS} -jar ${APPLICATION_DIR}/${APPLICATION_JAR} > ${APPLICATION_DIR}/cat.log  2>&1 &

WAIT_TIME=20
echo "wait for $WAIT_TIME seconds to check wether the process is running"
sleep $WAIT_TIME

if [ ! -f $APPLICATION_PID_FILE ]; then
    echo "no pid file for this application found, expect $APPLICATION_PID_FILE"
    exit 1
fi

PID=`cat $APPLICATION_PID_FILE`
if [ ! -n "$PID" ]; then
    echo "no pid for this application found"
    exit 1
fi

echo "get pid $PID"

if test $( ps ax | awk '{ print $1 }' | grep -e "^$PID$" |wc -l ) -eq 0
then
    echo "the process $PID is not running, please check log"
    exit 1
fi

echo "application started, pid:$PID"