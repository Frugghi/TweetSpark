#!/usr/bin/env bash

STOP_HADOOP=0
STOP_SPARK=0

for i in "$@"
do
case $i in
    -h|--hadoop)
        STOP_HADOOP=1
        shift
        ;;
    -s|--spark)
        STOP_SPARK=1
        shift
        ;;
    *)
        ;;
esac
done

echo "Stopping services..."
if [ $STOP_HADOOP -eq 1 ]; then
    echo "Stopping Hadoop DFS..."
    ${HADOOP_PREFIX}/sbin/stop-dfs.sh
    echo "Stopping YARN..."
    ${HADOOP_PREFIX}/sbin/stop-yarn.sh
    echo "Stopping Job History..."
    ${HADOOP_PREFIX}/sbin/mr-jobhistory-daemon.sh stop historyserver --config $HADOOP_CONF_DIR
fi

if [ $STOP_SPARK -eq 1 ]; then
    echo "Stopping Spark..."
    ${SPARK_HOME}/sbin/stop-all.sh
fi
