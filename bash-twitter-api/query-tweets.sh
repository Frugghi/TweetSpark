#!/bin/bash

export PATH=$PATH:.
source twitter-api
source transformers

# Helper functions
function print-help {
  echo ""
  echo "Usage: $0 [options]"
  echo ""
  echo "Options:"
  echo " -q    search by terms"
  echo " -h    search by hashtags"
  echo " -l    restricts tweets to the given language, given by an ISO 639-1 code"
  echo " -c    the number of tweets to return"
  echo " -r    reverse geocode places, <lat,lon>"
  echo ""
  exit 1
}

function do-search {
  local OPTIND arg
  local query=""
  local total=0
  local lang
  local transformers=()
  while getopts 'q:c:l:t:' arg
  do
      case ${arg} in
          q) query=${OPTARG} ;;
          c) total=${OPTARG} ;;
          l) lang=${OPTARG} ;;
          t) transformers[${#transformers[@]}]=${OPTARG} ;;
      esac
  done

  basename="$query"
  QUERY="$query -filter:retweets"

  page=0
  page_id=""
  for (( i=$total; i>0; i=i-100 ))
  do
    count=$i
    if [ "$count" -gt 100 ]; then
      count=100
    fi

    echo "Downloading page $page ($count tweets)..."
    output="${basename}-page${page}.json"
    if [ -z "$page_id" ]; then
      search -q "$query" -c "$count" -l "$lang" -e "1" > "$output"
    else
      search -q "$query" -c "$count" -l "$lang" -e "1" -i "$page_id" > "$output"
    fi
    transform "$output" "${transformers[@]}"

    page_id=`jq '.search_metadata.next_results' "$output" | egrep -o 'max_id=[0-9]+' | cut -d'=' -f2`
    page=$((page+1))
  done
}

# Params parsing
COUNT=0
RATE_LIMIT=0
while getopts q:l:c:h:r:a: arg
do
   case ${arg} in
      q) QUERY=${OPTARG} ;;
      h) QUERY="#${OPTARG}" ;;
      l) LANG=${OPTARG} ;;
      c) COUNT=${OPTARG} ;;
      r) REVERSE_GEOCODE=${OPTARG} ;;
      a) RATE_LIMIT=1 ;;
   esac
done

# Body
if [ ! -z "$QUERY" ]; then
  do-search -q "$QUERY" -c "$COUNT" -l "$LANG" -t "spark" -t "pretty"
elif [ ! -z "$REVERSE_GEOCODE" ]; then
  output="$REVERSE_GEOCODE.json"
  if [ ! -e "$output" ]; then
    reverse-geocode "$REVERSE_GEOCODE" > "$output"
  fi

  jq '.result.places.place_type == "city"' "$output"
elif [ "$RATE_LIMIT" -eq 1 ]; then
  echo `rate-limit`
fi
