package com.xebia.rest.util
import dispatch._
import io.Source._
import scala.xml._
import net.liftweb.json.JsonAST.{ JValue, JArray }
import com.xebia.xkeng.serialization.util._



object RestClientTester extends RestClientHelper with App {
  val host = "localhost"
  val port = 8080
  val contextRoot = ""

  http x (:/(host, port).POST / contextRoot / "login" << """{"username":"u", "password":"pwd"}""" >- { printResp(_) })
  val (status, result) = query("conferences")(printResp)
  println(status + " " + result)

  //  http x (:/(host, port) / contextRoot / "conferences"  >- { printResp(_) })

  val req: Request = :/(host, port) / contextRoot / "conferences"
  //====================

  //handler is a function!!!
  val handler: Handler[Map[String, Set[String]]] = req >:> identity

  val r2 = http x (handler) {
    case (200, _, Some(entity), _) => {
      (200, Some(fromInputStream(entity.getContent()).getLines.mkString))
    }
  }
  println(r2)
  //======================
  val r = http x req >- (printResp)
  println(r)

  //  val f = (handler:Handler[String]) => handler ~> (s => s)
}

trait RestClientHelper {
  import io.Source._
  val host: String
  val port: Int
  def http = new Http
  val contextRoot: String
  val printResp = (resp: String) => println(resp)

  def query[T](target: String)(callback: String => T): (Int, Option[T]) = {
    val get = new Request(:/(host, port))
    val handler = get / contextRoot / target >:> identity
    http x (handler) {
      case (200, _, Some(entity), _) =>
        (200, Some(callback(fromInputStream(entity.getContent()).getLines.mkString)))
      case (status, _, _, _) => (status, None)
    }
  }

  //Low level http methods
  def add[T](target: String)(reqBody: String)(fromRespStr: String => T): T = {
    http(:/(host, port).POST / contextRoot / target << reqBody >- { fromRespStr })
  }

  
    //Low level http methods
  def add[T](target: String)(reqBody: JValue)(fromRespJson: JValue => T): T = {
    http(:/(host, port).POST / contextRoot / target << deserializeToStr(reqBody) >- { resStr => fromRespJson(serializeToJson(resStr)) })
  }

  
  //Low level http methods
  def add[T](target: String)(reqBody: Node)(fromRespXml: Node => T): T = {
    http(:/(host, port).POST / contextRoot / target << reqBody.toString <> { fromRespXml })
  }

  //Low level http methods
  def update[T](target: String, reqBody: String)(fromRespStr: String => T): T = {
    http(:/(host, port).PUT / contextRoot / target <<< reqBody >- { fromRespStr })
  }

  //Low level http methods
  def update[T](target: String, reqBody: Node)(fromRespXml: Node => T): T = {
    http(:/(host, port).PUT / contextRoot / target <<< reqBody.toString <> { fromRespXml })
  }

  def update[T](target: String, reqBody: String): Int = {
    val req = :/(host, port).PUT
    http x ((req / contextRoot / target <<< reqBody >:> identity) {
      case (status, _, _, _) => status
    })
  }

  def delete(target: String)  = {
    http x ((:/(host, port)).DELETE / contextRoot / target >:> identity) {
      case (status, _, _, _) => status
    }
  }

}

