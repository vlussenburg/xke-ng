package com.xebia.xkeng.rest

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.joda.time.format._
import org.joda.time.DateTime
import net.liftweb.json.JsonAST.{ JValue, JArray }
import net.liftweb.json.ext.JodaTimeSerializers
import javax.security.auth.login.LoginContext
import com.xebia.xkeng.model.{ Credential, Session, Location, Conference, Author, Comment, Rating }
import net.liftweb.common.Logger
import com.xebia.xkeng.serialization.util._


/**
 * {
 * "conferences":[
 * {
 * "id":"1234",
 * "title":"XKE",
 * "date":"2011-06-07T00:00:00.000Z",
 * "startTime":"0000-00-00T16:00:00.000Z",
 * "endTime":"0000-00-00T21:00:00.000Z",
 * "locations":[
 * {
 * "id":"701",
 * "description":"Maupertuus",
 * "standard":"true"
 * },
 * {
 * "id":"702",
 * "description":"Library",
 * "standard":"true"
 * },
 * {
 * "id":"703",
 * "description":"Laap",
 * "standard":"true"
 * }
 * ]
 * }
 * ]
 * }
 */
object JsonDomainConverters extends Logger {

  /**
   * =====================================================
   * to JValue conversions
   * =====================================================
   */

  /**
   * {
   * "title":"The power of Android",
   * "description":"No easy task to develop for Android, due to all the stuff.
   * Join this session to have some info.",
   * "startTime":"0000-00-00T18:00:00.000Z",
   * "endTime":"0000-00-00T19:00:00.000Z",
   * "id":"8802",
   * "limit":"10 people",
   * "type":"STRATEGIC",
   * "location": {
   * "description": "Library",
   * "id": "714",
   * "standard": "true"
   * }
   * }
   */
  //TODO: lastUpdate, lastReschedule, limit, type
  implicit def sessionToJValue(session: Session): JValue = {
    ("id" -> session.id) ~
      ("title" -> session.title) ~
      ("description" -> session.description) ~
      ("startTime" -> fmt.print(session.start)) ~
      ("endTime" -> fmt.print(session.end)) ~
      ("limit" -> session.limit) ~
      ("type" -> session.sessionType) ~
      ("authors" -> authorsToJArray(session.authors)) ~
      ("comments" -> commentsToJArray(session.comments)) ~
      ("ratings" -> ratingsFullToJArray(session.ratings)) ~
      ("labels" -> serializeStringsToJArray(session.labels.toSeq : _*)) ~
      ("location" -> locationToJValue(session.location))
  }

  /**
   *
   * "id":"4e4a0c48b39c8578c8f7b6d2",
   * "title":"XKE",
   * "begin":"2011-08-09T16:00:56.527+02:00",
   * "end":"2011-08-09T20:00:56.527+02:00",
   * "locations":[{
   * "id":-784335934,
   * "name":"Maup",
   * "capacity":20
   * }]
   * }
   */
  implicit def conferenceToJValue(conference: Conference): JValue = {
    ("id" -> conference._id.toString) ~
      ("title" -> conference.title) ~
      ("begin" -> fmt.print(conference.begin)) ~
      ("end" -> fmt.print(conference.end)) ~
      ("sessions" -> conference.sessions.map(sessionToJValue(_))) ~
      ("locations" -> conference.locations.map(locationToJValue(_)))
  }

  /**
   *
   * "locations" : [
   * {
   * "id" : -1098499121,
   * "name" : "Maup",
   * "capacity" : 20
   * },
   * {
   * "id" : -1098499119,
   * "name" : "Laap",
   * "capacity" : 30
   * }
   * ]
   */
  implicit def locationToJValue(location: Location): JValue = {
    ("id" -> location.id) ~
      ("description" -> location.description) ~
      ("capacity" -> location.capacity)
  }

  implicit def commentToJValue(comment: Comment): JValue = {
    ("user" -> comment.userId) ~
      ("comment" -> comment.comment)
  }

  implicit def ratingFullToJValue(rating: Rating): JValue = {
    ("user" -> rating.userId) ~
      ("rate" -> rating.rate)
  }

  
  implicit def conferencesToJArray(conferences: List[Conference]): JValue = new JArray(conferences.map(conferenceToJValue))
  implicit def locationsToJArray(locations: List[Location]): JValue = new JArray(locations.map(locationToJValue))
  implicit def commentsToJArray(comments: List[Comment]): JValue = new JArray(comments.map(commentToJValue))
  implicit def ratingsToJArray(ratings: List[Rating]): JValue = new JArray(ratings.map(rating => JInt(rating.rate)))
  implicit def authorsToJArray(authors: List[Author]): JValue = new JArray(authors.map(_.serializeToJson))
  implicit def labelsToJArray(labels: Set[String]): JValue = serializeStringsToJArray(labels.toSeq : _*)
  def ratingsFullToJArray(ratings: List[Rating]): JValue = new JArray(ratings.map(ratingFullToJValue))

