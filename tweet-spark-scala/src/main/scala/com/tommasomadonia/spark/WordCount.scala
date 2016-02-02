package com.tommasomadonia.spark

import java.util.Locale

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.functions._

import scala.collection.mutable.{ArrayBuffer, StringBuilder}

import com.github.nscala_time.time.Imports._

object WordCount {
  type WordCountTuple = (String, Int)

  def countInTime(sparkContext: SparkContext, dataFrame: DataFrame, hours: Int): RDD[(String, List[WordCountTuple])] = {
    def moduloFloor(number: Int, modulo: Int) = number - (number % modulo)

    val timeSliceFunction: (String => String) = (timestamp: String) => {
      val inputFormat = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy").withOffsetParsed()
      val outputFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
      val date = inputFormat.parseDateTime(timestamp)
      val startDate = date.withHour(moduloFloor(date.getHourOfDay, hours)).withMinuteOfHour(0)
      startDate.toString(outputFormat) + " - " + (startDate + hours.hours - 1.minutes).toString(outputFormat)
    }

    val timeSlice = udf(timeSliceFunction)
    val timeSlicedDataFrame = dataFrame.withColumn("time_slice", timeSlice(col("created_at")))
    timeSlicedDataFrame
      .select("text", "user.name", "user.screen_name", "entities.hashtags", "entities.media", "entities.urls", "entities.user_mentions", "time_slice")
      .flatMap(row => tokenize(row).map(word => (row.getAs[String]("time_slice"), word)))
      .map(word => word -> 1)
      .reduceByKey(_ + _)
      .map({ case ((timeSlice, word), count) => (timeSlice, (word, count)) })
      .groupByKey()
      .sortByKey()
      .map({ case (key, wordCount) => key -> wordCount.toList.sortBy(-_._2) })
  }

  def countInTime(sparkContext: SparkContext, dataFrame: DataFrame, hours: Int, limit: Int): RDD[(String, List[WordCountTuple])] = {
    countInTime(sparkContext, dataFrame, hours).map({ case (key, wordCount) => key -> wordCount.take(limit) })
  }

  def count(sparkContext: SparkContext, dataFrame: DataFrame): RDD[WordCountTuple] = (sparkContext, dataFrame) match {
    case (sparkContext, dataFrame) if !dataFrame.columns.contains("text") || !dataFrame.columns.contains("user") || !dataFrame.columns.contains("entities") => sparkContext.emptyRDD[WordCountTuple]
    case (_, dataFrame) => {
      dataFrame
        .select("text", "user.name", "user.screen_name", "entities.hashtags", "entities.media", "entities.urls", "entities.user_mentions")
        .flatMap(row => tokenize(row))
        .map(word => word -> 1)
        .reduceByKey(_ + _)
    }
  }

  def count(sparkContext: SparkContext, dataFrame: DataFrame, limit: Int): Array[WordCountTuple] = {
    count(sparkContext, dataFrame).top(limit)(Ordering[Long].on(x => x._2))
  }

  private[this] def extractIndices(row: Row, fieldNames: String*): List[(Long, Long)] = {
    var result = Array.empty[(Long, Long)].toList
    for (fieldName <- fieldNames) {
      val index = row.fieldIndex(fieldName)
      if (!row.isNullAt(index)) {
        val indices = row.getSeq[Row](index).map({ row =>
          val indices = row.getAs[Seq[Long]]("indices")
          (indices.head, indices.last)
        }).toList
        result = List(result, indices).flatten
      }
    }

    result
  }

  private[this] def tokenize(row: Row): TraversableOnce[String] = {
    val indices = extractIndices(row, "hashtags", "media", "urls", "user_mentions").sortWith(_._1 > _._1)
    val tweet = row.getAs[String]("text")
    val text = new StringBuilder(tweet.replaceAll("[^\u0000-\uFFFF]", " ").replaceAll("\\n", " "))
    val token = ArrayBuffer.empty[String]
    for (index <- indices) {
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
    token
  }

}
