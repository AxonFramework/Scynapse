package com.thenewmotion.scynapse.serialization.xml

import com.thoughtworks.xstream.mapper.Mapper
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter
import com.thoughtworks.xstream.io.{HierarchicalStreamReader, HierarchicalStreamWriter}
import com.thoughtworks.xstream.converters.{UnmarshallingContext, MarshallingContext}

class ListXStreamConverter(mapper: Mapper) extends AbstractCollectionConverter(mapper) {

  override def canConvert(cls: Class[_]) = classOf[::[_]] == cls

  override def marshal(value: Any, writer: HierarchicalStreamWriter, context: MarshallingContext) {
    val list = value.asInstanceOf[List[_]]
    list foreach (writeItem(_, context, writer))
  }

  override def unmarshal(reader: HierarchicalStreamReader, context: UnmarshallingContext): AnyRef = {
    def loop(acc: List[_]): List[_] =
      if (!reader.hasMoreChildren) acc
      else {
        reader.moveDown()
        val item = readItem(reader, context, acc)
        reader.moveUp()
        loop(item :: acc)
      }

    loop(List.empty).reverse
  }
}
