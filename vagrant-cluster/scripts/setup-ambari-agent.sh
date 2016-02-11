#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

if [[ "$1" == file* ]]; then
	echo "Installing local repo $1..."
	installLocalRepo "$1"
else
	echo "Installing remote repo $1..."
	installRemoteRepo "$1"
fi

echo "Installing Ambari agent..."
yum -y install ambari-agent
ambari-agent reset "monitor.cluster"

echo "Starting Ambari agent..."
ambari-agent start
