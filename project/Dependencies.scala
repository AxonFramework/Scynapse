import sbt._

object Dependencies {

  lazy val scalaV = "2.12.3"
  lazy val axonV = "3.0.5"
  lazy val akkaV = "2.5.6"

  val axonCore = "org.axonframework" % "axon-core" % axonV
  val axonTest = "org.axonframework" % "axon-test" % axonV
  val scalaXml = "org.scala-lang.modules"  %% "scala-xml" % "1.0.6"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaV
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaV
  val hamcrest = "org.hamcrest" % "hamcrest-core" % "1.3"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4"
}
