package com.xebia.xkeng.dao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import org.scalatest.{ BeforeAndAfterEach, FlatSpec }
import org.joda.time.DateTime
import com.xebia.xkeng.model._
import org.joda.time.format._
import com.xebia.xkeng.dao.RepositoryTestAssembly._

@RunWith(classOf[JUnitRunner])
class FacilityRepositoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MongoTestConnection {

  val l1 = Location("Maup", 20)
  val l2 = Location("Laap", 30)
  val l3 = Location("New", 100)

  override def beforeEach() { 
    init()
    createTestLocations();
  }

  private def createTestLocations() = {
    Facility.drop
    List(l1, l2, l3).map(facilityRepository.addLocation(_))
  }

  it should "find All locations" in {
    val locations = facilityRepository.findAllLocations
    locations.size should be(3)
  }
  it should "update existing location" in {
    facilityRepository.updateLocation(l1.copy(description = "Maup2"))
    val changed = facilityRepository.findLocationByName("Maup2")
    changed should not be (None)
    changed.get.description should be("Maup2")
  }
  it should "add new location" in {
    val l4 = Location("Library", 20)
    facilityRepository.addLocation(l4)
    val changed = facilityRepository.findLocationByName(l4.description)
    changed should not be (None)
    changed.get.description should be("Library")
  }
  it should "not add new location if name is equal" in {
    val l4 = Location("Maup", 20)
    intercept[IllegalArgumentException] {
      facilityRepository.addLocation(l4)
    }
  }
  type ? = this.type
}