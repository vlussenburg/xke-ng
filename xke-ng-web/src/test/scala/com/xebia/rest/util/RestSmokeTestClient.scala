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
import net.liftweb.util.Helpers._

object RestSmokeTestClient {

  trait Config {
    val host: String
    val contextRoot: String
    val port: Int
    val secure: Boolean
  }
  case class LocalhostSecureCfg(host: String = "localhost", contextRoot: String = "xkeng", port: Int = 8443, secure: Boolean = true) extends Config
  case class LocalhostCfg(host: String = "localhost", contextRoot: String = "xkeng", port: Int = 8020, secure: Boolean = false) extends Config
  case class AwsSecureCfg(host: String = "ec2-46-137-184-99.eu-west-1.compute.amazonaws.com", contextRoot: String = "xkeng", port: Int = 8443, secure: Boolean = true) extends Config
  case class AwsCfg(host: String = "ec2-46-137-184-99.eu-west-1.compute.amazonaws.com", contextRoot: String = "xkeng", port: Int = 8080, secure: Boolean = false) extends Config
  case class AwsApacheSecureCfg(host: String = "ssl-lb-xkeng-1607107363.eu-west-1.elb.amazonaws.com", contextRoot: String = "xkeng", port: Int = 443, secure: Boolean = true) extends Config
  case class AwsXkeXebiaComCfg(host: String = "xke.xebia.com", contextRoot: String = "xkeng", port: Int = 443, secure: Boolean = true) extends Config
 
  //define configuration to use:
  val cfg = LocalhostCfg()
  object RestClientHelperImpl extends RestClientHelper {
    val host = cfg.host
    val contextRoot = cfg.contextRoot
    val port = cfg.port
    val secure = cfg.secure
  }

  import RestClientHelperImpl._

