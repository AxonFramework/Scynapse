
lazy val basicSettings = {
  val currentScalaVersion = "2.12.7"
  val scala211Version     = "2.11.12"

  Seq(
    organization := "org.axonframework.scynapse",
    description := "Scala add-on to the Axon framework",
    licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := currentScalaVersion,
    crossScalaVersions := Seq(currentScalaVersion, scala211Version),
    releaseCrossBuild := true,
    scalacOptions := Seq(
        "-encoding", "UTF-8",
        "-unchecked",
        "-deprecation"
    ),
    scalafmtConfig := Some((baseDirectory in ThisBuild).value / "project/.scalafmt.conf"),
    scalafmtOnCompile := true
  )
}

lazy val moduleSettings = basicSettings ++ Seq(
  publishTo := Some(
    if (isSnapshot.value)
         Opts.resolver.sonatypeSnapshots
       else
         Opts.resolver.sonatypeStaging
    ),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ ⇒ false },
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

lazy val scynapseRoot = (project in file("."))
  .settings(basicSettings: _*)
  .aggregate(scynapseCore, scynapseAkka, scynapseTest)

val scynapseCore = (project in file("scynapse-core"))
  .settings(moduleSettings: _*)
  .enablePlugins(ReleasePlugin)
  .settings(
    name := "scynapse-core",
      libraryDependencies ++= Seq(
          Dependencies.axonCore,
        Dependencies.scalaXml,
        Dependencies.scalaTest % "test"))

val scynapseAkka = (project in file("scynapse-akka"))
  .dependsOn(scynapseCore)
  .settings(moduleSettings: _*)
  .enablePlugins(ReleasePlugin)
  .settings(
    name := "scynapse-akka",
      libraryDependencies ++= Seq(
        Dependencies.akkaActor,
        Dependencies.akkaTestkit % "test",
        Dependencies.scalaTest % "test",
        Dependencies.logback % "test"))

val scynapseTest = (project in file("scynapse-test"))
  .dependsOn(scynapseCore)
  .settings(moduleSettings: _*)
  .enablePlugins(ReleasePlugin)
  .settings(
    name := "scynapse-test",
      libraryDependencies ++= Seq(
        Dependencies.axonTest,
        Dependencies.hamcrest,
        Dependencies.scalaTest
      ))
