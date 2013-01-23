package com.thenewmotion.scynapse.test

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import org.axonframework.eventsourcing.annotation.{AggregateIdentifier, AbstractAnnotatedAggregateRoot}
import org.axonframework.test.Fixtures
import org.axonframework.commandhandling.annotation.CommandHandler

import com.thenewmotion.scynapse.annotations._

class EventMatchersSpec extends Specification with EventMatchers {
  "Event matchers" should {
    "allow for" in {
      "strict equality match" in new sut {
        fixture
          .given(new AnyRef) // no empty given allowed ???
          .when(
            DoStrict("test", "equals"))
          .expectEventsMatching(withPayloads(
            isEqualTo(StrictDone("equals"))))
      }
      "partial match" in new sut {
        fixture
          .given(new AnyRef) // no empty given allowed ???
          .when(
            DoPartial("test", "this is", 1, "part" :: "of" :: "big event" :: Nil))
          .expectEventsMatching(withPayloads(
            isLike {
              case PartialDone(_, first, _, List(_, _, last)) =>
                s"$first $last" == "this is big event"
            }))
      }
    }
  }

  trait sut extends Scope {
    val fixture = {
      val f = Fixtures.newGivenWhenThenFixture(classOf[TestAggregate])
      f.setReportIllegalStateChange(false)
      f
    }

  }
}

case class DoStrict(@aggregateId id: String, a: String)
case class StrictDone(a: String)

case class DoPartial(@aggregateId id: String, a: String, b: Int, c: List[String])
case class PartialDone(id: String, a: String, b: Int, c: List[String])

class TestAggregate extends AbstractAnnotatedAggregateRoot[String] {
  @AggregateIdentifier
  private val id: String = "test"

  @CommandHandler
  def handle(cmd: DoStrict) { apply(StrictDone(cmd.a)) }

  @CommandHandler
  def handle(cmd: DoPartial) { apply(PartialDone(cmd.id, cmd.a, cmd.b, cmd.c)) }
}
