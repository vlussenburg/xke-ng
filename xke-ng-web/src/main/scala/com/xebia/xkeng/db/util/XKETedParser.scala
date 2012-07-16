package com.xebia.xkeng.db.util

import util.parsing.combinator.RegexParsers
sealed trait Part
case class Header(who: String, time: String, title: String, notes: String)
case class XKESession(who: String, minutes: Int, title: String, notes: String) extends Part
case class Break(breakType: String, minutes: Int) extends Part

object XKETedParser extends RegexParsers {

  override def skipWhitespace = false

  def onlyWord = "[A-Za-z. ]+".r
  def word = """[A-Za-z0-9.:/,'\?\!"\[\]~\(\) -]+""".r
  def spaties = " +".r
  def sep = "|"
  def headerSep = "||"
  def wikiNewLine = """[\\ ]*""".r
  def newline = "\n"
  def wikiStyle = """\s?\{.+?\}""".r
  def breakToken = """\s+:?(BREAK|DINER)\s+""".r
  def threeDigitNumber = """[0-9]{1,3}""".r

  def timeToken = opt(spaties) ~> threeDigitNumber <~ "m" <~ opt(spaties) ^^ {
    case t => t.toInt
  }

  /** header **/
  def header = headerSep ~> (word <~ headerSep) ~ (word <~ headerSep) ~ (word <~ headerSep) ~ (word <~ headerSep) <~ opt(newline) ^^ {
    case who ~ time ~ title ~ notes =>
      val tr = Header(who.trim, time.trim, title.trim, notes.trim); tr
  }

  /** break **/
  def break = sep ~> breakToken ~ (sep ~> timeToken) <~ sep <~ word <~ sep <~ word <~ sep <~ opt(newline) ^^ {
    case breakType ~ time =>
      val tr = Break(breakType.trim, time); tr
  }

  /** session **/
  def session = sep ~> (word <~ opt(wikiNewLine) <~ sep) ~ (timeToken <~ sep) ~ (opt(wikiStyle) ~> word <~ opt(wikiStyle) <~ opt(wikiNewLine) <~ sep) ~ opt(word) <~ opt(wikiNewLine) <~ sep <~ opt(newline) ^^ {
    case who ~ time ~ title ~ notes =>
      val tr = XKESession(who.trim, time, title.trim, notes.getOrElse("").trim); tr
  }

  /** Combine all**/
  def sessions = header ~> rep(break | session) ^^ { case ts => ts.toSeq }

  def ted(in: String): Seq[Part] = {
    parseSource(in, sessions)
  }

  def parseSource[T](in: String, parser: Parser[T]): T = parseAll(parser, in.mkString) match {
    case XKETedParser.Success(a, _) => a
    case x: XKETedParser.Failure => println(x); throw new IllegalStateException(x.toString)
  }

}