#!/usr/bin/env bash

function getHosts {
   java -jar ./ambari/ambari-shell.jar --ambari.host=localhost <<< "host list" | grep -c ".*\.cluster"
}

cd "$VAGRANT_RES_DIR"
total_hosts=4
n_hosts=`getHosts`
while [ $n_hosts -lt $total_hosts ]
do
   echo "$n_hosts/$total_hosts hosts up, waiting 10 seconds..."
   sleep 10
   n_hosts=`getHosts`
done
echo "All hosts are up!"

echo "Creating the cluster..."
echo "Open $(hostname):8080 to track the progress"
blueprint=./ambari/spark-cluster-$total_hosts-blueprint.json
blueprint_name=`grep -o '"blueprint_name"\s*:\s*".*"' $blueprint | cut -d':' -f2 | cut -d'"' -f2`
java -jar ./ambari/ambari-shell.jar --ambari.host=localhost <<EOF >/dev/null
blueprint add --file $blueprint
cluster build --blueprint $blueprint_name
cluster assign --hostGroup host_group_1 --host master.cluster
cluster assign --hostGroup host_group_2 --host slave1.cluster
cluster assign --hostGroup host_group_3 --host slave2.cluster
cluster assign --hostGroup host_group_4 --host slave3.cluster
cluster create --exitOnFinish true
EOF