  val xkeStartDate = new DateTime().plusDays(3)
  val l1 = Location("Maup", 20)
  val l3 = Location("Meeting Room", 10)
  val l2 = Location("Laap", 30)
  val c1 = Comment("bla bla comment", "peteru")
  val r1 = Rating(10, "peteru")
  val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
  val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")
  val lbl = Set("Scala", "DSL")
  val s1 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "Mongo is a nosql db", "STRATEGIC", "10 people")
  val s2 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l2, "Scala rocks even more", "Scala is a scalable programming language", "STRATEGIC", "20 people", List(a1, a2), Nil, Nil, lbl)
  val c = Conference("XKE", xkeStartDate, xkeStartDate.plusHours(4), Nil, List(l1, l2))

  def queryConference(id: String): Option[Conference] = {
    query("conference/" + id) {
      fromConferenceJson(_)
    }
  }

  def queryConferenceSlots(id: String): Option[Conference] = {
    query("conference/" + id + "/slots") {
      fromConferenceJson(_)
    }
  }

  
  def deleteConference(id: String): Unit = {
    delete("conference/" + id)(printResp)
  }

  def queryLabels(): Option[Set[String]] = {
    query("labels") {
      r =>
        deserializeStringList(serializeStringsToJArray(r)).toSet
    }
  }

  def querySession(id: Long): Option[Session] = {
    query("session/" + id) {
      r =>
        fromSessionJson(false)(r)
    }
  }
  def deleteSession(id: Long): Unit = {
    delete("session/" + id)(printResp)
  }

  def queryLabelsByAuthor(userId: String): Option[Set[String]] = {
    query("labels/author/" + userId) {
      r =>
        deserializeStringList(serializeStringsToJArray(r)).toSet
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

  def updateSession(c: Conference, s: Session): Int = {
    val (status, _) = update("session/" + s.id, sessionToJValue(s))
    status
  }
  def addSession(confId: String, session: Session): Session = {
    add("conference/" + confId + "/session", sessionToJValue(session)) {
      fromSessionJson(false)(_)
    }
  }

  def addLocation(location: Location): Location = {
    add("location", locationToJValue(location)) {
      Location(_)
    }

  }

  def rateSession(sessionId: Long, rate: Rating): List[Int] = {
    add("feedback/" + sessionId + "/rating", ("rate" -> rate.rate))(r => deserializeIntList(serializeToJson(r)))
  }

  def commentSession(sessionId: Long, comment: Comment): List[Comment] = {
    add("feedback/" + sessionId + "/comment", ("comment" -> comment.comment))(fromCommentListJson(_))
  }
  def login(username: String, password: String): Unit = {
    login(username, password, false)
  }

  def login(username: String, password: String, encrypted: Boolean): Unit = {
    add("login", if (encrypted) ("username" -> username) ~ ("encryptedPassword" -> password) else Credential(username, password, encrypted).serializeToJson)(a => Unit)
  }

  val printResp = (resp: String) => println(resp)

  def main(args: Array[String]) {
    println("Login...")
    val aPwd = new String(hexDecode("757065746572"))
    val aUser = "716AF9A87BD5A9735C20AC8FCED05F40"
    val loggedIn = login(aPwd, aUser, true)

    //    println("Create location")
    //    val l = addLocation(l3)
    //    assert(l.description == l3.description)
    //    println("new location %s" format l)
    //    

    println("Create new conference...")
    val newConf = addConference(c)
    assert(newConf._id == c._id)
    println("new conference %s" format newConf)

    println("Search added conference...")
    var found = queryConference(newConf._id.toString)
    assert(found != None)
    println("found conference %s" format found)

    println("Update conference...")
    var status = updateConference(c.copy(title = "XKENG"))
    assert(status == 200)

    println("Query updated conference...")
    found = queryConference(newConf._id.toString)
    assert(found != None)
    assert(found.get.title == "XKENG")
    println("found conference %s" format found)

    println("Add session to conference...")
    val newSession = addSession(c._id.toString, s2)
    assert(newSession.id != s2.id)
    println("new session %s" format newSession)

    println("Update session ...")
    status = updateSession(c, newSession.copy(title = s2.title + " title changed!"))
    assert(status == 200)

    println("Query session ...")
    var queriedSession = querySession(newSession.id)
    assert(queriedSession != None)
    assert(queriedSession.get.title == s2.title + " title changed!")
    println("queried session %s" format queriedSession)

    println("Add rating to session...")
    val ratings = rateSession(newSession.id, r1)
    assert(ratings.contains(r1.rate))
    println("rated session %s %s" format (newSession, ratings))

    println("Add comment to session...")
    val comments = commentSession(newSession.id, c1)
    println("commented session %s %s" format (newSession, comments))
    assert(comments.map(_.comment).contains(c1.comment))

    println("Query conference slots...")
    val slots = queryConferenceSlots(newConf._id.toString)
    assert(slots != None)

    println("Delete session...")
    deleteSession(newSession.id)
    println("deleted session %s" format (newSession.id))

    println("Query session...")
    queriedSession = querySession(newSession.id)
    assert(queriedSession == None)
    println("queried session %s" format (queriedSession))

    println("Query labels...")
    var labels = queryLabels()
    assert(labels != None)
    println("labels %s" format (labels))

    println("Query labels by author...")
    labels = queryLabelsByAuthor(a2.userId)
    assert(labels != None)
    println("labels %s" format (labels))

    println("Delete conference...")
    deleteConference(c._id.toString)
    println("deleted conference %s" format (newConf._id))

    println("Search added conference...")
    found = queryConference(newConf._id.toString)
    assert(found == None)
    println("found conference %s" format found)
    
    

  }
  /*
  
        var found = queryConference(xke)
    
    
    def calcRatings(session:Session) = {
      val ratings = session.ratings
            val sum = ratings.foldLeft(0)(_ + _.rate)
      val total = ratings.size
      (if (total == 0) 0 else sum / total, total)
      "%s\t%s\t%s".format(session.title, if (total == 0) 0 else sum.toDouble / total.toDouble, total)

    }
    
    val result = found.map{_.sessions map calcRatings}.getOrElse(List("Empty"))
    result foreach println


  
  */

  trait RestClientHelper {
    val host: String
    val port: Int
    val contextRoot: String
    protected val secure: Boolean
    protected val executer = new Http
    private def req = {
      val req = :/(host, port) / contextRoot
      if (secure) req.secure else req
    }

    def query[T](target: String)(callback: String => T): Option[T] = {
      //val req = new Request(:/(host, port)).secure
      executer x ((req / target >:> identity) {
        case (200, response, _, _) => {
          Some(callback(io.Source.fromInputStream(response.getEntity().getContent()).getLines.mkString))
        }
        case (status, _, _, _) => println("Query %s did not yield a result" format target); None
      })
    }

    //Low level http methods
    def add[T](target: String, json: JValue)(callback: String => T): T = {
      executer(req / target << serializeToJsonStr(json) >~ { resp => callback(resp.getLines.mkString) })

    }

    //Low level http methods
    def update[T](target: String, json: JValue, callback: String => T): T = {
      executer(req / target <<< serializeToJsonStr(json) >~ { resp => callback(resp.getLines.mkString) })

    }

    def update(target: String, json: JValue) = {
      //val req = new Request(:/(host, port).PUT.secure)
      val (status, headers) = executer x ((req / target <<< serializeToJsonStr(json) >:> identity) {
        case (status, _, _, out) => (status, out())
      })
      (status, headers)

    }

    def delete(target: String)(callback: String => Unit = printResp) = {
      executer(req.DELETE / target >~ { resp => callback(resp.getLines.mkString) })
    }
  }
}