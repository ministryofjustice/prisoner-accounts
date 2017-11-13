FROM openjdk:8-alpine
MAINTAINER Mike Jackson <michael.jackson@digital.justice.gov.uk>

COPY build/libs/prisoner-accounts-1.0-SNAPSHOT.jar /root/prisoner-accounts.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/root/prisoner-accounts.jar"]
