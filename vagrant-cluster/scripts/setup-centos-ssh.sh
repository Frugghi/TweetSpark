#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

INSTALL=0
AUTHORIZE=0

for i in "$@"
do
case $i in
    -i|--install)
        INSTALL=1
        shift
        ;;
    -a|--authorize)
        AUTHORIZE=1
        shift
        ;;
    *)
        ;;
esac
done

function installInsecureKeys {
    echo "Installing insecure SSH key"
    mkdir -p ~/.ssh
    cp -f $RES_SSH_CONFIG ~/.ssh
    cp -f $SSH_RES_DIR/vagrant ~/.ssh/id_rsa
    cp -f $SSH_RES_DIR/vagrant.pub ~/.ssh/id_rsa.pub

    chmod go-w ~ ~/.ssh
    chmod 600 ~/.ssh/id_rsa
    chmod 600 ~/.ssh/id_rsa.pub
}

function authorizeInsecureKeys {
    echo "Authorizing insecure SSH key"
    mkdir -p ~/.ssh
    cat $SSH_RES_DIR/vagrant.pub >> ~/.ssh/authorized_keys

    chmod go-w ~ ~/.ssh
    chmod 600 ~/.ssh/authorized_keys
    chown `whoami` ~/.ssh/authorized_keys
}

echo "Setup SSH"

if [ $INSTALL -eq 1 ]; then
    installInsecureKeys
fi

if [ $AUTHORIZE -eq 1 ]; then
    authorizeInsecureKeys
fi
