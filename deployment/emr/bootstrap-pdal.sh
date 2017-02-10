#!/bin/bash

# This scripts bootstraps each node in the the EMR cluster to install PDAL.

# Ensure that config files are copied to S3
aws s3 cp /etc/hadoop/conf/core-site.xml s3://geotrellis-test/pdal-test/
aws s3 cp /etc/hadoop/conf/yarn-site.xml s3://geotrellis-test/pdal-test/

# Install minimal explicit dependencies.
sudo yum -y install git geos-devel libcurl-devel cmake libtiff-devel

# Install binaries for pdal
cd /mnt
aws s3 cp s3://geotrellis-demo/emr/pdal-binaries-v1.tar.gz .
tar -xf pdal-binaries-v1.tar.gz
cd pdal-binaries
sudo mv pdal /usr/local/bin/pdal
sudo mv gdal /usr/local/share/gdal
cd lib
sudo cp -r * /usr/local/lib
