name := """UglyChat"""

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies += "org.webjars" % "foundation" % "5.5.2"
libraryDependencies += "org.webjars" % "webjars-play_2.11" % "2.3.0-3"

lazy val root = project.in(file(".")).enablePlugins(PlayScala)
