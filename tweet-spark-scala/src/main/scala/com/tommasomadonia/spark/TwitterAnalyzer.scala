package com.tommasomadonia.spark

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}

object TwitterAnalyzer {

  def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Usage: " + this.getClass.getSimpleName + " <path>")
      System.exit(1)
    }

    // Validating the parameters
    val hadoopConfiguration = new Configuration()
    val path = new Path("hdfs://" + args(0))
    val fileSystem = FileSystem.get(hadoopConfiguration)
    if (!fileSystem.exists(path)) {
      System.err.println("Path '" + args(0) + "' does not exists")
      System.exit(1)
    }

    // Initializing Spark context
    val sparkConfiguration = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sparkContext = new SparkContext(sparkConfiguration)
    val sqlContext = new SQLContext(sparkContext)

    // Useful stuff
    def printlnTitle(title: String) = println("------ " + title + " ------")
    def measureTime[T](function: => T) = {
      val startTime = System.nanoTime
      val result = function
      println("Execution time: " + (System.nanoTime - startTime)/1e9 + " sec")
      result
    }

    // Initializing Tweets table
    val tweetsTable = "Tweets"
    val dataFrame = sqlContext.read.json(path.toString)
    dataFrame.registerTempTable(tweetsTable)
    printlnTitle("Tweet table schema")
    dataFrame.printSchema()

    // Find more active tweeters
    measureTime {
      val limit = 20
      printlnTitle(s"Top $limit active tweeters")
      ActiveTweeters.find(limit, sqlContext, tweetsTable).collect.foreach(println)
    }

    // Find more tweeted words
    measureTime {
      val limit = 20
      printlnTitle(s"Top $limit tweeted words")
      WordCount.count(sparkContext, dataFrame, limit).foreach(println)
    }

    // Find more tweeted words in time
    measureTime {
      val limit = 20
      val hours = 6
      printlnTitle(s"Top $limit tweeted words/" + hours + "h")
      WordCount.countInTime(sparkContext, dataFrame, hours, limit).collect
        .foreach({ case (key, list) =>
          println(s"$key:")
          list.foreach(println)
      })
    }

    sparkContext.stop()
  }

}
