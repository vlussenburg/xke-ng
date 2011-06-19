package com.xebia.xkeng.store

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.liftweb._
import json.ext.JodaTimeSerializers
import mongodb._
import org.bson.types.ObjectId
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.joda.time.DateTime

object Conference extends MongoDocumentMeta[Conference] {
  override def collectionName = "confs"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all
}

case class Conference(_id: ObjectId, name: String, date: DateTime, session: List[SessionInfo]) extends MongoDocument[Conference] {
  def meta = Conference

}
case class SessionInfo(title: String, presenter:String, slots: List[Slot], sessionRef:Option[ObjectId] = None)

case class Slot(start: DateTime, end: DateTime, location: Location)

case class Location(name: String, capacity: Int)

object Session extends MongoDocumentMeta[Session] {
  override def collectionName = "session"
  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all
}

//, rating:Option[List[Int]]
case class Session(_id: ObjectId, title: String, presenter:String, descr:String) extends MongoDocument[Session] {
  def meta = Session

}



@RunWith(classOf[JUnitRunner])
class MongoConferenceTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  override def beforeEach() {
    val srvr = new ServerAddress("127.0.0.1", 27017)
    val mo = new MongoOptions
    mo.socketTimeout = 10

    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), "xkeng")
  }

  it should "save a converence correctly" in {
    val sess1 = Session(ObjectId.get, "Scala rocks even more",  "upeter@xebia.com", "Scala is a scalable programming language"  )
    sess1.save

    val xkeStartDate = new DateTime().plusDays(3)
    val l1 = Location("Maup", 20)
    val l2 = Location("Laap", 30)
    val s1 = SessionInfo("Mongo rocks", "amooi@xebia.com", Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l1) :: Nil)
    val s2 = SessionInfo("Scala rocks even more", "upeter@xebia.com", Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l2) :: Nil, Some(sess1._id))

    val c = Conference(ObjectId.get, "XKE", xkeStartDate, s1 :: s2 :: Nil)

    c._id
    c.save
    val pFromDb = Conference.find(c._id)
    println(pFromDb)
    pFromDb should not be (None)
    pFromDb.get._id should be(c._id)

    val sRef = pFromDb.get.session.filter(_.sessionRef != None).head.sessionRef.get
    val sFromDb = Session.find(sRef)
    println(sFromDb)

    sFromDb should not be (None)
    sFromDb.get._id should be(sRef)
    c.delete
    sess1.delete
  }


}