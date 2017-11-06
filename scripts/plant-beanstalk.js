const fs = require('fs');
const path = require('path');

const version = process.argv[2];

if (!version) {
  throw new Error("Missing <version> argument");
}

const dockerrun = {
    AWSEBDockerrunVersion: "2",
    volumes: [
        {
            name: "prisoner-accounts-web",
            host: {
                sourcePath: "ui/dist"
            }
        },
        {
            name: "prisoner-accounts-service",
            host: {
                sourcePath: "build/libs/"
            }
        }
    ],
    containerDefinitions: [
        {
            name: "prisoner-accounts-web",
            image: "node:boron-alpine",
            environment: [
                {
                    name: "ACCOUNT_SERVICE_HOST",
                    value: "prisoner-accounts-service"
                },
                {
                    name: "ACCOUNT_SERVICE_PORT",
                    value: "8080"
                }
            ],
            essential: true,
            memory: 128,
            portMappings: [
                {
                  hostPort: 80,
                  containerPort: 3000
                }
            ],
            links: [
                "prisoner-accounts-service"
            ],
            mountPoints: [
                {
                    sourceVolume: "prisoner-accounts-web",
                    containerPath: "/opt/app"
                },
                {
                    sourceVolume: "awseb-logs-prisoner-accounts-web",
                    containerPath: "/var/log/prisoner-accounts-web"
                }
            ],
            command: [
                "npm", "start"
            ]
        },
    ]
};

const output = JSON.stringify(dockerrun, null, 2);

console.log(output);

fs.writeFileSync(path.resolve(__dirname, '../Dockerrun.aws.json'), output);