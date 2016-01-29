#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

function url_encode {
    local LANG=C
    local length="${#1}"
    for (( i = 0; i < length; i++ )); do
        local c="${1:i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf "$c" ;;
            *) printf '%%%02X' "'$c" ;; 
        esac
    done
}

for path in $VAGRANT_HDFS_DIR/*
do
  echo "Copying $path to HDFS..."
  encoded_path=`url_encode "$path"`
  hadoop fs -put "$encoded_path" "/"
done
