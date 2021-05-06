package io.aakre.zio_streams_and_http_test

import zhttp.http._
import zhttp.service.Server
import zio.console.putStrLn
import zio.json.{DecoderOps, DeriveJsonDecoder, JsonDecoder}
import zio.process.Command
import zio.stream.ZSink
import zio.{ExitCode, URIO, ZIO}

object Application extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = prog.exitCode

  private val blackBox = Command("blackbox.win.exe").linesStream
  private val eventStream = blackBox.map(makeEvent).filter(_.nonEmpty).map(_.get)

  private val sink = ZSink.collectAll[Event] // TODO: how to access stream in webApp???

  private val webApp = Http.collectM[Request] {
    case Method.GET -> Root =>
      for {
        x        <- ZIO.succeed(-1)
        // y     <- sink.map(chunk => chunk.size) // TODO: any way to get the sink contents as a ZIO?
        response <- ZIO.succeed(Response.text(s"Time: ${System.currentTimeMillis()}\nEvents: $x"))
      } yield response
  }

  private val prog = for {
    _ <- putStrLn("Starting Server")
    s <- Server.start(8090, webApp).zipPar(eventStream.tap(e => putStrLn(e.toString)).run(sink))
  } yield s

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
