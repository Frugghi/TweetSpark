#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

while getopts h: option
do
    case "${option}"
    in
        h) HADOOP_VERSION=${OPTARG};;
    esac
done

function installLocalHadoop {
    echo "Installing Hadoop $HADOOP_VERSION from local archive"
    FILE=$VAGRANT_RES_DIR/$HADOOP_ARCHIVE
    tar -xf $FILE -C /usr/local
}

function setupHadoop {
    echo "Creating Hadoop directories"
    mkdir /var/hadoop
    mkdir /var/hadoop/hadoop-datanode
    mkdir /var/hadoop/hadoop-namenode
    mkdir /var/hadoop/mr-history
    mkdir /var/hadoop/mr-history/done
    mkdir /var/hadoop/mr-history/tmp
    
    echo "Copying over Hadoop configuration files"
    cp -f $HADOOP_RES_DIR/*.xml $HADOOP_CONF
    cp -f $HADOOP_RES_DIR/*-env.sh $HADOOP_CONF
}

function setupEnvVars {
    echo "Creating Hadoop environment variables"
    cp -f $HADOOP_RES_DIR/hadoop.sh /etc/profile.d/hadoop.sh
}

function installHadoop {
    if resourceExists $HADOOP_ARCHIVE; then
        installLocalHadoop
    else
        exit 1
    fi
    ln -s /usr/local/$HADOOP_DIR $HADOOP_PREFIX
}


echo "Setup Hadoop"

setupHadoopVariables "$HADOOP_VERSION"
installHadoop
setupHadoop
setupEnvVars
