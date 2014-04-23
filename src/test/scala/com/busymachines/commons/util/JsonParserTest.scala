package com.busymachines.commons.util

import org.specs2.mutable._
import org.parboiled.common.FileUtils
import spray.json._

class JsonParserSpec extends Specification {

  "The JsonParser" should {
    "parse 'null' to JsNull" in {
      JsonParser.parse("null") mustEqual JsNull
    }
    "parse 'true' to JsTrue" in {
      JsonParser.parse("true") mustEqual JsTrue
    }
    "parse 'false' to JsFalse" in {
      JsonParser.parse("false") mustEqual JsFalse
    }
    "parse '0' to JsNumber" in {
      JsonParser.parse("0") mustEqual JsNumber(0)
    }
    "parse '1.23' to JsNumber" in {
      JsonParser.parse("1.23") mustEqual JsNumber(1.23)
    }
    "parse '-1E10' to JsNumber" in {
      JsonParser.parse("-1E10") mustEqual JsNumber("-1E+10")
    }
    "parse '12.34e-10' to JsNumber" in {
      JsonParser.parse("12.34e-10") mustEqual JsNumber("1.234E-9")
    }
    "parse \"xyz\" to JsString" in {
      JsonParser.parse("\"xyz\"") mustEqual JsString("xyz")
    }
    "parse escapes in a JsString" in {
      JsonParser.parse(""""\"\\/\b\f\n\r\t"""") mustEqual JsString("\"\\/\b\f\n\r\t")
      JsonParser.parse("\"L\\" + "u00e4nder\"") mustEqual JsString("Länder")
    }
    "parse all representations of the slash (SOLIDUS) character in a JsString" in {
      JsonParser.parse( "\"" + "/\\/\\u002f" + "\"") mustEqual JsString("///")
    }
    "properly parse a simple JsObject" in (
      JsonParser.parse(""" { "key" :42, "key2": "value" }""") mustEqual
        JsObject("key" -> JsNumber(42), "key2" -> JsString("value"))
      )
    "properly parse a simple JsArray" in (
      JsonParser.parse("""[null, 1.23 ,{"key":true } ] """) mustEqual
        JsArray(JsNull, JsNumber(1.23), JsObject("key" -> JsBoolean(true)))
      )
    "properly parse a large file" in {
      val largeJsonSource = FileUtils.readAllCharsFromResource("test.json")
      val jsobj = JsonParser.parse(largeJsonSource).asInstanceOf[JsObject]
      jsobj.fields("questions").asInstanceOf[JsArray].elements.size mustEqual 100
    }
    "be reentrant" in {
      val largeJsonSource = FileUtils.readAllCharsFromResource("test.json")
      List.fill(20)(largeJsonSource).map(JsonParser.parse).toList.map {
        _.asInstanceOf[JsObject].fields("questions").asInstanceOf[JsArray].elements.size
      } mustEqual List.fill(20)(100)
    }
  }
}

object JsonParserPerformanceTest extends App {
  val largeJsonSource = FileUtils.readAllCharsFromResource("test.json")

  val smallJsonSource =
    """
      |{
      |      "tags": [
      |        "c#",
      |        "asp.net",
      |        "profiling"
      |      ],
      |      "answer_count": 1,
      |      "favorite_count": 0,
      |      "question_timeline_url": "/questions/5667978/timeline",
      |      "question_comments_url": "/questions/5667978/comments",
      |      "question_answers_url": "/questions/5667978/answers",
      |      "question_id": 5667978,
      |      "owner": {
      |        "user_id": 299408,
      |        "user_type": "registered",
      |        "display_name": "Joshua Enfield",
      |        "reputation": 766,
      |        "email_hash": "a9d1f9135b43b217b9325eed54745460"
      |      },
      |      "creation_date": 1302806349,
      |      "last_edit_date": 1302806677,
      |      "last_activity_date": 1302810904,
      |      "up_vote_count": 2,
      |      "down_vote_count": 0,
      |      "view_count": 15,
      |      "score": 2,
      |      "community_owned": false,
      |      "title": "Code Profiling ASP.NET MVC2 applications"
      |    }
    """.stripMargin

  // warm up
  print("Warming up")
  for (i <- 1 until 120) {
    spray.json.JsonParser(largeJsonSource)
    JsonParser.parse(largeJsonSource)
    if (i % 20 == 0) print(".")
  }
  println()
  println("Running tests with large document")
  var count = 200
  var start = System.currentTimeMillis
  for (i <- 0 to count) {
    spray.json.JsonParser(largeJsonSource)
  }
  var end = System.currentTimeMillis - start
  val time1 = end/count.toDouble
  println(s"Parse time spray parser: $time1 msec")
  start = System.currentTimeMillis
  val parser = new JsonParser
  for (i <- 0 to count) {
    parser.parse(largeJsonSource)
  }
  end = System.currentTimeMillis - start
  val time2 = end/count.toDouble
  println(s"Parse time new parser: $time2 msec")
  println(s"Speedup: ${(time1/time2).toInt}x")

  println("Running tests with small document")

  count = 20000
  start = System.currentTimeMillis
  for (i <- 0 to count) {
    spray.json.JsonParser(smallJsonSource)
  }
  end = System.currentTimeMillis - start
  val time3 = end/count.toDouble
  println(s"Parse time spray parser: $time3 msec")
  start = System.currentTimeMillis
  for (i <- 0 to count) {
    parser.parse(smallJsonSource)
  }
  end = System.currentTimeMillis - start
  val time4 = end/count.toDouble
  println(s"Parse time new parser: $time4 msec")
  println(s"Speedup: ${(time3/time4).toInt}x")

}