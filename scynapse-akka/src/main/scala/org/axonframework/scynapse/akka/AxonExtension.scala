package org.axonframework.scynapse.akka

import java.util
import java.util.function.BiFunction

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.axonframework.common.Registration

import scala.util.Try
import org.axonframework.eventhandling.{EventMessage, EventBus => AxonEventBus}
import org.axonframework.messaging.{Message, MessageDispatchInterceptor}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import collection.JavaConverters._


/**
  * The [[akka.actor.ExtensionId]] and [[akka.actor.ExtensionIdProvider]] for Axon extension
  */
object AxonEventBusExtension extends ExtensionId[AxonEventBusExtension]
                             with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem): AxonEventBusExtension =
    new AxonEventBusExtension(system)

  def lookup(): this.type = this

}

/**
  * Defines an interface to subscribe Akka actors to Axon event bus
  */
private[scynapse] trait AxonAkkaBridge {
  def eventBus: AxonEventBus

  def subscribe(ref: ActorRef): Try[_]
  def subscribeEvent(ref: ActorRef): Try[_]
  def unsubscribe(ref: ActorRef): Try[_]
  def isSubscribed(ref: ActorRef): Boolean
}


/**
  * Axon extension
  *
  * @param system The ActorSystem this extension belongs to
  */
class AxonEventBusExtension(system: ActorSystem) extends Extension {

  /**
    * Gets a "bridge" object providing interface to manage actors'
    * subscriptions.
    *
    * @param bus [[org.axonframework.eventhandling.EventBus]]
    * @return a [[org.axonframework.scynapse.akka.AxonAkkaBridge]]
    */
  def forEventBus(bus: AxonEventBus) = new AxonAkkaBridge {
//    import SubscriptionManager._
    implicit val timeout = Timeout(1 second)
    val eventBus: AxonEventBus = bus
    var subscriptions: mutable.Map[ActorRef, Registration] = mutable.Map()

    val manager = system.actorOf(SubscriptionManager.props(eventBus))

    /**
     * subscribe to the eventbus and the actor will receive only the typed payload of the events
     * @param ref ActorRef that will receive the events
     */
    def subscribe(ref: ActorRef) = {
      Try {
        val registration = bus.registerDispatchInterceptor(new DispatchToAkkaInterceptor[EventMessage[_]](ref))
        subscriptions += ((ref, registration))
      }
    }

    /**
     * subscribe to the eventbus and the actor will receive the full DomainEventMessage
     * @see org.axonframework.domain.DomainEventMessage
     * @param ref ActorRef that will receive the DomainEventMessages
     */
    def subscribeEvent(ref: ActorRef) = {
      Try {
        val registration = bus.registerDispatchInterceptor(new DispatchToAkkaInterceptor[EventMessage[_]](ref))
        subscriptions.+=((ref, registration))
      }
    }

    def unsubscribe(ref: ActorRef) =
      subscriptions.remove(ref).map(r => {
          Try(r.close())
      }).get


    def isSubscribed(ref: ActorRef) =
      subscriptions.contains(ref)
      //Await.result((manager ? CheckSubscription(ref)).mapTo[Boolean], timeout.duration)
  }
}

class DispatchToAkkaInterceptor[T <: Message[_]](dispatchTo: ActorRef) extends MessageDispatchInterceptor[T] {
  override def handle(messages: util.List[T]): BiFunction[Integer, T, T]= ???
}
