package com.tommasomadonia.spark

import org.apache.spark.sql.{DataFrame, SQLContext}

object ActiveTweeters {

  def find(sqlContext: SQLContext, table: String): DataFrame = {
    sqlContext.sql(s"""
                      |SELECT user.screen_name, COUNT(*) AS total_count
                      |FROM $table
                      |WHERE user.screen_name IS NOT NULL
                      |GROUP BY user.screen_name
                      |ORDER BY total_count DESC""".stripMargin)
  }

  def find(limit: Int, sqlContext: SQLContext, table: String): DataFrame = {
    sqlContext.sql(s"""
                      |SELECT user.screen_name, COUNT(*) AS total_count
                      |FROM $table
                      |WHERE user.screen_name IS NOT NULL
                      |GROUP BY user.screen_name
                      |ORDER BY total_count DESC
                      |LIMIT $limit""".stripMargin)
  }

}
