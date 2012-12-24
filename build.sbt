organization := "com.thenewmotion"

name := "scynapse"

scalaVersion := "2.10.0-RC5"

libraryDependencies ++= Seq(
  "org.axonframework" % "axon-core" % "2.0-rc2",
  "org.specs2"        %% "specs2" % "1.13" % "test"
)

releaseSettings

resolvers ++= Seq(
    "Releases"  at "http://nexus.thenewmotion.com/content/repositories/releases",
    "Snapshots" at "http://nexus.thenewmotion.com/content/repositories/snapshots")

publishTo <<= version { (v: String) =>
  val nexus = "http://nexus.thenewmotion.com/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots-public")
  else                             Some("releases"  at nexus + "content/repositories/releases-public")
}

publishMavenStyle := true

pomExtra :=
<licenses>
    <license>
        <name>Apache License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
    </license>
</licenses>

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
