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
class SecurityAPI extends RestHelper with Logger {
  this: RepositoryComponent =>
  serve {
    // POST /login
    case req @ Req("login" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        login(_)
      }
  }

  private def login(json: String): Box[LiftResponse] = {
    val credential = fromCredentialJson(json)
    authenticationRepository.authenticate(credential) match {
      case Some(user) => {
        val author = updateAuthors(user)
        UserHolder.addToSession(author)
        Full(OkResponse())
      }
      case None => Full(ForbiddenResponse("Invalid username or password"))
    }
  }

  private def updateAuthors(user: CrowdUser): Author = {
    authorRepository.findAuthorById(user.getName) match {
      case None => {
        val author = Author(user.getName(), user.getEmailAddress(), user.getDisplayName())
        authorRepository.addAuthor(author)
        info("User %s added to list of authors" format author)
        author

      }
      case Some(author) => author
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