package org.axonframework.scynapse.akka

import java.util

import akka.actor._
import akka.testkit.{TestProbe}
import org.joda.time.DateTime
import scala.util.{Try, Success, Failure}
import org.axonframework.domain.{MetaData, DomainEventMessage, GenericEventMessage}
import org.axonframework.eventhandling.SimpleEventBus

class TestDomainEventMessage[T](payload: T) extends DomainEventMessage[T] {

  override def withMetaData(metaData: util.Map[String, _]): DomainEventMessage[T] = ???

  override def getAggregateIdentifier: AnyRef = ???

  override def andMetaData(metaData: util.Map[String, _]): DomainEventMessage[T] = ???

  override def getSequenceNumber: Long = 1

  override def getTimestamp: DateTime = DateTime.now

  override def getIdentifier: String = "domain-event-message-1"

  override def getMetaData: MetaData = ???

  override def getPayload: T = payload

  override def getPayloadType: Class[_] = ???
}

class AxonExtensionSpec extends ScynapseAkkaSpecBase {

  trait Ctx {
    val eventBus = new SimpleEventBus()
    val axonAkkaBridge = AxonEventBusExtension(system) forEventBus eventBus
    def eventMessage[T](payload: T): GenericEventMessage[T] =
      new GenericEventMessage(payload)

    def domainEventMessage[T](payload: T): DomainEventMessage[T] =
      new TestDomainEventMessage[T](payload)

    val probe = TestProbe()
  }

  behavior of "An Axon <-> Akka EventBus bridge"

  it should "forward payloads to subscribed actor" in new Ctx {
    axonAkkaBridge subscribe probe.ref
    eventBus publish eventMessage("hi")
    probe expectMsg "hi"
  }

  it should "forward full events to the subscribed actor" in new Ctx {
    axonAkkaBridge subscribeEvent probe.ref
    eventBus publish domainEventMessage("hi")
    probe.expectMsgPF() {
      case msg: TestDomainEventMessage[_] => if (msg.getSequenceNumber == 1l) true else false
      case other => false
    }
  }

  it should "check if actors are subscribed" in new Ctx {
    axonAkkaBridge subscribe probe.ref
    axonAkkaBridge isSubscribed probe.ref shouldBe true
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

  it should "unsubsribe actors on termination" in new Ctx {
    axonAkkaBridge subscribe probe.ref
    system.stop(probe.ref)
    Thread.sleep(200) // wait for async inner stuff to occur
    axonAkkaBridge isSubscribed probe.ref shouldBe false
  }
}
