#!/usr/bin/env bash

MASTER_ENV="/etc/profile.d/master-env.sh"
echo "export PATH=\"\$PATH:$VAGRANT_RES_DIR/deploy\"" > $MASTER_ENV
