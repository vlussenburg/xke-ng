package com.xebia.xkeng.dao

import com.xebia.xkeng.model.Conference
import net.liftweb.json.JsonDSL._
import org.joda.time.format._
import org.joda.time.DateTime

trait ConferenceRepository {
  def findConferences(year: Int): List[Conference]

  def findConferences(year: Int, month: Int): List[Conference]

  def findConference(year: Int, month: Int, day: Int): Option[Conference]

  def findConferenceOn(yearmonthday: String): Option[Conference]

  def findConference(id: String): Option[Conference]

}

trait RepositoryComponent {

  val conferenceRepository:ConferenceRepository


  class ConferenceRepositoryImpl extends ConferenceRepository {

      val fmt = DateTimeFormat.forPattern("yyyyMMdd");

    private def dateRegexpQry(date:String) = {
      ("date" -> ("$regex" -> ("^%s.*".format(date))))
    }



    /**
     * db.confs.find( { date : { $regex : "^<year>.*" } } );
     */
    def findConferences(year: Int) = Conference.findAll(dateRegexpQry("%04d" format(year)))

    /**
     * db.confs.find( { date : { $regex : "^<year>-<month>.*" } } );
     */
    def findConferences(year: Int, month: Int) = Conference.findAll(dateRegexpQry("%04d-%02d" format(year, month)))

    /**
     * db.confs.find( { date : { $regex : "^<year>-<month>-<day>.*" } } );
     */
    def findConference(year: Int, month: Int, day: Int) = Conference.find(dateRegexpQry("%04d-%02d-%02d" format(year, month, day)))


    /**
     * db.confs.find( { date : { $regex : "^<year>-<month>-<day>.*" } } );
     */
    def findConferenceOn(yearmonthday: String) = {
      val date:DateTime = fmt.parseDateTime(yearmonthday)
      Conference.find(dateRegexpQry("%04d-%02d-%02d" format(date.getYear, date.getMonthOfYear, date.getDayOfMonth)))
    }

    /**
     * db.confs.find( { _id: ObjectId("<id>")} );
     */
    def findConference(id: String) = Conference.find(id)

  }

}