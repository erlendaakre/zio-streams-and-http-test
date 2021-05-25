package io.aakre.zio_streams_and_http_test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.defined
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class ApplicationTest extends AnyFlatSpec {

  val testEvents = List(
    Event("baz", "cheese", 1),
    Event("baz", "cheese", 2),
    Event("baz", "cheese", 3),
    Event("bar", "pineapple", 4),
    Event("bar", "pineapple", 5),
    Event("foo", "broccoli", 9000)
  )

  "Parsing a json string" should "produce the expected Event" in {
    val str = "{ \"event_type\": \"baz\", \"data\": \"foo\", \"timestamp\": 1620215144 }"
    Application.makeEvent(str) shouldBe Some(Event("baz", "foo", 1620215144))
  }

  "A Result" should "produce the expected json" in {
    import zio.json.EncoderOps
    ResultForType("baz", Map("cheese" -> 3)).toJson shouldBe "{\"type\":\"baz\",\"count\":{\"cheese\":3}}"
  }

  "Counting words in a list of events" should "produce the correct result" in {
    val res = Application.countWords(testEvents)
    res.wordCount.size shouldBe 3
    res.wordCount.find(p => p == ResultForType("baz", Map("cheese" -> 3))) shouldBe defined
    res.wordCount.find(p => p == ResultForType("bar", Map("pineapple" -> 2))) shouldBe defined
    res.wordCount.find(p => p == ResultForType("foo", Map("broccoli" -> 1))) shouldBe defined
  }

  "Counting words from a certain time " should "produce the correct results" in {
    val res = Application.countWords(testEvents, 4)
    res.wordCount.size shouldBe 2
    res.wordCount.find(p => p == ResultForType("bar", Map("pineapple" -> 2))) shouldBe defined
    res.wordCount.find(p => p == ResultForType("foo", Map("broccoli" -> 1))) shouldBe defined
  }
  "Counting words before a certain time " should "produce the correct results" in {
    val res = Application.countWords(testEvents, 0, 5)
    res.wordCount.size shouldBe 2
    res.wordCount.find(p => p == ResultForType("baz", Map("cheese" -> 3))) shouldBe defined
    res.wordCount.find(p => p == ResultForType("bar", Map("pineapple" -> 1))) shouldBe defined
  }
  "Counting words in an interval " should "produce the correct results" in {
    val res = Application.countWords(testEvents, 3, 6)
    res.wordCount.size shouldBe 2
    res.wordCount.find(p => p == ResultForType("baz", Map("cheese" -> 1))) shouldBe defined
    res.wordCount.find(p => p == ResultForType("bar", Map("pineapple" -> 2))) shouldBe defined
  }
}
