name := "pointcloud-ingest"

libraryDependencies ++= Seq(
  "org.locationtech.geotrellis" %% "geotrellis-spark"      % Version.geotrellis,
  "org.locationtech.geotrellis" %% "geotrellis-s3"         % Version.geotrellis,
  "org.locationtech.geotrellis" %% "geotrellis-pointcloud" % Version.geotrellis,
  "org.apache.spark" %% "spark-core"    % Version.spark,
  "org.apache.hadoop" % "hadoop-client" % Version.hadoop,
  "org.scalatest"    %% "scalatest"     % "3.0.0" % "test",
  "com.iheart"       %% "ficus"         % "1.4.0"
)
