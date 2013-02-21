package com.thenewmotion.scynapse.serialization.xml

import com.thoughtworks.xstream.converters.{UnmarshallingContext, MarshallingContext, Converter}
import com.thoughtworks.xstream.io.{HierarchicalStreamReader, HierarchicalStreamWriter}

import reflect.runtime.universe._


class EnumConverter extends Converter {

  def canConvert(cls: Class[_]): Boolean = classOf[Enumeration#Value].isAssignableFrom(cls)

  import EnumConverter._

  def marshal(source: Any, writer: HierarchicalStreamWriter, context: MarshallingContext) {
    val v = source.asInstanceOf[Enumeration#Value]

    val enum = mirror.reflect(v).reflectField(outerEnum).get.asInstanceOf[Enumeration]

    writer.addAttribute("in"  , enum.getClass.getName.stripSuffix("$"))
    writer.addAttribute("name", v.toString)
  }

  def unmarshal(reader: HierarchicalStreamReader, context: UnmarshallingContext): AnyRef = {
    val enumClassName = reader.getAttribute("in")
    val enumValueName = reader.getAttribute("name")

    val symbol = mirror.staticModule(enumClassName).asModule
    val enum = mirror.reflectModule(symbol).instance.asInstanceOf[Enumeration]

    enum.withName(enumValueName)
  }

}

object EnumConverter {
  lazy val outerEnum = typeOf[Enumeration#Value]
    .declaration(newTermName("scala$Enumeration$$outerEnum"))
    .asTerm.accessed.asTerm

  lazy val mirror = runtimeMirror(getClass.getClassLoader)
}
