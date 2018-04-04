enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

name := "Wallet-webservice"

version := "0.1"

scalaVersion := "2.12.5"

mainClass in Compile := Some("com.casino.WalletWebservice")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0",
  "com.typesafe.akka" %% "akka-http" % "10.1.0",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",

  "com.typesafe.akka" %% "akka-persistence" % "2.5.11",
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.83",
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % "0.83" % Test
)

