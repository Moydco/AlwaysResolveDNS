name := "SCALA-DNS"

version := "1.14"

scalaVersion := "2.11.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

//resolvers += "repo.codahale.com" at "http://repo.codahale.com"

libraryDependencies += "io.netty" % "netty-all" % "4.0.23.Final"

//libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3"
 
//libraryDependencies += "com.typesafe.akka" % "akka-remote" % "2.0.3"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.0"

libraryDependencies += "com.typesafe" % "config" % "1.2.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.4.1"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.4.2"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "0.3.16"

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.2"

libraryDependencies += "com.rabbitmq" % "amqp-client" % "3.3.5"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "commons-codec" % "commons-codec" % "1.10"