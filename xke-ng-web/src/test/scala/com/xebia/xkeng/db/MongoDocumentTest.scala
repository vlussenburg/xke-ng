package com.xebia.xkeng.db

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.liftweb._
import json.ext.JodaTimeSerializers
import json.{NoTypeHints, Serialization, Formats}
import mongodb._
import org.bson.types.ObjectId
import java.util.Date
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.joda.time.DateTime

case class Address(street: String, city: String)

case class Child(name: String, age: Int, birthdate: Option[DateTime])

object Person extends MongoDocumentMeta[Person] {
  override def collectionName = "mypersons"
  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all
}

case class Person(_id: ObjectId, name: String, age: Int, address: Address, children: List[Child]) extends MongoDocument[Person] {
  def meta = Person

}


@RunWith(classOf[JUnitRunner])
class MongoDocumentTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
    override def beforeEach() {
      val srvr = new ServerAddress("127.0.0.1", 27017)
      val mo = new MongoOptions
      mo.socketTimeout = 10

      MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), "xkeng")
    }

    it should " connect correctly" in {
      def date(s: String) = Person.formats.dateFormat.parse(s).get
      val p = Person(
        ObjectId.get,
        "joe",
        27,
        Address("Bulevard", "Helsinki"),
        List(Child("Mary", 5, Some(new DateTime)), Child("Mazy", 3, None))
//          List(Child("Mary", 5, Some(date("2004-09-04T18:06:22.000Z"))), Child("Mazy", 3, None))
      )

      p._id
      p.save
      val pFromDb = Person.find(p._id)
      println(pFromDb)
      //p.delete
    }




  }