#!/bin/bash

APPLICATION_NAME=${1}
BUILD_VERSION=${2}

cd $PWD

./scripts/generateDockerrun.sh ${BUILD_VERSION}
./scripts/generateArchive.sh ${APPLICATION_NAME}

~/.local/bin/eb deploy --process --verbose
