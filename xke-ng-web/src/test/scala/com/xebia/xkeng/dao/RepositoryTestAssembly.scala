package com.xebia.xkeng.dao

import com.mongodb.Mongo
import com.mongodb.MongoOptions
import com.mongodb.ServerAddress

import net.liftweb.mongodb.DefaultMongoIdentifier
import net.liftweb.mongodb.MongoDB
import org.specs.mock.Mockito
import org.specs.specification._

object RepositoryTestAssembly extends RepositoryComponent with Mockito with DefaultExampleExpectationsListener{

  val conferenceRepository = new ConferenceRepositoryImpl
  val sessionRepository = new SessionRepositoryImpl
  val facilityRepository = new FacilityRepositoryImpl
  val authorRepository = new AuthorRepositoryImpl
  val labelRepository = new LabelRepositoryImpl
  val authenticationRepository =   mock[AuthenticationRepository]
}