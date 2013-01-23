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
