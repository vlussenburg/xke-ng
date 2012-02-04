package com.xebia.xkeng.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.{ Mongo, MongoOptions, ServerAddress }
import net.liftweb._
import mongodb._
import org.bson.types.ObjectId
import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
import org.joda.time.DateTime
import com.xebia.xkeng.model._
import com.xebia.xkeng.dao.MongoTestConnection

@RunWith(classOf[JUnitRunner])
class MongoConferenceTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MongoTestConnection {

  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
  val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
  val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")
  val xkeStartDate = new DateTime().plusDays(3)

  override def beforeEach() {
    init()
    Conference.drop
  }

  private def createTestConference() = {
    val s1 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Mongo rocks", "Mongo is a nosql db", "STRATEGIC", "10 people")
    val s2 = Session(xkeStartDate, xkeStartDate.plusMinutes(60), l2, "Scala rocks even more", "Scala is a scalable programming language", "STRATEGIC", "20 people", List(a1, a2))
    val c = Conference("XKE", xkeStartDate, xkeStartDate.plusHours(4), List(s1, s2), List(l1, l2))
    c.save
    (s1, s2, c)

  }

  it should "save a converence correctly" in {
    val (s1, s2, c) = createTestConference()

    var pFromDb = Conference.find(c._id)
    pFromDb should not be (None)
    pFromDb.get._id should be(c._id)

    c.delete

  }

  it should "add a session correctly" in {
    val (s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.sessions.size should be(2)

    c.saveOrUpdate(Session(xkeStartDate, xkeStartDate.plusMinutes(60), l1, "Git rocks", "Git is the star among cvs", "STRATEGIC", "20 people"))

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.sessions.size should be(3)
    c.sessions(0).location should not be (None)

    c.delete

  }

  it should "remove a session correctly" in {
    val (s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.remove(s1)

    cFromDb = Conference.find(c._id)
    cFromDb should not be (None)
    c = cFromDb.get
    c.sessions.size should be(1)

    c.delete

  }

  it should "update a session correctly" in {
    val (s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.sessions.size should be(2)

    c.saveOrUpdate(s2.copy(title = "another title"))

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.sessions.size should be(2)
    c.sessions.exists(_.title == "another title") should be(true)
    c.sessions(0).location should not be (None)

    c.delete

  }

  it should "update a location correctly" in {
    val (s1, s2, cOriginal) = createTestConference()

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

  }

  it should "add a location correctly" in {
    val (s1, s2, cOriginal) = createTestConference()

    var cFromDb = Conference.find(cOriginal._id)
    cFromDb should not be (None)
    var c = cFromDb.get
    c.locations.size should be(2)

    c.saveOrUpdate(Location("Zolder", 5))

    cFromDb = Conference.find(c._id)
    c = cFromDb.get
    c.locations.size should be(3)

    c.delete

  }

  it should "remove a location correctly" in {
    val (s1, s2, cOriginal) = createTestConference()

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

  }

}