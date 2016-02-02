package com.tommasomadonia.spark.test

import com.tommasomadonia.spark.WordCount
import org.scalatest.{Matchers, GivenWhenThen, FlatSpec}

class WordCountTests extends FlatSpec with SparkSQLSpec with GivenWhenThen with Matchers {

  "Empty JSON" should "have no words" in {
    Given("an empty DataFrame")
    val dataFrame = sqlContext.read.json("test/empty.json")

    When("count words")
    val wordCounts = WordCount.count(sparkContext, dataFrame).collect()

    Then("word counts should be empty")
    wordCounts shouldBe empty
  }

  "A tweet" should "ignore the emojis" in {
    Given("a DataFrame")
    val dataFrame = sqlContext.read.json("test/emoji.json")

    When("count words")
    val wordCounts = WordCount.count(sparkContext, dataFrame).collect().toSet

    Then("word counted")
    wordCounts shouldEqual Set(
      ("Cinema", 1),
      ("stasera", 1),
      ("#starwars", 1),
      ("https://t.co/5xxkexsxxc", 1)
    )
  }

  "A collection of tweets" should "be counted" in {
    Given("a DataFrame")
    val dataFrame = sqlContext.read.json("test/random.json")

    When("count words")
    val wordCounts = WordCount.count(sparkContext, dataFrame).collect().toSet

    Then("word counted")
    wordCounts shouldEqual Set(
      ("I", 1),
      ("vecchi", 1),
      ("tromboni", 1),
      ("dello", 1),
      ("@ABCDEF", 1),
      ("ancora", 1),
      ("su", 1),
      ("#StarWars", 1),
      ("https://t.co/3xxxFxxxx1", 2),
      ("Ti", 1),
      ("odio", 1),
      ("caro", 1)
    )
  }

}