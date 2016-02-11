#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

echo "Installing NTP..."
yum -y install ntp

chkconfig ntpd on
service ntpd start
