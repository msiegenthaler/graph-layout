name := "graph-layout"

organization := "ch.inventsoft.graph"

scalaVersion := "2.11.2"

version := "1.0.2"

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/msiegenthaler/graph-layout"))



libraryDependencies += "com.assembla.scala-incubator" %% "graph-core" % "1.9.0"


libraryDependencies += "org.specs2" %% "specs2" % "2.4.2" % "test"

libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.6" % "test"



publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false}

pomExtra := (
  <scm>
    <url>git@github.com:msiegenthaler/graph-layout</url>
    <connection>scm:git:git@github.com:msiegenthaler/graph-layout</connection>
  </scm>
    <developers>
      <developer>
        <id>msiegenthaler</id>
        <name>Mario Siegenthaler</name>
        <url>https://github.com/msiegenthaler</url>
      </developer>
    </developers>)