  /**
   * =====================================================
   * from Json to Object conversions
   * =====================================================
   */

  def fromSessionJson(isNew: Boolean)(jsonString: String): Session = {
    val JObject(sessJson) = JsonParser.parse(jsonString)
    val JString(AsDateTime(start)) = sessJson \\! "startTime"
    val JString(AsDateTime(end)) = sessJson \\! "endTime"
    val JString(title) = sessJson \\! "title"
    val JString(limit) = sessJson \! "limit"
    val JString(description) = sessJson \! "description"
    val JString(sessionType) = sessJson \! "type"
    val location = deserialize[Location](sessJson \\ "location")
    val authors: List[Author] = deserializeList[Author](sessJson \\ "authors")
    val commentSerializer = (json: JValue) => fromCommentJson(serializeToJsonStr(json))
    val comments = deserializeList[Comment](sessJson \\ "comments", commentSerializer)
    val ratingsSerializer = (json: JValue) => fromRatingFullJson(serializeToJsonStr(json))
    val ratings = deserializeList[Rating](sessJson \\ "ratings", ratingsSerializer)
    val labels = deserializeStringList(sessJson \\ "labels").toSet

    val session = Session(start, end, location, title, description, sessionType, limit, authors, ratings, comments, labels)

    if (!isNew) {
      val JInt(id) = (sessJson \! "id")
      return session.copy(id = id.toLong)
    }
    session
  }

  def fromConferenceJson(jsonString: String): Conference = {
    val JObject(confJson) = JsonParser.parse(jsonString)

    val JString(title) = confJson \! "title"
    val JString(AsDateTime(begin)) = confJson \\! "begin"
    val JString(AsDateTime(end)) = confJson \\! "end"
    val locations: List[Location] = deserializeList[Location](confJson \\ "locations")
    val sessionSerializer = (json: JValue) => fromSessionJson(false)(serializeToJsonStr(json))
    val sessions: List[Session] = deserializeList[Session](confJson \\ "sessions", sessionSerializer)
    val conference = (confJson \ "id") match {
      case JString(id) => Conference(id, title, begin, end, sessions, locations)
      case _ => Conference(title, begin, end, sessions, locations)
    }
    conference

  }

  def fromLocationJson(isNew: Boolean)(jsonString: String): Location = {
    val JObject(locJson) = JsonParser.parse(jsonString)
    val JString(desc) = locJson \! "description"
    val JInt(capacity) = locJson \! "capacity"
    val location = Location(desc, capacity.toInt)
    if (!isNew) {
      val JInt(id) = (locJson \! "id")
      return location.copy(id = id.toLong)
    }
    location
  }

  def fromCommentJson(jsonString: String): Comment = {
    val JObject(jsonValue) = JsonParser.parse(jsonString)
    val JString(comment) = jsonValue \! "comment"
    //TODO dynamically get current logged in user
    Comment(comment, "guest")
  }

  def fromCommentListJson(jsonString: String): List[Comment] = {
    def fromCommentFullJson(jsonValue: JValue): Comment = {
      val JString(comment) = jsonValue \ "comment"
      val JString(user) = jsonValue \ "user"
      Comment(comment, user)
    }

    val JArray(jsonValue) = JsonParser.parse(jsonString)
    deserializeList[Comment](jsonValue, fromCommentFullJson(_))
  }

  def fromRatingJson(jsonString: String): Rating = {
    val JObject(jsonValue) = JsonParser.parse(jsonString)
    val JInt(rate) = jsonValue \! "rate"
    //TODO dynamically get current logged in user
    Rating(rate.toInt, "guest")
  }

  def fromRatingFullJson(jsonString: String): Rating = {
    val JObject(jsonValue) = JsonParser.parse(jsonString)
    val JInt(rate) = jsonValue \! "rate"
    val JString(user) = jsonValue \! "user"
    Rating(rate.toInt, user)
  }

  def fromCredentialJson(jsonString: String): Credential = {
    val JObject(jsonValue) = JsonParser.parse(jsonString)
    val JString(name) = jsonValue \! "username"
    val pwd = (jsonValue \ "password").values   
    if(pwd != None) Credential(name, pwd.toString) else {
      val JString(encryptedPwd) = (jsonValue \! "encryptedPassword")
      Credential(name, encryptedPwd, true) 
    }
  }

}