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
import org.codehaus.jackson.annotate.JsonValue
import org.mortbay.util.ajax.JSON
import tools.nsc.doc.model.comment.Title

@RunWith(classOf[JUnitRunner])
class DomainConversionsTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
  val sess3: Session = Session("Clojure is Cool", "svdberg", "Clojure for world domination")
  val fmt = ISODateTimeFormat.dateTime()
  val xkeStartDate = fmt.parseDateTime("2011-06-27T09:57:47.945Z")
  override def beforeEach() {

  }

  it should "serialize a conference correctly" in {
    val s1 = Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "amooi@xebia.com", None)
    println(Printer.pretty(JsonAST.render(s1.serializeToJson)))
    val expected: JValue = ("start" -> "2011-06-27T09:57:47.945Z") ~
      ("end" -> "2011-06-27T10:57:47.945Z") ~
      ("location" -> l1.serializeToJson) ~
      ("title" -> "Mongo rocks") ~
      ("presenter" -> "amooi@xebia.com") ~
      ("id" -> s1.id)
    s1.serializeToJson should be(expected)
    val s2 = Slot(s1.serializeToJsonStr)
    s1 should be(s2)
    val t = ("standard" -> "true") ~ ("a" -> "b")
    println(Printer.pretty(JsonAST.render(t)))
    //TODO. This doesn't test conference serialization!!
  }

  it should "deserialize a conference with json correctly" in {
    val jsonString = """{"title":"TED-style XKE","begin":"2011-11-07T16:00:00.000Z", "end":"2011-11-07T21:00:00.000Z"}"""
    val result = fromConferenceJson(jsonString)
    val Conference(_, title, begin, end, _, _) = result
    title should be("TED-style XKE")
    begin should be(fmt.parseDateTime("2011-11-07T16:00:00.000Z"))
    end should be(fmt.parseDateTime("2011-11-07T21:00:00.000Z"))
  }
   it should "deserialize a conference with locations with json correctly" in {
    val jsonString = """{"title":"TED-style XKE","begin":"2011-11-07T16:00:00.000Z", "end":"2011-11-07T21:00:00.000Z", "locations" : [{"id" : -1706815601, "name" : "Maup", "capacity" : 20 }]}"""
    val result = fromConferenceJson(jsonString)
    val Conference(_, title, begin, end, _, locations) = result
    title should be("TED-style XKE")
    begin should be(fmt.parseDateTime("2011-11-07T16:00:00.000Z"))
    end should be(fmt.parseDateTime("2011-11-07T21:00:00.000Z"))
    locations.size should not be (0)
    locations.head should be (Location(-1706815601, "Maup", 20))           
  }
  it should "serialize a location correctly" in {
    val expected: JValue = ("id" -> l1.id) ~
      ("name" -> "Maup") ~
      ("capacity" -> 20)
    println(Printer.pretty(JsonAST.render(l1.serializeToJson)))
    println(Printer.pretty(JsonAST.render(expected)))
    l1.serializeToJson should be(expected)
  }
  it should "serialize a session correctly" in {

    val expected: JValue = ("id" -> sess3._id.toString) ~
      ("title" -> sess3.title) ~
      ("presenter" -> sess3.presenter) ~
      ("descr" -> sess3.descr)

    val json: JValue = sess3 //use implicits to convert to JSON
    println(Printer.pretty(JsonAST.render(json)))
    json should be(expected)

  }
  it should "deserialize a session correctly" in {
    val jsonString = """{"id":"4e49258f09d02ddc451de365","title":"Clojure is Cool","presenter":"svdberg","descr":"Clojure for world domination"}"""
    val result = fromSessionJson(jsonString)
    val Session(_,title, presenter, descr) = result //use pattern-matching to bind the values
    title should be("Clojure is Cool")
    presenter should be("svdberg")
    descr should be("Clojure for world domination")

  }
}