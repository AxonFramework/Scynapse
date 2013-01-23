import sbt._
import Keys._

import sbtrelease.ReleasePlugin._
import scala._

object ScynapseBuild extends Build {

  lazy val basicSettings = seq(
    organization := "com.thenewmotion",
    description  := "Scala add-on to Axon framework",

    scalaVersion := "2.10.0",
    resolvers ++= Seq(
      "Releases"  at "http://nexus.thenewmotion.com/content/repositories/releases",
      "Snapshots" at "http://nexus.thenewmotion.com/content/repositories/snapshots"
    ),

    scalacOptions := Seq(
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation"
    )
  )

  lazy val moduleSettings = basicSettings ++ releaseSettings ++ seq(
    publishTo <<= version { (v: String) =>
      val nexus = "http://nexus.thenewmotion.com/content/repositories/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "snapshots-public")
      else                             Some("releases"  at nexus + "releases-public")
    },
    publishMavenStyle := true,
    pomExtra :=
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )

  lazy val root = Project("root", file("."))
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)
    .aggregate(scynapseCore, scynapseTest)

  lazy val scynapseCore = Project("scynapse-core", file("scynapse-core"))
    .settings(moduleSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.axonframework" %  "axon-core"    % "2.0",
        "org.specs2"        %% "specs2"       % "1.13" % "test"))

  lazy val scynapseTest = Project("scynapse-test", file("scynapse-test"))
    .dependsOn(scynapseCore)
    .settings(moduleSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.axonframework" % "axon-test"     % "2.0",
        "org.hamcrest"      % "hamcrest-core" % "1.3",
        "org.specs2"        %% "specs2"       % "1.13"
      ))
}