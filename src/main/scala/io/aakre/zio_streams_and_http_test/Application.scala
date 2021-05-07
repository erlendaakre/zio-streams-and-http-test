package io.aakre.zio_streams_and_http_test

import zhttp.http._
import zhttp.service.Server
import zio.console.putStrLn
import zio.json.{DecoderOps, DeriveJsonDecoder, JsonDecoder}
import zio.process.Command
import zio.{ExitCode, Ref, URIO, ZIO}

object Application extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = prog.exitCode

  private val state = zio.Runtime.default.unsafeRun(Ref.make(0)) // (╯°□°）╯︵ ┻━┻

  private val blackBox = Command("blackbox.win.exe").linesStream
  private val eventStream = blackBox.map(makeEvent).filter(_.nonEmpty).map(_.get).tap(e => putStrLn(e.toString))
  private val streamEventsToState = eventStream.foreach(event => state.set(event.timestamp.toInt))

  private val webApp = Http.collectM[Request] {
    case Method.GET -> Root =>
      state.get.flatMap(s =>
        ZIO.succeed(Response.text(s"Time: ${System.currentTimeMillis()}\nEvents: $s"))
      )
  }

  private val prog = Server.start(8090, webApp).zipPar(streamEventsToState.run)

  type EventType = String
  type Word = String
  case class Event(event_type: EventType, data: Word, timestamp: Long)
  object Event {
    implicit val decoder: JsonDecoder[Event] = DeriveJsonDecoder.gen[Event]
  }

  def makeEvent(s: String): Option[Event] = s.fromJson[Event].toOption

  def groupEvents(es: List[Event]): Map[EventType, List[Event]] = es.groupBy(_.event_type)

  def countWords(es: List[Event]): Map[Word, Int] = es.groupBy(_.data).map(t => (t._1, t._2.size))
}
