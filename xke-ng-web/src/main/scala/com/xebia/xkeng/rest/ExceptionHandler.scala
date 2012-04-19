package com.xebia.xkeng.rest

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util.Props
import com.xebia.xkeng.serialization.util._
import net.liftweb.json.JsonDSL._
/**
 * Exception handler helper class that combines 1-n exception handlers
 */
trait ExceptionHandlerHelper extends PartialFunction[(Props.RunModes.Value, Req, Throwable), LiftResponse] with Logger {

  @volatile private var dispatch: List[PartialFunction[(Props.RunModes.Value, Req, Throwable), LiftResponse]] = Nil
  /**
   * Add request handlers
   */
  protected def handle(handler: PartialFunction[(Props.RunModes.Value, Req, Throwable), LiftResponse]): Unit = dispatch ::= handler

  /**
   * Apply the exception handler helper
   */
  override def apply(t: (Props.RunModes.Value, Req, Throwable)): LiftResponse =
    dispatch.find(_.isDefinedAt(t)).get.apply(t)

  /**
   * Is the exception  helper defined for a given request
   */
  override def isDefinedAt(t: (Props.RunModes.Value, Req, Throwable)) = dispatch.find(_.isDefinedAt(t)).isDefined

}
/**
 * Exceptionhandler for XKENG
 */
trait ExceptionHandler extends ExceptionHandlerHelper {

  handle {
    case (mode, req, e: IllegalArgumentException) => {
      error("illegal argument", e)
      badResponse(e.getMessage)

    }
    case ((mode: Props.RunModes.Value, req: Req, e: Exception)) => {
      error("an unexpected error occured", e)
      badResponse(e.getMessage)
    }

  }

  private def badResponse(reason: String) = ResponseWithReason(BadResponse(), "Invalid request. Reason: " + reason)
}