package org.axonframework.scynapse.annotations

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier
import org.scalatest.FlatSpec
import org.scalatest.matchers.MustMatchers

class AggregateIdSpec extends FlatSpec with MustMatchers {

    "AggregateId annotation" should "affect fields" in {
        val cls = Test("id", 0).getClass

        val annotatedFields = cls.getDeclaredFields
          .filter(f => f.getAnnotation(classOf[TargetAggregateIdentifier]) != null)

        annotatedFields must have size (0)

    }

    it should "not affect methods" in {
        val cls = Test("id", 0).getClass

        val annotatedMethods = cls.getDeclaredMethods
          .filter(m => m.getAnnotation(classOf[TargetAggregateIdentifier]) != null)

        annotatedMethods must have size (1)
    }

}

case class Test(@aggregateId id: String, something: Int)
