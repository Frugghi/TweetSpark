#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

while getopts i: option
do
    case "${option}"
    in
        i) INFLUXDB_VERSION=${OPTARG};;
    esac
done

function installLocalInfluxDB {
    echo "Installing InfluxDB $INFLUXDB_VERSION from local archive"
    FILE=$VAGRANT_RES_DIR/$INFLUXDB_RPM
    yum -y localinstall $FILE
}

function installInfluxDB {
    if resourceExists $INFLUXDB_RPM; then
        installLocalInfluxDB
    else
        exit 1
    fi
}

function startInfluxDB {
	service influxdb start
}

function setupInfluxDB {
	influx <<EOF
CREATE DATABASE profiler
CREATE USER profiler WITH PASSWORD 'profiler' WITH ALL PRIVILEGES
EXIT
EOF
}

function installDashboard {
	if [ -d "$VAGRANT_RES_DIR/profiler/dashboard" ]; then
		curl --silent --location https://rpm.nodesource.com/setup_4.x | bash -
		yum -y install gcc-c++ make nodejs
		cd $VAGRANT_RES_DIR/profiler/dashboard
		npm install
		node app
	fi
}

echo "Setup InfluxDB"

setupInfluxDBVariables "$INFLUXDB_VERSION"
installInfluxDB
startInfluxDB
setupInfluxDB
installDashboard
