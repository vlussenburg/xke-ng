package com.xebia.xkeng.db

import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.liftweb.record.field.{IntField, StringField, DoubleField}

import net.liftweb._
import common.Full
import mongodb.record.field._
import mongodb.record._
import mongodb.record.field.ObjectIdPk
import mongodb.{DefaultMongoIdentifier, MongoDB}
import org.bson.types.ObjectId
import java.util.Date
import org.scalatest.{BeforeAndAfterEach, FlatSpec}


class LocationDoc private() extends BsonRecord[LocationDoc] {
  def meta = LocationDoc

  object name extends StringField(this, 100)

  object persons extends IntField(this)

}

object LocationDoc extends LocationDoc with BsonMetaRecord[LocationDoc]


class SlotDoc private() extends MongoRecord[SlotDoc] with ObjectIdPk[SlotDoc] {
  def meta = SlotDoc

  object from extends StringField(this, 5)

  object duration extends IntField(this)

  object location extends BsonRecordField(this, LocationDoc)

}

object SlotDoc extends SlotDoc with MongoMetaRecord[SlotDoc] {
  override def collectionName = "slots"
}


@RunWith(classOf[JUnitRunner])
class MongoDBTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var md1 = SlotDoc.createRecord.from("16:00").duration(60)

  override def beforeEach() {
    val srvr = new ServerAddress("127.0.0.1", 27017)
    val mo = new MongoOptions
    mo.socketTimeout = 10
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), "xkeng")
  }

  override def afterEach() {
    SlotDoc.delete("_id", md1.id.is)
  }

  it should " embed correctly" in {
    val loc1 = LocationDoc.createRecord.name("Maup").persons(20)
    md1 = SlotDoc.createRecord.from("16:00").duration(60).location(loc1).save
    val md = SlotDoc.find("_id", md1.id.is)
    md.get.location.get.name.is should be("Maup")
  }

}