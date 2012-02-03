package com.xebia.xkeng.dao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
import org.joda.time.DateTime
import com.xebia.xkeng.model._
import org.joda.time.format._
import com.xebia.xkeng.dao.RepositoryTestAssembly._

@RunWith(classOf[JUnitRunner])
class ConferenceRepositoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MongoTestConnection {

  val l1 = Location("Laap", 20)
  val l2 = Location("Maup", 30)
  val l3 = Location("New", 100)
  val fmt = ISODateTimeFormat.dateTime()
  val outputFmt = DateTimeFormat.forPattern("yyyyMMdd");
  val xke2011_06_03 = fmt.parseDateTime("2011-06-03T16:00:00.000Z")
  val xke2011_06_17 = fmt.parseDateTime("2011-06-17T16:00:00.000Z")
  val xke2011_05_03 = fmt.parseDateTime("2011-05-03T16:00:00.000Z")
  val xke2010_05_01 = fmt.parseDateTime("2010-05-01T16:00:00.000Z")
  val xkeFuture1 = new DateTime().plusWeeks(1);
  val xkeFuture2 = new DateTime().plusWeeks(3);
  var conferences: List[Conference] = Nil

  override def beforeEach() {
    init()
    Conference.drop
    val dates = xke2010_05_01 :: xke2011_05_03 :: xke2011_06_03 :: xke2011_06_17 :: xkeFuture1 :: xkeFuture2 :: Nil
    conferences = dates.map(createTestConference(_))
  }

  private def createTestConference(startDate: DateTime) = {
    val s1 = Session(startDate, startDate.plusMinutes(60), l1, "Mongo rocks", "Mongo rocks like a stone", "STRATEGIC", "10 people")
    val s2 = Session(startDate, startDate.plusMinutes(60), l2, "Scala rocks even more", "Scala is great and consice", "STRATEGIC", "20 people")
    val c = Conference("XKE", startDate, startDate.plusHours(4), List(s1, s2), List(l1, l2, l3))
    c.save
    c
  }

  it should "return slots that are grouped based on equal slot times and sorted by location within a slot" in {
    val slot1Start = new DateTime
    val slot1End = slot1Start.plusMinutes(60)
    val slot2End = slot1End.plusMinutes(60)
    val templateSession = Session(slot1Start, new DateTime, l1, "Title", "Desc", "STRATEGIC", "10 people")
    val s1A = templateSession.copy(end = slot1End, title = "S1A")
    val s1B = templateSession.copy(end = slot1End, title = "S1B", location = l2)
    val s1C = templateSession.copy(end = slot1Start.plusMinutes(120), title = "S1C", location = l3)
    val s2A = templateSession.copy(start = slot1End, end = slot2End, title = "S2A")
    val s2B = templateSession.copy(start = slot1End, end = slot2End, title = "S2B", location = l2)
    val c = Conference("XKE", slot1Start, slot2End, List(s2B, s2A, s1C, s1B, s1A), List(l1, l2, l3))
    val slots = c.slots
    slots.size should be(3)
    slots(0).sessions(0) should be(s1A)
    slots(0).sessions(1) should be(s1B)
    slots(1).sessions(0) should be(s1C)
    slots(2).sessions(0) should be(s2A)
    slots(2).sessions(1) should be(s2B)
    //println(c.getSlots.map(s => s.key + "\n   " + s.sessions.map(_.title + " " ).mkString).mkString("\n"))
  }

  it should "find conference by year" in {
    val confs = conferenceRepository.findConferences(xke2011_06_03.getYear)
    confs.isEmpty should not be true
    confs.size should equal(3)
  }

  it should "find next conference" in {
    var conf = conferenceRepository.findNextConference(1)
    conf should not be None
    val Some(c1) = conf
    c1.begin should be(xkeFuture1)
    conf = conferenceRepository.findNextConference(2)
    conf should not be None
    val Some(c2) = conf
    c2.begin should be(xkeFuture2)
    conf = conferenceRepository.findNextConference(3)
    conf should be(None)
  }

  it should "find conference by year and month" in {
    val confs = conferenceRepository.findConferences(xke2011_06_03.getYear, xke2011_06_03.getMonthOfYear)
    confs should not be (Nil)
    confs.size should be(2)
  }

  it should "find conference by year and month and day" in {
    val confs = conferenceRepository.findConferences(xke2011_05_03.getYear, xke2011_05_03.getMonthOfYear, xke2011_05_03.getDayOfMonth)
    confs should not be (Nil)
    confs.size should be(1)
    confs.head.sessions.size should be(2)
  }
  it should "find conference by id" in {
    val conf = conferenceRepository.findConference(conferences.head._id.toString)
    conf should not be (None)
  }
  it should "find session of a conference by id" in {
    val session = conferenceRepository.findSessionsOfConference(conferences.head._id.toString)
    session should not be (None)
  }
  type ? = this.type
}