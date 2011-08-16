package com.xebia.xkeng.rest

import com.xebia.xkeng.dao._
import net.liftweb.util.Props
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.xebia.xkeng.model.{Session, Slot, Location, Conference}

object Assembly {

	var p1, p2, p3 : Session =  null.asInstanceOf[Session]

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

		Session.drop
		Conference.drop

		val today = new DateTime().hourOfDay.setCopy(16).minuteOfHour.setCopy(0)
		val prevWeek = today.minusWeeks(1)
		val nextWeek = today.plusWeeks(1)

		p1 = Session("Mongo rocks", "amooi@xebia.com", "Mongo is a paperless document database")
		p2 = Session("Scala rocks even more", "upeter@xebia.com", "Scala is a codeless programming language")
		p3 = Session("Scala quirks", "upeter@xebia.com", "No such thing as a free ride when doing scala")
		p1.save
		p2.save
		p3.save

		val dates = today :: prevWeek :: nextWeek :: Nil
		val sessions = p1 :: p2 :: Product13 :: Nil

		dates.map(createTestConference(_))
	}

	private def createTestConference(startDate: DateTime) = {

		val l1 = Location("Maup", 20)
		val l2 = Location("Laap", 30)
		val l3 = Location("Library", 10)

		val s1 = Slot(startDate, startDate.plusMinutes(60), l1, "Mongo rocks", "amooi@xebia.com", Option(p1._id))
		val s2 = Slot(startDate, startDate.plusMinutes(60), l2, "Scala rocks even more", "upeter@xebia.com", Option(p2._id))
		val s3 = Slot(startDate, startDate.plusMinutes(120), l2, "Scala quirks", "upeter@xebia.com", Option(p3._id))
		val c = Conference(ObjectId.get, "XKE", startDate, startDate.plusHours(4), List(s1, s2, s3), List(l1, l2, l3))
		c.save
	}
}