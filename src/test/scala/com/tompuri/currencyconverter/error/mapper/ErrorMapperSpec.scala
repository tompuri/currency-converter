package com.tompuri.currencyconverter.error.mapper

import com.tompuri.currencyconverter.error.models.external.{BadRequest, Forbidden, InternalServerError}
import com.tompuri.currencyconverter.error.models.internal.{DeserializationError, HttpError, NetworkError}
import com.tompuri.currencyconverter.error.models.{external, internal}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class ErrorMapperSpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks {
  it should "map HttpError to external" in {
    val testCases = (400 to 599).map { code =>
      val expectedClass = code match {
        case 400 => classOf[BadRequest]
        case 403 => classOf[Forbidden]
        case _   => classOf[InternalServerError]
      }
      (HttpError("message", code), expectedClass)
    }

    forAll(Table(("internal", "external"), testCases*)) { (internalError, externalErrorClass) =>
      ErrorMapper.toExternal(internalError).getClass shouldBe externalErrorClass
    }
  }

  it should "map DeserializationError to external" in {
    val internalError = DeserializationError("message")
    ErrorMapper.toExternal(internalError) shouldBe a[InternalServerError]
  }

  it should "map NetworkError to external" in {
    val internalError = NetworkError("message")
    ErrorMapper.toExternal(internalError) shouldBe a[InternalServerError]
  }
}
