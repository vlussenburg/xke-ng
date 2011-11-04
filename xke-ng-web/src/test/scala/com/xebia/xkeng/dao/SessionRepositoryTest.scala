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
class SessionRepositoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MongoTestConnection {

  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
  val l3 = Location("New", 100)
  val fmt = ISODateTimeFormat.dateTime()
  val outputFmt = DateTimeFormat.forPattern("yyyyMMdd");
  val startDate = fmt.parseDateTime("2011-06-03T16:00:00.000Z")
    val s1 = Session(startDate, startDate.plusMinutes(60), l1, "Mongo rocks", "Mongo rocks like a stone", "STRATEGIC", "10 people")
    val s2 = Session(startDate, startDate.plusMinutes(60), l2, "Scala rocks even more", "Scala is great and consice", "STRATEGIC", "20 people")

  
  var conferences: List[Conference] = Nil

  override def beforeEach() {
    init()
    Conference.drop
    val c = Conference("XKE", startDate, startDate.plusHours(4), List(s1, s2), List(l1, l2, l3))
    c.save
  }

  it should "rate a session" in {
    val rating = Rating(3, "peteru")
        val rating2 = Rating(6, "amooy")
    var ratings = sessionRepository.rateSessionById(s1.id, rating)
    ratings.isEmpty should not be true
    ratings.size should equal(1)
    ratings.head should equal(rating)
    ratings = sessionRepository.rateSessionById(s1.id, rating2)
    ratings.isEmpty should not be true
    ratings.size should equal(2)
    ratings.head should equal(rating2)    
  }
  it should "adjust rate of rating by the same user" in {
    val rating = Rating(3, "peteru")
    var ratings = sessionRepository.rateSessionById(s1.id, rating)
    ratings.isEmpty should not be true
    ratings.size should equal(1)
    ratings.head should equal(rating)
    val rating2 = Rating(5, "peteru")
    ratings = sessionRepository.rateSessionById(s1.id, rating2)
    ratings.isEmpty should not be true
    ratings.size should equal(1)
    ratings.head should equal(rating2)   
  }
  it should "comment a session" in {
    val comment = Comment("awesome", "peteru")
    val comments = sessionRepository.commentSessionById(s1.id, comment)
    comments.isEmpty should not be true
    comments.size should equal(1)
    comments.head should equal(comment)
  }
 type ? = this.type
}