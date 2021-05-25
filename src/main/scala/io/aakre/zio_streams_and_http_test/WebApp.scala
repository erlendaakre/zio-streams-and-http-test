package io.aakre.zio_streams_and_http_test

import io.aakre.zio_streams_and_http_test.Application.countWords
import zhttp.http.{Http, Method, Request, Response, _}
import zio.json.EncoderOps
import zio.{Ref, ZIO}

import scala.util.Try

object WebApp {

  def apply(state: Ref[List[Event]]): Http[Any, HttpError, Request, Response] = Http.collectM[Request] {
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

  private val timeFormatError = Response.fromHttpError(HttpError.BadRequest("Invalid time, times must be given as unix time, eg: \"1621957740\""))
  private val startAfterEndTimeError = Response.fromHttpError(HttpError.BadRequest("End time can not be before start time"))
}
