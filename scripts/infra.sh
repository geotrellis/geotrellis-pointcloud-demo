#!/bin/bash

set -e

if [[ -n "${PC_DEMO_DEBUG}" ]]; then
    set -x
fi

set -u

DIR="$(dirname "$0")"

function usage() {
    echo -n \
"Usage: $(basename "$0") COMMAND OPTION[S]
Execute Terraform subcommands with remote state management.
"
}

if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    if [ "${1:-}" = "--help" ]; then
        usage
    else
        TERRAFORM_DIR="${DIR}/../deployment/terraform"
        echo
        echo "Attempting to deploy application version [${TRAVIS_COMMIT:0:7}]..."
        echo "-----------------------------------------------------"
        echo
    fi

    if [[ -n "${PC_DEMO_SETTINGS_BUCKET}" ]]; then
        pushd "${TERRAFORM_DIR}"

        case "${1}" in
            plan)
                rm -rf .terraform/ terraform.tfstate*
                terraform init \
                    -backend-config="bucket=${PC_DEMO_SETTINGS_BUCKET}" \
                    -backend-config="key=terraform/pointcloud/state"

                terraform plan \
                          -var="image_version=\"${TRAVIS_COMMIT:0:7}\"" \
                          -var="remote_state_bucket=\"${PC_DEMO_SETTINGS_BUCKET}\"" \
                          -out="${PC_DEMO_SETTINGS_BUCKET}.tfplan"
                ;;
            apply)
                terraform apply "${PC_DEMO_SETTINGS_BUCKET}.tfplan"
                ;;
            *)
                echo "ERROR: I don't have support for that Terraform subcommand!"
                exit 1
                ;;
        esac

        popd
    else
        echo "ERROR: No PC_DEMO_SETTINGS_BUCKET variable defined."
        exit 1
    fi
fi
