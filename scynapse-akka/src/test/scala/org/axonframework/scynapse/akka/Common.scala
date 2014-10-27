package org.axonframework.scynapse.akka

import org.scalatest.{FunSpecLike, FlatSpecLike, Matchers}
import akka.actor.ActorSystem
import akka.testkit.{TestKit, ImplicitSender}
import akka.util.Timeout
import concurrent.duration._


class ScynapseAkkaSpecBase extends FlatSpecLike with Matchers {
  final implicit val timeout = Timeout(5 seconds)
  implicit val system = ActorSystem("scynapse-akka")
}
