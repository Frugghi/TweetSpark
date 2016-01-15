export SPARK_HOME=/usr/local/spark
export PATH=${SPARK_HOME}/bin:${PATH}
export SPARK_DIST_CLASSPATH=$(hadoop classpath)
