package com.xebia.xkeng.dao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
import org.joda.time.DateTime
import com.xebia.xkeng.model._
import org.joda.time.format._
import com.xebia.xkeng.dao.RepositoryTestAssembly._

@RunWith(classOf[JUnitRunner])
class AuthorRepositoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MongoTestConnection {

  val a1 = Author("peteru", "upeter@xebia.com", "Urs Peter")
  val a2 = Author("amooy", "amooy@xebia.com", "Age Mooy")

  override def beforeEach() {
    init()
    AuthorDoc.drop
    createTestAuthors();
  }

  private def createTestAuthors() = {  
    List(a1, a2).map(authorRepository.addAuthor(_))
  }

  it should "find All authors" in {
    val authors = authorRepository.findAllAuthors
    authors.size should be(2)
  }
  it should "update existing author" in {
    authorRepository.updateAuthor(a1.copy(name = "Urs Peter 2"))
    val changed = authorRepository.findAuthorByName("Urs Peter 2")
    changed should not be (None)
  }
  it should "add new author" in {
    val a3 = Author("mleeuwen", "mleeuwen@xebia.com", "Michael van Leeuwen")
    authorRepository.addAuthor(a3)
    val changed = authorRepository.findAuthorByName(a3.name)
    changed should not be (None)
  }
  it should "not create existing author" in {
    intercept[IllegalArgumentException] {
      authorRepository.addAuthor(a2)
    }
  }
  it should "remove author" in {
    authorRepository.removeAuthor(a2)

  }
  type ? = this.type
}