package com.xebia.xkeng.rest

import com.xebia.xkeng.dao._
import net.liftweb.util.Props
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.xebia.xkeng.model.{Session, Location, Conference, Author}

object Assembly {

	object XKENGDispatchAPIAssembly extends XKENGDispatchAPI with RepositoryComponent {
		val conferenceRepository = new ConferenceRepositoryImpl
		val sessionRepository = new SessionRepositoryImpl
	}


	def initMongoDB() = {
		val srvr = new ServerAddress(Props.get("mongo.host").getOrElse("127.0.0.1"), Props.get("mongo.port").map(_.toInt).getOrElse(27017))
		val mo = new MongoOptions
		mo.socketTimeout = Props.get("mongo.socket.timeout").map(_.toInt).getOrElse(10)
		MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), Props.get("mongo.db.name").getOrElse("xkeng"))
	}

	def purgeAndPushTestdata() = {


		Conference.drop

		val today = new DateTime().hourOfDay.setCopy(16).minuteOfHour.setCopy(0)
		val prevWeek = today.minusWeeks(1)
		val nextWeek = today.plusWeeks(1)

		val dates = today :: prevWeek :: nextWeek :: Nil

		dates.map(createTestConference(_))
	}

	private def createTestConference(startDate: DateTime) = {

		val l1 = Location("Maup", 20)
		val l2 = Location("Laap", 30)
		val l3 = Location("Library", 10)

    val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
    val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")


		val s1 = Session(startDate, startDate.plusMinutes(60), l1, "Mongo rocks", "Mongo is a paperless document database", "STRATEGIC", "10 people", List(a1))
		val s2 = Session(startDate, startDate.plusMinutes(60), l2, "Scala rocks even more", "Scala is a codeless programming language", "STRATEGIC","10 people", List(a2))
		val s3 = Session(startDate, startDate.plusMinutes(120), l2, "Scala quirks", "No such thing as a free ride when doing scala", "STRATEGIC", "10 people", List(a1, a2))
		val c = Conference(ObjectId.get, "XKE", startDate, startDate.plusHours(4), List(s1, s2, s3), List(l1, l2, l3))
		c.save
	}
}