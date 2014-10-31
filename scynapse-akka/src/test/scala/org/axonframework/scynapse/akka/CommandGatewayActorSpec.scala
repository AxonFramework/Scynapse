package org.axonframework.scynapse.akka

import akka.actor._
import akka.pattern.ask
import akka.testkit.{TestProbe, TestKit, ImplicitSender}
import scala.concurrent.{Await, TimeoutException}
import org.scalatest.Entry
import org.axonframework.domain.{MetaData => AxonMetaData}
import org.axonframework.commandhandling.{
  CommandBus, SimpleCommandBus,
  CommandHandler, CommandMessage}
import org.axonframework.unitofwork.UnitOfWork


class CommandGatewayActorSpec extends ScynapseAkkaSpecBase {
  trait Ctx {
    case class AddNumbersCmd(x: Int, y: Int)
    case class LogMessageCmd(message: String)
    case class Result(x: Any)

    val probe = TestProbe()

    def cmdHandler[T, R <: Any](f: CommandMessage[T] => R): CommandHandler[T] =
      new CommandHandler[T] {
        def handle(cmd: CommandMessage[T], uow: UnitOfWork): AnyRef =
          Result(f(cmd))
      }

    def nullHandler[T]: CommandHandler[T] =
      new CommandHandler[T] {
        def handle(cmd: CommandMessage[T], uow: UnitOfWork) = {
          null
        }
      }

    val commandBus = new SimpleCommandBus()

    val addNumbersHandler =
      cmdHandler { (msg: CommandMessage[AddNumbersCmd]) => {
        val cmd = msg.getPayload
        cmd.x + cmd.y
      } }

    def forwardingHandler[T](ref: ActorRef) =
      cmdHandler { (msg: CommandMessage[T]) => ref ! msg.getPayload }

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

  it should "not receive any result if cmd handler returns null" in new Ctx {
    case class NullCmd()
    commandBus.subscribe(classOf[NullCmd].getName, nullHandler[NullCmd])

    cmdGateway.tell(NullCmd(), probe.ref)
    probe expectNoMsg
  }

  it should "allow to pass command metadata" in new Ctx {
    case class MetaCmd(data: String)
    commandBus.subscribe(classOf[MetaCmd].getName,
      cmdHandler { (msg: CommandMessage[MetaCmd]) =>
        probe.ref ! msg.getMetaData
      })
    cmdGateway ! CommandGatewayActor.WithMeta(MetaCmd("hi"), Map("userId" -> 120))
    probe.expectMsgType[AxonMetaData] should contain (Entry("userId", 120))
  }
}
