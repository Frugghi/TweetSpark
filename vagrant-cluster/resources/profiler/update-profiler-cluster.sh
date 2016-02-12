#!/usr/bin/env bash
source "$VAGRANT_RES_DIR/common.sh"

SERVER="monitor.cluster"
PORT="8086"
REPORTER="InfluxDBReporter"
DATABASE="profiler"
USERNAME="profiler"
PASSWORD="profiler"
WHITELIST=""
BLACKLIST=""
PROFILERS=""

while getopts s:t:r:d:u:p:w:b:f: option
do
    case "${option}"
    in
        s) SERVER=${OPTARG};;
        t) PORT=${OPTARG};;
        r) REPORTER=${OPTARG};;
        d) DATABASE=${OPTARG};;
        u) USERNAME=${OPTARG};;
        p) PASSWORD=${OPTARG};;
        w) WHITELIST=${OPTARG};;
        b) BLACKLIST=${OPTARG};;
        f) PROFILERS=${OPTARG};;
    esac
done

echo "Updating profiler settings..."
local profiler="$VAGRANT_RES_DIR/profiler/update-profiler-node.sh"
local profiler_jar=""
if [ -e "$VAGRANT_RES_DIR/profiler/profiler" ]; then
	profiler_jar=`cat "$VAGRANT_RES_DIR/profiler/profiler"`
else
	profiler_jar=`basename $(find "$VAGRANT_RES_DIR/profiler" -name 'statsd-jvm-profiler*.jar')`
fi

local profiler_options="-javaagent:/home/vagrant/resources/profiler/${profiler_jar}=server=${SEVER},port=${PORT},reporter=${REPORTER},database=${DATABASE},username=${USERNAME},password=${PASSWORD},prefix=bigdata.profiler.$(basename $1 .jar).USERNAME_HERE.${2//\//}.$RANDOM.$(date +'%y%m%d-%H%M'),tagMapping=SKIP.SKIP.username.job.flow.stage.phase"
if [ ! -z "$WHITELIST" ]; then
    profiler_options="${profiler_options},packageWhitelist=${WHITELIST}"
fi
if [ ! -z "$BLACKLIST" ]; then
    profiler_options="${profiler_options},packageBlacklist=${BLACKLIST}"
fi
if [ ! -z "$PROFILERS" ]; then
    profiler_options="${profiler_options},profilers=${PROFILERS}"
fi

$profiler "$profiler_options"
while IFS= read -r host
do
	ssh -n $host "$profiler $profiler_options"
done < <( grep -o -e 'slave[0-9]*\.cluster$' /etc/hosts )
${HADOOP_PREFIX}/sbin/stop-yarn.sh
${HADOOP_PREFIX}/sbin/start-yarn.sh
