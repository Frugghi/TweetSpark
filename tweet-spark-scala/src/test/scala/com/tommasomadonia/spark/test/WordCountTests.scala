package com.tommasomadonia.spark.test

import com.tommasomadonia.spark.WordCount
import org.scalatest.{Matchers, GivenWhenThen, FlatSpec}

class WordCountTests extends FlatSpec with JSONSchemaSpec with GivenWhenThen with Matchers {

  "Empty JSON" should "have no words" in {
    Given("an empty DataFrame")
    val dataFrame = dataFrameReader.json("test/empty.json")

    When("count words")
    val wordCounts = WordCount.count(dataFrame, false).collect()

    Then("word counts should be empty")
    wordCounts shouldBe empty
  }

  "Ignored retweets" should "have no words" in {
    Given("an empty DataFrame")
    val dataFrame = dataFrameReader.json("test/retweet.json")

    When("count words")
    val wordCounts = WordCount.count(dataFrame, true).collect()

    Then("word counts should be empty")
    wordCounts shouldBe empty
  }

  "Retweets" should "be coalesced" in {
    Given("an empty DataFrame")
    val dataFrame = dataFrameReader.json("test/retweet.json")

    When("count words")
    val wordCounts = WordCount.count(dataFrame, false).collect().toSet

    Then("word counts should be empty")
    wordCounts shouldEqual Set(
      ("#Disney", 1),
      ("e", 2),
      ("OpenBionics", 1),
      ("producono", 1),
      ("protesi", 1),
      ("per", 1),
      ("bambini", 1),
      ("ispirate", 1),
      ("a", 1),
      ("#IronMan", 1),
      ("#Frozen", 1),
      ("#StarWars", 1),
      ("https://t.co/DxxxKxxxxo", 1),
      ("https://t.co/9xCxxPxxxR", 1)
    )
  }

  "A tweet" should "ignore the emojis" in {
    Given("a DataFrame")
    val dataFrame = dataFrameReader.json("test/emoji.json")

    When("count words")
    val wordCounts = WordCount.count(dataFrame, false).collect().toSet

    Then("word counted")
    wordCounts shouldEqual Set(
      ("Cinema", 1),
      ("stasera", 1),
      ("#starwars", 1),
      ("https://t.co/5xxkexsxxc", 1)
    )
  }

  "A tweet with wrong indices" should "be counted correctly" in {
    Given("a DataFrame")
    val dataFrame = dataFrameReader.json("test/newline.json")

    When("count words")
    val wordCounts = WordCount.count(dataFrame, false).collect().toSet

    Then("word counted")
    wordCounts shouldEqual Set(
      ("Fonte", 1),
      ("Corriere", 1),
      ("Fiorentino", 1),
      ("https://t.co/cxx1xxxxxJ", 1),
      ("#pittiuomo", 1),
      ("#firenze", 1),
      ("#StarWars", 1),
      ("#ilvolo", 1),
      ("12", 1),
      ("01", 1),
      ("2016", 1)
    )
  }

  "A collection of tweets" should "be counted" in {
    Given("a DataFrame")
    val dataFrame = dataFrameReader.json("test/random.json")

    When("count words")
    val wordCounts = WordCount.count(dataFrame, false).collect().toSet

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

  "A collection of tweets" should "be counted and ordered" in {
    Given("a DataFrame")
    val dataFrame = dataFrameReader.json("test/random.json")

    When("count top 1 word")
    val wordCounts = WordCount.count(dataFrame, false, 1)

    Then("the top 1 word")
    wordCounts shouldEqual Array(
      ("https://t.co/3xxxFxxxx1", 2)
    )
  }

}