package org.axonframework.scynapse.akka

import java.util

import akka.actor._
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit, TestProbe}

import scala.concurrent.{Await, TimeoutException}
import org.scalatest.Entry
import org.axonframework.commandhandling.{CommandHandler, CommandMessage, SimpleCommandBus}
import org.axonframework.messaging.{Message, MessageHandler, MetaData}


class CommandGatewayActorSpec extends ScynapseAkkaSpecBase {
  trait Ctx {
    case class AddNumbersCmd(x: Int, y: Int) extends CommandMessage[AddNumbersCmd] {
      override def withMetaData(metaData: util.Map[String, _]): CommandMessage[AddNumbersCmd] = ???

      override def andMetaData(metaData: util.Map[String, _]): CommandMessage[AddNumbersCmd] = ???

      override def getCommandName: String = this.getClass.getSimpleName

      override def getIdentifier: String = ???

      override def getPayloadType: Class[AddNumbersCmd] = ???

      override def getPayload: AddNumbersCmd = ???

      override def getMetaData: MetaData = ???
    }

    case class LogMessageCmd(message: String) extends CommandMessage[LogMessageCmd] {

      override def withMetaData(metaData: util.Map[String, _]): CommandMessage[LogMessageCmd] = ???

      override def andMetaData(metaData: util.Map[String, _]): CommandMessage[LogMessageCmd] = ???

      override def getCommandName: String = this.getClass.getSimpleName

      override def getIdentifier: String = ???

      override def getPayloadType: Class[LogMessageCmd] = ???

      override def getPayload: LogMessageCmd = ???

      override def getMetaData: MetaData = ???
    }

    case class Result(x: Any)

    val probe = TestProbe()

    def cmdHandler[T <: Message[_], R](f: T => R): MessageHandler[T] =
      (message: T) => Result(f(message))

//    def nullHandler[T]: MessageHandler[T] =
//      (message: CommandMessage[_]) => null

    val commandBus = new SimpleCommandBus()

    val addNumbersHandler =
      cmdHandler { (msg: CommandMessage[_]) => {
        msg.getPayload() match {
          case p: AddNumbersCmd => p.x + p.y
          case other => //
        }
      } }

    def forwardingHandler[T](ref: ActorRef) =
      cmdHandler { (msg: CommandMessage[_]) => ref ! msg.getPayload }

    commandBus.subscribe(classOf[AddNumbersCmd].getName, addNumbersHandler)
    commandBus.subscribe(classOf[LogMessageCmd].getName,
      forwardingHandler[LogMessageCmd](probe.ref))

    val cmdGateway = system.actorOf(CommandGatewayActor.props(commandBus))
  }

  behavior of "Akka-based Axon Command gateway"

  it should "forward commands to Command Bus" in new Ctx {
    cmdGateway ! LogMessageCmd("hi")
    probe expectMsg LogMessageCmd("hi")
  }

  it should "receive command result back" in new Ctx {
    val resFuture = (cmdGateway ? AddNumbersCmd(5, 4)).mapTo[Result]
    Await.result(resFuture, timeout.duration) shouldBe Result(9)
  }

//  it should "not receive any result if cmd handler returns null" in new Ctx {
//    case class NullCmd() extends CommandMessage[NullCmd]
//    commandBus.subscribe(classOf[NullCmd].getName, nullHandler[NullCmd])
//
//    cmdGateway.tell(NullCmd(), probe.ref)
//    probe expectNoMsg
//  }

  it should "allow to pass command metadata" in new Ctx {
    case class MetaCmd(data: String) extends CommandMessage[MetaCmd] {
      override def withMetaData(metaData: util.Map[String, _]): CommandMessage[MetaCmd] = ???

      override def andMetaData(metaData: util.Map[String, _]): CommandMessage[MetaCmd] = ???

      override def getCommandName: String = this.getClass.getSimpleName

      override def getIdentifier: String = ???

      override def getPayloadType: Class[MetaCmd] = ???

      override def getPayload: MetaCmd = ???

      override def getMetaData: MetaData = ???
    }

    commandBus.subscribe(classOf[MetaCmd].getName,
      cmdHandler { (msg: CommandMessage[_]) =>
        probe.ref ! msg.getMetaData
      })
    cmdGateway ! CommandGatewayActor.WithMeta(MetaCmd("hi"), Map("userId" -> 120))
    probe.expectMsgType[MetaData] should contain (Entry("userId", 120))
  }
}
