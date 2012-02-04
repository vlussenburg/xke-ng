package com.xebia.xkeng.dao

import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb._
import com.mongodb._
import org.joda.time.format._
import org.joda.time.DateTime
import com.xebia.xkeng.model.{ Location, Session, Conference, Facility, Author, AuthorDoc, Comment, Rating, Labels, SessionListener, Credential }
import com.atlassian.crowd.service.client.CrowdClient
import com.atlassian.crowd.service.client.ClientProperties
import com.atlassian.crowd.service.client.ClientPropertiesImpl._
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.exception._
import java.util.Properties
import net.liftweb.common.Logger
import com.xebia.xkeng.security.util.SecurityUtils._

trait ConferenceRepository {
  def findConferences(year: Int): List[Conference]

  def findConferences(year: Int, month: Int): List[Conference]

  def findConferences(year: Int, month: Int, day: Int): List[Conference]

  def findConference(id: String): Option[Conference]

  def findNextConference(ahead: Int): Option[Conference]

  def findSessionsOfConference(id: String): List[Session]

}

trait SessionRepository {
  def findSessionById(id: Long): Option[(Conference, Session)]
  def deleteSessionById(id: Long): Unit
  def rateSessionById(id: Long, rate: Rating): List[Rating]
  def commentSessionById(id: Long, rate: Comment): List[Comment]
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
  def findAuthorById(userId: String): Option[Author]

}

trait LabelRepository {

  def findAllLabels(): Set[String]
  def findLabelsByAuthorId(userId: String): Set[String]
  def addLabels(label: String*): Unit
}

trait AuthenticationRepository {

  def authenticate(cred: Credential): Option[User]
}

trait RepositoryComponent {

  val conferenceRepository: ConferenceRepository
  val sessionRepository: SessionRepository
  val facilityRepository: FacilityRepository
  val authorRepository: AuthorRepository
  val labelRepository: LabelRepository
  val authenticationRepository: AuthenticationRepository

  class ConferenceRepositoryImpl extends ConferenceRepository {

    val fmt = DateTimeFormat.forPattern("yyyyMMdd");
    val MONG_DB_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

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

    /**
     * db.confs.find({}, {'sessions.labels':1})
     */
    def findAllLabels(): List[String] = {
      val confererencesWithLabels = Conference.findAll(new BasicDBObject(), Some(new BasicDBObjectBuilder().add("sessions.label", 1).get()))
      Nil
    }
   
    /**
     * db.confs.find({'begin':{$gte:'2012-02-03T10:39:07.270Z'}}).sort({'begin':1}).limit(<ahead>)
     */
    def findNextConference(ahead: Int): Option[Conference] = {
      val futureConferencesQry = ("begin" -> ("$gte" -> new DateTime().minusDays(1).toString(MONG_DB_DATE_FORMAT)))
      val sortQry = ("begin" -> 1)
      val r = Conference.findAll(futureConferencesQry, sortQry, Limit(ahead))
      if (r.size == ahead) Some(r(ahead - 1)) else None
    }

  }

  class SessionRepositoryImpl extends SessionRepository {
    def findSessionById(id: Long): Option[(Conference, Session)] = {
      Conference.find(("sessions.id" -> id)).map(c => (c, c.sessions.find(_.id == id).get))
    }
    def deleteSessionById(id: Long): Unit = {
      val conf = Conference.find(("sessions.id" -> id))
      conf.map(c => c.remove(c.getSessionById(id).get))
    }

    def rateSessionById(id: Long, rating: Rating): List[Rating] = {
      val (conference, _) = getSessionById(id)
      val ratedSession = conference.rateSessionById(id, rating)
      //conference.saveOrUpdate(ratedSession)
      ratedSession.ratings
    }

    def commentSessionById(id: Long, comment: Comment): List[Comment] = {
      val (conference, _) = getSessionById(id)
      val commentedSession = conference.commentSessionById(id, comment)
      //conference.saveOrUpdate(commentedSession)
      commentedSession.comments
    }

    private def getSessionById(id: Long): (Conference, Session) = {
      findSessionById(id).map(t => t).getOrElse {
        throw new IllegalArgumentException("Cannot comment session with id %s because it does not exist".format(id))
      }
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
      findAuthorDocById(author.userId) match {
        case Some(foundAuthor) => throw new IllegalArgumentException("Cannot create author %s because an author (%s) with the same userId exists already".format(author, foundAuthor))
        case None => AuthorDoc(author).save
      }
    }

    def updateAuthor(author: Author): Unit = {
      findAuthorDocById(author.userId) match {
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
      findAuthorDocById(author.userId).map(_.delete)
    }

    def findAuthorById(userId: String): Option[Author] = {
      AuthorDoc.find(("author.userId" -> userId)).map(_.author).orElse(None)
    }

    private def findAuthorDocById(userId: String): Option[AuthorDoc] = {
      AuthorDoc.find(("author.userId" -> userId))
    }

  }

