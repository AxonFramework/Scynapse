package com.thenewmotion.scynapse.commandhandling

import org.axonframework.commandhandling.CommandCallback
import concurrent._


class PromisingCallback[T] extends CommandCallback[T] {
  private val p = promise[T]()

  override def onSuccess(v: T) {
    p.success(v)
  }

  override def onFailure(t: Throwable) {
    p.failure(t)
  }

  def future = p.future
}

