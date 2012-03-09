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
import RestUtils._

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

trait XKENGSecuredAPI extends RestHelper with Logger {
  this: RestHandlerComponent =>

  serve {
    /**
     * *******************
     * conference
     * *******************
     */
    // GET /conferences/<year>[/<month>[/<day>]]
    case Req("conferences" :: Nil, _, GetRequest) =>
      getConferences
    case Req("conferences" :: AsInt(year) :: Nil, _, GetRequest) =>
      getConferences(year)
    case Req("conferences" :: AsInt(year) :: AsInt(month) :: Nil, _, GetRequest) =>
      getConferences(year, month)
    case Req("conferences" :: AsInt(year) :: AsInt(month) :: AsInt(day) :: Nil, _, GetRequest) =>
      getConferences(year, month, day)

    // GET /conference/next/<ahead>
    case Req("conference" :: "next" :: Nil, _, GetRequest) =>
      getNextConference(1)
    case Req("conference" :: "next" :: AsInt(ahead) :: Nil, _, GetRequest) =>
      getNextConference(ahead)
    case Req("conferences" :: "summary" :: "pastcount" :: AsInt(amountPastConferences) :: "futurecount" :: AsInt(amountFutureConferences) :: Nil, _, GetRequest) =>
      getConferencesRange(amountPastConferences, amountFutureConferences)

    // GET /conference/next/<ahead>/slots
    case Req("conference" :: "next" :: "slots" :: Nil, _, GetRequest) =>
      getNextConferenceSlots(1)
    case Req("conference" :: "next" :: AsInt(ahead) :: "slots" :: Nil, _, GetRequest) =>
      getNextConferenceSlots(ahead)
    // GET /conference/<id>
    case Req("conference" :: id :: "slots" :: Nil, _, GetRequest) =>
      getConferenceSlot(id)

    // POST /conference
    case req @ Req("conference" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        handleConferenceCreate(_)
      }
    // GET /conference/<id>
    case Req("conference" :: id :: Nil, _, GetRequest) =>
      getConference(id)
    // PUT /conference
    case req @ Req("conference" :: id :: Nil, _, PutRequest) =>
      doWithRequestBody(req.body) {
        handleConferenceUpdate(id, _)
      }
    // DELETE /conference/<id>
    case Req("conference" :: id :: Nil, _, DeleteRequest) =>
      handleConferenceDelete(id)
    /**
     * *******************
     * session
     * *******************
     */

    // Obsolete. Conference returns all sessions already.
    // GET /conference/<id>/sessions
    //    case Req("conference" :: id :: "sessions" :: Nil, _, GetRequest) =>
    //      handleSessionsList(id)

    // POST /conference/<id>/session
    case req @ Req("conference" :: id :: "session" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        handleSessionCreate(id, _)
      }
    // PUT /conference/<id>/session OBSOLETE
    //-> PUT session/<id>
    //    case req @ Req("conference" :: id :: "session" :: Nil, _, PutRequest) =>
    //      doWithRequestBody(req.body) {
    //        handleSessionUpdate(id, _)
    //      }
    case req @ Req("session" :: AsLong(id) :: Nil, _, PutRequest) =>
      doWithRequestBody(req.body) {
        handleSessionUpdate(id, _)
      }
    //switch same business rules as create session
    //PUT /session/<sessionid>/conference/<conferenceid>

    // GET /session/<id>
    case Req("session" :: AsLong(id) :: Nil, _, GetRequest) =>
      getSession(id)
    // DELETE /session/<sid>
    case Req("session" :: AsLong(id) :: Nil, _, DeleteRequest) =>
      handleSessionDelete(id)

    /**
     * *******************
     * location
     * *******************
     */
    // GET /locations
    case Req("locations" :: Nil, _, GetRequest) =>
      getLocations

    // POST /location
    case req @ Req("location" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        handleLocationCreate(_)
      }

    /**
     * *******************
     * authors
     * *******************
     */
    // GET /authors
    case Req("authors" :: Nil, _, GetRequest) =>
      getAuthors

    /**
     * *******************
     * labels
     * *******************
     */
    // GET /labels
    case Req("labels" :: Nil, _, GetRequest) =>
      getLabels
    // GET /labels/author/<id>
    case Req("labels" :: "author" :: authorId :: Nil, _, GetRequest) =>
      getLabelsByAuthor(authorId)

    /**
     * *******************
     * search
     * *******************
     */

    // POST /search/authors
    case req @ Req("search" :: "authors" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        handleSearchAuthors(_)
      }

    // POST /search/sessions
    case req @ Req("search" :: "sessions" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        handleSearchSessions(_)
      }

    /**
     * *******************
     * feedback
     * *******************
     */
    // GET /feedback/<id>/comment
    case Req("feedback" :: AsLong(sessionId) :: "comment" :: Nil, _, GetRequest) =>
      readComments(sessionId)
    // POST /feedback/<id>/comment
    case req @ Req("feedback" :: AsLong(sessionId) :: "comment" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        handleCommentCreate(sessionId, _)
      }
    // GET /feedback/<id>/rating
    case Req("feedback" :: AsLong(sessionId) :: "rating" :: Nil, _, GetRequest) =>
      readRatings(sessionId)
    // POST /feedback/<id>/rating
    case req @ Req("feedback" :: AsLong(sessionId) :: "rating" :: Nil, _, PostRequest) =>
      doWithRequestBody(req.body) {
        handleRatingCreate(sessionId, _)
      }

    /**
     * *******************
     * logout
     * *******************
     */
    // GET /logout
    case Req("logout" :: Nil, _, GetRequest) =>
      UserHolder.logout
      S.session.open_!.terminateHint
      Full(OkResponse())
  }

}
