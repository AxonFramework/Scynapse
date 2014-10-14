package com.thenewmotion.scynapse.akka

import akka.actor._
import akka.testkit.{TestProbe}
import org.axonframework.domain.GenericEventMessage
import org.axonframework.eventhandling.SimpleEventBus


class AxonExtensionSpec extends ScynapseAkkaSpecBase {
  trait Ctx {
    implicit val system = ActorSystem("scynapse-akka")
    val eventBus = new SimpleEventBus()
    val axonAkkaBridge = AxonEventBusExtension(system) forEventBus eventBus
    def eventMessage[T](payload: T): GenericEventMessage[T] =
      new GenericEventMessage(payload)

    val probe = TestProbe()
  }

  behavior of "An Axon <-> Akka EventBus bridge"

  it should "forward events to subscribed actor" in new Ctx {
      axonAkkaBridge subscribe probe.ref
      eventBus publish eventMessage("hi")
      probe expectMsg "hi"
  }

  it should "forward events to subscribed actor2" in new Ctx {
    axonAkkaBridge subscribe probe.ref
    eventBus publish eventMessage("hi")
    probe expectMsg "hi"
  }

  it should "not send events to unsubscribed actors" in new Ctx {
      axonAkkaBridge subscribe probe.ref
      axonAkkaBridge unsubscribe probe.ref
      eventBus publish eventMessage("not for actor")
      probe expectNoMsg
  }

  it should "not subscribe actors more than once" in new Ctx {
    axonAkkaBridge subscribe probe.ref
    axonAkkaBridge subscribe probe.ref
    eventBus publish eventMessage("only one message")
    probe expectMsg "only one message"
    probe expectNoMsg
  }
}
