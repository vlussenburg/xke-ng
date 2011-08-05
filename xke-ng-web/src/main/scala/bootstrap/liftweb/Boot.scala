package bootstrap.liftweb

import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import com.xebia.xkeng.rest.{Assembly, XKENGDispatchAPI}
import net.liftweb.util.Props
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    // where to search snippet
    LiftRules.addToPackages("com.xebia.xkeng")

    // Build SiteMap
    /*    def sitemap() = SiteMap(

          Menu("Home") / "index" >> User.AddUserMenusAfter, // Simple menu form
          // Menu with special Link
          Menu(Loc("Static", Link(List("static"), true, "/static/index"),
             "Static Content")))
    */

    //    LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap()))


    Assembly.initMongoDB()
    LiftRules.dispatch.append(Assembly.XKENGDispatchAPIAssembly)
    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }



}
