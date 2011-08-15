package com.xebia.xkeng.rest

import net.liftweb.http.rest.RestHelper
import net.liftweb.common._
import collection.mutable.{ListBuffer => MList}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.xebia.xkeng.dao.RepositoryComponent
import JsonDomainConverters._
import net.liftweb.http._
import org.joda.time._
import net.liftweb.util.BasicTypesHelpers._
import com.xebia.xkeng.model.Session

trait XKENGDispatchAPI extends RestHelper with Logger {
  this: RepositoryComponent =>

  // TODO. If specified path/ without argument, it errors on the toInt

  serve {
    // GET /conferences/<year>[/<month>[/<day>]]
    case Req("conferences" :: Nil, _, GetRequest) =>
      asJsonResp(conferenceRepository.findConferences(new DateTime().getYear))
    case Req("conferences" :: AsInt(year) :: Nil, _, GetRequest) =>
      asJsonResp(conferenceRepository.findConferences(year))
    case Req("conferences" :: AsInt(year) :: AsInt(month) :: Nil, _, GetRequest) =>
      asJsonResp(conferenceRepository.findConferences(year, month))
    case Req("conferences" :: AsInt(year) :: AsInt(month) :: AsInt(day) :: Nil, _, GetRequest) =>
      asJsonResp(conferenceRepository.findConferences(year, month, day))


    // POST /conference
    case req@Req("conference" :: Nil, _, PostRequest) =>
      handleConferenceCreate(req.body.toOption)
    // GET /conference/<id>
    case Req("conference" :: id :: Nil, _, GetRequest) =>
      asJsonResp(conferenceRepository.findConference(id))
    // PUT /conference
    case req@Req("conference" :: id :: Nil, _, PutRequest) =>
      handleConferenceUpdate(id, req.body.toOption)
    // DELETE /conference/<id>
    case Req("conference" :: id :: Nil, _, DeleteRequest) =>
      handleConferenceDelete(id)
    // GET /conference/<id>/sessions
    case Req("conference" :: id :: "sessions" :: Nil, _, GetRequest) =>
      handleSessionsList(id)
    // POST /conference/<id>/session
    case req@Req("conference" :: id :: "session" :: Nil, _, PostRequest) =>
      handleSessionCreate(id.toInt, req.body.toOption)
    // PUT /conference/<id>/session
    case req@Req("conference" :: id :: "session" :: Nil, _, PutRequest) =>
      handleSessionUpdate(id.toInt, req.body.toOption)


    // GET /session/<id>
    case Req("session" :: id :: Nil, _, GetRequest) =>
      handleSessionRead(id)
    // DELETE /session/<sid>
    case Req("session" :: id :: Nil, _, DeleteRequest) =>
      handleSessionDelete(id)



    // GET /locations
    case Req("locations" :: Nil, _, GetRequest) =>
      handleLocations

    // GET /authors
    case Req("authors" :: Nil, _, GetRequest) =>
      handleAuthors

    // GET /labels
    case Req("labels" :: Nil, _, GetRequest) =>
      handleLabels
    // GET /labels/author/<id>
    case Req("labels" :: "author" :: authorId :: Nil, _, GetRequest) =>
      handleLabels(authorId)

    // PUT /label/<name>
    case req@Req("label" :: name :: Nil, _, PutRequest) =>
      handleLabelUpdate(name, req.body.toOption)

    // PUT /location
    case req@Req("location" :: Nil, _, PutRequest) =>
      handleLocationUpdate(req.body.toOption)

    // POST /search/authors
    case req@Req("search" :: "authors" :: Nil, _, PostRequest) =>
      handleSearchAuthors(req.body.toOption)
    // POST /search/sessions
    case req@Req("search" :: "sessions" :: Nil, _, PostRequest) =>
      handleSearchSessions(req.body.toOption)

    // POST /login
    case req@Req("login" :: Nil, _, PostRequest) =>
      handleLogin(req.body.toOption)
    // POST /error
    case req@Req("error" :: Nil, _, PostRequest) =>
      handleError(req.body.toOption)

    // GET /feedback/<id>/comment
    case Req("feedback" :: sessionId :: "comment" :: Nil, _, GetRequest) =>
      handleComments(sessionId.toInt)
    // PUT /feedback/<id>/comment
    case req@Req("feedback" :: sessionId :: "comment" :: Nil, _, PutRequest) =>
      handleCommentCreate(sessionId.toInt, req.body.toOption)
    // GET /feedback/<id>/rating
    case Req("feedback" :: sessionId :: "rating" :: Nil, _, GetRequest) =>
      handleRating(sessionId.toInt)
    // PUT /feedback/<id>/rating
    case req@Req("feedback" :: sessionId :: "rating" :: Nil, _, PutRequest) =>
      handleRatingCreate(sessionId.toInt, req.body.toOption)
  }

  private def asJsonResp(json: JValue) = Full(JsonResponse(json))

  private def handleConferenceUpdate(id: String, jsonBody: Option[Array[Byte]]) = {
    conferenceRepository.findConference(id) match {
      case Some(confToUpdate) => {
        val confFromJson = fromConferenceJson(new String(jsonBody.get))
        val updatedConf = confFromJson.copy(_id = confToUpdate._id)
        updatedConf.save
        asJsonResp(updatedConf)

      }
      case None => Full(BadResponse())
    }

  }

  private def handleConferenceCreate(jsonBody: Option[Array[Byte]]) = {
    val conference = fromConferenceJson(new String(jsonBody.get))
    conference.save
    asJsonResp(conference)
  }

  private def handleConferenceDelete(conferenceId: String) = {
    conferenceRepository.findConference(conferenceId).map(_.delete)
    Full(OkResponse())
  }

  private def handleSessionsList(conferenceId: String) : Full[LiftResponse] = {
    //should return the sessions of a single conference.
    asJsonResp(conferenceRepository.findSessionsOfConference(conferenceId))
    //Full(OkResponse())
  }

  private def handleSessionCreate(sessionId: Int, jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleSessionRead(sessionId: String) = {
    asJsonResp(sessionRepository.findSession(sessionId))
  }

  private def handleSessionUpdate(sessionId: Int, jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleSessionDelete(sessionId: String) = {
    sessionRepository.findSession(sessionId).map(_.delete)
    Full(OkResponse()) //don't we need to handle errors?
  }

  private def handleLocations = {
    Full(NotFoundResponse())
  }

  private def handleAuthors = {
    Full(NotFoundResponse())
  }

  private def handleLabels = {
    Full(NotFoundResponse())
  }

  private def handleLabels(authorId: String) = {
    Full(NotFoundResponse())
  }

  private def handleLabelUpdate(name: String, jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleLocationUpdate(jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleSearchAuthors(jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleSearchSessions(jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleLogin(jsonBody: Option[Array[Byte]]) = {
    val confFromJson = fromCredentialJson(new String(jsonBody.get))
	// TODO Validate the credential object
   asJsonResp("token")
  }

  private def handleError(paramBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleComments(sessionId: Int) = {
    Full(NotFoundResponse())
  }

  private def handleCommentCreate(sessionId: Int, jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleRating(sessionId: Int) = {
    Full(NotFoundResponse())
  }

  private def handleRatingCreate(sessionId: Int, jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }
}
