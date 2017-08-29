#!/usr/bin/env bash

./gradlew build
docker build -t prisoner_accounts .

# To run within Docker:
# docker run -d prisoner_accounts
