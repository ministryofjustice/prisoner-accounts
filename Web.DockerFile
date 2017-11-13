FROM node:boron-alpine
MAINTAINER Matt Smith <matt.smith@digital.justice.gov.uk>

# use changes to package.json to force Docker not to use the cache
# when we change our application's nodejs dependencies:
ADD ./ui/dist/package.json /tmp/package.json
RUN cd /tmp && npm install --production
RUN mkdir -p /opt/app && cp -a /tmp/node_modules /opt/app/

# From here we load our application's code in, therefore the previous docker
# "layer" thats been cached will be used if possible
WORKDIR /opt/app
ADD ./ui/dist /opt/app

EXPOSE 3000

CMD ["npm", "start"]
