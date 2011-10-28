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

  def asJsonResp(json: JValue): Box[LiftResponse] = Full(JsonResponse(json))

  def doWithRequestBody(byteArray: Box[Array[Byte]])(process: (String) => Box[LiftResponse]): Box[LiftResponse] = {
    byteArray.toOption match {
      case Some(arr) => process(new String(arr))
      case None => Full(BadResponse())
    }
  }
}