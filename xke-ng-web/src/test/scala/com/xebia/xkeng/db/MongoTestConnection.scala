package com.xebia.xkeng.dao
import com.mongodb.Mongo
import com.mongodb.MongoOptions
import com.mongodb.ServerAddress

import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoDB

trait MongoTestConnection {
  val srvr = new ServerAddress("127.0.0.1", 27017)
  val mo = new MongoOptions
  mo.socketTimeout = 100

  def init() =  MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr, mo), "xkeng_test")
}