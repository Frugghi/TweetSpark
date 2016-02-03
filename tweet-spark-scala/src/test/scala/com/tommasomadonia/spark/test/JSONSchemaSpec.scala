package com.tommasomadonia.spark.test

import org.apache.spark.sql.DataFrameReader
import org.apache.spark.sql.types.StructType
import org.scalatest.Suite

trait JSONSchemaSpec extends SparkSQLSpec {
  this: Suite =>

  private val jsonSchema = "test/schema.json"

  private var _schema: StructType = _
  private var _dataFrameReader: DataFrameReader = _

  def schema = _schema
  def dataFrameReader = _dataFrameReader

  override def beforeAll(): Unit = {
    super.beforeAll()

    _schema = sqlContext.read.json(jsonSchema).schema
    _dataFrameReader = sqlContext.read.schema(_schema)
  }

  override def afterAll(): Unit = {
    _dataFrameReader = null
    _schema = null

    super.afterAll()
  }

}