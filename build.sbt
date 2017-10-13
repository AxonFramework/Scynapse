
lazy val basicSettings = Seq(
    organization := "org.axonframework.scynapse",
    description := "Scala add-on to the Axon framework",
    licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := Dependencies.scalaV,
    scalacOptions := Seq(
        "-encoding", "UTF-8",
        "-unchecked",
        "-deprecation"
    )
)

lazy val moduleSettings = basicSettings ++ Seq(
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

lazy val scynapseRoot = (project in file("."))
  .settings(basicSettings: _*)
  .enablePlugins(ReleasePlugin)
  //.settings(releaseSettings)
  .aggregate(scynapseCore, scynapseAkka, scynapseTest)

val scynapseCore = (project in file("scynapse-core"))
  .settings(moduleSettings: _*)
  .settings(
      libraryDependencies ++= Seq(
          Dependencies.axonCore,
        Dependencies.scalaXml,
        Dependencies.scalaTest % "test"))

val scynapseAkka = (project in file("scynapse-akka"))
  .dependsOn(scynapseCore)
  .settings(moduleSettings: _*)
  .settings(
      libraryDependencies ++= Seq(
        Dependencies.akkaActor,
        Dependencies.akkaTestkit % "test",
        Dependencies.scalaTest % "test"))

val scynapseTest = (project in file("scynapse-test"))
  .dependsOn(scynapseCore)
  .settings(moduleSettings: _*)
  .settings(
      libraryDependencies ++= Seq(
        Dependencies.axonTest,
        Dependencies.hamcrest,
        Dependencies.scalaTest
      ))
