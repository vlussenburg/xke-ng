package com.xebia.xkeng.serialization

import scala.io.Source

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL.seq2jvalue
import net.liftweb.json.ext.JodaTimeSerializers
import net.liftweb.json.Formats
import net.liftweb.json.JObject
import net.liftweb.json.JsonAST
import net.liftweb.json.NoTypeHints
import net.liftweb.json.Printer
import net.liftweb.json.Serialization
import net.liftweb.json.parse
package object util {

  val DATE_TIME_FORMAT = ISODateTimeFormat.dateTime()
    val TIME_FORMAT = "HH:mm"
  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all

  object AsDateTime {
    def unapply(isoDateString: String): Option[DateTime] = {
      try { Some(DATE_TIME_FORMAT.parseDateTime(isoDateString)) }
      catch {
        case e: Exception => None
      }
    }
  }

  /**
   * =====================================================
   * Generic deserialization helpers
   * =====================================================
   */

  def deserialize[T](json: String)(implicit m: Manifest[T]): T = {
    Serialization.read[T](json)
  }

  def deserialize[T](json: JValue)(implicit m: Manifest[T]): T = {
    deserialize[T](Printer.pretty(JsonAST.render(json)))
  }

  def deserializeList[T](value: JValue)(implicit m: Manifest[T]): List[T] = {
    value match {
      case JArray(items) => items.map((v: JValue) => deserialize[T](v))
      case _ => Nil
    }
  }

  def deserializeStringList(value: JValue): List[String] = {
    value match {
      case JArray(items) => {
        items match {
          case l @ List(_: JString, _*) => l.asInstanceOf[List[JString]].map(_.values.toString())
          case _ => Nil
        }
      }
      case _ => Nil
    }
  }

  def deserializeIntList(value: JValue): List[Int] = {
    value match {
      case JArray(items) => {
        items match {
          case l @ List(_: JInt, _*) => l.asInstanceOf[List[JInt]].map(_.values.toInt)
          case _ => Nil
        }
      }
      case _ => Nil
    }
  }

  def deserializeList[T](value: JValue, convert: JValue => T)(implicit m: Manifest[T]): List[T] = {
    value match {
      case JArray(items) => items.map((v: JValue) => convert(v))
      case _ => Nil
    }
  }

  def deserializeToStr(value: JValue): String = {
    Printer.pretty(JsonAST.render(value))
  }

  /**
   * =====================================================
   * Generic serialization helpers
   * =====================================================
   */
  def serializeToJson(s: String): JValue = {
    parse(s)
  }

  def serializeStringsToJArray(strings: String*): JArray = {
    JArray(strings.toList.map(JString(_)))
  }

  def serializeIntsToJArray(ints: Int*): JArray = {
    JArray(ints.toList.map(JInt(_)))
  }
  def serializeToJsonStr(t: AnyRef): String = {
    val r = Serialization.write(t)
    println(r)
    r
  }

  implicit def pimpJValueWithInformativeFailingSelector[T <: JValue](values: List[T]): { def \\!(query: String): JValue; def \!(query: String): JValue } = new {

    def handleNotFound(query: String, values: List[T]) = {
      val message = "mandatory json field=[%s] of json=[%s] was not present." format (query, serializeToJsonStr(values).replace("\"" ,"" ))
      error(message)
      throw new IllegalArgumentException(message)

    }
    def emptyObjectPartial(query: String): PartialFunction[JValue, JValue] = {
      case JObject(Nil) => handleNotFound(query, values)
    }
    def jNothingPartial(query: String): PartialFunction[JValue, JValue] = {
      case JNothing => handleNotFound(query, values)
    }

    val default: PartialFunction[JValue, JValue] = { case a: JValue => a }

    def \\!(query: String): JValue = {
      val selected: JValue = (values \\ query)
      emptyObjectPartial(query).orElse(jNothingPartial(query)).orElse(default)(selected)
    }
    def \!(query: String): JValue = {
      val selected: JValue = (values \ query)
      emptyObjectPartial(query).orElse(jNothingPartial(query)).orElse(default)(selected)
    }
  }

  /**
   * =====================================================
   * File utils
   * =====================================================
   */

  def fileFromClasspathAsString(fileName: String) = {
    Source.fromInputStream(fileFromClasspathAsInputStream(fileName)).mkString
  }

  def fileFromClasspathAsInputStream(fileName: String) = {
    getClass.getClassLoader.getResourceAsStream(fileName)
  }
}

import util._
/**
 * Traits for serialisation and deserialisation to/from JSON
 */
trait FromJsonDeserializer[T] {
  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all
  def apply(json: String)(implicit m: Manifest[T]): T = {
    deserialize[T](json)
  }

}

trait ToJsonSerializer[T] {
  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all
  def serializeToJson: JValue = {
    parse(serializeToJsonStr)
  }
  def serializeToJsonStr: String = {
    Serialization.write(this)
  }
}

