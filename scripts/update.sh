#!/bin/bash

set -e

if [[ -n "${PC_DEMO_DEBUG}" ]]; then
    set -x
fi

function usage() {
    echo -n \
         "Usage: $(basename "$0")

Builds and pulls container images using docker-compose.
"
}

if [ "${BASH_SOURCE[0]}" = "${0}" ]
then
    if [ "${1:-}" = "--help" ]
    then
        usage
    else
        docker-compose -f docker-compose.yml pull
        
        # Build React application
        docker-compose \
            -f docker-compose.yml \
            run --rm --no-deps pc-assets \
            install --quiet

        docker-compose \
            -f docker-compose.yml \
            run --rm --no-deps pc-assets

        # Update scala dependencies
        docker-compose \
            -f docker-compose.yml \
            run --rm --no-deps pc-api-server update
    fi
fi
