package com.xebia.xkeng.db

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.amazonaws.auth.BasicAWSCredentials
import collection.JavaConversions._
import com.amazonaws.services.simpledb.model._
import com.amazonaws.services.simpledb.{AmazonSimpleDB, AmazonSimpleDBClient}

@RunWith(classOf[JUnitRunner])
class StoreTest extends FlatSpec with ShouldMatchers {

  def getClient:AmazonSimpleDB = {
    val awsCredential = new BasicAWSCredentials("AKIAJVZ4QGT7BZLLBIOA", "3h7NXsAqSO7gElT0+I1a+06QlnPXcq7kAgTk4Fuh")
    new AmazonSimpleDBClient(awsCredential)
  }

  "SimpleDBStore" should " connect correctly" in {
    val client = getClient
    client.createDomain(new CreateDomainRequest("test"))
    client.putAttributes(new PutAttributesRequest("test",  "robin", List(new ReplaceableAttribute("role",  "author", true))))
    client.select(new SelectRequest("select * from `test` where role='author'")).getItems.foreach(println)
    client.deleteDomain(new DeleteDomainRequest("test"))
  }

  //no longer in the code, commented out so we can use it later?
  /*
  "SimpleDBStore" should " domain Session correctly" in {
    def map(sess:XKESession) = {
      sess.getClass.getDeclaredFields.foreach(a => {a.setAccessible(true);println(a.getName + "=" + a.get(sess) )})
    }
    map(XKESession("Scala takes over", "BladiBla", "evanderkoogh", "Maup", new DateTime(), new DateTime().plusHours(1), "1234"))
  }
  */




}