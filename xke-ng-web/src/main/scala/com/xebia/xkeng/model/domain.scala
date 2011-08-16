package com.xebia.xkeng.model

import java.util.concurrent.atomic.AtomicLong
import net.liftweb.json.ext.JodaTimeSerializers
import org.bson.types.ObjectId
import org.joda.time._
import net.liftweb.json._
import scala.Predef._
import scala.Long
import net.liftweb.mongodb.{MongoDocument, ObjectIdSerializer, MongoDocumentMeta}
import net.liftweb.json.JsonDSL._
import java.lang.IllegalArgumentException

package object domain {
  val counter = new AtomicLong((System.currentTimeMillis() % 11).abs)

  def nextSeq = System.currentTimeMillis() + counter.getAndIncrement
}


import domain._


/**
 * Traits for serialisation and deserialisation to/from JSON
 */
trait FromJsonDeserializer[T] {
  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all
  def apply(json: String)(implicit m: Manifest[T]): T = {
    Serialization.read[T](json)
  }

}

trait ToJsonSerializer[T] {
  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all
  def serializeToJson: JValue = {
    parse(serializeToJsonStr)
  }
  def serializeToJsonStr: String = {
    Serialization.write(this)
  }
}


trait EmbeddedDocumentOps[T] {
  self: MongoDocumentMeta[T] =>

  /**
   * Mongo qry:
   *  db.confs.update({"_id":ObjectId("<id>")},{$push:{"<arrayname>":<json>}})
   */
  def pushToArray(_id: ObjectId, arrayName: String, newobj: JValue) = {
    self.update(("_id" -> _id.toString), ("$push" -> (arrayName -> newobj)))
  }

  /**
   * Mongo qry:
   *  db.confs.update({"_id":ObjectId("<id>"), "<arrayname>":  { $elemMatch : { "<field>" : "<valueToMatch>" }}},{$set:{"<arrayname>.$":<json>}})
   */
  def updateInArray(_id: ObjectId, arrayName: String, elemInArrayQry: JValue, adjustedobj: JValue) = {
    self.update(("_id" -> _id.toString) ~ (arrayName -> ("$elemMatch" -> elemInArrayQry)), ("$set" -> (arrayName + ".$" -> adjustedobj)))
  }

  /**
   * Mongo qry:
   * db.confs.update({"_id" : ObjectId("<id>")},	{$pull : {<arrayname> : {"<field>" : "<valueToMatch>"}}})
   */
  def removeFromArray(_id: ObjectId, arrayName: String, elemInArrayQry: JValue) = {
    self.update(("_id" -> _id.toString), ("$pull" -> (arrayName -> elemInArrayQry)))
  }

}

/**
 * Defines the structure of a Conference.
 */
object Conference extends MongoDocumentMeta[Conference] with EmbeddedDocumentOps[Conference]  {
  override def collectionName = "confs"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all

  def apply(title: String, begin: DateTime, end: DateTime, slots: List[Slot], locations: List[Location]): Conference = {
    Conference(ObjectId.get, title, begin, end, slots, locations)
  }
  
}

/**
 * Represents a Conference, a conference has a number of locations, where slots are available.
 * This class is a case class, due to net.liftweb.mongodb requirements.
 */
case class Conference(_id: ObjectId, title: String, begin: DateTime, end: DateTime, var slots: List[Slot], var locations: List[Location]) extends MongoDocument[Conference] {
  def meta = Conference

  type EmbeddedElem = {def id: Long; def serializeToJson: JValue}
//  slots = slots.map(s => {
//    s.location = Some(locations.find(_.id == s.locationRefId).get); s
//  })
//
  def saveOrUpdate(slot: Slot) = {
    doSaveOrUpdate("slots", slots, slot)
    slots = slot :: (slots - slot)
    slots
  }

  def remove(slot: Slot) = {
    doRemove("slots", slot)
    slots = slots.filter(_.id != slot.id)
    slots
  }

  def saveOrUpdate(location: Location) = { 
    doSaveOrUpdate("locations", locations, location)
    locations = location :: (locations - location)
    locations
  }
 
  def remove(location: Location) = {
    if(slots.exists(_.location.id == location.id)) {
      throw new IllegalArgumentException("The following slots: %s still depend on the location: %s you want to remove. A location can only be removed if it has no references to slots." format(slots, location))
    }
    doRemove("locations", location)
    locations = locations.filter(_.id != location.id)
    locations
  }

  private def doSaveOrUpdate(nameMongoArray: String, mongoArray: List[EmbeddedElem], elem: EmbeddedElem) = {
    if (!mongoArray.exists(_.id == elem.id)) {
      meta.pushToArray(_id, nameMongoArray, elem.serializeToJson)
    } else {
      meta.updateInArray(_id, nameMongoArray, ("id" -> elem.id), elem.serializeToJson)
    }
  }

  private def doRemove(nameMongoArray: String, elem: EmbeddedElem) = {
    meta.removeFromArray(_id, nameMongoArray, ("id" -> elem.id))
  }

}

/**
 * Represents a slot at a location. A slot is an indication of time and space.
 */
case class Slot(val id: Long, val start: DateTime, val end: DateTime, val location:Location, val title: String, val presenter: String, val sessionRefId: Option[ObjectId]) extends ToJsonSerializer[Slot] {
  def period = new Period(start.getMillis, end.getMillis)
}

/**
 * defines the structure of a slot
 */
object Slot extends FromJsonDeserializer[Slot] {

 def apply(start: DateTime, end: DateTime, location: Location, title: String, presenter: String, sessionRefId: Option[ObjectId]) = {
    new Slot(nextSeq, start, end, location, title, presenter, sessionRefId)
  }
}

/**
 * Represents credentials used for authentication
 */
case class Credential(val user: String, val cryptedPassword: String) extends ToJsonSerializer[Credential] {
}

/**
 * Represents a location, a physical space.
 */
case class Location(id: Long, name: String, capacity: Int) extends ToJsonSerializer[Location]

object Location extends FromJsonDeserializer[Location]{
  def apply(name: String, capacity: Int): Location = {
    Location(nextSeq.toInt, name, capacity)
  }
}

/**
 * Represents a Session, a talk/workshop/demo about a topic.
 */
case class Session(_id: ObjectId, title: String, presenter: String, descr: String) extends MongoDocument[Session] {
  def meta = Session

}
object Session extends MongoDocumentMeta[Session] {
  override def collectionName = "session"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all

  def apply(title: String, presenter: String, descr: String): Session = {
    Session(ObjectId.get, title, presenter, descr)
  }

}