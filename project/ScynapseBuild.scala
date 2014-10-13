import sbt._
import Keys._
import bintray.Plugin._
import sbtrelease.ReleasePlugin._
import scala._

object ScynapseBuild extends Build {
  import Deps._

  lazy val basicSettings = seq(
    organization := "com.thenewmotion",
    description  := "Scala add-on to Axon framework",

    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),

    bintray.Keys.bintrayOrganization := Some("thenewmotion"),
    resolvers += bintray.Opts.resolver.mavenRepo("thenewmotion"),

    scalaVersion := V.scala,

    scalacOptions := Seq(
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation"
    )

  ) ++ releaseSettings ++ bintraySettings

  lazy val moduleSettings = basicSettings ++ seq(
    publishMavenStyle := true,
    pomExtra :=
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
  )

  lazy val root = Project("scynapse-root", file("."))
    .settings(basicSettings: _*)
    .aggregate(scynapseCore, scynapseAkka, scynapseTest)

  lazy val scynapseCore = Project("scynapse-core", file("scynapse-core"))
    .settings(moduleSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        axonCore,
        scalaTest % "test"))

  lazy val scynapseAkka = Project("scynapse-akka", file("scynapse-akka"))
    .dependsOn(scynapseCore)
    .settings(moduleSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        akkaActor,
        scalaTest % "test"))

  lazy val scynapseTest = Project("scynapse-test", file("scynapse-test"))
    .dependsOn(scynapseCore)
    .settings(moduleSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        axonTest,
        hamcrest,
        scalaTest
      ))
}

object Deps {
  object V {
    val scala = "2.11.2"
    val axon  = "2.3.2"
    val akka = "2.3.6"
  }

  val axonCore  = "org.axonframework" %  "axon-core"      % V.axon
  val axonTest  = "org.axonframework" %  "axon-test"      % V.axon
  val akkaActor = "com.typesafe.akka" %% "akka-actor"     % V.akka
  val hamcrest  = "org.hamcrest"      %  "hamcrest-core"  % "1.3"
  val scalaTest = "org.scalatest"     %% "scalatest"      % "2.2.1"
}
