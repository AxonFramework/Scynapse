package com.thenewmotion.scynapse.serialization.xml

import org.specs2.Specification
import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.DomDriver
import xml.{XML, NodeSeq}


object SomeEnum extends Enumeration {
  val A, B = Value
}

object OtherEnum extends Enumeration {
  val P, Q = Value
}

case class Simple(field: SomeEnum.Value)
case class Complex(some: SomeEnum.Value, other: OtherEnum.Value)

class EnumSerializerSpec extends Specification { def is =

  "Enumeration value serialized"                    ^
    "using names"                         ! sut().name ^
    "and enclosing object's class name"   ! sut().cls  ^
    "which allows multiple types to be deserialized"    ! sut().cmplx ^
    end

  case class sut() {
    val xStream = {
      val x = new XStream(new DomDriver())
      x.registerConverter(new EnumConverter)
      x
    }

    def name = {
      val xml = xStream.toXML(Simple(SomeEnum.A))
      XML.loadString(xml) must \("field", "name" -> "A")
    }

    def cls = {
      val xml = xStream.toXML(Simple(SomeEnum.B))
      XML.loadString(xml) must \("field", "in" -> "com.thenewmotion.scynapse.serialization.xml.SomeEnum")
    }

    def vl = {
      val xml = xStream.toXML(Simple(SomeEnum.B))
      XML.loadString(xml) must \("field", "class" -> "scala.Enumeration$Val")
    }

    def cmplx = {
      val origin = Complex(SomeEnum.A, OtherEnum.P)
      val xml = xStream.toXML(origin)
      val deserialized = xStream.fromXML(xml).asInstanceOf[Complex]
      deserialized mustEqual origin
    }
  }
}
