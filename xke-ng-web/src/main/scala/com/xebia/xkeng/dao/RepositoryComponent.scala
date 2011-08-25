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
  def findSessionById(id: Long): Option[(Conference, Session)]
  def deleteSessionById(id:Long):Unit
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
      this.findConference(conferenceId).map(_.sessions).getOrElse(Nil)
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
    def findSessionById(id: Long):Option[(Conference, Session)] = {
      Conference.find(("sessions.id" -> id)).map(c => (c, c.sessions.find(_.id == id).get))
    }
     def deleteSessionById(id: Long):Unit = {
      val conf = Conference.find(("sessions.id" -> id))
      conf.map(c => c.remove(c.getSessionById(id).get))
    }
  }
  
}