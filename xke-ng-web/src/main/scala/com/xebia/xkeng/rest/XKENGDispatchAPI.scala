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

trait XKENGDispatchAPI extends RestHelper with Logger {
  this: RepositoryComponent with RestHandlerComponent =>
    
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
    /**
     * *******************
     * conference
     * *******************
     */

    // POST /conference
    case req @ Req("conference" :: Nil, _, PostRequest) =>
      handleConferenceCreate(req.body.toOption)
    // GET /conference/<id>
    case Req("conference" :: id :: Nil, _, GetRequest) =>
      conferenceRepository.findConference(id) match {
        case Some(c) => asJsonResp(c)
        case _ => Full(NotFoundResponse())
      }
    // PUT /conference
    case req @ Req("conference" :: id :: Nil, _, PutRequest) =>
      handleConferenceUpdate(id, req.body.toOption)
    // DELETE /conference/<id>
    case Req("conference" :: id :: Nil, _, DeleteRequest) =>
      handleConferenceDelete(id)
    /**
     * *******************
     * session
     * *******************
     */

    // GET /conference/<id>/sessions
    case Req("conference" :: id :: "sessions" :: Nil, _, GetRequest) =>
      handleSessionsList(id)
    // POST /conference/<id>/session
    case req @ Req("conference" :: id :: "session" :: Nil, _, PostRequest) =>
      handleSessionCreate(id, req.body.toOption)
    // PUT /conference/<id>/session
    //-> PUT session/<id>
    case req @ Req("conference" :: id :: "session" :: Nil, _, PutRequest) =>
      handleSessionUpdate(id, req.body.toOption)
    //switch same business rules as create session
    //PUT /session/<sessionid>/conference/<conferenceid>

    // GET /session/<id>
    case Req("session" :: AsLong(id) :: Nil, _, GetRequest) =>
      asJsonResp(Some(sessionRepository.findSessionById(id).map(_._2)))
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
      asJsonResp(facilityRepository.findAllLocations)

    // POST /location
    case req @ Req("location" :: Nil, _, PostRequest) =>
      handleLocationCreate(req.body.toOption)

    /**
     * *******************
     * authors
     * *******************
     */

    // GET /authors
    case Req("authors" :: Nil, _, GetRequest) =>
      asJsonResp(authorRepository.findAllAuthors)

    /**
     * *******************
     * labels
     * *******************
     */
    // GET /labels
    case Req("labels" :: Nil, _, GetRequest) =>
      asJsonResp(labelRepository.findAllLabels())
    // GET /labels/author/<id>
    case Req("labels" :: "author" :: authorId :: Nil, _, GetRequest) =>
      asJsonResp(labelRepository.findLabelsByAuthorId(authorId))

    /**
     * *******************
     * search
     * *******************
     */

    // POST /search/authors
    case req @ Req("search" :: "authors" :: Nil, _, PostRequest) =>
      handleSearchAuthors(req.body.toOption)
    // POST /search/sessions
    case req @ Req("search" :: "sessions" :: Nil, _, PostRequest) =>
      handleSearchSessions(req.body.toOption)

    // POST /error
    case req @ Req("error" :: Nil, _, PostRequest) =>
      //dump in logfile
      //include name
      handleError(req.body.toOption)

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
      handleCommentCreate(sessionId, req.body.toOption)
    // GET /feedback/<id>/rating
    case Req("feedback" :: AsLong(sessionId) :: "rating" :: Nil, _, GetRequest) =>
      readRatings(sessionId)
    // POST /feedback/<id>/rating
    case req @ Req("feedback" :: AsLong(sessionId) :: "rating" :: Nil, _, PostRequest) =>
      handleRatingCreate(sessionId, req.body.toOption)
  }


  private def handleConferenceUpdate(id: String, jsonBody: Option[Array[Byte]]) = {
    conferenceRepository.findConference(id) match {
      case Some(confToUpdate) => {
        val confFromJson = fromConferenceJson(new String(jsonBody.get))
        val updatedConf = confFromJson.copy(_id = confToUpdate._id).copy(sessions = confToUpdate.sessions)
        updatedConf.save
        asJsonResp(updatedConf)
      }
      case None => Full(NotFoundResponse())
    }

  }

  private def handleConferenceCreate(jsonBody: Option[Array[Byte]]) = {
    val conference = fromConferenceJson(new String(jsonBody.get))
    conference.copy(sessions = Nil).save
    asJsonResp(conference)
  }

  private def handleConferenceDelete(conferenceId: String) = {
    conferenceRepository.findConference(conferenceId).map(_.delete)
    Full(OkResponse())
  }

  private def handleSessionsList(conferenceId: String): Box[LiftResponse] = {
    //should return the sessions of a single conference.
    asJsonResp(conferenceRepository.findSessionsOfConference(conferenceId))

  }

  private def handleSessionCreate(conferenceId: String, jsonBody: Option[Array[Byte]]) = {
    conferenceRepository.findConference(conferenceId) match {
      case Some(conf) => {
        val session = fromSessionJson(true)(new String(jsonBody.get))
        conf.saveOrUpdate(session)
        asJsonResp(session)
      }
      case _ => Full(NotFoundResponse())
    }
  }

  private def handleSessionUpdate(confId: String, jsonBody: Option[Array[Byte]]) = {
    conferenceRepository.findConference(confId) match {
      case Some(conf) => {
        val updatedSession = fromSessionJson(false)(new String(jsonBody.get))
        conf.saveOrUpdate(updatedSession)
        Full(OkResponse())
      }
      case _ => Full(NotFoundResponse())

    }
  }

  private def handleSessionDelete(sessionId: Long) = {
    sessionRepository.deleteSessionById(sessionId)
    Full(OkResponse()) //don't we need to handle errors?
  }

  private def handleLabelUpdate(name: String, jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleLocationCreate(jsonBody: Option[Array[Byte]]) = {
    val location = fromLocationJson(true)(new String(jsonBody.get))
    facilityRepository.addLocation(location)
    asJsonResp(location)

  }

  private def handleSearchAuthors(jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def handleSearchSessions(jsonBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }


  private def handleError(paramBody: Option[Array[Byte]]) = {
    Full(NotFoundResponse())
  }

  private def readComments(sessionId: Long) = {
    sessionRepository.findSessionById(sessionId) match {
      case Some((conference, session)) => asJsonResp(session.comments)
      case None => Full(NotFoundResponse())
    }

  }

  private def handleCommentCreate(sessionId: Long, jsonBody: Option[Array[Byte]]) = {
    val comment = fromCommentJson(new String(jsonBody.get))
    asJsonResp(sessionRepository.commentSessionById(sessionId, comment))
  }

  private def readRatings(sessionId: Long) = {
    sessionRepository.findSessionById(sessionId) match {
      case Some((conference, session)) => asJsonResp(session.ratings)
      case None => Full(NotFoundResponse())
    }
  }

  private def handleRatingCreate(sessionId: Long, jsonBody: Option[Array[Byte]]) = {
    val rating = fromRatingJson(new String(jsonBody.get))
    asJsonResp(sessionRepository.rateSessionById(sessionId, rating))
  }
}
