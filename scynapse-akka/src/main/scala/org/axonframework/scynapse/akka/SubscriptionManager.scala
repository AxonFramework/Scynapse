package org.axonframework.scynapse.akka

import java.util
import java.util.function.BiFunction

import akka.actor._
import org.axonframework.common.Registration

import scala.util.{Failure, Success}
import org.axonframework.eventhandling.{EventMessage, EventBus => AxonEventBus}
import org.axonframework.messaging.{Message, MessageDispatchInterceptor}

import scala.collection.mutable

case class SubscriptionError(msg: String) extends RuntimeException(msg)
case class SubscriptionPublishingError(msg: String, cause: Throwable) extends RuntimeException(msg, cause)

private[scynapse] class DispatchPayloadToAkkaInterceptor[T <: Message[_]](dispatchTo: ActorRef) extends MessageDispatchInterceptor[T] {
    override def handle(messages: util.List[T]): BiFunction[Integer, T, T] = (i, t) => {
        dispatchTo ! t.getPayload
        t
    }
}

private[scynapse] class DispatchMessageToAkkaInterceptor[T <: Message[_]](dispatchTo: ActorRef) extends MessageDispatchInterceptor[T] {
    override def handle(messages: util.List[T]): BiFunction[Integer, T, T] = (i, t) => {
        dispatchTo ! t
        t
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

    var subscriptions: mutable.Map[ActorRef, Registration] = mutable.Map()

//    context.system.registerOnTermination(() => {
//        context.system.log.info("system terminated, stopping all eventbus registrations")
//        subscriptions.foreach(i => i._2.close())
//        subscriptions.clear()
//    })

    def receive = {
        case Subscribe(ref, ofType) =>
            subscriptions.get(ref) match {
                case Some(el) =>
                    sender ! Failure(SubscriptionError(s"$ref is already subscribed to $eventBus"))
                case None =>
                    val listener = ofType match {
                        case TypeOfEvent.Payload => new DispatchPayloadToAkkaInterceptor[EventMessage[_]](ref)
                        case TypeOfEvent.Full => new DispatchMessageToAkkaInterceptor[EventMessage[_]](ref)
                    }
                    val registration = eventBus.registerDispatchInterceptor(listener)
                    subscriptions += ref -> registration
                    context watch ref
                    sender ! Success("OK")
            }

        case Unsubscribe(ref) =>
            subscriptions.get(ref) match {
                case Some(el) =>
                    el.close()
                    subscriptions.remove(ref)
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
