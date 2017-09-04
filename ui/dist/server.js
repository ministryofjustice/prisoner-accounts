const http = require('http');

const config = require('./server/config');
const log = require('./server/log');

const makeDB = require('./server/db');
const makeApp = require('./server/app');

const db = makeDB();
const app = makeApp(config, log, db);

const server = http.createServer(app);
server.listen(config.port, () => log.info({addr: server.address()}, 'HMPPS Reference Data Manager Server listening'));
