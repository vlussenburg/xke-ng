package com.xebia.xkeng.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.liftweb._
import mongodb._
import org.bson.types.ObjectId
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.joda.time.DateTime
import com.xebia.xkeng.model._


@RunWith(classOf[JUnitRunner])
class MongoConferenceTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
  val xkeStartDate = new DateTime().plusDays(3)

  override def beforeEach() {
    val srvr = new ServerAddress("127.0.0.1", 27017)
    val mo = new MongoOptions
    mo.socketTimeout = 10

    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), "xkeng")
  }

  private def createTestConference() = {
    val sess1 = Session(ObjectId.get, "Scala rocks even more", "upeter@xebia.com", "Scala is a scalable programming language")
    sess1.save
    val s1 = Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "amooi@xebia.com", None)
    val s2 = Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l2, "Scala rocks even more", "upeter@xebia.com", Some(sess1._id))
    val c = Conference(ObjectId.get, "XKE", xkeStartDate, xkeStartDate.plusHours(4), List(s1, s2), List(l1, l2))
    c.save
    (sess1, s1, s2, c)

  }

  it should "save a converence correctly" in {
    val (sess1, s1, s2, c) = createTestConference()

    var pFromDb = Conference.find(c._id)
    pFromDb should not be (None)
    pFromDb.get._id should be(c._id)

    val sRef = pFromDb.get.slots.filter(_.sessionRefId != None).head.sessionRefId.get
    val sFromDb = Session.find(sRef)

    sFromDb should not be (None)
    sFromDb.get._id should be(sRef)

    c.delete
    sess1.delete
  }

  it should "add a slot correctly" in {
    val (sess1, s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.slots.size should be(2)

    c.saveOrUpdate(Slot(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Git rocks", "amooi@xebia.com", None))

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.slots.size should be(3)
    c.slots(0).location should not be (None)

    c.delete
    sess1.delete
  }

  it should "remove a slot correctly" in {
    val (sess1, s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.remove(s1)

    cFromDb = Conference.find(c._id)
    cFromDb should not be (None)
    c = cFromDb.get
    c.slots.size should be(1)

    c.delete
    sess1.delete
  }


  it should "update a slot correctly" in {
    val (sess1, s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.slots.size should be(2)

    c.saveOrUpdate(s2.copy(title = "another title"))

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.slots.size should be(2)
    c.slots.exists(_.title == "another title") should be(true)
    c.slots(0).location should not be (None)

    c.delete
    sess1.delete
  }

  it should "update a location correctly" in {
    val (sess1, s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.locations.size should be(2)

    c.saveOrUpdate(l1.copy(capacity = 50))

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.locations.size should be(2)
    c.locations.exists(_.capacity == 50) should be(true)

    c.delete
    sess1.delete
  }


  it should "add a location correctly" in {
    val (sess1, s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.locations.size should be(2)

    c.saveOrUpdate(Location("Zolder", 5))

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.locations.size should be(3)

    c.delete
    sess1.delete
  }

  it should "remove a location correctly" in {
    val (sess1, s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.locations.size should be(2)

    intercept[IllegalArgumentException] {
      c.remove(l1)
    }
    val l3 = Location("Zolder", 5)
    c.saveOrUpdate(l3)

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.locations.size should be(3)
    c.remove(l3)

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.locations.size should be(2)

    c.delete
    sess1.delete
  }


}