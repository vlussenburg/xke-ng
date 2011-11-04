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

trait RestHandlerComponent extends Logger {
  this: RepositoryComponent =>

  /**
   * =============================
   * Login
   * =============================
   */
  def login(json: String): Box[LiftResponse] = {
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

  /**
   * =============================
   * Conferences
   * =============================
   */
  def getConferences = {
    asJsonResp(conferenceRepository.findConferences(new DateTime().getYear))
  }

  def getConferences(year: Int) = {
    asJsonResp(conferenceRepository.findConferences(year))
  }

  def getConferences(year: Int, month: Int) = {
    asJsonResp(conferenceRepository.findConferences(year, month))
  }

  def getConferences(year: Int, month: Int, day: Int) = {
    asJsonResp(conferenceRepository.findConferences(year, month, day))
  }

  def getConference(id: String) = {
    conferenceRepository.findConference(id) match {
      case Some(c) => asJsonResp(c)
      case _ => Full(NotFoundResponse())
    }
  }

  def handleConferenceUpdate(id: String, jsonBody: String) = {
    conferenceRepository.findConference(id) match {
      case Some(confToUpdate) => {
        val confFromJson = fromConferenceJson(jsonBody)
        val updatedConf = confFromJson.copy(_id = confToUpdate._id).copy(sessions = confToUpdate.sessions)
        updatedConf.save
        asJsonResp(updatedConf)
      }
      case None => Full(NotFoundResponse())
    }

  }
  def handleConferenceCreate(jsonBody: String) = {
    val conference = fromConferenceJson(jsonBody)
    conference.copy(sessions = Nil).save
    asJsonResp(conference)
  }

  def handleConferenceDelete(conferenceId: String) = {
    conferenceRepository.findConference(conferenceId).map(_.delete)
    Full(OkResponse())
  }
  /**
   * =============================
   * Sessions
   * =============================
   */

  def getSession(id: Long) = asJsonResp(Some(sessionRepository.findSessionById(id).map(_._2)))

  def handleSessionsList(conferenceId: String): Box[LiftResponse] = {
    //should return the sessions of a single conference.
    asJsonResp(conferenceRepository.findSessionsOfConference(conferenceId))

  }

  def handleSessionCreate(conferenceId: String, jsonBody: String) = {
    conferenceRepository.findConference(conferenceId) match {
      case Some(conf) => {
        val session = fromSessionJson(true)(jsonBody)
        conf.saveOrUpdate(session)
        asJsonResp(session)
      }
      case _ => Full(NotFoundResponse())
    }
  }

  def handleSessionUpdate(confId: String, jsonBody: String) = {
    conferenceRepository.findConference(confId) match {
      case Some(conf) => {
        val updatedSession = fromSessionJson(false)(jsonBody)
        conf.saveOrUpdate(updatedSession)
        Full(OkResponse())
      }
      case _ => Full(NotFoundResponse())

    }
  }

  def handleSessionUpdate(sessionId: Long, jsonBody: String) = {
    sessionRepository.findSessionById(sessionId) match {
      case Some((conf, session)) => {
        val updatedSession = fromSessionJson(false)(jsonBody)
        conf.saveOrUpdate(updatedSession.copy(id = session.id))
        Full(OkResponse())
      }
      case _ => Full(NotFoundResponse())

    }
  }

  def handleSessionDelete(sessionId: Long) = {
    sessionRepository.deleteSessionById(sessionId)
    Full(OkResponse()) //don't we need to handle errors?
  }
  /**
   * =============================
   * Labels
   * =============================
   */
  def handleLabelUpdate(name: String, jsonBody: String) = {
    Full(NotFoundResponse())
  }

  def getLabels = asJsonResp(labelRepository.findAllLabels())
  def getLabelsByAuthor(authorId: String) = asJsonResp(labelRepository.findLabelsByAuthorId(authorId))

  /**
   * =============================
   * Location
   * =============================
   */
  def handleLocationCreate(jsonBody: String) = {
    val location = fromLocationJson(true)(jsonBody)
    facilityRepository.addLocation(location)
    asJsonResp(location)

  }

  def getLocations = asJsonResp(facilityRepository.findAllLocations)

  /**
   * =============================
   * Authors
   * =============================
   */

  def getAuthors = asJsonResp(authorRepository.findAllAuthors)

  /**
   * =============================
   * Search
   * =============================
   */
  def handleSearchAuthors(jsonBody: String) = {
    Full(NotFoundResponse())
  }

  def handleSearchSessions(jsonBody: String) = {
    Full(NotFoundResponse())
  }

  /**
   * =============================
   * Device error
   * =============================
   */
  def handleError(paramBody: String) = {
    val user = if (UserHolder.isLoggedIn) UserHolder.loggedInAuthor.userId else "unknown"
    error("Device error of user %s. Error is %s" format (user, paramBody))
    Full(OkResponse())
  }
  /**
   * =============================
   * Comments
   * =============================
   */
  def readComments(sessionId: Long) = {
    sessionRepository.findSessionById(sessionId) match {
      case Some((conference, session)) => asJsonResp(session.comments)
      case None => Full(NotFoundResponse())
    }

  }

  def handleCommentCreate(sessionId: Long, jsonBody: String) = {
    val comment = fromCommentJson(jsonBody)
    asJsonResp(sessionRepository.commentSessionById(sessionId, comment))
  }
  /**
   * =============================
   * Ratings
   * =============================
   */
  def readRatings(sessionId: Long) = {
    sessionRepository.findSessionById(sessionId) match {
      case Some((conference, session)) => asJsonResp(session.ratings)
      case None => Full(NotFoundResponse())
    }
  }

  def handleRatingCreate(sessionId: Long, jsonBody: String) = {
    val rating = fromRatingJson(jsonBody)
    asJsonResp(sessionRepository.rateSessionById(sessionId, rating))
  }
  
}