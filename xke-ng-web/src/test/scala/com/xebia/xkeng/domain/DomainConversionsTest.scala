
package com.xebia.xkeng.domain
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.{ Mongo, MongoOptions, ServerAddress }
import net.liftweb._
import json.JsonAST.{ JInt, JValue }
import json.{ JsonAST, Printer }
import mongodb._
import org.bson.types.ObjectId
import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
import org.joda.time.DateTime
import com.xebia.xkeng.model._
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.joda.time.format.ISODateTimeFormat
import com.xebia.xkeng.rest.JsonDomainConverters._
import com.xebia.xkeng.serialization.util._
import org.codehaus.jackson.annotate.JsonValue
import org.mortbay.util.ajax.JSON

@RunWith(classOf[JUnitRunner])
class DomainConversionsTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
  val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
  val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")

  val fmt = ISODateTimeFormat.dateTime()
  val xkeStartDate = fmt.parseDateTime("2011-06-27T09:57:47.945Z")
  override def beforeEach() {
  }
  it should "deserialize a conference with json correctly" in {
    val jsonString = """{"title":"TED-style XKE","begin":"2011-11-07T16:00:00.000Z", "end":"2011-11-07T21:00:00.000Z"}"""
    val result = fromConferenceJson(jsonString)
    val Conference(_, title, begin, end, _, locations) = result
    title should be("TED-style XKE")
    begin should be(fmt.parseDateTime("2011-11-07T16:00:00.000Z"))
    end should be(fmt.parseDateTime("2011-11-07T21:00:00.000Z"))
  }
  it should "deserialize a conference with locations with json correctly" in {
    val jsonString = """{"title":"TED-style XKE","begin":"2011-11-07T16:00:00.000Z", "end":"2011-11-07T21:00:00.000Z", "locations" : [{"id" : -1706815601, "description" : "Maup", "capacity" : 20 }]}"""
    val result = fromConferenceJson(jsonString)
    val Conference(_, title, begin, end, _, locations) = result
    title should be("TED-style XKE")
    begin should be(fmt.parseDateTime("2011-11-07T16:00:00.000Z"))
    end should be(fmt.parseDateTime("2011-11-07T21:00:00.000Z"))
    locations.size should not be (0)
    locations.head should be(Location(-1706815601, "Maup", 20))
  }
  it should "serialize a location correctly" in {
    val expected: JValue = ("id" -> l1.id) ~
      ("description" -> "Maup") ~
      ("capacity" -> 20)
    l1.serializeToJson should be(expected)
  }

  it should "serialize a session without authors correctly" in {

    val s1 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "Mongo for world domination", "STRATEGIC", "10 people")

    val expected: JValue = ("id" -> s1.id) ~
      ("title" -> s1.title) ~
      ("description" -> s1.description) ~
      ("startTime" -> fmt.print(s1.start)) ~
      ("endTime" -> fmt.print(s1.end)) ~
      ("limit" -> s1.limit) ~
      ("type" -> s1.sessionType) ~
      ("authors" -> List[Author]()) ~
      ("location" -> l1.serializeToJson)
    val json: JValue = s1 //use implicits to convert to JSON
    json should be(expected)
  }
  it should "serialize a session with authors correctly" in {

    val s1 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "Mongo for world domination", "STRATEGIC", "10 people", List(a1))
    val expected: JValue = ("id" -> s1.id) ~
      ("title" -> s1.title) ~
      ("description" -> s1.description) ~
      ("startTime" -> fmt.print(s1.start)) ~
      ("endTime" -> fmt.print(s1.end)) ~
      ("authors" -> List(a1)) ~
      ("limit" -> s1.limit) ~
      ("type" -> s1.sessionType) ~
      ("location" -> l1.serializeToJson)

    val json: JValue = s1 //use implicits to convert to JSON

    json should be(expected)
  }
  it should "deserialize a session without authors correctly" in {

    val jsonString = """{    
    "title":"The power of Android",
    "description":"Android descr",
    "startTime":"2011-07-01T17:58:54.812Z",
    "endTime":"2011-07-01T21:58:54.812Z",
    "limit":"10 people",
    "type":"STRATEGIC",
        "location": {
           "description": "Library",
           "id": 714,
           "capacity": 10
        }
    }"""
    val result = fromSessionJson(true)(jsonString)
    val Session(id, start, end, location, title, descr, sessionType, limit, authors) = result //use pattern-matching to bind the values
    authors.isEmpty should be(true)
    title should be("The power of Android")
    descr should be("Android descr")
  }
  it should "deserialize a session with authors correctly" in {

    val jsonString = """{    
    "title":"The power of Android",
    "description":"Android descr",
    "startTime":"2011-07-01T17:58:54.812Z",
    "endTime":"2011-07-01T21:58:54.812Z",
    "limit":"10 people",
    "type":"STRATEGIC",
    "location": {
           "description": "Library",
           "id": 714,
           "capacity": 10
        }
    "authors" :[  {
	    "userId":"marnix",
	    "mail":"info@xebia.com",
	    "name":"Marnix van Wendel de Joode"
	  },
	  {
	    "userId":"gschoonheim",
	    "mail":"info@xebia.com",
	    "name":"Guido Schoonheim"
	  }
	]	
    }"""
    val result = fromSessionJson(true)(jsonString)
    val Session(id, start, end, location, title, descr, sessionType, limit, authors) = result //use pattern-matching to bind the values
    authors.size should be(2)
    title should be("The power of Android")
    descr should be("Android descr")

  }

}