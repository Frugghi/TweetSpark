#!/bin/bash

function disableFirewall {
	echo "Disabling firewall"
	if [ -e /usr/lib/systemd ]; then
		# CentOS 7
		systemctl mask firewalld
		systemctl stop firewalld
		chkconfig firewalld off
	else
		# CentOS 6
		service iptables save
		service iptables stop
		chkconfig iptables off
	fi
}

echo "Setup CentOS"

disableFirewall
