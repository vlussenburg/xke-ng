package com.xebia.model

import java.util.concurrent.atomic.AtomicLong
import net.liftweb.json.ext.JodaTimeSerializers
import org.bson.types.ObjectId
import org.joda.time.DateTime
import net.liftweb.mongodb.{MongoDocument, ObjectIdSerializer, MongoDocumentMeta}
import java.lang.Long


package object domain {
  val counter = new AtomicLong()

  def nextSeq = System.currentTimeMillis() + counter.getAndIncrement
}
import domain._
object Conference extends MongoDocumentMeta[Conference] {
  override def collectionName = "confs"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all
}

case class Conference(_id: ObjectId, name: String, date: DateTime, var slots: List[Slot], var locations:List[Location]) extends MongoDocument[Conference] {
  def meta = Conference

  slots = slots.map(s => {s.location = Some(locations.find(_.id == s.locId).get);s})

  def +(slot: Slot) = {
    slots = slot :: (slots - slot)
    slots
  }

  def -(slot: Slot) = {
    slots = slots.filter(_.id != slot.id)
    slots
  }


}

case class Slot(start: DateTime, end: DateTime, locRef: Long, title: String, presenter: String, sessionRef: Option[ObjectId], id: Long) {
  var location:Option[Location] = None

  def this(start: DateTime, end: DateTime, loc: Location, title: String, presenter: String, sessionRef: Option[ObjectId]) {
    this(start, end, loc.id, title, presenter, sessionRef, nextSeq)
    location = Some(loc)
  }
}

object Slot {
  def apply(start: DateTime, end: DateTime, loc: Location, title: String, presenter: String, sessionRef: Option[ObjectId]) = {
    new Slot(start, end, loc, title, presenter, sessionRef)
  }
}

case class Location(name: String, capacity: Int, id: Long = nextSeq)

object Session extends MongoDocumentMeta[Session] {
  override def collectionName = "session"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all
}

case class Session(_id: ObjectId, title: String, presenter: String, descr: String) extends MongoDocument[Session] {
  def meta = Session

}