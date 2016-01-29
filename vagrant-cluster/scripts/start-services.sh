#!/usr/bin/env bash

START_HADOOP=0
START_SPARK=0

for i in "$@"
do
case $i in
    -h|--hadoop)
        START_HADOOP=1
        shift
        ;;
    -s|--spark)
        START_SPARK=1
        shift
        ;;
    *)
        ;;
esac
done

echo "Starting services..."
if [ $START_HADOOP -eq 1 ]; then
    echo "Starting Hadoop DFS..."
    ${HADOOP_PREFIX}/sbin/start-dfs.sh
    echo "Starting YARN..."
    ${HADOOP_PREFIX}/sbin/start-yarn.sh
    echo "Starting Job History..."
    ${HADOOP_PREFIX}/sbin/mr-jobhistory-daemon.sh start historyserver --config $HADOOP_CONF_DIR
fi

if [ $START_SPARK -eq 1 ]; then
    echo "Starting Spark..."
    ${SPARK_HOME}/sbin/start-all.sh
fi
