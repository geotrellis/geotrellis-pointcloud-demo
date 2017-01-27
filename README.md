# GeoTrellis PointCloud Demo

### This project build
 * `./sbt ingest/assembly && ./sbt server/assembly` 

## Makefile

| Command          | Description
|------------------|------------------------------------------------------------|
|local-run         |Run benchmark job locally                                   |
|upload-code       |Upload code and scripts to S3                               |
|create-cluster    |Create EMR cluster with configurations                      |
|load-hdfs         |Load input source into hdfs                                 |
|ingest-idw        |IDW ingest with or without pyramiding                       |
|ingest-tin        |TIN ingest with or without pyramiding                       |
|ingest-pc         |Raw PointCloud ingest without pyramiding yet                |
|local-ingest-idw  |Local IDW ingest with or without pyramiding                 |
|run-server        |Run server on EMR master                                    |
|local-run-server  |Run server locally                                          |
|wait              |Wait for last step to finish                                |
|proxy             |Create SOCKS proxy for active cluster                       |
|ssh               |SSH into cluster master                                     |
|get-logs          |Get spark history logs from active cluster                  |
|update-route53    |Update Route53 DNS record with active cluster ip            |
|clean             |Clean local project                                         |

## Ingest jobs options

[IngestConf.scala](https://github.com/pomadchin/geotrellis-pointcloud-demo/blob/master/ingest/src/main/scala/com/azavea/pointcloud/ingest/conf/IngestConf.scala#L7-L22)

| Command          | Description
|------------------|------------------------------------------------------------|
|inputPath         |default: /data/test                                         |
|catalogPath       |default: /data/catalog                                      |
|layerName         |default: elevation                                          |
|persist           |default: true                                               |
|pyramid           |default: true                                               |
|zoomed            |default: true                                               |
|cellSize          |default: 0.5,0.5                                            |
|numPartitions     |default: 5000                                               |
|minZoom           |default: 7                                                  |
|maxValue          |default: None                                               |
|destCrs           |default: EPSG:3857                                          |
|extent            |default: None                                               |
|inputCrs          |default: None                                               |
|testOutput        |default: None                                               |


## Running on EMR

_Requires_: Reasonably up to date [`aws-cli`](https://aws.amazon.com/cli/).

EMR boostrup script would build PDAL with JNI bindings on each node.

### Configuration

 - [config-aws.mk](./config-aws.mk) AWS credentials, S3 staging bucket, subnet, etc
 - [config-emr.mk](./config-emr.mk) EMR cluster type and size
 - [config-run.mk](./config-run.mk) Ingest step parameters

You will need to modify `config-aws.mk` to reflect your credentials and your VPC configuration. `config-emr.mk` and `config-ingest.mk` have been configured with an area over Japan. Be especially aware that as you change instance types `config-emr.mk` parameters like `EXECUTOR_MEMORY` and `EXECUTOR_CORES` need to be reviewed and likely adjusted.

### EMR pipeline example

```bash
make upload-code && make create-cluster
make load-hdfs # after launching cluster
make ingest-idw
make run-server # after completing ingest
```

## Licence

* Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
