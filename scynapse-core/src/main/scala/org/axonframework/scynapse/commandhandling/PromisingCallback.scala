package org.axonframework.scynapse.commandhandling

import org.axonframework.commandhandling.{CommandCallback, CommandMessage}

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

    override def onSuccess(commandMessage: CommandMessage[_ <: T], result: R) = {
        p.success(result)
    }

    override def onFailure(commandMessage: CommandMessage[_ <: T], cause: Throwable) = {
        p.failure(cause)
    }

    def future = p.future
}

