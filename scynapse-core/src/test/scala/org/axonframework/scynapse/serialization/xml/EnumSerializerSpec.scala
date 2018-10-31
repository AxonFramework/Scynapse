package org.axonframework.scynapse.serialization.xml

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.DomDriver

import xml.XML
import org.scalatest.{FlatSpec, MustMatchers}

object SomeEnum extends Enumeration {
  val A, B = Value
}

object OtherEnum extends Enumeration {
  val P, Q = Value
}

case class Simple(field: SomeEnum.Value)

case class Complex(some: SomeEnum.Value, other: OtherEnum.Value)

class EnumSerializerSpec extends FlatSpec with MustMatchers {

  def xStream = {
    val x = new XStream(new DomDriver())
    x.registerConverter(new EnumConverter)
    x
  }

  "Enumeration value converter" should "serialize by name" in {
    val xml = xStream.toXML(Simple(SomeEnum.A))
    (XML.loadString(xml) \\ "field" \ "@name").text must equal("A")
  }

  it should "include enum full class name" in {
    val xml = xStream.toXML(Simple(SomeEnum.B))
    val res = XML.loadString(xml)
    (res \\ "field" \ "@class").text must equal("scala.Enumeration$Val")
    (res \\ "field" \ "@in").text must equal("org.axonframework.scynapse.serialization.xml.SomeEnum")
  }

  it should "allows multiple types to be deserialized" in {
    val origin       = Complex(SomeEnum.A, OtherEnum.P)
    val xml          = xStream.toXML(origin)
    val deserialized = xStream.fromXML(xml).asInstanceOf[Complex]
    deserialized must equal(origin)
  }

}
