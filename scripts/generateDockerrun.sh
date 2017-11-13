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
      "environment": [
        {
          "name": "ACCOUNT_SERVICE_HOST",
          "value": "prisoner-accounts-service"
        },
        {
          "name": "ACCOUNT_SERVICE_PORT",
          "value": "8080"
        }
      ],
      "essential": true,
      "memory": 128,
      "portMappings": [
        {
          "hostPort": 80,
          "containerPort": 3000
        }
      ],
      "links": [
        "prisoner-accounts-service"
      ],
      "mountPoints": [
        {
          "sourceVolume": "awseb-logs-prisoner-accounts-web",
          "containerPath": "/var/log/prisoner-accounts-web"
        }
      ]
    },
    {
      "name": "prisoner-accounts-service",
      "image": "mojdigitalstudio/prisoner-accounts-service:${BUILD_VERSION}",
      "essential": true,
      "memory": 128,
      "portMappings": [
        {
          "hostPort": 8080,
          "containerPort": 8080
        }
      ],
      "mountPoints": [
        {
          "sourceVolume": "awseb-logs-prisoner-accounts-service",
          "containerPath": "/var/log/prisoner-accounts-service"
        }
      ]
    }
  ]
}

_EOF_
