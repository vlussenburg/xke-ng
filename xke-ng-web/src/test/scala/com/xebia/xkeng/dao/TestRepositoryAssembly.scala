package com.xebia.xkeng.dao

import com.xebia.xkeng.dao.{RepositoryComponent, ConferenceRepository}
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{Mongo, MongoOptions, ServerAddress}

object TestRepositoryAssembly extends RepositoryComponent {
  val srvr = new ServerAddress("127.0.0.1", 27017)
  val mo = new MongoOptions
  mo.socketTimeout = 10

  def init() =  MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), "xkeng")

  val conferenceRepository = new ConferenceRepositoryImpl
}