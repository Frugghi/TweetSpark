package com.tommasomadonia.spark

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}

import scala.collection.mutable.{ArrayBuffer, StringBuilder}

object WordCount {
  type WordCountTuple = (String, Int)

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
