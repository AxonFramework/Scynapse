package org.axonframework.scynapse.akka

import akka.util.Timeout
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._


class ScynapseAkkaSpecBase extends FlatSpecLike with Matchers {
    final implicit val timeout = Timeout(5 seconds)
}
