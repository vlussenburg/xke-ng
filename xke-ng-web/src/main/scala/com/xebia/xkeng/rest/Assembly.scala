package com.xebia.xkeng.rest

import com.xebia.xkeng.dao._
import net.liftweb.util.Props
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}

object Assembly  {


  object XKENGDispatchAPIAssembly  extends XKENGDispatchAPI with RepositoryComponent {
      val conferenceRepository = new ConferenceRepositoryImpl
      val sessionRepository = new SessionRepositoryImpl
  }


  def initMongoDB() = {
    val srvr = new ServerAddress(Props.get("mongo.host").getOrElse("127.0.0.1"), Props.get("mongo.port").map(_.toInt).getOrElse(27017))
    val mo = new MongoOptions
    mo.socketTimeout = Props.get("mongo.socket.timeout").map(_.toInt).getOrElse(10)
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo),Props.get("mongo.db.name").getOrElse("xkeng"))
  }


}