package com.xebia.xkeng.security.util

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
import SecurityUtils._

@RunWith(classOf[JUnitRunner])
class SecurityUtilsTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  override def beforeEach() {

  }

  it should "decrypt and encrypt correctly" in {
    //println( decrypt("4722B942DC8997D3A62E349833936ECD"))
       println( encrypt("Pindakaas99"))
    val token = "this is the original token"
    val encyrpted = encrypt(token)
    val decrypted = decrypt(encyrpted)
    decrypted should be(token)

  }

}