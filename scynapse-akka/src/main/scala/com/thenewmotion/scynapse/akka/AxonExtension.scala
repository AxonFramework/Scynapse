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

  def subscribe(ref: ActorRef)
  def unsubscribe(ref: ActorRef)
}

// TODO:
// - unsubscribe on actor termination
//
class AxonEventBusExtension(system: ActorSystem) extends Extension {

  def forEventBus(bus: AxonEventBus) = new Subscriptions {
    import SubscriptionManager._
    implicit val timeout = Timeout(1 second)

    val eventBus: AxonEventBus = bus
    val manager = system.actorOf(SubscriptionManager.props(eventBus))

    def sendManagerCmd(cmd: SubscriptionManager.Cmd): Future[Try[_]] =
      (manager ? cmd).mapTo[Try[_]]

    def subscribe(ref: ActorRef) =
      Await.result(sendManagerCmd(Subscribe(ref)), timeout.duration)

    def unsubscribe(ref: ActorRef) =
      Await.result(sendManagerCmd(Unsubscribe(ref)), timeout.duration)
  }
}
