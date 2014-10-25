package org.axonframework.scynapse.serialization.xml

import org.scalatest.FlatSpec
import org.scalatest.matchers.MustMatchers

case class ListTestEvent(elements: List[String])

class XStreamSerializerSpec extends FlatSpec with MustMatchers {

    "XStreamSerializer" should "support immutable list serialization" in {
        val serializer = new XStreamSerializer
        val testEvent = ListTestEvent(List("abc", "bcd", "cde"))

        val ser = serializer.serialize(testEvent, classOf[Array[Byte]])
        (serializer.deserialize(ser): ListTestEvent) must equal(testEvent)
    }

}
