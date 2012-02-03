
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
//import org.codehaus.jackson.annotate.JsonValue
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
    //val jsonString = """{"title":"TED-style XKE","begin":"2011-11-07T16:00:00.000Z", "end":"2011-11-07T21:00:00.000Z", "locations" : [{"id" : -1706815601, "description" : "Maup", "capacity" : 20 }]}"""
    val jsonString = """{"title":"XKE", "begin":"2011-10-12T18:32:00.354+02:00","end":"2011-10-12T22:32:00.354+02:00","sessions":[{"id":1318177920605,"title":"Mongo rocks","description":"Mongo is a nosql db","startTime":"2011-10-12T18:32:00.354+02:00","endTime":"2011-10-12T19:32:00.354+02:00","limit":"10 people","type":"STRATEGIC","authors":[],"location":{"id":-377039271,"description":"Maup","capacity":20}},{"id":1318177920606,"title":"Scala rocks even more","description":"Scala is a scalable programming language","startTime":"2011-10-12T18:32:00.354+02:00","endTime":"2011-10-12T19:32:00.354+02:00","limit":"20 people","type":"STRATEGIC","authors":[{"userId":"peteru","mail":"upeter@xebia.com","name":"Urs Peter"},{"userId":"amooy","mail":"amooy@xebia.com","name":"Age Mooy"}],"location":{"id":-377039270,"description":"Laap","capacity":30}}],"locations":[{"id":-377039271,"description":"Maup","capacity":20},{"id":-377039270,"description":"Laap","capacity":30}]}"""

    val result = fromConferenceJson(jsonString)
    val Conference(_, title, begin, end, sessions, locations) = result
    title should be("XKE")
    begin should be(fmt.parseDateTime("2011-10-12T18:32:00.354+02:00"))
    end should be(fmt.parseDateTime("2011-10-12T22:32:00.354+02:00"))
    locations.size should be(2)
    sessions.size should be(2)
    locations.head should be(Location(-377039271, "Maup", 20))
  }
  it should "serialize a location correctly" in {
    val expected: JValue = ("id" -> l1.id) ~
      ("description" -> "Maup") ~
      ("capacity" -> 20)
    l1.serializeToJson should be(expected)
  }

  it should "serialize a slot correctly" in {
    val from = fmt.parseDateTime("2011-11-07T16:00:00.000Z")
    val to = fmt.parseDateTime("2011-11-07T17:00:00.000Z")
    val expected: JValue = ("from" -> from.toString("HH:mm")) ~
      ("to" -> to.toString("HH:mm")) ~
      ("sessions" -> List[Session]())
    val slot = Slot(from, to, Nil)
    slotsToJValue(slot) should be(expected)
  }
  it should "serialize slots correctly" in {
    val s1 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "Mongo for world domination", "STRATEGIC", "10 people", List(a1), Nil, Nil, Set())
    val c = Conference("XKE", xkeStartDate, xkeStartDate.plusHours(4), List(s1), Nil)
    
    val json = conferenceSlotsToJValue(c)
    val JArray(slots) = json \ "slots"
    val JString(from) = json \ "slots" \ "from"
    from should be(xkeStartDate.toString("HH:mm"))
  }

  it should "serialize a session with authors, comments and ratings" in {
    val r1 = Rating(10, "peteru")
    val c1 = Comment("blabla", "peteru")
    val lb1 = "Scala"
    val s1 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "Mongo for world domination", "STRATEGIC", "10 people", List(a1), List(r1), List(c1), Set(lb1))

    val expected: JValue = ("id" -> s1.id) ~
      ("title" -> s1.title) ~
      ("description" -> s1.description) ~
      ("startTime" -> fmt.print(s1.start)) ~
      ("endTime" -> fmt.print(s1.end)) ~
      ("limit" -> s1.limit) ~
      ("type" -> s1.sessionType) ~
      ("authors" -> List(a1)) ~
      ("comments" -> List(c1)) ~
      ("ratings" -> ratingsFullToJArray(List(r1))) ~
      ("labels" -> serializeStringsToJArray(lb1)) ~
      ("location" -> l1.serializeToJson)
    val json: JValue = s1 //use implicits to convert to JSON
    json should be(expected)
  }
  it should "serialize a session without authors, comments, labels and ratings" in {

    val s1 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "Mongo for world domination", "STRATEGIC", "10 people")
    val expected: JValue = ("id" -> s1.id) ~
      ("title" -> s1.title) ~
      ("description" -> s1.description) ~
      ("startTime" -> fmt.print(s1.start)) ~
      ("endTime" -> fmt.print(s1.end)) ~
      ("authors" -> List[Author]()) ~
      ("limit" -> s1.limit) ~
      ("type" -> s1.sessionType) ~
      ("comments" -> List[Comment]()) ~
      ("ratings" -> List[Rating]()) ~
      ("labels" -> List[String]()) ~
      ("location" -> l1.serializeToJson)

    val json: JValue = s1 //use implicits to convert to JSON
    //   println(deserializeToStr(expected))

    json should be(expected)
  }
  it should "deserialize a session without authors, comments, labels and ratings correctly" in {

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
    val Session(id, start, end, location, title, descr, sessionType, limit, authors, _, _, _) = result //use pattern-matching to bind the values
    authors.isEmpty should be(true)
    title should be("The power of Android")
    descr should be("Android descr")
  }
  it should "deserialize a session with authors, comments, ratings and labels correctly" in {

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
    },
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
	],
	"comments":[  {
    	"user":"guest",
    	"comment":"bla"
      }
    ],
	"labels":["Scala", "DSL" ], 
    "ratings" :[ { 
    	"user": "guest",
    	"rate": 1
    	},
    	{
    	"user" : "upeter", 
    	"rate" : 2
    	}
    ] }"""
    val result = fromSessionJson(true)(jsonString)
    val Session(id, start, end, location, title, descr, sessionType, limit, authors, ratings, comments, labels) = result //use pattern-matching to bind the values
    authors.size should be(2)
    ratings.size should be(2)
    comments.size should be(1)
    labels.size should be(2)
    title should be("The power of Android")
    descr should be("Android descr")

  }

  it should "deserialize a rating correctly" in {
    val jsonString = """{"rate":3}"""
    val Rating(rate, userId) = fromRatingJson(jsonString)
    rate should be(3)
    //TODO needs to be changed when login is implemented
    userId should be("guest")
  }

  it should "deserialize a list of Strings correctly" in {

    val jsonString = """["first", "second"]"""
    val l = deserializeStringList(serializeToJson(jsonString))
    l should be(List("first", "second"))

  }
  it should "deserialize a list of ints correctly" in {

    val jsonString = """[1, 2]"""
    val l = deserializeIntList(serializeToJson(jsonString))
    l should be(List(1, 2))

  }
  it should "deserialize a comment correctly" in {
    val jsonString = """{"comment":"This is awesome"}"""
    val Comment(comment, userId) = fromCommentJson(jsonString)
    comment should be("This is awesome")
    //TODO needs to be changed when login is implemented
    userId should be("guest")
  }
  it should "serialize ratings correctly" in {

    val r1 = Rating(1, "guest") :: Rating(2, "guest") :: Nil
    val expected: JValue = JArray(List(1, 2))
    val json: JValue = r1 //use implicits to convert to JSON

    json should be(expected)
  }
  it should "serialize comments correctly" in {

    val c = Comment("guest", "Be my guest")
    val expected: JValue = ("user" -> c.userId) ~ ("comment" -> c.comment)
    val json: JValue = c //use implicits to convert to JSON

    json should be(expected)
  }
  it should "deserialize credential correctly" in {

    val unencryptedCredJson = """{"username":"upeter", "password":"secret"}"""
    val Credential(username, unencryptedPwd, notencrypted) = fromCredentialJson(unencryptedCredJson)
    username should be("upeter")
    unencryptedPwd should be("secret")
    notencrypted should be(false)

    val encryptedCredJson = """{"username":"upeter", "encryptedPassword":"87ujy79hjy"}"""
    val Credential(_, encryptedPwd, encrypted) = fromCredentialJson(encryptedCredJson)
    encryptedPwd should be("87ujy79hjy")
    encrypted should be(true)
  }

}