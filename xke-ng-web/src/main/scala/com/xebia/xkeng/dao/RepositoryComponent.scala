package com.xebia.xkeng.dao

import net.liftweb.json.JsonDSL._
import org.joda.time.format._
import org.joda.time.DateTime
import com.xebia.xkeng.model.{ Location, Session, Conference, Facility, Author, AuthorDoc, Comment }

trait ConferenceRepository {
  def findConferences(year: Int): List[Conference]

  def findConferences(year: Int, month: Int): List[Conference]

  def findConferences(year: Int, month: Int, day: Int): List[Conference]

  def findConference(id: String): Option[Conference]

  def findSessionsOfConference(id: String): List[Session]

}

trait SessionRepository {
  def findSessionById(id: Long): Option[(Conference, Session)]
  def deleteSessionById(id: Long): Unit
  def rateSessionById(id: Long, rate: Int): List[Int]
  def commentSessionById(id: Long, rate: Int): List[Comment]
}

trait FacilityRepository {
  def findAllLocations: List[Location]

  def findLocationByName(name: String): Option[Location]

  def addLocation(location: Location): Unit

  def updateLocation(location: Location): Unit
}

trait AuthorRepository {
  def findAllAuthors: List[Author]

  def addAuthor(author: Author): Unit

  def updateAuthor(author: Author): Unit

  def removeAuthor(author: Author): Unit

  def findAuthorByName(name: String): Option[Author]

}

trait RepositoryComponent {

  val conferenceRepository: ConferenceRepository
  val sessionRepository: SessionRepository
  val facilityRepository: FacilityRepository
  val authorRepository: AuthorRepository

  class ConferenceRepositoryImpl extends ConferenceRepository {

    val fmt = DateTimeFormat.forPattern("yyyyMMdd");

    private def dateRegexpQry(begin: String) = {
      ("begin" -> ("$regex" -> ("^%s.*".format(begin))))
    }

    def findSessionsOfConference(conferenceId: String): List[Session] = {
      this.findConference(conferenceId).map(_.sessions).getOrElse(Nil)
    }

    /**
     * db.confs.find( { begin : { $regex : "^<year>.*" } } );
     */
    def findConferences(year: Int) = Conference.findAll(dateRegexpQry("%04d" format (year)))

    /**
     * db.confs.find( { begin : { $regex : "^<year>-<month>.*" } } );
     */
    def findConferences(year: Int, month: Int) = Conference.findAll(dateRegexpQry("%04d-%02d" format (year, month)))

    /**
     * db.confs.find( { begin : { $regex : "^<year>-<month>-<day>.*" } } );
     */
    def findConferences(year: Int, month: Int, day: Int) = Conference.findAll(dateRegexpQry("%04d-%02d-%02d" format (year, month, day)))

    /**
     * db.confs.find( { _id: ObjectId("<id>")} );
     */
    def findConference(id: String) = Conference.find(id)

  }

  class SessionRepositoryImpl extends SessionRepository {
    def findSessionById(id: Long): Option[(Conference, Session)] = {
      Conference.find(("sessions.id" -> id)).map(c => (c, c.sessions.find(_.id == id).get))
    }
    def deleteSessionById(id: Long): Unit = {
      val conf = Conference.find(("sessions.id" -> id))
      conf.map(c => c.remove(c.getSessionById(id).get))
    }
    def rateSessionById(id: Long, rate: Int): List[Int] = {
      List(1, 2, 3)
    }
    def commentSessionById(id: Long, rate: Int): List[Comment] = {
      val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
      val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")
      List(Comment("This stuf is cool", a1), Comment("This stuff is not cool", a2))
    }

  }

  class FacilityRepositoryImpl extends FacilityRepository {

    def findAllLocations: List[Location] = getFacility.locations

    def findLocationByName(name: String): Option[Location] = getFacility.locations.find { _.description.equalsIgnoreCase(name) }

    def addLocation(location: Location): Unit = {
      getFacility.locations.find(_.description.equalsIgnoreCase(location.description)) match {
        case Some(l) => throw new IllegalArgumentException("Cannot create location %s because it does already exist".format(location))
        case None => getFacility.saveOrUpdate(location)
      }
    }

    def updateLocation(location: Location): Unit = {
      val f = getFacility
      f.locations.find(_.id == location.id) match {
        case Some(currentLocation) => f.saveOrUpdate(location)
        case None => throw new IllegalArgumentException("Cannot update location %s because it does not exist".format(location))
      }
    }

    /**
     * Make sure there is only one facility
     */
    private def getFacility: Facility = {
      Facility.findAll match {
        case Nil => {
          val f = Facility("NL", Nil)
          f.save
          f
        }
        case f :: xs => f
      }
    }
  }

  class AuthorRepositoryImpl extends AuthorRepository {
    def findAllAuthors: List[Author] = {
      AuthorDoc.findAll.map(_.author)
    }

    def addAuthor(author: Author): Unit = {
      findAuthorById(author.userId) match {
        case Some(foundAuthor) => throw new IllegalArgumentException("Cannot create author %s because an author (%s) with the same userId exists does already exist".format(author, foundAuthor))
        case None => AuthorDoc(author).save
      }
    }

    def updateAuthor(author: Author): Unit = {
      findAuthorById(author.userId) match {
        case Some(currentAuthor) => currentAuthor.delete; AuthorDoc(author).save
        case None => throw new IllegalArgumentException("Cannot update author %s because author with userId %s does not exist".format(author, author.userId))
      }

    }

    def findAuthorByName(name: String): Option[Author] = {
      AuthorDoc.find(("author.name" -> name)) match {
        case Some(aDoc) => Some(aDoc.author)
        case _ => None
      }
    }

    def removeAuthor(author: Author): Unit = {
      findAuthorById(author.userId).map(_.delete)
    }

    private def findAuthorById(userId: String): Option[AuthorDoc] = {
      AuthorDoc.find(("author.userId" -> userId))
    }

  }
}

 