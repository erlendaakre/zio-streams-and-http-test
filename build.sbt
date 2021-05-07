name := "zio-streams-and-http-test"
version := "0.1"
scalaVersion := "2.13.4"

libraryDependencies += "dev.zio" %% "zio" % "1.0.7"
libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.7"
libraryDependencies += "dev.zio" %% "zio-process" % "0.3.0"
libraryDependencies += "dev.zio" %% "zio-json" % "0.1.4"
libraryDependencies += "io.d11" % "zhttp" % "1.0.0-SNAPSHOT-RC10"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"