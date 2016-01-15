#!/bin/bash

$SPARK_HOME/bin/spark-submit --class org.apache.spark.examples.SparkPi \
    --master yarn-cluster \
    --num-executors 2 \
    --executor-cores 1 \
    $SPARK_HOME/lib/spark-examples*.jar \
    100
