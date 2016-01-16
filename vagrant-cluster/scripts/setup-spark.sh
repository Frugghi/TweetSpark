#!/bin/bash
source "/home/vagrant/resources/common.sh"

while getopts s: option
do
    case "${option}"
    in
        s) SPARK_VERSION=${OPTARG};;
    esac
done

function installLocalSpark {
    echo "Installing Spark $SPARK_VERSION from local archive"
    FILE=$VAGRANT_RES_DIR/$SPARK_ARCHIVE
    tar -xzf $FILE -C /usr/local
}

function installRemoteSpark {
    echo "Installing Spark $SPARK_VERSION from remote source"
    curl -o $VAGRANT_RES_DIR/$SPARK_ARCHIVE -O -L $SPARK_MIRROR_DOWNLOAD
    tar -xzf $VAGRANT_RES_DIR/$SPARK_ARCHIVE -C /usr/local
}

function setupSpark {
    echo "Copying over Spark configuration files"
    cp -f $VAGRANT_RES_DIR/spark/slaves $SPARK_CONF_DIR/slaves
    cp -f $VAGRANT_RES_DIR/spark/spark-env.sh $SPARK_CONF_DIR/spark-env.sh
}

function setupEnvVars {
    echo "Creating Spark environment variables"
    cp -f $SPARK_RES_DIR/spark.sh /etc/profile.d/spark.sh
}

function installSpark {
    if resourceExists $SPARK_ARCHIVE; then
        installLocalSpark
    else
        installRemoteSpark
    fi
    ln -s /usr/local/$SPARK_DIR /usr/local/spark
}

echo "Setup Spark"

setupSparkVariables "$SPARK_VERSION"
installSpark
setupSpark
setupEnvVars
