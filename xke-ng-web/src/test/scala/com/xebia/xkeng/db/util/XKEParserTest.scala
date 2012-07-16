package com.xebia.xkeng.db.util

import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.xebia.xkeng.db._

@RunWith(classOf[JUnitRunner])
class XKEParserTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  it should "parse header correctly" in {
    val txt = "|| Time || Maup || Laap || Library || Meeting room || Upstairs ||\n"
    val ts = XKEParser.parseSource(txt, XKEParser.headers)
    ts should be(List("Maup", "Laap", "Library", "Meeting room", "Upstairs"))
  }
  it should "parse time correctly" in {
    val txt = "| 16:00-16:55 |"
    val res = XKEParser.parseSource(txt, XKEParser.time)
    println(res)
    true
  }
  it should "parse session correctly" in {
    val txt = " *Title*\\Summary [link|link loc]\\by [~author] |"
    val res = XKEParser.parseSource(txt, XKEParser.session)
    println(res)
    true
  }
  it should "parse session title correctly" in {
    val txt = " *This is the title * "
    val res = XKEParser.parseSource(txt, XKEParser.sessTitle)
    res should be("This is the title")
  }
  it should "parse whole session correctly" in {
    val txt = """ *Network analysis using Hadoop en Neo4j* \\ Re-run of my [2012 Berlin Buzzwords talk|http://berlinbuzzwords.de/sessions/serious-network-analysis-using-hadoop-and-neo4j], but with extended code walk-through. \\  [~fvanvollenhoven] \\ |"""
    val Some((title, summary, autors)) = XKEParser.parseSource(txt, XKEParser.filledSess)
    title should be("Network analysis using Hadoop en Neo4j")
    summary should be("Re-run of my 2012 Berlin Buzzwords talk, but with extended code walk-through. ") 
  }
  it should "parse link session title correctly" in {
    val txt = " [This is a link title | the link ]"
    val res = XKEParser.parseSource(txt, XKEParser.sessTitle)
    res should be("This is a link title")
  }
  it should "parse slots correctly" in {
    val txt = "|| Time || Maupertuus hall || Laapersveld hall || Maupertuus meeting room ||\n" +
      "| 16:00-16:55 |  *Title*\\Summary [link|link loc]\\by [~author] | *Title*\\Summary [link|link loc]\\by [~author] | *Title*\\Summary [link|link loc]\\by [~author]|\n" +
      "| 17:05-18:00 |  *Title*\\Summary [link|link loc]\\by [~author] | *Title*\\Summary [link|link loc]\\by [~author] | |"
    val res = XKEParser.parseSource(txt, XKEParser.sessions)
    println(res)
    true
  }
  it should "parse real slots correctly" in {
    val txt = "|| Time || Maupertuus hall || Laapersveld hall || Maupertuus meeting room ||" +
      "| 16:00-16:55 | [All you wanted to know about systems availability, but never dared to ask|Presentation - All you wanted to know about systems availability, but never dared to ask] \\ To elaborate on the intrisic relationship between availability, failures, mean time to repair, mean between failures, root cause analysis and defect prevention. \\by [~mvanholsteijn]\\ | *Deployit* \\Q&A \\XebiaLabs| *The limits of Agile* \\ACT welcomes [AgileBen|http://www.agileben.com/] and discusses the limits of Agile. Does it work homeopathically, or only at cask strength? |" +
      "| 17:05-18:00 | *Cloud development using Play, Scala and OpenShift* \\This is a demo session where I will show how easy it is to develop an application using an online cloud platform. The choice for Play and Scala is only there to make it even more fun \\by [~jdewinne]\\ | *Deployit (continued)* \\ Q&E \\XebiaLabs | *Technical Debt On The Backlog* \\Technical Debt On The Backlog, Doom Or Boon?\\by [~mrijk] |" +
      "| 19:00-19:55 | *Security in software development* \\Martin Knobloch Security Consultant bij Pervasive Security, Dutch Chapter Lead OWASP en Global Education Committee Chair OWASP. Deze brainstorm sessie is voor iedereen die betrokken is bij ontwikkelprojecten. Van architect, designer tot ontwikkelaar en tester. Met een andere bril naar functionaliteit kijken. Wat is security en hoe implementeer ik functionaliteit op een veilige manier. \\by Martin Knobloch invited by [~hjjacobs] | *Network analysis using Hadoop en Neo4j* \\Re-run of my [2012 Berlin Buzzwords talk|http://berlinbuzzwords.de/sessions/serious-network-analysis-using-hadoop-and-neo4j], but with extended code walk-through. \\  [~fvanvollenhoven] \\ | [icrosoft Azure technical deep dive|Presentation - Microsoft Azure technical deep dive]\\To provide insight into the Azure platform and communicate that it is a hetrogenuous platform. \\ by Astrid Hackenberg invited by [~mvanholsteijn] |" +
      "| 20:05-21:00 | [Presentation on Design and Engineering Methodology of Organisations (DEMO)|Presentation on Design and Engineering Methodology of Organisations (DEMO)]\\Ralph Nijpels will share with you an excerpt (and rather more specific) version of the lecture he gave last May at the Radbout University . In this presentation we show how a linguistics-based model of Air France - KLM Cargo looks. How can it be used for a number of important concepts to interconnect. \\by Ralph Nijpels invited by [~mvanholsteijn]\\ | | *Ziggo proposal* \\Ziggo proposal \\by [~rdijkxhoorn] [~eoldenbeuving] [~mvanbenthem] \\ |"
    val res = XKEParser.parseSource(txt, XKEParser.sessions)
    println(res)
    true
  }

  /*
  it should "parse breaks correctly" in {
    val txt = "| BREAK | 15m | | |\n"
    val res = XKEParser.parseSource(txt, XKEParser.break)
    val expected = Break("BREAK", 15)
    res should be(expected)
  }
  it should "parse a session correctly" in {
    val txt = """| [~kgeusebroek] | 5m | {color:#222222}Parsing: everybody should know it?{color} | About the use of Scala Parser Combinators \\ |"""
    val ts = XKEParser.parseSource(txt, XKEParser.session)
    val expected = Session("[~kgeusebroek]", 5, "Parsing: everybody should know it?", "About the use of Scala Parser Combinators")
    ts should be(expected)
  }
  it should "parse sessions correctly" in {
    val txt = io.Source.fromInputStream(getClass.getResourceAsStream("/xke_ted.txt")).mkString
    val ts = XKETedParser.ted(txt)
    println(ts)
  }
  */
  /*
|| Time || Maupertuus hall || Laapersveld hall || Maupertuus meeting room ||
| 16:00-16:55 |  Session1 | Session2 | Session 3|


session title: *title* or [link|link] 

| 16:00-16:55 | [Presentation - All you wanted to know about systems availability, but never dared to ask|Presentation - All you wanted to know about systems availability, but never dared to ask] by \\
[~mvanholsteijn]\\ | Deployit&nbsp;Q&A \\XebiaLabs | Technical Debt On The Backlog, Doom Or Boon? (Title by [~lbonnema]) by [~mrijk] |



|| Time || Maup || Laap ||  Library || Meeting room ||  Upstairs ||
| 16:00-16:55 |  | *Deployit 3.8 training:* Everything that is new since 3.0.  Very much recommended for all middleware engineers.  [~vpartington] | Agile Metrics on Portfolio level \\  [~rdegroot] | *Book writing best practices:*  Planning, process, self-discipline, editors, reviewers, iterations, etc.  Met externe spreker: Nicole van der Steen  [~rroestenburg] & [~adejonge]   |  |
| 17:05-18:00 | ﻿[Modelgebaseerde testautomatisering (Machiel vd Bijl, Axini) theorie en Demo|Modelgebaseerde testautomatisering] TL;DR: &nbsp;model your domain, model your use cases, the tool generates all the testcases that can be calculated, including appropriate testdata.&nbsp;  Look inside (follow link) for whitepaper  Nifty stuff\! | *Deployit 3.8 training continued* | *Agile adoption offer for Ziggo*  The Ziggo offer contains our latest thinking in how to present Agile to client management. We present the offer and the reasoning behind it. The offer will be mailed prior to the session.  [~eoldenbeuving] & [~rdijkxhoorn] | [*Workshop - Large Scale JavaScript Application Architectures*|knowledge:Workshop - Large Scale JavaScript Application Architectures]&nbsp;\- by [~gvermaas],&nbsp;[~mvanholsteijn] en [~adejonge] | *Hadoop: No more Single Point of Failure\!*  Closer look at recent developments around the Hadoop NameNode: High Availability & Federation.  Assuming basic familiarity with Hadoop, HDFS, etc.  [~jbontje] |
| 18:00-19:00 | Diner | \\ | \\ | 18:45\- 19:00 Afscheid RanVijay | |
| 19:00-19:55 | Sharecompany architecture: processing 1 million messages per second.  Sharecompany processes stock price information in real time and built a system to deal with the information overload. This talk is about the architecture that handles 1M messages/sec, which was developed in house and is being moved to production currently. This is a informational talk and a request for constructive feedback on the solution. \\  [~fvanvollenhoven] | *Deployit 3.8 training continued* | *Agile Architectuur, het vervolg*  Lessons learned XebiCon 2012  Reaching managers  Making it practical  [~hschuurmans] en [~adejonge] | De klant vertelt: [Verhaal van de Scrum masters bij AEGON] door Wiger Middelkamp en Daniel Wiersma&nbsp;  (uitnodiging door&nbsp;[~mvaneekelen]) | \\ |
| 20:05-21:00 | [Xebia & Scala: Next Moves|Xebia Scala Next Moves]  Scala is gaining traction inside and outside Xebia. The recent accomplishments are quite impressive:  We have successfully completed 2-? projects with Scala, we do Scala consultancy, we are writing a book, we gave the first Fast Track to Scala course, we ran a DUSE meetup with \~50 people. \\   We're getting somewhere but are not there yet.  In this session we'll formulate the goals we want to achieve with Scala and define the roadmap for the rest of 2012, which will take us there. \\  [~upeter] | *Deployit 3.8 training continued* | *Alpe d'HuZes 2012* \\  [~mvansteenis] & [~gvermaas] | De klant vertelt: [Verhaal van de Scrum masters bij AEGON] door Wiger Middelkamp en Daniel Wiersma&nbsp;  (uitnodiging door&nbsp;[~mvaneekelen]) | \\ |

*/
}
