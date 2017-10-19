package org.axonframework.scynapse.akka

import akka.actor._
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout

import org.scalatest.{BeforeAndAfterAll, Entry, Matchers, WordSpecLike}
import org.axonframework.commandhandling.{CommandMessage, GenericCommandMessage, SimpleCommandBus}
import org.axonframework.messaging.{Message, MessageHandler, MetaData}
import org.scalatest.concurrent.ScalaFutures

class CommandGatewayActorSpec extends TestKit(ActorSystem("AxonExtensionSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with ScalaFutures {

  trait Ctx {
    case class AddNumbersPayload(x: Int, y: Int)
    case class AddNumbersCmd(payload: AddNumbersPayload) extends GenericCommandMessage[AddNumbersPayload](payload)

    case class LogMessagePayload(message: String)
    case class LogMessageCmd(payload: LogMessagePayload) extends GenericCommandMessage[LogMessagePayload](payload)

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
          case p: AddNumbersPayload => p.x + p.y
          case other => //
        }
      } }

    def forwardingHandler[T](ref: ActorRef) =
      cmdHandler { (msg: CommandMessage[_]) => ref ! msg.getPayload }

    commandBus.subscribe(classOf[AddNumbersPayload].getName, addNumbersHandler)
    commandBus.subscribe(classOf[LogMessagePayload].getName,
      forwardingHandler[LogMessageCmd](probe.ref))

    val cmdGateway = system.actorOf(CommandGatewayActor.props(commandBus))
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Axon <-> Akka Command gateway" must {
    import scala.concurrent.duration._
    implicit val timeout = Timeout(5.seconds)
    val probe = TestProbe()

    "forward commands to Command Bus" in new Ctx {
      cmdGateway ! LogMessageCmd(LogMessagePayload("hi"))
      probe expectMsg LogMessagePayload("hi")
    }

    "receive command result back" in new Ctx {
      whenReady(cmdGateway ? AddNumbersCmd(AddNumbersPayload(5, 4))) { answer => answer shouldBe Result(9)}
    }

    // "not receive any result if cmd handler returns null" in new Ctx {
    //    case class NullCmd() extends CommandMessage[NullCmd]
    //    commandBus.subscribe(classOf[NullCmd].getName, nullHandler[NullCmd])
    //
    //    cmdGateway.tell(NullCmd(), probe.ref)
    //    probe expectNoMsg
    //  }

    "allow to pass command metadata" in new Ctx {

      case class MetaPayload(data: String)
      case class MetaCmd(payload: MetaPayload) extends GenericCommandMessage[MetaPayload](payload)

      commandBus.subscribe(classOf[MetaPayload].getName,
        cmdHandler { (msg: CommandMessage[_]) =>
          probe.ref ! msg.getMetaData
        })
      cmdGateway ! CommandGatewayActor.WithMeta(MetaCmd(MetaPayload("hi")), Map("userId" -> 120))
      probe.expectMsgType[MetaData] should contain(Entry("userId", 120))
    }
  }
}
