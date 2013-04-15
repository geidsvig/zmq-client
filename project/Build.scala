import sbt._
import Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions}
 
object ZmqClientBuild extends Build {
  val Organization = "com.geidsvig"
  val Version      = "1.0"
  val ScalaVersion = "2.9.2"

  val appDependencies = Dependencies.zmqClientKernel
  libraryDependencies ++= Dependencies.zmqClientKernel
 
  lazy val ZmqClientKernel = Project(
    id = "zmq-client-kernel",
    base = file("."),
    settings = defaultSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
      libraryDependencies ++= Dependencies.zmqClientKernel,
      distJvmOptions in Dist := "-Xms2G -Xmx4G -Xss1M -XX:+UseParallelGC -XX:GCTimeRatio=19",
      outputDirectory in Dist := file("target/zmq-client-dist")
    )
  )
 
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version      := Version,
    scalaVersion := ScalaVersion,
    crossPaths   := false,
    organizationName := "geidsvig",
    libraryDependencies ++= Dependencies.zmqClientKernel
  )
  
  lazy val defaultSettings = buildSettings ++ Seq(
    resolvers ++= Seq(
      "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
      "repo.novus snaps" at "http://repo.novus.com/snapshots/",
      "Sonatype Repo" at "https://oss.sonatype.org/content/groups/scala-tools/"),
 
    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Dependencies.zmqClientKernel
 
  )
}
 
object Dependencies {
  import Dependency._

  val test = Seq(scalaTest)
 
  val zmqClientKernel = Seq(
    akkaKernel, akkaSlf4j, akkaRemote, jzmq
  ) ++ test
}
 
object Dependency {
  // Versions
  object V {
    val Akka      = "2.0.4"
  }

  val akkaKernel        = "com.typesafe.akka" % "akka-kernel"        % V.Akka
  val akkaSlf4j         = "com.typesafe.akka" % "akka-slf4j"         % V.Akka
  val akkaRemote        = "com.typesafe.akka" % "akka-remote"        % V.Akka

  val jzmq              = "org.zeromq"        % "jzmq"               % "1.0.0"

  val scalaTest         = "org.scalatest"     %% "scalatest"         % "1.8" % "test"
  
}

