name := "pointcloud-ingest"

libraryDependencies ++= Seq(
  "org.locationtech.geotrellis" %% "geotrellis-spark"      % Version.geotrellis,
  "org.locationtech.geotrellis" %% "geotrellis-s3"         % Version.geotrellis,
  "com.azavea.geotrellis"       %% "geotrellis-pointcloud" % Version.pointcloud,
  "org.apache.spark" %% "spark-core"    % Version.spark,
  "org.apache.hadoop" % "hadoop-client" % Version.hadoop,
  "org.scalatest"    %% "scalatest"     % Version.scalaTest % "test",
  "com.iheart"       %% "ficus"         % Version.ficus
)
