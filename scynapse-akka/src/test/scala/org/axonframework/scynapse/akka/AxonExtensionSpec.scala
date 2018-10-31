package org.axonframework.scynapse.akka

import java.time.Instant

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.axonframework.eventhandling.{GenericEventMessage, SimpleEventBus}
import org.axonframework.eventsourcing.DomainEventMessage
import org.axonframework.messaging.MetaData
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TestDomainEventMessage[T](payload: T) extends DomainEventMessage[T] {

  override def withMetaData(metaData: java.util.Map[String, _]): DomainEventMessage[T] = ???

  override def getAggregateIdentifier: String = ???

  override def andMetaData(metaData: java.util.Map[String, _]): DomainEventMessage[T] = ???

  override def getSequenceNumber: Long = 1

  override def getTimestamp: Instant = Instant.now()

  override def getIdentifier: String = "domain-event-message-1"

  override def getMetaData: MetaData = ???

  override def getPayload: T = payload

  override def getPayloadType: Class[T] = ???

  override def getType: String = ???
}

class AxonExtensionSpec() extends TestKit(ActorSystem("AxonExtensionSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with ScalaFutures {


  trait Ctx {
    val eventBus = new SimpleEventBus()
    val axonAkkaBridge = AxonEventBusExtension(system) forEventBus eventBus

    def eventMessage[T](payload: T): GenericEventMessage[T] =
      new GenericEventMessage(payload)

    def domainEventMessage[T](payload: T): DomainEventMessage[T] =
      new TestDomainEventMessage[T](payload)
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  //import scala.concurrent.ExecutionContext.Implicits.global

  "An Axon <-> Akka EventBus bridge" must {
    val probe = TestProbe()

    "forward payloads to subscribed actor" in new Ctx {
      axonAkkaBridge subscribe probe.ref
      eventBus publish eventMessage("hi")
      probe expectMsg "hi"
    }

    "forward full events to the subscribed actor" in new Ctx {
      whenReady(axonAkkaBridge subscribeEvent probe.ref) { answer =>
        answer shouldBe Success("OK")
      }
      eventBus publish domainEventMessage("hi")
      probe.expectMsgPF() {
        case msg: TestDomainEventMessage[_] => if (msg.getSequenceNumber == 1l) true else false
        case other => false
      }
    }

    "check if actors are subscribed" in  new Ctx {
      whenReady(axonAkkaBridge subscribe probe.ref) { answer =>
        answer shouldBe Success("OK")
      }
      whenReady(axonAkkaBridge isSubscribed probe.ref) { answer =>
        answer shouldBe true
      }
    }

    "not send events to unsubscribed actors" in new Ctx {
      whenReady(axonAkkaBridge subscribe probe.ref) { answer =>
        answer shouldBe Success("OK")
      }
      whenReady(axonAkkaBridge unsubscribe probe.ref) { answer =>
        answer shouldBe Success("OK")
      }
      eventBus publish eventMessage("not for actor")
      probe expectNoMsg
    }

    "not subscribe actors more than once" in  new Ctx {
      whenReady(axonAkkaBridge subscribe probe.ref) { answer =>
        answer shouldBe Success("OK")
      }
      whenReady(axonAkkaBridge subscribe probe.ref) {
        case Failure(any) => // do nothing
        case Success(msg) => fail("Subscribing a second time should give an error")
      }
      eventBus publish eventMessage("only one message")
      probe expectMsg "only one message"
      probe expectNoMsg
    }

     "unsubsribe actors on termination" in new Ctx {
       whenReady(axonAkkaBridge subscribe probe.ref) { answer =>
         answer shouldBe Success("OK")
       }
      system.stop(probe.ref)
      Thread.sleep(200) // wait for async inner stuff to occur
      whenReady(axonAkkaBridge isSubscribed probe.ref) {
        answer => answer shouldBe false
      }
    }

  }
}
