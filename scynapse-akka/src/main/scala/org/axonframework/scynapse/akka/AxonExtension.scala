package org.axonframework.scynapse.akka

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.util.{Try, Success, Failure}
import org.axonframework.domain.EventMessage
import org.axonframework.eventhandling.{EventBus => AxonEventBus, EventListener}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


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
    import SubscriptionManager._
    implicit val timeout = Timeout(1 second)

    val eventBus: AxonEventBus = bus
    val manager = system.actorOf(SubscriptionManager.props(eventBus))

    private[this] def sendManagerCmd(cmd: SubscriptionManager.Cmd): Future[Try[_]] =
      (manager ? cmd).mapTo[Try[_]]

    /**
     * subscribe to the eventbus and the actor will receive only the typed payload of the events
     * @param ref ActorRef that will receive the events
     */
    def subscribe(ref: ActorRef) =
      Await.result(sendManagerCmd(Subscribe(ref, TypeOfEvent.Payload)), timeout.duration)

    /**
     * subscribe to the eventbus and the actor will receive the full DomainEventMessage
     * @see org.axonframework.domain.DomainEventMessage
     * @param ref ActorRef that will receive the DomainEventMessages
     */
    def subscribeEvent(ref: ActorRef) =
      Await.result(sendManagerCmd(Subscribe(ref, TypeOfEvent.Full)), timeout.duration)

    def unsubscribe(ref: ActorRef) =
      Await.result(sendManagerCmd(Unsubscribe(ref)), timeout.duration)

    def isSubscribed(ref: ActorRef) =
      Await.result((manager ? CheckSubscription(ref)).mapTo[Boolean], timeout.duration)
  }
}
