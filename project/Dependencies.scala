import sbt._

object Dependencies {

  lazy val scalaV = "2.12.7"
  lazy val axonV = "4.0"
  lazy val akkaV = "2.5.17"

  val axonTest = "org.axonframework" % "axon-test" % axonV
  val axonConfig = "org.axonframework" % "axon-configuration" % axonV
  val axonEventSourcing = "org.axonframework" % "axon-eventsourcing" % axonV
  val axonMessaging = "org.axonframework" % "axon-messaging" % axonV
  val axonModelling = "org.axonframework" % "axon-modelling" % axonV
  val axonMongo = "org.axonframework.extensions.mongo" % "axon-mongo" % axonV
  val scalaXml = "org.scala-lang.modules"  %% "scala-xml" % "1.0.6"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaV
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaV
  val hamcrest = "org.hamcrest" % "hamcrest-core" % "1.3"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4"
  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
