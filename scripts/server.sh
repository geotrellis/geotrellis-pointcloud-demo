#!/bin/bash

set -e

if [[ -n "${PC_DEMO_DEBUG}" ]]; then
    set -x
fi

function usage() {
    echo -n \
         "Usage:
$(basename "$0")
Starts nginx using docker-compose.
"
}

if [ "${BASH_SOURCE[0]}" = "${0}" ]
then
    if [ "${1:-}" = "--help" ]
    then
        usage
    else
        docker-compose -f docker-compose.yml up pc-nginx pc-api-server
    fi
fi
