package org.axonframework.scynapse.akka

import akka.actor._
import akka.pattern.ask
import akka.testkit.{TestProbe}
import scala.concurrent.Await
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

    def cmdHandler[T, R <: Any](f: T => R): CommandHandler[T] =
      new CommandHandler[T] {
        def handle(cmd: CommandMessage[T], uow: UnitOfWork): AnyRef =
          Result(f(cmd.getPayload))
      }

    val commandBus = new SimpleCommandBus()

    val addNumbersHandler =
      cmdHandler { (cmd: AddNumbersCmd) => cmd.x + cmd.y }

    def forwardingHandler[T](ref: ActorRef) =
      cmdHandler { (cmd: T) => ref ! cmd }

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
}
