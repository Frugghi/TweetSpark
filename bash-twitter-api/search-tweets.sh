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
  local page_id=""
  local resume=0
  while getopts 'q:c:l:t:r:' arg
  do
    case ${arg} in
      q) query=${OPTARG} ;;
      c) total=${OPTARG} ;;
      l) lang=${OPTARG} ;;
      t) transformers[${#transformers[@]}]=${OPTARG} ;;
      r) [ ! -z "$resume" ] && resume=${OPTARG} ;;
    esac
  done

  mkdir -p "tweets"
  basename="./tweets/${query//:/}"
  QUERY="$query -filter:retweets"
  page=0

  if [ "$resume" -eq 1 ]; then
    while [ -e "${basename}-page${page}.json" ]
    do
      page_id=`jq '.search_metadata.next_results' "${basename}-page${page}.json" | egrep -o 'max_id=[0-9]+' | cut -d'=' -f2`
      page=$((page+1))
    done

    if [ -z "$page_id" ]; then
      echo "Last page reached, no more tweets available!"
      return
    fi

    echo "Resuming from page $page and ID $page_id..."
  fi

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

    if [ -z "$page_id" ]; then
      break
    fi
  done
}

# Params parsing
COUNT=0
RATE_LIMIT=0
while getopts q:l:c:h:g:p:ar arg
do
  case ${arg} in
    q) QUERY=${OPTARG} ;;
    h) QUERY="#${OPTARG}" ;;
    p) QUERY="place:${OPTARG}" ;;
    l) LANG=${OPTARG} ;;
    c) COUNT=${OPTARG} ;;
    g) REVERSE_GEOCODE=${OPTARG} ;;
    a) RATE_LIMIT=1 ;;
    r) RESUME=1 ;;
  esac
done

# Body
init-search-api

if [ ! -z "$QUERY" ]; then
  do-search -q "$QUERY" -c "$COUNT" -l "$LANG" -r "$RESUME" -t "spark" -t "pretty"
elif [ ! -z "$REVERSE_GEOCODE" ]; then
  output="$REVERSE_GEOCODE.json"
  if [ ! -e "$output" ]; then
    reverse-geocode "$REVERSE_GEOCODE" > "$output"
  fi
elif [ "$RATE_LIMIT" -eq 1 ]; then
  echo `rate-limit`
else
  print-help
fi
