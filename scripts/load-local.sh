#!/usr/bin/env bash

# hadoop fs -mkdir -p /data/test/a
# hadoop fs -mkdir -p /data/test/b
# cd /tmp; aws s3 cp s3://geotrellis-test/ross/lidar_CO_Flood_before_after.zip .
# unzip lidar_CO_Flood_before_after.zip
# hadoop fs -copyFromLocal 13*.las /data/test/a
# hadoop fs -copyFromLocal 30*.las /data/test/b

# mkdir -p /tmp/SHCZO_Dec10
# mkdir -p /tmp/SHCZO_Jul10

wget -r --no-parent -P ./data https://cloud.sdsc.edu/v1/AUTH_opentopography/PC_Bulk/SHCZO_Dec10/ot_SH2_238500_4499000.laz

mv ./data/cloud.sdsc.edu/v1/AUTH_opentopography/PC_Bulk/* ./data/ && rm -r ./data/cloud.sdsc.edu/

# hadoop fs -copyFromLocal /tmp/SHCZO* /data/test/
