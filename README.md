# TweetSpark

## Installation

### Bash Twitter API
- Download [jq](https://stedolan.github.io/jq/download/)
- Create `app-credentials.json`
```JSON
{
    "key": "PUT YOUR KEY HERE",
    "secret": "PUT YOUR SECRET HERE"
}
```
- Create `user-credentials.json` (for user authenticated API calls)
```JSON
{
    "key": "PUT YOUR KEY HERE",
    "secret": "PUT YOUR SECRET HERE"
}
```
- Run `search-tweets.sh` to query the Search API
- Run `streaming-tweets.sh` to query the Streaming API

#### Notes
The script uses [application-only authentication](https://dev.twitter.com/oauth/application-only) and [application owner access token](https://dev.twitter.com/oauth/overview/application-owner-access-tokens).

### Vagrant cluster
- Download and install [VirtualBox](https://www.virtualbox.org/wiki/Downloads)
- Download and install [Vagrant](https://www.vagrantup.com/downloads.html)
- Add CentOS 7 box: `vagrant box add centos7 http://cloud.centos.org/centos/7/vagrant/x86_64/images/CentOS-7-x86_64-Vagrant-1601_01.VirtualBox.box`
- Rename `settings.yml.sample` to `settings.yml`
- See **Manual** or **Ambari**
- Run `vagrant up` to create the cluster
- Run `vagrant ssh master` to SSH into `master` node
- Run `vagrant destroy` to destroy the cluster
 
#### Manual
- Download [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- Download [Hadoop](http://hadoop.apache.org/releases.html)
- Download [Spark](http://spark.apache.org/downloads.html)
- If you enable the *monitor* node you must download [InfluxDB](https://influxdata.com/downloads/#influxdb)
- Put the downloaded files into `vagrant-cluster/resources`
 
#### Ambari
- Set `ambari.enabled` to `true` in `settings.yml`

#### Environment
Currently the script set up a cluster of 1 master node and 2 slave nodes with 64bit CentOS 7, Java 7u79, Hadoop 2.7.1 and Spark 1.6.0 (without Hadoop).

You can configure the cluster in `settings.yml`.

#### Web UI
You can check the following URLs to monitor the Hadoop daemons:
- [NameNode](http://10.211.55.100:50070/dfshealth.html)
- [ResourceManager](http://10.211.55.100:8088/cluster)
- [JobHistory](http://10.211.55.100:19888/jobhistory)
- [Ambari](http://10.211.55.99:8080/)

## Thanks to
- [Resty](http://github.com/micha/resty)
- [jq](https://stedolan.github.io/jq/)
- [Vagrant Hadoop Spark Cluster](https://github.com/dnafrance/vagrant-hadoop-spark-cluster)
- [Cluster profiling](http://ihorbobak.com/index.php/2015/08/05/cluster-profiling/)
- [Statsd JVM profiler](https://github.com/etsy/statsd-jvm-profiler)
