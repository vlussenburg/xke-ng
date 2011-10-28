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
import net.liftweb.util.Props
import com.atlassian.crowd.exception._
import net.liftweb.util.Helpers._
@RunWith(classOf[JUnitRunner])
class AuthenticationRepositoryTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  private val crowdSysUser = Props.get("crowd.sysuser").get
  private val crowdSysUserPwd = Props.get("crowd.sysuser.pwd").get
  private val crowdBase = Props.get("crowd.base.url").get
  private val aUser = new String(hexDecode("49726f716f78783437"))
  private val aPwd = new String(hexDecode("757065746572"))
  override def beforeEach() {
  }

  it should "succeed authentication test" in {
    val repo = AuthenticationRepository(crowdBase, crowdSysUser, crowdSysUserPwd, true)
    val returnValue = repo.authenticate(Credential(aPwd, aUser, false))
    returnValue should not be (None)
  }
  it should "not succeed connection test" in {
    intercept[InvalidAuthenticationException] {
      AuthenticationRepository(crowdBase, "unknown", crowdSysUserPwd, true)
    }

  }

  type ? = this.type
}
