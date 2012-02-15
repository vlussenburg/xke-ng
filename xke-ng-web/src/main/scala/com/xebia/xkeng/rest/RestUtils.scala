package com.xebia.xkeng.rest
import net.liftweb.common._
import collection.mutable.{ ListBuffer => MList }
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.http._
import org.joda.time._
import net.liftweb.util.BasicTypesHelpers._
object RestUtils {
  //=================================//
  //Utilities                        //
  //=================================//

  def asJsonResp(json: Option[JValue]): Box[LiftResponse] = json match {
    case Some(v) => asJsonResp(v)
    case _ => Full(NotFoundResponse())
  }

  def doWithSome[T](result:Option[T])(process:(T) => JValue):Box[LiftResponse] = result match {
     case Some(v) => asJsonResp(process(v))
    case _ => Full(NotFoundResponse())
  }
  
  def doWithSomeNoReturn[T](result:Option[T])(process:(T) => Unit):Box[LiftResponse] = result match {
     case Some(v) => process(v);Full(OkResponse())
    case _ => Full(NotFoundResponse())
  }
  
  def asJsonResp(json: JValue): Box[LiftResponse] = Full(JsonResponse(json))

  def doWithRequestBody(byteArray: Box[Array[Byte]])(process: (String) => Box[LiftResponse]): Box[LiftResponse] = {
    doWithRequestBody(byteArray, BadResponse())(process)
  }
  
   def doWithRequestBody(byteArray: Box[Array[Byte]], alternativeResp:LiftResponse)(process: (String) => Box[LiftResponse]): Box[LiftResponse] = {
    byteArray.toOption match {
      case Some(arr) => process(new String(arr))
      case None => Full(alternativeResp)
    }
  }
}