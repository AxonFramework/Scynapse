package org.axonframework.scynapse.serialization.converters

import java.nio.charset.Charset
import org.axonframework.serialization.AbstractContentTypeConverter
import scala.xml.Elem

/**
 * This converter, together with the ByteArrayToXmlElemConverter allow to deserialize
 * events to standard scala XML. This is used for Upcasting.
 *
 * It is not required to register this Converter in any way. This is done via
 * the file "org.axonframework.serializer.ContentTypeConverter" in META-INF/services of this package
 */
class XmlElemToByteArrayConverter extends AbstractContentTypeConverter[Elem, Array[Byte]] {

  private val UTF8: Charset = Charset.forName("UTF-8")

  def expectedSourceType: Class[Elem] = {
    classOf[Elem]
  }

  def targetType: Class[Array[Byte]] = {
    classOf[Array[Byte]]
  }

  def convert(original: Elem): Array[Byte] = {
    original.buildString(true).getBytes(UTF8)
  }
}
