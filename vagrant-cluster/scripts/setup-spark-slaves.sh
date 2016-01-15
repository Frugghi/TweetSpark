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
	echo "$1" >> $SPARK_CONF_DIR/slaves
}

echo "Setup Spark slaves"

echo "Modifying $SPARK_CONF_DIR/slaves"
for i in $(seq 1 $TOTAL_SLAVES)
do 
	setupSlave "slave${i}"
done
