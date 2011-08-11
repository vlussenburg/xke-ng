package com.xebia.xkeng.domain.rest

import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import net.liftweb.json.JsonAST.JValue
import com.xebia.xkeng.rest.{Conversion, XKESession}

@RunWith(classOf[JUnitRunner])
class ConversionTest extends FlatSpec with ShouldMatchers {
  "Conversion" should " serialize XKESession correclty" in {
    val s = XKESession("Scala takes over", "BladiBla", "evanderkoogh", "Maup", new DateTime(), new DateTime().plusHours(1), "1234")
    val json:JValue = Conversion.toJson(Some(s))
    println(json)
    //json should be(expected)
  }

   "Conversion" should " deserialize XKESession correclty" in {

     val expected = XKESession("Scala takes over", "BladiBla", "evanderkoogh", "Maup", new DateTime(), new DateTime().plusHours(1), "1234")
     val json = """{"title":"Scala takes over","desc":"BladiBla","presenter":"evanderkoogh","room":"Maup","from":"2011-05-02T13:17:45.446Z","to":"2011-05-02T14:17:45.571Z","id":"1234"}"""
     val xkeSession = Conversion.fromJson(json)
     println(xkeSession)
    //json should be(expected)
  }
}