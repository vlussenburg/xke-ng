package com.xebia.xkeng.dao

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
import org.joda.time.format._
import com.xebia.xkeng.dao.RepositoryTestAssembly._

@RunWith(classOf[JUnitRunner])
class ConferenceRepositoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
  val fmt = ISODateTimeFormat.dateTime()
  val outputFmt = DateTimeFormat.forPattern("yyyyMMdd");
  val xke2011_06_03 = fmt.parseDateTime("2011-06-03T16:00:00.000Z")
  val xke2011_06_17 = fmt.parseDateTime("2011-06-17T16:00:00.000Z")
  val xke2011_05_03 = fmt.parseDateTime("2011-05-03T16:00:00.000Z")
  val xke2010_05_01 = fmt.parseDateTime("2010-05-01T16:00:00.000Z")
  var conferences:List[Conference] = Nil


  override def beforeEach() {
    init()
    val dates = xke2010_05_01 :: xke2011_05_03 :: xke2011_06_03 :: xke2011_06_17 :: Nil
    conferences = dates.map(createTestConference(_))
  }

  override def afterEach() {
    //conferences.foreach(_.delete)
  }

  private def createTestConference(startDate:DateTime) = {
    val s1 = Slot(startDate, startDate.plusMinutes(60), l1, "Mongo rocks", "amooi@xebia.com", None)
    val s2 = Slot(startDate, startDate.plusMinutes(60), l2, "Scala rocks even more", "upeter@xebia.com", None)
    val c = Conference(ObjectId.get, "XKE", startDate, startDate.plusHours(4), List(s1, s2), List(l1, l2))
    c.save
    c
  }

  it should "find conference by year" in {
    var confs = conferenceRepository.findConferences(xke2011_06_03.getYear)
    confs should not be (Nil)
    confs.size should be(3)

    confs = conferenceRepository.findConferences(xke2011_06_03.getYear, xke2011_06_03.getMonthOfYear)
    confs should not be (Nil)
    confs.size should be(2)

    confs = conferenceRepository.findConferences(xke2011_05_03.getYear, xke2011_05_03.getMonthOfYear)
    confs should not be (Nil)
    confs.size should be(1)

    var conf = conferenceRepository.findConference(xke2011_05_03.getYear, xke2011_05_03.getMonthOfYear, xke2011_05_03.getDayOfMonth)
    conf should not be (None)
    
    conf = conferenceRepository.findConferenceOn(outputFmt.print(xke2011_05_03))
    conf should not be (None)

    conf = conferenceRepository.findConference(conferences.head._id.toString)
    conf should not be (None)
  }

}