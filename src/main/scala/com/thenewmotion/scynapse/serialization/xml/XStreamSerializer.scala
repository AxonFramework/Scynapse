package com.thenewmotion.scynapse.serialization.xml

import org.axonframework.serializer.xml.{XStreamSerializer => AxonXStreamSerializer}

class XStreamSerializer extends AxonXStreamSerializer {
  getXStream.alias("List", classOf[::[_]])
  getXStream.registerConverter(new ListXStreamConverter(getXStream.getMapper))
}
