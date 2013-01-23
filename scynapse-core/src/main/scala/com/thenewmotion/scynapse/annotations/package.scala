package com.thenewmotion.scynapse

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier
import annotation.meta.getter

package object annotations {
  type aggregateId = TargetAggregateIdentifier @getter
}
