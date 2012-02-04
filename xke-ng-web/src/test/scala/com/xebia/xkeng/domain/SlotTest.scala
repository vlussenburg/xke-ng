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
import org.joda.time.Minutes

@RunWith(classOf[JUnitRunner])
class SlotTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  val l1 = Location("Maup", 20)
  val s1 = Session(new DateTime, new DateTime, l1, "Mongo rocks", "Mongo rocks like a stone", "STRATEGIC", "10 people")

  override def beforeEach() {
  }

  it should "fit sessions in slots correctly" in {
    val slotTrom = new DateTime
    val slotTo = slotTrom.plusMinutes(60)
    val slot = SlotInfo(slotTrom, slotTo)

    val s1BeginBeforeSlot = slotTrom.minusMinutes(60)
    val s1EndAtBeginOfSlot = slotTrom

    slot.fits(s1.copy(start = s1BeginBeforeSlot, end = s1EndAtBeginOfSlot)) should be(false)

    val s1EndInSlot = slotTrom.plusMinutes(10)
    slot.fits(s1.copy(start = s1BeginBeforeSlot, end = s1EndInSlot)) should be(true)

    val s1EndAfterSlot = slotTo.plusMinutes(10)
    slot.fits(s1.copy(start = s1BeginBeforeSlot, end = s1EndAfterSlot)) should be(true)

    val s1BeginInSlot = slotTrom.plusMinutes(10)
    slot.fits(s1.copy(start = s1BeginInSlot, end = s1EndAfterSlot)) should be(true)

    val s1BeginAtEndOfSlot = slotTo
    slot.fits(s1.copy(start = s1BeginAtEndOfSlot, end = s1EndAfterSlot)) should be(false)

    val s1BeginAfterSlot = slotTo.plusMinutes(5)
    slot.fits(s1.copy(start = s1BeginAtEndOfSlot, end = s1EndAfterSlot)) should be(false)

  }

}