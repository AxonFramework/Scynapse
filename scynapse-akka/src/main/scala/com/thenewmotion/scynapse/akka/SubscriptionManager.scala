package com.thenewmotion.scynapse.akka

import akka.actor._
import org.axonframework.domain.EventMessage
import org.axonframework.eventhandling.{EventBus => AxonEventBus, EventListener}


private[scynapse] class ActorEventListener(ref: ActorRef) extends EventListener {
  def handle(msg: EventMessage[_]) = {
    ref ! msg.getPayload
  }
}


private[scynapse] object SubscriptionManager {
  case class Subscribe(ref: ActorRef)
  case class Unsubscribe(ref: ActorRef)

  def props(eventBus: AxonEventBus) = Props(new SubscriptionManager(eventBus))
}


private[scynapse] class SubscriptionManager(eventBus: AxonEventBus)
    extends Actor with ActorLogging {
  import SubscriptionManager._

  var subscriptions: Map[ActorRef, ActorEventListener] = Map()

  def receive = {
    case Subscribe(ref) =>
      subscriptions.get(ref) match {
        case Some(el) =>
          log.debug("{} is already subscribed to {}", ref, eventBus)
        case None =>
          val listener = new ActorEventListener(ref)
          eventBus subscribe listener
          subscriptions += ref -> listener
      }

    case Unsubscribe(ref) =>
      subscriptions.get(ref) match {
        case Some(el) =>
          eventBus unsubscribe el
          subscriptions -= ref
        case None =>
          log.info("{} is not subscribed to anything", ref)
      }
  }
}
