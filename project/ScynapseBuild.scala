import sbt._
import Keys._
import sbtrelease.ReleasePlugin._

object ScynapseBuild extends Build {

    import Deps._

    lazy val basicSettings = seq(
        organization := "org.axonframework.scynapse",
        description := "Scala add-on to the Axon framework",

        licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),

        scalaVersion := V.scala,

        scalacOptions := Seq(
            "-encoding", "UTF-8",
            "-unchecked",
            "-deprecation"
        )

    ) ++ releaseSettings

    lazy val moduleSettings = basicSettings ++ seq(
        publishMavenStyle := true,
        publishTo := {
            val nexus = "https://oss.sonatype.org/"
            if (isSnapshot.value)
                Some("snapshots" at nexus + "content/repositories/snapshots")
            else
                Some("releases"  at nexus + "service/local/staging/deploy/maven2")
        },
        publishArtifact in Test := false,
        pomIncludeRepository := { _ => false },
        pomExtra :=
          <url>https://github.com/AxonFramework/Scynapse</url>
          <issueManagement>
              <system>YouTrack</system>
              <url>http://issues.axonframework.org</url>
          </issueManagement>
          <scm>
              <url>git@github.com:AxonFramework/Scynapse.git</url>
              <connection>scm:git:git@github.com:AxonFramework/Scynapse.git</connection>
          </scm>
          <developers>
            <developer>
              <id>olger</id>
              <name>Olger Warnier</name>
                <email>olger@spectare.nl</email>
            </developer>
          </developers>
    )

    lazy val root = Project("scynapse-root", file("."))
      .settings(basicSettings: _*)
      .aggregate(scynapseCore, scynapseAkka, scynapseTest)

    lazy val scynapseCore = Project("scynapse-core", file("scynapse-core"))
      .settings(moduleSettings: _*)
      .settings(
          libraryDependencies ++= Seq(
              axonCore,
              scalaXml,
              scalaTest % "test"))

    lazy val scynapseAkka = Project("scynapse-akka", file("scynapse-akka"))
      .dependsOn(scynapseCore)
      .settings(moduleSettings: _*)
      .settings(
          libraryDependencies ++= Seq(
              akkaActor,
              akkaTestkit % "test",
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
        val scala = "2.12.2"
        val axon = "3.0.5"
        val akka = "2.5.6"
    }

    val axonCore = "org.axonframework" % "axon-core" % V.axon
    val axonTest = "org.axonframework" % "axon-test" % V.axon
    val scalaXml = "org.scala-lang.modules"  %% "scala-xml" % "1.0.6"
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % V.akka
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % V.akka
    val hamcrest = "org.hamcrest" % "hamcrest-core" % "1.3"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4"
}
