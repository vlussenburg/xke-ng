package com.xebia.xkeng.db.util
import com.atlassian.crowd.exception._
import net.liftweb.util.Helpers._
import com.atlassian.crowd.model.user.User
import net.liftweb.util.Props
import com.xebia.xkeng.assembly.Assembly._
import com.xebia.xkeng.model.{ Session, Location, Conference, Author }
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import com.xebia.xkeng.model.SlotInfo

object XKEInserter extends RepositoryComponentImpl with MongoConnection {
  val fmt = ISODateTimeFormat.dateTime()
  val begin = fmt.parseDateTime("2012-07-17T14:00:00.000Z")

  def main(args: Array[String]) {
    init()

    val maup = createOrGetLocation("Maup")
    val laap = createOrGetLocation("Laap")
    val library = createOrGetLocation("Library")
    val meetingRoom = createOrGetLocation("Meeting Room")
    val upstairs = createOrGetLocation("Upstairs")
    val locations = List(maup, laap, meetingRoom, library, upstairs)
    val locationsMap = Map("Maupertuus hall" -> maup, "Maup" -> maup, "Laapersveld hall" -> laap, "Laap" -> laap, "Meeting Room" -> meetingRoom, "Maupertuus meeting room" -> meetingRoom, "Library" -> library, "Upstairs" -> upstairs, "Meeting Room Upstairs" -> upstairs)

    val secondSlotBegin = begin.plusHours(1)
    val dinnerBegin = secondSlotBegin.plusHours(1)
    val thirdSlotBegin = dinnerBegin.plusHours(1)
    val fourthSlotBegin = thirdSlotBegin.plusHours(1)
    val end = fourthSlotBegin.plusHours(1)

    val slot1 = SlotInfo(begin, secondSlotBegin, "Session")
    val slot2 = SlotInfo(secondSlotBegin, dinnerBegin, "Session")
    val dinnerSlot = SlotInfo(dinnerBegin, thirdSlotBegin, "Dinner")
    val slot3 = SlotInfo(thirdSlotBegin, fourthSlotBegin, "Session")
    val slot4 = SlotInfo(fourthSlotBegin, end, "Session")
    val schedule = List(slot1, slot2, dinnerSlot, slot3, slot4)

    val title = "XKE"
  val diner = Session(dinnerBegin, thirdSlotBegin, maup, "Diner", "Diner", "STANDARD", "Unlimited", Nil)

    val raw = XKEParser.parseSource(txt, XKEParser.sessions)
    val sess = raw.map(rawSess => resolveSession(rawSess, locationsMap)) :+ diner
    replaceConferenceWithSessions(title, begin, schedule, sess, locations)
  }

  val txt = "|| Time || Maupertuus hall || Laapersveld hall || Maupertuus meeting room ||" +
    "| 16:00-16:55 | [All you wanted to know about systems availability, but never dared to ask|Presentation - All you wanted to know about systems availability, but never dared to ask] \\ To elaborate on the intrisic relationship between availability, failures, mean time to repair, mean between failures, root cause analysis and defect prevention. \\by [~mvanholsteijn]\\ | *Deployit* \\Q&A \\XebiaLabs| *The limits of Agile* \\ACT welcomes [AgileBen|http://www.agileben.com/] and discusses the limits of Agile. Does it work homeopathically, or only at cask strength? |" +
    "| 17:05-18:00 | *Cloud development using Play, Scala and OpenShift* \\This is a demo session where I will show how easy it is to develop an application using an online cloud platform. The choice for Play and Scala is only there to make it even more fun \\by [~jdewinne]\\ | *Deployit (continued)* \\ Q&E \\XebiaLabs | *Technical Debt On The Backlog* \\Technical Debt On The Backlog, Doom Or Boon?\\by [~mrijk] |" +
    "| 19:00-19:55 | *Security in software development* \\Martin Knobloch Security Consultant bij Pervasive Security, Dutch Chapter Lead OWASP en Global Education Committee Chair OWASP. Deze brainstorm sessie is voor iedereen die betrokken is bij ontwikkelprojecten. Van architect, designer tot ontwikkelaar en tester. Met een andere bril naar functionaliteit kijken. Wat is security en hoe implementeer ik functionaliteit op een veilige manier. \\by Martin Knobloch | *Network analysis using Hadoop en Neo4j* \\Re-run of my [2012 Berlin Buzzwords talk|http://berlinbuzzwords.de/sessions/serious-network-analysis-using-hadoop-and-neo4j], but with extended code walk-through. \\  [~fvanvollenhoven] \\ | [Microsoft Azure technical deep dive|Presentation - Microsoft Azure technical deep dive]\\To provide insight into the Azure platform and communicate that it is a hetrogenuous platform. \\ by Astrid Hackenberg|" +
    "| 20:05-21:00 | [Presentation on Design and Engineering Methodology of Organisations (DEMO)|Presentation on Design and Engineering Methodology of Organisations (DEMO)]\\Ralph Nijpels will share with you an excerpt (and rather more specific) version of the lecture he gave last May at the Radbout University . In this presentation we show how a linguistics-based model of Air France - KLM Cargo looks. How can it be used for a number of important concepts to interconnect. \\by Ralph Nijpels \\ | | *Ziggo proposal* \\Ziggo proposal \\by [~rdijkxhoorn] [~eoldenbeuving] [~mvanbenthem] \\ |"

  def resolveSession(tmpSess: TmpSession, locations: Map[String, Location]) = {
    val authList = tmpSess.authors.flatMap { name =>
      val userId =name.toLowerCase.replace(" ", "")
      val authorOpt = authorRepository.findAuthorById(userId)
      println("Resolved author " + name + " is " + authorOpt.getOrElse("?"))
      authorOpt match {
        case Some(author) => List(author)
        case None if (!name.trim.isEmpty()) =>
          val author = Author(userId, "unkown@unkown.com", name)
          authorRepository.addAuthor(author)
          List(author)
        case _ => Nil
      }
    }
    val location = locations(tmpSess.location)
    Session(tmpSess.begin, tmpSess.end, location, tmpSess.title, tmpSess.summary, "STANDARD", "Unlimited", authList.toList)
  }


  
  def replaceConferenceWithSessions(title: String, startDate: DateTime, schedule: List[SlotInfo], sess: List[Session], locations: List[Location]) = {
    val (year, month, day) = (startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth())
    println("%04d-%02d-%02d" format (year, month, day))
    val conf = conferenceRepository.findConferences(year, month, day)
    if (conf.size == 1) {
      println("remove: " + conf)
      conf(0).delete
    } else
      println("no existing conf found")

    val c = Conference(title, startDate, startDate.plusHours(5), sess, locations, schedule)
    c.save
    println("Saved " + c)
  }

  def createOrGetLocation(name: String): Location = {
    facilityRepository.findLocationByName(name) match {
      case Some(l) => l
      case None => {
        val l = Location(name, 30)
        facilityRepository.addLocation(l)
        l
      }
    }
  }

}