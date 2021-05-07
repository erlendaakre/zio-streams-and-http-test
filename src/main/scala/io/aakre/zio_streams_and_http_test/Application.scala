package io.aakre.zio_streams_and_http_test

import zhttp.http._
import zhttp.service.Server
import zio.console.putStrLn
import zio.json.{DecoderOps, DeriveJsonDecoder, JsonDecoder}
import zio.process.Command
import zio.{ExitCode, Ref, URIO, ZIO}

object Application extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = prog.exitCode

  private val state = zio.Runtime.default.unsafeRun(Ref.make(collection. mutable.ListBuffer.empty[Event])) // (╯°□°）╯︵ ┻━┻

  private val blackBox = Command("blackbox.win.exe").linesStream
  private val eventStream = blackBox.map(makeEvent).filter(_.nonEmpty).map(_.get).tap(e => putStrLn(e.toString))
  private val streamEventsToState = eventStream.foreach(event => for {
    s <- state.get
    _ <- state.set(s += event)
  } yield ())

  private val webApp = Http.collectM[Request] {
    case Method.GET -> Root =>
      state.get.flatMap(events =>
        ZIO.succeed(Response.text(superPrettyResult(groupEvents(events.toList).map(s => (s._1, countWords(s._2))))))
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

  def superPrettyResult(data: Map[EventType, Map[Word,Int]]): String = {
    def eventTypeToString(eventType: EventType, count: Map[Word,Int]) =
      s"$eventType\n${"="*eventType.length}\n" + count.mkString("\n")
    "Event Count\n===========\n\n" + data.map(d => eventTypeToString(d._1, d._2)).mkString("\n\n")
  }
}
