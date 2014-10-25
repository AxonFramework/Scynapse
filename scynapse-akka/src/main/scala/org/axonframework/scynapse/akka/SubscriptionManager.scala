package org.axonframework.scynapse.akka

import akka.actor._
import scala.util.{Success, Failure}
import org.axonframework.domain.EventMessage
import org.axonframework.eventhandling.{EventBus => AxonEventBus, EventListener}


case class SubscriptionError(msg: String) extends RuntimeException(msg)


private[scynapse] class ActorEventListener(ref: ActorRef) extends EventListener {
    def handle(msg: EventMessage[_]) = {
        ref ! msg.getPayload
    }
}


private[scynapse] object SubscriptionManager {

    sealed trait Cmd

    case class Subscribe(ref: ActorRef) extends Cmd

    case class Unsubscribe(ref: ActorRef) extends Cmd

    case class CheckSubscription(ref: ActorRef) extends Cmd

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
                    sender ! Failure(SubscriptionError(s"$ref is already subscribed to $eventBus"))
                case None =>
                    val listener = new ActorEventListener(ref)
                    eventBus subscribe listener
                    subscriptions += ref -> listener
                    context watch ref
                    sender ! Success("OK")
            }

        case Unsubscribe(ref) =>
            subscriptions.get(ref) match {
                case Some(el) =>
                    eventBus unsubscribe el
                    subscriptions -= ref
                    context unwatch ref
                    sender ! Success("OK")
                case None =>
                    sender ! Failure(SubscriptionError(s"$ref is not subscribed to $eventBus"))
            }

        case Terminated(ref) =>
            self ! Unsubscribe(ref)

        case CheckSubscription(ref) =>
            sender ! subscriptions.contains(ref)
    }
}
