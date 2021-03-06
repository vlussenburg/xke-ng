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
import java.net.URLDecoder

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
    doWithSome(conferenceRepository.findConference(id)) { c => c }
  }

  
   def getConferenceSlot(id: String) = {
    doWithSome(conferenceRepository.findConference(id)) { 
      conferenceSlotsToJValue(_)
    }
  }
   
  def getNextConference(ahead: Int) = {
    asJsonResp(conferenceRepository.findNextConference(ahead))
  }

  def getNextConferenceSlots(ahead: Int) = {
    doWithSome(conferenceRepository.findNextConference(ahead)) {
      conferenceSlotsToJValue(_)
    }
  }

  def getConferencesRange(amountPastConferences: Int, amountFutureConferences: Int) = {
    val (confs, next) = conferenceRepository.findConferencesRange(amountPastConferences, amountFutureConferences)
    asJsonResp(conferencesSummaryToJValue(confs, next))
  }

  def handleConferenceUpdate(id: String, jsonBody: String) = {
    doWithSome(conferenceRepository.findConference(id)) {
      confToUpdate =>
        {
          val confFromJson = fromConferenceJson(jsonBody)
          val updatedConf = confFromJson.copy(_id = confToUpdate._id, sessions = confToUpdate.sessions)
          updatedConf.save
          updatedConf
        }
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

  def getSession(id: Long) = {
    doWithSome(sessionRepository.findSessionById(id).map(_._2)) { s => s }
  }

  def handleSessionsList(conferenceId: String): Box[LiftResponse] = {
    //should return the sessions of a single conference.
    asJsonResp(conferenceRepository.findSessionsOfConference(conferenceId))

  }

  def handleSessionCreate(conferenceId: String, jsonBody: String) = {
    doWithSome(conferenceRepository.findConference(conferenceId)) {
      conf =>
        {
          val session = fromSessionJson(true)(jsonBody)
          conf.saveOrUpdate(session)
          session
        }
    }
  }

  def handleSessionUpdate(confId: String, jsonBody: String): Box[LiftResponse] = {
    val updatedSession = fromSessionJson(false)(jsonBody)
    handleSessionUpdate(updatedSession.id, jsonBody)
  }

  def handleSessionUpdate(sessionId: Long, jsonBody: String): Box[LiftResponse] = {
    doWithSomeNoReturn(sessionRepository.findSessionById(sessionId)) {
      case (conf, session) => {
        val updatedSession = fromSessionJson(false)(jsonBody)
        conf.saveOrUpdate(updatedSession.copy(id = session.id, ratings = session.ratings, comments = session.comments))
      }
    }
  }

  def handleSessionDelete(sessionId: Long) = {
    sessionRepository.deleteSessionById(sessionId)
    Full(OkResponse())
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
    val body = URLDecoder.decode(paramBody, "UTF-8")
    error("Device error of user %s. Error is: %s\n" format (user, body))
    Full(OkResponse())
  }
  /**
   * =============================
   * Comments
   * =============================
   */
  def readComments(sessionId: Long) = {
    doWithSome(sessionRepository.findSessionById(sessionId)) {
      case (_, session) => session.comments
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
    doWithSome(sessionRepository.findSessionById(sessionId)) {
      case (_, session) => session.ratings
    }
  }

  def handleRatingCreate(sessionId: Long, jsonBody: String) = {
    val rating = fromRatingJson(jsonBody)
    asJsonResp(sessionRepository.rateSessionById(sessionId, rating))
  }

}