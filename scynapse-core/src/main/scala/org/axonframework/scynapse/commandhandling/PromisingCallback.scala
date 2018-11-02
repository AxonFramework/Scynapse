package org.axonframework.scynapse.commandhandling

import org.axonframework.commandhandling.{CommandCallback, CommandMessage, CommandResultMessage}

import concurrent._

/**
  * Promise describing a callback that is invoked when command handler execution has finished. Depending of the outcome
  * of the execution, the Promise will result in the expected Type or will call failure with a Throwable.
  *
  * @tparam[T] type of CommandMessage
  * @tparam[R] type of result of the command handling
  */
class PromisingCallback[T, R] extends CommandCallback[T, R] {

  private val p = Promise[R]()

  override def onResult(
    commandMessage: CommandMessage[_ <: T],
    commandResultMessage: CommandResultMessage[_ <: R]
  ): Unit = {
    if (commandResultMessage.isExceptional) {
      p.failure(commandResultMessage.exceptionResult())
    } else {
      p.success(commandResultMessage.getPayload)
    }
  }

  def future = p.future
}
