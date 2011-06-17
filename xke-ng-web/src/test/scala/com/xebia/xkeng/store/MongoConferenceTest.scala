package com.xebia.xkeng.store

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.liftweb._
import json.ext.JodaTimeSerializers
import json.{NoTypeHints, Serialization, Formats}
import mongodb._
import org.bson.types.ObjectId
import java.util.Date
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.joda.time.DateTime

object Conference extends MongoDocumentMeta[Conference] {
  override def collectionName = "confs"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all
}

case class Conference(_id: ObjectId, name: String, date: DateTime, session: List[Session]) extends MongoDocument[Conference] {
  def meta = Conference

}

case class Session(title: String, slots: List[Slot])

case class Slot(start: DateTime, end: DateTime, location: Location)

case class Location(name: String, capacity: Int)


@RunWith(classOf[JUnitRunner])
class MongoConferenceTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  override def beforeEach() {
    val srvr = new ServerAddress("127.0.0.1", 27017)
    val mo = new MongoOptions
    mo.socketTimeout = 10

    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), "xkeng")
  }

  it should "save a converence correctly" in {
    val xkeStartDate = new DateTime().plusDays(3)
    val l1 = Location("Maup", 20)
    val l2 = Location("Laap", 30)
    val s1 = Session("Mongo rocks", Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l1) :: Nil)
    val s2 = Session("Scala rocks even more", Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l2) :: Nil)

    val c = Conference(ObjectId.get, "XKE", xkeStartDate, s1 :: s2 :: Nil)

    c._id
    c.save
    val pFromDb = Conference.find(c._id)
    println(pFromDb)
    pFromDb should not be (None)
    pFromDb.get._id should be(c._id)
    c.delete
  }


}