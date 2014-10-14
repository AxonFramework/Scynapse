package com.thenewmotion.scynapse.akka

import akka.actor._
import org.axonframework.domain.EventMessage
import org.axonframework.eventhandling.{EventBus => AxonEventBus, EventListener}


object AxonEventBusExtension extends ExtensionId[AxonEventBusExtension]
                             with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem): AxonEventBusExtension =
    new AxonEventBusExtension(system)

  def lookup(): this.type = this

}


private[scynapse] trait Subscriptions {
  def eventBus: AxonEventBus

  def subscribe(ref: ActorRef)
  def unsubscribe(ref: ActorRef)
}


class AxonEventBusExtension(system: ActorSystem) extends Extension {

  def forEventBus(bus: AxonEventBus) = new Subscriptions {
    import SubscriptionManager._
    val eventBus: AxonEventBus = bus
    val manager = system.actorOf(SubscriptionManager.props(eventBus))

    def subscribe(ref: ActorRef) = {
      manager ! Subscribe(ref)
    }

    def unsubscribe(ref: ActorRef) = {
      manager ! unsubscribe(ref)
    }
  }
}
