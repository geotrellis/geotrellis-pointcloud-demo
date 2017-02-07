#!/usr/bin/env bash

hadoop fs -mkdir -p /data/test/JRB_10_Jul && hadoop fs -mkdir -p /data/test/JRB_10_Mar

hdfs dfs -cp -p s3://geotrellis-test/pointcloud-demo/JRB_10_Jul_subset/* /data/test/JRB_10_Jul
hdfs dfs -cp -p s3://geotrellis-test/pointcloud-demo/JRB_10_Mar_subset/* /data/test/JRB_10_Mar
