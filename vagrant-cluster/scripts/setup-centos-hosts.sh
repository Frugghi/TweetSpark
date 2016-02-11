#!/usr/bin/env bash

tmpfile=`mktemp`

function addEntry {
    if [ -z `hostname` ]; then
	    entry="$1 $2"
	else
	    entry="$1 $2 $2.$(hostname | rev | cut -d'.' -f1 | rev)"
	fi
    echo "Adding ${entry}"
    echo "${entry}" >> "$tmpfile"
}

function setupHosts {
    echo "Modifying /etc/hosts file"
    echo "127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4" >> "$tmpfile"
    echo "::1         localhost localhost.localdomain localhost6 localhost6.localdomain6" >> "$tmpfile"
	
	addEntry "$1" "monitor"
    addEntry "$2" "master"
    counter=0
    for ip in "${@:3}"
    do 
        counter=$((counter+1))
        addEntry "${ip}" "slave${counter}"
    done

    cp "$tmpfile" /etc/hosts
    rm -f "$tmpfile"
	
	echo -e "NETWORKING=yes\nHOSTNAME=$(hostname)" > /etc/sysconfig/network
}

echo "Setup CentOS hosts file"

setupHosts "$@"
