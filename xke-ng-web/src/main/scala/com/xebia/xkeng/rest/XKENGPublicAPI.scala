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
 * Interceptor that checks whether a user is logged in
 * before forwarding a request
 */
object SecurityInterceptor {
  val authenticationInterceptor: PartialFunction[Req, Unit] = {
    case _ if UserHolder.isLoggedIn =>
  }
}