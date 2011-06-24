package com.xebia.xkeng.rest

import net.liftweb.http.rest.RestHelper
import net.liftweb.common._
import net.liftweb.http._
import org.joda.time.DateTime
import net.liftweb.json.ext.JodaTimeSerializers

import collection.mutable.{ListBuffer => MList}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.xebia.xkeng.dao.RepositoryComponent
import JsonDomainConverters._

trait XKENGDispatchAPI extends RestHelper with Logger {
  this:RepositoryComponent =>

  serve {
    case Req("conferences" :: year :: Nil, _, GetRequest) =>
      handleConferencesQry(year.toInt)
/*
    case Req("session" :: sessionId :: Nil, _, GetRequest) =>
      handleRead(sessionId)
    case req @ Req("session" :: sessionId :: Nil, _, PutRequest) =>
      handleUpdate(req.body.toOption, sessionId)
    case req @ Req("session" :: Nil, _, PostRequest) =>
      handleCreate(req.body.toOption)
    case Req("xkeng" :: "session" :: sessionId :: Nil, _, DeleteRequest) =>
      handleDelete(sessionId)
*/
  }

  private def handleConferencesQry(year:Int) = {
    Full(JsonResponse(conferenceRepository.findConferences(year)))
  }
/*

  private def handleReadAll(year:Int):Box[LiftResponse] = {
     val res = ("xkesessions" -> XKESessionRepo.findAll.map(s => Conversion.toJson(Some(s))))
     Full(JsonResponse(res))
  }

  private def handleRead(sessionId:String):Box[LiftResponse] = {
    Full(JsonResponse(Conversion.toJson(XKESessionRepo.findById(sessionId))))
  }

  private def handleDelete(sessionId:String):Box[LiftResponse] = {
    XKESessionRepo.delete(sessionId)
    Full(OkResponse())
  }


  private def handleCreate(jsonBody:Option[Array[Byte]]):Box[LiftResponse] = {
    handleXKESession(jsonBody)(_.copy(id = ""))
  }

  private def handleUpdate(jsonBody:Option[Array[Byte]], sessionId:String):Box[LiftResponse]  = {
     XKESessionRepo.findById(sessionId) match {
       case Some(xkeSession) =>  handleXKESession(jsonBody)(_.copy(id = sessionId))
       case _ => Full(BadResponse())
     }
  }

  private def handleXKESession(jsonBody:Option[Array[Byte]])(doWithXKESession : (XKESession) => XKESession) = {
    jsonBody match {
    case Some(json) => {
      val bodyStr = new String(json)
      debug("handleXKESession - Body is " + bodyStr)
      val  xkeSession = Conversion.fromJson(bodyStr)
      val result = doWithXKESession(xkeSession)

      Full(JsonResponse(Conversion.toJson(Some(XKESessionRepo.saveOrUpdate(result)))))
      }
    case _ => Full(BadResponse())
  }
  }

*/



}