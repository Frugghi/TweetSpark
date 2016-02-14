package com.tommasomadonia.spark

import scala.collection.mutable.ArrayBuffer

case class Tweet(text: String, indices: Array[(Long, Long)]) {

  def tokenize(): TraversableOnce[String] = this match {
    case Tweet(tweet, indices) if (tweet == null || tweet.isEmpty) => Array[String]()
    case Tweet(tweet, indices) => {
      val sortedIndices = (if (indices != null) indices else Array[(Long, Long)]()).sortWith(_._1 > _._1)
      val text = new StringBuilder(tweet.replaceAll("[^\u0000-\uFFFF]", " ").replaceAll("\\n", " "))
      val token = ArrayBuffer.empty[String]
      for (index <- sortedIndices) {
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

      token ++= "(\\w[\\w']*\\w|\\w)".r.findAllIn(text.toString).toArray[String]
      token.filter(_.nonEmpty).filterNot(Set("\u2026").contains(_))
    }
  }

}