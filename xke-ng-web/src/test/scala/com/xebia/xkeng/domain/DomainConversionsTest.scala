package com.xebia.xkeng.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.liftweb._
import json.JsonAST.{JInt, JValue}
import json.{JsonAST, Printer}
import mongodb._
import org.bson.types.ObjectId
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.joda.time.DateTime
import com.xebia.xkeng.model._
import net.liftweb.json.JsonDSL._
import org.joda.time.format.ISODateTimeFormat

@RunWith(classOf[JUnitRunner])
class DomainConversionsTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
   val fmt = ISODateTimeFormat.dateTime()
  val xkeStartDate = fmt.parseDateTime("2011-06-27T09:57:47.945Z")

  override def beforeEach() {

  }


  it should "save a converence correctly" in {
    val s1 = Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "amooi@xebia.com", None)
    println(Printer.pretty(JsonAST.render(s1.serializeToJson)))
    val expected:JValue = ("start" -> "2011-06-27T09:57:47.945Z") ~
      ("end" -> "2011-06-27T10:57:47.945Z") ~
      ("location" -> l1.serializeToJson) ~
      ("title" -> "Mongo rocks") ~
      ("presenter" -> "amooi@xebia.com") ~
      ("id" -> s1.id)
    s1.serializeToJson should be(expected)
    val s2 = Slot(s1.serializeToJsonStr)
    s1 should be(s2)
    val t = ("standard" -> "true") ~ ("a" -> "b")
    println(t.getClass)
    println(Printer.pretty(JsonAST.render(t)))

  }




}