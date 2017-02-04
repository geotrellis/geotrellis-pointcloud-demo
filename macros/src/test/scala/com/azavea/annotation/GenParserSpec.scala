package com.azavea.annotation

import com.azavea.SystemExitControl
import com.azavea.SystemExitControl.ExitTrappedException
import org.scalatest._

class GenParserSpec extends FunSpec with Matchers {

  @GenParser("gen-parser-spec")
  case class TestConfig(str: String = "str1", i: Int = 2, b: Option[Boolean] = None)

  @GenParser("gen-parser-spec", requiredFields = "str")
  case class TestConfigReq(str: String = "str1", i: Int = 2, b: Option[Boolean] = None)

  describe("GenParser Test") {
    it("should correctly parse empty seq") {
      val res = TestConfig.parse(Seq())
      res shouldBe TestConfig()
    }

    it ("should correctly parse seq") {
      val res = TestConfig.parse(Seq("--str", "str2", "--i", "3", "--b", "true"))
      res shouldBe TestConfig("str2", 3, Some(true))
    }

    it ("should not parse unknown args") {
      SystemExitControl.forbidSystemExitCall()
      intercept[ExitTrappedException] {
        TestConfig.parse(Seq("--test", "str2"))
      }
      SystemExitControl.enableSystemExitCall()
    }

    it ("should generate nonempty help string") {
      TestConfig.help.nonEmpty shouldBe true
    }

    it ("should parse with required args") {
      val res = TestConfigReq.parse(Seq("--str", "str2", "--i", "3", "--b", "true"))
      res shouldBe TestConfigReq("str2", 3, Some(true))
    }

    it ("should not parse without required args") {
      SystemExitControl.forbidSystemExitCall()
      intercept[ExitTrappedException] {
        TestConfigReq.parse(Seq("--i", "3", "--b", "true"))
      }
      SystemExitControl.enableSystemExitCall()
    }
  }
}
