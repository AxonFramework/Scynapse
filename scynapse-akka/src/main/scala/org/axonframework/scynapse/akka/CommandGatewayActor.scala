package org.axonframework.scynapse.akka

import akka.actor._
import akka.pattern.pipe
import scala.concurrent.{Future, ExecutionContext}
import scala.collection.JavaConverters._
import org.axonframework.scynapse.commandhandling.PromisingCallback
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.{CommandMessage, GenericCommandMessage}


object CommandGatewayActor {
  type CommandMeta = Map[String, Any]

  case class WithMeta(cmd: Any, meta: CommandMeta)

  def props(axonCommandBus: CommandBus) =
    Props(new CommandGatewayActor(axonCommandBus))
}


/**
  * Actor-based command gateway
  *
  * @param axonCommandBus [[org.axonframework.commandhandling.CommandBus]]
  */
class CommandGatewayActor(axonCommandBus: CommandBus) extends Actor with ActorLogging {
  import CommandGatewayActor._

  implicit val executor: ExecutionContext = ExecutionContext.Implicits.global

  def asCommandMessage(cmd: Any, meta: CommandMeta = Map.empty): CommandMessage[_] = {
    val msg = GenericCommandMessage.asCommandMessage(cmd)
    msg.withMetaData(meta.asJava)
  }

  def dispatchMessage[T](cmd: CommandMessage[T]): Future[Any] = {
    val pc = new PromisingCallback[T, Any]
    axonCommandBus.dispatch(cmd, pc)
    pc.future
  }

  def receive = {
    case cmdMessage: CommandMessage[_] =>
      val respondTo = sender()
      for(x <- dispatchMessage(cmdMessage) if x != null) {
        respondTo ! x
      }

    case WithMeta(cmd, meta) =>
      self forward asCommandMessage(cmd, meta)

    case cmd =>
      self forward asCommandMessage(cmd)
  }
}
