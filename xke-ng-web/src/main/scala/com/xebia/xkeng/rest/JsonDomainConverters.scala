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

  /**
   * {
    "title":"The power of Android",
    "description":"No easy task to develop for Android, due to all the stuff.
	Join this session to have some info.",
    "startTime":"0000-00-00T18:00:00.000Z",
    "endTime":"0000-00-00T19:00:00.000Z",
    "lastUpdate":"2011-07-01T17:58:54.812Z",
    "lastReschedule":"2011-12-12T17:58:54.812Z",
    "id":"8802",
    "limit":"10 people",
    "type":"STRATEGIC",
	"location": {
           "description": "Library",
           "id": "714",
           "standard": "true"
	}
    }
   */
  //TODO: lastUpdate, lastReschedule, limit, type
  implicit def sessionToJValue(session: Session): JValue = {
    ("id" -> session.id) ~
    ("title" -> session.title) ~
    ("description" -> session.description) ~
    ("startTime" -> fmt.print(session.start)) ~
    ("endTime" -> fmt.print(session.end)) ~
    ("presenter" -> session.presenter) ~
    ("lastUpdate" -> fmt.print(session.start)) ~
    ("lastReschedule" -> fmt.print(session.start)) ~
    ("limit" -> "10 people") ~
    ("type" -> "STRATEGIC") ~
    ("location" -> locationToJValue(session.location))
  }
  
  /**
   * 
  "id":"4e4a0c48b39c8578c8f7b6d2",
  "title":"XKE",
  "begin":"2011-08-09T16:00:56.527+02:00",
  "end":"2011-08-09T20:00:56.527+02:00",
  "locations":[{
    "id":-784335934,
    "name":"Maup",
    "capacity":20
  	}]
  }
   */
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
      ("description" -> location.description) ~
      ("capacity" -> location.capacity)
  }

  implicit def conferencesToJArray(conferences: List[Conference]): JValue = new JArray(conferences.map(conferenceToJValue))
  implicit def locationsToJArray(locations: List[Location]): JValue = new JArray(locations.map(locationToJValue))


  def fromSessionJson(isNew:Boolean)(jsonString: String): Session = {
    val JObject(sessJson) = JsonParser.parse(jsonString)
   
    val JString(AsDateTime(start)) = sessJson \\ "startTime"
      val JString(AsDateTime(end)) = sessJson \\ "endTime"      
      val JString(title) = sessJson \\ "title"
      
      //TODO: check where the presenter went to...
      //val JString(presenter) = sessJson \\ "presenter"
      val presenter = "scal@man.com"
      val JString(description) = sessJson \ "description"
      val location =  deserialize[Location](Printer.pretty(JsonAST.render(sessJson \\ "location")))
      val session =  Session(start, end, location, title, description, presenter)
      if(!isNew) {
        val JInt(id) = (sessJson \ "id")
        session.copy(id = id.toLong)
      } else {
        session
      }
    
  
  }

  def fromConferenceJson(jsonString: String): Conference = {
    val JObject(jsonValue) = JsonParser.parse(jsonString)
    def toConf(confJson: JValue) = {
      val id: Option[String] = (confJson \\ "id") match {
        case JString(id) => Some(id)
        case _ => None
      }  //why parse Id if u don't use it?
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