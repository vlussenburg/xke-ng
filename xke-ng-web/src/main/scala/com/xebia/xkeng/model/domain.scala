package com.xebia.xkeng.model

import java.util.concurrent.atomic.AtomicLong
import net.liftweb.json.ext.JodaTimeSerializers
import org.bson.types.ObjectId
import org.joda.time._
import net.liftweb.json._
import scala.Predef._
import scala.Long
import net.liftweb.mongodb.{ MongoDocument, ObjectIdSerializer, MongoDocumentMeta }
import net.liftweb.json.JsonDSL._
import java.lang.IllegalArgumentException
import com.xebia.xkeng.serialization._
import util._

package object helper {
  private val counter = new AtomicLong((System.currentTimeMillis() % 11).abs)
  def nextSeq = System.currentTimeMillis() + counter.getAndIncrement
}

import helper._

trait EmbeddedDocumentOps[T] {
  self: MongoDocumentMeta[T] =>

  type EmbeddedElem = { def id: Long; def serializeToJson: JValue }

  /**
   * Mongo qry:
   *  db.confs.update({"_id":ObjectId("<id>")},{$push:{"<arrayname>":<json>}})
   */
  def pushToArray(_id: ObjectId, arrayName: String, newobj: JValue) = {
    self.update(("_id" -> _id.toString), ("$push" -> (arrayName -> newobj)))
  }

  /**
   * Mongo qry:
   *  db.confs.update({"_id":ObjectId("<id>"), "<arrayname>":  { $elemMatch : { "<field>" : "<valueToMatch>" }}},{$set:{"<arrayname>.$":<json>}})
   */
  def updateInArray(_id: ObjectId, arrayName: String, elemInArrayQry: JValue, adjustedobj: JValue) = {
    self.update(("_id" -> _id.toString) ~ (arrayName -> ("$elemMatch" -> elemInArrayQry)), ("$set" -> (arrayName + ".$" -> adjustedobj)))
  }

  /**
   * Mongo qry:
   * db.confs.update({"_id" : ObjectId("<id>")},	{$pull : {<arrayname> : {"<field>" : "<valueToMatch>"}}})
   */
  def removeFromArray(_id: ObjectId, arrayName: String, elemInArrayQry: JValue) = {
    self.update(("_id" -> _id.toString), ("$pull" -> (arrayName -> elemInArrayQry)))
  }

  protected[model] def doSaveOrUpdate(_id: ObjectId, nameMongoArray: String, mongoArray: List[EmbeddedElem], elem: EmbeddedElem) = {
    if (!mongoArray.exists(_.id == elem.id)) {
      pushToArray(_id, nameMongoArray, elem.serializeToJson)
    } else {
      updateInArray(_id, nameMongoArray, ("id" -> elem.id), elem.serializeToJson)
    }
  }

  protected[model] def doRemove(_id: ObjectId, nameMongoArray: String, elem: EmbeddedElem) = {
    removeFromArray(_id, nameMongoArray, ("id" -> elem.id))
  }

}

/**
 * Defines the structure of a Conference.
 */
object Conference extends MongoDocumentMeta[Conference] with EmbeddedDocumentOps[Conference] {
  override def collectionName = "confs"
	
  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all

  var listeners:List[SessionListener] = Nil;
  def addSessionListener(listener:SessionListener *) = listeners =  listeners ::: listener.toList
  
  
  def apply(title: String, begin: DateTime, end: DateTime, sessions: List[Session], locations: List[Location]): Conference = {
    Conference(ObjectId.get, title, begin, end, sessions, locations)
  }

  def apply(id: String, title: String, begin: DateTime, end: DateTime, sessions: List[Session], locations: List[Location]): Conference = {
    Conference(new ObjectId(id), title, begin, end, sessions, locations)
  }
}

/**
 * Represents a Conference, a conference has a number of locations, where sessions are available.
 * This class is a case class, due to net.liftweb.mongodb requirements.
 */
case class Conference(val _id: ObjectId, title: String, begin: DateTime, end: DateTime, var sessions: List[Session], var locations: List[Location]) extends MongoDocument[Conference] {

  def meta = Conference

  def saveOrUpdate(session: Session) = {
    meta.doSaveOrUpdate(_id, "sessions", sessions, session)
    sessions = session :: (sessions - session)
    meta.listeners.foreach(_.sessionUpdated(session))
    sessions
  }

  def remove(session: Session) = {
    meta.doRemove(_id, "sessions", session)
    sessions = sessions.filter(_.id != session.id)
    sessions
  }

  def saveOrUpdate(location: Location) = {
    meta.doSaveOrUpdate(_id, "locations", locations, location)
    locations = location :: (locations - location)
    locations
  }

  def remove(location: Location) = {
    if (sessions.exists(_.location.id == location.id)) {
      throw new IllegalArgumentException("The following sessions: %s still depend on the location: %s you want to remove. A location can only be removed if it has no references to sessions." format (sessions, location))
    }
    meta.doRemove(_id, "locations", location)
    locations = locations.filter(_.id != location.id)
    locations
  }

  def getLocationById(id: Long): Option[Location] = locations.find(_.id == id)

  def getSessionById(id: Long): Option[Session] = sessions.find(_.id == id)
  

}

/**
 * Represents a Session at a location. A Session contains time, space and session properties.
 */
