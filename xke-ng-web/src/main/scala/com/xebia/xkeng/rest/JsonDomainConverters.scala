package com.xebia.xkeng.rest

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.joda.time.format._
import org.joda.time.DateTime
import net.liftweb.json.JsonAST.{ JValue, JArray }
import net.liftweb.json.ext.JodaTimeSerializers
import javax.security.auth.login.LoginContext
import com.xebia.xkeng.model.{Credential, Session, Location, Conference}

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
object JsonDomainConverters {
  val fmt = ISODateTimeFormat.dateTime()
  object AsDateTime {
    def unapply(isoDateString: String): Option[DateTime] = {
      try { Some(fmt.parseDateTime(isoDateString)) }
      catch {
        case e: Exception => None
      }

    }
  }

  implicit def sessionToJValue(session: Session): JValue = {
    ("id" -> session._id.toString) ~
    ("title" -> session.title) ~
    ("presenter" -> session.presenter) ~
    ("descr" -> session.descr)
  }

  implicit def conferenceToJValue(conference: Conference): JValue = {
    ("id" -> conference._id.toString) ~
      ("title" -> conference.title) ~
      ("begin" -> fmt.print(conference.begin)) ~
      ("end" -> fmt.print(conference.end)) ~
      ("locations" -> conference.locations.map(locationToJValue(_)))
  }

  /**
   *
    "locations" : [
	    {
		    "id" : -1098499121,
		    "name" : "Maup",
		    "capacity" : 20
	    },
	    {
		    "id" : -1098499119,
		    "name" : "Laap",
		    "capacity" : 30
	    }
    ]
   */
  implicit def locationToJValue(location: Location): JValue = {
    ("id" -> location.id) ~
      ("name" -> location.name) ~
      ("capacity" -> location.capacity)
  }

  implicit def conferencesToJArray(conferences: List[Conference]): JValue = new JArray(conferences.map(conferenceToJValue))
  implicit def locationsToJArray(locations: List[Location]): JValue = new JArray(locations.map(locationToJValue))

  def fromConferenceJson(jsonString: String): Conference = {
    val JObject(jsonValue) = JsonParser.parse(jsonString)
    def toConf(confJson: JValue) = {
      val id: Option[String] = (confJson \\ "id") match {
        case JString(id) => Some(id)
        case _ => None
      }
      val JString(title) = confJson \\ "title"
      val JString(AsDateTime(begin)) = confJson \\ "begin"
      val JString(AsDateTime(end)) = confJson \\ "end"
      val locations: List[Location] = (confJson \\ "locations") match {
        case JArray(locs) => locs.map((v: JValue) => deserialize[Location](Printer.pretty(JsonAST.render(v))))
        case _ => Nil
      }
      val conference = Conference(title, begin, end, Nil, locations)
      conference
    }

    toConf(jsonValue)
  }

  def fromCredentialJson(jsonString: String): Credential = {
	  val JObject(jsonValue) = JsonParser.parse(jsonString)
	  val JString(name) = jsonValue \\ "name"
	  val JString(pwd) = jsonValue \\ "cryptedPassword"

	  Credential(name, pwd)
  }

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all
  def deserialize[T](json: String)(implicit m: Manifest[T]): T = {
    Serialization.read[T](json)
  }

}