package com.xebia.xkeng.db
import com.xebia.xkeng.dao.RepositoryTestAssembly._
import com.atlassian.crowd.exception._
import net.liftweb.util.Helpers._
import com.atlassian.crowd.model.user.User
import net.liftweb.util.Props
import com.xebia.xkeng.model._
import com.xebia.xkeng.dao.MongoTestConnection

object UserInserter extends MongoTestConnection {
  private val crowdSysUser = Props.get("crowd.sysuser").get
  private val crowdSysUserPwd = Props.get("crowd.sysuser.pwd").get
  private val crowdBase = Props.get("crowd.base.url").get
  private val aUser = new String(hexDecode("49726f716f78783437"))
  private val aPwd = new String(hexDecode("757065746572"))

  def main(args: Array[String]) {
    init()
    val (_, client) = AuthenticationRepository.createCrowdClient(crowdBase, crowdSysUser, crowdSysUserPwd)
    import collection.JavaConversions._
    val users = client.getUsersOfGroup("allnl", 0, 200)
    println(users.size)
    users.foreach(updateAuthors)
  }

  private def updateAuthors(user: User): Author = {
    authorRepository.findAuthorById(user.getName) match {
      case None => {
        val author = Author(user.getName(), user.getEmailAddress(), user.getDisplayName())
        authorRepository.addAuthor(author)
        println("User %s added to list of authors" format author)
        author
      }
      case Some(author) => author
    }
  }

}