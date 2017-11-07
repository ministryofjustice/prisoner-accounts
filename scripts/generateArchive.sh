#!/bin/bash

APPLICATION_NAME=${1}

cd $PWD

mkdir dist

cp build/libs/prisoner-accounts-1.0-SNAPSHOT.jar dist/prisoner-accounts.jar

zip -r ${APPLICATION_NAME}.zip dist
