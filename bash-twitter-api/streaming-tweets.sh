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
  echo ""
  exit 1
}

# Params parsing
LANG=""
TRACK=""
PLACES=""
FOLLOW=""
NAME="stream"
while getopts 't:l:p:f:n:' arg
do
   case ${arg} in
     l) LANG="${OPTARG}" ;;
     n) NAME="${OPTARG}" ;;
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

# Body
init-stream-api

count=0
echo -n "Init stream..."
while IFS='\n' read line ; do
  count=$((count+1))
  printf "\r\e[0KTweets received: $count"
  echo "$line" >> "$NAME.json"
done < <( stream -l "$LANG" -t "$TRACK" -f "$FOLLOW" -p "$PLACES" )
