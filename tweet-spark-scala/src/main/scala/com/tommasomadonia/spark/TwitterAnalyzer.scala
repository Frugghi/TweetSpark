package com.tommasomadonia.spark

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}

import com.tommasomadonia.spark.dataframe_extension._

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
    dataFrame.cache()

    // Find more active tweeters
    measureTime {
      val limit = 20
      printlnTitle(s"Top $limit active tweeters")
      ActiveTweeters.find(limit, sqlContext, tweetsTable).show(limit, false)
    }

    // Find more tweeted words
    measureTime {
      val limit = 20
      printlnTitle(s"Top $limit tweeted words")
      WordCount.countDF(dataFrame, false).show(limit, false)
    }

    measureTime {
      val authorLimit = 5
      val wordLimit = 20
      printlnTitle(s"Top $wordLimit words and top $authorLimit authors")
      WordCount.countPerAuthor(dataFrame, false, authorLimit).take(wordLimit)
        .foreach({ case ((word, count), list) =>
          println(s"$word (tweeted $count times):")
          list.foreach({ case (author, count) =>
            println(s"- $author: $count")
          })
      })
    }

    // Find more tweeted words in time
    measureTime {
      val limit = 20
      val hours = 6
      printlnTitle(s"Top $limit tweeted words/" + hours + "h")
      WordCount.countInTime(dataFrame, false, hours, limit).collect
        .foreach({ case ((timeSlice, count), list) =>
          println(s"$timeSlice, $count tweets:")
          list.foreach(println)
      })
    }

    sparkContext.stop()
  }

}
