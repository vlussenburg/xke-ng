package com.xebia.xkeng.model
import net.liftweb.http._
/**
 * Represents the logged in user
 */
object UserHolder {
  object LoggedInAuthor extends SessionVar[Author](null)

  def loggedInAuthor: Author = LoggedInAuthor.get

  def addToSession(author: Author) {
    LoggedInAuthor(author)
  }

  def isLoggedIn = LoggedInAuthor.is != null

  def logout = LoggedInAuthor(null)

}
