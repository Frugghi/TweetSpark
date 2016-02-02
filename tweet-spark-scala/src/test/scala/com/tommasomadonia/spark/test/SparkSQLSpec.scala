package com.tommasomadonia.spark.test

import org.apache.spark.sql.SQLContext
import org.scalatest.Suite

trait SparkSQLSpec extends SparkSpec {
  this: Suite =>

  private var _sqlContext: SQLContext = _

  def sqlContext = _sqlContext

  override def beforeAll(): Unit = {
  super.beforeAll()

    _sqlContext = new SQLContext(sparkContext)
}

  override def afterAll(): Unit = {
    _sqlContext = null

  super.afterAll()
}

}