  class LabelRepositoryImpl extends LabelRepository with SessionListener {

    /**
     * Register listener
     */
    Conference.addSessionListener(this)
    /**
     * The labels are updated when a session is updated, which can contain
     * a new label
     */
    override def sessionUpdated(session: Session) = addLabels(session.labels.toSeq: _*)

    def findAllLabels(): Set[String] = {
      getLabelsDoc.labels
    }
    def addLabels(labels: String*): Unit = {
      val labelsDoc = getLabelsDoc
      labels.foreach(labelsDoc.add(_))
    }

    def findLabelsByAuthorId(userId: String): Set[String] = {
      val sessionsGivenByAuthor: List[Session] = Conference.findAll(("sessions.authors.userId" -> userId)).flatMap {
        _.sessions.filter(_.authors.exists(_.userId == userId))
      }
      sessionsGivenByAuthor.flatMap(_.labels).toSet
    }

    /**
     * Make sure there is only one Labels document
     */
    private def getLabelsDoc: Labels = {
      Labels.findAll match {
        case Nil => {
          val f = Labels("NL", Set.empty)
          f.save
          f
        }
        case f :: xs => f
      }
    }
  }

  class DummyAuthenticationRepositoryImpl extends AuthenticationRepository {
    def authenticate(cred: Credential): Option[User] = {
      Some(new User {
        override def getDisplayName() = "Dummy User"
        override def getEmailAddress() = "Dummy@notexisting.com"
        override def getFirstName() = "Dummy"
        override def getLastName() = "User"
        override def getDirectoryId() = -1
        override def isActive = true
        override def getName = "udummy"
        override def compareTo(user: com.atlassian.crowd.embedded.api.User) = 0
      })
    }
  }

  class AuthenticationRepositoryImpl(client: CrowdClient) extends AuthenticationRepository with Logger {

    def authenticate(cred: Credential): Option[User] = {
      def authenticate(userId: String, password: String) = {
        try {
          val authenticateUser = client.authenticateUser(userId, password);
          info("User %s successfully authenticated." format authenticateUser)
          Some(authenticateUser)
        } catch {
          case unf: UserNotFoundException =>
            info("User %s could not be found. Reason: %s " format (userId, unf.getMessage())); None
          case iae: InvalidAuthenticationException =>
            info("User %s provided invalid credentials. Reason: %s " format (userId, iae.getMessage())); None
          case e: Exception => info("User %s could not be authenticated. Reason: %s " format (userId, e.getMessage())); None
        }
      }

      if (!cred.isEncrypted)
        authenticate(cred.username, cred.password)
      else {
        val decryptedPwd = decrypt(cred.password)
        authenticate(cred.username, decryptedPwd)
      }

    }
  }

  object AuthenticationRepository extends Logger {

    def apply(crowdBase: String, crowdSysUser: String, crowdSysPwd: String, crowdConnectionCheck: Boolean = true): AuthenticationRepository = {
      val (crowdUrl, crowdClient) = createCrowdClient(crowdBase, crowdSysUser, crowdSysPwd)
      if (crowdConnectionCheck) {
        testCrowdConnection((crowdUrl, crowdClient))
      }
      new AuthenticationRepositoryImpl(crowdClient)
    }

    def createCrowdClient(crowdBase: String, crowdSysUser: String, crowdSysPwd: String): (String, CrowdClient) = {

      val properties = new Properties();
      // properties.load(CrowdTools.class.getResourceAsStream("/crowd.properties"));
      properties.setProperty("application.name", crowdSysUser);
      properties.setProperty("application.password", crowdSysPwd);
      properties.setProperty("application.login.url", crowdBase + "/console/");
      properties.setProperty("crowd.server.url", crowdBase + "/services/");
      properties.setProperty("crowd.base.url", crowdBase);
      properties.setProperty("session.isauthenticated", "session.isauthenticated");
      properties.setProperty("session.tokenkey", "session.tokenkey");
      properties.setProperty("session.validationinterval", "0");
      properties.setProperty("session.lastvalidation", "session.lastvalidation");
      val clientProperties: ClientProperties = newInstanceFromProperties(properties);
      val crowdUrl = crowdSysUser + "@" + crowdBase
      info("Using Crowd " + crowdUrl);
      val client: CrowdClient = new RestCrowdClientFactory().newInstance(clientProperties);
      (crowdUrl, client)
    }

    def testCrowdConnection(crowdClient: (String, CrowdClient)) = {
      val (crowdUrl, client) = crowdClient
      try {
        info("Test crowd connection " + crowdUrl + " ...");
        client.testConnection();
        info("Crowd connection test successful");
      } catch {
        case e: InvalidAuthenticationException => error("Crowd connection test for url " + crowdUrl + " failed. Access to Crowd denied!", e); throw e
      }

    }

  }
}

 