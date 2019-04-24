#!/bin/bash

set -e

if [[ -n "${PC_DEMO_DEBUG}" ]]; then
    set -x
fi

if [[ -n "${TRAVIS_COMMIT}" ]]; then
    VERSION="${TRAVIS_COMMIT:0:7}"
else
    VERSION=$(git rev-parse --short HEAD)
fi

function usage() {
    echo -n \
         "Usage: $(basename "$0")

Builds container images using docker-compose.
"
}

if [ "${BASH_SOURCE[0]}" = "${0}" ]
then
    if [ "${1:-}" = "--help" ]
    then
        usage
    else

        # Build React application

        echo "Building static bundle..."
        VERSION="${VERSION}" docker-compose \
            -f docker-compose.yml \
            run --rm --no-deps pc-assets
    fi
fi
