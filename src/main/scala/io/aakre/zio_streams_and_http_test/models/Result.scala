package io.aakre.zio_streams_and_http_test.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Event(event_type: EventType, data: Word, timestamp: Long)
object Event {
  implicit val decoder: JsonDecoder[Event] = DeriveJsonDecoder.gen[Event]
}

case class ResultForType(`type`: EventType, count: Map[Word, Int])
object ResultForType {
  implicit val encoder: JsonEncoder[ResultForType] = DeriveJsonEncoder.gen[ResultForType]
}

case class Result(wordCount: Iterable[ResultForType])
object Result {
  implicit val encoder: JsonEncoder[Result] = DeriveJsonEncoder.gen[Result]
}