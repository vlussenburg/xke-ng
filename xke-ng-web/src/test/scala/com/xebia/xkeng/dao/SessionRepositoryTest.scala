package com.xebia.xkeng.dao

import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import org.joda.time.DateTime
import com.xebia.xkeng.model.Session
import com.xebia.xkeng.dao.RepositoryTestAssembly._

/*
 * Created by IntelliJ IDEA.
 * User: svdberg
 * Date: 10-08-11
 * Time: 11:12
 */
@RunWith(classOf[JUnitRunner])
class SessionRepositoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var exampleSession:Session = null

  override def beforeEach() {
    init()
    exampleSession = createTestSession()
  }

  override def afterEach() {
    if (exampleSession != null)
      exampleSession.delete
  }

  private def createTestSession() = {
    //create a new example session
    val s = Session("test session", "Sander", "something nice")
    s.save
    s
  }

  it should "find session by id" in {
    val sess = sessionRepository.findSession(exampleSession._id.toString)
    sess should not be (None)
  }

  it should "delete session with id" in  {
    val sess = sessionRepository.findSession(exampleSession._id.toString)
    sess.map(_.delete)
    val newSess = sessionRepository.findSession(exampleSession._id.toString)
    newSess should be (None)
  }
}