case class Session(val id: Long, val start: DateTime, val end: DateTime, val location: Location, val title: String, val description: String, sessionType: String, val limit: String, authors: List[Author], ratings: List[Rating], comments: List[Comment], labels:Set[String]) extends ToJsonSerializer[Session] {
  def period = new Period(start.getMillis, end.getMillis)
  
  def addComment(comment:Comment):Session = copy(comments = comment :: comments)

  def addRating(rating:Rating):Session = {
    ratings.find(_.userId == rating.userId) match {
      case Some(_) => copy(ratings = (rating :: ratings.filterNot(_.userId == rating.userId)))
      case None => copy(ratings = rating :: ratings)
    }
  }

}

/**
 * defines the structure of a session
 */
object Session extends FromJsonDeserializer[Session] {

  def apply(start: DateTime, end: DateTime, location: Location, title: String, description: String, sessionType: String, limit: String) = {
    new Session(nextSeq, start, end, location, title, description, sessionType, limit, Nil, Nil, Nil, Set.empty)
  }
  def apply(start: DateTime, end: DateTime, location: Location, title: String, description: String, sessionType: String, limit: String, authors: List[Author]) = {
    new Session(nextSeq, start, end, location, title, description, sessionType, limit, authors, Nil, Nil, Set.empty)
  }
  def apply(start: DateTime, end: DateTime, location: Location, title: String, description: String, sessionType: String, limit: String, authors: List[Author], ratings: List[Rating]) = {
    new Session(nextSeq, start, end, location, title, description, sessionType, limit, authors, ratings, Nil, Set.empty)
  }
  def apply(start: DateTime, end: DateTime, location: Location, title: String, description: String, sessionType: String, limit: String, authors: List[Author], ratings: List[Rating], comments: List[Comment]) = {
    new Session(nextSeq, start, end, location, title, description, sessionType, limit, authors, ratings, comments, Set.empty)
  }
  def apply(start: DateTime, end: DateTime, location: Location, title: String, description: String, sessionType: String, limit: String, authors: List[Author], ratings: List[Rating], comments: List[Comment], labels:Set[String]) = {
    new Session(nextSeq, start, end, location, title, description, sessionType, limit, authors, ratings, comments, labels)
  }

}
/**
 * Represents a comment for a session
 */
case class Comment(comment: String, userId: String)

/**
 * Represents a rating for a session
 */
case class Rating(rate:Int, userId:String)

/**
 * Represents an author
 */
case class Author(userId: String, mail: String, name: String) extends ToJsonSerializer[Author]

/**
 * Represents an author document wrapper.
 * This class is a case class, due to net.liftweb.mongodb requirements.
 */
case class AuthorDoc(_id: ObjectId, author: Author) extends MongoDocument[AuthorDoc] {
  def meta = AuthorDoc
}

/**
 * Defines the structure of a Facility.
 */
object AuthorDoc extends MongoDocumentMeta[AuthorDoc] {
  override def collectionName = "author"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all

  def apply(author: Author): AuthorDoc = {
    AuthorDoc(ObjectId.get, author)
  }

}

/**
 * Represents credentials used for authentication
 */
case class Credential(val user: String, val cryptedPassword: String) extends ToJsonSerializer[Credential]

/**
 * Represents a location, a physical space.
 */
case class Location(id: Long, description: String, capacity: Int) extends ToJsonSerializer[Location]

object Location extends FromJsonDeserializer[Location] {
  def apply(name: String, capacity: Int): Location = {
    Location(nextSeq.toInt, name, capacity)
  }
}

/**
 * Represents a collection of Locations belonging to a facility.
 * This class is a case class, due to net.liftweb.mongodb requirements.
 */
case class Facility(val _id: ObjectId, name: String, var locations: List[Location]) extends MongoDocument[Facility] {
  def meta = Facility

  def saveOrUpdate(location: Location) = {
    meta.doSaveOrUpdate(_id, "locations", locations, location)
    locations = location :: (locations - location)
    locations
  }

  def remove(location: Location) = {
    meta.doRemove(_id, "locations", location)
    locations = locations.filter(_.id != location.id)
    locations
  }
}

/**
 * Defines the structure of a Facility.
 */
object Facility extends MongoDocumentMeta[Facility] with EmbeddedDocumentOps[Facility] {
  override def collectionName = "facility"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all

  def apply(name: String, locations: List[Location]): Facility = {
    Facility(ObjectId.get, name, locations)
  }

}


/**
 * Represents a collection of Labels.
 * This class is a case class, due to net.liftweb.mongodb requirements.
 */
case class Labels(val _id: ObjectId, name: String, var labels: Set[String]) extends MongoDocument[Labels] {
  def meta = Labels

  def add(label: String) = {
    if(!labels.contains(label)) {
    meta.pushToArray(_id, "labels", new JString(label))
    labels = labels + label
    }
    labels
  }

}

/**
 * Defines the structure of a Label.
 */
object Labels extends MongoDocumentMeta[Labels] with EmbeddedDocumentOps[Labels] {
  override def collectionName = "labels"

  override def formats = (super.formats + new ObjectIdSerializer) ++ JodaTimeSerializers.all

  def apply(name: String, labels: Set[String]): Labels = {
    Labels(ObjectId.get, name, labels)
  }

}


/**
 * Listener for Session events
 */
trait SessionListener {
  
  def sessionUpdated(session:Session):Unit
}
