name := "pointcloud-server"

libraryDependencies ++= Seq(
  "org.locationtech.geotrellis" %% "geotrellis-spark"      % Version.geotrellis,
  "org.locationtech.geotrellis" %% "geotrellis-s3"         % Version.geotrellis,
  "org.locationtech.geotrellis" %% "geotrellis-pointcloud" % Version.geotrellis,
  "com.typesafe.akka" %% "akka-actor"            % Version.akka,
  "com.typesafe.akka" %% "akka-http-core"         % "2.4.7",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.7",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.7",
  "org.apache.spark" %% "spark-core"    % Version.spark,
  "org.apache.hadoop" % "hadoop-client" % Version.hadoop,
  "org.scalatest"    %% "scalatest"     % "3.0.0" % "test",
  "com.iheart"       %% "ficus"         % "1.4.0"
)
