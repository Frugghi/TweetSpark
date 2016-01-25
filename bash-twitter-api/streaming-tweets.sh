#!/bin/bash

export PATH=$PATH:.
source twitter-api

# Helper functions
function print-help {
  echo ""
  echo "Usage: $0 [options]"
  echo ""
  echo "Options:"
  echo " -t    the phrases which will be used to determine what Tweets will be delivered on the stream"
  echo " -l    restricts tweets to the given language, given by an ISO 639-1 code"
  echo " -p    bounding boxes to filter Tweets by. <south, west, north, east>"
  echo " -f    the users whose Tweets should be delivered on the stream"
  echo " -n    set the stream name, output file will be <name>.json"
  echo " -s    use sample endpoint instead of filter"
  echo ""
  exit 1
}

# Params parsing
LANG=""
TRACK=""
PLACES=""
FOLLOW=""
NAME="stream"
API="stream"
while getopts 't:l:p:f:n:s' arg
do
  case ${arg} in
    l) LANG="${OPTARG}" ;;
    n) NAME="${OPTARG}" ;;
    s) API="sample" ;;
    t) if [ -z "$TRACK" ]; then
        TRACK="${OPTARG}"
       else
        TRACK="$TRACK,${OPTARG}"
       fi ;;
    p) if [ -z "$PLACES" ]; then
        PLACES="${OPTARG}"
       else
        PLACES="$PLACES,${OPTARG}"
       fi ;;
    f) if [ -z "$FOLLOW" ]; then
        FOLLOW="${OPTARG}"
       else
        FOLLOW="$FOLLOW,${OPTARG}"
       fi ;;
  esac
done

if [ "$API" == "stream" -a -z "$TRACK" -a -z "$FOLLOW" -a -z "$PLACES" ]; then
  print-help
  exit 1
fi

# Body
init-stream-api

trap "echo" EXIT

output="$NAME.json"
count=0
if [ -e "$output" ]; then
  printf "\r\e[0KIntegrity checking..."
  tmpfile=`mktemp`
  filter-tweets "$output" > "$tmpfile"
  if cmp -s "$tmpfile" "$output"; then
    rm -f "$tmpfile"
  else
    mv -f "$tmpfile" "$output"
  fi
  count=`wc -l "$output" | egrep -o '[0-9]+' | head -n 1`
fi

printf "\r\e[0KInitializing stream..."
$API -l "$LANG" -t "$TRACK" -f "$FOLLOW" -p "$PLACES" \
      | monitor-control-messages \
      | filter-tweets \
      | tee -a "$output" \
      | print-streaming-info -c "$count" -n "$output"
