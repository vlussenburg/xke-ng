package com.xebia.xkeng.db

import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.liftweb.record.field.{DoubleField, IntField, StringField}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}


class ArticleDoc private() extends MongoRecord[ArticleDoc] with ObjectIdPk[ArticleDoc] {
  def meta = ArticleDoc

  object name extends StringField(this, 20)

  object price extends DoubleField(this)

}

object ArticleDoc extends ArticleDoc with MongoMetaRecord[ArticleDoc] {
  override def collectionName = "articles"
}


class MainDoc private() extends MongoRecord[MainDoc] with ObjectIdPk[MainDoc] {
  def meta = MainDoc

  object name extends StringField(this, 12)

  object cnt extends IntField(this)

}

object MainDoc extends MainDoc with MongoMetaRecord[MainDoc]

@RunWith(classOf[JUnitRunner])
class MongoRecordTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  val srvr = new ServerAddress("127.0.0.1", 27017)
  val mo = new MongoOptions
  mo.socketTimeout = 10

  MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), "xkeng")

   it should " domain correctly" in {
    val md1 = MainDoc.createRecord
      .name("md1")
      .cnt(5)
      .save
    println(md1)
  }


  it should " domain correctly" in {
    val s = MongoDB.useCollection("confs")(_.find().size())
    println("s ======== " + s)
    val md1 = MainDoc.createRecord
      .name("md1")
      .cnt(5)
      .save
    ArticleDoc.createRecord.name("milk").price(25).save
  }
}