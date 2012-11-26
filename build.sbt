organization := "com.thenewmotion"

name := "scynapse"

scalaVersion := "2.10.0-RC2"

libraryDependencies ++= Seq(
  "org.axonframework" % "axon-core" % "2.0-m3",
  "org.specs2"        % "specs2_2.10.0-RC2" % "1.12.2" % "test"
)

releaseSettings

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