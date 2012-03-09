package com.xebia.xkeng.rest
import net.liftweb.http.rest.RestHelper
import net.liftweb.common._
import collection.mutable.{ ListBuffer => MList }
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.xebia.xkeng.dao.RepositoryComponent
import JsonDomainConverters._
import net.liftweb.http._
import org.joda.time._
import net.liftweb.util.BasicTypesHelpers._
import com.xebia.xkeng.model._
import com.atlassian.crowd.model.user.{ User => CrowdUser };
import RestUtils._

/**
 * Responsible for security related tasks such as authentication
 */
class XKENGPublicAPI extends RestHelper with Logger {
  this: RestHandlerComponent =>
  serve {
    // POST /login
    case req @ Req("login" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body, ForbiddenResponse("Invalid credentials")) {
        login(_)
      }

    // POST /error
    case req @ Req("error" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        handleError(_)
      }

  }

}
/**
 * Responsible for intercepting all secured requests
 * and check whether user is authorized to perform
 * requested action
 */
object XKENGSecurityInterceptor extends RestHelper {
  serve {
    case req if !UserHolder.isLoggedIn => {
      Full(ForbiddenResponse("Not authorized"))
    }
  }
  /**
   * Allows you to put a guard around a Dispatch Partial Function
   */
  def guard(other: LiftRules.DispatchPF): LiftRules.DispatchPF = this.orElse(other)
}