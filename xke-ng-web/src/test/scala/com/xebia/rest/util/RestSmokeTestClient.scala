package com.xebia.rest.util

import dispatch._
import scala.reflect.BeanInfo
import scala.xml._
import scala.io._
import java.util.Date
import com.xebia.xkeng.model._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.joda.time.format._
import org.joda.time.DateTime
import net.liftweb.json.JsonAST.{ JValue, JArray }
import net.liftweb.json.ext.JodaTimeSerializers
import javax.security.auth.login.LoginContext
import com.xebia.xkeng.model.{ Credential, Session, Location, Conference, Author, Rating, Comment }
import net.liftweb.common.Logger
import com.xebia.xkeng.serialization.util._
import com.xebia.xkeng.rest.JsonDomainConverters._

object RestSmokeTestClient {

  val host = "localhost"
  //val host = "ec2-46-137-184-99.eu-west-1.compute.amazonaws.com"
  val port = 8080
  val http = new Http

  val xkeStartDate = new DateTime().plusDays(3)
  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
  val c1 = Comment("bla bla comment", "peteru")
  val r1 = Rating(10, "peteru" )
  val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
  val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")
  val lbl = Set("Scala", "DSL")
  val s1 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "Mongo is a nosql db", "STRATEGIC", "10 people")
  val s2 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l2, "Scala rocks even more", "Scala is a scalable programming language", "STRATEGIC", "20 people", List(a1, a2), Nil, Nil, lbl)
  val c = Conference("XKE", xkeStartDate, xkeStartDate.plusHours(4), Nil, List(l1, l2))

  def searchConference(id: String): Conference = {
    query("conference/" + id) {
      fromConferenceJson(_)
    }
  }
  
  def queryLabels():Set[String] = {
    query("labels") {
      r => deserializeStringList(serializeStringsToJArray(r)).toSet
    }
  }

  def addConference(c: Conference): Conference = {
    add("conference", conferenceToJValue(c)) {
      fromConferenceJson(_)
    }
  }

  def updateConference(c: Conference): Int = {
    val (status, _) = update("conference/" + c._id.toString, conferenceToJValue(c))
    status
  }

  def addSession(confId: String, session: Session): Session = {
    add("conference/" + confId + "/session", sessionToJValue(session)) {
      fromSessionJson(false)(_)
    }
  }

  def rateSession(sessionId: Long, rate: Rating): List[Int] = {
    add("feedback/" + sessionId + "/rating", ("rate" -> rate.rate))(r => deserializeIntList(serializeToJson(r)))
  }

   def commentSession(sessionId: Long, comment: Comment): List[Comment] = {
    add("feedback/" + sessionId + "/comment", ("comment" -> comment.comment))(fromCommentListJson(_))
  }

  
  def query[T](target: String)(callback: String => T) = {
    val req = new Request(:/(host, port))
    http x ((req / target >:> identity) {
      case (200, response, _, _) => {
        callback(io.Source.fromInputStream(response.getEntity().getContent()).getLines.mkString)
      }
      case (status, _, _, _) => throw new IllegalArgumentException("Query %s did not yield a result" format target)
    })
  }

  //Low level http methods
  private def add[T](target: String, json: JValue)(callback: String => T): T = {
    http(:/(host, port).POST / target << serializeToJsonStr(json) >~ { resp => callback(resp.getLines.mkString) })

  }

   //Low level http methods
  private def update[T](target: String, json: JValue, callback: String => T): T = {
    http(:/(host, port).PUT / target <<< serializeToJsonStr(json) >~ { resp => callback(resp.getLines.mkString) })

  }

  
  private def update(target: String, json: JValue) = {
    val req = new Request(:/(host, port).PUT)
    val (status, headers) = http x ((req / target <<< serializeToJsonStr(json) >:> identity) {
      case (status, _, _, out) => (status, out())
    })
    (status, headers)

  }

  private def delete(target: String)(callback: String => Unit = printResp) = {
    http(:/(host, port).DELETE / target >~ { resp => callback(resp.getLines.mkString) })
  }

  val printResp = (resp: String) => println(resp)

  def main(args: Array[String]) {
    println("Create new conference...")
    val newConf = addConference(c)
    println("new conference %s" format newConf)

    println("Search added conference...")
    var found = searchConference(newConf._id.toString)
    println("found conference %s" format found)

    println("Update conference...")
    val status = updateConference(c.copy(title = "XKENG"))
    assert(status == 200)

    println("Search updated conference...")
    found = searchConference(newConf._id.toString)
    println("found conference %s" format found)

    println("Add session to conference...")
    val newSession = addSession(c._id.toString, s2)
    println("new session %s" format newSession)

    println("Add rating to session...")
    val ratings = rateSession(newSession.id, r1)
    println("rated session %s %s" format (newSession, ratings))

    println("Add comment to session...")
    val comments = commentSession(newSession.id, c1)
    println("commented session %s %s" format (newSession, comments))

    println("Query labels...")
    val labels = queryLabels()
    println("labels %s" format (labels))
    
    
  }

}