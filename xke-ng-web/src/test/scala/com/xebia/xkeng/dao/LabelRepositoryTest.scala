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
class LabelRepositoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MongoTestConnection {

  val fmt = ISODateTimeFormat.dateTime()
  val startDate = fmt.parseDateTime("2011-06-03T16:00:00.000Z")

  val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
  val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")

  override def beforeEach() {
    init()

	createTestData();
  }

  private def createTestData() = {
    Labels.drop
    Conference.drop
    val l1 = Location("Maup", 20)
    val s1 = Session(startDate, startDate.plusMinutes(60), l1, "Mongo rocks", "Mongo rocks like a stone", "STRATEGIC", "10 people", List(a2), Nil, Nil, Set("Mongo"))
    val s2 = Session(startDate, startDate.plusMinutes(60), l1, "Scala rocks even more", "Scala is great and consice", "STRATEGIC", "20 people", List(a1), Nil, Nil, Set("Scala", "DSL"))
    val s3 = Session(startDate, startDate.plusMinutes(60), l1, "Javascript is on the move", "All you wanted to know about Javascript", "STRATEGIC", "20 people", List(a2), Nil, Nil, Set("Javascript", "MVC"))
    List(Conference("XKE", startDate, startDate.plusHours(4), List(s1, s2), List(l1)), Conference("XKE", startDate, startDate.plusHours(4), List(s3), List(l1))).foreach(_.save)
    List(s1, s2, s3).foreach(s => labelRepository.addLabels(s.labels.toSeq: _*))

  }

  it should "find labels" in {
    val labels = labelRepository.findAllLabels
    labels.size should be(5)
  }
  it should "add new label" in {
    labelRepository.addLabels("Clojosure")
    val labels = labelRepository.findAllLabels
    labels.size should be(6)
  }
  it should "not add new label if it already exists" in {
    labelRepository.addLabels("Scala")
    val labels = labelRepository.findAllLabels
    labels.size should be(5)
  }
  it should "find labels by author" in {
    val labelsa1 = labelRepository.findLabelsByAuthorId(a1.userId)
    labelsa1.size should be(2)
    val labelsa2 = labelRepository.findLabelsByAuthorId(a2.userId)
    labelsa2.size should be(3)
  }
  type ? = this.type
}