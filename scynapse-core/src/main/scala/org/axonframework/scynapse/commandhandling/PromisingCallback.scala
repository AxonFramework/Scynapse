package org.axonframework.scynapse.commandhandling

import org.axonframework.commandhandling.CommandCallback
import concurrent._

/**
 * Promise describing a callback that is invoked when command handler execution has finished. Depending of the outcome
 * of the execution, the Promise will result in the expected Type or will call failure with a Throwable.
 *
 * @tparam[T] type of result of the command handling
 */
class PromisingCallback[T] extends CommandCallback[T] {
    private val p = Promise[T]()

    override def onSuccess(v: T) {
        p.success(v)
    }

    override def onFailure(t: Throwable) {
        p.failure(t)
    }

    def future = p.future
}

