package com.xebia.xkeng.db.util

import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.xebia.xkeng.db._

@RunWith(classOf[JUnitRunner])
class XKETedParserTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  it should "parse header correctly" in {
    val txt = "|| Who || Time || Title || Notes ||\n"
    val ts = XKETedParser.parseSource(txt, XKETedParser.header)
    ts should be(Header("Who", "Time", "Title", "Notes"))
  }
  it should "parse breaks correctly" in {
    val txt = "| BREAK | 15m | | |\n"
    val res = XKETedParser.parseSource(txt, XKETedParser.break)
    val expected = Break("BREAK", 15)
    res should be(expected)
  }
  it should "parse a session correctly" in {
    val txt = """| [~kgeusebroek] | 5m | {color:#222222}Parsing: everybody should know it?{color} | About the use of Scala Parser Combinators \\ |"""
    val ts = XKETedParser.parseSource(txt, XKETedParser.session)
    val expected = XKESession("[~kgeusebroek]", 5, "Parsing: everybody should know it?", "About the use of Scala Parser Combinators")
    ts should be(expected)
  }
  it should "parse sessions correctly" in {
    val txt = io.Source.fromInputStream(getClass.getResourceAsStream("/xke_ted.txt")).mkString
    val ts = XKETedParser.ted(txt)
    println(ts)
  }

}
