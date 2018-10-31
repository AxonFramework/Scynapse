package org.axonframework.scynapse.serialization.converters

import java.nio.charset.Charset

import org.axonframework.serialization.ContentTypeConverter

import scala.xml.{Elem, XML}

/**
  * This converter, together with the XmlElemToByteArrayConverter allow to deserialize
  * events to standard scala XML. This is used for Upcasting.
  *
  * It is not required to register this Converter in any way. This is done via
  * the file "org.axonframework.serializer.ContentTypeConverter" in META-INF/services of this package
  */
//class ByteArrayToXmlElemConverter extends AbstractContentTypeConverter[Array[Byte], Elem] {
class ByteArrayToXmlElemConverter extends Object with ContentTypeConverter[Array[Byte], Elem] {

  private val UTF8: Charset = Charset.forName("UTF-8")

  override def expectedSourceType: Class[Array[Byte]] = {
    classOf[Array[Byte]]
  }

  override def targetType: Class[Elem] = {
    classOf[Elem]
  }

  override def convert(original: Array[Byte]): Elem = {
    XML.loadString(new String(original, UTF8))
  }
}
