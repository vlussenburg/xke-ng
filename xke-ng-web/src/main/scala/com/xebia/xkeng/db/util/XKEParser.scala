package com.xebia.xkeng.db.util

import util.parsing.combinator.RegexParsers
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime

case class TmpSession(begin: DateTime, end: DateTime, title: String, summary:String, authors:List[String], location: String = "")

object XKEParser extends RegexParsers {

  val fmt = ISODateTimeFormat.dateTime()
  val timeTemplate = "2012-07-17T%s:00.000Z"

  override def skipWhitespace = false

  def onlyWord = "[A-Za-z. ]+".r
  def word = """[A-Za-z0-9.:/,'\?\!"~\(\) &-]+""".r
  def spaties = " +".r
  def sep = "|"
  def headerSep = "||"
  def wikiNewLine = """[\\ ]*""".r
  def newline = "\n"
  def wikiStyle = """\s?\{.+?\}""".r
  def timeToken = """\d\d:\d\d""".r
  def bold = """[\s]*[\*][\s]*""".r
  def openLink = """[\s]*[\[][\s]*""".r
  def closeLink = """[\s]*[\]][\s]*""".r
  def by = """[\s]*:?by[\s]*""".r
  def authLinkStart = """\[~""".r
  def authLinkEnd = """\]""".r
  def invitedBy = """\s*:?(invited by)\s*""".r

  /** header **/
  def header = word <~ headerSep ^^ {
    case location => location.trim
  }

  def headers = headerSep ~> word ~> headerSep ~> rep(header) <~ opt(newline) ^^ {
    case locations => locations
  }
  /** time */
  def time = sep ~> opt(spaties) ~> (timeToken <~ "-") ~ timeToken <~ opt(spaties) <~ sep ^^ {
    case from ~ to => toDateTime(from) -> toDateTime(to)
  }

  /** session */
  def session = (emptySess | filledSess) ^^ {
    case session => session
  }

  def emptySess = spaties <~ sep <~ opt(newline) ^^ {
    case empty => None
  }

  def filledSess = (sessTitle <~ wikiNewLine) ~ (sessSummary <~ wikiNewLine) ~ sessAuthors <~ opt(wikiNewLine) <~ sep <~ opt(newline) ^^ {
    case title ~ summary ~ author => Some((title, summary, author))
  }

  def link = openLink ~> (word <~ sep) ~ word <~ closeLink ^^ {
    case name ~ href => (name, href)
  }

  def sessTitle:Parser[String] = ((bold ~> word <~ bold) | link) ^^ {
    case title => title match {
      case (name, href) => name.toString.trim
      case title => title.toString.trim
    }
  }

  def sessSummary = opt(wikiNewLine) ~> rep(word | link) ^^ {
    case summary => summary.map{
      case (name, link) => name.toString
      case sumElement => sumElement
    } mkString
  }

  def sessAuthor = opt(wikiNewLine) ~> opt(by) ~> authLinkStart ~> word <~ authLinkEnd ^^ {
    case authorAbbreviation => authorAbbreviation.trim
  }

  def sessAuthors = rep(sessAuthor | opt(by)  ~> word) <~ opt(invitedBy <~ sessAuthor) ^^ {
    case authors => authors
  }
  
  /** slot */
  def slot = time ~ rep(session) ^^ {
    case (begin, end) ~ sessions => sessions.collect{case Some((title, summary, authors)) => TmpSession(begin, end, title, summary, authors)}

  }
/** all slots */
  def sessions = headers ~ rep(slot) ^^ {
    case locations ~ slots => {
      val slotsWithLocs = slots.map(_ zip locations)
      for {
        slot <- slotsWithLocs
        (sess, loc) <- slot
      } yield sess.copy(location = loc)
    }
  }

/** combine all */
  def parseSource[T](in: String, parser: Parser[T]): T = parseAll(parser, in.mkString) match {
    case XKEParser.Success(a, _) => a
    case x: XKEParser.Failure => println(x); throw new IllegalStateException(x.toString)
  }

  def toDateTime(HHMM: String) = {
    fmt.parseDateTime(timeTemplate format HHMM).minusHours(2)
  }

}