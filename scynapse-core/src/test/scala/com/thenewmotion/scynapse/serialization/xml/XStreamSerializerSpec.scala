package com.thenewmotion.scynapse.serialization.xml

import org.specs2.Specification

class XStreamSerializerSpec extends Specification { def is =

  "XStreamSerializer supports"                              ^
    "Immutable list serialization"                          ! sut().serialize ^
    end

  case class sut() {
    val serializer = new XStreamSerializer
    val testEvent = ListTestEvent(List("abc", "bcd", "cde"))

    def serialize = {
      val ser = serializer.serialize(testEvent, classOf[Array[Byte]])
      (serializer.deserialize(ser): ListTestEvent) mustEqual testEvent
    }
  }

  case class ListTestEvent(elements: List[String])

}

/*
import org.scalatest.FlatSpec
import org.scalatest.matchers.MustMatchers

class XStreamSerializerSpec extends FlatSpec with MustMatchers {

  "XStreamSerializer" should  "support immutable list serialization" in {
    val serializer = new XStreamSerializer
    val testEvent = ListTestEvent(List("abc", "bcd", "cde"))

    val ser = serializer.serialize(testEvent, classOf[Array[Byte]])
    (serializer.deserialize(ser): ListTestEvent) must equal(testEvent)
  }

  case class ListTestEvent(elements: List[String])

}

*/
