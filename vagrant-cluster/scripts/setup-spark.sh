#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

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
    tar -xf $FILE -C /usr/local
}

function setupSpark {
    echo "Copying over Spark configuration files"
    cp -f $VAGRANT_RES_DIR/spark/slaves $SPARK_CONF/slaves
    cp -f $VAGRANT_RES_DIR/spark/spark-env.sh $SPARK_CONF/spark-env.sh
}

function setupEnvVars {
    echo "Creating Spark environment variables"
    cp -f $SPARK_RES_DIR/spark.sh /etc/profile.d/spark.sh
}

function installSpark {
    if resourceExists $SPARK_ARCHIVE; then
        installLocalSpark
    else
        exit 1
    fi
    ln -s /usr/local/$SPARK_DIR $SPARK_PREFIX
}

echo "Setup Spark"

setupSparkVariables "$SPARK_VERSION"
installSpark
setupSpark
setupEnvVars
