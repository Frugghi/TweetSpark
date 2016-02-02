package com.tommasomadonia.spark

import org.apache.spark.sql.{Row, DataFrame}
import org.apache.spark.sql.functions._

package object dataframe_extension {

  case class MediaIndices(indices: Array[Long])

  implicit class TweetDataFrame(dataFrame: DataFrame) {

    def filterRetweets(filter: Boolean): DataFrame = if (filter) dataFrame.filter(dataFrame("retweeted_status").isNull) else dataFrame

    def coalesceRetweets(): DataFrame = {
      val extractIndicesFunction: (Seq[Row] => Seq[MediaIndices]) = (elements: Seq[Row]) => {
        elements.map(row => MediaIndices(row.getAs[Seq[Long]]("indices").toArray))
      }

      val extractIndices = udf(extractIndicesFunction)

      dataFrame
        .withColumn("tweet_text", when(dataFrame("retweeted_status").isNull, dataFrame("text")).otherwise(dataFrame("retweeted_status.text")))
        .withColumn("hashtags", when(dataFrame("retweeted_status").isNull, dataFrame("entities.hashtags")).otherwise(dataFrame("retweeted_status.entities.hashtags")))
        .withColumn("media", when(dataFrame("retweeted_status").isNull, extractIndices(dataFrame("entities.media"))).otherwise(extractIndices(dataFrame("retweeted_status.entities.media"))))
        .withColumn("urls", when(dataFrame("retweeted_status").isNull, dataFrame("entities.urls")).otherwise(dataFrame("retweeted_status.entities.urls")))
        .withColumn("user_mentions", when(dataFrame("retweeted_status").isNull, dataFrame("entities.user_mentions")).otherwise(dataFrame("retweeted_status.entities.user_mentions")))
    }
  }
}
