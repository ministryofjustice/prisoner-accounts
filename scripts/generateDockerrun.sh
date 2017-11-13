#!/bin/bash

BUILD_VERSION=${1}

cd $PWD

cat <<- _EOF_ > Dockerrun.aws.json
{
  "AWSEBDockerrunVersion": 2,
  "containerDefinitions": [
    {
      "name": "prisoner-accounts-web",
      "image": "mojdigitalstudio/prisoner-accounts-web:${BUILD_VERSION}",
      "portMappings": [
        {
          "hostPort": 3000,
          "containerPort": 3000
        }
      ]
    },
    {
      "name": "prisoner-accounts-service",
      "image": "mojdigitalstudio/prisoner-accounts-service:${BUILD_VERSION}",
      "portMappings": [
        {
          "hostPort": 8080,
          "containerPort": 8080
        }
      ]
    }
  ]
}

_EOF_
