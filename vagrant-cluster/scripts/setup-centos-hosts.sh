#!/bin/bash

function addEntry {
	entry="$1 $2"
	echo "Adding ${entry}"
	echo "${entry}" >> /etc/nhosts
}

function setupHosts {
	echo "Modifying /etc/hosts file"
	addEntry "$1" "master"
	counter=0
	for ip in "${@:2}"
	do 
		counter=$((counter+1))
		addEntry "${ip}" "slave${counter}"
	done
	echo "127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4" >> /etc/nhosts
	echo "::1         localhost localhost.localdomain localhost6 localhost6.localdomain6" >> /etc/nhosts
	#cat /etc/hosts >> /etc/nhosts
	cp /etc/nhosts /etc/hosts
	rm -f /etc/nhosts
}

echo "Setup CentOS hosts file"

setupHosts "$@"
