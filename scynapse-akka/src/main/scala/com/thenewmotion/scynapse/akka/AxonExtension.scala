package com.thenewmotion.scynapse.akka

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.util.{Try, Success, Failure}
import org.axonframework.domain.EventMessage
import org.axonframework.eventhandling.{EventBus => AxonEventBus, EventListener}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object AxonEventBusExtension extends ExtensionId[AxonEventBusExtension]
                             with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem): AxonEventBusExtension =
    new AxonEventBusExtension(system)

  def lookup(): this.type = this

}


private[scynapse] trait Subscriptions {
  def eventBus: AxonEventBus

  def subscribe(ref: ActorRef): Try[_]
  def unsubscribe(ref: ActorRef): Try[_]
  def isSubscribed(ref: ActorRef): Boolean
}


class AxonEventBusExtension(system: ActorSystem) extends Extension {

  def forEventBus(bus: AxonEventBus) = new Subscriptions {
    import SubscriptionManager._
    implicit val timeout = Timeout(1 second)

    val eventBus: AxonEventBus = bus
    val manager = system.actorOf(SubscriptionManager.props(eventBus))

    private[this] def sendManagerCmd(cmd: SubscriptionManager.Cmd): Future[Try[_]] =
      (manager ? cmd).mapTo[Try[_]]

    def subscribe(ref: ActorRef) =
      Await.result(sendManagerCmd(Subscribe(ref)), timeout.duration)

    def unsubscribe(ref: ActorRef) =
      Await.result(sendManagerCmd(Unsubscribe(ref)), timeout.duration)

    def isSubscribed(ref: ActorRef) =
      Await.result((manager ? CheckSubscription(ref)).mapTo[Boolean], timeout.duration)
  }
}
