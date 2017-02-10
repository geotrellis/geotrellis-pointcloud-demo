export EMR_TAG := ${USER}
export NAME := PointCloud Benchmark ${EMR_TAG}
export MASTER_INSTANCE:=m3.xlarge
export MASTER_PRICE := 0.5
export WORKER_INSTANCE:=m3.2xlarge
export WORKER_PRICE := 0.5
export WORKER_COUNT := 1
export USE_SPOT := true
