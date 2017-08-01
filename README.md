# prisoner-accounts

## Provides an API to manage, and report on, prisoner accounts and transactions
This application is a demonstrator and uses an in-memory H2 database for persistence.

Information will be lost over application restarts.

## Building and Running the application
Build using gradle:

`./gradlew build`

Run using

`java -jar build/libs/prisoner-accounts-1.0-SNAPSHOT.jar`

By default the application will run on port 8080. To run on a different port:

`java -jar -Dserver.port=8280 build/libs/prisoner-accounts-1.0-SNAPSHOT.jar`

or

`SERVER_PORT=8123 java -jar build/libs/prisoner-accounts-1.0-SNAPSHOT.jar`

or

`java -jar build/libs/prisoner-accounts-1.0-SNAPSHOT.jar --server.port=8800`


### Swagger UI
Swagger UI is exposed under /swagger-ui.html

### Swagger json
Swagger json is exposed under /v2/api-docs