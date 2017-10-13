package org.axonframework.scynapse.akka

import akka.actor._

import scala.util.{Failure, Success}
import org.axonframework.eventhandling.{EventListener, EventMessage, EventBus => AxonEventBus}


case class SubscriptionError(msg: String) extends RuntimeException(msg)
case class SubscriptionPublishingError(msg: String, cause: Throwable) extends RuntimeException(msg, cause)


private[scynapse] class ActorEventListenerPayloadPublisher(ref: ActorRef) extends EventListener {
    def handle(msg: EventMessage[_]) = {
        ref ! msg.getPayload
    }
}

private[scynapse] class ActorEventListenerFullPublisher(ref: ActorRef) extends EventListener {
    def handle(msg: EventMessage[_]) = {
        try {
            ref ! msg
        } catch {
            case ex: Exception => throw SubscriptionPublishingError(s"$msg could not be sent to Actor ${ref.path}", ex)
        }
    }
}


private[scynapse] object SubscriptionManager {

    sealed trait Cmd

    // enum TypeOfEvent in a way that exhaustive checks are done with matching
    sealed trait TypeOfEvent
    object TypeOfEvent {
        case object Full extends TypeOfEvent
        case object Payload extends TypeOfEvent
    }

    //default value is Payload in order to keep backwards compatibility
    case class Subscribe(ref: ActorRef, typeOfEvent: TypeOfEvent = TypeOfEvent.Payload) extends Cmd
    
    case class Unsubscribe(ref: ActorRef) extends Cmd

    case class CheckSubscription(ref: ActorRef) extends Cmd

    def props(eventBus: AxonEventBus) = Props(new SubscriptionManager(eventBus))


}


private[scynapse] class SubscriptionManager(eventBus: AxonEventBus)
  extends Actor with ActorLogging {

    import SubscriptionManager._

    var subscriptions: Map[ActorRef, EventListener] = Map()

    def receive = {
        case Subscribe(ref, ofType) =>
            subscriptions.get(ref) match {
                case Some(el) =>
                    sender ! Failure(SubscriptionError(s"$ref is already subscribed to $eventBus"))
                case None =>
                    val listener = ofType match {
                        case TypeOfEvent.Payload => new ActorEventListenerPayloadPublisher(ref)
                        case TypeOfEvent.Full => new ActorEventListenerFullPublisher(ref)
                    }
                    //TODO check how eventbus impl works nowadays
                    //eventBus.subscribe(listener)
                    subscriptions += ref -> listener
                    context watch ref
                    sender ! Success("OK")
            }

        case Unsubscribe(ref) =>
            subscriptions.get(ref) match {
                case Some(el) =>
                    //eventBus.unsubscribe(el)
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
