package io.aakre.zio_streams_and_http_test

import zio.console.putStrLn
import zio.json.{DecoderOps, DeriveJsonDecoder, JsonDecoder}
import zio.process.Command
import zio.{ExitCode, URIO}

object Server extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = prog.exitCode

  private val blackBox = Command("blackbox.win.exe").linesStream
  private val eventStream = blackBox.map(makeEvent).filter(_.isDefined).map(_.get)

  private val prog = {
    putStrLn("foo") *> eventStream.foreach(e => putStrLn(e.toString))
  }

  case class Event(event_type: String, data: String, timestamp: Long)
  object Event {
    implicit val decoder: JsonDecoder[Event] = DeriveJsonDecoder.gen[Event]
  }

  def makeEvent(s: String): Option[Event] = s.fromJson[Event].toOption
}
