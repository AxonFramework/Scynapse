package org.axonframework.scynapse.serialization.xml

import org.axonframework.serialization.AbstractXStreamSerializer
import org.axonframework.serialization.xml.XStreamSerializer

/**
  * XStream serializer registration.
  *
  * This serializer can be extended in your own application like:
  * <code>
  * class EventSerializer extends XStreamSerializer {
  *   getXStream.aliasPackage("myalias", "com.fully.qualified.package.name")
  *   getXStream.registerConverter(new MyApplicationEventTypeConverter)
  * }
  * <code>
  * In this sample you register an alias for a package in order to serialize your events not with the
  * fully qualified package name and it registers additional converters of importance for your application.
  *
  * Thereafter the eventstore can be configured to use this serializer
  *
  * You may use this XStreamSerializer without extension.
  */
public class ScalaXStreamSerializer extends XStreamSerializer {
  getXStream.alias("list", classOf[::[_]])
  getXStream.registerConverter(new ListConverter(getXStream.getMapper))
  getXStream.registerConverter(new EnumConverter)

}

object ScalaXStreamSerializer {

  def build(): AbstractXStreamSerializer.Builder = {
    new AbstractXStreamSerializer.Builder()
  }

}