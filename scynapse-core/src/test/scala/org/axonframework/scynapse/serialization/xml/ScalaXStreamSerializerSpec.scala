package org.axonframework.scynapse.serialization.xml

import org.axonframework.serialization.xml.XStreamSerializer
import org.scalatest.{FlatSpec, MustMatchers}
case class ListTestEvent(elements: List[String])

class ScalaXStreamSerializerSpec extends FlatSpec with MustMatchers {

  "XStreamSerializer" should "support immutable list serialization" in {
    val serializer = ScalaXStreamSerializer.builder()
    val testEvent  = ListTestEvent(List("abc", "bcd", "cde"))

    val ser = serializer.serialize(testEvent, classOf[Array[Byte]])
    (serializer.deserialize(ser): ListTestEvent) must equal(testEvent)
  }

}
