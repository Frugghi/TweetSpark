package com.tommasomadonia.spark

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, Column}
import org.apache.spark.sql.functions._

import scala.collection.mutable.{ArrayBuffer, StringBuilder}

import com.github.nscala_time.time.Imports._

import com.tommasomadonia.spark.dataframe_extension._

case class Word(word: String)

object WordCount {
  type WordCountTuple = (String, Long)

  def countInTime(dataFrame: DataFrame, ignoreRetweets: Boolean, hours: Int): RDD[((String, Long), List[WordCountTuple])] = dataFrame match {
    case dataFrame if !dataFrame.columns.contains("user") => dataFrame.sqlContext.sparkContext.emptyRDD[((String, Long), List[WordCountTuple])]
    case dataFrame => {
      def moduloFloor(number: Int, modulo: Int) = number - (number % modulo)

      val timeSliceFunction: (String => String) = (timestamp: String) => {
        val inputFormat = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy").withOffsetParsed()
        val outputFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
        val date = inputFormat.parseDateTime(timestamp)
        val startDate = date.withHour(moduloFloor(date.getHourOfDay, hours)).withMinuteOfHour(0)
        startDate.toString(outputFormat) + " - " + (startDate + hours.hours - 1.minutes).toString(outputFormat)
      }

      val timeSlice = udf(timeSliceFunction)

      val timeSlicedDataFrame = dataFrame
        .filterRetweets(ignoreRetweets)
        .withColumn("time_slice", timeSlice(col("created_at")))

      val countDataFrame: DataFrame = timeSlicedDataFrame
        .groupBy("time_slice")
        .count()
        .withColumnRenamed("count", "total_tweets")

      timeSlicedDataFrame
        .tweetDataFrame("tweet")
        .explode(col("tweet")) { row =>
          val indices = row.getStruct(0).getSeq[Row](1).map(index => (index.getLong(0), index.getLong(1)))
          val tweet = Tweet(row.getStruct(0).getString(0), indices.toArray)
          tokenize(tweet).map(Word(_))
        }
        .groupBy("time_slice", "word")
        .count()
        .join(countDataFrame, "time_slice")
        .orderBy(asc("time_slice"), desc("count"))
        .rdd
        .map(row => (row.getAs[String]("time_slice"), row.getAs[Long]("total_tweets")) -> (row.getAs[String]("word"), row.getAs[Long]("count")))
        .groupByKey()
        .sortBy(_._1._1)
        .map({ case (key, wordCount) => key -> wordCount.toList.sortBy(-_._2) })
    }
  }

  def countInTime(dataFrame: DataFrame, ignoreRetweets: Boolean = false, hours: Int, limit: Int): RDD[((String, Long), List[WordCountTuple])] = {
    countInTime(dataFrame, ignoreRetweets, hours).map({ case (key, wordCount) => key -> wordCount.take(limit) })
  }

  def countDF(dataFrame: DataFrame, ignoreRetweets: Boolean): DataFrame = dataFrame match {
    case dataFrame if !dataFrame.columns.contains("user") => dataFrame.sqlContext.emptyDataFrame
    case dataFrame => {
      dataFrame
        .filterRetweets(ignoreRetweets)
        .tweetDataFrame("tweet")
        .explode(col("tweet")) { row =>
          val indices = row.getStruct(0).getSeq[Row](1).map(index => (index.getLong(0), index.getLong(1)))
          val tweet = Tweet(row.getStruct(0).getString(0), indices.toArray)
          tokenize(tweet).map(Word(_))
        }
        .groupBy("word")
        .count()
        .orderBy(desc("count"))
    }
  }

  def count(dataFrame: DataFrame, ignoreRetweets: Boolean): Seq[WordCountTuple] = {
    countDF(dataFrame, ignoreRetweets).collect().map(row => (row.getAs("word"), row.getAs("count")))
  }

  def count(dataFrame: DataFrame, ignoreRetweets: Boolean, limit: Int): Seq[WordCountTuple] = {
    countDF(dataFrame, ignoreRetweets).take(limit).map(row => (row.getAs("word"), row.getAs("count")))
  }

  def countPerAuthor(dataFrame: DataFrame, ignoreRetweets: Boolean): RDD[((String, Long), List[WordCountTuple])] = dataFrame match {
    case dataFrame if !dataFrame.columns.contains("user") => dataFrame.sqlContext.sparkContext.emptyRDD[((String, Long), List[WordCountTuple])]
    case dataFrame => {
      dataFrame
        .filterRetweets(ignoreRetweets)
        .tweetDataFrame("tweet")
        .explode(col("tweet")) { row =>
          val indices = row.getStruct(0).getSeq[Row](1).map(index => (index.getLong(0), index.getLong(1)))
          val tweet = Tweet(row.getStruct(0).getString(0), indices.toArray)
          tokenize(tweet).map(Word(_))
        }
        .groupBy("word", "user.screen_name")
        .count()
        .rdd
        .map(row => row.getAs[String]("word") -> (row.getAs[String]("screen_name"), row.getAs[Long]("count")))
        .groupByKey()
        .map({ case (key, list) => (key, list.map(_._2).reduce(_ + _)) -> (list.toList.sortBy(-_._2)) })
        .sortBy(-_._1._2)
    }
  }

  def countPerAuthor(dataFrame: DataFrame, ignoreRetweets: Boolean = false, limitAuthor: Int): RDD[((String, Long), List[WordCountTuple])] = {
    countPerAuthor(dataFrame, ignoreRetweets).map({ case (key, authorCount) => key -> authorCount.take(limitAuthor) })
  }


  private[this] def tokenize(tweet: Tweet): TraversableOnce[String] = tweet match {
    case Tweet(tweet, indices) => {
      val sortedIndices = indices.sortWith(_._1 > _._1)
      val text = new StringBuilder(tweet.replaceAll("[^\u0000-\uFFFF]", " ").replaceAll("\\n", " "))
      val token = ArrayBuffer.empty[String]
      for (index <- sortedIndices) {
        // Apparently Twitter API are bugged (?) and sometimes oob indices are returned
        var startIndex = index._1.toInt
        var endIndex = index._2.toInt
        if (endIndex > text.length) {
          val delta = endIndex - text.length
          startIndex -= delta
          endIndex -= delta
        }
        var word = text.substring(startIndex, endIndex)
        if (word.trim != word) {
          val headTrimmedWord = word.replaceFirst("^\\s+", "")
          if (word.length != headTrimmedWord.length) {
            val delta = word.length - headTrimmedWord.length
            startIndex += delta
            endIndex += delta
          } else {
            val tailTrimmedWord = word.replaceFirst("\\s+$", "")
            val delta = word.length - tailTrimmedWord.length
            startIndex -= delta
            endIndex -= delta
          }
          word = text.substring(startIndex, endIndex).trim
        }
        token += word
        text.delete(startIndex, endIndex)
      }
      token ++= text.toString.trim.split("\\W+")
      token.filter(_.nonEmpty).filterNot(Set("\u2026").contains(_))
    }
  }

}
