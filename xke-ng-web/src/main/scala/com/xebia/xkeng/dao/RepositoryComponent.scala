package com.xebia.xkeng.dao

import com.xebia.xkeng.model.Conference
import net.liftweb.json.JsonDSL._
import org.joda.time.format._
import org.joda.time.DateTime

trait ConferenceRepository {
  def findConferences(year: Int): List[Conference]

  def findConferences(year: Int, month: Int): List[Conference]

  def findConferences(year: Int, month: Int, day: Int): List[Conference]

  def findConference(id: String): Option[Conference]

}

trait RepositoryComponent {

  val conferenceRepository:ConferenceRepository


  class ConferenceRepositoryImpl extends ConferenceRepository {

      val fmt = DateTimeFormat.forPattern("yyyyMMdd");

    private def dateRegexpQry(begin:String) = {
      ("begin" -> ("$regex" -> ("^%s.*".format(begin))))
    }



    /**
     * db.confs.find( { begin : { $regex : "^<year>.*" } } );
     */
    def findConferences(year: Int) = Conference.findAll(dateRegexpQry("%04d" format(year)))

    /**
     * db.confs.find( { begin : { $regex : "^<year>-<month>.*" } } );
     */
    def findConferences(year: Int, month: Int) = Conference.findAll(dateRegexpQry("%04d-%02d" format(year, month)))

    /**
     * db.confs.find( { begin : { $regex : "^<year>-<month>-<day>.*" } } );
     */
    def findConferences(year: Int, month: Int, day: Int) = Conference.findAll(dateRegexpQry("%04d-%02d-%02d" format(year, month, day)))


    /**
     * db.confs.find( { _id: ObjectId("<id>")} );
     */
    def findConference(id: String) = Conference.find(id)

  }

}