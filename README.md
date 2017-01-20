# [GeoTrellis PointCloud Demo]

## Build

### PDAL Build
 * Example scripts can be found [here](./scripts/other).

### PDAL JNI Binaries build
 * README.md in the following directory: https://github.com/pomadchin/PDAL/tree/feature/pdal-jni/java

### This project build
 * `./sbt assembly`
 * As the only argument [com.azavea.PackedPointCount](https://github.com/pomadchin/geotrellis-pdal-benchmark/blob/master/src/main/scala/com/azavea/PackedPointCount.scala) accepts an HDFS path to files.

## Run

After everything installed on all Spark nodes, run your job:

```bash
spark-submit \
  --conf spark.driver.extraJavaOptions="-Djava.library.path=/usr/lib/jni/" \
  --class com.azavea.PackedPointCount \
  geotrellis-pdal-assembly-0.1.0-SNAPSHOT.jar hdfs://{PATH}/
```

Instead of providing `-Djava.library.path` you may provide `$LD_LIBRARY_PATH` or `$DYLD_LIBRARY_PATH`.

## Makefile

| Command          | Description
|------------------|------------------------------------------------------------|
|local-run         |Run benchmark job locally                                   |
|upload-code       |Upload code and scripts to S3                               |
|create-cluster    |Create EMR cluster with configurations                      |
|run               |Add benchmar step to running cluster                        |
|wait              |Wait for last step to finish                                |
|proxy             |Create SOCKS proxy for active cluster                       |
|ssh               |SSH into cluster master                                     |
|get-logs          |Get spark history logs from active cluster                  |
|update-route53    |Update Route53 DNS record with active cluster ip            |
|clean             |Clean local project                                         |


## Running on EMR

_Requires_: Reasonably up to date [`aws-cli`](https://aws.amazon.com/cli/).

EMR boostrup script would build PDAL wiht JNI bindings on each node.

### Configuration

 - [config-aws.mk](./config-aws.mk) AWS credentials, S3 staging bucket, subnet, etc
 - [config-emr.mk](./config-emr.mk) EMR cluster type and size
 - [config-run.mk](./config-run.mk) Ingest step parameters

You will need to modify `config-aws.mk` to reflect your credentials and your VPC configuration. `config-emr.mk` and `config-ingest.mk` have been configured with an area over Japan. Be especially aware that as you change instance types `config-emr.mk` parameters like `EXECUTOR_MEMORY` and `EXECUTOR_CORES` need to be reviewed and likely adjusted.

### Run the step

```
make upload-code && make run && make proxy
```

## Licence

* Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
