package com.xebia.xkeng.dao

import com.mongodb.Mongo
import com.mongodb.MongoOptions
import com.mongodb.ServerAddress

import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoDB

object RepositoryTestAssembly extends RepositoryComponent {


  val conferenceRepository = new ConferenceRepositoryImpl
  val sessionRepository = new SessionRepositoryImpl
}