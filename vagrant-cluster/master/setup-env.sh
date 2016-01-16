#!/usr/bin/env bash

MASTER_ENV="/etc/profile.d/master-env.sh"
echo 'export PATH="$PATH:/home/vagrant/resources/deploy"' > $MASTER_ENV
