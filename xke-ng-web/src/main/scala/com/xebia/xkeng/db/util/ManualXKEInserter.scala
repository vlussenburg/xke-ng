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

object ManualXKEInserter extends RepositoryComponentImpl with MongoConnection {
  val fmt = ISODateTimeFormat.dateTime()
  val begin = fmt.parseDateTime("2012-06-19T14:00:00.000Z")

  def main(args: Array[String]) {
    init()

    val maup = createOrGetLocation("Maup")
    val laap = createOrGetLocation("Laap")
    val library = createOrGetLocation("Library")
    val meetingRoom = createOrGetLocation("Meeting Room")
    val upstairs = createOrGetLocation("Upstairs")
    val locations = List(maup, laap, library, meetingRoom, upstairs)

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

    val maup1 = resolveSession(begin, secondSlotBegin, maup, "WWDC 2012", "WWDC 2012 or what have we learned last week in San Francisco ", "Robert van Loghem")
    val laap1_2 = resolveSession(begin, dinnerBegin, laap, "Deployit 3.8 training", "Everything that is new since 3.0. Very much recommended for all middleware engineers.", "Vincent Partington")
    val laap2_4 = resolveSession(thirdSlotBegin, end, laap, "Deployit 3.8 training", "Everything that is new since 3.0. Very much recommended for all middleware engineers.", "Vincent Partington")
    val library1 = resolveSession(begin, secondSlotBegin, library, "Portfolio Level Agile Metrics", "Agile Metrics on Portfolio level.", "Rik de Groot")
    val meetingRoom1 = resolveSession(begin, secondSlotBegin, meetingRoom, "Book writing best practices", "Planning, process, self-discipline, editors, reviewers, iterations, etc.", "Raymond Roestenburg", "Adriaan de Jonge")

    val library2 = resolveSession(secondSlotBegin, dinnerBegin, library, "Mansal training for ACT", "Niels, Marcel en Remco will explain the Mansal training for ACT.", "Edwin Oldenbeuving", "Remco Dijkxhoorn")
    val meetingRoom2 = resolveSession(secondSlotBegin, dinnerBegin, meetingRoom, "Workshop - Large Scale JavaScript Application Architectures ", "We would like to define the goals, means  and tasks required to achieve our goal by 31 dec 2012. Defining the XITA curriculum for 2012 and beyond. Start with Large Scale JavaScript Application Architectures in a first session and cover Large Scale Cloud Application Architectures in a follow-up XKE session.", "Gero Vermaas", "Mark van Holsteijn", "Adriaan de Jonge")
    val maup2 = resolveSession(secondSlotBegin, dinnerBegin, maup, "Hadoop: No more Single Point of Failure! ", "Closer look at recent developments around the Hadoop NameNode: High Availability & Federation. Assuming basic familiarity with Hadoop, HDFS, etc.", "Joris Bontje")

    val maup3 = resolveSession(thirdSlotBegin, fourthSlotBegin, maup, "Sharecompany architecture: processing 1 million messages per second.", "Sharecompany processes stock price information in real time and built a system to deal with the information overload. This talk is about the architecture that handles 1M messages/sec, which was developed in house and is being moved to production currently. This is a informational talk and a request for constructive feedback on the solution.", "Friso van Vollenhoven")
    val library3 = resolveSession(thirdSlotBegin, fourthSlotBegin, library, "Agile Architectuur, het vervolg", "Lessons learned XebiCon 2012. Reaching managers, making it practical", "Herbert Schuurmans", "Adriaan de Jonge")
    val meetingRoom3_4 = resolveSession(thirdSlotBegin, end, meetingRoom, "Verhaal van de Scrum masters bij AEGON", "De klant vertelt: Verhaal van de Scrum masters bij AEGON door Wiger Middelkamp en Daniel Wiersma (uitnodiging door Menno van Eekelen)")

    val upstairs4 = resolveSession(fourthSlotBegin, end, upstairs, "Xebia & Scala: Next Moves", "Scala is gaining traction inside and outside Xebia. The recent accomplishments are quite impressive: We have successfully completed 2-? projects with Scala, we do Scala consultancy, we are writing a book, we gave the first Fast Track to Scala course, we ran a DUSE meetup with ~50 people. We're getting somewhere but are not there yet. In this session we'll formulate the goals we want to achieve with Scala and define the roadmap for the rest of 2012, which will take us there.", "Urs Peter")
    val library4 = resolveSession(fourthSlotBegin, end, library, "Alpe d'HuZes 2012", "Report: Alpe d'HuZes 2012", "Martin van Steenis", "Gero Vermaas")
    val maup4 = resolveSession(fourthSlotBegin, end, maup, "Modelgebaseerde testautomatisering - theorie en Demo", "Model your domain, model your use cases, the tool generates all the testcases that can be calculated, including appropriate testdata. Nifty stuff!")

    val dinner = Session(dinnerBegin, thirdSlotBegin, maup, "Dinner", "", "BREAK", "Unlimited")
    val sess = List(maup1, laap1_2, laap2_4, library1, dinner, meetingRoom1, maup2, library2, meetingRoom2, upstairs4, maup3, library3, meetingRoom3_4, maup4, library4)
    val title = "XKE Maurits/Adé/Barend"
    replaceConferenceWithSessions(title, begin, schedule, sess, locations)
  }

  def resolveSession(begin: DateTime, end: DateTime, location: Location, title: String, content: String, authors: String*) = {
    val authList = authors.flatMap { name =>
      val authorOpt = authorRepository.findAuthorByName(name)
      println("Resolved author " + name + " is " + authorOpt.getOrElse("?"))
      authorOpt.map(a => List(a)).getOrElse(Nil)
    }

    Session(begin, end, location, title, content, "STANDARD", "Unlimited", authList.toList)

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