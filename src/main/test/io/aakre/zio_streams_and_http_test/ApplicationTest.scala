package io.aakre.zio_streams_and_http_test

import io.aakre.zio_streams_and_http_test.Application.Event
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{contain, defined}
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

  "Counting words in a list of events" should "produce the correct result" in {
    val res = Application.countWords(testEvents)
    res.size shouldBe 3
    res.get("cheese") shouldBe Some(3)
    res.get("pineapple") shouldBe Some(2)
    res.get("broccoli") shouldBe Some(1)
  }

  "Grouping events" should "produce a map of EventTypes to List of Event" in  {
    val grouped = Application.groupEvents(testEvents)
    grouped.keySet should contain allOf("foo", "bar", "baz")
    grouped("bar").size shouldBe 2
    grouped("bar").head.data shouldBe "pineapple"
  }

}
