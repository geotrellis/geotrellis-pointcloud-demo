#!/bin/bash

set -e

if [[ -n "${PC_DEMO_DEBUG}" ]]; then
    set -x
fi

if [[ -n "${TRAVIS_COMMIT}" ]]; then
    GIT_COMMIT="${TRAVIS_COMMIT:0:7}"
else
    GIT_COMMIT="$(git rev-parse --short HEAD)"
fi

function usage() {
    echo -n \
"Usage: $(basename "$0")
Publish images to Amazon ECR
"
}

if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    if [ "${1:-}" = "--help" ]; then
        usage
    else

        if [[ -n "${AWS_ECR_ENDPOINT}" ]]; then

            echo "Building Pointcloud Demo Server JAR..."
            docker-compose run --rm pc-api-server server/assembly

            echo "Building API Server and Nginx containers"
            GIT_COMMIT="${GIT_COMMIT}" \
            docker-compose -f docker-compose.yml \
                           -f docker-compose.test.yml build \
                              pc-nginx pc-api-server
            eval "$(aws ecr get-login --no-include-email)"
            docker tag "pointcloud-api-server:${GIT_COMMIT}" \
                "${AWS_ECR_ENDPOINT}/pointcloud-api-server:${GIT_COMMIT}"

            docker tag "pointcloud-nginx:${GIT_COMMIT}" \
                "${AWS_ECR_ENDPOINT}/pointcloud-nginx:${GIT_COMMIT}"

            docker push "${AWS_ECR_ENDPOINT}/pointcloud-api-server:${GIT_COMMIT}"
            docker push "${AWS_ECR_ENDPOINT}/pointcloud-nginx:${GIT_COMMIT}"
        else
            echo "ERROR: No AWS_ECR_ENDPOINT variable defined."
            exit 1
        fi

    fi
fi
