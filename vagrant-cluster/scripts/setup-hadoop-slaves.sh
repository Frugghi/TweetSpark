#!/bin/bash
source "/home/vagrant/resources/common.sh"

while getopts t: option
do
    case "${option}"
    in
        t) TOTAL_SLAVES=${OPTARG};;
    esac
done

function setupSlave {
    echo "Adding $1"
    echo "$1" >> $HADOOP_CONF/slaves
}

echo "Setup Hadoop slaves"

echo "Modifying $HADOOP_CONF/slaves"
for i in $(seq 1 $TOTAL_SLAVES)
do 
    setupSlave "slave${i}"
done
