package com.thenewmotion.scynapse.serialization.xml

import org.axonframework.serializer.xml.{XStreamSerializer => AxonXStreamSerializer}

class XStreamSerializer extends AxonXStreamSerializer {
  getXStream.alias("list", classOf[::[_]])
  getXStream.registerConverter(new ListXStreamConverter(getXStream.getMapper))
}
