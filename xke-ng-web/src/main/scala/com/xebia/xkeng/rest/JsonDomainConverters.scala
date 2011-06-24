package com.xebia.xkeng.rest

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.joda.time.format._
import org.joda.time.DateTime
import com.xebia.xkeng.model.{Location, Conference}
import net.liftweb.json.JsonAST.{JValue, JArray}

/**
 * {
  "conferences":[
  {
    "id":"1234",
    "title":"XKE",
    "date":"2011-06-07T00:00:00.000Z",
    "startTime":"0000-00-00T16:00:00.000Z",
    "endTime":"0000-00-00T21:00:00.000Z",
  "locations":[
  {
    "id":"701",
    "description":"Maupertuus",
    "standard":"true"
  },
  {
    "id":"702",
    "description":"Library",
    "standard":"true"
  },
  {
    "id":"703",
    "description":"Laap",
    "standard":"true"
  }
  ]
  }
  ]
}
 */
object JsonDomainConverters {
   val fmt = ISODateTimeFormat.dateTime()

  def conferenceToJValue(conference: Conference):JValue = {
      //TODO add start and end time
      ("id" -> conference._id.toString) ~
        ("title" -> conference.name) ~
        ("date" -> fmt.print(conference.date)) ~
        ("startTime" -> fmt.print(conference.date)) ~
        ("endTime" -> fmt.print(conference.date)) ~
     ("locations" -> conference.locations.map(locationToJValue(_)))
    }

  def locationToJValue(location: Location):JValue = {
      //TODO add location type
      location.serializeToJson merge ("standard" -> "true")
    }

   implicit def conferencesToJArray(conferences: List[Conference]):JValue = new JArray(conferences.map(conferenceToJValue))


}