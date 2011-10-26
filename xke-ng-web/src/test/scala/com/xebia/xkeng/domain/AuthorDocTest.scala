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

@RunWith(classOf[JUnitRunner])
class AuthorDocTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MongoTestConnection {

 
  val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
  val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")


  override def beforeEach() {
    init()
    AuthorDoc.drop
  }

  private def createTestAuthors() = {
    val ad1 = AuthorDoc(a1)
    val ad2 = AuthorDoc(a2)
    ad1.save
    ad2.save
    (ad1, ad2)
  }

  it should "save an author correctly" in {
    val (ad1, ad2) = createTestAuthors()
    var dbAdoc = AuthorDoc.find(ad1._id)
    dbAdoc should not be (None)
    dbAdoc.get._id should be(ad1._id)
    dbAdoc.get.author should be(a1)
    ad1.delete

  }

}