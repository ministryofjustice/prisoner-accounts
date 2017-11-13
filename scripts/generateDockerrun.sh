#!/bin/bash

BUILD_VERSION=${1}

cd $PWD

cat <<- _EOF_ > Dockerrun.aws.json
{
  "AWSEBDockerrunVersion": "2",
  "volumes": [
    {
      "name": "prisoner-accounts-web",
      "host": {
        "sourcePath": "ui/dist"
      }
    },
    {
      "name": "prisoner-accounts-service",
      "host": {
        "sourcePath": "build/libs/"
      }
    }
  ],
  "containerDefinitions": [
    {
      "name": "prisoner-accounts-web",
      "Image": {
        "Name": "mojdigitalstudio/prisoner-accounts-web:${BUILD_VERSION}",
        "Update": "true"
      },
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
          "sourceVolume": "prisoner-accounts-web",
          "containerPath": "/home/node/app"
        },
        {
          "sourceVolume": "awseb-logs-prisoner-accounts-web",
          "containerPath": "/var/log/prisoner-accounts-web"
        }
      ],
      "workingDirectory": "/home/node/app",
      "command": ["npm", "start"]
    },
    {
      "name": "prisoner-accounts-service",
      "Image": {
        "Name": "mojdigitalstudio/prisoner-accounts-service:${BUILD_VERSION}",
        "Update": "true"
      },
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
          "sourceVolume": "prisoner-accounts-service",
          "containerPath": "/usr/src/myapp"
        },
        {
          "sourceVolume": "awseb-logs-prisoner-accounts-service",
          "containerPath": "/var/log/prisoner-accounts-service"
        }
      ],
      "command": ["java", "-jar", "/usr/src/myapp/prisoner-accounts-1.0-SNAPSHOT.jar"]
    }
  ]
}

_EOF_
