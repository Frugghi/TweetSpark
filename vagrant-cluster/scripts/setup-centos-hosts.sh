#!/usr/bin/env bash

tmpfile=`mktemp`

function addEntry {
    entry="$1 $2"
    echo "Adding ${entry}"
    echo "${entry}" >> "$tmpfile"
}

function setupHosts {
    echo "Modifying /etc/hosts file"
    echo "127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4" >> "$tmpfile"
    echo "::1         localhost localhost.localdomain localhost6 localhost6.localdomain6" >> "$tmpfile"

	if [ `hostname` == "monitor" ]; then
        addEntry "127.0.0.1" "monitor"
	fi
	
    addEntry "$1" "master"
    counter=0
    for ip in "${@:2}"
    do 
        counter=$((counter+1))
        addEntry "${ip}" "slave${counter}"
    done

    cp "$tmpfile" /etc/hosts
    rm -f "$tmpfile"
}

echo "Setup CentOS hosts file"

setupHosts "$@"
