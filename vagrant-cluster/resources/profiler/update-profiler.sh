#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

COMMENT=""
OPTIONS="$1"
if [ -z "$OPTIONS" ]; then
	COMMENT="#"
fi

sed -i -r "s|#?export _JAVA_OPTIONS=.*|${COMMENT}export _JAVA_OPTIONS=\"${OPTIONS//USERNAME_HERE/hadoop}\"|" $HADOOP_CONF/hadoop-env.sh
sed -i -r "s|#?export _JAVA_OPTIONS=.*|${COMMENT}export _JAVA_OPTIONS=\"${OPTIONS//USERNAME_HERE/mapred}\"|" $HADOOP_CONF/mapred-env.sh
sed -i -r "s|#?export _JAVA_OPTIONS=.*|${COMMENT}export _JAVA_OPTIONS=\"${OPTIONS//USERNAME_HERE/yarn}\"|" $HADOOP_CONF/yarn-env.sh
sed -i -r "s|#?spark.driver.extraJavaOptions .*|${COMMENT}spark.driver.extraJavaOptions ${OPTIONS//USERNAME_HERE/spark-driver}|" $SPARK_CONF/spark-defaults.conf
sed -i -r "s|#?spark.executor.extraJavaOptions .*|${COMMENT}spark.executor.extraJavaOptions ${OPTIONS//USERNAME_HERE/spark-executor}|" $SPARK_CONF/spark-defaults.conf
