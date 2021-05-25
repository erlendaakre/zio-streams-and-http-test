package io.aakre.zio_streams_and_http_test

import zhttp.http._
import zhttp.service.Server
import zio.console.putStrLn
import zio.json.{DecoderOps, EncoderOps}
import zio.process.Command
import zio.{ExitCode, Ref, URIO, ZIO}

import scala.util.Try

object Application extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = prog.exitCode

  // FIXME: Zionomicon pg 150           (╯°□°）╯︵ `unsafeRun`           `mutable listbuffer` ︵└(՞▃՞ └)
  private val state = zio.Runtime.default.unsafeRun(Ref.make(collection.mutable.ListBuffer.empty[Event]))

  private val blackBox = Command("blackbox.win.exe").linesStream
  private val eventStream = blackBox.map(makeEvent).filter(_.nonEmpty).map(_.get).tap(e => putStrLn(e.toString))
  private val streamEventsToState = eventStream.foreach(event => state.update(_ += event))

  private val webApp = Http.collectM[Request] {
    case Method.GET -> Root =>
      state.get.flatMap(events => ZIO.succeed(Response.jsonString(countWords(events).toJsonPretty)))

    case Method.GET -> Root / "after" / startTimeStr =>
      val startTimeOpt = Try(startTimeStr.toLong).toOption
      if (startTimeOpt.isEmpty) ZIO.succeed(timeFormatError)
      else state.get.flatMap(events => ZIO.succeed(Response.jsonString(countWords(events, startTimeOpt.get).toJsonPretty)))

    case Method.GET -> Root / "before" / endTimeStr =>
      val endTimeStrOpt = Try(endTimeStr.toLong).toOption
      if (endTimeStrOpt.isEmpty) ZIO.succeed(timeFormatError)
      else state.get.flatMap(events => ZIO.succeed(Response.jsonString(countWords(events, 0, endTimeStrOpt.get).toJsonPretty)))

    case Method.GET -> Root / "between" / startTimeStr / endTimeStr =>
      val startTimeOpt = Try(startTimeStr.toLong).toOption
      val endTimeStrOpt = Try(endTimeStr.toLong).toOption
      if (startTimeOpt.isEmpty || endTimeStrOpt.isEmpty) ZIO.succeed(timeFormatError)
      else if (startTimeOpt.get > endTimeStrOpt.get) ZIO.succeed(startAfterEndTimeError)
      else state.get.flatMap(events => ZIO.succeed(Response.jsonString(countWords(events, startTimeOpt.get, endTimeStrOpt.get).toJsonPretty)))
  }

  private val prog = Server.start(8090, webApp) zipPar streamEventsToState.run

  def makeEvent(s: String): Option[Event] = s.fromJson[Event].toOption

  def countWords(events: Iterable[Event], start: Long = 0L, end: Long = Long.MaxValue): Result = Result(
    events.filter(e => e.timestamp >= start && e.timestamp < end).
      groupBy(_.event_type).map(groupedType => ResultForType(
      groupedType._1, groupedType._2.groupBy(_.data).map(
        groupedWords => (groupedWords._1, groupedWords._2.size)))))

  private val timeFormatError = Response.fromHttpError(HttpError.BadRequest("Invalid time, times must be given as unix time, eg: \"1621957740\""))
  private val startAfterEndTimeError = Response.fromHttpError(HttpError.BadRequest("End time can not be before start time"))
}
