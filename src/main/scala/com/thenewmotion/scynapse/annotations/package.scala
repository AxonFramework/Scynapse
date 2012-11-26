package com.thenewmotion.scynapse

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier
import annotation.target.field

package object annotations {
  type aggregateId = TargetAggregateIdentifier @field
}
