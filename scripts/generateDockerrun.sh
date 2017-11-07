#!/bin/bash

VERSION="$1"

cd $PWD

mkdir dist

cat <<- _EOF_ > dist/Dockerrun.aws.json
{
  "AWSEBDockerrunVersion": "2",
  "volumes": [
    {
      "name": "prisoner-accounts-service",
      "host": {
        "sourcePath": "service/"
      }
    }
  ],
  "containerDefinitions": [
    {
      "name": "prisoner-accounts-service",
      "image": "openjdk:alpine",
      "essential": true,
      "environment": [
        {
          "name": "VERSION",
          "value": "${VERSION}"
        }
      ],
      "memory": 128,
      "portMappings": [
        {
          "hostPort": 8080,
          "containerPort": 8080
        }
      ],
      "mountPoints": [
        {
          "sourceVolume": "prisoner-accounts-service",
          "containerPath": "/usr/src/myapp"
        },
        {
          "sourceVolume": "awseb-logs-prisoner-accounts-service",
          "containerPath": "/var/log/prisoner-accounts-service"
        }
      ],
      "command": ["java", "-jar", "/usr/src/myapp/prisoner-accounts.jar"]
    }
  ]
}
_EOF_