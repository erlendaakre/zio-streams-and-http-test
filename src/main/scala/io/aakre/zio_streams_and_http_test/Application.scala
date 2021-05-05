package io.aakre.zio_streams_and_http_test

import zio.console.putStrLn
import zio.json.{DecoderOps, DeriveJsonDecoder, JsonDecoder}
import zio.process.Command
import zio.{ExitCode, Queue, Ref, RefM, URIO, ZIO}
import zhttp.http._
import zhttp.service.Server
import zio.stream.ZSink

object Application extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = prog.exitCode

  private val blackBox = Command("blackbox.win.exe").linesStream
  private val eventStream = blackBox.map(makeEvent).filter(_.nonEmpty).map(_.get)

  val sink = ZSink.collectAll[Event] // TODO: how to access stream in webApp??? zlayers?

  private val webApp = Http.collectM[Request] {
    case Method.GET -> Root => {
      val x = sink.map(chunk => chunk.size).map(x => x)
      // TODO: combine zio below with zio that gets state? (Http.fromEffectFunction?)
      ZIO.succeed(Response.text(s"Time: ${System.currentTimeMillis()}\nEvents: $x"))
    }
  }

  private val prog = for {
    q <- Queue.bounded[Event](100)
    _ <- putStrLn("Starting Server")
    _ <- Server.start(8090, webApp).fork
    _ <- putStrLn("Starting Streaming...")
    _ <- eventStream.tap(e => putStrLn(e.toString)).run(sink)
  } yield ()

  type EventType = String
  case class Event(event_type: EventType, data: String, timestamp: Long)
  object Event {
    implicit val decoder: JsonDecoder[Event] = DeriveJsonDecoder.gen[Event]
  }

  def makeEvent(s: String): Option[Event] = s.fromJson[Event].toOption

  def groupEvents(es: List[Event]): Map[EventType, List[Event]] = es.groupBy(_.event_type)

  def countEvents(es: List[Event]) = ???
}
