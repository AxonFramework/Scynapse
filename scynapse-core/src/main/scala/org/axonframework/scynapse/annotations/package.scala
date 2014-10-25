package org.axonframework.scynapse

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier
import annotation.meta.getter

package object annotations {

    /**
     * Field or method level annotation that marks a field or method providing the identifier of the aggregate that a
     * command targets.
     * <p/>
     * If placed on a method, that method must contain no parameters. The return value will be used as the
     * Aggregate Identifier.
     * <p/>
     * If placed on a field, the field's value will be converted into an AggregateIdentifier instance identical to how a
     * method's return value is converted.
     *
     * @see org.axonframework.commandhandling.annotation.TargetAggregateIdentifier
     */
    type aggregateId = TargetAggregateIdentifier@getter
}
