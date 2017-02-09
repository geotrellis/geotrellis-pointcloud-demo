# Query parameters
export DRIVER_MEMORY := 20000M
export DRIVER_CORES := 2
export EXECUTOR_MEMORY := 20000M
export EXECUTOR_CORES := 2
export YARN_OVERHEAD := 1500

# export DRIVER_MEMORY := 4200M
# export DRIVER_CORES := 2
# export EXECUTOR_MEMORY := 3000M
# export EXECUTOR_CORES := 1
# export EXECUTOR_COUNT := 129
# export YARN_OVERHEAD := 300
export POINTCLOUD_PATH := /data/test/a
export LOCAL_POINTCLOUD_PATH := file:///${PWD}/data/raw
export LOCAL_CATALOG := file:///${PWD}/data/catalog/
export S3_CATALOG := s3://geotrellis-test/pointcloud-demo/catalog-v2
export S3_POINTCLOUD_PATH := s3://geotrellis-test/pointcloud-demo
