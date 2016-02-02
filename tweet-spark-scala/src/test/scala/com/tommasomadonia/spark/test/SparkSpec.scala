package com.tommasomadonia.spark.test

import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.scalatest._

trait SparkSpec extends BeforeAndAfterAll {
  this: Suite =>

  private val master = "local[2]"
  private val appName = this.getClass.getSimpleName

  private var _sparkContext: SparkContext = _

  def sparkContext = _sparkContext

  val conf: SparkConf = new SparkConf()
    .setMaster(master)
    .setAppName(appName)

  override def beforeAll(): Unit = {
    super.beforeAll()

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)
    Logger.getLogger("Remoting").setLevel(Level.ERROR)

    _sparkContext = new SparkContext(conf)
    _sparkContext.setLogLevel("ERROR")
  }

  override def afterAll(): Unit = {
    if (_sparkContext != null) {
      _sparkContext.stop()
      _sparkContext = null
    }

    super.afterAll()
  }

}

