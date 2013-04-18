package com.thenewmotion.scynapse.serialization.xml

import com.thoughtworks.xstream.converters.{UnmarshallingContext, MarshallingContext, Converter}
import com.thoughtworks.xstream.io.{HierarchicalStreamReader, HierarchicalStreamWriter}


class EnumConverter extends Converter {

  def canConvert(cls: Class[_]): Boolean = classOf[Enumeration#Value].isAssignableFrom(cls)

  def marshal(source: Any, writer: HierarchicalStreamWriter, context: MarshallingContext) {
    val v = source.asInstanceOf[Enumeration#Value]

    val enum = v.getClass.getField("$outer").get(v).asInstanceOf[Enumeration]

    writer.addAttribute("in"  , enum.getClass.getName.stripSuffix("$"))
    writer.addAttribute("name", v.toString)
  }

  def unmarshal(reader: HierarchicalStreamReader, context: UnmarshallingContext): AnyRef = {
    val enum = Class.forName(reader.getAttribute("in"))
    enum
      .getMethod("withName", classOf[String])
      .invoke(enum, reader.getAttribute("name"))
  }
}