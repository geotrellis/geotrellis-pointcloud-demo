lazy val commonSettings = Seq(
  organization := "com.azavea",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-Yinline-warnings",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials",
    "-feature"),
  outputStrategy := Some(StdoutOutput),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  fork := true,
  fork in Test := true,
  parallelExecution in Test := false,
  javaOptions ++= Seq(s"-Djava.library.path=${Environment.ldLibraryPath}", "-Xmx10G"),
  test in assembly := {},
  ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
  resolvers ++=
    Seq(
      "geosolutions" at "http://maven.geo-solutions.it/",
      "osgeo" at "http://download.osgeo.org/webdav/geotools/"
    ),

  assemblyMergeStrategy in assembly := {
    case "reference.conf" | "application.conf" => MergeStrategy.concat
    case "META-INF/MANIFEST.MF" | "META-INF\\MANIFEST.MF" => MergeStrategy.discard
    case "META-INF/ECLIPSEF.RSA" | "META-INF/ECLIPSEF.SF" => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
)

lazy val root =
  Project("root", file("."))
    .aggregate(server, ingest)
    .settings(commonSettings: _*)

lazy val server =
  (project in file("server"))
    .settings(commonSettings: _*)

lazy val ingest =
  (project in file("ingest"))
    .settings(commonSettings: _*)    
