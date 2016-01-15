#!/bin/bash

export PATH=$PATH:.
source resty -W

command -v jq >/dev/null 2>&1 || {
  echo >&2 "'jq' is required but it's not installed."
  echo >&2 "Download it from https://stedolan.github.io/jq/"
  exit 1
}

# Twitter API endpoints
API_VERSION="1.1"
TOKEN_API="/oauth2/token"
RATE_LIMIT_API="/$API_VERSION/application/rate_limit_status.json"
SEARCH_API="/$API_VERSION/search/tweets.json"

KEY=`jq '.key' app-credentials.json | cut -d '"' -f 2`
SECRET=`jq '.secret' app-credentials.json | cut -d '"' -f 2`
TOKEN=""

# Helper functions
function get-access-token {
  local token_path="./app-token.json"

  if [ ! -e "$token_path" ]; then
    resty "https://api.twitter.com/" > /dev/null
    POST $TOKEN_API "grant_type=client_credentials" -H "Content-Type:application/x-www-form-urlencoded;charset=UTF-8" -u "$KEY:$SECRET" > $token_path
  fi

  TOKEN=`jq '.access_token' $token_path | cut -d '"' -f 2`
  resty "https://api.twitter.com/" -H "Authorization: Bearer $TOKEN" > /dev/null
}

function request {
  if [ -z "$TOKEN" ]; then
    get-access-token
  fi

  echo `GET $@`
}

function search {
  local OPTIND arg
  local params=""
  while getopts 'q:c:l:e:i:' arg
  do
      case ${arg} in
          q) params="${params}&q=${OPTARG//"#"/"%23"}" ;;
          c) params="${params}&count=${OPTARG}" ;;
          l) params="${params}&lang=${OPTARG}" ;;
          e) params="${params}&include_entities=${OPTARG}" ;;
          i) params="${params}&max_id=${OPTARG}" ;;
      esac
  done
  params=`echo "$params" | cut -c 2-`

  echo `request $SEARCH_API -q "$params"`
}

function print-help {
  echo ""
  echo "Usage: $0 [options]"
  echo ""
  echo "Options:"
  echo " -q    search by terms"
  echo " -h    search by hashtags"
  echo " -l    restricts tweets to the given language, given by an ISO 639-1 code"
  echo " -c    the number of tweets to return"
  echo ""
  exit 1
}

# Transformers
function spark-transform {
  jq '.statuses[]' "$1" -c > ${1/%.json/.spark.json}
}

function pretty-transform {
  jq '.' "$1" -M > ${1/%.json/.pretty.json}
}

# Do search!
QUERY=""
COUNT=""
LANG=""

while getopts q:l:c:h: arg
do
   case ${arg} in
      q) QUERY=${OPTARG} ;;
      h) QUERY="#${OPTARG}" ;;
      l) LANG=${OPTARG} ;;
      c) COUNT=${OPTARG} ;;
   esac
done

if [ -z $QUERY ]; then
  >&2 echo "Query cannot be empty!"
  print-help
fi

if [ "$COUNT" -gt "0" ]; then
  >&2 echo "Count must be positive!"
  print-help
fi

basename="$QUERY"

page=0
page_id=""
for (( i=$COUNT; i>=0; i=i-100 ))
do
  count=$i
  if [ "$count" -gt "100" ]; then
    count=100
  fi

  echo "Downloading page $page ($count tweets)..."
  output="${basename}-page${page}.json"
  if [ -z $page_id ]; then
    search -q "$QUERY" -c "$count" -l "$LANG" -e "1" > $output
  else
    search -q "$QUERY" -c "$count" -l "$LANG" -e "1" -i "$page_id" > $output
  fi
  spark-transform $output
  pretty-transform $output

  page_id=`jq '.search_metadata.next_results' $output | egrep -o 'max_id=[0-9]+' | cut -d'=' -f2`
  page=$((page+1))
done
