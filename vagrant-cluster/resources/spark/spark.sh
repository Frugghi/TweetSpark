export SPARK_HOME=/usr/local/spark
export SPARK_CONF_DIR=$SPARK_HOME/conf
export PATH=${SPARK_HOME}/bin:${PATH}
export SPARK_DIST_CLASSPATH=$(hadoop classpath)
