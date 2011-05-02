package com.xebia.rest

import net.liftweb.http.rest.RestHelper
import net.liftweb.common._
import net.liftweb.http._
import org.joda.time.DateTime
import net.liftweb.json.ext.JodaTimeSerializers

import collection.mutable.{ListBuffer => MList}
import net.liftweb.json._

object XKENGDispatchAPI extends RestHelper with Logger {
  serve {
    case Req("xkeng" :: "session" :: sessionId :: Nil, _, GetRequest) =>
    Full(JsonResponse(Conversion.toJson(XKESessionRepo.findById(sessionId))))
    case req @ Req("xkeng" :: "session" :: sessionId :: Nil, _, PutRequest) =>
      handleUpdate(req.body.toOption, sessionId)
    case req @ Req("xkeng" :: "session" :: Nil, _, PostRequest) =>
      handleCreate(req.body.toOption)
    case Req("xkeng" :: "session" :: sessionId :: Nil, _, DeleteRequest)
    => <b>Static</b>
  }

  def handleCreate(jsonBody:Option[Array[Byte]]):Box[LiftResponse] = {
    handleXKESession(jsonBody)(_.copy(id = ""))
  }

  def handleUpdate(jsonBody:Option[Array[Byte]], sessionId:String):Box[LiftResponse]  = {
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

}

case class XKESession(title: String, desc: String, presenter: String, room: String, from: DateTime, to: DateTime, id: String = "")

object Conversion {
  import net.liftweb.json.JsonDSL._

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all

  def toJson(s: Option[XKESession]): JValue = {
    s match {
      case Some(s) => Serialization.write(s)
      case _ => JNull
    }
  }
  def fromJson(json:String): XKESession = {
      Serialization.read[XKESession](json)
  }

}

object XKESessionRepo extends Logger {

  @volatile var counter = 0
  def increment:String = {
    counter += 1
    counter.toString
  }

  private val xkeSessions = MList[XKESession](XKESession("Scala takes over", "BladiBla", "evanderkoogh", "Maup", new DateTime(), new DateTime().plusHours(1), "1234"))

  def findById(id: String): Option[XKESession] = xkeSessions.find(_.id == id)

  def saveOrUpdate(s: XKESession): XKESession = {
    findById(s.id) match {
      case Some(found) => {
        debug("Found " + found)
        (xkeSessions - found) += s
        s
      }
      case _ => {
        val newS = s.copy(id = increment)
        debug("New " + newS)
        xkeSessions += newS
        newS
      }
    }
  }

}