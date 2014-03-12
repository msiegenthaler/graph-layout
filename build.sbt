name := "graph-layout"

organization := "ch.inventsoft.graph"

scalaVersion := "2.10.3"

version := "1.0.0-SNAPSHOT"

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/msiegenthaler/graph-layout"))

resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/snapshots"



libraryDependencies += "com.assembla.scala-incubator" %% "graph-core" % "1.7.2"


libraryDependencies += "org.specs2" %% "specs2" % "2.3.10" % "test"

libraryDependencies += "com.github.axel22" %% "scalameter" % "0.4" % "test"



publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}