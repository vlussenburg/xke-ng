package com.xebia.xkeng.dao

import net.liftweb.json.JsonDSL._
import org.joda.time.format._
import org.joda.time.DateTime
import com.xebia.xkeng.model.{Location, Session, Conference}

trait ConferenceRepository {
  def findConferences(year: Int): List[Conference]

  def findConferences(year: Int, month: Int): List[Conference]

  def findConferences(year: Int, month: Int, day: Int): List[Conference]

  def findConference(id: String): Option[Conference]

  def findSessionsOfConference(id: String): List[Session]

  def findAllLocations: List[Location]

}

trait SessionRepository {
  def findSession(id: String): Option[Session]
}

trait RepositoryComponent {

  val conferenceRepository:ConferenceRepository
  val sessionRepository:SessionRepository


  class ConferenceRepositoryImpl extends ConferenceRepository {

    val fmt = DateTimeFormat.forPattern("yyyyMMdd");

    private def dateRegexpQry(begin:String) = {
      ("begin" -> ("$regex" -> ("^%s.*".format(begin))))
    }

    def findSessionsOfConference(conferenceId: String): List[Session] = {
      this.findConference(conferenceId) match {
        case Some(conf) => {
          //get the sessions
          val sessionIds = conf.slots.map(_.sessionRefId).flatten
          val sessions: List[Session] = sessionIds.map(id => sessionRepository.findSession(id.toString)).flatten //objectId to string.. does that work?
          sessions
        }
        case None => Nil
      }
    }

    /**
     * db.confs.find( { begin : { $regex : "^<year>.*" } } );
     */
    def findConferences(year: Int) = Conference.findAll(dateRegexpQry("%04d" format(year)))

    /**
     * db.confs.find( { begin : { $regex : "^<year>-<month>.*" } } );
     */
    def findConferences(year: Int, month: Int) = Conference.findAll(dateRegexpQry("%04d-%02d" format(year, month)))

    /**
     * db.confs.find( { begin : { $regex : "^<year>-<month>-<day>.*" } } );
     */
    def findConferences(year: Int, month: Int, day: Int) = Conference.findAll(dateRegexpQry("%04d-%02d-%02d" format(year, month, day)))


    /**
     * db.confs.find( { _id: ObjectId("<id>")} );
     */
    def findConference(id: String) = Conference.find(id)

    def findAllLocations = {
      val conferences = Conference.findAll
      var locations: Set[Location] = Set()
      conferences.foreach { locationCol =>
        locations = locations ++ locationCol.locations;
      }
      locations.toList
    }
  }

  class SessionRepositoryImpl extends SessionRepository {
    def findSession(id: String) = Session.find(id)
  }

}