package com.xebia.xkeng.db.util
import com.atlassian.crowd.exception._
import net.liftweb.util.Helpers._
import com.atlassian.crowd.model.user.User
import net.liftweb.util.Props
import com.xebia.xkeng.assembly.Assembly._
import com.xebia.xkeng.model.{ Session => XKESession, Location, Conference, Author }
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import com.xebia.xkeng.model.SlotInfo

object XKETedInserter extends RepositoryComponentImpl with MongoConnection {
  val fmt = ISODateTimeFormat.dateTime()

  def main(args: Array[String]) {
    init()
    val startDate = fmt.parseDateTime("2012-04-24T16:00:00.000Z")
    val parts = parseParts()
    val l = createOrGetLocation("Laapersveld")
    val sess = processParts(parts, startDate, l)
    val overallTED = XKESession(sess.last.end, sess.last.end.plusMinutes(1), l, "[Overall Impression TED]", "Please provide a rating for your overall impression of this TED XKE", "TED", "Unlimited")
    val overallApp = XKESession(sess.last.end, sess.last.end.plusMinutes(1), l, "[Feedback about this App]", "Please provide a rating about the usage of this application", "TED", "Unlimited")
    replaceConferenceWithSessions(sess :+ overallTED :+ overallApp, startDate, l)
  }

  def replaceConferenceWithSessions(sess: List[XKESession], startDate: DateTime, l: Location) = {
    val (year, month, day) = (startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth())
    println("%04d-%02d-%02d" format (year, month, day))
    val conf = conferenceRepository.findConferences(year, month, day)
    if (conf.size == 1) {
      println("remove: " + conf)
      conf(0).delete
    } else
      println("no existing conf found")

    val schedule = createSchedule(startDate)
    val c = Conference("XKE TED", startDate, startDate.plusHours(5), sess, List(l), schedule)
    c.save
    println("Saved " + c)
  }

  def createSchedule(startDate: DateTime) = {
    def createRecursively(lengths:List[(Int, String)], startDate:DateTime):List[SlotInfo] = {
      lengths match {
        case (length, sessionType) :: tail => {
          val end = startDate.plusHours(length);
          SlotInfo(startDate, end, sessionType) :: createRecursively(tail, end)
        }
        case _ => Nil
      }
    }
    val lengths = List((2, "Session"),(1, "Diner"),(2, "Session"))
    createRecursively(lengths, startDate)
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

  def processParts(parts: Seq[Part], start: DateTime, l: Location): List[XKESession] = {
    parts match {
      case head :: tail => {
        val (s, end) = createSession(head, start, l)
        s match {
          case Some(session) => session :: processParts(tail, end, l)
          case None => processParts(tail, end, l)
        }
      }
      case _ => Nil
    }
  }

  private def createSession(s: Part, start: DateTime, l: Location): (Option[XKESession], DateTime) = {
    s match {
      case s: Session => {
        val authorOpt = authorRepository.findAuthorByName(s.who)
        val end = start.plusMinutes(s.minutes)
        val s1 = XKESession(start, end, l, s.title, s.notes, "TED", "Unlimited", authorOpt.map(a => List(a)).getOrElse(Nil))
        (Some(s1), end)
      }
      case Break("BREAK", minutes) => {
        val end = start.plusMinutes(minutes)
        (None, end)
      }
      case b @ Break("DINER", _) => {
        val start = fmt.parseDateTime("2012-04-24T18:00:00.000Z")
        val end = start.plusMinutes(b.minutes)
        val s1 = XKESession(start, end, Location("Maup", 30), b.breakType, b.breakType, "TED", "Unlimited")
        (Some(s1), end)
      }
    }
  }

  def parseParts(): Seq[Part] = {
    val txt = io.Source.fromInputStream(getClass.getResourceAsStream("/xke_ted.txt")).mkString
    val ts = XKETedParser.ted(txt)
    ts
  }

}