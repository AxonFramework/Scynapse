package org.axonframework.scynapse.test

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.scynapse.annotations._
import org.axonframework.test.aggregate.{AggregateTestFixture, FixtureConfiguration}
import org.scalatest.{FlatSpec, MustMatchers}
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.slf4j.LoggerFactory.getLogger

class EventMatchersSpec extends FlatSpec with MustMatchers with EventMatchers {
  "Event matchers" should "allow strict equality match" in new sut {
    fixture
      .given(new AnyRef) // no empty given allowed ???
      .when(DoStrict("test", "equals"))
      .expectEventsMatching(withPayloads(isEqualTo(StrictDone("equals"))))
  }

  it should "allow partial match" in new sut {
    fixture
      .given(new AnyRef) // no empty given allowed ???
      .when(DoPartial("test", "this is", 1, "part" :: "of" :: "big event" :: Nil))
      .expectEventsMatching(withPayloads(isLike {
        case PartialDone(_, first, _, List(_, _, last)) =>
          s"$first $last" == "this is big event"
      }))
  }
}

trait sut {
  val fixture: FixtureConfiguration[TestAggregate] = {
    val f = new AggregateTestFixture(classOf[TestAggregate])
    f.setReportIllegalStateChange(false)
    f
  }
}

case class DoStrict(@aggregateId id: String, a: String)

case class StrictDone(a: String)

case class DoPartial(@aggregateId id: String, a: String, b: Int, c: List[String])

case class PartialDone(id: String, a: String, b: Int, c: List[String])

class TestAggregate {
  @AggregateIdentifier
  private val id: String = "test"

  @CommandHandler
  def handle(cmd: DoStrict) {
    apply(StrictDone(cmd.a))
  }

  @CommandHandler
  def handle(cmd: DoPartial) {
    apply(PartialDone(cmd.id, cmd.a, cmd.b, cmd.c))
  }
}
