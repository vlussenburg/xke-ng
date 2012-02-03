package bootstrap.liftweb

import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import com.xebia.xkeng.rest.Assembly._
import com.xebia.xkeng.rest.SecurityInterceptor._
import net.liftweb.util.Props
import net.liftweb.mongodb.{ DefaultMongoIdentifier, MongoDB }
import com.mongodb.{ Mongo, MongoOptions, ServerAddress }
import net.liftweb.util.Helpers._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    // where to search snippet
    LiftRules.addToPackages("com.xebia.xkeng")
    LiftRules.enableLiftGC = false;
    init()
    val purge = Props.get("mongo.purge.data").map(_.trim.toBoolean).getOrElse(false)
    val enableSecurity = Props.get("enable.secruity").map(_.trim.toBoolean).getOrElse(true)

    purgeAndPushTestdata(purge)
    LiftRules.exceptionHandler.prepend(new ExceptionHandlerAssembly)
    if (enableSecurity) {
      LiftRules.dispatch.append(new XKENGPublicAPIAssembly)
    } else {
      warn("Starting application in dev mode without security")
      LiftRules.dispatch.append(new XKENGPublicAPIAssembly with DummySecurityRepositoryComponentImpl)
    }
    LiftRules.dispatch.append(authenticationInterceptor guard XKENGSecuredAPIAssembly)
    LiftRules.early.append(makeUtf8)
    //exclude htmls to be processed by lift
    LiftRules.liftRequest.append {
      case Req(_, "html", GetRequest) => false
      case Req("static" :: Nil, "js", GetRequest) => false
      case Req("static" :: Nil, "css", GetRequest) => false
    } 
    

  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

}
