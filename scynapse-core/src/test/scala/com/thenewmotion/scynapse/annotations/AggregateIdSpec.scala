package com.thenewmotion.scynapse.annotations

import org.specs2.mutable.Specification
import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier

class AggregateIdSpec extends Specification {
  "AggregateId annotation" should {
    "affect field only" in {
      val cls = Test("id", 0).getClass

      cls.getDeclaredMethods
        .filter(m => m.getAnnotation(classOf[TargetAggregateIdentifier]) != null)
        .aka("annotated methods") must haveSize(1)

      cls.getDeclaredFields
        .filter(f => f.getAnnotation(classOf[TargetAggregateIdentifier]) != null)
        .aka("annotated fields") must haveSize(0)

    }
  }

}

case class Test(@aggregateId id: String, something: Int)
