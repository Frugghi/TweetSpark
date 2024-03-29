package com.tommasomadonia.spark

import org.apache.spark.sql.{Row, DataFrame}
import org.apache.spark.sql.functions._

import scala.collection.mutable.ArrayBuffer

private case class _Indices(indices: Array[Long])

package object dataframe_extension {

  implicit class TweetDataFrame(dataFrame: DataFrame) {

    def filterRetweets(filter: Boolean): DataFrame = if (filter && dataFrame.columns.contains("retweeted_status")) dataFrame.filter(col("retweeted_status").isNull) else dataFrame

    def filterMalformed(): DataFrame = dataFrame.filter(col("text").isNotNull).filter(col("created_at").isNotNull)

    def coalesceRetweets(): DataFrame = {
      val extractIndicesFunction: (Seq[Row] => Seq[_Indices]) = (elements: Seq[Row]) => {
        if (elements != null) {
          elements.map(row => _Indices(row.getAs[Seq[Long]]("indices").toArray))
        } else {
          Array[_Indices]()
        }
      }

      val extractIndices = udf(extractIndicesFunction)

      if (!dataFrame.columns.contains("retweeted_status")) {
        dataFrame
          .withColumn("tweet_text", col("text"))
          .withColumn("hashtags", col("entities.hashtags"))
          .withColumn("media", extractIndices(col("entities.media")))
          .withColumn("urls", col("entities.urls"))
          .withColumn("user_mentions", col("entities.user_mentions"))
      } else {
        dataFrame
          .withColumn("tweet_text", when(col("retweeted_status").isNull, col("text")).otherwise(col("retweeted_status.text")))
          .withColumn("hashtags", when(col("retweeted_status").isNull, extractIndices(col("entities.hashtags"))).otherwise(extractIndices(col("retweeted_status.entities.hashtags"))))
          .withColumn("media", when(col("retweeted_status").isNull, extractIndices(col("entities.media"))).otherwise(extractIndices(col("retweeted_status.entities.media"))))
          .withColumn("urls", when(col("retweeted_status").isNull, extractIndices(col("entities.urls"))).otherwise(extractIndices(col("retweeted_status.entities.urls"))))
          .withColumn("user_mentions", when(col("retweeted_status").isNull, extractIndices(col("entities.user_mentions"))).otherwise(extractIndices(col("retweeted_status.entities.user_mentions"))))
      }
    }

    def tweetDataFrame(column: String): DataFrame = {
      val extractTweetFunction: ((String, Seq[Row], Seq[Row], Seq[Row], Seq[Row]) => Tweet) =
        (text: String, hashtags: Seq[Row], media: Seq[Row], urls: Seq[Row], user_mentions: Seq[Row]) => {
        val indices = ArrayBuffer.empty[(Long, Long)]
        if (hashtags != null) { indices ++= hashtags.map(row => row.getAs[Seq[Long]]("indices").toArray).map(index => (index(0), index(1))) }
        if (media != null) { indices ++= media.map(row => row.getAs[Seq[Long]]("indices").toArray).map(index => (index(0), index(1))) }
        if (urls != null) { indices ++= urls.map(row => row.getAs[Seq[Long]]("indices").toArray).map(index => (index(0), index(1))) }
        if (user_mentions != null) { indices ++= user_mentions.map(row => row.getAs[Seq[Long]]("indices").toArray).map(index => (index(0), index(1))) }
        Tweet(text, indices.toArray)
      }

      val extractTweet = udf(extractTweetFunction)

      dataFrame
        .coalesceRetweets()
        .withColumn(column, extractTweet(col("tweet_text"), col("hashtags"), col("media"), col("urls"), col("user_mentions")))
    }
  }
}
