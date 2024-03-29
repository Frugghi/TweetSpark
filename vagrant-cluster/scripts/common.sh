#!/usr/bin/env bash

#Common
VAGRANT_HDFS_DIR=$VAGRANT_RES_DIR/hdfs

#Java
JAVA_HOME=/usr/local/java
function setupJavaVariables {
    if [ $(uname -m) == 'x86_64' ]; then
        JAVA_ARCH="x64"
    else
        JAVA_ARCH="i586"
    fi
    JAVA_ARCHIVE="jdk-$1-linux-$JAVA_ARCH.gz"
    JAVA_MAJOR_VERSION=`echo $JAVA_ARCHIVE | cut -d '-' -f 2 | cut -c1`
    JAVA_BUILD_VERSION=`echo $JAVA_ARCHIVE | cut -d '-' -f 2 | cut -c3-4`
}

#Hadoop
HADOOP_PREFIX=/usr/local/hadoop
HADOOP_CONF=$HADOOP_PREFIX/etc/hadoop
HADOOP_RES_DIR=$VAGRANT_RES_DIR/hadoop

function setupHadoopVariables {
    HADOOP_VERSION="$1"
    HADOOP_DIR=hadoop-$HADOOP_VERSION
    HADOOP_ARCHIVE=$HADOOP_DIR.tar.gz
    HADOOP_MIRROR_DOWNLOAD=../resources/$HADOOP_ARCHIVE
}

#Spark
SPARK_PREFIX=/usr/local/spark
SPARK_CONF=$SPARK_PREFIX/conf
SPARK_RES_DIR=$VAGRANT_RES_DIR/spark

function setupSparkVariables {
    SPARK_VERSION="$1"
    SPARK_DIR=spark-$SPARK_VERSION-bin-without-hadoop
    SPARK_ARCHIVE=$SPARK_DIR.tgz
    SPARK_MIRROR_DOWNLOAD=../resources/$SPARK_ARCHIVE
}

#InfluxDB
function setupInfluxDBVariables {
    INFLUXDB_VERSION="$1"
    INFLUXDB_RPM=influxdb-$INFLUXDB_VERSION.x86_64.rpm
}

#SSH
SSH_RES_DIR=$VAGRANT_RES_DIR/ssh
RES_SSH_CONFIG=$SSH_RES_DIR/config

#Functions
function resourceExists {
    if [ -e "$VAGRANT_RES_DIR/$1" ]
    then
        return 0
    else
        return 1
    fi
}

function fileExists {
    if [ -e "$1" ]
    then
        return 0
    else
        return 1
    fi
}

function installLocalRepo {
	yum -y install yum-plugin-priorities

	repo=`echo "$1" | sed 's/^file:\/\///'`
	path=`dirname "$repo"`
	cd "$path"
	tar -xzvf "$repo" > /dev/null
	repo_name=`basename "$repo" .tar.gz`
	repo_path=`dirname $(find "$path/$reponame" -name 'repodata')`
	echo -e "[local]\nname=$repo_name\nbaseurl=file://$repo_path\nenabled=1\ngpgcheck=0\nprotect=1" > "/etc/yum.repos.d/$repo_name.repo"
}

function installRemoteRepo {
	yum -y install wget

	cd /etc/yum.repos.d/
	wget "$1"
}
