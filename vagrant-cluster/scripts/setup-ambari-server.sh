#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

if [[ "$1" == file* ]]; then
	echo "Installing local repo $1..."
	installLocalRepo "$1"
else
	echo "Installing remote repo $1..."
	installRemoteRepo "$1"
fi

echo "Installing Ambari server..."
yum -y install ambari-server
ambari-server setup --silent -j "$JAVA_HOME"

echo "Starting Ambari server..."
ambari-server start
